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
    @Query("""
            DELETE FROM Channel c
            WHERE c.owner.userId = :userId
              AND NOT EXISTS (
                  SELECT 1 FROM ChannelTariff ct
                  WHERE ct.channel = c
                    AND ct.startAt IS NOT NULL
              )
            """)
    void deleteUserChannelsWithoutActiveTariffs(@Param("userId") Long userId);


    @Query("""
            SELECT c FROM Channel c
            WHERE (
                SELECT COALESCE(SUM(ct.subscriptionAmountGoal), 0)
                FROM ChannelTariff ct
                WHERE ct.channel = c
                  AND ct.startAt <= :now
                  AND ct.endAt >= :now
            ) > (
                SELECT COUNT(s)
                FROM Subscription s
                WHERE s.channel = c
                  AND s.status = 'FOLLOWED'
            )
            AND c.owner <> :user
            AND c NOT IN (
                SELECT s.channel FROM Subscription s WHERE s.user = :user
            )
            """)
    List<Channel> findChannelsForSubscriptionNotOwnedAndNotSubscribedBy(
            @Param("user") BotUser user,
            @Param("now") LocalDateTime now
    );
}
