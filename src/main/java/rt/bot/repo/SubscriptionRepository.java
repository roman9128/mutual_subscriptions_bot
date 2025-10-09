package rt.bot.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rt.bot.entity.Subscription;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
}
