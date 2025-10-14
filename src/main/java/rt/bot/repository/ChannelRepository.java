package rt.bot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import rt.bot.entity.BotUser;
import rt.bot.entity.Channel;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChannelRepository extends JpaRepository<Channel, Long> {

    @Modifying
    @Query("UPDATE Channel c SET c.botIsAdmin = :botIsAdmin WHERE c.channelId = :channelId")
    void updateBotIsAdmin(@Param("channelId") Long channelId, @Param("botIsAdmin") boolean botIsAdmin);

    @Modifying
    @Query("UPDATE Channel c SET c.paidSince = :paidSince WHERE c.owner.userId = :userId AND c.paidSince IS NULL")
    void updatePaidSinceForUserChannels(@Param("userId") Long userId, @Param("paidSince") LocalDateTime paidSince);

    @Modifying
    @Query("DELETE FROM Channel c WHERE c.owner.userId = :userId AND c.paidSince IS NULL")
    void deleteByOwnerUserIdAndPaidSinceIsNull(@Param("userId") Long userId);

    @Query("SELECT c FROM Channel c " +
            "WHERE c.subscriptionsAmountGoal > (" +
            "    SELECT COUNT(s) FROM Subscription s " +
            "    WHERE s.channel = c AND s.status = 'FOLLOWED'" +
            ") " +
            "AND c.owner != :user " +
            "AND c NOT IN (" +
            "    SELECT s.channel FROM Subscription s " +
            "    WHERE s.user = :user" +
            ")")
    List<Channel> findChannelsForSubscriptionNotOwnedAndNotSubscribedBy(@Param("user") BotUser user);

    List<Channel> findByOwner(BotUser owner);
}
