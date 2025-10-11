package rt.bot.cases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import rt.bot.entity.BotUser;
import rt.bot.telegram.TelegramUtils;
import rt.bot.telegram.client.MessageSender;
import rt.bot.telegram.constants.Menu;
import rt.bot.telegram.constants.Text;

@Slf4j
@Component
@RequiredArgsConstructor
public class InformationCase implements Case {

    private final MessageSender sender;

    @Override
    public void process(Update update, BotUser botUser) {
        Long userId = botUser.getUserId();
        String userMsg = TelegramUtils.extractUserTextFromUpdate(update);
        switch (userMsg) {
            case Text.START -> sendGreetings(userId);
            case Text.TARIFF_1_REQUEST -> sendTariffInfo(userId, Text.TARIFF_1_INFO, Text.TARIFF_1_START);
            case Text.TARIFF_2_REQUEST -> sendTariffInfo(userId, Text.TARIFF_2_INFO, Text.TARIFF_2_START);
            case Text.TARIFF_3_REQUEST -> sendTariffInfo(userId, Text.TARIFF_3_INFO, Text.TARIFF_3_START);
            case null, default -> sendInfo(userId);
        }
    }

    private void sendGreetings(Long userId) {
        sender.send(userId, Text.GREET, Menu.getTariffMenu(), "MarkdownV2");
    }

    private void sendTariffInfo(Long userId, String tariffInfo, String tariffStart) {
        sender.send(userId, tariffInfo, Menu.of(tariffStart, Text.LEARN_ANOTHER_TARIFF_REQUEST));
    }

    private void sendInfo(Long userId) {
        sender.send(userId, Text.LEARN_ANOTHER_TARIFF, Menu.getTariffMenu());
    }
}
