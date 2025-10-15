package rt.bot.constant;

import lombok.experimental.UtilityClass;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class Menu {

    public static InlineKeyboardMarkup getButtonWithLink(String name, String link) {
        return InlineKeyboardMarkup.builder()
                .keyboard(new ArrayList<>(List.of(
                        new InlineKeyboardRow(List.of(
                                InlineKeyboardButton.builder()
                                        .text(name)
                                        .url(link)
                                        .build()))
                ))).build();
    }

    public static InlineKeyboardMarkup buttonFor(String text) {
        return InlineKeyboardMarkup.builder()
                .keyboard(new ArrayList<>(List.of(
                        new InlineKeyboardRow(List.of(
                                InlineKeyboardButton.builder()
                                        .text(text)
                                        .callbackData(text)
                                        .build()))
                ))).build();
    }

    public static InlineKeyboardMarkup combineMenus(InlineKeyboardMarkup... keyboards) {
        List<InlineKeyboardRow> rows = new ArrayList<>();
        for (InlineKeyboardMarkup keyboard : keyboards) {
            rows.addAll(keyboard.getKeyboard());
        }
        return new InlineKeyboardMarkup(rows);
    }

    public static ReplyKeyboardMarkup getTariffMenu() {
        KeyboardRow row1 = new KeyboardRow(new KeyboardButton(Text.TARIFF_1_REQUEST));
        KeyboardRow row2 = new KeyboardRow(new KeyboardButton(Text.TARIFF_2_REQUEST));
        KeyboardRow row3 = new KeyboardRow(new KeyboardButton(Text.TARIFF_3_REQUEST));
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup(List.of(row1, row2, row3));
        keyboard.setResizeKeyboard(true);
        return keyboard;
    }

    public static ReplyKeyboardMarkup of(String... args) {
        List<KeyboardRow> rows = new ArrayList<>();
        for (String arg : args) {
            rows.add(new KeyboardRow(new KeyboardButton(arg)));
        }
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup(rows);
        keyboard.setResizeKeyboard(true);
        return keyboard;
    }

    public static ReplyKeyboardRemove removeReplyKeyboard() {
        return ReplyKeyboardRemove.builder()
                .removeKeyboard(true)
                .build();
    }
}