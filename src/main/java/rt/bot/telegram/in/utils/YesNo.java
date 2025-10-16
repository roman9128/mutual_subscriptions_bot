package rt.bot.telegram.in.utils;

import lombok.experimental.UtilityClass;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberAdministrator;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberLeft;
import rt.bot.constant.Text;
import rt.bot.entity.BotUser;
import rt.bot.entity.ChannelTariff;

@UtilityClass
public class YesNo {

    public static boolean isItValidUpdate(Update update) {
        return (TelegramUtils.extractUserFromUpdate(update) != null &&
                TelegramUtils.extractUserIdFromUpdate(update) != null);
    }

    public static boolean isBotAddedAsAdmin(Update update, BotUser botUser) {
        return isBotAddedAsAdmin(update) &&
                botUser.getDialogStatus() == BotUser.DialogStatus.WAITING_ADD_BOT_AS_ADMIN;
    }

    public static boolean isBotAddedAsAdmin(Update update) {
        return update.hasMyChatMember() &&
                update.getMyChatMember().getNewChatMember() instanceof ChatMemberAdministrator;
    }

    public static boolean isBotDismissedDuringRegistration(Update update, BotUser botUser) {
        return isBotDismissed(update) &&
                botUser.getDialogStatus() != BotUser.DialogStatus.NONE;
    }

    public static boolean isBotDismissed(Update update) {
        return update.hasMyChatMember() &&
                update.getMyChatMember().getNewChatMember() instanceof ChatMemberLeft;
    }

    public static boolean isItStartTariffRequest(BotUser botUser, String userMsg) {
        return botUser.getDialogStatus() == BotUser.DialogStatus.NONE &&
                botUser.getChosenTariff() == ChannelTariff.Tariff.NONE &&
                userMsg.startsWith(Text.TARIFF_START);
    }

    public static boolean isItStartVipRequest(BotUser botUser, String userMsg) {
        if (botUser.getRole() != BotUser.Role.ADMIN) return false;
        return (botUser.getDialogStatus() == BotUser.DialogStatus.NONE &&
                botUser.getChosenTariff() == ChannelTariff.Tariff.NONE &&
                userMsg.equals(Text.START_VIP) ||
                botUser.getDialogStatus() == BotUser.DialogStatus.WAITING_CHANNEL_LINK);
    }

    public static boolean isSubscriptionConfirmed(BotUser botUser, String userMsg) {
        return botUser.getDialogStatus() == BotUser.DialogStatus.WAITING_SUBSCRIPTION_CONFIRMATION &&
                userMsg.equals(Text.SUBSCRIPTION_CONFIRMATION);
    }

    public static boolean isPaymentConfirmed(BotUser botUser, String userMsg) {
        if (botUser.getDialogStatus() != BotUser.DialogStatus.WAITING_PAYMENT_CONFIRMATION) return false;
        return userMsg.equals(Text.PAYMENT_CONFIRM);
    }

    public static boolean isItCancel(String userMsg) {
        return userMsg.equals(Text.CANCEL_PARTICIPATION);
    }
}
