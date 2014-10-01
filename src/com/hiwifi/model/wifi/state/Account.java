package com.hiwifi.model.wifi.state;

import java.io.Serializable;

import com.hiwifi.store.AccountModel;
import com.hiwifi.utils.encode.Security;

public class Account implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4756657834446083858L;
	protected String username;
	protected String password;// 明文
	private transient AccountModel model;
	protected int aid;
	private int type;

	public enum Type {
		TypeIsUndefined, TypeIsCmcc, TypeIsChinanet, TypeIsChinaunicom;

		public static Type valueOf(int value) {
			switch (value) {
			case 0:
				return TypeIsUndefined;
			case 1:
				return TypeIsCmcc;
			case 2:
				return TypeIsChinanet;
			case 3:
				return TypeIsChinaunicom;
			default:
				return TypeIsUndefined;
			}
		}
	}

	public synchronized final int getAid() {
		return aid;
	}

	public synchronized final void setAid(int aid) {
		this.aid = aid;
	}

	public synchronized final int getType() {
		return type;
	}

	public synchronized final void setType(int type) {
		this.type = type;
	}

	public Account() {
		super();
	}

	public Account(AccountModel model) {
		this.username = model.username;
		this.password = Security.decode_string(model.getPassword());
		this.aid = model.aid;
		this.model = model;
		this.type = model.specialID;
	}

	public void delete() {
		if (this.model != null) {
			this.model.delete();
		}
	}

	public synchronized final String getUsername() {
		return username;
	}

	public synchronized final void setUsername(String username) {
		this.username = username;
	}

	public synchronized final String getPassword(Boolean needEncoded) {
		if (needEncoded) {
			return Security.encode_string(this.password);
		}
		return password;
	}

	public synchronized final void setPassword(String password,
			Boolean hasEncode) {
		if (hasEncode) {
			this.password = Security.decode_string(password);
		} else {
			this.password = password;
		}
	}

}
