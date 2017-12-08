package com.sz.mobilesdk.util;

import android.text.TextUtils;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.UnsupportedEncodingException;
import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by hudq on 2016/11/28.
 */

public class AESUtil {


    /************************ AES加密相关 ***********************/
    /**
     * AES转变
     * <p>算法名称/加密模式/填充方式</p>
     * <p>加密模式有：电子密码本模式ECB、加密块链模式CBC、加密反馈模式CFB、输出反馈模式OFB</p>
     * <p>填充方式有：NoPadding、ZerosPadding、PKCS5Padding、PKCS7Padding</p>
     */
    private static final String AES_Transformation = "AES/CBC/PKCS7Padding";
    private static final String AES_Algorithm = "AES";
    private static final String AES_IV = "0102030405060708";
    private static final String AES_KEY = "80F008F8C906098FCE93A89B3DB2EF4E";

    /**
     * AES加密
     *
     * @param content 加密源
     * @return
     */
    public static String encrypt(String content) {
        if (TextUtils.isEmpty(content)) return null;
        try {
            return encrypt(content.getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * AES加密
     *
     * @param data 加密源
     * @return
     */
    public static String encrypt(byte[] data) {
        return encrypt(data, AES_KEY);
    }

    /**
     * 加密
     *
     * @param data
     * @param key
     * @return
     */
    public static String encrypt(byte[] data, String key) {
        try {
            byte[] rawKey = toByte(key);//getRawKey(AES_KEY.getBytes());
            byte[] result = encryptBase(rawKey, data);
            return toHex(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * AES解密
     *
     * @param encrypted 加密后的String
     * @return
     */
    public static String decrypt(String encrypted) {
        if (TextUtils.isEmpty(encrypted)) return null;
        return decrypt(encrypted, AES_KEY);
    }

    /**
     * 解密
     *
     * @param encrypted
     * @param key
     * @return
     */
    public static String decrypt(String encrypted, String key) {
        if (TextUtils.isEmpty(encrypted)) return null;

        return decrypt(toByte(encrypted), key);
//        try {
//            byte[] rawKey = toByte(key);//getRawKey(AES_KEY.getBytes());
//            byte[] enc = toByte(encrypted);
//            byte[] result = decryptBase(rawKey, enc);
//            return new String(result);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
    }

    public static String decrypt(byte[] encrypted, String key) {
        try {
            byte[] rawKey = toByte(key);//getRawKey(AES_KEY.getBytes());
            //byte[] enc = encrypted;
            byte[] result = decryptBase(rawKey, encrypted);
            return new String(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

//    private static byte[] getRawKey(byte[] seed) throws Exception {
//        KeyGenerator kgen = KeyGenerator.getInstance(AES_Algorithm);
//        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG", "Crypto");
//        sr.setSeed(seed);
//        kgen.init(128, sr);
//        SecretKey skey = kgen.generateKey();
//        return skey.getEncoded();
//    }

    private static byte[] encryptBase(byte[] key, byte[] data) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(key, AES_Algorithm);
        Security.addProvider(new BouncyCastleProvider());
        Cipher cipher = Cipher.getInstance(AES_Transformation, "BC");
        //cipher.init(Cipher.ENCRYPT_MODE, skeySpec, new IvParameterSpec(
        //       new byte[cipher.getBlockSize()]));
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, new IvParameterSpec(AES_IV.getBytes("utf-8")));
        return cipher.doFinal(data);
    }

    private static byte[] decryptBase(byte[] key, byte[] encrypted)
            throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(key, AES_Algorithm);
        Security.addProvider(new BouncyCastleProvider());
        Cipher cipher = Cipher.getInstance(AES_Transformation, "BC");
        //cipher.init(Cipher.DECRYPT_MODE, skeySpec, new IvParameterSpec(
        //        new byte[cipher.getBlockSize()]));
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, new IvParameterSpec(AES_IV.getBytes("utf-8")));
        return cipher.doFinal(encrypted);
    }

//    private static String toHex(String txt) {
//        return toHex(txt.getBytes());
//    }
//
//    private static String fromHex(String hex) {
//        return new String(toByte(hex));
//    }

    private static byte[] toByte(String hexString) {
        int len = hexString.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++)
            result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2),
                    16).byteValue();
        return result;
    }

    private final static String HEX = "0123456789ABCDEF";

    private static String toHex(byte[] buf) {
        if (buf == null)
            return "";
        StringBuilder result = new StringBuilder(2 * buf.length);
        for (int i = 0; i < buf.length; i++) {
            byte b = buf[i];
            result.append(HEX.charAt((b >> 4) & 0x0f)).append(HEX.charAt(b & 0x0f));
        }
        return result.toString();
    }

//
//    /************************ AES加密相关 ***********************/
//    /**
//     * AES转变
//     * <p>算法名称/加密模式/填充方式</p>
//     * <p>加密模式有：电子密码本模式ECB、加密块链模式CBC、加密反馈模式CFB、输出反馈模式OFB</p>
//     * <p>填充方式有：NoPadding、ZerosPadding、PKCS5Padding、PKCS7Padding</p>
//     */
//    private static final String AES_Transformation = "AES/CBC/PKCS7Padding";
//    private static final String AES_Algorithm = "AES";
//    private static final String AES_IV = "0102030405060708";
//    private static final String AES_KEY = "80F008F8C906098FCE93A89B3DB2EF4E";
//
//    /**
//     * AES加密
//     *
//     * @param content 加密源
//     * @return
//     */
//    public static String encrypt(String content) {
//        if (TextUtils.isEmpty(content)) return null;
//        try {
//            return encrypt(content.getBytes("utf-8"));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    /**
//     * AES加密
//     *
//     * @param data 加密源
//     * @return
//     */
//    public static String encrypt(byte[] data) {
//        try {
//            byte[] rawKey = toByte(AES_KEY);//getRawKey(AES_KEY.getBytes());
//            byte[] result = encrypt(rawKey, data);
//            return toHex(result);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    /**
//     * AES解密
//     *
//     * @param encrypted 加密后的String
//     * @return
//     */
//    public static String decrypt(String encrypted) {
//        if (TextUtils.isEmpty(encrypted)) return null;
//        try {
//            byte[] rawKey = toByte(AES_KEY);//getRawKey(AES_KEY.getBytes());
//            byte[] enc = toByte(encrypted);
//            byte[] result = decrypt(rawKey, enc);
//            return new String(result);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
////    private static byte[] getRawKey(byte[] seed) throws Exception {
////        KeyGenerator kgen = KeyGenerator.getInstance(AES_Algorithm);
////        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG", "Crypto");
////        sr.setSeed(seed);
////        kgen.init(128, sr);
////        SecretKey skey = kgen.generateKey();
////        return skey.getEncoded();
////    }
//
//    private static byte[] encrypt(byte[] key, byte[] data) throws Exception {
//        SecretKeySpec skeySpec = new SecretKeySpec(key, AES_Algorithm);
//        Security.addProvider(new BouncyCastleProvider());
//        Cipher cipher = Cipher.getInstance(AES_Transformation, "BC");
//        //cipher.init(Cipher.ENCRYPT_MODE, skeySpec, new IvParameterSpec(
//        //       new byte[cipher.getBlockSize()]));
//        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, new IvParameterSpec(AES_IV.getBytes("utf-8")));
//        return cipher.doFinal(data);
//    }
//
//    private static byte[] decrypt(byte[] key, byte[] encrypted)
//            throws Exception {
//        SecretKeySpec skeySpec = new SecretKeySpec(key, AES_Algorithm);
//        Security.addProvider(new BouncyCastleProvider());
//        Cipher cipher = Cipher.getInstance(AES_Transformation, "BC");
//        //cipher.init(Cipher.DECRYPT_MODE, skeySpec, new IvParameterSpec(
//        //        new byte[cipher.getBlockSize()]));
//        cipher.init(Cipher.DECRYPT_MODE, skeySpec, new IvParameterSpec(AES_IV.getBytes("utf-8")));
//        return cipher.doFinal(encrypted);
//    }
//
////    private static String toHex(String txt) {
////        return toHex(txt.getBytes());
////    }
////
////    private static String fromHex(String hex) {
////        return new String(toByte(hex));
////    }
//
//    private static byte[] toByte(String hexString) {
//        int len = hexString.length() / 2;
//        byte[] result = new byte[len];
//        for (int i = 0; i < len; i++)
//            result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2),
//                    16).byteValue();
//        return result;
//    }
//
//    private final static String HEX = "0123456789ABCDEF";
//
//    private static String toHex(byte[] buf) {
//        if (buf == null)
//            return "";
//        StringBuilder result = new StringBuilder(2 * buf.length);
//        for (int i = 0; i < buf.length; i++) {
//            byte b = buf[i];
//            result.append(HEX.charAt((b >> 4) & 0x0f)).append(HEX.charAt(b & 0x0f));
//        }
//        return result.toString();
//    }

}
