package com.bikefinder.auth.application.port.input;


import com.bikefinder.auth.application.command.RefreshTokenCommand;
import com.bikefinder.auth.application.dto.AuthResponseDto;

public interface RefreshTokenUseCase {
    AuthResponseDto execute(RefreshTokenCommand command);
}
