package rt.bot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rt.bot.dto.BotUserChannelsSet;
import rt.bot.entity.BotUser;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<BotUser, Long> {

    @Query("""
                SELECT new rt.bot.dto.BotUserChannelsSet(
                    u,
                    CAST(COLLECT(DISTINCT c) AS java.util.Set)
                )
                FROM BotUser u
                JOIN Channel c 
                    ON c.owner.userId <> u.userId
                LEFT JOIN Subscription s 
                    ON s.user.userId = u.userId 
                    AND s.channel.channelId = c.channelId
                WHERE s.id IS NULL
                  AND (
                      SELECT COUNT(sub)
                      FROM Subscription sub
                      JOIN sub.channel ch2
                      WHERE sub.user.userId = u.userId
                        AND ch2.owner.userId <> u.userId
                  ) < (
                      SELECT COALESCE(SUM(t.subscriptionAmountInReturn), 0)
                      FROM ChannelTariff t
                      JOIN t.channel ch
                      WHERE ch.owner.userId = u.userId
                        AND t.startAt <= :now
                        AND t.endAt >= :now
                  )
                GROUP BY u
                ORDER BY u.userId
            """)
    List<BotUserChannelsSet> findUsersWithMissingSubscriptions(@Param("now") LocalDateTime now);
}