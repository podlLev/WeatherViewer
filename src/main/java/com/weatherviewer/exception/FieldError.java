package com.weatherviewer.exception;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * A single field-level validation error, returned to REST API clients
 * as part of a 422 Unprocessable Entity response body.
 */
@Schema(description = "A single field validation error")
public record FieldError(

        @Schema(description = "Name of the invalid field", example = "name") String field,

        @Schema(description = "Human-readable validation message", example = "Name cannot be blank") String message

) {

}
