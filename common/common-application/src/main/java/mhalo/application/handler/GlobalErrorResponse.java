package mhalo.application.handler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class GlobalErrorResponse {
    private final String code;
    private final String message;
}
