package com.naengtori.repository;

import com.naengtori.entity.MemberEntity;
import com.naengtori.entity.SocialProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<MemberEntity, Long> {

    Optional<MemberEntity> findByProviderAndProviderUserId(
            SocialProvider provider,
            String providerUserId
    );
}