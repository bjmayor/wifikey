package com.seo.activity.setting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.seo.activity.base.BaseActivity;
import com.seo.model.ClientInfo;
import com.seo.wifikey.R;
import com.umeng.analytics.MobclickAgent;
import com.umeng.fb.FeedbackAgent;
import com.umeng.fb.model.Conversation;
import com.umeng.fb.model.DevReply;
import com.umeng.fb.model.Reply;
import com.umeng.fb.model.UserInfo;

public class FeedbackActivity extends BaseActivity {

	private Button submit;
	private EditText content;
	private EditText contact;
	private FeedbackAgent agent;
	private Conversation defaultConversation;
	private InputMethodManager im;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_feedback);
		agent = new FeedbackAgent(this);
		im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		initView();
	}

    @Override
    protected void onClickEvent(View paramView) {

    }

    @Override
    protected void findViewById() {

    }

    @Override
    protected void loadViewLayout() {

    }

    @Override
    protected void processLogic() {

    }

    @Override
    protected void setListener() {

    }

    private void initView() {
		contact = (EditText) findViewById(R.id.contact_info);
		content = (EditText) findViewById(R.id.feedback_content);
		content.requestFocus();
		submit = (Button) findViewById(R.id.submit_feedback);
		submit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				submitFeedbakToUmeng();
			}

		});

		uContact = ClientInfo.shareInstance().getUserContact();
		if (!TextUtils.isEmpty(uContact)) {
			contact.setText(uContact);
		}

	}

	private static final String KEY_UMENG_CONTACT_INFO_PLAIN_TEXT = "plain";
	private String fuck;
	private String uContact;

	private void submitFeedbakToUmeng() {
		String userContact = contact.getText().toString().trim();
		String errorInfo = content.getText().toString().trim();
		if (TextUtils.isEmpty(errorInfo)) {
			Toast.makeText(this, "请填写反馈信息", Toast.LENGTH_SHORT).show();
			return;
		}
//		if (TextUtils.isEmpty(userContact)) {
//			Toast.makeText(this, "请填写联系方式", 0).show();
//			return;
//		}
//		String feedbackContent = errorInfo + "\n     联系方式：" + userContact;
		defaultConversation = agent.getDefaultConversation();
		defaultConversation.addUserReply(errorInfo);
		UserInfo info = agent.getUserInfo();

		if (info == null) {
			info = new UserInfo();
		}
		Map<String, String> contact = info.getContact();
		if (contact == null) {
			contact = new HashMap<String, String>();
		}
		if (!userContact.equals(uContact) && TextUtils.isEmpty(userContact)) {
			contact.put(KEY_UMENG_CONTACT_INFO_PLAIN_TEXT, userContact);
			info.setContact(contact);
			agent.setUserInfo(info);
			ClientInfo.shareInstance().setUserContact(userContact);
		}
		sync();
		Toast.makeText(this, "提交成功", Toast.LENGTH_SHORT).show();
		this.finish();
	}

	private void sync() {
		Conversation.SyncListener listener = new Conversation.SyncListener() {

			@Override
			public void onSendUserReply(List<Reply> replyList) {
				// adapter.notifyDataSetChanged();
			}

			@Override
			public void onReceiveDevReply(List<DevReply> replyList) {
			}

		};
		defaultConversation.sync(listener);
	}

	// 获得焦点弹出数字键盘
	private void InputMethodPanel(final View view) {
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				if (im.isActive()) {
					im.toggleSoftInputFromWindow(view.getWindowToken(), 0, 0);
				}
			}
		}, 500);
	}

	private void hideInput() {
		if (im != null && im.isActive()) {
			im.hideSoftInputFromWindow(FeedbackActivity.this.getCurrentFocus()
					.getWindowToken(), 0);
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		MobclickAgent.onPageStart(this.getClass().getSimpleName()); //统计页面
		MobclickAgent.onResume(this);          //统计时长
		InputMethodPanel(content);
		content.setSelection(content.getText().toString().length());
	}
	
	@Override
    public void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd(this.getClass().getSimpleName());
		MobclickAgent.onPause(this);
	}

    @Override
    protected void updateView() {

    }

}
