package com.example.appointments;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class MockNotificationController {

    @PostMapping("/notify")
    public String notify(@RequestBody Map<String, String> ticketDetails) {
        System.out.println("Notification received for ticket: " + ticketDetails);
        return "Notification sent for ticket #" + ticketDetails.get("id");
    }
}