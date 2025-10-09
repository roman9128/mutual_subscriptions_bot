package rt.bot.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import rt.bot.entity.Channel;

@Repository
public interface ChannelRepository extends JpaRepository<Channel, Long> {

    @Transactional
    @Modifying
    @Query("UPDATE Channel c SET c.botIsAdmin = :botIsAdmin WHERE c.channelId = :channelId")
    void updateBotIsAdmin(@Param("channelId") Long channelId, @Param("botIsAdmin") boolean botIsAdmin);
}
