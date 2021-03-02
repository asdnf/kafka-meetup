package com.example.kakafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Executable;
import java.util.concurrent.Executor;

@Configuration
public class Config {

    @Value("${net.example.topic.name}")
    String topicName;

    @Value("${net.example.topic.partitions}")
    Integer numPartitions;

    @Value("${net.example.topic.replication}")
    Short replicationFactor;

    @Bean
    NewTopic testTopic() {
        return new NewTopic(topicName, numPartitions, replicationFactor);
    }

    @Bean
    Executor executor() {
        ThreadPoolTaskExecutor tp = new ThreadPoolTaskExecutor();
        tp.setCorePoolSize(10);
        tp.setMaxPoolSize(10);
        tp.setQueueCapacity(100);
        return tp;
    }
}
