package rt.bot.telegram.in;

import lombok.experimental.UtilityClass;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberAdministrator;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberLeft;
import rt.bot.entity.BotUser;
import rt.bot.constant.Text;

@UtilityClass
public class UpdateClassifier {

    public static UpdateClass classify(Update update, BotUser botUser) {
        if (botUser == null) return UpdateClass.NONE;
        if (isAddChannelProcess(update, botUser)) return UpdateClass.ADD_CHANNEL;
        else if (isAdminRightsProcess(update, botUser)) return UpdateClass.ADMIN_RIGHTS;
        else return UpdateClass.INFO_REQUEST;
    }

    private static boolean isAddChannelProcess(Update update, BotUser botUser) {
        String userMsg = TelegramUtils.extractUserTextFromUpdate(update);
        return userMsg.equals(Text.TARIFF_1_START) ||
                userMsg.equals(Text.TARIFF_2_START) ||
                userMsg.equals(Text.TARIFF_3_START) ||
                userMsg.equals(Text.START_VIP) ||
                botUser.getDialogStatus() != BotUser.DialogStatus.NONE;
    }

    private static boolean isAdminRightsProcess(Update update, BotUser botUser) {
        if (update.getMyChatMember() == null) return false;
        return botUser.getDialogStatus() == BotUser.DialogStatus.NONE &&
                (update.getMyChatMember().getNewChatMember() instanceof ChatMemberAdministrator ||
                        update.getMyChatMember().getNewChatMember() instanceof ChatMemberLeft);
    }
}