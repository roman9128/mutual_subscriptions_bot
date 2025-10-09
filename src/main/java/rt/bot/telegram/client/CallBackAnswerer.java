package rt.bot.telegram.client;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class CallBackAnswerer {
    private final TelegramClient telegramClient;

    public void answerCallback(Update update) {
        final AnswerCallbackQuery answer = AnswerCallbackQuery.builder()
                .callbackQueryId(update.getCallbackQuery().getId())
                .showAlert(false)
                .build();
        try {
            telegramClient.execute(answer);
        } catch (Exception e) {
            log.warn("Could not send answerCallBackQuery to update: {}", e.getMessage());
        }
    }
}
