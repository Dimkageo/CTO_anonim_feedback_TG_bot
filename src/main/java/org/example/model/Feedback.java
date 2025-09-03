package org.example.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "feedback")
@Getter
@Setter
@NoArgsConstructor
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chat_id", nullable = false)
    private Long chatId; // Telegram chatId користувача

    @Column(name = "message_text", columnDefinition = "TEXT", nullable = false)
    private String messageText; // текст відгуку

    @Column(name = "sentiment", nullable = false)
    private String sentiment; // позитивний/нейтральний/негативний/insufficient_quota

    @Column(name = "criticality")
    private int criticality; // 1-5

    @Column(name = "solution", columnDefinition = "TEXT")
    private String solution; // як можна вирішити питання

    @Column(name = "filial")
    private String filial; // філія користувача

    @Column(name = "created_at", nullable = false, updatable = false)
    private Timestamp createdAt;

    public Feedback(Long chatId, String messageText, String sentiment, int criticality, String solution, String filial) {
        this.chatId = chatId;
        this.messageText = messageText;
        this.sentiment = sentiment;
        this.criticality = criticality;
        this.solution = solution;
        this.filial = filial;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }
}
