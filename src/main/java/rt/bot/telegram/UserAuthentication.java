package rt.bot.telegram;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import rt.bot.entity.BotUser;
import rt.bot.entity.Tariff;
import rt.bot.repository.UserRepository;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserAuthentication {

    private final UserRepository userRepository;

    public BotUser authenticate(Update update) {
        if (!TelegramUtils.isValidUpdate(update)) return null;

        Long userId = TelegramUtils.extractUserIdFromUpdate(update);
        Optional<BotUser> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            User tgUserFromUpdate = TelegramUtils.extractUserFromUpdate(update);
            BotUser botUser = new BotUser();
            botUser.setUserId(userId);
            botUser.setTelegramFirstName(tgUserFromUpdate.getFirstName());
            botUser.setTelegramLastName(tgUserFromUpdate.getLastName());
            botUser.setTelegramUsername(tgUserFromUpdate.getUserName());
            botUser.setRole(BotUser.Role.GUEST);
            botUser.setDialogStatus(BotUser.DialogStatus.NONE);
            botUser.setTariff(Tariff.NONE);
            return userRepository.save(botUser);
        } else {
            return optionalUser.get();
        }
    }
}