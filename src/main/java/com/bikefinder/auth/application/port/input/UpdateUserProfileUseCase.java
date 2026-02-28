package com.bikefinder.auth.application.port.input;


import com.bikefinder.auth.application.dto.AuthResponseDto;
import com.bikefinder.auth.domain.valueobject.UserId;
import com.bikefinder.auth.infrastructure.adapter.in.rest.dto.UpdateProfileRequestDto;


public interface UpdateUserProfileUseCase {
    AuthResponseDto.UserInfoDto execute(UserId userId, UpdateProfileRequestDto request);
}
