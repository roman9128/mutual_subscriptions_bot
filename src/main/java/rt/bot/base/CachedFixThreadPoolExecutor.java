package rt.bot.base;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CachedFixThreadPoolExecutor extends ThreadPoolExecutor {
    public CachedFixThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, int queueCapacity) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS, new LinkedBlockingQueue<>(queueCapacity));
    }
}