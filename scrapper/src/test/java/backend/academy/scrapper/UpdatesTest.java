package backend.academy.scrapper;

import backend.academy.scrapper.Apis.GitHubScrapper;
import backend.academy.scrapper.Store.Storage;
import backend.academy.scrapper.models.Link;
import backend.academy.scrapper.models.LinkUpdate;
import backend.academy.scrapper.services.ScrappingService;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

public class UpdatesTest extends ScrapperApplicationTests {
    @MockitoBean
    Storage storage;

    @MockitoBean
    GitHubScrapper scrapper;

    @Autowired
    ScrappingService service;

    @Test
    void checkUsers() {
        Link link1 = new Link("https://github.com/dummie/repo", List.of(), List.of());
        Link innerLink1 = new Link("dummie/repo", List.of(), List.of());
        LocalDateTime time = LocalDateTime.now();
        LinkUpdate update = new LinkUpdate(
            "https://github.com/dummie/repo",
            "topic",
            "preview",
            "username",
            time
        );
        HashMap<Link, LocalDateTime> links = new HashMap();
        links.put(link1, time);
        List<Long> link1Users = List.of(1L, 2L);
        doReturn(links).when(storage).getTimeLinks();
        doReturn(link1Users).when(storage).getLinkChats(link1);
        doReturn(update).when(scrapper).getUpdates(innerLink1, time);
        ArgumentCaptor<LinkUpdate> linkCaptor = ArgumentCaptor.forClass(LinkUpdate.class);

        service.parseAllLinks();

        verify(storage).registerUpdate(linkCaptor.capture());
        assertThat(linkCaptor.getValue()).isEqualTo(update);
    }
}
