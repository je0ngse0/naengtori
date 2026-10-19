package com.naengtori.security.oauth2;

import com.naengtori.entity.SocialProvider;

import java.util.Map;

public final class KakaoOAuth2UserInfoMapper {

    private KakaoOAuth2UserInfoMapper() {
    }

    public static OAuth2UserInfo map(Map<String, Object> attributes) {
        String providerUserId = toString(attributes.get(OAuth2Constants.ID));

        if (providerUserId == null) {
            throw new IllegalArgumentException("카카오 사용자 ID가 없습니다.");
        }

        Map<String, Object> account =
                getNestedMap(attributes, OAuth2Constants.Kakao.ACCOUNT);

        Map<String, Object> profile =
                getNestedMap(account, OAuth2Constants.Kakao.PROFILE);

        return new OAuth2UserInfo(
                SocialProvider.KAKAO,
                providerUserId,
                toString(account.get(OAuth2Constants.EMAIL)),
                toString(profile.get(OAuth2Constants.NICKNAME)),
                toString(profile.get(OAuth2Constants.Kakao.PROFILE_IMAGE))
        );
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> getNestedMap(
            Map<String, Object> source,
            String key
    ) {
        Object value = source.get(key);

        if (value instanceof Map<?, ?>) {
            return (Map<String, Object>) value;
        }

        return Map.of();
    }

    private static String toString(Object value) {
        return value == null ? null : value.toString();
    }
}