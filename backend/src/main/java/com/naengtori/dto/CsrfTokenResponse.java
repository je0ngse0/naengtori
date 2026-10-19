package com.naengtori.dto;

public record CsrfTokenResponse(
        String headerName,
        String token
) {
}