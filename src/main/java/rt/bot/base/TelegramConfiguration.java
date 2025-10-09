package rt.bot.base;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Getter
@Configuration
public class TelegramConfiguration {
    @Value("${application.telegram.token}")
    private String token;

    @Bean
    public TelegramClient telegramClient() {
        return new OkHttpTelegramClient(token);
    }
}