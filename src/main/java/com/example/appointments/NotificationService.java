package com.example.appointments;

import com.example.appointments.ticket.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;
import java.util.HashMap;
import java.util.Map;

@Service
public class NotificationService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String NOTIFY_URL = "http://localhost:8080/notify";
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

                System.out.println("INFO - Notification sent successfully for Ticket #" + ticket.getId());
                return response;

            } catch (ResourceAccessException e) {
                attempt++;
                System.out.println("WARN - Notification service unreachable. Attempt " 
                + attempt + " of " + MAX_RETRIES);

                if (attempt == MAX_RETRIES) {
                    System.out.println("ERROR - Notification failed after " + MAX_RETRIES + 
                    " attempts for Ticket #" + ticket.getId());
                    return "Notification failed — ticket was saved but notification could not be sent";
                }
            }
        }
        return "Notification failed";
    }
}