package backend.academy.scrapper.services;

import backend.academy.scrapper.Apis.Scrapper;
import backend.academy.scrapper.Store.Storage;
import backend.academy.scrapper.models.Link;
import backend.academy.scrapper.models.LinkUpdate;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ScrappingService {

    private final Storage storage;

    private final LinkParser parser;

    public ScrappingService(
        @Autowired Storage storage, @Autowired LinkParser parser) {
        this.storage = storage;
        this.parser = parser;
    }

    @Scheduled(fixedRate = 30000)
    public void parseAllLinks() {
        Map<Link, LocalDateTime> links = storage.getTimeLinks();
        log.atInfo()
            .addKeyValue("scrapping updates start-time", LocalDateTime.now())
            .log();

        for (Map.Entry<Link, LocalDateTime> entry : links.entrySet()) {
            Link link = entry.getKey();
            Scrapper scrapper = parser.getScrapper(link);
            Link current = parser.parseLink(link);

            if (scrapper == null || current == null) {
                continue;
            }

            LinkUpdate update = scrapper.getUpdates(current, entry.getValue());

            if (update != null) {
                update.url(link.url());
                storage.registerUpdate(update);
            }
        }
        log.atInfo()
            .addKeyValue("scrapping updates end-time", LocalDateTime.now())
            .log();
    }
}
