package rt.bot.telegram.in.cases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import rt.bot.entity.BotUser;
import rt.bot.telegram.handler.ChannelRegularHandler;
import rt.bot.telegram.handler.ChannelVipHandler;
import rt.bot.telegram.in.utils.TelegramUtils;
import rt.bot.telegram.in.utils.YesNo;

@Slf4j
@Component
@RequiredArgsConstructor
public class AddChannelCase implements Case {

    private final ChannelVipHandler channelVipHandler;
    private final ChannelRegularHandler channelRegularHandler;

    @Override
    public void process(Update update, BotUser botUser) {
        Long userId = TelegramUtils.extractUserIdFromUpdate(update);
        String userMsg = TelegramUtils.extractUserTextFromUpdate(update);

        if (YesNo.isItStartVipRequest(botUser, userMsg)) {
            channelVipHandler.process(botUser, userId, userMsg);
        } else {
            channelRegularHandler.process(update, botUser, userId, userMsg);
        }
    }
}