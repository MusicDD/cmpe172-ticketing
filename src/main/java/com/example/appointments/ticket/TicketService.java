package com.example.appointments.ticket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import com.example.appointments.NotificationService;

@Service
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private NotificationService notificationService;

    private static final Logger logger = LoggerFactory.getLogger(TicketService.class);

    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public String submitTicket(Ticket newTicket) {
        int maxRetries = 3;
        int attempt = 0;

        logger.info("Ticket submission started for user: {} Priority: {}", newTicket.getUserName(), newTicket.getPriority());

        while (attempt < maxRetries) {
            try {
                List<Ticket> activeTickets = ticketRepository.findAll()
                    .stream()
                    .filter(t -> t.getHelpRequest().equals(newTicket.getHelpRequest())
                              && t.getStatus().equals("in progress"))
                    .toList();

                if (activeTickets.isEmpty()) {
                    newTicket.setStatus("in progress");
                    ticketRepository.save(newTicket);
                    notificationService.sendNotification(newTicket);
                    logger.info("Ticket submitted successfully for user: {}", newTicket.getUserName());
                    return "Ticket submitted successfully";

                } else {
                    Ticket existing = activeTickets.get(0);

                    if (newTicket.getPriority() > existing.getPriority()) {
                        existing.setStatus("queued");
                        ticketRepository.save(existing);
                        newTicket.setStatus("in progress");
                        ticketRepository.save(newTicket);
                        notificationService.sendNotification(newTicket);
                        logger.warn("Ticket bumped — higher priority ticket took over for help request: {}", newTicket.getHelpRequest());
                        return "Higher priority ticket took over. Previous ticket queued.";

                    } else {
                        newTicket.setStatus("queued");
                        ticketRepository.save(newTicket);
                        notificationService.sendNotification(newTicket);
                        logger.warn("Ticket queued for user: {} — lower priority than active ticket", newTicket.getUserName());
                        return "Ticket queued behind higher priority ticket";
                    }
                }

            } catch (ObjectOptimisticLockingFailureException e) {
                attempt++;
                logger.error("Version mismatch on attempt {} for user: {}", attempt, newTicket.getUserName());
                if (attempt == maxRetries) {
                    logger.error("Ticket submission failed after {} attempts for user: {}", maxRetries, newTicket.getUserName());
                    return "Could not process ticket after " + maxRetries + " attempts. Please try again.";
                }
            }
        }
        return "Ticket submission failed";
    }

    public String updateTicketStatus(int id, String status, String notes) {
        return ticketRepository.findById(id).map(ticket -> {
            ticket.setStatus(status);
            ticketRepository.save(ticket);
            logger.info("Admin updated Ticket #{} to status: {} Notes: {}", id, status, notes);
            return "Ticket #" + id + " updated to " + status + (notes.isEmpty() ? "" : " — Note: " + notes);
        }).orElse("Ticket not found");
    }
}