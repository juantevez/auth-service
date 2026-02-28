package com.bikefinder.auth.infrastructure.adapter.in.rest.dto;

import java.time.LocalDate;

public record UpdateProfileRequestDto(
        String fullName,
        String phoneNumber,
        String gender,
        LocalDate birthDate,
        Integer localityId,
        String localityName,
        String departmentName,
        String provinceName,
        String countryName
) {}
