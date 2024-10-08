package io.ybg.demo.repository;

import io.ybg.demo.entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepo extends JpaRepository<MemberEntity, Long> {
    boolean existsByEmail(String email);
}