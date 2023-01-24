package com.luxoft.consumer.configuration;

import com.luxoft.messages.avro.TrackAvro;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.VoidDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConsumerConfiguration {

    private static final String BOOTSTRAP_SERVERS = ":9092";
    private static final String CONSUMER_GROUP = "first";

    @Bean
    public ConsumerFactory<Void, TrackAvro> consumerFactory() {
        Map<String, Object> properties = new HashMap<>();

        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, CONSUMER_GROUP);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, VoidDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        properties.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);
        properties.put("schema.registry.url", "http://127.0.0.1:8081");

        return new DefaultKafkaConsumerFactory<>(properties);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<Void, TrackAvro> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<Void, TrackAvro> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

}
