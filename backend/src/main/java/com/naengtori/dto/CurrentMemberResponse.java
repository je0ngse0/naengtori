package com.naengtori.dto;

public record CurrentMemberResponse(
        boolean authenticated,
        Long memberId,
        String provider,
        String email,
        String nickname,
        String profileImageUrl
) {
}