package rt.bot.telegram.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageSender {

    private final TelegramClient telegramClient;

    public void send(Long userId, String text) {
        try {
            telegramClient.execute(
                    SendMessage.builder()
                            .chatId(userId)
                            .text(text)
                            .build());
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке сообщения [{}] пользователю с id {}", text, userId);
        }
    }

    public void send(Long userID, String text, ReplyKeyboardMarkup keyboard) {
        try {
            telegramClient.execute(
                    SendMessage.builder()
                            .chatId(userID)
                            .text(text)
                            .replyMarkup(keyboard)
                            .build());
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке сообщения с Reply клавиатурой пользователю [{}]", userID);
        }
    }

    public void send(Long userID, String text, ReplyKeyboardRemove removeKeyboard) {
        try {
            telegramClient.execute(
                    SendMessage.builder()
                            .chatId(userID)
                            .text(text)
                            .replyMarkup(removeKeyboard)
                            .build());
        } catch (TelegramApiException e) {
            log.error("Ошибка при удалении Reply клавиатуры у пользователя [{}]: {}", userID, e.getMessage());
        }
    }

    public Message send(Long userID, String text, InlineKeyboardMarkup keyboard) {
        try {
            return telegramClient.execute(
                    SendMessage.builder()
                            .chatId(userID)
                            .text(text)
                            .replyMarkup(keyboard)
                            .build());
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке сообщения c inline-menu [{}] пользователю [{}]: {}", text, userID, e.getMessage());
            return null;
        }
    }

    public void send(Long userID, String text, ReplyKeyboardMarkup keyboard, String parseMode) {
        try {
            telegramClient.execute(
                    SendMessage.builder()
                            .chatId(userID)
                            .text(text)
                            .replyMarkup(keyboard)
                            .parseMode(parseMode)
                            .build());
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке сообщения с MarkdownV2 [{}] пользователю [{}]: {}", text, userID, e.getMessage());
        }
    }

    public void send(Queue<Long> allUsersIds, String userMsg) {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleWithFixedDelay(() -> {
            Long userId = allUsersIds.poll();
            if (userId != null) {
                send(userId, userMsg);
            } else {
                executor.shutdown();
            }
        }, 0, 51, TimeUnit.MILLISECONDS);
    }
}