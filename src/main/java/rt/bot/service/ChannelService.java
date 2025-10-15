package rt.bot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import rt.bot.entity.BotUser;
import rt.bot.entity.Channel;
import rt.bot.repository.ChannelRepository;
import rt.bot.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChannelService {

    private final ChannelRepository channelRepository;

    @Transactional
    public boolean toggleBotAdminRights(Chat chat, boolean botIsAdmin) {
        Long channelId = chat.getId();
        if (channelRepository.existsById(channelId)) {
            channelRepository.updateBotIsAdmin(channelId, botIsAdmin);
            log.info("Изменён статус бота в канале с id {}. Бот админ: {}", channelId, botIsAdmin);
            return true;
        } else {
            log.error("Не найден канал с id {} для изменения статуса бота", channelId);
            return false;
        }
    }

    public void createNewChannel(BotUser botUser, Chat chat) {
        Channel channel = new Channel();
        channel.setChannelId(chat.getId());
        channel.setTitle(chat.getTitle());
        channel.setUsername(chat.getUserName());
        channel.setOwner(botUser);
        channel.setBotIsAdmin(true);
        channel.setTariff(botUser.getTariff());
        channel.addSubscriptionAmountGoal(botUser.getTariff().getSubscriptionAmountGoal());

        channelRepository.save(channel);
        log.info("В базу данных добавлен канал {} и бот назначен админом в нём", chat.getTitle());
    }

    public void createNewVipChannel(BotUser botUser, Chat chat) {
        Channel channel = new Channel();
        channel.setChannelId(chat.getId());
        channel.setTitle(chat.getTitle());
        channel.setUsername(chat.getUserName());
        channel.setOwner(botUser);
        channel.setTariff(botUser.getTariff());
        channel.setPaidSince(LocalDateTime.now(ZoneId.of("Europe/Moscow")));
        channel.addSubscriptionAmountGoal(botUser.getTariff().getSubscriptionAmountGoal());

        channelRepository.save(channel);
        log.info("В базу данных добавлен VIP канал {}", chat.getTitle());
    }

    @Transactional
    public void setPaidSince(Long userId) {
        channelRepository.updatePaidSinceForUserChannels(userId, LocalDateTime.now(ZoneId.of("Europe/Moscow")));
    }

    @Transactional
    public void removeCancelledChannel(Long userId) {
        channelRepository.deleteByOwnerUserIdAndPaidSinceIsNull(userId);
    }

    public boolean channelExists(Long channelId) {
        boolean result = channelRepository.existsById(channelId);
        log.info("Канал с id {} существует: {}", channelId, result);
        return result;
    }
}