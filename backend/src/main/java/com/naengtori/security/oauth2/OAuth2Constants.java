package com.naengtori.security.oauth2;

public final class OAuth2Constants {
    public static final String KAKAO = "kakao";
    public static final String NAVER = "naver";

    public static final String MEMBER_ID = "memberId";
    public static final String PROVIDER = "provider";
    public static final String NICKNAME = "nickname";
    public static final String PROFILE_IMAGE_URL = "profileImageUrl";

    public static final String ID = "id";
    public static final String EMAIL = "email";

    public static final class Kakao {

        public static final String ACCOUNT = "kakao_account";
        public static final String PROFILE = "profile";
        public static final String PROFILE_IMAGE = "profile_image_url";

        private Kakao() {
        }
    }

    public static final class Naver {

        public static final String RESPONSE = "response";
        public static final String PROFILE_IMAGE = "profile_image";

        private Naver() {
        }
    }

    private OAuth2Constants() {
    }
}