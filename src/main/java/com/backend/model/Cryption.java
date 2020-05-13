package com.backend.model;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class Cryption {

    public String decryptRSA(String enc, String prk) throws InvalidKeyException,
            IOException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        PrivateKey prvk;
        byte[] privateKeyBytes = Base64.getDecoder().decode(prk);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        prvk = keyFactory.generatePrivate(privateKeySpec);
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, prvk);
        return new String(cipher.doFinal(Base64.getDecoder().decode(enc)));
    }

    public String encryptRSA(byte[] inpBytes, String plk) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException,
            NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        PublicKey pubk;
        byte[] publicKeyBytes = Base64.getDecoder().decode(plk);
        EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        pubk = keyFactory.generatePublic(publicKeySpec);
        Cipher cipher = Cipher.getInstance("RSA");
        System.out.println("\n" + cipher.getProvider().getInfo());
        cipher.init(Cipher.ENCRYPT_MODE, pubk);
        return Base64.getEncoder().encodeToString(cipher.doFinal(inpBytes)).replace("\r", "");
    }
}
