package rt.bot.telegram.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChat;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChannelInfoGetter {

    private final TelegramClient telegramClient;

    public Chat getChatInfoByChannelName(String name) {
        try {
            return telegramClient.execute(new GetChat(name));
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
            return null;
        }
    }
}
