package rt.bot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import rt.bot.entity.BotUser;
import rt.bot.entity.Tariff;
import rt.bot.repo.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public void setUserRole(Long userId, BotUser.Role role) {
        BotUser botUser = userRepository.findById(userId).orElse(null);
        if (botUser != null) {
            botUser.setRole(role);
            userRepository.save(botUser);
            log.info("Изменил роль пользователя с id {} на {}", userId, role);
        } else {
            log.error("Не удалось найти пользователя в базе данных по id {} и изменить его роль на {}", userId, role);
        }
    }

    public BotUser getBotUserById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    public void setLastMessageIdToDelete(BotUser botUser, int messageId) {
        botUser.setLastMessageIdToDelete(messageId);
        userRepository.save(botUser);
        log.info("Сохранил id сообщения для удаления, отправленного пользователю с id {}", botUser.getUserId());
    }

    public void setDialogStatus(BotUser botUser, BotUser.DialogStatus dialogStatus) {
        botUser.setDialogStatus(dialogStatus);
        userRepository.save(botUser);
        log.info("Изменил статус пользователя с id {} на {}", botUser.getUserId(), dialogStatus);
    }

    public void setTariff(BotUser botUser, Tariff tariff) {
        botUser.setTariff(tariff);
        userRepository.save(botUser);
        log.info("Пользователю с id {} выбрал тариф {}", botUser.getUserId(), tariff);
    }

    public void setTariffAndStatus(BotUser botUser, Tariff tariff, BotUser.DialogStatus dialogStatus) {
        botUser.setTariff(tariff);
        botUser.setDialogStatus(dialogStatus);
        userRepository.save(botUser);
        log.info("Изменил пользователю с id {} тариф на {} и статус на {}", botUser.getUserId(), tariff, dialogStatus);
    }
}