package com.maleyk.subscription_service.controller;

import com.maleyk.subscription_service.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SubscriptionControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getSubscription_shouldAutoCreateFreeSubscription_whenLoginUnknown() throws Exception {
        mockMvc.perform(get("/api/subscriptions/{login}", "brandNewUser")
                        .header("X-User-Login", "brandNewUser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("brandNewUser"))
                .andExpect(jsonPath("$.subscriptionType").value("FREE"));
    }

    @Test
    void getSubscription_shouldReturn403_whenRequesterIsNotOwner() throws Exception {
        mockMvc.perform(get("/api/subscriptions/{login}", "someoneElse")
                        .header("X-User-Login", "malika"))
                .andExpect(status().isForbidden());
    }
}