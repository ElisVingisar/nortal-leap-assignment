package com.nortal.library.api.dto;

import java.time.LocalDate;
import java.util.List;

public record BookResponse(
    String id, String title, String loanedTo, LocalDate dueDate, List<String> reservationQueue) {}
