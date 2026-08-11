package com.maleyk.subscription_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
@Getter
@Setter
public class Subscription {
    @Id
    private String login;

    @Enumerated(EnumType.STRING)
    private SubscriptionType subscriptionType;

    private LocalDateTime expiresAt;
}
