package com.maleyk.subscription_service.service;

import com.maleyk.subscription_service.dto.SubscriptionResponse;
import com.maleyk.subscription_service.model.Subscription;
import com.maleyk.subscription_service.model.SubscriptionType;
import com.maleyk.subscription_service.outbox.OutboxMessage;
import com.maleyk.subscription_service.outbox.OutboxMessageRepository;
import com.maleyk.subscription_service.outbox.OutboxStatus;
import com.maleyk.subscription_service.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository repository;

    @Mock
    private OutboxMessageRepository outboxMessageRepository;

    @InjectMocks
    private SubscriptionService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "subscriptionExpiredTopic", "subscription-expired");
    }

    @Test
    void getSubscription_shouldReturnExisting_whenFound() {
        Subscription existing = new Subscription();
        existing.setLogin("malika");
        existing.setSubscriptionType(SubscriptionType.PAID);
        existing.setExpiresAt(LocalDateTime.now().plusDays(30));

        when(repository.findById("malika")).thenReturn(Optional.of(existing));

        SubscriptionResponse response = service.getSubscription("malika");

        assertEquals("malika", response.login());
        assertEquals(SubscriptionType.PAID, response.subscriptionType());
        verify(repository, never()).save(any());
    }

    @Test
    void getSubscription_shouldCreateDefaultFree_whenNotFound() {
        when(repository.findById("newUser")).thenReturn(Optional.empty());
        when(repository.save(any(Subscription.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SubscriptionResponse response = service.getSubscription("newUser");

        assertEquals("newUser", response.login());
        assertEquals(SubscriptionType.FREE, response.subscriptionType());
        assertNull(response.expiresAt());

        ArgumentCaptor<Subscription> captor = ArgumentCaptor.forClass(Subscription.class);
        verify(repository).save(captor.capture());
        assertEquals("newUser", captor.getValue().getLogin());
        assertEquals(SubscriptionType.FREE, captor.getValue().getSubscriptionType());
    }

    @Test
    void downgradeExpiredSubscriptions_shouldDowngradeAndCreatePendingOutboxMessage_whenExpiredFound() {
        Subscription expired = new Subscription();
        expired.setLogin("expiredUser");
        expired.setSubscriptionType(SubscriptionType.PAID);
        expired.setExpiresAt(LocalDateTime.now().minusDays(1));

        when(repository.findAllBySubscriptionTypeAndExpiresAtBefore(eq(SubscriptionType.PAID), any(LocalDateTime.class)))
                .thenReturn(List.of(expired));

        service.downgradeExpiredSubscriptions();

        assertEquals(SubscriptionType.FREE, expired.getSubscriptionType());
        verify(repository).save(expired);

        ArgumentCaptor<OutboxMessage> outboxCaptor = ArgumentCaptor.forClass(OutboxMessage.class);
        verify(outboxMessageRepository).save(outboxCaptor.capture());
        OutboxMessage saved = outboxCaptor.getValue();
        assertEquals("subscription-expired", saved.getTopic());
        assertEquals("expiredUser", saved.getPayload());
        assertEquals(OutboxStatus.PENDING, saved.getStatus());
    }

    @Test
    void downgradeExpiredSubscriptions_shouldDoNothing_whenNoneExpired() {
        when(repository.findAllBySubscriptionTypeAndExpiresAtBefore(eq(SubscriptionType.PAID), any(LocalDateTime.class)))
                .thenReturn(List.of());

        service.downgradeExpiredSubscriptions();

        verify(repository, never()).save(any());
        verify(outboxMessageRepository, never()).save(any());
    }
}