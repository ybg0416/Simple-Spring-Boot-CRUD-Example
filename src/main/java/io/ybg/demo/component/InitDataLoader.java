package io.ybg.demo.component;

import io.ybg.demo.entity.MemberEntity;
import io.ybg.demo.repository.MemberRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class InitDataLoader implements CommandLineRunner {
    private final MemberRepo memberRepo;

    public InitDataLoader(final MemberRepo memberRepo) {
        this.memberRepo = memberRepo;
    }

    @Override
    public void run(final String... args) {
        memberRepo.save(MemberEntity.builder().email("admin@mail.com").name("운영자").phone("010-1234-5678").build());
        log.info("Admin Member created");
    }
}
