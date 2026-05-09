package com.example.appointments;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.appointments.ticket.Ticket;
import com.example.appointments.ticket.TicketService;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private TicketService ticketService;

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/appointments")
    @ResponseBody
    public List<Ticket> appointments() {
        return ticketService.getAllTickets();
    }

    @PostMapping("/submit")
    @ResponseBody
    public String submitTicket(
        @RequestParam String userName,
        @RequestParam String title,
        @RequestParam String helpRequest,
        @RequestParam int priority
    ) {
        Ticket ticket = new Ticket(
            0, userName, title, helpRequest,
            "2026-03-27", "09:00", "pending", priority
        );
        return ticketService.submitTicket(ticket);
    }
}