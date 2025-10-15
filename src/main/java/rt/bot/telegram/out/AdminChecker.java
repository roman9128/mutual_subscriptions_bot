package rt.bot.telegram.out;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberAdministrator;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import rt.bot.base.BotInfo;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminChecker {

    private final TelegramClient telegramClient;
    private final BotInfo botInfo;

    public boolean checkBotAdminRights(Long channelId) {
        if (botInfo.getBotUserId() == null) {
            return false;
        }
        return isBotChannelAdmin(channelId);
    }

    private boolean isBotChannelAdmin(Long channelId) {
        try {
            GetChatMember getChatMember = GetChatMember.builder()
                    .chatId(channelId)
                    .userId(botInfo.getBotUserId())
                    .build();

            ChatMember chatMember = telegramClient.execute(getChatMember);
            return isAdmin(chatMember);

        } catch (TelegramApiException e) {
            log.error("Ошибка при проверке прав администратора: {}", e.getMessage());
            return false;
        }
    }

    private boolean isAdmin(ChatMember chatMember) {
        return chatMember instanceof ChatMemberAdministrator;
    }
}