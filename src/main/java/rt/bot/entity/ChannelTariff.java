package rt.bot.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "channel_tariffs")
public class ChannelTariff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    private Tariff tariff;
    private int subscriptionAmountGoal;
    private int subscriptionAmountInReturn;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    private Channel channel;
    private LocalDateTime startAt;
    private LocalDateTime endAt;

    public boolean isActive() {
        if (startAt == null || endAt == null) return false;
        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(startAt) && !now.isAfter(endAt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChannelTariff that)) return false;
        if (id == null || that.id == null) return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public enum Tariff {
        NONE(0, 0, 0, 0),
        REFERRAL(10, 0, 0, 0),
        TARIFF_1(50, 100, 0, 0),
        TARIFF_2(100, 50, 250, 2500),
        TARIFF_3(200, 0, 480, 5000),
        VIP(10000000, 0, 0, 0);

        @Getter
        private final int subscriptionAmountGoal;
        @Getter
        private final int subscriptionAmountInReturn;
        @Getter
        private final int monthPrice;
        @Getter
        private final int yearPrice;

        Tariff(int subscriptionAmountGoal, int subscriptionAmountInReturn, int monthPrice, int yearPrice) {
            this.subscriptionAmountGoal = subscriptionAmountGoal;
            this.subscriptionAmountInReturn = subscriptionAmountInReturn;
            this.monthPrice = monthPrice;
            this.yearPrice = yearPrice;
        }
    }

    public enum ChosenPeriod {
        NONE(0),
        MONTH(1),
        YEAR(12),
        FOREVER(1000);

        @Getter
        private final int periodLengthInMonth;

        ChosenPeriod(int periodLengthInMonth) {
            this.periodLengthInMonth = periodLengthInMonth;
        }
    }
}