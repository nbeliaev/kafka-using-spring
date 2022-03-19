package com.learningkafka.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learningkafka.entity.LibraryEvent;
import com.learningkafka.jpa.LibraryEventsRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class LibraryEventsService {

    private final LibraryEventsRepository repository;
    private final ObjectMapper objectMapper;

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
}