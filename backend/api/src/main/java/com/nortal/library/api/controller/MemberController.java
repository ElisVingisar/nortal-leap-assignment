package com.nortal.library.api.controller;

import com.nortal.library.api.dto.CreateMemberRequest;
import com.nortal.library.api.dto.DeleteMemberRequest;
import com.nortal.library.api.dto.MemberResponse;
import com.nortal.library.api.dto.MemberSummaryResponse;
import com.nortal.library.api.dto.MembersResponse;
import com.nortal.library.api.dto.ResultResponse;
import com.nortal.library.api.dto.UpdateMemberRequest;
import com.nortal.library.core.LibraryService;
import com.nortal.library.core.domain.Member;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
public class MemberController {

  private final LibraryService libraryService;

  public MemberController(LibraryService libraryService) {
    this.libraryService = libraryService;
  }

  @GetMapping
  public MembersResponse list() {
    return new MembersResponse(libraryService.allMembers().stream().map(this::toResponse).toList());
  }

  @GetMapping("/{memberId}/summary")
  public MemberSummaryResponse summary(@PathVariable("memberId") String memberId) {
    LibraryService.MemberSummary summary = libraryService.memberSummary(memberId);
    if (!summary.ok()) {
      return MemberSummaryResponse.failure(summary.reason());
    }

    var loans =
        summary.loans().stream()
            .map(
                book ->
                    new MemberSummaryResponse.BookLoanSummary(
                        book.getId(), book.getTitle(), book.getDueDate()))
            .toList();
    var reservations =
        summary.reservations().stream()
            .map(
                reservation -> {
                  String title =
                      libraryService
                          .findBook(reservation.bookId())
                          .map(com.nortal.library.core.domain.Book::getTitle)
                          .orElse(null);
                  return new MemberSummaryResponse.ReservationSummary(
                      reservation.bookId(), title, reservation.position());
                })
            .toList();
    return MemberSummaryResponse.success(loans, reservations);
  }

  @PostMapping
  public ResultResponse create(@RequestBody @Valid CreateMemberRequest request) {
    LibraryService.Result result = libraryService.createMember(request.id(), request.name());
    return new ResultResponse(result.ok(), result.reason());
  }

  @PutMapping
  public ResultResponse update(@RequestBody @Valid UpdateMemberRequest request) {
    LibraryService.Result result = libraryService.updateMember(request.id(), request.name());
    return new ResultResponse(result.ok(), result.reason());
  }

  @DeleteMapping
  public ResultResponse delete(@RequestBody @Valid DeleteMemberRequest request) {
    LibraryService.Result result = libraryService.deleteMember(request.id());
    return new ResultResponse(result.ok(), result.reason());
  }

  private MemberResponse toResponse(Member member) {
    return new MemberResponse(member.getId(), member.getName());
  }
}
