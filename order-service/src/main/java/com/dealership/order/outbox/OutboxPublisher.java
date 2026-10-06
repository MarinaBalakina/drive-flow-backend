package com.dealership.order.outbox;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxPublisher {
    private final OutboxRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;

    @Scheduled(fixedDelayString = "${app.outbox.publish-delay-ms:5000}")
    @Transactional
    public void publishPendingMessages(){
        var messages = outboxRepository.findTop50ByPublishedFalseOrderByCreatedAtAsc();

        for (OutboxMessage cur : messages){
            rabbitTemplate.convertAndSend(
                    cur.getExchangeName(),
                    cur.getRoutingKey(),
                    cur.getPayload()
            );

            cur.markPublished();
            outboxRepository.save(cur);
        }
    }
}
