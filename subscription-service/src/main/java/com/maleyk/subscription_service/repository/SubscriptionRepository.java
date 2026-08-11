package com.maleyk.subscription_service.repository;

import com.maleyk.subscription_service.model.Subscription;
import com.maleyk.subscription_service.model.SubscriptionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, String> {

    List<Subscription> findAllBySubscriptionTypeAndExpiresAtBefore
            (SubscriptionType type, LocalDateTime now);

}
