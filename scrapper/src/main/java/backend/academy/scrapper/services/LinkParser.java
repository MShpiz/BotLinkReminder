package backend.academy.scrapper.services;

import backend.academy.scrapper.Apis.GitHubScrapper;
import backend.academy.scrapper.Apis.SOScrapper;
import backend.academy.scrapper.Apis.Scrapper;
import backend.academy.scrapper.models.Link;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LinkParser {
    private final Map<String, Scrapper> scrapperMap;
    private final Map<String, LinkMutation> linkMutationMap;

    public LinkParser(@Autowired GitHubScrapper scrapper, @Autowired SOScrapper sScrapper) {
        scrapperMap = new HashMap<>();
        String gtDomain = "https://github.com/";
        scrapperMap.put(gtDomain, scrapper);
        String soDomain = "https://stackoverflow.com/questions/";
        scrapperMap.put(soDomain, sScrapper);
        linkMutationMap = new HashMap<>();
        linkMutationMap.put(gtDomain, new LinkMutation() {
            @Override
            public Link mutate(Link link) {
                return new Link(link.url().substring(gtDomain.length()), link.filters(), link.tags());
            }
        });

        linkMutationMap.put(soDomain, new LinkMutation() {
            @Override
            public Link mutate(Link link) {
                return new Link(
                    link.url().substring(soDomain.length(), link.url().indexOf('/', soDomain.length())),
                    link.filters(),
                    link.tags());
            }
        });
    }

    public Scrapper getScrapper(Link link) {
        for (var elem : scrapperMap.entrySet()) {
            if (link.url().startsWith(elem.getKey())) {
                return elem.getValue();
            }
        }
        return null;
    }

    public Link parseLink(Link link) {
        for (var elem : linkMutationMap.entrySet()) {
            if (link.url().startsWith(elem.getKey())) {
                return elem.getValue().mutate(link);
            }
        }
        return null;
    }

    private interface LinkMutation {
        Link mutate(Link l);
    }
}
