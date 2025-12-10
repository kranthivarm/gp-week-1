package com.example.w1AuthTotp.utils;


import org.bouncycastle.asn1.pkcs.RSAPrivateKey;
import org.bouncycastle.util.io.pem.PemReader;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.security.spec.MGF1ParameterSpec;




import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.spec.*;
import java.util.Base64;

public class RsaUtil {

    /** Read a PEM private key (PKCS#8 or PKCS#1). This supports common PEM encodings. */
    public static PrivateKey loadPrivateKey(Path pemPath) throws Exception {
        String pem = Files.readString(pemPath);
        pem = pem.replaceAll("\\r", "");
        if (pem.contains("BEGIN PRIVATE KEY")) {
            String b64 = pem.replaceAll("-----BEGIN PRIVATE KEY-----", "")
                    .replaceAll("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");
            byte[] keyBytes = Base64.getDecoder().decode(b64);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(spec);
        } else if (pem.contains("BEGIN RSA PRIVATE KEY")) {
            // convert PKCS#1 to PKCS#8
            String b64 = pem.replaceAll("-----BEGIN RSA PRIVATE KEY-----", "")
                    .replaceAll("-----END RSA PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");
            byte[] pkcs1 = Base64.getDecoder().decode(b64);
            // wrap PKCS#1 in PKCS#8
            RSAPrivateKey rsa = RSAPrivateKey.getInstance(pkcs1);
            // Construct PKCS8 structure via RSAPrivateCrtKeySpec
            RSAPrivateKeySpec spec = new RSAPrivateKeySpec(rsa.getModulus(), rsa.getPrivateExponent());
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(spec);
        } else {
            throw new IllegalArgumentException("Unrecognized PEM format for private key");
        }
    }

    /** Decrypt using RSA/OAEP with SHA-256 and MGF1(SHA-256) */
    public static byte[] decryptOaepSha256(PrivateKey priv, byte[] ciphertext) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        OAEPParameterSpec oaepParams = new OAEPParameterSpec(
                "SHA-256",
                "MGF1",
                MGF1ParameterSpec.SHA256,
                PSource.PSpecified.DEFAULT
        );
        cipher.init(Cipher.DECRYPT_MODE, priv, oaepParams);
        return cipher.doFinal(ciphertext);
    }
}
