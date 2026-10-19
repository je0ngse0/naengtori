package com.naengtori.security.oauth2;

import com.naengtori.entity.SocialProvider;

public record OAuth2UserInfo(
        SocialProvider provider,
        String providerUserId,
        String email,
        String nickname,
        String profileImageUrl
) {
}