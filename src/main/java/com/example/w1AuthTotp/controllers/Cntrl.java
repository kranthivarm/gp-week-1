package com.example.w1AuthTotp.controllers;
import com.example.w1AuthTotp.Services.OtpService;
import com.example.w1AuthTotp.utils.RsaUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.PrivateKey;
import java.util.Base64;
import java.util.Map;

@RestController
public class Cntrl {

    private final OtpService totpService;
    // path to private key (adjust if needed)
//    private final Path privateKeyPath = Path.of("student_private.pem");
    private final Path privateKeyPath =
            Path.of("D:/projects/Gp projects/week1/w1AuthTotp/w1AuthTotp/student_private.pem");
    // persistent seed path required by spec
    private final Path seedPath = Path.of("D:/projects/Gp projects/week1/w1AuthTotp/w1AuthTotp/data/seed.txt");

    public Cntrl(OtpService totpService) {
        this.totpService = totpService;
    }

    @PostMapping("/decrypt-seed")
    public ResponseEntity<?> decryptSeed(@RequestBody Map<String, String> body) {
        try {
            System.out.println("decrypt seed call came");
            String encB64 = body.get("encrypted_seed");
            if (encB64 == null || encB64.isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Missing encrypted_seed"));
            }
            byte[] cipherBytes = Base64.getDecoder().decode(encB64.trim());

            PrivateKey pk = RsaUtil.loadPrivateKey(privateKeyPath);
            byte[] plain = RsaUtil.decryptOaepSha256(pk, cipherBytes);
            String hexSeed = new String(plain, java.nio.charset.StandardCharsets.UTF_8).trim().toLowerCase();

            // Validate: 64-char hex
            if (hexSeed.length() != 64 || !hexSeed.matches("[0-9a-f]{64}")) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Decryption failed"));
            }

            // Ensure directory exists and write
            Files.createDirectories(seedPath.getParent() == null ? Path.of("/data") : seedPath.getParent());
            Files.writeString(seedPath, hexSeed, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            // set file perms (best-effort)
            try { seedPath.toFile().setReadable(true, true); seedPath.toFile().setWritable(true, true); } catch(Exception ignored){}

            return ResponseEntity.ok(Map.of("status", "ok"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Decryption failed"));
        }
    }

    @GetMapping("/generate-2fa")
    public ResponseEntity<?> generate2fa() {
        try {
            if (!Files.exists(seedPath)) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Seed not decrypted yet"));
            }
            String hexSeed = Files.readString(seedPath).trim().toLowerCase();
            if (hexSeed.length() != 64) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Seed invalid"));
            }
            String code = totpService.generateCurrentCode(hexSeed);
            int validFor = totpService.secondsRemainingInPeriod();
            return ResponseEntity.ok(Map.of("code", code, "valid_for", validFor));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Seed not decrypted yet"));
        }
    }

    @PostMapping("/verify-2fa")
    public ResponseEntity<?> verify2fa(@RequestBody Map<String, String> body) {
        try {
            String code = body.get("code");
            if (code == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Missing code"));
            if (!Files.exists(seedPath)) return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Seed not decrypted yet"));

            String hexSeed = Files.readString(seedPath).trim().toLowerCase();
            boolean valid = totpService.verify(hexSeed, code.trim(), 1); // ±1 period
            return ResponseEntity.ok(Map.of("valid", valid));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Seed not decrypted yet"));
        }
    }
}

//package com.example.w1AuthTotp.controllers;
//
//import com.example.w1AuthTotp.Services.DecryptSeedService;
//import com.example.w1AuthTotp.Services.OtpService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.io.FileInputStream;
//import java.security.KeyStore;
//import java.security.PrivateKey;
//import java.time.Instant;
//import java.util.Map;
//
//
//@RestController
//@RequestMapping("api")
//public class cntrl {
//    @Autowired
//    private DecryptSeedService decryptSeedService;
//    @Autowired
//    private OtpService otpService;
//
//    @GetMapping("/generate-2fa")
//    public ResponseEntity<?> generate() {
//        try {
//            String seed = decryptSeedService.readSeed();
//            String code = otpService.generateCode(seed);
//
//            long valid = 30 - (Instant.now().getEpochSecond() % 30);
//
//            return ResponseEntity.ok(Map.of("code", code, "valid_for", valid));
//
//        } catch (Exception e) {
//            return ResponseEntity.status(500).body(Map.of("error", "Seed missing"));
//        }
//    }
//    @PostMapping("/verify-2fa")
//    public ResponseEntity<?> verify(@RequestBody Map<String, String> body) {
//        try {
//            String code = body.get("code");
//            String seed = decryptSeedService.readSeed();
//            boolean valid = otpService.verify(seed,code);
//
//            return ResponseEntity.ok(Map.of("valid", valid));
//
//        } catch (Exception e) {
//            return ResponseEntity.status(500).body(Map.of("error", "Seed missing"));
//        }
//    }
//    @PostMapping("decrypt-seed")
//    public ResponseEntity<?> seedDecryption(@RequestBody Map<String,String>body){
//        try{
//            String seed=body.get("encrypted_seed");
//            PrivateKey pk= KeyStoreLoader.getPrivateKey();
//            boolean ptr= decryptSeedService.decryptSeed(seed,pk);
//            if(!ptr)return ResponseEntity.status(500).body(Map.of("error","Decryption failed"));
//            return ResponseEntity.ok(Map.of("status", "ok"));
//
//        }catch (Exception e){
//            return ResponseEntity.status(500).body(Map.of("error",e.getMessage()));
//        }
//    }
//
//}
//
//
//
//class KeyStoreLoader {
//
//    private static final String KEYSTORE_PATH = "src/main/resources/keystore.p12";
//    private static final String KEYSTORE_PASSWORD = "changeit"; // 🔹 Replace
//    private static final String KEY_ALIAS = "totpkey";          // 🔹 Replace
//
//    public static PrivateKey getPrivateKey() throws Exception {
//
//        KeyStore keyStore = KeyStore.getInstance("PKCS12");
//        keyStore.load(new FileInputStream(KEYSTORE_PATH), KEYSTORE_PASSWORD.toCharArray());
//
//        return (PrivateKey) keyStore.getKey(KEY_ALIAS, KEYSTORE_PASSWORD.toCharArray());
//    }
//}

