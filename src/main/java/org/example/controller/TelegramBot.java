package org.example.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    @Value("${bot.name}")
    private String botName;

    @Value("${bot.token}")
    private String botToken;

    private final UpdateController updateController;

    public TelegramBot(UpdateController updateController) {
        this.updateController = updateController;
    }

    @PostConstruct
    public void init() {
        List<BotCommand> botCommands = new ArrayList<>();
        botCommands.add(new BotCommand("/help", "Допомога."));
        botCommands.add(new BotCommand("/reset", "Перезавантаження."));

        SetMyCommands setMyCommands = new SetMyCommands();
        setMyCommands.setCommands(botCommands);
        setMyCommands.setScope(new BotCommandScopeDefault());

        try {
            this.execute(setMyCommands);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }

        updateController.registreBot(this);
    }

    @Override
    public void onUpdateReceived(Update update) {
        updateController.processUpdate(update);
    }

    // --- Методи-відправники ---
    public void sendAnswerMessage(SendMessage message) {
        if (message != null) {
            try {
                execute(message);
            } catch (TelegramApiException e) {
                log.error("Помилка при sendAnswerMessage: " + e.getMessage());
            }
        }
    }

    public Message sendAnswerMessageDel(SendMessage message) {
        if (message != null) {
            try {
                return execute(message);
            } catch (TelegramApiException e) {
                log.error("Помилка при sendAnswerMessageDel: " + e.getMessage());
            }
        }
        return null;
    }

    public void sendEditMessageText(EditMessageText editMessageText) {
        if (editMessageText != null) {
            try {
                execute(editMessageText);
            } catch (TelegramApiException e) {
                log.error("Помилка при sendEditMessageText: " + e.getMessage());
            }
        }
    }

    public void sendEditMessageReplyMarkup(EditMessageReplyMarkup editMessageReplyMarkup) {
        if (editMessageReplyMarkup != null) {
            try {
                execute(editMessageReplyMarkup);
            } catch (TelegramApiException e) {
                log.error("Помилка при sendEditMessageReplyMarkup: " + e.getMessage());
            }
        }
    }

    public Message sendDocument(SendDocument sendDocument) {
        try {
            return execute(sendDocument);
        } catch (TelegramApiException e) {
            log.error("Не вдалося відправити документ: " + e.getMessage());
        }
        return null;
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }
}

