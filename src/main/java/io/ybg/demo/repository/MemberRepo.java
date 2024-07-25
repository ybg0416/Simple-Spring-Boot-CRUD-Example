package io.ybg.demo.repository;

import io.ybg.demo.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepo extends JpaRepository<Member, Integer> {
    boolean existsByEmail(String email);
}