package com.example.Documentation.config;

import com.example.Documentation.ApplicationBot;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class BotConfig {

    private final ApplicationBot applicationBot;

    public BotConfig(ApplicationBot applicationBot) {
        this.applicationBot = applicationBot;
    }

    @PostConstruct
    public void registerBot() {
        try {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(applicationBot);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }



}
