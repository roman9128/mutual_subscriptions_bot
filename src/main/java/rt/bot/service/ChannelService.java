package rt.bot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import rt.bot.constant.Num;
import rt.bot.entity.BotUser;
import rt.bot.entity.Channel;
import rt.bot.entity.ChannelTariff;
import rt.bot.repository.ChannelRepository;
import rt.bot.repository.ChannelTariffRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChannelService {

    private final ChannelRepository channelRepository;
    private final ChannelTariffRepository channelTariffRepository;

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

    @Transactional
    public void createNewChannel(BotUser botUser, Chat chat) {
        Channel channel = new Channel();
        channel.setChannelId(chat.getId());
        channel.setTitle(chat.getTitle());
        channel.setUsername(chat.getUserName());
        channel.setOwner(botUser);
        channel.setBotIsAdmin(true);

        ChannelTariff channelTariff = new ChannelTariff();
        channelTariff.setTariff(botUser.getChosenTariff());
        channelTariff.setChannel(channel);

        channel.getChannelTariffs().add(channelTariff);

        channelRepository.save(channel);
        log.info("В базу данных добавлен канал {} с тарифом {} и бот назначен админом в нём", channel.getTitle(), channelTariff.getTariff());
    }

    @Transactional
    public void createNewVipChannel(BotUser botUser, Chat chat) {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Europe/Moscow"));

        Channel channel = new Channel();
        channel.setChannelId(chat.getId());
        channel.setTitle(chat.getTitle());
        channel.setUsername(chat.getUserName());
        channel.setOwner(botUser);

        ChannelTariff channelTariff = new ChannelTariff();
        channelTariff.setTariff(botUser.getChosenTariff());
        channelTariff.setChannel(channel);
        channelTariff.setStartAt(now);
        channelTariff.setEndAt(now.plusYears(Num.VIP_LENGTH_YEARS));

        channel.getChannelTariffs().add(channelTariff);

        channelRepository.save(channel);
        log.info("В базу данных добавлен VIP канал {}", chat.getTitle());
    }

    @Transactional
    public void setStartAt(Long userId) {
        channelTariffRepository.setStartAtForUserTariffs(userId, LocalDateTime.now(ZoneId.of("Europe/Moscow")));
    }

    @Transactional
    public void removeCancelledChannel(Long userId) {
        channelRepository.deleteUserChannelsWithoutActiveTariffs(userId);
    }

    public boolean channelExists(Long channelId) {
        boolean result = channelRepository.existsById(channelId);
        log.info("Канал с id {} существует: {}", channelId, result);
        return result;
    }
}