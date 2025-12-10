//package com.example.w1AuthTotp.Services;
//
//import org.apache.commons.codec.binary.Base32;
//import org.apache.commons.codec.binary.Hex;
//import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator;
//import org.springframework.stereotype.Service;
//
//import javax.crypto.spec.SecretKeySpec;
//import java.time.Instant;
//
//@Service
//public class OtpService {
//
//    private final TimeBasedOneTimePasswordGenerator totpGenerator;
//
//    public OtpService() throws Exception {
//        this.totpGenerator = new TimeBasedOneTimePasswordGenerator();
//    }
//
//    public String generateCode(String hexSeed) throws Exception {
//        byte[] bytes = Hex.decodeHex(hexSeed.toCharArray());
//
//        Base32 base32 = new Base32();
//        String base32Secret = base32.encodeToString(bytes);
//
//        SecretKeySpec key = new SecretKeySpec(bytes, "HmacSHA1");
//        int code = totpGenerator.generateOneTimePassword(key, Instant.now());
//
//        return String.format("%06d", code);
//    }
//
//    public boolean verify(String hexSeed, String code) throws Exception {
//        return generateCode(hexSeed).equals(code);
//    }
//}


package com.example.w1AuthTotp.Services;
import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Hex;
//import org.apache.commons.codec.binary.Hex;

import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator;
import java.util.concurrent.TimeUnit;


@Service
public class OtpService {
    private final TimeBasedOneTimePasswordGenerator totpGenerator;

    public OtpService() throws NoSuchAlgorithmException {
        // TOTP with SHA-1, 30s, 6 digits (default constructor uses HmacSHA1 and 30s)
//        this.totpGenerator = new TimeBasedOneTimePasswordGenerator(30, TimeUnit.SECONDS, 6);
//        this.totpGenerator = new TimeBasedOneTimePasswordGenerator(30, TimeUnit.SECONDS);
        this.totpGenerator= new TimeBasedOneTimePasswordGenerator(Duration.ofSeconds(30), 6);

    }

    /** Convert 64-char hex seed -> Base32 string required by TOTP secret */
    public String hexToBase32(String hexSeed) throws DecoderException {
        byte[] bytes = Hex.decodeHex(hexSeed.toCharArray());
        Base32 base32 = new Base32();
        return base32.encodeToString(bytes).replace("=", ""); // some libs strip padding
    }

    private SecretKeySpec secretKeyFromHex(String hexSeed) throws Exception {
        byte[] keyBytes = Hex.decodeHex(hexSeed.toCharArray());
        // TOTP expects a secret key for HmacSHA1
        return new SecretKeySpec(keyBytes, "HmacSHA1");
    }

    public String generateCurrentCode(String hexSeed) throws Exception {
        SecretKeySpec key = secretKeyFromHex(hexSeed);
        int code = totpGenerator.generateOneTimePassword(key, Instant.now());
        return String.format("%06d", code);
    }

    public int secondsRemainingInPeriod() {
        long epochSec = Instant.now().getEpochSecond();
        int period = (int)(epochSec % 30);
        return 30 - period;
    }

    /** Verify with ±window periods (window = 1 => ±30s). Returns true if any matches. */
    public boolean verify(String hexSeed, String code, int window) throws Exception {
        SecretKeySpec key = secretKeyFromHex(hexSeed);
        Instant now = Instant.now();
        for (int i = -window; i <= window; i++) {
            Instant t = now.plusSeconds(i * 30L);
            int candidate = totpGenerator.generateOneTimePassword(key, t);
            if (String.format("%06d", candidate).equals(code)) return true;
        }
        return false;
    }
}
