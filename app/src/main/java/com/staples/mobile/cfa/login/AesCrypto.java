/*
 * Copyright (c) 2015 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.cfa.login;

import android.util.Base64;
import android.util.Log;

import java.security.Key;
import java.security.MessageDigest;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;


/**
 * Created by sutdi001 on 1/22/15.
 * Code borrowed from:
 *   https://www.owasp.org/index.php/Using_the_Java_Cryptographic_Extensions
 *   http://stackoverflow.com/questions/3451670/java-aes-and-using-my-own-key
 *   and from Stephen Dow
 */
public class AesCrypto {

    private static final String TAG = AesCrypto.class.getSimpleName();

    /**
     * encrypt a string using AES
     * @param textToEncrypt
     * @param key must be a complex string for adequate security
     */
    public static String encrypt(String textToEncrypt, String key) {
        try {
            Cipher cipher = createCipher(Cipher.ENCRYPT_MODE, key);
            byte[] encryptedBytes = cipher.doFinal(textToEncrypt.getBytes("UTF-8"));
            String encryptedText = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP);
            return encryptedText;
        } catch(Exception e) {
            Log.d(TAG, e.getMessage());
        }
        return null;
    }

    /**
     * decrypt an encrypted string using AES
     * @param encryptedText
     * @param key must be a complex string for adequate security
     */
    public static String decrypt(String encryptedText, String key) {
        try {
            Cipher cipher = createCipher(Cipher.DECRYPT_MODE, key);
            byte[] encryptedBytes = Base64.decode(encryptedText, Base64.NO_WRAP);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes);
        } catch(Exception e) {
            Log.d(TAG, e.getMessage());
        }
        return null;
    }


    /**
     * create and initialize a cipher
     * @param mode Cipher.ENCRYPT_MODE or Cipher.DECRYPT_MODE
     * @param key must be a complex string for adequate security
     * @return
     */
    private static Cipher createCipher(int mode, String key) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(mode, generateKey(key));
            return cipher;
        } catch(Exception e) {
            Log.d(TAG, e.getMessage());
        }
        return null;
    }

    /**
     * generates key for creating cipher
     * code borrowed from http://stackoverflow.com/questions/3451670/java-aes-and-using-my-own-key
     * @param seed must be a complex string for adequate security
     * @return
     */
    private static Key generateKey(String seed) {
        try {
            byte[] keyBytes = ("3xtraS@lt" + seed).getBytes("UTF-8");
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            keyBytes = sha.digest(keyBytes);
            keyBytes = Arrays.copyOf(keyBytes, 16); // use only first 128 bit (16x8=128)
            return new SecretKeySpec(keyBytes, "AES");
        } catch(Exception e) {
            Log.d(TAG, e.getMessage());
        }
        return null;
    }


    // The android version of the javadocs for SecureRandom recommends not using this.
//    /**
//     * generates key for creating cipher
//     * code borrowed from Stephen Dow
//     * @param seed
//     * @return
//     */
//    private static Key generateKey(String seed) {
//        try {
//            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG", "Crypto"); // see http://stackoverflow.com/questions/13383006/encryption-error-on-android-4-2
//            secureRandom.setSeed(seed.getBytes("UTF-8"));
//
//            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
//            keyGenerator.init(128, secureRandom);
//
//            SecretKey secretKey = keyGenerator.generateKey();
//            return new SecretKeySpec(secretKey.getEncoded(), "AES");
//        } catch(Exception e) {
//            Log.d(TAG, e.getMessage());
//        }
//        return null;
//    }

}
