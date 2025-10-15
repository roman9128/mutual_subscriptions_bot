package rt.bot.telegram.in.cases;

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
import rt.bot.telegram.in.TelegramUtils;
import rt.bot.telegram.out.MessageRemover;
import rt.bot.telegram.out.MessageSender;
import rt.bot.constant.Menu;
import rt.bot.constant.Text;
import rt.bot.telegram.handler.BotAdminHandler;
import rt.bot.telegram.handler.TariffAvailabilityHandler;
import rt.bot.telegram.handler.VipChannelHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class AddChannelCase implements Case {

    private final BotInfo botInfo;
    private final MessageSender sender;
    private final MessageRemover remover;
    private final BotAdminHandler botAdminHandler;
    private final TariffAvailabilityHandler tariffAvailabilityHandler;
    private final VipChannelHandler vipChannelHandler;
    private final UserService userService;
    private final ChannelService channelService;
    private final SubscriptionService subscriptionService;

    @Override
    public void process(Update update, BotUser botUser) {
        Long userId = TelegramUtils.extractUserIdFromUpdate(update);
        String userMsg = TelegramUtils.extractUserTextFromUpdate(update);

        if (isStartVipRequest(botUser, userMsg)) {
            vipChannelHandler.process(botUser, userId, userMsg);
            return;
        }
        if (isCancel(userMsg)) {
            handleCancelParticipation(botUser, userId);
        } else if (isStartTariffRequest(botUser, userMsg)) {
            handleStartRequest(botUser, userId, userMsg);
        } else if (botAddedAsAdmin(update, botUser)) {
            handleBotAssignment(update, botUser, userId);
        } else if (botDismissedDuringRegistration(update, botUser)) {
            handleBotDismissedDuringRegistration(botUser, userId);
        } else if (subscriptionConfirmed(botUser, userMsg)) {
            handleSubscriptionConfirmation(botUser, userId);
        } else if (paymentConfirmed(botUser, userMsg)) {
            handlePaymentConfirmation(botUser, userId);
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
            userService.setTariffAndStatus(botUser, Tariff.NONE, BotUser.DialogStatus.NONE);
            sender.send(userId, Text.CHANNEL_ADDED_EARLY, Menu.of(Text.START_AGAIN));
            return;
        }
        channelService.createNewChannel(botUser, update.getMyChatMember().getChat());
        sendChannelsToSubscribe(botUser, userId);
    }

    private void sendChannelsToSubscribe(BotUser botUser, Long userId) {
        if (botUser.getTariff() == Tariff.TARIFF_3) {
            handleSubscriptionConfirmation(botUser, userId);
            return;
        }
        String channelsList = subscriptionService.getChannelListToSubscribe(botUser);
        if (channelsList == null || channelsList.isBlank()) {
            channelService.removeCancelledChannel(userId);
            userService.setTariffAndStatus(botUser, Tariff.NONE, BotUser.DialogStatus.NONE);
            sender.send(userId, Text.CANCEL_DUE_TO_LACK_OF_CHANNELS, Menu.of(Text.START_AGAIN));
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
            return;
        }
        String link = "";
        userService.setDialogStatus(botUser, BotUser.DialogStatus.WAITING_PAYMENT_CONFIRMATION);
        if (botUser.getTariff() == Tariff.TARIFF_2) {
            link = "t2";
        } else if (botUser.getTariff() == Tariff.TARIFF_3) {
            link = "t3";
        }
        //todo payment service
        sender.send(
                userId,
                Text.PAYMENT_LINK.formatted(link),
                Menu.of(Text.PAYMENT_CONFIRM, Text.CANCEL_PARTICIPATION));
    }

    private void handlePaymentConfirmation(BotUser botUser, Long userId) {
        // todo payment service check payment
        finishProcess(botUser, userId);
    }

    private void handleCancelParticipation(BotUser botUser, Long userId) {
        channelService.removeCancelledChannel(userId);
        userService.setTariffAndStatus(botUser, Tariff.NONE, BotUser.DialogStatus.NONE);
        sender.send(userId, Text.CANCEL_PARTICIPATION_SUCCESS, Menu.of(Text.START_AGAIN));
    }

    private void handleBotDismissedDuringRegistration(BotUser botUser, Long userId) {
        channelService.removeCancelledChannel(userId);
        userService.setTariffAndStatus(botUser, Tariff.NONE, BotUser.DialogStatus.NONE);
        sender.send(userId, Text.FAILED_PARTICIPATION_DUE_TO_BOT_ADMIN_RIGHTS, Menu.of(Text.START_AGAIN));
    }

    private void finishProcess(BotUser botUser, Long userId) {
        userService.setRoleTariffStatus(botUser, BotUser.Role.USER, Tariff.NONE, BotUser.DialogStatus.NONE);
        channelService.setPaidSince(botUser.getUserId());
        sender.send(userId, Text.SUCCESS, Menu.of(Text.MY_SUBSCRIPTIONS));
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

    private boolean botAddedAsAdmin(Update update, BotUser botUser) {
        return TelegramUtils.botIsAddedAsAdmin(update) &&
                botUser.getDialogStatus() == BotUser.DialogStatus.WAITING_ADD_BOT_AS_ADMIN;
    }

    private boolean isStartTariffRequest(BotUser botUser, String userMsg) {
        return botUser.getDialogStatus() == BotUser.DialogStatus.NONE &&
                botUser.getTariff() == Tariff.NONE &&
                userMsg.startsWith(Text.TARIFF_START);
    }

    private boolean isStartVipRequest(BotUser botUser, String userMsg) {
        if (botUser.getRole() != BotUser.Role.ADMIN) return false;
        return (botUser.getDialogStatus() == BotUser.DialogStatus.NONE &&
                botUser.getTariff() == Tariff.NONE &&
                userMsg.equals(Text.START_VIP) ||
                botUser.getDialogStatus() == BotUser.DialogStatus.WAITING_CHANNEL_LINK);
    }

    private boolean subscriptionConfirmed(BotUser botUser, String userMsg) {
        return botUser.getDialogStatus() == BotUser.DialogStatus.WAITING_SUBSCRIPTION_CONFIRMATION &&
                userMsg.equals(Text.SUBSCRIPTION_CONFIRMATION);
    }

    private boolean paymentConfirmed(BotUser botUser, String userMsg) {
        if (botUser.getDialogStatus() != BotUser.DialogStatus.WAITING_PAYMENT_CONFIRMATION) return false;
        return userMsg.equals(Text.PAYMENT_CONFIRM);
    }

    private boolean isCancel(String userMsg) {
        return userMsg.equals(Text.CANCEL_PARTICIPATION);
    }

    private boolean botDismissedDuringRegistration(Update update, BotUser botUser) {
        return TelegramUtils.botIsDismissed(update) &&
                botUser.getDialogStatus() != BotUser.DialogStatus.NONE;
    }
}
