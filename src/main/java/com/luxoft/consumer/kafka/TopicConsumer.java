package com.luxoft.consumer.kafka;

import com.luxoft.consumer.model.Track;
import com.luxoft.consumer.repository.TrackRepository;
import com.luxoft.messages.avro.TrackAvro;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TopicConsumer {

    private static final String TOPIC = "track";

    @Autowired
    TrackRepository trackRepository;

    private TopicConsumer() {}

    @KafkaListener(topics = TOPIC)
    public void listenUniversityTopic(TrackAvro trackAvro) {
        log.info("Received message: {}", trackAvro.getResource());

        trackRepository.save(new Track(trackAvro.getUser(), trackAvro.getMethod(), trackAvro.getResource(), trackAvro.getTimestamp()));
    }

}
