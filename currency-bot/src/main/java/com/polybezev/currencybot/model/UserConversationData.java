package com.polybezev.currencybot.model;

import lombok.Data;

@Data
public class UserConversationData {

    private ConversationState state;
    private Double amount;
    private String fromCurrency;

    public UserConversationData() {
        this.state = ConversationState.IDLE;
    }
}
