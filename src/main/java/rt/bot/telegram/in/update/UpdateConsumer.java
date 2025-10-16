package rt.bot.telegram.in.update;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.concurrent.ExecutorService;

@Component
@RequiredArgsConstructor
public class UpdateConsumer implements LongPollingUpdateConsumer {
    private final ExecutorService updateConsumerExecutorService;
    private final UpdateProcessor updateProcessor;

    @Override
    public void consume(List<Update> list) {
        list.forEach(update ->
                updateConsumerExecutorService.execute(() ->
                        consumeThis(update)));
    }

    private void consumeThis(Update update) {
        updateProcessor.process(update);
    }
}