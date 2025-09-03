package org.example.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.model.Feedback;
import org.example.model.FeedbackRepository;
import org.example.model.TgUser;
import org.example.model.TgUserRepository;
import org.example.service.FeedbackAnalysisService;
import org.example.service.GoogleDocsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static org.example.constants.Constants.HELP;

@Slf4j
@Component
public class UpdateController {

    private final TgUserRepository tgUserRepository;
    private final FeedbackRepository feedbackRepository;
    private final FeedbackAnalysisService feedbackAnalysisService;
    private final GoogleDocsService googleDocsService;

    private TelegramBot telegramBot;

    @Autowired
    public UpdateController(TgUserRepository tgUserRepository,
                            FeedbackRepository feedbackRepository,
                            FeedbackAnalysisService feedbackAnalysisService, GoogleDocsService googleDocsService) {
        this.tgUserRepository = tgUserRepository;
        this.feedbackRepository = feedbackRepository;
        this.feedbackAnalysisService = feedbackAnalysisService;
        this.googleDocsService = googleDocsService;
    }

    public void registreBot(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    public void processUpdate(Update update) {
        if (update == null) {
            log.error("--- Received update is null");
            return;
        }

        if (update.hasCallbackQuery()) {
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            int messageId = update.getCallbackQuery().getMessage().getMessageId();
            handleCallback(chatId, update.getCallbackQuery().getData(), messageId);
        } else if (update.hasMessage() && !update.getMessage().getFrom().getIsBot()) {
            processUserMessageUpdate(update);
        } else {
            log.error("--- Received unsupported message type " + update);
        }
    }

    private void handleCallback(long chatId, String callbackData, int messageId) {
        if (!callbackData.startsWith("button_click")) return;

        removeButtons(chatId, messageId);

        if (callbackData.endsWith("ROLE")) {
            String role = callbackData.contains("1ROLE") ? "Механік" :
                    callbackData.contains("2ROLE") ? "Електрик" : "Менеджер";

            saveUserRole(chatId, role);

            sendMessage(chatId, "✅ Ваша посада: " + role + "\nТепер оберіть філію.");

            String[] filialButtons = {"Оберіть філію", "СТО №1", "СТО №2", "СТО №3"};
            sendButtonMessage(chatId, filialButtons, "FILIAL");
        } else if (callbackData.endsWith("FILIAL")) {
            String filial = callbackData.contains("1FILIAL") ? "СТО №1" :
                    callbackData.contains("2FILIAL") ? "СТО №2" : "СТО №3";

            saveUserFilial(chatId, filial);

            sendMessage(chatId, "✅ Ви закріплені за філією: " + filial +
                    "\nТепер можете надсилати відгуки.");

            sendUserProfile(chatId);
        }
    }

    private void processUserMessageUpdate(Update update) {
        long chatId = update.getMessage().getChatId();
        String messageText = update.getMessage().getText();

        switch (messageText) {
            case "/start":
                sendMessage(chatId, "Вітаємо.");
                if (checkID(chatId)) selectRole(chatId);
                break;
            case "/help":
                sendMessage(chatId, HELP);
                break;
            case "/reset":
                if (checkID(chatId)) {
                    sendMessage(chatId, "Треба завершити реєстрацію.");
                    selectRole(chatId);
                } else {
                    sendMessage(chatId, "Можна залишати відгук.");
                }
                break;
            default:
                if (checkID(chatId)) {
                    sendMessage(chatId, "Треба завершити реєстрацію.");
                    selectRole(chatId);
                } else {
                    try {
                        // --- Аналіз відгуку через OpenAI ---
                        Feedback feedback = feedbackAnalysisService.analyzeText(chatId, messageText);
                        // --- Збереження у PostgreSQL ---
                        feedbackRepository.save(feedback);
                        // --- Дублювання у Google Docs ---
                        googleDocsService.appendFeedback(
                                String.format("Філія: %s\nВідгук: %s\nТональність: %s\nКритичність: %d\nРішення: %s\n",
                                        feedback.getFilial(),
                                        feedback.getMessageText(),
                                        feedback.getSentiment(),
                                        feedback.getCriticality(),
                                        feedback.getSolution())
                        );

                        String reply = "✅ Дякуємо за відгук!";
                        sendMessage(chatId, reply);

                    } catch (Exception e) {
                        e.printStackTrace();
                        sendMessage(chatId, "❌ Сталася помилка при обробці відгуку. Спробуйте пізніше.");
                    }
                }
                break;
        }
    }

    // --- Методи для роботи з користувачем ---
    private void saveUserRole(long chatId, String role) {
        TgUser user = tgUserRepository.findByChatId(chatId).orElse(null);
        if (user == null) {
            user = new TgUser();
            user.setChatId(chatId);
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));
        }
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole(role);
            tgUserRepository.save(user);
        }
    }

    private void saveUserFilial(long chatId, String filial) {
        TgUser user = tgUserRepository.findByChatId(chatId).orElse(null);
        if (user == null) {
            user = new TgUser();
            user.setChatId(chatId);
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));
        }
        if (user.getFilial() == null || user.getFilial().isEmpty()) {
            user.setFilial(filial);
            tgUserRepository.save(user);
        }
    }

    private boolean checkID(long chatId) {
        TgUser user = tgUserRepository.findByChatId(chatId).orElse(null);
        if (user == null) {
            user = new TgUser();
            user.setChatId(chatId);
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));
            tgUserRepository.save(user);
        }
        return user.getRole() == null || user.getRole().isEmpty() ||
                user.getFilial() == null || user.getFilial().isEmpty();
    }

    private void selectRole(long chatId) {
        String[] text = {"Ваша посада", "механік", "електрик", "менеджер"};
        sendButtonMessage(chatId, text, "ROLE");
    }

    private void sendUserProfile(long chatId) {
        TgUser user = tgUserRepository.findByChatId(chatId)
                .orElseThrow(() -> new RuntimeException("Користувач з chatId " + chatId + " не знайдений"));

        StringBuilder sb = new StringBuilder();
        sb.append("✅ Ваш профіль:\n")
                .append("ID: ").append(user.getId()).append("\n")
                .append("chatId: ").append(user.getChatId()).append("\n")
                .append("Роль: ").append(user.getRole() != null ? user.getRole() : "-").append("\n")
                .append("Філія: ").append(user.getFilial() != null ? user.getFilial() : "-").append("\n")
                .append("Зареєстрований: ").append(user.getRegisteredAt() != null ? user.getRegisteredAt() : "-");

        sendMessage(chatId, sb.toString());
    }

    private void deleteUserByChatId(Long chatId) {
        TgUser user = tgUserRepository.findByChatId(chatId)
                .orElseThrow(() -> new RuntimeException("Користувач не знайдений"));
        tgUserRepository.delete(user);
    }

    // --- Telegram клавіатура та повідомлення ---
    private void removeButtons(long chatId, int messageId) {
        EditMessageReplyMarkup editMessage = new EditMessageReplyMarkup();
        editMessage.setChatId(chatId);
        editMessage.setMessageId(messageId);
        editMessage.setReplyMarkup(null);
        telegramBot.sendEditMessageReplyMarkup(editMessage);
    }

    private void sendButtonMessage(long chatId, String[] text, String key) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text[0]);

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        for (int t = 1; t < text.length; t++) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(text[t]);
            button.setCallbackData("button_click" + t + key);
            List<InlineKeyboardButton> rowInline = new ArrayList<>();
            rowInline.add(button);
            rowsInline.add(rowInline);
        }

        inlineKeyboardMarkup.setKeyboard(rowsInline);
        message.setReplyMarkup(inlineKeyboardMarkup);
        setView(message);
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(textToSend);
        setView(message);
    }

    public void setView(SendMessage sendMessage) {
        telegramBot.sendAnswerMessage(sendMessage);
    }
}
