package com.naengtori.service;

import com.naengtori.entity.MemberEntity;
import com.naengtori.entity.SocialProvider;
import com.naengtori.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public MemberEntity findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "회원을 찾을 수 없습니다: " + memberId
                        )
                );
    }

    @Transactional
    public MemberEntity findOrCreateMember(
            SocialProvider provider,
            String providerUserId,
            String email,
            String nickname,
            String profileImageUrl
    ) {
        return memberRepository
                .findByProviderAndProviderUserId(provider, providerUserId)
                .map(member -> {
                    member.recordLogin();
                    return member;
                })
                .orElseGet(() -> memberRepository.save(
                        new MemberEntity(
                                provider,
                                providerUserId,
                                email,
                                nickname,
                                profileImageUrl
                        )
                ));
    }
}