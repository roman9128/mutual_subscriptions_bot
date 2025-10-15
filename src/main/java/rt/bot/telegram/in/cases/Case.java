package rt.bot.telegram.in.cases;

import org.telegram.telegrambots.meta.api.objects.Update;
import rt.bot.entity.BotUser;

public interface Case {
    void process(Update update, BotUser botUser);
}
