package com.example.kakafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@ShellComponent
@Log4j2
public class ShellPoint {

    @Value("${net.example.topic.name}")
    String topicName;

    @Value("${net.example.topic.partitions}")
    Integer numPartitions;

    @Value("${net.example.topic.replication}")
    Short replicationFactor;

    @Autowired
    KafkaProperties kafkaProperties;

    ObjectMapper mapper = new ObjectMapper();

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    NewTopic topic;

    public static void futureSneaker(CompletableFuture future) {
        try {
            future.get();
        } catch (Exception ex) {
            log.error("Something wrong", ex);
        }
    }

    Consumer<String, String> createConsumer(String topicName) {
        Map<String, Object> props = KafkaTestUtils.consumerProps(kafkaProperties.getBootstrapServers().get(0),
                "testGroup", "true");
        Consumer<String, String> consumer =
                new DefaultKafkaConsumerFactory<>(props, StringDeserializer::new, StringDeserializer::new)
                        .createConsumer();
        consumer.subscribe(Collections.singletonList(topicName));
        return consumer;
    }

    @ShellMethod("Shows topic name, partitions number and replication factor")
    public String showStats() {
        return String.format("topic name: %s, partitions number: %d, replication factor: %d",
                topicName, numPartitions, replicationFactor);
    }

    @ShellMethod("consume")
    public String consume() throws JsonProcessingException {
        Consumer<String, String> consumer = createConsumer(topicName);
        ConsumerRecords<String, String> records = KafkaTestUtils.getRecords(consumer, 5000);
        ArrayList<String> actualValues = new ArrayList<>();
        records.forEach(s -> actualValues.add(s.value()));
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(actualValues);
    }

    @ShellMethod("produce")
    public void produce(@ShellOption String value) {
        String key = "kafka-key";
        kafkaTemplate.send(topic.name(), key, value).addCallback(result -> {
            RecordMetadata recordMetadata = result.getRecordMetadata();
            log.info("Produced to topic {}, partition {}, offset {}",
                    recordMetadata.topic(), recordMetadata.partition(), recordMetadata.offset());
        }, ex -> {
            throw new RuntimeException(ex);
        });
        kafkaTemplate.flush();
    }

    @Async
    public CompletableFuture<Void> runTask(String value) {
        log.info(value);
        String key = UUID.randomUUID().toString();
        kafkaTemplate.send(topic.name(), key, value).addCallback(result -> {
            RecordMetadata recordMetadata = result.getRecordMetadata();
            log.info("Produced to topic {}, partition {}, offset {}",
                    recordMetadata.topic(), recordMetadata.partition(), recordMetadata.offset());
        }, ex -> {
            throw new RuntimeException(ex);
        });
        kafkaTemplate.flush();
        log.info(value);
        return CompletableFuture.completedFuture(null);
    }

    @ShellMethod("produce simultaneous")
    public void produceSimultaneous(@ShellOption String value) {
        List<CompletableFuture> futures = new LinkedList<>();
        for (int i = 0; i < 100; i++) {
            CompletableFuture future = runTask("Range pointer " + i);
            futures.add(future);
        }
        log.info("-------------------------------------------------------------------------");
        for (CompletableFuture future : futures) {
            futureSneaker(future);
        }
        log.info("-------------------------------------------------------------------------");
    }
}
