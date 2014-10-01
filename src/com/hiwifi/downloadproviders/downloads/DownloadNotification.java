package com.hiwifi.downloadproviders.downloads;

/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.Collection;
import java.util.HashMap;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.SparseLongArray;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.hiwifi.hiwifi.R;

/**
 * This class handles the updating of the Notification Manager for the cases
 * where there is an ongoing download. Once the download is complete (be it
 * successful or unsuccessful) it is no longer the responsibility of this
 * component to show the download in the notification manager.
 * 
 */
class DownloadNotification {

	private Context mContext;
	private NotificationManager mNotifManager;
	private HashMap<String, NotificationItem> mNotifications;

	/** Time when each {@link DownloadInfo#mId} was first shown. */
	private SparseLongArray mFirstShown = new SparseLongArray();

	static final String LOGTAG = "DownloadNotification";
	static final String WHERE_RUNNING = "(" + Downloads.COLUMN_STATUS
			+ " >= '100') AND (" + Downloads.COLUMN_STATUS + " <= '199') AND ("
			+ Downloads.COLUMN_VISIBILITY + " IS NULL OR "
			+ Downloads.COLUMN_VISIBILITY + " == '"
			+ Downloads.VISIBILITY_VISIBLE + "' OR "
			+ Downloads.COLUMN_VISIBILITY + " == '"
			+ Downloads.VISIBILITY_VISIBLE_NOTIFY_COMPLETED + "')";
	static final String WHERE_COMPLETED = Downloads.COLUMN_STATUS
			+ " >= '200' AND " + Downloads.COLUMN_VISIBILITY + " == '"
			+ Downloads.VISIBILITY_VISIBLE_NOTIFY_COMPLETED + "'";

	/**
	 * This inner class is used to collate downloads that are owned by the same
	 * application. This is so that only one notification line item is used for
	 * all downloads of a given application.
	 * 
	 */
	static class NotificationItem {
		// TODO: refactor to mNotifId and avoid building Uris based on it, since
		// they can overflow
		int mId; // This first db _id for the download for the app
		long mTotalCurrent = 0;
		long mTotalTotal = 0;
		int mTitleCount = 0;
		String mPackageName; // App package name
		String mDescription;
		String[] mTitles = new String[2]; // download titles.
		String mPausedText = null;

		/*
		 * Add a second download to this notification item.
		 */
		void addItem(String title, long currentBytes, long totalBytes) {
			mTotalCurrent += currentBytes;
			if (totalBytes <= 0 || mTotalTotal == -1) {
				mTotalTotal = -1;
			} else {
				mTotalTotal += totalBytes;
			}
			if (mTitleCount < 2) {
				mTitles[mTitleCount] = title;
			}
			mTitleCount++;
		}
	}

	/**
	 * Constructor
	 * 
	 * @param ctx
	 *            The context to use to obtain access to the Notification
	 *            Service
	 */
	DownloadNotification(Context ctx, SystemFacade systemFacade) {
		mContext = ctx;
		mNotifManager = (NotificationManager) mContext
				.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotifications = new HashMap<String, NotificationItem>();
	}

	/*
	 * Update the notification ui.
	 */
	public void updateNotification(Collection<DownloadInfo> downloads) {
		updateActiveNotification(downloads);
		updateCompletedNotification(downloads);
	}

	private void updateActiveNotification(Collection<DownloadInfo> downloads) {
		// Collate the notifications
		mNotifications.clear();
		for (DownloadInfo download : downloads) {
			if (!isActiveAndVisible(download)) {
				continue;
			}
			String packageName = download.mPackage;
			long max = download.mTotalBytes;
			long progress = download.mCurrentBytes;
			long id = download.mId;
			String title = download.mTitle;
			if (title == null || title.length() == 0) {
				title = mContext.getResources().getString(
						R.string.download_unknown_title);
			}

			NotificationItem item;
			if (mNotifications.containsKey(packageName)) {
				item = mNotifications.get(packageName);
				item.addItem(title, progress, max);
			} else {
				item = new NotificationItem();
				item.mId = (int) id;
				item.mPackageName = packageName;
				item.mDescription = download.mDescription;
				item.addItem(title, progress, max);
				mNotifications.put(packageName, item);
			}
			if (download.mStatus == Downloads.STATUS_QUEUED_FOR_WIFI
					&& item.mPausedText == null) {
				item.mPausedText = mContext.getResources().getString(
						R.string.notification_need_wifi_for_size);
			}
		}

		// Add the notifications
		for (NotificationItem item : mNotifications.values()) {
			// Build the notification object
			final NotificationCompat.Builder builder = new NotificationCompat.Builder(
					mContext);

			boolean hasPausedText = (item.mPausedText != null);
			int iconResource = android.R.drawable.stat_sys_download;
			if (hasPausedText) {
				iconResource = android.R.drawable.stat_sys_warning;
			}
			builder.setSmallIcon(iconResource);
			builder.setOngoing(true);

			// set notification "when" to be first time this DownloadInfo.mId
			// was encountered, which avoids fighting with other notifs.
			long firstShown = mFirstShown.get(item.mId, -1);
			if (firstShown == -1) {
				firstShown = System.currentTimeMillis();
				mFirstShown.put(item.mId, firstShown);
			}
			builder.setWhen(firstShown);

			boolean hasContentText = false;
			StringBuilder title = new StringBuilder(item.mTitles[0]);
			if (item.mTitleCount > 1) {
				title.append(mContext
						.getString(R.string.notification_filename_separator));
				title.append(item.mTitles[1]);
				if (item.mTitleCount > 2) {
					title.append(mContext.getString(
							R.string.notification_filename_extras,
							new Object[] { Integer
									.valueOf(item.mTitleCount - 2) }));
				}
			} else if (!TextUtils.isEmpty(item.mDescription)) {
				builder.setContentText(item.mDescription);
				hasContentText = true;
			}
			builder.setContentTitle(title);

			if (hasPausedText) {
				builder.setContentText(item.mPausedText);
			} else {
				builder.setProgress((int) item.mTotalTotal,
						(int) item.mTotalCurrent, item.mTotalTotal == -1);
				if (hasContentText) {
					builder.setContentInfo(buildPercentageLabel(mContext,
							item.mTotalTotal, item.mTotalCurrent));
				}
			}

			Intent intent = new Intent(Constants.ACTION_LIST);
			intent.setClassName(mContext.getPackageName(),
					DownloadReceiver.class.getName());
			intent.setData(ContentUris.withAppendedId(
					Downloads.ALL_DOWNLOADS_CONTENT_URI, item.mId));
			intent.putExtra("multiple", item.mTitleCount > 1);

			builder.setContentIntent(PendingIntent.getBroadcast(mContext, 0,
					intent, 0));

			mNotifManager.notify(item.mId, builder.build());
		}
	}

	private void updateCompletedNotification(Collection<DownloadInfo> downloads) {
		for (DownloadInfo download : downloads) {
			if (!isCompleteAndVisible(download)) {
				continue;
			}
			notificationForCompletedDownload(download.mId, download.mTitle,
					download.mStatus, download.mDestination, download.mLastMod);
		}
	}

	void notificationForCompletedDownload(long id, String title, int status,
			int destination, long lastMod) {
		// Add the notifications
		NotificationCompat.Builder builder = new NotificationCompat.Builder(
				mContext);
		builder.setSmallIcon(android.R.drawable.stat_sys_download_done);
		if (title == null || title.length() == 0) {
			title = mContext.getResources().getString(
					R.string.download_unknown_title);
		}
		Uri contentUri = ContentUris.withAppendedId(
				Downloads.ALL_DOWNLOADS_CONTENT_URI, id);
		String caption;
		Intent intent;
		if (Downloads.isStatusError(status)) {
			caption = mContext.getResources().getString(
					R.string.notification_download_failed);
			intent = new Intent(Constants.ACTION_LIST);
		} else {
			caption = mContext.getResources().getString(
					R.string.notification_download_complete);
			intent = new Intent(Constants.ACTION_OPEN);
		}
		intent.setClassName(mContext.getPackageName(),
				DownloadReceiver.class.getName());
		intent.setData(contentUri);

		builder.setWhen(lastMod);
		builder.setContentTitle(title);
		builder.setContentText(caption);
		builder.setContentIntent(PendingIntent.getBroadcast(mContext, 0,
				intent, 0));

		intent = new Intent(Constants.ACTION_HIDE);
		intent.setClassName(mContext.getPackageName(),
				DownloadReceiver.class.getName());
		intent.setData(contentUri);
		builder.setDeleteIntent(PendingIntent.getBroadcast(mContext, 0, intent,
				0));

		mNotifManager.notify((int) id, builder.build());
	}

	private boolean isActiveAndVisible(DownloadInfo download) {
		return 100 <= download.mStatus && download.mStatus < 200
				&& download.mVisibility != Downloads.VISIBILITY_HIDDEN;
	}

	private boolean isCompleteAndVisible(DownloadInfo download) {
		return download.mStatus >= 200
				&& download.mVisibility == Downloads.VISIBILITY_VISIBLE_NOTIFY_COMPLETED;
	}

	private static String buildPercentageLabel(Context context,
			long totalBytes, long currentBytes) {
		if (totalBytes <= 0) {
			return null;
		} else {
			final int percent = (int) (100 * currentBytes / totalBytes);
			return "%"+percent;
		}
	}
}
