package org.example.model;


import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import java.sql.Timestamp;

@Data
@Entity
@Table(name = "tg_user") // рекомендую нижній регістр для імені таблиці
public class TgUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long chatId;
    private String role;
    private String filial;
    private Timestamp registeredAt;
}


