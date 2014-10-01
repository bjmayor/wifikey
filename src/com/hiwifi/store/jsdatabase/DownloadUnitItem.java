package com.hiwifi.store.jsdatabase;

import java.io.Serializable;

public class DownloadUnitItem implements Serializable {
	private static final long serialVersionUID = 19891208L;
	public String name;
	public String fullpath;

	public DownloadUnitItem() {
		this.name = "";
		this.fullpath = "";
	}

	public DownloadUnitItem(String name, String fullpath) {
		this.name = name;
		this.fullpath = fullpath;
	}
}