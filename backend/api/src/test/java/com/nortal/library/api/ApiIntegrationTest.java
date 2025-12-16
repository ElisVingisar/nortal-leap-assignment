package com.nortal.library.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.nortal.library.api.dto.BookResponse;
import com.nortal.library.api.dto.BooksResponse;
import com.nortal.library.api.dto.BorrowRequest;
import com.nortal.library.api.dto.CancelReservationRequest;
import com.nortal.library.api.dto.CreateBookRequest;
import com.nortal.library.api.dto.CreateMemberRequest;
import com.nortal.library.api.dto.DeleteBookRequest;
import com.nortal.library.api.dto.DeleteMemberRequest;
import com.nortal.library.api.dto.LoanExtensionRequest;
import com.nortal.library.api.dto.MemberResponse;
import com.nortal.library.api.dto.MemberSummaryResponse;
import com.nortal.library.api.dto.MembersResponse;
import com.nortal.library.api.dto.ReserveRequest;
import com.nortal.library.api.dto.ResultResponse;
import com.nortal.library.api.dto.ResultWithNextResponse;
import com.nortal.library.api.dto.ReturnRequest;
import com.nortal.library.api.dto.UpdateBookRequest;
import com.nortal.library.api.dto.UpdateMemberRequest;
import java.time.LocalDate;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ApiIntegrationTest {

  @LocalServerPort int port;

  private final TestRestTemplate rest = new TestRestTemplate();

  @Test
  void listsSeedBooksAndMembers() {
    BooksResponse books = rest.getForObject(url("/api/books"), BooksResponse.class);
    MembersResponse members = rest.getForObject(url("/api/members"), MembersResponse.class);

    assertThat(books).isNotNull();
    assertThat(books.items()).hasSizeGreaterThanOrEqualTo(6);
    assertThat(books.items()).allSatisfy(b -> assertThat(b.id()).isNotBlank());

    assertThat(members).isNotNull();
    assertThat(members.items()).hasSizeGreaterThanOrEqualTo(4);
    assertThat(members.items()).allSatisfy(m -> assertThat(m.id()).isNotBlank());
  }

  @Test
  void bookCrudRoundtrip() {
    ResultResponse created =
        rest.postForObject(
            url("/api/books"), new CreateBookRequest("vb1", "Visible Book"), ResultResponse.class);
    assertThat(created.ok()).isTrue();

    ResultResponse updated =
        rest.exchange(
                url("/api/books"),
                HttpMethod.PUT,
                new org.springframework.http.HttpEntity<>(new UpdateBookRequest("vb1", "Renamed")),
                ResultResponse.class)
            .getBody();
    assertThat(updated.ok()).isTrue();

    BookResponse book =
        rest.getForObject(url("/api/books"), BooksResponse.class).items().stream()
            .filter(b -> b.id().equals("vb1"))
            .findFirst()
            .orElse(null);
    assertThat(book).isNotNull();
    assertThat(book.title()).isEqualTo("Renamed");

    ResultResponse deleted =
        rest.exchange(
                url("/api/books"),
                HttpMethod.DELETE,
                new org.springframework.http.HttpEntity<>(new DeleteBookRequest("vb1")),
                ResultResponse.class)
            .getBody();
    assertThat(deleted.ok()).isTrue();

    BooksResponse afterDelete = rest.getForObject(url("/api/books"), BooksResponse.class);
    assertThat(afterDelete.items().stream().noneMatch(b -> Objects.equals(b.id(), "vb1"))).isTrue();
  }

  @Test
  void memberCrudRoundtrip() {
    ResultResponse created =
        rest.postForObject(
            url("/api/members"),
            new CreateMemberRequest("vm1", "Visible Member"),
            ResultResponse.class);
    assertThat(created.ok()).isTrue();

    ResultResponse updated =
        rest.exchange(
                url("/api/members"),
                HttpMethod.PUT,
                new org.springframework.http.HttpEntity<>(
                    new UpdateMemberRequest("vm1", "Renamed")),
                ResultResponse.class)
            .getBody();
    assertThat(updated.ok()).isTrue();

    MemberResponse member =
        rest.getForObject(url("/api/members"), MembersResponse.class).items().stream()
            .filter(m -> m.id().equals("vm1"))
            .findFirst()
            .orElse(null);
    assertThat(member).isNotNull();
    assertThat(member.name()).isEqualTo("Renamed");

    ResultResponse deleted =
        rest.exchange(
                url("/api/members"),
                HttpMethod.DELETE,
                new org.springframework.http.HttpEntity<>(new DeleteMemberRequest("vm1")),
                ResultResponse.class)
            .getBody();
    assertThat(deleted.ok()).isTrue();

    MembersResponse afterDelete = rest.getForObject(url("/api/members"), MembersResponse.class);
    assertThat(afterDelete.items().stream().noneMatch(m -> Objects.equals(m.id(), "vm1"))).isTrue();
  }

  @Test
  void borrowAndReturnHappyPath() {
    ResultResponse borrow =
        rest.postForObject(url("/api/borrow"), new BorrowRequest("b1", "m1"), ResultResponse.class);
    assertThat(borrow.ok()).isTrue();

    ResultWithNextResponse returned =
        rest.postForObject(
            url("/api/return"), new ReturnRequest("b1"), ResultWithNextResponse.class);
    assertThat(returned.ok()).isTrue();
    assertThat(returned.nextMemberId()).isNull();

    BookResponse book =
        rest.getForObject(url("/api/books"), BooksResponse.class).items().stream()
            .filter(b -> b.id().equals("b1"))
            .findFirst()
            .orElseThrow();
    assertThat(book.loanedTo()).isNull();
  }

  @Test
  void reserveAndCancelReservation() {
    rest.postForObject(url("/api/borrow"), new BorrowRequest("b2", "m1"), ResultResponse.class);
    ResultResponse reserved =
        rest.postForObject(
            url("/api/reserve"), new ReserveRequest("b2", "m2"), ResultResponse.class);
    assertThat(reserved.ok()).isTrue();

    ResultResponse canceled =
        rest.postForObject(
            url("/api/cancel-reservation"),
            new CancelReservationRequest("b2", "m2"),
            ResultResponse.class);
    assertThat(canceled.ok()).isTrue();
  }

  @Test
  void extendLoanUpdatesDueDate() {
    ResultResponse borrow =
        rest.postForObject(url("/api/borrow"), new BorrowRequest("b3", "m1"), ResultResponse.class);
    assertThat(borrow.ok()).isTrue();

    BooksResponse afterBorrow = rest.getForObject(url("/api/books"), BooksResponse.class);
    LocalDate dueDate =
        afterBorrow.items().stream()
            .filter(b -> b.id().equals("b3"))
            .map(BookResponse::dueDate)
            .findFirst()
            .orElse(null);
    assertThat(dueDate).isNotNull();

    ResultResponse extended =
        rest.postForObject(
            url("/api/extend"), new LoanExtensionRequest("b3", 3), ResultResponse.class);
    assertThat(extended.ok()).isTrue();

    BooksResponse afterExtend = rest.getForObject(url("/api/books"), BooksResponse.class);
    LocalDate extendedDate =
        afterExtend.items().stream()
            .filter(b -> b.id().equals("b3"))
            .map(BookResponse::dueDate)
            .findFirst()
            .orElse(null);
    assertThat(extendedDate).isAfter(dueDate);
  }

  @Test
  void searchReturnsFilteredResults() {
    rest.postForObject(
        url("/api/books"),
        new CreateBookRequest("vb-search", "Algorithms 101"),
        ResultResponse.class);

    BooksResponse all =
        rest.getForObject(url("/api/books/search?titleContains=Algo"), BooksResponse.class);
    assertThat(all.items().stream().anyMatch(b -> b.id().equals("vb-search"))).isTrue();

    rest.postForObject(
        url("/api/borrow"), new BorrowRequest("vb-search", "m1"), ResultResponse.class);
    BooksResponse availableOnly =
        rest.getForObject(url("/api/books/search?available=true"), BooksResponse.class);
    assertThat(availableOnly.items().stream().noneMatch(b -> b.id().equals("vb-search"))).isTrue();
  }

  @Test
  void memberSummaryShowsLoansAndReservations() {
    rest.postForObject(url("/api/borrow"), new BorrowRequest("b4", "m2"), ResultResponse.class);
    rest.postForObject(url("/api/borrow"), new BorrowRequest("b5", "m1"), ResultResponse.class);
    rest.postForObject(url("/api/reserve"), new ReserveRequest("b5", "m2"), ResultResponse.class);

    MemberSummaryResponse summary =
        rest.getForObject(url("/api/members/m2/summary"), MemberSummaryResponse.class);

    assertThat(summary.ok()).isTrue();
    assertThat(summary.loans().stream().anyMatch(l -> l.bookId().equals("b4"))).isTrue();
    assertThat(
            summary.reservations().stream()
                .anyMatch(r -> r.bookId().equals("b5") && r.position() == 0))
        .isTrue();
  }

  @Test
  void overdueEndpointListsPastDue() {
    rest.postForObject(url("/api/borrow"), new BorrowRequest("b6", "m1"), ResultResponse.class);
    rest.postForObject(
        url("/api/extend"), new LoanExtensionRequest("b6", -30), ResultResponse.class);

    BooksResponse overdue = rest.getForObject(url("/api/overdue"), BooksResponse.class);
    assertThat(overdue.items().stream().anyMatch(b -> b.id().equals("b6"))).isTrue();
  }

  @Test
  void healthEndpointRespondsOk() {
    ResponseEntity<String> response = rest.getForEntity(url("/api/health"), String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).contains("ok");
  }

  private String url(String path) {
    return "http://localhost:" + port + path;
  }
}
