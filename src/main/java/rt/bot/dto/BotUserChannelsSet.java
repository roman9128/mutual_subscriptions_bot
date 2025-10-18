package rt.bot.dto;

import rt.bot.entity.BotUser;
import rt.bot.entity.Channel;

import java.util.Set;

public record BotUserChannelsSet(BotUser botUser, Set<Channel> channels) {
}
