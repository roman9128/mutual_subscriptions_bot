package rt.bot.telegram.in.cases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import rt.bot.entity.BotUser;
import rt.bot.telegram.handler.BotAdminHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminRightsCase implements Case {

    private final BotAdminHandler botAdminHandler;

    @Override
    public void process(Update update, BotUser botUser) {
        botAdminHandler.process(update);
    }
}