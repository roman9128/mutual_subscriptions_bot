package rt.bot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rt.bot.entity.ChannelTariff;

import java.time.LocalDateTime;

@Repository
public interface ChannelTariffRepository extends JpaRepository<ChannelTariff, Long> {

    @Modifying
    @Query("""
            UPDATE ChannelTariff ct
            SET ct.startAt = :startAt
            WHERE ct.channel.owner.userId = :userId
              AND ct.startAt IS NULL
            """)
    void setStartAtForUserTariffs(@Param("userId") Long userId,
                                  @Param("startAt") LocalDateTime startAt);

    @Modifying
    @Query("""
            UPDATE ChannelTariff ct
            SET ct.endAt = :endAt
            WHERE ct.channel.owner.userId = :userId
              AND ct.endAt IS NULL
            """)
    void setEndAtForUserTariffs(@Param("userId") Long userId,
                                @Param("endAt") LocalDateTime endAt);

    @Modifying
    @Query("""
            DELETE FROM ChannelTariff ct
            WHERE ct.channel.owner.userId = :userId
              AND ct.startAt IS NULL
            """)
    void deleteTariffsWithNullStartForUser(@Param("userId") Long userId);

    @Query("""
            SELECT COALESCE(SUM(ct.subscriptionAmountGoal), 0)
            FROM ChannelTariff ct
            WHERE ct.channel.id = :channelId
              AND ct.startAt IS NOT NULL
              AND ct.endAt IS NOT NULL
              AND :now BETWEEN ct.startAt AND ct.endAt
            """)
    int getTotalSubscriptionGoalForChannel(@Param("channelId") Long channelId,
                                           @Param("now") LocalDateTime now);

    @Query("""
            SELECT COALESCE(SUM(ct.subscriptionAmountInReturn), 0)
            FROM ChannelTariff ct
            WHERE ct.channel.id = :channelId
              AND ct.startAt IS NOT NULL
              AND ct.endAt IS NOT NULL
              AND :now BETWEEN ct.startAt AND ct.endAt
            """)
    int getTotalSubscriptionInReturnForChannel(@Param("channelId") Long channelId,
                                               @Param("now") LocalDateTime now);
}
