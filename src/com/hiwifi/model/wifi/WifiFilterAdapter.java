/**
 * WifiFilterAdapter.java
 * shunpingliu
 * 2014-4-3 ����11:33:41
 * TODO
 * description
 */
package com.hiwifi.model.wifi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author shunpingliu
 * 
 */
public class WifiFilterAdapter {
	public static List<AccessPoint> filterList(List<AccessPoint> list) {
		Iterator<AccessPoint> iterable = list.iterator();
		List<AccessPoint> mAvaiableList = new ArrayList<AccessPoint>();

		while (iterable.hasNext()) {
			AccessPoint accessPoint = (AccessPoint) iterable.next();
			if (accessPoint.canAutoConnnect() && !accessPoint.isBlockAutoLink()) {
				mAvaiableList.add(accessPoint);
			}
		}
		return mAvaiableList.size() > 0 ? mAvaiableList : null;
	}

}
