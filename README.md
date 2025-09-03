# CTO Anonim Service Bot 🤖

Telegram-бот для анонімного збору відгуків співробітників СТО.  
Він зберігає дані у базі, аналізує їх за допомогою OpenAI, а також записує результати у Google Docs.

---

## 🚀 Функціонал
- Реєстрація користувачів (по chatId).
- Збір анонімних відгуків.
- Визначення **тональності** відгуку (через OpenAI API).
- Оцінка **критичності** (через OpenAI API).
- Пропозиція можливого **рішення** (через OpenAI API).
- Збереження результатів у:
    - **PostgreSQL/MySQL** (таблиці `tg_user`, `feedback`).
    - **Google Docs** (для керівництва).

---

## 🛠️ Використані технології
- **Java 17**
- **Spring Boot 2.7**
- **Hibernate / JPA**
- **PostgreSQL**
- **OpenAI API (ChatGPT)**
- **Google Docs API**
- **Telegram Bots API**
- **Lombok**

---

## 🗂️ Структура проекту

src/main/java/org/example
├── controller     # Telegram UpdateHandler, Bot
├── model          # Сутності (TgUser, Feedback)
├── service        # GoogleDocsService, OpenAIClient, CurrencyRateService
├── repository     # JPA-репозиторії
└── AnonimServiceBot.java # entry-point

---

## 📌 Приклад збереження у Google Docs

СТО: Київ-Центр
Відгук: Дуже сподобався сервіс
Тональність: positive
Критичність: 0
Рішення: Продовжувати у тому ж дусі ✅

---

## 🧑‍💻 Автор

Розроблено в рамках навчального проекту.

Вдосконалюється 🚀

