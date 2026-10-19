package com.naengtori.security.oauth2;

import com.naengtori.entity.SocialProvider;

import java.util.Map;

public final class NaverOAuth2UserInfoMapper {

    private NaverOAuth2UserInfoMapper() {
    }

    public static OAuth2UserInfo map(Map<String, Object> attributes) {
        Map<String, Object> response =
                getNestedMap(attributes, OAuth2Constants.Naver.RESPONSE);

        String providerUserId = toString(response.get(OAuth2Constants.ID));

        if (providerUserId == null) {
            throw new IllegalArgumentException("네이버 사용자 ID가 없습니다.");
        }

        return new OAuth2UserInfo(
                SocialProvider.NAVER,
                providerUserId,
                toString(response.get(OAuth2Constants.EMAIL)),
                toString(response.get(OAuth2Constants.NICKNAME)),
                toString(response.get(OAuth2Constants.Naver.PROFILE_IMAGE))
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