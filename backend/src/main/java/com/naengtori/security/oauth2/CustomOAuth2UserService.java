package com.naengtori.security.oauth2;

import com.naengtori.entity.MemberEntity;
import com.naengtori.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService
        implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final MemberService memberService;

    private final DefaultOAuth2UserService delegate =
            new DefaultOAuth2UserService();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest)
            throws OAuth2AuthenticationException {

        OAuth2User oauth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest
                .getClientRegistration()
                .getRegistrationId();

        OAuth2UserInfo userInfo = switch (registrationId) {
            case OAuth2Constants.KAKAO ->
                    KakaoOAuth2UserInfoMapper.map(oauth2User.getAttributes());

            case OAuth2Constants.NAVER ->
                    NaverOAuth2UserInfoMapper.map(oauth2User.getAttributes());

            default -> throw new OAuth2AuthenticationException(
                    new OAuth2Error("unsupported_provider"),
                    "지원하지 않는 로그인 제공자입니다: " + registrationId
            );
        };

        MemberEntity member = memberService.findOrCreateMember(
                userInfo.provider(),
                userInfo.providerUserId(),
                userInfo.email(),
                userInfo.nickname(),
                userInfo.profileImageUrl()
        );

        Map<String, Object> principalAttributes =
                new HashMap<>(oauth2User.getAttributes());

        principalAttributes.put(OAuth2Constants.MEMBER_ID, member.getId());
        principalAttributes.put(OAuth2Constants.PROVIDER, member.getProvider().name());
        principalAttributes.put(OAuth2Constants.EMAIL, member.getEmail());
        principalAttributes.put(OAuth2Constants.NICKNAME, member.getNickname());
        principalAttributes.put(OAuth2Constants.PROFILE_IMAGE_URL, member.getProfileImageUrl());

        return new DefaultOAuth2User(
                oauth2User.getAuthorities(),
                principalAttributes,
                OAuth2Constants.MEMBER_ID
        );
    }
}