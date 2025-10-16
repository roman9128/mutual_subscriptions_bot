package rt.bot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import rt.bot.constant.Num;
import rt.bot.dto.ChannelStatus;
import rt.bot.dto.ChannelStatusLists;
import rt.bot.entity.BotUser;
import rt.bot.entity.Channel;
import rt.bot.entity.Subscription;
import rt.bot.repository.ChannelRepository;
import rt.bot.repository.SubscriptionRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final ChannelRepository channelRepository;
    private final SubscriptionRepository subscriptionRepository;

    public ChannelStatusLists getChannelStatusList(Long userId) {
        List<ChannelStatus> all = subscriptionRepository.findChannelStatusBy(userId);
        List<ChannelStatus> followed = new ArrayList<>();
        List<ChannelStatus> unfollowed = new ArrayList<>();

        for (ChannelStatus dto : all) {
            if (dto.status() == Subscription.Status.FOLLOWED) {
                followed.add(dto);
            } else {
                unfollowed.add(dto);
            }
        }
        return new ChannelStatusLists(followed, unfollowed);
    }

    public String getChannelListToSubscribe(BotUser botUser) {
        List<Channel> channelsToSend = channelRepository
                .findChannelsForSubscriptionNotOwnedAndNotSubscribedBy(botUser, LocalDateTime.now(ZoneId.of("Europe/Moscow")))
                .stream()
                .limit(Num.SENDING_LIMIT)
                .toList();
        createSubscriptions(botUser, channelsToSend);
        return channelsToSend.stream()
                .map(c -> "@" + c.getUsername())
                .collect(Collectors.joining(System.lineSeparator()));
    }

    private void createSubscriptions(BotUser botUser, List<Channel> channelsToSend) {
        if (channelsToSend == null || channelsToSend.isEmpty()) return;
        List<Subscription> subscriptions = channelsToSend.stream()
                .map(c -> getSubscription(botUser, c))
                .toList();
        subscriptionRepository.saveAll(subscriptions);
        log.info("Сохранил в базе данных {} новых подписок для пользователя с id {}", subscriptions.size(), botUser.getUserId());
    }

    private Subscription getSubscription(BotUser botUser, Channel channel) {
        Subscription s = new Subscription();
        s.setUser(botUser);
        s.setChannel(channel);
        s.setStatus(Subscription.Status.UNFOLLOWED);
        return s;
    }

//    private long getSubscriptionsAmountToAdd(BotUser botUser) {
//        long subscriptionsAmountUserHas = subscriptionRepository.countByUserAndStatus(botUser, Subscription.Status.FOLLOWED);
//        long subscriptionsAmountUserMustHave = channelRepository.findByOwner(botUser).stream()
//                .map(Channel::getTariff)
//                .mapToLong(Tariff::getSubscriptionAmountInReturn)
//                .sum();
//        long amount = subscriptionsAmountUserMustHave - subscriptionsAmountUserHas;
//        return Math.max(0, amount);
//    }
}
