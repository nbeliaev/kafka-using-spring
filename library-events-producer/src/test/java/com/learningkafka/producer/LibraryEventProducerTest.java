package com.learningkafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learningkafka.controller.LibraryEventFactory;
import com.learningkafka.domain.LibraryEvent;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SettableListenableFuture;

import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LibraryEventProducerTest {

    @InjectMocks
    LibraryEventProducer libraryEventProducer;

    @Mock
    KafkaTemplate<Integer, String> kafkaTemplate;

    @Spy
    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void sendEventUsingProducerRecord_failure() throws JsonProcessingException, ExecutionException, InterruptedException {
        LibraryEvent libraryEvent = LibraryEventFactory.createLibraryEvent();
        SettableListenableFuture<SendResult<Integer, String>> future = new SettableListenableFuture<>();
        future.setException(new RuntimeException("Exception calling Kafka"));

        when(kafkaTemplate.send(isA(ProducerRecord.class))).thenReturn(future);

        assertThrows(Exception.class, () -> libraryEventProducer.sendEventUsingProducerRecord(libraryEvent).get());
    }

    @Test
    void sendEventUsingProducerRecord_success() throws JsonProcessingException, ExecutionException, InterruptedException {
        LibraryEvent libraryEvent = LibraryEventFactory.createLibraryEvent();
        SettableListenableFuture<SendResult<Integer, String>> future = new SettableListenableFuture<>();
        ProducerRecord<Integer, String> producerRecord =
                new ProducerRecord<>("library-events", libraryEvent.getId(), objectMapper.writeValueAsString(libraryEvent));

        RecordMetadata recordMetadata =
                new RecordMetadata(new TopicPartition("library-events", 1),
                        1, 1, 342, System.currentTimeMillis(), 1, 2);
        SendResult<Integer, String> sendResult = new SendResult<>(producerRecord, recordMetadata);
        future.set(sendResult);

        when(kafkaTemplate.send(isA(ProducerRecord.class))).thenReturn(future);

        ListenableFuture<SendResult<Integer, String>> listenableFuture = libraryEventProducer.sendEventUsingProducerRecord(libraryEvent);

        SendResult<Integer, String> result = listenableFuture.get();
        assertThat(result.getRecordMetadata().partition()).isEqualTo(1);
    }
}