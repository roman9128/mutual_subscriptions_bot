package rt.bot.telegram.out;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageRemover {

    private final TelegramClient telegramClient;

    public void removeMsg(Long chatId, int messageId) {
        try {
            telegramClient.execute(
                    DeleteMessage.builder()
                            .chatId(chatId)
                            .messageId(messageId)
                            .build());
        } catch (TelegramApiException e) {
            log.error("Ошибка при удалении сообщения, отправленного пользователю с id {}: {}", chatId, e.getMessage());
        }
    }
}