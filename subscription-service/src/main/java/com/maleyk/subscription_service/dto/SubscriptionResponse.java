package com.maleyk.subscription_service.dto;

import com.maleyk.subscription_service.model.SubscriptionType;

import java.time.LocalDateTime;

public record SubscriptionResponse(
        String login,
        SubscriptionType subscriptionType,
        LocalDateTime expiresAt
) {
}
