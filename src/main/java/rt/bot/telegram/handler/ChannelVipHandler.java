package rt.bot.telegram.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import rt.bot.entity.BotUser;
import rt.bot.entity.ChannelTariff;
import rt.bot.service.ChannelService;
import rt.bot.service.UserService;
import rt.bot.telegram.in.utils.TelegramUtils;
import rt.bot.telegram.out.ChannelInfoGetter;
import rt.bot.telegram.out.MessageSender;
import rt.bot.constant.Menu;
import rt.bot.constant.Text;

@Component
@RequiredArgsConstructor
public class ChannelVipHandler {

    private final MessageSender sender;
    private final UserService userService;
    private final ChannelService channelService;
    private final ChannelInfoGetter channelInfoGetter;

    public void process(BotUser botUser, Long userId, String userMsg) {
        if (userMsg.equals(Text.CANCEL_PARTICIPATION)) {
            cancel(botUser, userId);
        } else if (userMsg.equals(Text.START_VIP)) {
            askLink(botUser, userId);
        } else if (botUser.getDialogStatus() == BotUser.DialogStatus.WAITING_CHANNEL_LINK) {
            addVipChannel(botUser, userId, userMsg);
        }
    }

    private void cancel(BotUser botUser, Long userId) {
        userService.setTariffAndStatus(botUser, ChannelTariff.Tariff.NONE, BotUser.DialogStatus.NONE);
        sender.send(userId, Text.CANCEL_PARTICIPATION_SUCCESS, Menu.of(Text.START_AGAIN));
    }

    private void askLink(BotUser botUser, Long userId) {
        userService.setTariffAndStatus(botUser, ChannelTariff.Tariff.VIP, BotUser.DialogStatus.WAITING_CHANNEL_LINK);
        sender.send(userId, Text.LINK_REQUEST, Menu.of(Text.CANCEL_PARTICIPATION));
    }

    private void addVipChannel(BotUser botUser, Long userId, String userMsg) {
        if (!userMsg.matches(Text.LINK_PATTERN)) {
            sender.send(userId, Text.ERR_LINK, Menu.of(Text.CANCEL_PARTICIPATION));
            return;
        }
        Chat channel = channelInfoGetter.getChatInfoByChannelName(TelegramUtils.getChatNameFromLink(userMsg));
        if (channel == null) {
            userService.setTariffAndStatus(botUser, ChannelTariff.Tariff.NONE, BotUser.DialogStatus.NONE);
            sender.send(userId, Text.UNKNOWN_ERR, Menu.of(Text.START_AGAIN));
            return;
        }
        if (channelService.channelExists(channel.getId())) {
            userService.setTariffAndStatus(botUser, ChannelTariff.Tariff.NONE, BotUser.DialogStatus.NONE);
            sender.send(userId, Text.CHANNEL_ADDED_EARLY, Menu.of(Text.START_AGAIN));
            return;
        }
        channelService.createNewVipChannel(botUser, channel);
        userService.setTariffAndStatus(botUser, ChannelTariff.Tariff.NONE, BotUser.DialogStatus.NONE);
        sender.send(userId, Text.SUCCESS_VIP, Menu.of(Text.START_AGAIN));
    }
}
