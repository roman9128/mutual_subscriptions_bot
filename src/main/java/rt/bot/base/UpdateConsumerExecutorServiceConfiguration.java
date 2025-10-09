package rt.bot.base;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;

@Configuration
@RequiredArgsConstructor
public class UpdateConsumerExecutorServiceConfiguration {

    private final UpdateConsumerMultiThreadingConfiguration updateConsumerMultiThreadingConfiguration;

    @Bean
    public ExecutorService updateConsumerExecutorService() {
        return new CachedFixThreadPoolExecutor(
                updateConsumerMultiThreadingConfiguration.getCorePoolSize(),
                updateConsumerMultiThreadingConfiguration.getMaxPoolSize(),
                updateConsumerMultiThreadingConfiguration.getKeepAliveTime(),
                updateConsumerMultiThreadingConfiguration.getQueueCapacity()
        );
    }
}