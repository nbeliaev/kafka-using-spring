package com.learningkafka.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.learningkafka.domain.LibraryEvent;
import com.learningkafka.domain.LibraryEventType;
import com.learningkafka.producer.LibraryEventProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
public class LibraryEventsController {

    private final LibraryEventProducer libraryEventProducer;

    @PostMapping("/v1/libraryevent")
    public ResponseEntity<LibraryEvent> createLibraryEvent(@RequestBody @Valid LibraryEvent libraryEvent) throws JsonProcessingException {
        libraryEvent.setEventType(LibraryEventType.NEW);
        libraryEventProducer.sendEventUsingProducerRecord(libraryEvent);
        return ResponseEntity.status(HttpStatus.CREATED).body(libraryEvent);
    }

    @PutMapping("/v1/libraryevent")
    public ResponseEntity<LibraryEvent> updateLibraryEvent(@RequestBody @Valid LibraryEvent libraryEvent) {
        return ResponseEntity.ok(libraryEvent);
    }
}
