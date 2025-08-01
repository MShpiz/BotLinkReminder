package backend.academy.scrapper.services;

import backend.academy.scrapper.models.LinkUpdate;
import java.util.List;

public interface BotClient {
    boolean sendUserUpdates(LinkUpdate update, List<Long> users);
}
