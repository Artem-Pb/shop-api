package com.polybezev.currencybot.config;

import com.polybezev.currencybot.bot.CurrencyBot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BotInitializer {
    private final CurrencyBot currencyBot;

    @EventListener(ContextRefreshedEvent.class)
    public void init() throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(currencyBot);
        List<BotCommand> botCommands = new ArrayList<>();
        botCommands.add(new BotCommand("/start", "Запустить бота"));
        botCommands.add(new BotCommand("/help", "Все команды и помощь"));
        botCommands.add(new BotCommand("/list", "Список доступных валют"));
        botCommands.add(new BotCommand("/curse", "Курсы валют - пример: /curse USD"));
        botCommands.add(new BotCommand("/convert", "Конвертер - пример: /convert 100 USD RUB"));

        currencyBot.execute(new SetMyCommands(botCommands, new BotCommandScopeDefault(), null));
        log.info("CurrencyBot registered successfully");
    }
}