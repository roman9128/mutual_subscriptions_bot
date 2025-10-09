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

    @PostConstruct
    public void initialCheckUp() {
        log.info("Запущен планировщик");
    }

    @Scheduled(cron = "0 30 8 * * ?", zone = "Europe/Moscow")
    public void sendMorningPic() {
        log.info("Запущена задача отправки сообщений в 8.30 утра");
        log.info("Завершена задача отправки сообщений в 8.30 утра");
    }
}