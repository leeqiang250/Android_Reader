package com.sz.mobilesdk.util;

import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;

/**
 * 安全加密工具类
 *
 * @author hudq
 */
public class SecurityUtil {

    // 十六进制下数字到字符的映射数组
    private final static String[] hexDigits = {"0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};


    /**
     * MD5加盐
     *
     * @param originStr 加密字符串
     * @param salt      盐值
     * @return
     */
    public static String encodeMD5BySalt(String originStr, String salt) {
        return encodeByMD5(originStr + "{" + salt + "}");
    }

    /**
     * 对字符串进行MD5加密
     */
    public static String encodeByMD5(String originString) {
        if (originString != null) {
            try {
                // 创建具有指定算法名称的信息摘要
                // 使用指定的字节数组对摘要进行最后更新，然后完成摘要计算
                // 将得到的字节数组变成字符串返回
                return byteArrayToHexString(MessageDigest.getInstance("MD5")
                        .digest(originString.getBytes()));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }
    /**
     * 根据策略加盐生成指定的参数，再对结果进行md5处理
     *
     * @param salt      盐值
     * @param originStr 原始字符串String
     * @return String
     */
    public static String getParamByMD5(String salt, String originStr) {
        return encodeByMD5(encodeByMD5(originStr + "{" + salt + "}"));
    }

    /**
     * 转换字节数组为十六进制字符串
     *
     * @return 十六进制字符串
     */
    public static String byteArrayToHexString(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < b.length; i++) {
            sb.append(byteToHexString(b[i]));
        }
        return sb.toString();
    }

    /**
     * 将一个字节转化成十六进制形式的字符串
     */
    private static String byteToHexString(byte b) {
        int n = b;
        if (n < 0) n = 256 + n;
        int d1 = n / 16;
        int d2 = n % 16;
        return hexDigits[d1] + hexDigits[d2];
    }

    /**
     * hexString 转 byte[]
     *
     * @param hexString
     * @return
     */
    public static byte[] hexStringToByte(String hexString) {
        int len = hexString.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++)
            result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2),
                    16).byteValue();
        return result;

    }

    /**
     * hexString 转 byte[]
     *
     * @param hexString 十六进制形式的字符串
     * @return 字节byte
     */
    public static byte[] hexStringtoByteArray(String hexString) {
        int len = hexString.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++)
            result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2), 16).byteValue();
        return result;
    }

    public static long bytes2Long(byte[] byteNum) {
        long num = 0;
        for (int ix = 0; ix < 8; ++ix) {
            num <<= 8;
            num |= (byteNum[ix] & 0xff);
        }
        return num;
    }

    /**
     * BASE64 加密
     *
     * @param str
     * @return
     */
    public static String encryptBASE64(String str) {
        if (str == null || str.length() == 0) {
            return "";
        }
        try {
            byte[] encode = str.getBytes("UTF-8");
            // base64 加密
            return new String(Base64.encode(encode, 0, encode.length,
                    Base64.DEFAULT), "UTF-8");

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return "";
    }

    /**
     * BASE64 解密
     *
     * @param str
     * @return
     */
    public static String decryptBASE64(String str) {
        if (str == null || str.length() == 0) {
            return "";
        }
        try {
            byte[] encode = str.getBytes("UTF-8");
            // base64 解密
            return new String(Base64.decode(encode, 0, encode.length,
                    Base64.DEFAULT), "UTF-8");

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return null;
    }

}
