package rt.bot.telegram.cases;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberAdministrator;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import rt.bot.base.BotInfo;
import rt.bot.entity.BotUser;
import rt.bot.entity.Tariff;
import rt.bot.service.UserService;
import rt.bot.telegram.TelegramUtils;
import rt.bot.telegram.client.MessageRemover;
import rt.bot.telegram.client.MessageSender;
import rt.bot.telegram.constants.Menu;
import rt.bot.telegram.constants.Text;
import rt.bot.telegram.handlers.BotAdminHandler;
import rt.bot.telegram.handlers.SubscriptionsHandler;

@Component
@RequiredArgsConstructor
public class TariffCase implements Case {

    private final BotInfo botInfo;
    private final MessageSender sender;
    private final MessageRemover remover;
    private final UserService userService;
    private final BotAdminHandler botAdminHandler;
    private final SubscriptionsHandler subscriptionsHandler;

    @Override
    public void process(Update update, BotUser botUser) {
        Long userId = TelegramUtils.extractUserIdFromUpdate(update);
        String userMsg = TelegramUtils.extractUserTextFromUpdate(update);

        if (isStartTariffRequest(botUser, userMsg)) {
            startTariff(botUser, userId, userMsg);
        } else if (botIsAddedAsAdmin(update)) {
            handleBotAssignment(update, botUser);
        } else if (isPaymentConfirmation(botUser, userMsg)) {
            checkConfirmation(botUser, userId, userMsg);
        } else {
            informAboutError(botUser, userId);
        }
    }

    private void startTariff(BotUser botUser, Long userId, String userMsg) {
        Tariff tariff = switch (userMsg) {
            case Text.TARIFF_1_START -> Tariff.TARIFF_1;
            case Text.TARIFF_2_START -> Tariff.TARIFF_2;
            case Text.TARIFF_3_START -> Tariff.TARIFF_3;
            default -> Tariff.TARIFF_1;
        };
        userService.setTariffAndStatus(botUser, tariff, BotUser.DialogStatus.WAITING_ADD_BOT_AS_ADMIN);
        sender.send(userId, Text.TARIFF_SELECTED, Menu.removeReplyKeyboard());
        requestAddBotAsAdmin(botUser, userId);
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

    private void handleBotAssignment(Update update, BotUser botUser) {
        botAdminHandler.process(update);
        informUserAboutSuccessfulBotAssignment(update, botUser);
        sendChannelsToSubscribe(botUser);
    }

    private void informUserAboutSuccessfulBotAssignment(Update update, BotUser botUser) {
        remover.removeMsg(botUser.getUserId(), botUser.getLastMessageIdToDelete());
        sender.send(botUser.getUserId(), Text.CHANNEL_ADDED.formatted(update.getMyChatMember().getChat().getTitle()));
    }

    private void sendChannelsToSubscribe(BotUser botUser) {
        userService.setDialogStatus(botUser, BotUser.DialogStatus.WAITING_SUBSCRIPTION_CONFIRMATION);
        String channelsList = subscriptionsHandler.getChannelsToSubscribe(botUser);
        sender.send(botUser.getUserId(), channelsList);
        // добавить кнопку подтверждения подписки, после неё запрос оплаты или полная отмена
    }

    private void askPayment(BotUser botUser, Long userId, Tariff tariff) {
        if (tariff != Tariff.TARIFF_1) {
            //todo payment service
            String link = "";
            userService.setDialogStatus(botUser, BotUser.DialogStatus.WAITING_PAYMENT_CONFIRMATION);
            sender.send(
                    userId,
                    Text.PAYMENT_LINK.formatted(link),
                    Menu.of(Text.PAYMENT_CONFIRM, Text.PAYMENT_CANCEL));
        } else {
            askLink(botUser, userId);
        }
    }

    private void checkConfirmation(BotUser botUser, Long userId, String userMsg) {
        if (userMsg.equals(Text.PAYMENT_CONFIRM)) {
            // todo payment service check payment
            askLink(botUser, userId);
        } else if (userMsg.equals(Text.PAYMENT_CANCEL)) {
            userService.setTariffAndStatus(botUser, Tariff.NONE, BotUser.DialogStatus.NONE);
            sender.send(userId, Text.PAYMENT_CANCEL_SUCCESS);
            sender.send(userId, Text.GREET, Menu.getTariffMenu(), "MarkdownV2");
        }
    }

    private void askLink(BotUser botUser, Long userId) {
        userService.setDialogStatus(botUser, BotUser.DialogStatus.WAITING_ADD_BOT_AS_ADMIN);
        sender.send(userId, Text.ADD_BOT_AS_ADMIN_DESC, Menu.removeReplyKeyboard());
        sender.send(userId, Text.LINK_REQUEST);
    }

//    private void addChannel(BotUser botUser, Long userId, String userMsg) {
//        if (userMsg.length() <= Text.LINK_BASE.length()) {
//            informAboutError(botUser, userId);
//            return;
//        }
//        boolean channelIsAdded = channelService.addNewChannel(botUser, userMsg);
//        if (channelIsAdded) {
//            userService.setTariffAndStatus(botUser);
//            sender.send(userId, Text.SUCCESS);
//        } else {
//            sender.send(userId, Text.CHANNEL_ADDED_EARLY);
//            informAboutError(botUser, userId);
//        }
//    }

    private void informAboutError(BotUser botUser, Long userId) {
        switch (botUser.getDialogStatus()) {
            case WAITING_ADD_BOT_AS_ADMIN -> sender.send(userId, Text.LINK_REQUEST);
            case WAITING_PAYMENT_CONFIRMATION -> askPayment(botUser, userId, botUser.getTariff());
        }
    }


    private boolean botIsAddedAsAdmin(Update update) {
        return update.hasMyChatMember() &&
                update.getMyChatMember().getNewChatMember() instanceof ChatMemberAdministrator;
    }

    private boolean isStartTariffRequest(BotUser botUser, String userMsg) {
        return botUser.getDialogStatus() == BotUser.DialogStatus.NONE &&
                botUser.getTariff() == Tariff.NONE &&
                userMsg.startsWith(Text.TARIFF_START);
    }

    private boolean isPaymentConfirmation(BotUser botUser, String userMsg) {
        if (botUser.getDialogStatus() != BotUser.DialogStatus.WAITING_PAYMENT_CONFIRMATION) return false;
        return userMsg.equals(Text.PAYMENT_CONFIRM) || userMsg.equals(Text.PAYMENT_CANCEL);
    }
}
