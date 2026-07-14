package com.weatherviewer.exception;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A single field validation error")
public record FieldError(

        @Schema(description = "Name of the invalid field", example = "name") String field,

        @Schema(description = "Human-readable validation message", example = "Name cannot be blank") String message

) {

}
