package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CodigoRequest(@NotBlank @Pattern(regexp = "\\d{6}", message = "O código deve ter 6 dígitos") String codigo) {
}
