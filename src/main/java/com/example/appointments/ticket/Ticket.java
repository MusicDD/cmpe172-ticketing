package com.example.appointments.ticket;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

@Entity
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String userName;
    private String title;
    private String helpRequest;
    private String date;
    private String time;
    private String status;
    private int priority; // 1=employee, 2=manager, 3=executive

    @Version
    private int version; // optimistic locking

    public Ticket() {}

    public Ticket(int id, String userName, String title, String helpRequest,
                  String date, String time, String status, int priority) {
        this.id = id;
        this.userName = userName;
        this.title = title;
        this.helpRequest = helpRequest;
        this.date = date;
        this.time = time;
        this.status = status;
        this.priority = priority;
    }

    // Getters
    public int getId() { return id; }
    public String getUserName() { return userName; }
    public String getTitle() { return title; }
    public String getHelpRequest() { return helpRequest; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getStatus() { return status; }
    public int getPriority() { return priority; }
    public int getVersion() { return version; }

    // Setters
    public void setStatus(String status) { this.status = status; }
    public void setPriority(int priority) { this.priority = priority; }
    public void setUserName(String userName) { this.userName = userName; }
}