package rt.bot.telegram.client;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.GetMe;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberAdministrator;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminChecker {

    private final TelegramClient telegramClient;
    private Long botUserId;

    @PostConstruct
    public void setBotUserId() {
        botUserId = getBotUserId();
        log.info("Telegram Bot ID получен: {}", botUserId);
    }

    public boolean checkBotAdminRights(Long channelId) {
        if (botUserId == null) {
            return false;
        }
        return isBotChannelAdmin(channelId);
    }

    private Long getBotUserId() {
        try {
            return telegramClient.execute(GetMe.builder().build()).getId();
        } catch (TelegramApiException e) {
            log.error("Ошибка при получении ID бота: {}", e.getMessage());
            return null;
        }
    }

    private boolean isBotChannelAdmin(Long channelId) {
        try {
            GetChatMember getChatMember = GetChatMember.builder()
                    .chatId(channelId)
                    .userId(botUserId)
                    .build();

            ChatMember chatMember = telegramClient.execute(getChatMember);
            return isAdmin(chatMember);

        } catch (TelegramApiException e) {
            log.error("Ошибка при проверке прав администратора: {}", e.getMessage());
            return false;
        }
    }

    private boolean isAdmin(ChatMember chatMember) {
        if (chatMember instanceof ChatMemberAdministrator admin) {
            return admin.getStatus().equals("administrator");
        }

        String status = chatMember.getStatus();
        return status.equals("administrator");
    }
}