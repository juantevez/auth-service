package com.bikefinder.auth.application.port.input;

import com.bikefinder.auth.application.command.ResendVerificationCommand;

public interface ResendVerificationUseCase {
    void execute(ResendVerificationCommand command);
}
