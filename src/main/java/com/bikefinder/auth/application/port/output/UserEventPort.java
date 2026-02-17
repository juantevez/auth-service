package com.bikefinder.auth.application.port.output;


import com.bikefinder.auth.domain.valueobject.UserId;

public interface UserEventPort {
    void publishUserRegistered(UserId userId, String email);
    void publishUserLoggedIn(UserId userId);
    void publishPasswordChanged(UserId userId);
}
