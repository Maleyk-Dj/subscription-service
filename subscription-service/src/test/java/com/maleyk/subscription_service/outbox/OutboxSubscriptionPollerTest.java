package com.maleyk.subscription_service.outbox;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxSubscriptionPollerTest {

    @Mock
    private OutboxMessageRepository outboxMessageRepository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    private OutboxSubscriptionPoller poller;

    @Test
    void pollAndSend_shouldSendAndMarkSent_whenPendingExists() {
        OutboxMessage pending = new OutboxMessage();
        pending.setTopic("subscription-expired");
        pending.setPayload("malika");
        pending.setStatus(OutboxStatus.PENDING);

        when(outboxMessageRepository.findAllByStatus(OutboxStatus.PENDING)).thenReturn(List.of(pending));
        when(kafkaTemplate.send(anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        poller.pollAndSend();

        verify(kafkaTemplate).send("subscription-expired", "malika");
        assertEquals(OutboxStatus.SENT, pending.getStatus());
        verify(outboxMessageRepository).save(pending);
    }

    @Test
    void pollAndSend_shouldDoNothing_whenNoPending() {
        when(outboxMessageRepository.findAllByStatus(OutboxStatus.PENDING)).thenReturn(List.of());

        poller.pollAndSend();

        verifyNoInteractions(kafkaTemplate);
        verify(outboxMessageRepository, never()).save(any());
    }
}