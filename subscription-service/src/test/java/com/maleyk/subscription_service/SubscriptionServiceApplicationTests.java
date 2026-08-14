package com.maleyk.subscription_service;

import com.maleyk.subscription_service.controller.SubscriptionController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SubscriptionServiceApplicationTests extends BaseIntegrationTest {

	@Autowired
	private ApplicationContext context;

	@Test
	void contextLoads() {
		assertThat(context).isNotNull();
		assertThat(context.getBean(SubscriptionController.class)).isNotNull();
	}
}