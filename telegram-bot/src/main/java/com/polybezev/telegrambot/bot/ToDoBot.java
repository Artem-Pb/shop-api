package com.polybezev.telegrambot.bot;

import com.polybezev.telegrambot.model.Task;
import com.polybezev.telegrambot.service.TaskService;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Component
public class ToDoBot extends TelegramLongPollingBot {
    private final TaskService taskService;
    private String token;
    private String username;

    public ToDoBot(@Value("${telegram.bot.token}")String token,
                   @Value("${telegram.bot.username}") String username,
                   TaskService taskService) {
        super(token);
        this.token = token;
        this.username = username;
        this.taskService = taskService;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Long chatId = update.getMessage().getChatId();
            String text = update.getMessage().getText();

            if (text.equals("/start")) {
                sendMessage(chatId, "Привет. Тут ты можешь планировать свой день! \n" +
                        " /add [text] - создать задачу. \n" +
                        "/list - список \n" +
                        "/done <id> - выполнено \n" +
                        "/delete <id> - удалить");
            } else if (text.startsWith("/add ")) {
                taskService.addTask(chatId, text.substring(5));
                sendMessage(chatId, "Задача " + text.substring(5) + " - успешно создана!");
            } else if (text.equals("/list")) {
                sendMessage(chatId, "Сейчас, загрузим все задачи: \n");
                List<Task> tasks = taskService.getTasks(chatId);
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < tasks.size(); i++) {
                    stringBuilder.append(i).append(") ").append(tasks.get(i).getDescription()).append("\n");
                }
                sendMessage(chatId, stringBuilder.toString());
            } else if (text.startsWith("/done ")) {
                if (taskService.completeTask(chatId, Long.valueOf(text.substring(6)))) {
                    sendMessage(chatId, "Задача №" + Long.valueOf(text.substring(6)) + " выполнена!");
                } else {
                    sendMessage(chatId, "Задача №" + Long.valueOf(text.substring(6)) +" не найдена!");
                }
            } else if (text.startsWith("/delete ")) {
                if (taskService.deleteTask(chatId, Long.valueOf(text.substring(8)))) {
                    sendMessage(chatId, "Задача №" + Long.valueOf(text.substring(8)) + " удалена!");
                } else {
                    sendMessage(chatId, "Задача №" + Long.valueOf(text.substring(8)) + " не найдена!");
                }
            }
        }
    }

    private void sendMessage(Long chatId, String text) {
        SendMessage sendMessage = new SendMessage(String.valueOf(chatId), text);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
