package rt.bot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import rt.bot.entity.BotUser;
import rt.bot.entity.Channel;
import rt.bot.repo.ChannelRepository;
import rt.bot.repo.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChannelService {

    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;

    @Transactional
    public void addBotAsAdminToChannel(User user, Chat chat) {
        Long channelId = chat.getId();
        if (channelRepository.existsById(channelId)) {
            channelRepository.updateBotIsAdmin(channelId, true);
            log.info("Бот назначен админом в канале с id {}", channelId);
            return;
        }
        BotUser botUser = userRepository.findById(user.getId()).orElse(null);
        if (botUser == null) {
            log.error("Пользователь с id {} не найден в базе данных", user.getId());
            return;
        }

        Channel channel = new Channel();
        channel.setChannelId(chat.getId());
        channel.setTitle(chat.getTitle());
        channel.setUsername(chat.getUserName());
        channel.setOwner(botUser);
        channel.setBotIsAdmin(true);
        channel.setTariff(botUser.getTariff());
        channel.setSubscriptionsAmountGoal(botUser.getTariff().getSubscriptionAmountGoal());

        channelRepository.save(channel);
        log.info("В базу данных добавлен новый канал с id {} и бот назначен админом в нём", chat.getId());
    }
}