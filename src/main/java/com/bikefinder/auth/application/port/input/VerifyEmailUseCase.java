package com.bikefinder.auth.application.port.input;

import com.bikefinder.auth.application.command.VerifyEmailCommand;

public interface VerifyEmailUseCase {
    void execute(VerifyEmailCommand command);
}
