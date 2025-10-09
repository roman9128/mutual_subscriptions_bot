package rt.bot.base;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "application.multi-threading.update-consumer")
public class UpdateConsumerMultiThreadingConfiguration {
    private int corePoolSize;
    private int maxPoolSize;
    private long keepAliveTime;
    private int queueCapacity;

    @Override
    public String toString() {
        return "UpdateConsumerMultiThreadingConfiguration:" + System.lineSeparator() +
                "corePoolSize: " + corePoolSize + System.lineSeparator() +
                "maxPoolSize: " + maxPoolSize + System.lineSeparator() +
                "keepAliveTime: " + keepAliveTime + System.lineSeparator() +
                "queueCapacity: " + queueCapacity + System.lineSeparator();
    }
}