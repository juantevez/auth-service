package com.bikefinder.auth.application.port.input;

import com.bikefinder.auth.application.command.SocialLoginCommand;
import com.bikefinder.auth.application.dto.AuthResponseDto;

public interface SocialLoginUseCase {
    AuthResponseDto execute(SocialLoginCommand command);
}
