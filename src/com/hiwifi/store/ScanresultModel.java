package com.hiwifi.store;

import java.io.Serializable;

public class ScanresultModel implements Serializable{

    /** The network name. */
    public String SSID;
    /** The address of the access point. */
    public String BSSID;
    /**
     * Describes the authentication, key management, and encryption schemes
     * supported by the access point.
     */
    public String capabilities;
    /**
     * The detected signal level in dBm. At least those are the units used by
     * the TI driver.
     */
    public int level;
    /**
     * The frequency in MHz of the channel over which the client is communicating
     * with the access point.
     */
    public int frequency;
	public String getSSID() {
		return SSID;
	}
	public void setSSID(String sSID) {
		SSID = sSID;
	}
	public String getBSSID() {
		return BSSID;
	}
	public void setBSSID(String bSSID) {
		BSSID = bSSID;
	}
	public String getCapabilities() {
		return capabilities;
	}
	public void setCapabilities(String capabilities) {
		this.capabilities = capabilities;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public int getFrequency() {
		return frequency;
	}
	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}
	@Override
	public String toString() {
		return "ScanresultModel [SSID=" + SSID + ", BSSID=" + BSSID
				+ ", capabilities=" + capabilities + ", level=" + level
				+ ", frequency=" + frequency + "]";
	}
	public ScanresultModel(String sSID, String bSSID, String capabilities,
			int level, int frequency) {
		super();
		SSID = sSID;
		BSSID = bSSID;
		this.capabilities = capabilities;
		this.level = level;
		this.frequency = frequency;
	}
    
    
}
