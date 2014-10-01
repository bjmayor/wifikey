package com.hiwifi.model.wifi.adapter;
import java.io.Serializable;

import org.apache.http.message.BasicNameValuePair;


public class SerializedNameAndValuepair implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected String name;
	protected String value;
	public SerializedNameAndValuepair(String name, String value) {
		super();
		this.name = name;
		this.value = value;
	}
	public synchronized final String getName() {
		return name;
	}
	public synchronized final void setName(String name) {
		this.name = name;
	}
	public synchronized final String getValue() {
		return value;
	}
	public synchronized final void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return this.name+"="+this.value;
	}
}
