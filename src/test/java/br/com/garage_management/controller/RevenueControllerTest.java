package br.com.garage_management.controller;

import br.com.garage_management.domain.dto.RevenueResponseDto;
import br.com.garage_management.service.RevenueService;
import br.com.garage_management.util.TestFactoryUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RevenueController.class)
class RevenueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RevenueService revenueService;

    private static final String REVENUE_CONTROLLER_ENDPOINT = "/revenue";

    @TestConfiguration
    static class TestConfig {
        @Bean
        public RevenueService revenueService() {
            return mock(RevenueService.class);
        }
    }

    @Test
    @DisplayName("Deve retornar 200 OK com os dados de receita quando a requisição for válida")
    void shouldReturn200OkWhenRequestIsValid() throws Exception {
        LocalDate testDate = LocalDate.of(2025, 10, 15);
        String testSector = "A";
        RevenueResponseDto expectedDto = TestFactoryUtil.createMockRevenueResponseDto();

        when(revenueService.calculateDailyRevenue(testDate, testSector)).thenReturn(expectedDto);

        mockMvc.perform(get(REVENUE_CONTROLLER_ENDPOINT)
                        .param("date", "2025-10-15")
                        .param("sector", "A"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedDto)));
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request quando o parâmetro 'date' estiver faltando")
    void shouldReturnBadRequestWhenDateIsMissing() throws Exception {
        mockMvc.perform(get(REVENUE_CONTROLLER_ENDPOINT)
                        .param("sector", "A"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request quando o parâmetro 'sector' estiver faltando")
    void shouldReturn400BadRequestWhenSectorIsMissing() throws Exception {
        mockMvc.perform(get(REVENUE_CONTROLLER_ENDPOINT)
                        .param("date", "2025-10-15"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request quando o formato da data for inválido")
    void shouldReturn400BadRequestWhenDateFormatIsInvalid() throws Exception {
        mockMvc.perform(get(REVENUE_CONTROLLER_ENDPOINT)
                        .param("date", "15-10-2025")
                        .param("sector", "A"))
                .andExpect(status().isBadRequest());
    }
}