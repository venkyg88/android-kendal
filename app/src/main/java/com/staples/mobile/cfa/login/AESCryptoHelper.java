/*
 * Copyright (c) 2015 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.cfa.login;

import android.util.Base64;
import android.util.Log;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


/**
 * Created by sutdi001 on 1/22/15.
 * Code borrowed from https://www.owasp.org/index.php/Using_the_Java_Cryptographic_Extensions
 * Code also borrowed from Stephen Dow
 */
public class AESCryptoHelper {

    private static final String TAG = AESCryptoHelper.class.getSimpleName();

    /**
     * encrypt a string using AES
     */
    public static String encrypt(String strDataToEncrypt) {
        try {
            Cipher cipher = createCipher(Cipher.ENCRYPT_MODE);
            byte[] encryptedBytes = cipher.doFinal(strDataToEncrypt.getBytes());
            return Base64.encodeToString(encryptedBytes, Base64.DEFAULT);
        } catch(Exception e) {
            Log.d(TAG, e.getMessage());
        }
        return null;
    }

    /**
     * decrypt an encrypted string using AES
     */
    public static String decrypt(String encryptedText) {
        try {
            Cipher cipher = createCipher(Cipher.DECRYPT_MODE);
            byte[] byteDecryptedText = cipher.doFinal(Base64.decode(encryptedText, Base64.DEFAULT));
            return new String(byteDecryptedText);
        } catch(Exception e) {
            Log.d(TAG, e.getMessage());
        }
        return null;
    }

    /**
     * create and initialize a cipher
     * @param mode Cipher.ENCRYPT_MODE or Cipher.DECRYPT_MODE
     * @return
     */
    private static Cipher createCipher(int mode) {
        try {
            /**
             * Step 1. Generate an AES key using KeyGenerator Initialize the
             * keysize to 128 bits (16 bytes)
             *
             */
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128);
            SecretKey secretKey = keyGen.generateKey();


            /**
             * Step 2. Generate an Initialization Vector (IV)
             * 		a. Use SecureRandom to generate random bits
             * 		   The size of the IV matches the blocksize of the cipher (128 bits for AES)
             * 		b. Construct the appropriate IvParameterSpec object for the data to pass to Cipher's init() method
             */
            final int AES_KEYLENGTH = 128;	// change this as desired for the security level you want
            byte[] iv = new byte[AES_KEYLENGTH / 8];	// Save the IV bytes or send it in plaintext with the encrypted data so you can decrypt the data later
            SecureRandom prng = new SecureRandom();
            prng.nextBytes(iv);


            /**
             * Step 3. Create a Cipher by specifying the following parameters
             * 		a. Algorithm name - here it is AES
             * 		b. Mode - here it is CBC mode
             * 		c. Padding - e.g. PKCS7 or PKCS5
             */
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7PADDING"); // Must specify the mode explicitly as most JCE providers default to ECB mode!!


            /**
             * Step 4. Initialize the Cipher for Encryption
             */
            cipher.init(mode, generateSecretKeySpec());

            return cipher;
        } catch(Exception e) {
            Log.d(TAG, e.getMessage());
        }

        return null;
    }

    private static SecretKeySpec generateSecretKeySpec() {
        return new SecretKeySpec(new byte[]{1}, "");
    }
}
