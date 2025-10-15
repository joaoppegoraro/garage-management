package br.com.garage_management.controller;

import br.com.garage_management.domain.dto.EntryEventDto;
import br.com.garage_management.domain.dto.ExitEventDto;
import br.com.garage_management.domain.dto.ParkedEventDto;
import br.com.garage_management.domain.dto.WebhookEvent;
import br.com.garage_management.service.ParkingService;
import br.com.garage_management.util.TestFactoryUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WebhookController.class)
class WebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ParkingService parkingService;

    @TestConfiguration
    static class TestConfig {

        @Bean
        public ParkingService parkingService() {
            return mock(ParkingService.class);
        }
    }

    private static Stream<Arguments> webhookEventProvider() {
        return Stream.of(
                Arguments.of(
                        TestFactoryUtil.createEntryEventDto("ABC-1234", LocalDateTime.of(2025, 10, 15, 14, 0, 0))
                ),
                Arguments.of(
                        TestFactoryUtil.createParkedEventDto("DEF-5678")
                ),
                Arguments.of(
                        TestFactoryUtil.createExitEventDto("GHI-9012", LocalDateTime.of(2025, 10, 15, 16, 30, 0))
                )
        );
    }

    @ParameterizedTest
    @MethodSource("webhookEventProvider")
    @DisplayName("Deve receber um evento de webhook, chamar o serviço correto e retornar status 200 OK")
    void handleWebhookEvent_shouldCallCorrectServiceAndReturnOk(WebhookEvent event) throws Exception {
        String eventAsJson = objectMapper.writeValueAsString(event);

        mockMvc.perform(post("/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventAsJson))
                .andExpect(status().isOk());

        switch (event) {
            case EntryEventDto entryEvent -> {
                ArgumentCaptor<EntryEventDto> captor = ArgumentCaptor.forClass(EntryEventDto.class);
                verify(parkingService).processEntry(captor.capture());
                assertEquals(entryEvent, captor.getValue());
            }
            case ParkedEventDto parkedEvent -> {
                ArgumentCaptor<ParkedEventDto> captor = ArgumentCaptor.forClass(ParkedEventDto.class);
                verify(parkingService).processParked(captor.capture());
                assertEquals(parkedEvent, captor.getValue());
            }
            case ExitEventDto exitEvent -> {
                ArgumentCaptor<ExitEventDto> captor = ArgumentCaptor.forClass(ExitEventDto.class);
                verify(parkingService).processExit(captor.capture());
                assertEquals(exitEvent, captor.getValue());
            }
        }
    }
}