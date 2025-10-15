package rt.bot.entity;

import lombok.Getter;

public enum Tariff {
    NONE(0, 0),
    TARIFF_1(50, 100),
    TARIFF_2(100, 50),
    TARIFF_3(200, 0),
    VIP(10000000, 0);

    @Getter
    private final int subscriptionAmountGoal;
    @Getter
    private final int subscriptionAmountInReturn;

    Tariff(int subscriptionAmountGoal, int subscriptionAmountInReturn) {
        this.subscriptionAmountGoal = subscriptionAmountGoal;
        this.subscriptionAmountInReturn = subscriptionAmountInReturn;
    }
}
