package org.example.service;

import org.example.model.Feedback;
import org.example.model.FeedbackRepository;
import org.example.model.TgUser;
import org.example.model.TgUserRepository;
import org.example.openai.OpenAIClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Map;

@Service
public class FeedbackAnalysisService {

    private final OpenAIClient openAIClient;
    private final FeedbackRepository feedbackRepository;
    private final TgUserRepository tgUserRepository;


    public FeedbackAnalysisService(TgUserRepository tgUserRepository,
                                   OpenAIClient openAIClient, 
                                   FeedbackRepository feedbackRepository) {
        this.openAIClient = openAIClient;
        this.feedbackRepository = feedbackRepository;
        this.tgUserRepository = tgUserRepository;

    }

    /**
     * Аналізує текст від користувача та повертає Feedback.
     */
    public Feedback analyzeText(Long chatId, String messageText) {
        // Отримуємо користувача для філії
        TgUser user = tgUserRepository.findByChatId(chatId).orElse(null);

        // Створюємо Feedback до виклику OpenAI
        Feedback feedback = new Feedback();
        feedback.setChatId(chatId);
        feedback.setMessageText(messageText);
        feedback.setFilial(user != null ? user.getFilial() : "-");

        try {
            // Виклик OpenAI для аналізу тексту
            Map<String, Object> analysis = openAIClient.analyzeText(messageText);

            // Присвоюємо результати аналізу
            feedback.setSentiment((String) analysis.get("sentiment")); // POSITIVE, NEUTRAL, NEGATIVE
            feedback.setCriticality((Integer) analysis.get("criticality")); // 1..5
            feedback.setSolution((String) analysis.get("resolution")); // рекомендації

        } catch (HttpClientErrorException.TooManyRequests e) {
            // Якщо перевищено ліміт
            feedback.setSentiment("insufficient_quota");
            feedback.setCriticality(0);
            feedback.setSolution("insufficient_quota");

        } catch (Exception e) {
            // Будь-які інші помилки
            e.printStackTrace();
            feedback.setSentiment("error");
            feedback.setCriticality(0);
            feedback.setSolution("error");
        }

        // Зберігаємо у БД у будь-якому випадку
        feedbackRepository.save(feedback);

        return feedback;
    }

}
