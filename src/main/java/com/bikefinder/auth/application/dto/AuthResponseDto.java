package com.bikefinder.auth.application.dto;

import com.bikefinder.auth.domain.valueobject.PhoneNumber;

import java.time.Instant;
import java.time.LocalDate;

public record AuthResponseDto(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long expiresIn,
        Instant expiresAt,
        UserInfoDto user
) {
    public record UserInfoDto(
            String id,
            String email,
            String fullName,
            String phoneNumber,
            Boolean phoneVerified,
            String avatarUrl,
            String gender,
            LocalDate birthDate,
            LocationDto location
    ) {
        public record LocationDto(
                Integer localityId,
                String localityName,
                String departmentName,
                String provinceName,
                String countryName
        ) {}
    }
}
