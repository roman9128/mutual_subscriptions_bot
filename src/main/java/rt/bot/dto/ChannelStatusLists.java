package rt.bot.dto;

import java.util.List;

public record ChannelStatusLists(List<ChannelStatus> followed, List<ChannelStatus> unfollowed) {
}
