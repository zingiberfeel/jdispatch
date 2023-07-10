package com.potarski.jdispatch.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "emails")
public class Email {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fromAddress;

    @ElementCollection
    private List<String> toAddresses;

    @ElementCollection
    private List<String> ccAddresses;

    private String subject;
    private String body;
    private LocalDateTime timestamp;

    public Email() {

    }

    public Long getId() {
        return id;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public List<String> getToAddresses() {
        return toAddresses;
    }

    public List<String> getCcAddresses() {
        return ccAddresses;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    // Builder pattern implementation
    public static class Builder {
        private Email email;

        public Builder() {
            email = new Email();
        }

        public Builder from(String fromAddress) {
            email.fromAddress = fromAddress;
            return this;
        }

        public Builder to(List<String> toAddresses) {
            email.toAddresses = toAddresses;
            return this;
        }

        public Builder cc(List<String> ccAddresses) {
            email.ccAddresses = ccAddresses;
            return this;
        }

        public Builder subject(String subject) {
            email.subject = subject;
            return this;
        }

        public Builder body(String body) {
            email.body = body;
            return this;
        }

        public Builder timestamp(LocalDateTime timestamp) {
            email.timestamp = timestamp;
            return this;
        }

        public Email build() {
            return email;
        }
    }
}
