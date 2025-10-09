package rt.bot.telegram.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import rt.bot.entity.BotUser;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionsHandler {

    public String getChannelsToSubscribe(BotUser botUser){
        return " tut budiet spisok";
    }
}
