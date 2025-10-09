package rt.bot.entity;

import lombok.Getter;

public enum Tariff {
    NONE(0),
    TARIFF_1(50),
    TARIFF_2(100),
    TARIFF_3(200),
    VIP(10000000);

    @Getter
    private final int subscriptionAmountGoal;

    Tariff(int subscriptionAmountGoal) {
        this.subscriptionAmountGoal = subscriptionAmountGoal;
    }
}
