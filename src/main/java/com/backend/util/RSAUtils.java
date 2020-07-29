package com.backend.util;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import javax.xml.bind.DatatypeConverter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.*;


public class RSAUtils {

    public static PublicKey convertStringToPublicKey(String publicKey) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        PublicKey publicKey1 = null;

        byte[] publicKeyBytes = java.util.Base64.getDecoder().decode(publicKey);
        EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        publicKey1 = keyFactory.generatePublic(publicKeySpec);

        return publicKey1;
    }

    public static PrivateKey convertStringToPrivateKey(String privateKeyS) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        PrivateKey privateKey = null;

        byte[] privateKeyBytes = java.util.Base64.getDecoder().decode(privateKeyS);

        EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        privateKey = keyFactory.generatePrivate(privateKeySpec);
        privateKey = keyFactory.generatePrivate(privateKeySpec);

        return privateKey;
    }

    public static String convertPublicKeyToString(PublicKey pk) {
        String plk = "";
        plk = Base64.encodeBase64String(pk.getEncoded());
        return plk;
    }

    public static String convertPrivateKeyToString(PrivateKey prk) {
        String prkey = "";
        prkey = Base64.encodeBase64String(prk.getEncoded());
        return prkey;
    }


    public static PublicKey readPublicKey(String publicKey) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {
        String publicKeyContent = publicKey.replaceAll("\\n", "")
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "");

        byte[] decoded = Base64.decodeBase64(publicKeyContent);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }

    public static PrivateKey readPrivateKey(String privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String privateKeyContent = privateKey.replaceAll("\\n", "")
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "");

        KeyFactory kf = KeyFactory.getInstance("RSA");

        PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(DatatypeConverter.parseBase64Binary(privateKeyContent));
        return kf.generatePrivate(keySpecPKCS8);
    }

    public static String encrypt(String data, String publicKey) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException, NoSuchProviderException, InvalidAlgorithmParameterException, InvalidParameterSpecException {
        RSAPublicKey pubKey = (RSAPublicKey)readPublicKey(publicKey);
        byte[] b = data.getBytes(StandardCharsets.UTF_8);
        byte[] byteStr = Base64.decodeBase64(b);

//        AlgorithmParameters algp = AlgorithmParameters.getInstance("OAEP", new BouncyCastleProvider());
//        AlgorithmParameterSpec paramSpec = new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);
//        algp.init(paramSpec);
//        Cipher cipher = Cipher.getInstance("RSA/None/OAEPWithSHA-256AndMGF1Padding", new BouncyCastleProvider());
//        cipher.init(Cipher.ENCRYPT_MODE, pubKey, algp);

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);
        byte[] cipherContent = cipher.doFinal(byteStr);
        return Base64.encodeBase64String(cipherContent);
    }

    public static String decrypt(String data, String privateKey) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException {
        PrivateKey privKey = readPrivateKey(privateKey);
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.PRIVATE_KEY, privKey);
        byte[] cipherContentBytes = Base64.decodeBase64(data.getBytes());
        byte[] decryptedContent = cipher.doFinal(cipherContentBytes);
        return new String(decryptedContent);
    }
}
