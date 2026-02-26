package com.bikefinder.auth.application.port.input;

import com.bikefinder.auth.application.command.RequestPasswordResetCommand;

public interface RequestPasswordResetUseCase {
    void execute(RequestPasswordResetCommand command);
}
