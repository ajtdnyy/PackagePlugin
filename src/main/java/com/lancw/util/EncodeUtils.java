/*
 * 文 件 名:  EncodeUtils.java
 * 版    权:  Copyright YYYY-YYYY,  All rights reserved
 * 描    述:  <描述>
 * 修 改 人:  lancw
 * 修改时间:  2014-6-6
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.lancw.util;

import com.lancw.plugin.MainFrame;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * 项目名称：PackagePlugin
 * 类名称：EncodeUtils
 * 类描述：
 * 创建人：lancw
 * 创建时间：2014-6-6 16:07:56
 * 修改人：lancw
 * 修改时间：2014-6-6 16:07:56
 * 修改备注：
 * <p>
 * @version 1.0
 */
public class EncodeUtils {

    public static String encodeBySHA(String str) {
	try {
	    MessageDigest md = MessageDigest.getInstance("SHA-1");
	    return parseByte2HexStr(md.digest(str.getBytes()));
	} catch (NoSuchAlgorithmException ex) {
            MainFrame.LOGGER.log(Level.SEVERE, null, ex);
	    return str;
	}
    }

    public static String encodeByMD5(String str) {
	try {
	    MessageDigest md = MessageDigest.getInstance("MD5");
	    return parseByte2HexStr(md.digest(str.getBytes()));
	} catch (NoSuchAlgorithmException ex) {
            MainFrame.LOGGER.log(Level.SEVERE, null, ex);
	    return str;
	}
    }

    public static String decodeDES3(String pwd, String key) {
	try {
	    Cipher c3des = Cipher.getInstance("DESede/CBC/PKCS5Padding");
	    SecretKeySpec myKey = new SecretKeySpec(HexStringToByteArray(key), "DESede");
	    IvParameterSpec ivspec = new IvParameterSpec(defaultIV);
	    c3des.init(Cipher.DECRYPT_MODE, myKey, ivspec);
	    byte[] s = getByteArrFromBase64(pwd);
	    byte[] encoded = c3des.doFinal(s);
	    return new String(encoded);
	} catch (Exception ex) {
            MainFrame.LOGGER.log(Level.SEVERE, null, ex);
	}
	return "";
    }

    public static String encryptByDES3(String str, String keystr) {
	String strResult = "";
	try {
	    Cipher c3des = Cipher.getInstance("DESede/CBC/PKCS5Padding");
	    SecretKeySpec myKey = new SecretKeySpec(HexStringToByteArray(keystr), "DESede");
	    IvParameterSpec ivspec = new IvParameterSpec(defaultIV);
	    c3des.init(Cipher.ENCRYPT_MODE, myKey, ivspec);
	    byte[] encoded = c3des.doFinal(str.getBytes());
	    strResult = getBASE64_byte(encoded);
	} catch (Exception e) {
	    strResult = str;
            MainFrame.LOGGER.log(Level.SEVERE, null, e);
	}
	return strResult;
    }
    private static final byte[] defaultIV = {1, 2, 3, 4, 5, 6, 7, 8};

    public static String getBASE64_byte(byte[] s) {
	if (s == null) {
	    return null;
	}
	return (new sun.misc.BASE64Encoder()).encode(s);
    }

    public static byte[] getByteArrFromBase64(String s) throws Exception {
	if (s == null) {
	    return null;
	}
	return (new sun.misc.BASE64Decoder()).decodeBuffer(s);
    }

    public static byte[] HexStringToByteArray(String s) {
	byte[] buf = new byte[s.length() / 2];
	for (int i = 0; i < buf.length; i++) {
	    buf[i] = (byte) (chr2hex(s.substring(i * 2, i * 2 + 1)) * 0x10 + chr2hex(s.substring(i * 2 + 1, i * 2 + 2)));
	}
	return buf;
    }

    /**
     * 将二进制转换成16进制
     * <p>
     * @param buf
     * @return
     */
    public static String parseByte2HexStr(byte buf[]) {
	StringBuilder sb = new StringBuilder();
	for (int i = 0; i < buf.length; i++) {
	    String hex = Integer.toHexString(buf[i] & 0xFF);
	    if (hex.length() == 1) {
		hex = '0' + hex;
	    }
	    sb.append(hex.toUpperCase());
	}
	return sb.toString();
    }

    private static byte chr2hex(String chr) {
	if (chr.equals("0")) {
	    return 0x00;
	} else if (chr.equals("1")) {
	    return 0x01;
	} else if (chr.equals("2")) {
	    return 0x02;
	} else if (chr.equals("3")) {
	    return 0x03;
	} else if (chr.equals("4")) {
	    return 0x04;
	} else if (chr.equals("5")) {
	    return 0x05;
	} else if (chr.equals("6")) {
	    return 0x06;
	} else if (chr.equals("7")) {
	    return 0x07;
	} else if (chr.equals("8")) {
	    return 0x08;
	} else if (chr.equals("9")) {
	    return 0x09;
	} else if (chr.equals("A")) {
	    return 0x0a;
	} else if (chr.equals("B")) {
	    return 0x0b;
	} else if (chr.equals("C")) {
	    return 0x0c;
	} else if (chr.equals("D")) {
	    return 0x0d;
	} else if (chr.equals("E")) {
	    return 0x0e;
	} else if (chr.equals("F")) {
	    return 0x0f;
	}
	return 0x00;
    }
}
