package com.bikefinder.auth.application.port.input;

import com.bikefinder.auth.application.command.ConfirmPasswordResetCommand;

public interface ConfirmPasswordResetUseCase {
    void execute(ConfirmPasswordResetCommand command);
}
