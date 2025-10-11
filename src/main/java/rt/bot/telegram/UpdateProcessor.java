package rt.bot.telegram;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import rt.bot.entity.BotUser;
import rt.bot.cases.Case;
import rt.bot.cases.InformationCase;
import rt.bot.cases.NoneCase;
import rt.bot.cases.StartCase;

import java.util.EnumMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateProcessor {

    private final UserAuthentication userAuthentication;
    private final StartCase startCase;
    private final InformationCase informationCase;
    private final NoneCase noneCase;
    private final EnumMap<UpdateClass, Case> caseProcessors = new EnumMap<>(UpdateClass.class);

    @PostConstruct
    public void fillCaseProcessors() {
        caseProcessors.put(UpdateClass.START_PROCESS, startCase);
        caseProcessors.put(UpdateClass.INFO_REQUEST, informationCase);
        caseProcessors.put(UpdateClass.NONE, noneCase);
    }

    public void process(Update update) {
        BotUser botUser = userAuthentication.authenticate(update);
        UpdateClass updateClass = UpdateClassifier.classify(update, botUser);
        caseProcessors.get(updateClass).process(update, botUser);
    }
}