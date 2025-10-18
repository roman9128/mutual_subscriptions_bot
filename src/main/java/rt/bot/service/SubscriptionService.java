package rt.bot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import rt.bot.constant.Num;
import rt.bot.constant.Text;
import rt.bot.dto.BotUserChannelsSet;
import rt.bot.dto.ChannelStatus;
import rt.bot.dto.ChannelStatusLists;
import rt.bot.entity.BotUser;
import rt.bot.entity.Channel;
import rt.bot.entity.Subscription;
import rt.bot.repository.SubscriptionRepository;
import rt.bot.repository.UserRepository;
import rt.bot.telegram.out.MessageSender;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final MessageSender sender;

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

    public void sendChannelsToSubscribe() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Europe/Moscow"));
        List<BotUserChannelsSet> usersAndChannelsToSubscribe = userRepository.findUsersWithMissingSubscriptions(now);
        if (usersAndChannelsToSubscribe == null || usersAndChannelsToSubscribe.isEmpty()) return;

        Map<BotUser, Set<Channel>> usersAndLimitedCountOfChannelsToSubscribe = usersAndChannelsToSubscribe
                .stream()
                .collect(Collectors.toMap(
                        BotUserChannelsSet::botUser,
                        bucs -> bucs.channels()
                                .stream()
                                .limit(Num.SENDING_LIMIT)
                                .collect(Collectors.toSet())));
        createSubscriptions(usersAndLimitedCountOfChannelsToSubscribe);

        Map<Long, String> sendingMap = usersAndLimitedCountOfChannelsToSubscribe.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().getUserId(),
                        e -> prepareChannelListMsg(e.getValue())
                ));
        sender.send(sendingMap);
    }

    private void createSubscriptions(Map<BotUser, Set<Channel>> map) {
        List<Subscription> subscriptions = map.entrySet()
                .stream()
                .flatMap(e -> e.getValue()
                        .stream()
                        .map(c -> getSubscription(e.getKey(), c)))
                .toList();
        subscriptionRepository.saveAll(subscriptions);
        log.info("Сохранил в базе данных {} новых подписок", subscriptions.size());
    }

    private Subscription getSubscription(BotUser botUser, Channel channel) {
        Subscription s = new Subscription();
        s.setUser(botUser);
        s.setChannel(channel);
        s.setStatus(Subscription.Status.UNFOLLOWED);
        return s;
    }

    private String prepareChannelListMsg(Set<Channel> channels) {
        return Text.SUBSCRIPTION_REQUIRED.formatted(channels.stream()
                .map(c -> "@" + c.getUsername())
                .collect(Collectors.joining(System.lineSeparator())));
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
