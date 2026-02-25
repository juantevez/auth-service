package com.bikefinder.auth.application.port.input;

import com.bikefinder.auth.application.dto.AuthResponseDto;
import com.bikefinder.auth.domain.valueobject.UserId;

public interface GetUserProfileUseCase {
    AuthResponseDto.UserInfoDto execute(UserId userId);
}
