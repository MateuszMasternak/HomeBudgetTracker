package com.rainy.homebudgettracker.user;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@EqualsAndHashCode
public class UserInfoResponse {
    private boolean isPremiumUser;
}
