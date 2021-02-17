package com.example.kakafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.SuccessCallback;

import java.util.stream.LongStream;

@Component
@RequiredArgsConstructor
@Log4j2
public class Producer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final NewTopic topic;

    @EventListener(ApplicationStartedEvent.class)
    public void produce() {
        LongStream.range(0, 10).forEach(i -> {
            String key = "kafka-key";
            String value = "kkk" + i;
            kafkaTemplate.send(topic.name(), key, value).addCallback((SuccessCallback<SendResult<String, String>>) result -> {
                RecordMetadata recordMetadata = result.getRecordMetadata();
                log.info("Produced to topic {}, partition {}, offset {}",
                        recordMetadata.topic(), recordMetadata.partition(), recordMetadata.offset());
            }, ex -> {

            });
        });
        kafkaTemplate.flush();
    }
}
