package com.learningkafka.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learningkafka.domain.LibraryEvent;
import com.learningkafka.producer.LibraryEventProducer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LibraryEventsController.class)
public class LibraryEventsControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    LibraryEventProducer libraryEventProducer;

    @Test
    void createLibraryEvent() throws Exception {
        LibraryEvent libraryEvent = LibraryEventFactory.createLibraryEvent();
        mockMvc.perform(post("/v1/libraryevent")
                        .header("content-type", MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(libraryEvent)))
                .andExpect(status().isCreated());
    }

    @Test
    void createLibraryEvent_4xx() throws Exception {
        LibraryEvent libraryEvent = LibraryEventFactory.createLibraryEvent();
        libraryEvent.setBook(null);
        mockMvc.perform(post("/v1/libraryevent")
                        .header("content-type", MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(libraryEvent)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void updateLibraryEvent() throws Exception {
        LibraryEvent libraryEvent = LibraryEventFactory.createLibraryEvent();
        libraryEvent.setId(111);
        mockMvc.perform(put("/v1/libraryevent")
                        .header("content-type", MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(libraryEvent)))
                .andExpect(status().isOk());
    }

    @Test
    void updateLibraryEvent_4xx() throws Exception {
        LibraryEvent libraryEvent = LibraryEventFactory.createLibraryEvent();
        mockMvc.perform(put("/v1/libraryevent")
                        .header("content-type", MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(libraryEvent)))
                .andExpect(status().is4xxClientError());
    }
}