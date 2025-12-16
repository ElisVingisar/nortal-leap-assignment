package com.nortal.library.persistence.jpa;

import com.nortal.library.core.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaMemberRepository extends JpaRepository<Member, String> {}
