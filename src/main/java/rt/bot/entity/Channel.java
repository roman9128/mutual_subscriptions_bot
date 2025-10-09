package rt.bot.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "channels")
public class Channel {

    @Id
    @EqualsAndHashCode.Include
    private Long channelId;
    private String title;
    private String username;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private BotUser owner;
    private boolean botIsAdmin;
    @Enumerated(EnumType.STRING)
    private Tariff tariff;
    private int subscriptionsAmountGoal;
    @OneToMany(mappedBy = "channel", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Subscription> subscriptions = new HashSet<>();
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    private LocalDateTime paidSince;
}