package rt.bot.telegram.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberAdministrator;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberLeft;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberUpdated;
import rt.bot.constant.Text;
import rt.bot.service.ChannelService;
import rt.bot.telegram.out.MessageSender;

@Component
@RequiredArgsConstructor
public class BotAdminHandler {

    private final ChannelService channelService;
    private final MessageSender sender;

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
        boolean ok = channelService.toggleBotAdminRights(chat, true);
        if (ok) {
            sender.send(user.getId(), Text.BOT_ADDED.formatted(chat.getTitle()));
        } else {
            sender.send(user.getId(), Text.NO_CHANNEL.formatted(chat.getTitle()));
        }
    }

    private void handleBotDismissed(Chat chat, User user) {
        boolean ok = channelService.toggleBotAdminRights(chat, false);
        if (ok) {
            sender.send(user.getId(), Text.BOT_DISMISSED.formatted(chat.getTitle()));
        } else {
            sender.send(user.getId(), Text.NO_CHANNEL.formatted(chat.getTitle()));
        }
    }
}
