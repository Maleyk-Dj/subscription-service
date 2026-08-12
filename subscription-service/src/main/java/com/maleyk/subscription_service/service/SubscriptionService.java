package com.maleyk.subscription_service.service;

import com.maleyk.subscription_service.dto.SubscriptionResponse;
import com.maleyk.subscription_service.model.Subscription;
import com.maleyk.subscription_service.model.SubscriptionType;
import com.maleyk.subscription_service.outbox.OutboxMessage;
import com.maleyk.subscription_service.outbox.OutboxMessageRepository;
import com.maleyk.subscription_service.outbox.OutboxStatus;
import com.maleyk.subscription_service.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository repository;
    private final OutboxMessageRepository outboxMessageRepository;

    @Value("${kafka.topics.subscription-expired}")
    private String subscriptionExpiredTopic;

    @Transactional(readOnly = true)
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
            OutboxMessage message = new OutboxMessage();
            message.setTopic(subscriptionExpiredTopic);
            message.setPayload(subscription.getLogin());
            message.setStatus(OutboxStatus.PENDING);
            message.setCreatedAt(LocalDateTime.now());
            outboxMessageRepository.save(message);
            log.info("Подписка истекла, понижена до FREE: {}", subscription.getLogin());
        }
    }
}
