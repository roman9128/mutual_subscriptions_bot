package rt.bot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rt.bot.entity.BotUser;
import rt.bot.entity.Subscription;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    long countByUserAndStatus(BotUser user, Subscription.Status status);
}