package rt.bot.dto;

import rt.bot.entity.Subscription;

public record ChannelStatus(String username, Subscription.Status status) {
}
