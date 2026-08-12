package com.maleyk.subscription_service.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxSubscriptionPoller {

    private final OutboxMessageRepository outboxMessageRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 5000)
    @SchedulerLock(name = "subscriptionOutboxPoller", lockAtLeastFor = "PT2S", lockAtMostFor = "PT30S")
    @Transactional
    public void pollAndSend() {
        List<OutboxMessage> pending = outboxMessageRepository.findAllByStatus(OutboxStatus.PENDING);

        for (OutboxMessage message : pending) {
            kafkaTemplate.send(message.getTopic(), message.getPayload());
            message.setStatus(OutboxStatus.SENT);
            outboxMessageRepository.save(message);
            log.info("Outbox-сообщение отправлено: topic={}, payload={}", message.getTopic(), message.getPayload());
        }
    }
}