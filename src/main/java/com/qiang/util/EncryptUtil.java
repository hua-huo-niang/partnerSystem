package com.qiang.util;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import cn.hutool.crypto.symmetric.SymmetricAlgorithm;

public class EncryptUtil {

    static private byte[] key = null;
    static private AES aes = null;

    private EncryptUtil(){
        key = SecureUtil.generateKey(SymmetricAlgorithm.AES.getValue()).getEncoded();
        aes = SecureUtil.aes(key);
    }
    public static String encryptToHex(String content){
        return aes.encryptHex(content);
    }
}
