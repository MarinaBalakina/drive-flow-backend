package com.dealership.order.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class RabbitConfig {
    public static final String EXCHANGE = "dealership-orders";

    public static final String ORDER_SENT_FOR_APPROVAL_QUEUE = "storage-service.order-sent-for-approval";
    public static final String ORDER_APPROVED_QUEUE = "order-service.order-approved";
    public static final String ORDER_REJECTED_QUEUE = "order-service.order-rejected";

    public static final String ORDER_SENT_FOR_APPROVAL_ROUTING_KEY = "order.sent_for_approval";
    public static final String ORDER_APPROVED_ROUTING_KEY = "order.approved";
    public static final String ORDER_REJECTED_ROUTING_KEY = "order.rejected";

    @Bean
    TopicExchange ordersExchange(){
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    Queue orderSentForApprovalQueue(){
        return QueueBuilder.durable(ORDER_SENT_FOR_APPROVAL_QUEUE).build();
    }

    @Bean
    Queue orderApprovedQueue(){
        return QueueBuilder.durable(ORDER_APPROVED_QUEUE).build();
    }

    @Bean
    Queue orderRejectedQueue(){
        return QueueBuilder.durable(ORDER_REJECTED_QUEUE).build();
    }

    @Bean
    Binding orderSentForApprovalBinding(){
        return BindingBuilder.bind(orderSentForApprovalQueue())
                .to(ordersExchange())
                .with(ORDER_SENT_FOR_APPROVAL_ROUTING_KEY);
    }

    @Bean
    Binding orderApprovedBinding(){
        return BindingBuilder.bind(orderApprovedQueue()).to(ordersExchange()).with(ORDER_APPROVED_ROUTING_KEY);
    }

    @Bean
    Binding orderRejectedBinding(){
        return BindingBuilder.bind(orderRejectedQueue()).to(ordersExchange()).with(ORDER_REJECTED_ROUTING_KEY);
    }
}
