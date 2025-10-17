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
@Table(name = "bot_users")
public class BotUser {

    @Id
    @Column(name = "user_id")
    @EqualsAndHashCode.Include
    private Long userId;
    private String telegramUsername;
    private String telegramFirstName;
    private String telegramLastName;
    private int lastMessageIdToDelete;
    private int mistakeCount;
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Channel> ownedChannels = new HashSet<>();
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Subscription> subscriptions = new HashSet<>();
    @Enumerated(EnumType.STRING)
    private DialogStatus dialogStatus;
    @Enumerated(EnumType.STRING)
    private Role role;
    @Enumerated(EnumType.STRING)
    private ChannelTariff.Tariff chosenTariff;
    @Enumerated(EnumType.STRING)
    private ChannelTariff.ChosenPeriod chosenPeriod;
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum Role {
        ADMIN, GUEST, USER, REMOVED
    }

    public enum DialogStatus {
        NONE,
        WAITING_CHANNEL_LINK,
        WAITING_ADD_BOT_AS_ADMIN,
        WAITING_PAYMENT_PERIOD,
        WAITING_PAYMENT_CONFIRMATION
    }
}