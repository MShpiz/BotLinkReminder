package backend.academy.scrapper;

import backend.academy.scrapper.Controllers.linkrequest.requests.LinkListResponse;
import backend.academy.scrapper.Store.Storage;
import backend.academy.scrapper.models.Link;
import backend.academy.scrapper.services.LinkService;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ListServiceTest extends ScrapperApplicationTests {
    @MockitoBean
    public Storage storage;

    @Autowired
    public RedisTemplate<Long, LinkListResponse> cache;

    @InjectMocks
    @Autowired
    public LinkService service;

    @Test
    public void checkCachedGetUserLinksTest() {
        long chatId = 1;
        cache.delete(chatId);
        List<Link> links = new ArrayList<>();
        when(storage.getUserLinks(chatId)).thenReturn(links);
        service.getUserLinks(chatId);
        ArgumentCaptor<Long> linkCaptor = ArgumentCaptor.forClass(Long.class);

        service.getUserLinks(chatId);

        verify(storage).getUserLinks(linkCaptor.capture());
        assertThat(linkCaptor.getAllValues().size()).isEqualTo(1);
    }

    @Test
    public void checkCachedInvalidateAtAddLinkTest() {
        long chatId = 1;
        cache.delete(chatId);
        List<Link> links = new ArrayList<>();
        when(storage.getUserLinks(chatId)).thenReturn(links);
        service.getUserLinks(chatId);
        service.addLinkToUser(chatId, new Link());
        ArgumentCaptor<Long> linkCaptor = ArgumentCaptor.forClass(Long.class);

        service.getUserLinks(chatId);

        verify(storage, times(2)).getUserLinks(linkCaptor.capture());
    }

    @Test
    public void checkCachedInvalidateAtRemoveLinkTest() {
        long chatId = 1;
        cache.delete(chatId);
        List<Link> links = new ArrayList<>();
        when(storage.getUserLinks(chatId)).thenReturn(links);
        service.getUserLinks(chatId);
        service.deleteLinkFromUser(chatId, "a");
        ArgumentCaptor<Long> linkCaptor = ArgumentCaptor.forClass(Long.class);

        service.getUserLinks(chatId);

        verify(storage, times(2)).getUserLinks(linkCaptor.capture());
        assertThat(linkCaptor.getAllValues().size()).isEqualTo(2);
    }
}
