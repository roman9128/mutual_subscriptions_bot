package rt.bot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rt.bot.dto.ChannelStatus;
import rt.bot.entity.BotUser;
import rt.bot.entity.Subscription;

import java.util.List;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    long countByUserAndStatus(BotUser user, Subscription.Status status);

    @Query("""
                SELECT new rt.bot.dto.ChannelStatus(
                    s.channel.username,
                    s.status
                )
                FROM Subscription s
                WHERE s.user.userId = :userId
            """)
    List<ChannelStatus> findChannelStatusBy(@Param("userId") Long userId);
}