package com.polybezev.currencybot.model;

import lombok.Data;

@Data
public class UserConversationData {

    private ConversationState state;
    private UserMode mode;

    // Converter FSM
    private Double amount;
    private String fromCurrency;

    // Edit-in-place: messageId последнего бот-сообщения в FSM-диалоге
    private Integer lastBotMessageId;

    // Admin grant FSM
    private String adminGrantTargetId;

    public UserConversationData() {
        this.state = ConversationState.IDLE;
        this.mode = UserMode.MAIN;
    }
}
