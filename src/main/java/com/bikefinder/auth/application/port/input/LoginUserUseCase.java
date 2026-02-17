package com.bikefinder.auth.application.port.input;

import com.bikefinder.auth.application.command.LoginUserCommand;
import com.bikefinder.auth.application.dto.AuthResponseDto;

public interface LoginUserUseCase {
    AuthResponseDto execute(LoginUserCommand command);
}
