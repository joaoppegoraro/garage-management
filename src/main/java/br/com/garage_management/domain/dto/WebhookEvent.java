package br.com.garage_management.domain.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "event_type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = EntryEventDto.class, name = "ENTRY"),
        @JsonSubTypes.Type(value = ParkedEventDto.class, name = "PARKED"),
        @JsonSubTypes.Type(value = ExitEventDto.class, name = "EXIT")
})
public sealed interface WebhookEvent permits EntryEventDto, ParkedEventDto, ExitEventDto {
}