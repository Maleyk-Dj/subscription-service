package com.maleyk.subscription_service.service;

import com.maleyk.subscription_service.dto.SubscriptionResponse;
import com.maleyk.subscription_service.model.Subscription;
import com.maleyk.subscription_service.model.SubscriptionType;
import com.maleyk.subscription_service.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${kafka.topics.subscription-expired}")
    private String subscriptionExpiredTopic;

    public SubscriptionResponse getSubscription(String login) {
        Subscription subscription = repository.findById(login)
                .orElseGet(() -> createDefaultSubscription(login));
        return new SubscriptionResponse(subscription.getLogin(), subscription.getSubscriptionType(),
                subscription.getExpiresAt());
    }

    private Subscription createDefaultSubscription(String login) {
        Subscription subscription = new Subscription();
        subscription.setLogin(login);
        subscription.setSubscriptionType(SubscriptionType.FREE);
        return repository.save(subscription);
    }
    @Transactional
    public void downgradeExpiredSubscriptions() {
        List<Subscription> expired = repository.findAllBySubscriptionTypeAndExpiresAtBefore(
                SubscriptionType.PAID, LocalDateTime.now()
        );

        for (Subscription subscription : expired) {
            subscription.setSubscriptionType(SubscriptionType.FREE);
            repository.save(subscription);
            kafkaTemplate.send(subscriptionExpiredTopic,subscription.getLogin());
            log.info("Подписка истекла, понижена до FREE: {}",subscription.getLogin());
        }
    }
}
