package rt.bot.cases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import rt.bot.base.BotInfo;
import rt.bot.entity.BotUser;
import rt.bot.entity.Tariff;
import rt.bot.service.ChannelService;
import rt.bot.service.SubscriptionService;
import rt.bot.service.UserService;
import rt.bot.telegram.TelegramUtils;
import rt.bot.telegram.client.MessageRemover;
import rt.bot.telegram.client.MessageSender;
import rt.bot.telegram.constants.Menu;
import rt.bot.telegram.constants.Text;
import rt.bot.telegram.handlers.BotAdminHandler;
import rt.bot.telegram.handlers.TariffAvailabilityHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class StartCase implements Case {

    private final BotInfo botInfo;
    private final MessageSender sender;
    private final MessageRemover remover;
    private final BotAdminHandler botAdminHandler;
    private final TariffAvailabilityHandler tariffAvailabilityHandler;
    private final UserService userService;
    private final ChannelService channelService;
    private final SubscriptionService subscriptionService;

    @Override
    public void process(Update update, BotUser botUser) {
        Long userId = TelegramUtils.extractUserIdFromUpdate(update);
        String userMsg = TelegramUtils.extractUserTextFromUpdate(update);

        if (isStartTariffRequest(botUser, userMsg)) {
            handleStartRequest(botUser, userId, userMsg);
        } else if (botIsAddedAsAdmin(update, botUser)) {
            handleBotAssignment(update, botUser, userId);
        } else if (subscriptionConfirmed(botUser, userMsg)) {
            handleSubscriptionConfirmation(botUser, userId);
        } else if (isPaymentConfirmation(botUser, userMsg)) {
            handlePaymentConfirmation(botUser, userId, userMsg);
        } else if (isCancel(userMsg)) {
            handleCancelParticipation(botUser, userId);
        } else {
            informAboutError(botUser, userId);
        }
    }

    private void handleStartRequest(BotUser botUser, Long userId, String userMsg) {
        Tariff tariff = switch (userMsg) {
            case Text.TARIFF_1_START -> Tariff.TARIFF_1;
            case Text.TARIFF_2_START -> Tariff.TARIFF_2;
            case Text.TARIFF_3_START -> Tariff.TARIFF_3;
            default -> Tariff.TARIFF_1;
        };
        if (tariffAvailabilityHandler.isAvailable(tariff)) {
            userService.setTariffAndStatus(botUser, tariff, BotUser.DialogStatus.WAITING_ADD_BOT_AS_ADMIN);
            sender.send(userId, Text.TARIFF_SELECTED, Menu.removeReplyKeyboard());
            requestAddBotAsAdmin(botUser, userId);
        } else {
            sender.send(userId, Text.TARIFF_UNAVAILABLE, Menu.of(Text.LEARN_ANOTHER_TARIFF_REQUEST));
        }
    }

    private void requestAddBotAsAdmin(BotUser botUser, Long userId) {
        Message sentMessage = sender.send(
                userId,
                Text.ADD_BOT_AS_ADMIN_DESC,
                Menu.getButtonWithLink(
                        Text.ADD_BOT_AS_ADMIN,
                        "https://t.me/" + botInfo.getBotName() + "?startchannel&admin=invite_users+edit_messages"
                ));
        userService.setLastMessageIdToDelete(botUser, sentMessage.getMessageId());
    }

    private void handleBotAssignment(Update update, BotUser botUser, Long userId) {
        remover.removeMsg(userId, botUser.getLastMessageIdToDelete());
        botAdminHandler.process(update);
        if (channelService.channelExists(update.getMyChatMember().getChat().getId())) {
            sender.send(userId, Text.CHANNEL_ADDED_EARLY);
            userService.setTariffAndStatus(botUser, Tariff.NONE, BotUser.DialogStatus.NONE);
            sender.send(userId, Text.LEARN_ANOTHER_TARIFF, Menu.getTariffMenu());
            return;
        }
        channelService.createNewChannel(botUser, update.getMyChatMember().getChat());
        sendChannelsToSubscribe(botUser, userId);
    }

    private void sendChannelsToSubscribe(BotUser botUser, Long userId) {
        String channelsList = subscriptionService.getChannelListToSubscribe(botUser);
        if (channelsList == null || channelsList.isBlank()) {
            channelService.removeCancelledChannel(userId);
            userService.setTariffAndStatus(botUser, Tariff.NONE, BotUser.DialogStatus.NONE);
            sender.send(userId, Text.CANCEL_DUE_TO_LACK_OF_CHANNELS);
            log.info("Нет каналов для подписки для пользователя с id {}", userId);
            return;
        }
        userService.setDialogStatus(botUser, BotUser.DialogStatus.WAITING_SUBSCRIPTION_CONFIRMATION);
        sender.send(
                botUser.getUserId(),
                Text.SUBSCRIPTION_REQUIRED.formatted(channelsList),
                Menu.of(Text.SUBSCRIPTION_CONFIRMATION));
    }

    private void handleSubscriptionConfirmation(BotUser botUser, Long userId) {
        if (botUser.getTariff() == Tariff.TARIFF_1) {
            finishProcess(botUser, userId);
        } else {
            //todo payment service
            String link = "";
            userService.setDialogStatus(botUser, BotUser.DialogStatus.WAITING_PAYMENT_CONFIRMATION);
            sender.send(
                    userId,
                    Text.PAYMENT_LINK.formatted(link),
                    Menu.of(Text.PAYMENT_CONFIRM, Text.CANCEL_PARTICIPATION));
        }
    }

    private void handlePaymentConfirmation(BotUser botUser, Long userId, String userMsg) {
        // todo payment service check payment
        finishProcess(botUser, userId);
    }

    private void handleCancelParticipation(BotUser botUser, Long userId) {
        channelService.removeCancelledChannel(userId);
        userService.setTariffAndStatus(botUser, Tariff.NONE, BotUser.DialogStatus.NONE);
        sender.send(userId, Text.CANCEL_PARTICIPATION_SUCCESS);
        sender.send(userId, Text.GREET, Menu.getTariffMenu(), "MarkdownV2");
    }

    private void finishProcess(BotUser botUser, Long userId) {
        userService.setTariffAndStatus(botUser, Tariff.NONE, BotUser.DialogStatus.NONE);
        channelService.setPaidSince(botUser.getUserId());
        sender.send(userId, Text.SUCCESS, Menu.removeReplyKeyboard());
    }

    private void informAboutError(BotUser botUser, Long userId) {
        switch (botUser.getDialogStatus()) {
            case WAITING_ADD_BOT_AS_ADMIN -> sender.send(userId, Text.ERR_ADD_BOT, Menu.of(Text.CANCEL_PARTICIPATION));
            case WAITING_SUBSCRIPTION_CONFIRMATION ->
                    sender.send(userId, Text.ERR_SUBSCRIPTION, Menu.of(Text.SUBSCRIPTION_CONFIRMATION, Text.CANCEL_PARTICIPATION));
            case WAITING_PAYMENT_CONFIRMATION ->
                    sender.send(userId, Text.ERR_PAYMENT, Menu.of(Text.PAYMENT_CONFIRM, Text.CANCEL_PARTICIPATION));
            case null, default -> sender.send(userId, Text.UNKNOWN_ERR);
        }
    }

    private boolean botIsAddedAsAdmin(Update update, BotUser botUser) {
        return TelegramUtils.botIsAddedAsAdmin(update) &&
                botUser.getDialogStatus() == BotUser.DialogStatus.WAITING_ADD_BOT_AS_ADMIN;
    }

    private boolean isStartTariffRequest(BotUser botUser, String userMsg) {
        return botUser.getDialogStatus() == BotUser.DialogStatus.NONE &&
                botUser.getTariff() == Tariff.NONE &&
                userMsg.startsWith(Text.TARIFF_START);
    }

    private boolean subscriptionConfirmed(BotUser botUser, String userMsg) {
        return botUser.getDialogStatus() == BotUser.DialogStatus.WAITING_SUBSCRIPTION_CONFIRMATION &&
                userMsg.equals(Text.SUBSCRIPTION_CONFIRMATION);
    }

    private boolean isPaymentConfirmation(BotUser botUser, String userMsg) {
        if (botUser.getDialogStatus() != BotUser.DialogStatus.WAITING_PAYMENT_CONFIRMATION) return false;
        return userMsg.equals(Text.PAYMENT_CONFIRM);
    }

    private boolean isCancel(String userMsg) {
        return userMsg.equals(Text.CANCEL_PARTICIPATION);
    }
}
