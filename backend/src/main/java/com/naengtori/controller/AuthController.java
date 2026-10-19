package com.naengtori.controller;

import com.naengtori.dto.CurrentMemberResponse;
import com.naengtori.entity.MemberEntity;
import com.naengtori.security.oauth2.OAuth2Constants;
import com.naengtori.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.naengtori.dto.CsrfTokenResponse;
import org.springframework.security.web.csrf.CsrfToken;
import com.naengtori.dto.UpdateNicknameRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final MemberService memberService;

    @GetMapping("/me")
    public CurrentMemberResponse getCurrentMember(
            @AuthenticationPrincipal OAuth2User principal
    ) {
        Long memberId = principal.getAttribute(
                OAuth2Constants.MEMBER_ID
        );

        MemberEntity member = memberService.findMemberById(memberId);

        return toResponse(member);
    }

    @GetMapping("/csrf")
    public CsrfTokenResponse getCsrfToken(CsrfToken csrfToken) {
        return new CsrfTokenResponse(
                csrfToken.getHeaderName(),
                csrfToken.getToken()
        );
    }

    @PatchMapping("/me")
    public CurrentMemberResponse updateNickname(
            @AuthenticationPrincipal OAuth2User principal,
            @Valid @RequestBody UpdateNicknameRequest request
    ) {
        Long memberId = principal.getAttribute(
                OAuth2Constants.MEMBER_ID
        );

        MemberEntity member = memberService.updateNickname(
                memberId,
                request.nickname()
        );

        return toResponse(member);
    }

    private CurrentMemberResponse toResponse(MemberEntity member) {
        return new CurrentMemberResponse(
                true,
                member.getId(),
                member.getProvider().name(),
                member.getEmail(),
                member.getNickname(),
                member.getProfileImageUrl()
        );
    }
}