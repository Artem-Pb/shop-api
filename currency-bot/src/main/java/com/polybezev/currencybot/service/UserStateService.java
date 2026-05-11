package com.polybezev.currencybot.service;

import com.polybezev.currencybot.model.UserConversationData;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserStateService {
    private final Map<Long, UserConversationData> session = new ConcurrentHashMap<>();

    public UserConversationData getOrCreate(long chatId) {
        return session.computeIfAbsent(chatId, id -> new UserConversationData());
    }

    public UserConversationData reset(long chatId) {
        UserConversationData fresh = new UserConversationData();
        session.put(chatId, fresh);
        return fresh;
    }
}
