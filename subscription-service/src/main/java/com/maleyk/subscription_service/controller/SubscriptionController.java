package com.maleyk.subscription_service.controller;

import com.maleyk.subscription_service.dto.SubscriptionResponse;
import com.maleyk.subscription_service.exception.SubscriptionAccessDeniedException;
import com.maleyk.subscription_service.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService service;

    @GetMapping("/{login}")
    public SubscriptionResponse getSubscription(@PathVariable String login,
                                                @RequestHeader("X-User-Login") String requesterLogin) {
        if (!login.equals(requesterLogin)) {
            throw new SubscriptionAccessDeniedException("Нет доступа к чужой подписке");
        }
        return service.getSubscription(login);
    }
}
