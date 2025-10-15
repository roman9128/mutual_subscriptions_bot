package rt.bot.telegram.in.cases;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import rt.bot.entity.BotUser;

@Slf4j
@Component
public class NoneCase implements Case {

    @Override
    public void process(Update update, BotUser botUser) {
        // bot user is null
        log.warn("Отсутствует поддерживаемое содержание в обновлении: {}", update);
    }
}