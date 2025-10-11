package rt.bot.telegram.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import rt.bot.entity.Tariff;
import rt.bot.service.ChannelService;

@Slf4j
@Component
@RequiredArgsConstructor
public class TariffAvailabilityHandler {

//    private final ChannelService channelService;

    public boolean isAvailable(Tariff tariff) {
        return tariff == Tariff.TARIFF_1;
    }
}
