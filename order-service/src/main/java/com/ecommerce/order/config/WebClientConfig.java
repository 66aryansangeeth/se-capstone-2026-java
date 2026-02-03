package com.ecommerce.order.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

        @Value("${product.service.url}")
        private String productUrl;

        @Value("${stripe.api.url:https://api.stripe.com/v1}")
        private String stripeUrl;

        @Value("${stripe.secret-key}")
        private String stripeSecretKey;

        @Bean
        public WebClient productWebClient(WebClient.Builder builder) {
            return builder.clone()
                    .baseUrl(productUrl)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();
        }

        @Bean
        public WebClient stripeWebClient(WebClient.Builder builder) {

            System.out.println("Stripe Client initializing with base URL: " + stripeUrl);
            return builder.clone()
                    .baseUrl(stripeUrl)
                    .defaultHeader("Authorization", "Bearer " + stripeSecretKey)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();
        }
    }

