package com.nortal.library.api.dto;

import jakarta.validation.constraints.NotBlank;

public record ReturnRequest(@NotBlank String bookId) {}
