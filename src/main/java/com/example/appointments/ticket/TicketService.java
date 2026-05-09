package com.example.appointments.ticket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    // Get all tickets
    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    // Submit a ticket with priority and retry logic
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public String submitTicket(Ticket newTicket) {
        int maxRetries = 3;
        int attempt = 0;

        while (attempt < maxRetries) {
            try {
                // Find any active ticket for the same help request
                List<Ticket> activeTickets = ticketRepository.findAll()
                    .stream()
                    .filter(t -> t.getHelpRequest().equals(newTicket.getHelpRequest())
                              && t.getStatus().equals("in progress"))
                    .toList();

                if (activeTickets.isEmpty()) {
                    // No conflict — save directly
                    newTicket.setStatus("in progress");
                    ticketRepository.save(newTicket);
                    return "Ticket submitted successfully";

                } else {
                    Ticket existing = activeTickets.get(0);

                    if (newTicket.getPriority() > existing.getPriority()) {
                        // New ticket has higher priority — bump existing
                        existing.setStatus("queued");
                        ticketRepository.save(existing);
                        newTicket.setStatus("in progress");
                        ticketRepository.save(newTicket);
                        return "Higher priority ticket took over. Previous ticket queued.";

                    } else {
                        // Lower or equal priority — just queue it
                        newTicket.setStatus("queued");
                        ticketRepository.save(newTicket);
                        return "Ticket queued behind higher priority ticket";
                    }
                }

            } catch (ObjectOptimisticLockingFailureException e) {
                // Version mismatch — another transaction got there first, retry
                attempt++;
                if (attempt == maxRetries) {
                    return "Could not process ticket after " + maxRetries + " attempts. Please try again.";
                }
            }
        }
        return "Ticket submission failed";
    }
}