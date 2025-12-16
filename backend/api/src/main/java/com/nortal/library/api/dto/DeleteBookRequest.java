package com.nortal.library.api.dto;

import jakarta.validation.constraints.NotBlank;

public record DeleteBookRequest(@NotBlank String id) {}
