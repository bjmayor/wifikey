package com.hiwifi.model.wifi;

import java.io.Serializable;
import java.util.Locale;

public class BlockAlg implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int ruleWhole = 0;
	public static final int ruleBlur = 1;
	public int rule;
	public String matcher;

	public BlockAlg(String matcher, int rule) {
		this.rule = rule;
		this.matcher = matcher;
	}

	public Boolean isBlocked(AccessPoint accessPoint) {
		return isBlocked(accessPoint.getPrintableSsid());
	}
	
	public Boolean isBlocked(String target)
	{
		switch (rule) {
		case ruleWhole:
			return matcher.equals(target);
		case ruleBlur:
			return target.toLowerCase().contains(matcher.toLowerCase());
		default:
			break;
		}
		return false;
	}
}
