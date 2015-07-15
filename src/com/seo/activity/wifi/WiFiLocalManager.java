package com.seo.activity.wifi;


import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by maynard on 14-10-17.
 */
public class WiFiLocalManager implements Runnable {
    private List read() throws Exception {
        List wifiInfos = new ArrayList();
        Process process = null;
        DataOutputStream dataOutputStream = null;
        DataInputStream dataInputStream = null;
        StringBuffer wifiConf = new StringBuffer();
        try {
            process = Runtime.getRuntime().exec("su");
            dataOutputStream = new DataOutputStream(process.getOutputStream());
            dataInputStream = new DataInputStream(process.getInputStream());
            dataOutputStream.writeBytes("cat /data/misc/wifi/*.conf\n");
            dataOutputStream.writeBytes("exit\n");
            dataOutputStream.flush();
            InputStreamReader inputStreamReader = new InputStreamReader(dataInputStream, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                wifiConf.append(line);
            }
            bufferedReader.close();
            inputStreamReader.close();
            process.waitFor();
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                if (dataOutputStream != null) {
                    dataOutputStream.close();
                }
                if (dataInputStream != null) {
                    dataInputStream.close();
                }
                process.destroy();
            } catch (Exception e) {
                throw e;
            }
        }
        Pattern network = Pattern.compile("network=\\{([^\\}]+)\\}", Pattern.DOTALL);
        Matcher networkMatcher = network.matcher(wifiConf.toString());
        while (networkMatcher.find()) {
            String networkBlock = networkMatcher.group();
            Pattern ssid = Pattern.compile("ssid=\"([^\"]+)\"");
            Matcher ssidMatcher = ssid.matcher(networkBlock);
            if (ssidMatcher.find()) {
                WifiInfo wifiInfo = new WifiInfo();
                wifiInfo.ssid = ssidMatcher.group(1);
                Pattern psk = Pattern.compile("psk=\"([^\"]+)\"");
                Matcher pskMatcher = psk.matcher(networkBlock);
                if (pskMatcher.find()) {
                    wifiInfo.password = pskMatcher.group(1);
                } else {
                    wifiInfo.password = "无密码";
                }
                wifiInfos.add(wifiInfo);
            }
        }
        return wifiInfos;
    }

    private static List<WifiInfo> sList = null;
    private onListLoadListener listLoadListener;

    public void load(onListLoadListener listLoadListener) {
        this.listLoadListener = listLoadListener;
        new Thread(this).start();
    }


    public WifiInfo getWifiInfo(String ssid) {

        WifiInfo retInfo = new WifiInfo();
        retInfo.password = "没有找到密码";
        if (sList == null) {
            return null;
        }
        for (WifiInfo info : sList) {
            if (info.ssid.equals(ssid)) {
                retInfo = info;
                break;
            }
        }
        return retInfo;
    }

    @Override
    public void run() {
        if (sList == null) {
            try {
                sList = new WiFiLocalManager().read();
            } catch (Exception e) {
                sList = new ArrayList<WifiInfo>();
            } finally {
                this.listLoadListener.onWifiListLoaded();
            }
        } else {
            this.listLoadListener.onWifiListLoaded();
        }
    }

    public static class WifiInfo {
        public String ssid;
        public String password = "您的手机没有Root,无法查看";
    }

    public interface onListLoadListener {
        public void onWifiListLoaded();
    }

}
