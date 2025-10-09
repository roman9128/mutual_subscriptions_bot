package rt.bot.telegram;

import lombok.experimental.UtilityClass;
import org.telegram.telegrambots.meta.api.objects.Update;
import rt.bot.entity.BotUser;
import rt.bot.telegram.constants.Text;

@UtilityClass
public class UpdateClassifier {

    public static UpdateClass classify(Update update, BotUser botUser) {
        if (botUser == null) return UpdateClass.NONE;
        if (isTariffProcess(update, botUser)) return UpdateClass.TARIFF_PROCESS;
        else return UpdateClass.INFO_REQUEST;
    }

    private static boolean isTariffProcess(Update update, BotUser botUser) {
        String userMsg = TelegramUtils.extractUserTextFromUpdate(update);
        return userMsg.equals(Text.TARIFF_1_START) ||
                userMsg.equals(Text.TARIFF_2_START) ||
                userMsg.equals(Text.TARIFF_3_START) ||
                botUser.getDialogStatus() != BotUser.DialogStatus.NONE;
    }
}