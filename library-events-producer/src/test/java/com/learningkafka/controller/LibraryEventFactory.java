package com.learningkafka.controller;

import com.learningkafka.domain.Book;
import com.learningkafka.domain.LibraryEvent;

public class LibraryEventFactory {

    public static LibraryEvent createLibraryEvent() {
        Book book = Book.builder()
                .id(123)
                .author("John")
                .name("Kafka using Spring Boot")
                .build();

        return LibraryEvent.builder()
                .book(book)
                .build();
    }
}