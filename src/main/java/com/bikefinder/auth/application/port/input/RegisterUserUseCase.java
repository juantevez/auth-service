package com.bikefinder.auth.application.port.input;


import com.bikefinder.auth.application.command.RegisterUserCommand;
import com.bikefinder.auth.application.dto.AuthResponseDto;

public interface RegisterUserUseCase {
    AuthResponseDto execute(RegisterUserCommand command);
}
