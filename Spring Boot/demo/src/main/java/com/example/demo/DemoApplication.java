
package com.example.demo;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DemoApplication {

    private static final String[] ENV_KEYS = {
        "DB_HOST", "DB_PORT", "DB_NAME", "DB_USER", "DB_PASSWORD",
        "JWT_SECRET",
        "CORS_ALLOWED_ORIGINS", "SEATSYNC_ADMIN_EMAIL",
        "SEATSYNC_ADMIN_PASSWORD", "SEATSYNC_UPI_ID",
        "SMTP_HOST", "SMTP_PORT", "SMTP_USERNAME", "SMTP_PASSWORD",
        "SMTP_AUTH", "SMTP_STARTTLS",
        "EMAIL_NOTIFICATIONS_ENABLED", "EMAIL_FROM",
        "SMS_NOTIFICATIONS_ENABLED",
        "TWILIO_ACCOUNT_SID", "TWILIO_AUTH_TOKEN",
        "TWILIO_FROM", "DEMO_DATA_ENABLED"
    };

    public static void main(String[] args) {

        Dotenv dotenv = Dotenv.configure()
                .directory("D:/SeatSync/Spring Boot/demo")
                .ignoreIfMissing()
                .load();

        for (String key : ENV_KEYS) {
            String value = dotenv.get(key);

            if (value != null && System.getenv(key) == null) {
                System.setProperty(key, value);
            }
        }

        SpringApplication.run(DemoApplication.class, args);
    }
}
