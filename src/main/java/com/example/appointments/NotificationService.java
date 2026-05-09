package com.example.appointments;

import com.example.appointments.ticket.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private final String NOTIFY_URL = "http://localhost:8081/notify";
    private final int MAX_RETRIES = 3;

    public String sendNotification(Ticket ticket) {
        int attempt = 0;

        while (attempt < MAX_RETRIES) {
            try {
                Map<String, String> ticketDetails = new HashMap<>();
                ticketDetails.put("id", String.valueOf(ticket.getId()));
                ticketDetails.put("userName", ticket.getUserName());
                ticketDetails.put("priority", String.valueOf(ticket.getPriority()));
                ticketDetails.put("status", ticket.getStatus());

                String response = restTemplate.postForObject(
                    NOTIFY_URL,
                    ticketDetails,
                    String.class
                );

                logger.info("Notification sent successfully for Ticket #{}", ticket.getId());
                return response;

            } catch (ResourceAccessException e) {
                attempt++;
                logger.warn("Notification service unreachable. Attempt {} of {}", attempt, MAX_RETRIES);

                if (attempt == MAX_RETRIES) {
                    logger.error("Notification failed after {} attempts for Ticket #{}", MAX_RETRIES, ticket.getId());
                    return "Notification failed — ticket was saved but notification could not be sent";
                }
            }
        }
        return "Notification failed";
    }
}