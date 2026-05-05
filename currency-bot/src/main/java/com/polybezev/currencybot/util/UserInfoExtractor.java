package com.polybezev.currencybot.util;

import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;

public class UserInfoExtractor {

    public static String getFirstName(Update update) {
        try {
            if (update == null || !update.hasMessage()) return null;

            Chat chat = update.getMessage().getChat();
            if (chat == null) return null;

            String firstName = chat.getFirstName();
            return (firstName != null && !firstName.trim().isEmpty())
                    ? firstName.trim()
                    : null;
        } catch (Exception e) {
            return null;
        }
    }

    public static String getUserName(Update update) {
         try {
             if (update == null || !update.hasMessage()) return null;

             Chat chat = update.getMessage().getChat();
             if (chat == null) return null;

             String userName = chat.getUserName();
             return (userName != null && !userName.trim().isEmpty())
                     ? userName.trim()
                     : null;
         } catch (Exception e) {
             return null;
         }
    }

    public static String getBestName(Update update) {
        String firstName = getFirstName(update);
        if (firstName != null) {
            return firstName;
        }

        String userName = getUserName(update);
        if (userName != null) {
            return "@" + userName;
        }

        return "Аноним";
    }
}
