package com.rainy.homebudgettracker.auth;

import lombok.*;

@Getter
@Setter
@Builder
public class AuthenticationResponse {
    private String token;
}
