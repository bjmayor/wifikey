package com.hiwifi.utils;

import java.io.InputStream;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import javax.crypto.Cipher;

import com.hiwifi.utils.encode.Base64;




import android.content.Context;

/**
 * 证书组件
 * 
 * @author 
 * @version 1.0
 * @since 1.0
 */
public abstract class CertificateCoder {

	/**
	 * Java密钥库(Java Key Store，JKS)KEY_STORE
	 */
	public static final String KEY_STORE = "JKS";

	public static final String X509 = "X.509";

	public static String encryptBASE64(byte[] from) {
		return Base64.encodeToString(from, Base64.DEFAULT);
	}

	public static byte[] decryptBASE64(String to) {
		return Base64.decode(to, Base64.DEFAULT);
	}

	/**
	 * 公钥加密
	 * 
	 * @param data
	 * @param certificatePath
	 * @return
	 * @throws Exception
	 */
	public static String encryptByPublicKey(String data,Context context) throws Exception {
		CertificateFactory certificateFactory = CertificateFactory.getInstance(X509);
		InputStream in = context.getAssets().open("mojicert.cer");

		Certificate certificate = certificateFactory.generateCertificate(in);
		in.close();

		PublicKey publicKey = certificate.getPublicKey();

		// 对数据加密
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);

		return encryptBASE64(cipher.doFinal(data.getBytes()));

	}

}
