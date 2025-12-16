package com.nortal.library.core.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
public class Book {

  @Id private String id;

  @Column(nullable = false)
  private String title;

  @Column(name = "loaned_to")
  private String loanedTo;

  @Column(name = "due_date")
  private LocalDate dueDate;

  @ElementCollection
  @CollectionTable(name = "book_reservations", joinColumns = @JoinColumn(name = "book_id"))
  @OrderColumn(name = "position")
  @Column(name = "member_id")
  private List<String> reservationQueue = new ArrayList<>();

  public Book(String id, String title) {
    this.id = id;
    this.title = title;
  }
}
