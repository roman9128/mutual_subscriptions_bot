package rt.bot.cases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import rt.bot.dto.ChannelStatus;
import rt.bot.dto.ChannelStatusLists;
import rt.bot.entity.BotUser;
import rt.bot.service.SubscriptionService;
import rt.bot.telegram.TelegramUtils;
import rt.bot.telegram.client.MessageSender;
import rt.bot.telegram.constants.Menu;
import rt.bot.telegram.constants.Text;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class InformationCase implements Case {

    private final MessageSender sender;
    private final SubscriptionService subscriptionService;

    @Override
    public void process(Update update, BotUser botUser) {
        BotUser.Role role = botUser.getRole();
        Long userId = botUser.getUserId();
        String userMsg = TelegramUtils.extractUserTextFromUpdate(update);
        switch (userMsg) {
            case Text.START -> sendGreetings(userId, role);
            case Text.TARIFF_1_REQUEST -> sendTariffInfo(userId, Text.TARIFF_1_INFO, Text.TARIFF_1_START);
            case Text.TARIFF_2_REQUEST -> sendTariffInfo(userId, Text.TARIFF_2_INFO, Text.TARIFF_2_START);
            case Text.TARIFF_3_REQUEST -> sendTariffInfo(userId, Text.TARIFF_3_INFO, Text.TARIFF_3_START);
            case Text.MY_SUBSCRIPTIONS -> sendSubscriptionStat(userId, role);
            case null, default -> sendInfo(userId, role);
        }
    }

    private void sendGreetings(Long userId, BotUser.Role role) {
        if (role == BotUser.Role.GUEST) {
            sender.send(userId, Text.GREET, Menu.getTariffMenu(), "MarkdownV2");
        } else if (role == BotUser.Role.ADMIN) {
            sender.send(
                    userId,
                    Text.GREET,
                    Menu.of(
                            Text.TARIFF_1_REQUEST,
                            Text.TARIFF_2_REQUEST,
                            Text.TARIFF_3_REQUEST,
                            Text.START_VIP,
                            Text.MY_SUBSCRIPTIONS),
                    "MarkdownV2");
        } else {
            sender.send(
                    userId,
                    Text.GREET,
                    Menu.of(
                            Text.TARIFF_1_REQUEST,
                            Text.TARIFF_2_REQUEST,
                            Text.TARIFF_3_REQUEST,
                            Text.MY_SUBSCRIPTIONS),
                    "MarkdownV2");
        }
    }

    private void sendTariffInfo(Long userId, String tariffInfo, String tariffStart) {
        sender.send(userId, tariffInfo, Menu.of(tariffStart, Text.LEARN_ANOTHER_TARIFF_REQUEST));
    }

    private void sendInfo(Long userId, BotUser.Role role) {
        if (role == BotUser.Role.GUEST) {
            sender.send(userId, Text.LEARN_ANOTHER_TARIFF, Menu.getTariffMenu());
        } else if (role == BotUser.Role.ADMIN) {
            sender.send(
                    userId,
                    Text.LEARN_ANOTHER_TARIFF,
                    Menu.of(
                            Text.TARIFF_1_REQUEST,
                            Text.TARIFF_2_REQUEST,
                            Text.TARIFF_3_REQUEST,
                            Text.START_VIP,
                            Text.MY_SUBSCRIPTIONS));
        } else {
            sender.send(
                    userId,
                    Text.LEARN_ANOTHER_TARIFF,
                    Menu.of(
                            Text.TARIFF_1_REQUEST,
                            Text.TARIFF_2_REQUEST,
                            Text.TARIFF_3_REQUEST,
                            Text.MY_SUBSCRIPTIONS));
        }
    }

    private void sendSubscriptionStat(Long userId, BotUser.Role role) {
        if (role == BotUser.Role.GUEST) return;
        ChannelStatusLists channelStatusLists = subscriptionService.getChannelStatusList(userId);
        String followedChannels = channelStatusLists.followed()
                .stream()
                .map(cs -> "@" + cs.username())
                .collect(Collectors.joining(System.lineSeparator()));
        String unfollowedChannels = channelStatusLists.unfollowed()
                .stream()
                .map(cs -> "@" + cs.username())
                .collect(Collectors.joining(System.lineSeparator()));
        if (followedChannels.isBlank()) followedChannels = Text.NO_INFO;
        if (unfollowedChannels.isBlank()) unfollowedChannels = Text.NO_INFO;
        sender.send(
                userId,
                Text.MY_SUBSCRIPTIONS_DETAILED.formatted(
                        followedChannels,
                        unfollowedChannels),
                Menu.of(Text.START_AGAIN));
    }
}