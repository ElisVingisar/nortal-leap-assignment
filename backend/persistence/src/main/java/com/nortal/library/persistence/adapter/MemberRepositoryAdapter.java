package com.nortal.library.persistence.adapter;

import com.nortal.library.core.domain.Member;
import com.nortal.library.core.port.MemberRepository;
import com.nortal.library.persistence.jpa.JpaMemberRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class MemberRepositoryAdapter implements MemberRepository {

  private final JpaMemberRepository jpaRepository;

  public MemberRepositoryAdapter(JpaMemberRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public Optional<Member> findById(String id) {
    return jpaRepository.findById(id);
  }

  @Override
  public List<Member> findAll() {
    return jpaRepository.findAll();
  }

  @Override
  public Member save(Member member) {
    return jpaRepository.save(member);
  }

  @Override
  public void delete(Member member) {
    jpaRepository.delete(member);
  }

  @Override
  public boolean existsById(String id) {
    return jpaRepository.existsById(id);
  }
}
