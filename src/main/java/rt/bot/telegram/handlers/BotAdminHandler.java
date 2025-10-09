package rt.bot.telegram.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberAdministrator;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberLeft;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberUpdated;
import rt.bot.service.ChannelService;

@Slf4j
@Component
@RequiredArgsConstructor
public class BotAdminHandler {

    private final ChannelService channelService;

    public void process(Update update) {
        ChatMemberUpdated cmu = update.getMyChatMember();
        Chat chat = cmu.getChat();
        User user = cmu.getFrom();
        if (cmu.getNewChatMember() instanceof ChatMemberAdministrator) {
            handleBotAssigned(chat, user);
        } else if (cmu.getNewChatMember() instanceof ChatMemberLeft) {
            handleBotDismissed(chat, user);
        }
    }

    private void handleBotAssigned(Chat chat, User user) {
        channelService.addBotAsAdminToChannel(user, chat);
        log.info("Бот добавлен админом в канал {}", chat.getTitle());
    }

    private void handleBotDismissed(Chat chat, User user) {
        log.warn("Бот удалён из админов канала {}", chat.getTitle());
    }
}
