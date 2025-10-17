package rt.bot.telegram.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import rt.bot.entity.ChannelTariff;

@Slf4j
@Component
@RequiredArgsConstructor
public class TariffAvailabilityHandler {

//    private final ChannelService channelService;

    public boolean isAvailable(ChannelTariff.Tariff tariff) {
//        return tariff == ChannelTariff.Tariff.TARIFF_1;
        return true;
    }
}
