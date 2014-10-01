package com.hiwifi.utils;

import java.util.HashSet;

public class Blacklist {
	private static HashSet<String> blacklist = null;

	private static void init_list() {
		blacklist = new HashSet<String>();
		blacklist.add("com.hiwifi.hiwifi");
		blacklist.add("com.adobe.reader");
		blacklist.add("com.youloft.calendar");
		blacklist.add("com.when.coco");
		blacklist.add("com.zdworks.android.zdclock");
		blacklist.add("com.sohu.inputmethod.sogou");
		blacklist.add("com.anzhuoshoudiantong");
		blacklist.add("com.adobe.flashplayer");
		blacklist.add("com.iflytek.inputmethod");
		blacklist.add("com.estrongs.android.pop");
		blacklist.add("com.shangqu.security");
		blacklist.add("com.kingroot.kinguser");
		blacklist.add("com.tencent.qqpinyin");
		blacklist.add("com.txeasy.shoudiantong");
		blacklist.add("com.ptom.flashlight");
		blacklist.add("com.roamingsoft.manager");
		blacklist.add("uk.co.nickfines.RealCalc");
		blacklist.add("com.btaozuhong.flashlight");
		blacklist.add("com.totoro.flashlight");
		blacklist.add("com.sgxnncptc.flashlight");
		blacklist.add("com.xiaodingdang.flashlight");
		blacklist.add("com.frego.flashlight");
		blacklist.add("com.speedsoftware.rootexplorer");
		blacklist.add("com.fihtdc.filemanager");
		blacklist.add("com.lidroid.toolbox");
		blacklist.add("com.android.mifileexplorer");
		blacklist.add("com.aohe.icodestar.filemanager.fileexplorer");
		blacklist.add("com.youmi.filemasterlocal");
		blacklist.add("com.qihoo.explorer");
		blacklist.add("com.lonelycatgames.Xplore");
		blacklist.add("com.visionobjects.calculator");
		blacklist.add("longbin.helloworld");
		blacklist.add("com.tqkj.calculator");
		blacklist.add("jp.ne.kutu.Panecal");
		blacklist.add("jan.calculator");
		blacklist.add("com.digitalchemy.calculator.freedecimal");
		blacklist.add("com.timluo.calculater");
		blacklist.add("com.kexue.Calculator");
		blacklist.add("cn.etouch.ecalendar");
		blacklist.add("com.when.wannianli");
		blacklist.add("cn.bluecrane.calendar");
		blacklist.add("me.iweek.wannianli");
		blacklist.add("com.zdworks.android.zdcalendar");
		blacklist.add("me.iweek.rili");
		blacklist.add("com.updrv.lifecalendar");
		blacklist.add("com.cloud.calendar");
		blacklist.add("com.wandoujia.roshan");
		blacklist.add("com.cungu.callrecorder.ui");
		blacklist.add("com.dianxinos.dxbb");
		blacklist.add("com.modoohut.dialer");
		blacklist.add("com.modoohut.dialer.donate");
		blacklist.add("com.cm.app");
		blacklist.add("com.mari.cmcccall");
		blacklist.add("com.hiapk.dialer");
		blacklist.add("com.zyx.pkgviewer");
		blacklist.add("com.cleanmaster.mguard_cn");
		blacklist.add("com.tencent.qqpimsecure");
		blacklist.add("com.lbe.security");
		blacklist.add("cn.opda.a.phonoalbumshoushou");
		blacklist.add("com.zhimahu");
		blacklist.add("com.ijinshan.kbatterydoctor");
		blacklist.add("com.dianxinos.optimizer.duplay");
		blacklist.add("com.rock.lockscreenone");
		blacklist.add("com.ludashi.benchmark");
		blacklist.add("com.ijinshan.mguard");
		blacklist.add("com.lenovo.safecenter");
		blacklist.add("com.baidu.appsearch");
		blacklist.add("com.mxtech.videoplayer.pro");
		blacklist.add("com.storm.smart");
		blacklist.add("com.clov4r.android.nil");
		blacklist.add("com.xfplay.play");
		blacklist.add("com.tencent.research.drop");
		blacklist.add("cn.jingling.motu.photowonder");
	}

	public static boolean is_in_blacklist(String packageName) {
		if (blacklist == null) {
			init_list();
		}
		return blacklist.contains(packageName);
	}

	public static void recycle_list() {
		blacklist.clear();
		blacklist = null;
	}
}
