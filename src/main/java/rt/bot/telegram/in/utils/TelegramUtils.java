package rt.bot.telegram.in.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import rt.bot.constant.Text;

@Slf4j
@UtilityClass
public class TelegramUtils {

    public static User extractUserFromUpdate(Update update) {
        if (update.hasMessage()) return update.getMessage().getFrom();
        else if (update.hasCallbackQuery()) return update.getCallbackQuery().getFrom();
        else if (update.hasMyChatMember()) return update.getMyChatMember().getFrom();
        else return null;
    }

    public static Long extractUserIdFromUpdate(Update update) {
        if (update.hasMessage()) return update.getMessage().getFrom().getId();
        else if (update.hasCallbackQuery()) return update.getCallbackQuery().getFrom().getId();
        else if (update.hasMyChatMember()) return update.getMyChatMember().getFrom().getId();
        else {
            log.error("Не удалось определить id пользователя из обновления");
            return null;
        }
    }

    public static String extractUserTextFromUpdate(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            return update.getMessage().getText();
        } else if (update.hasMessage() && update.getMessage().hasCaption()) {
            return update.getMessage().getCaption();
        } else if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getData();
        } else {
            return "";
        }
    }

    public static String getChatNameFromLink(String link) {
        return "@" + link.substring(Text.LINK_BASE.length());
    }
}