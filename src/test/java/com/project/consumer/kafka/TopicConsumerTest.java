package com.project.consumer.kafka;

import com.project.consumer.model.Track;
import com.project.consumer.repository.TrackRepository;
import com.project.messages.avro.TrackAvro;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TopicConsumerTest {

    @InjectMocks
    TopicConsumer topicConsumer;

    @Mock
    TrackRepository trackRepositoryMock;

    @Test
    void shouldVerifyNormalBehaviour() {
        // given
        TrackAvro trackAvro = new TrackAvro("testUser", "POST", "/resource", new DateTime());

        // when
        topicConsumer.listenUniversityTopic(trackAvro);

        // then
        verify(trackRepositoryMock, times(1)).save(Mockito.any(Track.class));
    }

}
