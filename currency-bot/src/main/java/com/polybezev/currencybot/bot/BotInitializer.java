package com.polybezev.currencybot.bot;

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

        currencyBot.execute(new SetMyCommands(List.of(
                new BotCommand("/start",   "Запустить бота"),
                new BotCommand("/help",    "Все команды и помощь"),
                new BotCommand("/curse",   "Курс валюты — пример: /curse USD"),
                new BotCommand("/convert", "Конвертер — пример: /convert 100 USD RUB"),
                new BotCommand("/list",    "Список всех валют ЦБ РФ"),
                new BotCommand("/btc",     "Курс биткоина в ₽ и $"),
                new BotCommand("/tier",    "Подписки и возможности"),
                new BotCommand("/signal",  "Торговые сигналы RSI/MACD (TIER 2)")
        ), new BotCommandScopeDefault(), null));

        log.info("CurrencyBot registered successfully");
    }
}
