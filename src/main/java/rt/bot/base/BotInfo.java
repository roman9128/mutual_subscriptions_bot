package rt.bot.base;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.GetMe;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Slf4j
@Component
@Getter
@RequiredArgsConstructor
public class BotInfo {

    @Value("${application.telegram.name}")
    private String botName;
    private Long botUserId;
    private final TelegramClient telegramClient;

    @PostConstruct
    public void init() {
        botUserId = gainBotUserId();
        log.info("Telegram Bot ID: {}", botUserId);
        log.info("Telegram Bot name: {}", botName);
    }

    private Long gainBotUserId() {
        try {
            return telegramClient.execute(GetMe.builder().build()).getId();
        } catch (TelegramApiException e) {
            log.error("Ошибка при получении ID бота: {}", e.getMessage());
            return null;
        }
    }
}
