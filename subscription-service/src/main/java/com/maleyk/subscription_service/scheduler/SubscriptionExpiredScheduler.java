package com.maleyk.subscription_service.scheduler;

import com.maleyk.subscription_service.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionExpiredScheduler {

    private final SubscriptionService service;

    @Scheduled(fixedDelay = 60000)
    @SchedulerLock(name = "subscriptionExpirationCheck",
            lockAtLeastFor = "PT10S", lockAtMostFor = "PT50S")
    public void checkExpiredSubscriptions() {
        service.downgradeExpiredSubscriptions();
    }
}
