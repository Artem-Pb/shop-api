package com.polybezev.currencybot.model;

public enum ConversationState {
    IDLE,
    AWAIT_AMOUNT,
    AWAIT_FROM,
    AWAIT_TO,
    ADMIN_AWAIT_GRANT_ID,
    ADMIN_AWAIT_GRANT_TIER
}
