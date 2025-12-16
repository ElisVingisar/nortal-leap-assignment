package com.nortal.library.api.dto;

import java.time.LocalDate;
import java.util.List;

public record MemberSummaryResponse(
    boolean ok, String reason, List<BookLoanSummary> loans, List<ReservationSummary> reservations) {

  public static MemberSummaryResponse failure(String reason) {
    return new MemberSummaryResponse(false, reason, List.of(), List.of());
  }

  public static MemberSummaryResponse success(
      List<BookLoanSummary> loans, List<ReservationSummary> reservations) {
    return new MemberSummaryResponse(true, null, loans, reservations);
  }

  public record BookLoanSummary(String bookId, String title, LocalDate dueDate) {}

  public record ReservationSummary(String bookId, String title, int position) {}
}
