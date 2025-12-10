package com.example.w1AuthTotp;

import com.example.w1AuthTotp.Services.DecryptSeedService;
import com.example.w1AuthTotp.Services.OtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@EnableScheduling
@SpringBootApplication
public class W1AuthTotpApplication {

	public static void main(String[] args) {
		System.out.println("every thing ok");
		SpringApplication.run(W1AuthTotpApplication.class, args);

	}

}


//@Component
//class ScheduledLogger {
//
//	@Autowired
//	DecryptSeedService decryptSeedService;
//
//	@Autowired
//	OtpService otpService;
//
//	private final Path logPath = Paths.get("/cron/last_code.txt");
//
//	@Scheduled(fixedRate = 60000)
//	public void logCode() {
//		try {
//			String seed = decryptSeedService.readSeed();
//			String code = otpService.generateCode(seed);
//
//			String line = LocalDateTime.now(ZoneOffset.UTC) + " - 2FA Code: " + code + "\n";
//			Files.writeString(logPath, line, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
//
//		} catch (Exception ignored) {}
//	}
//}