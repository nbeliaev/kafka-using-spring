package com.learningkafka.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learningkafka.entity.LibraryEvent;
import com.learningkafka.jpa.LibraryEventsRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class LibraryEventsService {

    private final LibraryEventsRepository repository;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<Integer, String> kafkaTemplate;

    @SneakyThrows
    public void processLibraryEvent(ConsumerRecord<Integer, String> consumerRecord) {
        LibraryEvent libraryEvent = objectMapper.readValue(consumerRecord.value(), LibraryEvent.class);
        log.info("libraryEvent: {}", libraryEvent);

        switch (libraryEvent.getEventType()) {
            case NEW:
                save(libraryEvent);
                break;
            case UPDATE:
                validate(libraryEvent);
                save(libraryEvent);
                break;
            default:
                log.warn("Invalid library event type");
        }
    }

    public void handleRecovery(ConsumerRecord<Integer, String> consumerRecord) {
        Integer key = consumerRecord.key();
        String value = consumerRecord.value();
        ListenableFuture<SendResult<Integer, String>> listenableFuture = kafkaTemplate.send("library-event", key, value);
        listenableFuture.addCallback(
                result -> handleSuccess(key, value, result),
                ex -> handleFail(key, value, ex));
    }

    private void validate(LibraryEvent libraryEvent) {
        if (Objects.isNull(libraryEvent.getId())) {
            throw new IllegalArgumentException("Library event id is missing");
        }
        repository.findById(libraryEvent.getId())
                .orElseThrow(
                        () -> new IllegalArgumentException(String.format("Not a valid library id %d", libraryEvent.getId())));
    }

    private void save(LibraryEvent libraryEvent) {
        libraryEvent.getBook().setLibraryEvent(libraryEvent);
        repository.save(libraryEvent);
        log.info("Successfully Persisted the Library event {}", libraryEvent);
    }

    private void handleFail(Integer key, String value, Throwable e) {
        log.error("Fail to send the message for the key {} and the value {}, cause is {}", key, value, e.getMessage());
        try {
            throw e;
        } catch (Throwable throwable) {
            log.error("Error in handleFail {}", e.getMessage());
        }
    }

    private void handleSuccess(Integer key, String value, SendResult<Integer, String> result) {
        log.info("Message has been sent successfully for the key {} and the value is {}, partition is {}",
                key, value, result.getRecordMetadata().partition());
    }
}