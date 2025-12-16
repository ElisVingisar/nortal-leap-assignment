package com.nortal.library.core.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "members")
@Getter
@Setter
@NoArgsConstructor
public class Member {

  @Id private String id;

  @Column(nullable = false)
  private String name;

  public Member(String id, String name) {
    this.id = id;
    this.name = name;
  }
}
