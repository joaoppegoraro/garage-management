package br.com.garage_management.controller;

import br.com.garage_management.domain.dto.EntryEventDto;
import br.com.garage_management.domain.dto.ExitEventDto;
import br.com.garage_management.domain.dto.ParkedEventDto;
import br.com.garage_management.domain.dto.WebhookEvent;
import br.com.garage_management.service.ParkingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
public class WebhookController {

    private final ParkingService parkingService;

    @PostMapping
    public ResponseEntity<Void> handleWebhookEvent(@RequestBody WebhookEvent event) {
        log.info("Evento Webhook recebido com o tipo: {}", event.getClass().getSimpleName());

        switch (event) {
            case EntryEventDto entryEvent -> parkingService.processEntry(entryEvent);
            case ParkedEventDto parkedEvent -> parkingService.processParked(parkedEvent);
            case ExitEventDto exitEvent -> parkingService.processExit(exitEvent);
        }

        return ResponseEntity.ok().build();
    }
}