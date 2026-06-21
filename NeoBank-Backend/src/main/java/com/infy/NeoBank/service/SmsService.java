package com.infy.NeoBank.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;

@Slf4j
@Service
public class SmsService {

    @Value("${twilio.account.sid:}")
    private String accountSid;

    @Value("${twilio.auth.token:}")
    private String authToken;

    @Value("${twilio.phone.number:}")
    private String twilioPhoneNumber;

    private final RestTemplate restTemplate = new RestTemplate();

    @Async
    public void sendSms(String toPhoneNumber, String messageText) {
        log.info("Preparing to send SMS to: {}", toPhoneNumber);
        log.info("SMS Content:\n----------------------------------------\n{}\n----------------------------------------", messageText);

        if (accountSid == null || accountSid.isBlank() || accountSid.contains("YOUR_TWILIO") ||
            authToken == null || authToken.isBlank() || authToken.contains("YOUR_TWILIO")) {
            log.info("Twilio API credentials not configured. SMS logging fallback active.");
            return;
        }

        try {
            String url = "https://api.twilio.com/2010-04-01/Accounts/" + accountSid + "/Messages.json";

            // Basic Auth Header
            String auth = accountSid + ":" + authToken;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Authorization", "Basic " + encodedAuth);

            // Twilio Payload
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("To", formatPhoneNumber(toPhoneNumber));
            map.add("From", twilioPhoneNumber);
            map.add("Body", messageText);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("SMS successfully sent to {} via Twilio.", toPhoneNumber);
            } else {
                log.error("Twilio returned non-2xx status: {}. Response: {}", response.getStatusCode(), response.getBody());
            }

        } catch (Exception e) {
            log.error("Failed to send SMS to {} via Twilio: {}. (The SMS text was printed to logs above.)", toPhoneNumber, e.getMessage());
        }
    }

    private String formatPhoneNumber(String phone) {
        String trimmed = phone.trim();
        // If it already contains a country code, return as is. Otherwise prepend +91 (India) as default since the user's local timezone is IST
        if (trimmed.startsWith("+")) {
            return trimmed;
        }
        if (trimmed.length() == 10) {
            return "+91" + trimmed;
        }
        return trimmed;
    }
}
