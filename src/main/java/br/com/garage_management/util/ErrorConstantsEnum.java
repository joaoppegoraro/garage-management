package br.com.garage_management.util;

import br.com.garage_management.exception.BusinessException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum ErrorConstantsEnum {

    GARAGE_WITHOUT_SPACES("Garagem cheia.", "Entrada recusada para a placa: ", BAD_REQUEST),
    DUPLICATE_LICENSE_PLATE("Veículo com a placa duplicada.", "Tentativa de entrada duplicada para a placa: ", BAD_REQUEST),
    LICENSE_PLATE_NOT_FOUND("Veículo com a placa não encontrado.", "Não foi encontrado veículo estacionado para a placa: ", NOT_FOUND),
    PARKING_SPACE_NOT_FOUND("Vaga não encontrada.", "Não foi encontrado vaga com as coordenadas especificadas", NOT_FOUND),
    PARKING_SPACE_ALREADY_OCCUPIED("Vaga já ocupada.", "A vaga com coordenadas inseridas, já está ocupada", BAD_REQUEST),
    GARAGE_SECTOR_NOT_FOUND("Setor não encontrado.", "Não foi encontrado setor para o registro: ", NOT_FOUND),
    INCONSISTENT_DATA_SPOTS_NOT_FOUND("Inconsistência de dados.", "Setor não está cheio, mas nenhuma vaga livre foi encontrada.", BAD_REQUEST),
    INVALID_ENTRY_TIME("Tempo de entrada inválido", "O tempo de entrada é inválido ou está no passado: ", BAD_REQUEST),
    INVALID_EXIT_TIME("Tempo de saída inválido", "O tempo de saída não pode ser anterior ao de entrada", BAD_REQUEST);

    private String message;
    private String description;
    private HttpStatus httpStatus;

    ErrorConstantsEnum(String message, String description, HttpStatus httpStatus) {
        this.message = message;
        this.description = description;
        this.httpStatus = httpStatus;
    }

    public BusinessException asException(String... args) {
        String descriptionArgs = String.join(" ", args);

        return BusinessException.builder()
                .httpStatusCode(this.getHttpStatus())
                .code(this.getHttpStatus().getReasonPhrase())
                .message(this.getMessage())
                .description(this.getDescription() + descriptionArgs)
                .build();
    }

}
