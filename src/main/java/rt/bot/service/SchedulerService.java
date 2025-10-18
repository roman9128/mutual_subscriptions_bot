package rt.bot.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulerService {

    private final SubscriptionService subscriptionService;

    @PostConstruct
    public void initialCheckUp() {
        log.info("Запущен планировщик");
    }

    @Scheduled(cron = "0 00 10 * * ?", zone = "Europe/Moscow")
    public void sendChannelsToSubscribe() {
        log.info("Запущена задача отправки каналов для подписки");
        subscriptionService.sendChannelsToSubscribe();
        log.info("Завершена задача отправки каналов для подписки");
    }
}