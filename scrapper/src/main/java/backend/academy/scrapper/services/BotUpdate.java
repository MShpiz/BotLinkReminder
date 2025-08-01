package backend.academy.scrapper.services;

import backend.academy.scrapper.Store.Storage;
import backend.academy.scrapper.models.Link;
import backend.academy.scrapper.models.LinkUpdate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BotUpdate {
    @Autowired
    private final HttpBotClient botClient;
    @Autowired
    private final KafkaBotClient alternateClient;
    @Autowired
    private final Storage storage;
    @Autowired
    private boolean mainClientHttp = true;

    private BotClient currentClient;
    private LocalDateTime prevUpdateTime = LocalDateTime.now();

    @Scheduled(fixedRate = 300000)
    public void sendUpdates() {
        setCurrentClient();

        List<LinkUpdate> updates = this.storage.getUpdatesAfter(prevUpdateTime);
        for (var update : updates) {
            List<Long> chats = this.storage.getLinkChats(new Link(update.url(), null, null));
            boolean res = currentClient.sendUserUpdates(update, chats);
            if (!res) {
                mainClientHttp = !mainClientHttp;
                setCurrentClient();
                currentClient.sendUserUpdates(update, chats);
            }

        }
        prevUpdateTime = LocalDateTime.now();
    }

    private void setCurrentClient() {
        if (mainClientHttp) {
            currentClient = botClient;
        } else {
            currentClient = alternateClient;
        }
    }
}
