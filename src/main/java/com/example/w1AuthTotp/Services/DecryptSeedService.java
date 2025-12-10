package com.example.w1AuthTotp.Services;

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.util.Base64;

@Service
public class DecryptSeedService {

    private final Path seedPath = Paths.get("/data/seed.txt");

    public boolean decryptSeed(String encryptedSeedBase64, PrivateKey privateKey) {
        try {
            byte[] encryptedData = Base64.getDecoder().decode(encryptedSeedBase64);

            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);

            byte[] decryptedBytes = cipher.doFinal(encryptedData);
            String seed = new String(decryptedBytes);

            Files.write(seedPath, seed.getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String readSeed() throws IOException {
        return Files.readString(seedPath);
    }
}

