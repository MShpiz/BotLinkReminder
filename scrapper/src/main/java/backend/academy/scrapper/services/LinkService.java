package backend.academy.scrapper.services;

import backend.academy.scrapper.Controllers.linkrequest.requests.LinkListResponse;
import backend.academy.scrapper.Controllers.linkrequest.requests.ResponseLink;
import backend.academy.scrapper.Store.Storage;
import backend.academy.scrapper.models.Link;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class LinkService {
    private final Storage storage;
    private final RedisTemplate<Long, LinkListResponse> cache;

    public LinkService(@Autowired Storage storage, @Autowired RedisTemplate<Long, LinkListResponse> cache) {
        this.storage = storage;
        this.cache = cache;
    }

    public LinkListResponse getUserLinks(long chatId) {
        LinkListResponse response = cache.opsForValue().get(chatId);

        if (response != null) {
            return response;
        }
        List<Link> links = storage.getUserLinks(chatId);
        List<ResponseLink> resLinks = new ArrayList<>();
        for (Link link : links) {
            resLinks.add(new ResponseLink(chatId, link.url(), link.filters(), link.tags()));
        }
        response = new LinkListResponse(resLinks, resLinks.size());
        cache.opsForValue().set(chatId, response);
        return response;
    }

    public ResponseLink addLinkToUser(long chatId, Link link) {
        cache.delete(chatId);
        storage.addLink(chatId, link);
        return new ResponseLink(chatId, link.url(), link.filters(), link.tags());
    }

    public void deleteLinkFromUser(long chatId, String url) {
        cache.delete(chatId);
        storage.removeLink(chatId, url);
    }

}
