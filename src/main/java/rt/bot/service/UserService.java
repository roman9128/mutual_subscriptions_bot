package rt.bot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import rt.bot.entity.BotUser;
import rt.bot.entity.ChannelTariff;
import rt.bot.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public void setUserRole(BotUser botUser, BotUser.Role role) {
        botUser.setRole(role);
        userRepository.save(botUser);
        log.info("Изменил роль пользователя с id {} на {}", botUser.getUserId(), role);
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

    public void setTariff(BotUser botUser, ChannelTariff.Tariff tariff) {
        botUser.setChosenTariff(tariff);
        userRepository.save(botUser);
        log.info("Пользователю с id {} выбрал тариф {}", botUser.getUserId(), tariff);
    }

    public void setTariffAndStatus(BotUser botUser, ChannelTariff.Tariff tariff, BotUser.DialogStatus dialogStatus) {
        botUser.setChosenTariff(tariff);
        botUser.setDialogStatus(dialogStatus);
        userRepository.save(botUser);
        log.info("Изменил пользователю с id {} тариф на {} и статус на {}", botUser.getUserId(), tariff, dialogStatus);
    }

    public void setRoleTariffStatus(BotUser botUser, BotUser.Role role, ChannelTariff.Tariff tariff, BotUser.DialogStatus dialogStatus) {
        botUser.setRole(role);
        botUser.setChosenTariff(tariff);
        botUser.setDialogStatus(dialogStatus);
        userRepository.save(botUser);
        log.info("Изменил пользователю с id {} роль на {}, тариф на {} и статус на {}", botUser.getUserId(), role, tariff, dialogStatus);
    }
}