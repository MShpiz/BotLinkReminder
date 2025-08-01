package backend.academy.scrapper;

import backend.academy.scrapper.Controllers.linkrequest.LinksController;
import backend.academy.scrapper.Controllers.linkrequest.requests.ResponseLink;
import backend.academy.scrapper.Store.Storage;
import backend.academy.scrapper.models.Link;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.doThrow;

public class ListControllerTest extends ScrapperApplicationTests {
    @MockitoBean
    Storage storage;

    @InjectMocks
    @Autowired
    LinksController controller;

    @Test
    public void HandleAddLink_correctLink() {
        long chatId = 123;
        String url = "aaa";
        List<String> tagsFilters = new ArrayList<>();
        Link link = new Link(url, tagsFilters, tagsFilters);
        ResponseLink response = new ResponseLink(chatId, url, tagsFilters, tagsFilters);

        ResponseLink resp = controller.addLink(chatId, link);

        assertThat(resp).isEqualTo(response);
    }

    @Test
    public void HandleAddLink_noUser() {
        long chatId = 123;
        String url = "aaa";
        List<String> tagsFilters = new ArrayList<>();
        Link link = new Link(url, tagsFilters, tagsFilters);
        String message = "no user with such id";
        doThrow(new NoSuchElementException(message)).when(storage).addLink(chatId, link);

        NoSuchElementException thrown =
            assertThrows(NoSuchElementException.class, () -> controller.addLink(chatId, link));

        assertThat(thrown).hasMessage(message);
    }
}
