package rt.bot.telegram.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import rt.bot.base.BotInfo;
import rt.bot.constant.Menu;
import rt.bot.constant.Text;
import rt.bot.entity.BotUser;
import rt.bot.entity.ChannelTariff;
import rt.bot.service.ChannelService;
import rt.bot.service.UserService;
import rt.bot.telegram.in.utils.YesNo;
import rt.bot.telegram.out.MessageRemover;
import rt.bot.telegram.out.MessageSender;

@Component
@RequiredArgsConstructor
public class ChannelRegularHandler {

    private final BotInfo botInfo;
    private final MessageSender sender;
    private final MessageRemover remover;
    private final TariffAvailabilityHandler tariffAvailabilityHandler;
    private final BotAdminHandler botAdminHandler;
    private final UserService userService;
    private final ChannelService channelService;

    public void process(Update update, BotUser botUser, Long userId, String userMsg) {
        if (YesNo.isItCancel(userMsg)) {
            handleCancelParticipation(botUser, userId);
        } else if (YesNo.isItStartTariffRequest(botUser, userMsg)) {
            handleStartRequest(botUser, userId, userMsg);
        } else if (YesNo.isBotAddedAsAdmin(update, botUser)) {
            handleBotAssignment(update, botUser, userId);
        } else if (YesNo.isBotDismissedDuringRegistration(update, botUser)) {
            handleBotDismissedDuringRegistration(botUser, userId);
        } else if (YesNo.isPaymentPeriod(botUser, userMsg)) {
            askPayment(botUser, userId, userMsg);
        } else if (YesNo.isPaymentConfirmed(botUser, userMsg)) {
            handlePaymentConfirmation(botUser, userId);
        } else {
            informAboutError(botUser, userId);
        }
    }

    private void handleStartRequest(BotUser botUser, Long userId, String userMsg) {
        ChannelTariff.Tariff tariff = switch (userMsg) {
            case Text.TARIFF_1_START -> ChannelTariff.Tariff.TARIFF_1;
            case Text.TARIFF_2_START -> ChannelTariff.Tariff.TARIFF_2;
            case Text.TARIFF_3_START -> ChannelTariff.Tariff.TARIFF_3;
            default -> ChannelTariff.Tariff.TARIFF_1;
        };
        if (tariffAvailabilityHandler.isAvailable(tariff)) {
            userService.updateUser(botUser, tariff, BotUser.DialogStatus.WAITING_ADD_BOT_AS_ADMIN);
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
        userService.updateUser(botUser, sentMessage.getMessageId());
    }

    private void handleBotAssignment(Update update, BotUser botUser, Long userId) {
        remover.removeMsg(userId, botUser.getLastMessageIdToDelete());
        if (channelService.channelExists(update.getMyChatMember().getChat().getId())) {
            userService.updateUser(botUser, ChannelTariff.Tariff.NONE, BotUser.DialogStatus.NONE);
            sender.send(userId, Text.CHANNEL_ADDED_EARLY, Menu.of(Text.START_AGAIN));
            botAdminHandler.process(update);
            return;
        }
        channelService.createNewChannel(botUser, update.getMyChatMember().getChat());
        askPaymentPeriod(botUser, userId);
    }

    private void askPaymentPeriod(BotUser botUser, Long userId) {
        if (botUser.getChosenTariff() == ChannelTariff.Tariff.TARIFF_1) { // dlia niego toljko 1 god
            botUser.setChosenPeriod(ChannelTariff.ChosenPeriod.YEAR);
            finishProcess(botUser, userId);
            return;
        }
        userService.updateUser(botUser, BotUser.DialogStatus.WAITING_PAYMENT_PERIOD);
        sender.send(userId, Text.PAYMENT_PERIOD_REQUIRED, Menu.of(Text.PAY_1_MONTH, Text.PAY_1_YEAR, Text.CANCEL_PARTICIPATION));
    }

    private void askPayment(BotUser botUser, Long userId, String userMsg) {
        ChannelTariff.ChosenPeriod period = switch (userMsg) {
            case Text.PAY_1_MONTH -> ChannelTariff.ChosenPeriod.MONTH;
            case null, default -> ChannelTariff.ChosenPeriod.YEAR;
        };
        userService.updateUser(botUser, BotUser.DialogStatus.WAITING_PAYMENT_CONFIRMATION, period);
        String link = period.name();       //todo payment service
        if (botUser.getChosenTariff() == ChannelTariff.Tariff.TARIFF_2) {
            link += "t2";
        } else if (botUser.getChosenTariff() == ChannelTariff.Tariff.TARIFF_3) {
            link += "t3";
        }
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
        userService.updateUser(botUser, ChannelTariff.Tariff.NONE, ChannelTariff.ChosenPeriod.NONE, BotUser.DialogStatus.NONE);
        sender.send(userId, Text.CANCEL_PARTICIPATION_SUCCESS, Menu.of(Text.START_AGAIN));
    }

    private void handleBotDismissedDuringRegistration(BotUser botUser, Long userId) {
        remover.removeMsg(userId, botUser.getLastMessageIdToDelete());
        channelService.removeCancelledChannel(userId);
        userService.updateUser(botUser, ChannelTariff.Tariff.NONE, ChannelTariff.ChosenPeriod.NONE, BotUser.DialogStatus.NONE);
        sender.send(userId, Text.FAILED_PARTICIPATION_DUE_TO_BOT_ADMIN_RIGHTS, Menu.of(Text.START_AGAIN));
    }

    private void finishProcess(BotUser botUser, Long userId) {
        BotUser.Role role = botUser.getRole();
        if (role != BotUser.Role.ADMIN) role = BotUser.Role.USER;
        String msg = "";
        if (botUser.getChosenTariff() != ChannelTariff.Tariff.TARIFF_3) {
            msg = Text.SUBSCRIPTION_DESC;
        }
        channelService.setPeriod(userId, botUser.getChosenPeriod());
        userService.updateUser(botUser, role, ChannelTariff.Tariff.NONE, ChannelTariff.ChosenPeriod.NONE, BotUser.DialogStatus.NONE);
        sender.send(userId, Text.SUCCESS, Menu.of(Text.START_AGAIN, Text.MY_SUBSCRIPTIONS));
        informAboutSubscription(userId, msg);
    }

    private void informAboutSubscription(Long userId, String text) {
        if (!text.isBlank()) sender.send(userId, text);
    }

    private void informAboutError(BotUser botUser, Long userId) {
        switch (botUser.getDialogStatus()) {
            case WAITING_ADD_BOT_AS_ADMIN -> sender.send(userId, Text.ERR_ADD_BOT, Menu.of(Text.CANCEL_PARTICIPATION));
            case WAITING_PAYMENT_PERIOD ->
                    sender.send(userId, Text.ERR_PAY_PERIOD, Menu.of(Text.PAY_1_MONTH, Text.PAY_1_YEAR, Text.CANCEL_PARTICIPATION));
            case WAITING_PAYMENT_CONFIRMATION ->
                    sender.send(userId, Text.ERR_PAYMENT, Menu.of(Text.PAYMENT_CONFIRM, Text.CANCEL_PARTICIPATION));
            case null, default -> sender.send(userId, Text.UNKNOWN_ERR);
        }
    }
}