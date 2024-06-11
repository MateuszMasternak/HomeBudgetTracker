package com.rainy.homebudgettracker.email;

import lombok.Getter;

@Getter
public enum EmailTemplateName {
    ACTIVATE_ACCOUNT("activate_account_message")
    ;

    private final String name;

    EmailTemplateName(String name) {
        this.name = name;
    }
}
