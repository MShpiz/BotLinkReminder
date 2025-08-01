package backend.academy.scrapper;

import backend.academy.scrapper.Apis.GitHubScrapper;
import backend.academy.scrapper.Apis.SOScrapper;
import backend.academy.scrapper.Apis.Scrapper;
import backend.academy.scrapper.models.Link;
import backend.academy.scrapper.services.LinkParser;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
public class LinkParserTest extends ScrapperApplicationTests {
    @MockitoBean
    SOScrapper soScrapper;

    @MockitoBean
    GitHubScrapper gtScrapper;

    @Autowired
    @InjectMocks
    LinkParser parser;

    @Test
    public void parseGtLink_Test() {
        Link link = new Link("https://github.com/dummie/repo", List.of(), List.of());
        Link expected = new Link("dummie/repo", List.of(), List.of());

        Link result = parser.parseLink(link);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void parseSoLink_Test() {
        Link link = new Link("https://stackoverflow.com/questions/123/aaaa", List.of(), List.of());
        Link expected = new Link("123", List.of(), List.of());

        Link result = parser.parseLink(link);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void getScrapperGt_Test() {
        Link link = new Link("https://github.com/dummie/repo", List.of(), List.of());

        Scrapper result = parser.getScrapper(link);

        assertThat(result).isEqualTo(gtScrapper);
    }

    @Test
    public void getScrapperSo_Test() {
        Link link = new Link("https://stackoverflow.com/questions/123/aaaa", List.of(), List.of());

        Scrapper result = parser.getScrapper(link);

        assertThat(result).isEqualTo(soScrapper);
    }
}
