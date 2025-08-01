package backend.academy.scrapper;

import backend.academy.scrapper.models.LinkUpdate;
import backend.academy.scrapper.services.HttpBotClient;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.util.ArrayList;
import java.util.List;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.wiremock.spring.EnableWireMock;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@EnableWireMock
public class HttpBotClientTest extends ScrapperApplicationTests {

    public String wireMockUrl = "http://localhost";
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(8080));

    @Autowired
    public HttpBotClient client;

    @Test
    public void retryTest() {
        LinkUpdate update = new LinkUpdate();
        List<Long> users = new ArrayList<>();
        stubFor(post(urlEqualTo("/updates")).inScenario("update")
            .whenScenarioStateIs(STARTED)
            .withRequestBody(containing("tgChats"))
            .willReturn(aResponse().withStatus(500))
            .willSetStateTo("did not get updates"));

        stubFor(get(urlEqualTo("/updates")).inScenario("did not get updates")
            .whenScenarioStateIs("Cancel newspaper item added")
            .willReturn(aResponse().withStatus(200))
            .willSetStateTo("got updates"));
        try {
            client.sendUserUpdates(update, users);
        } catch (Exception e) {

        }
    }

    @Test
    public void circuitBreakerTest() {
        LinkUpdate update = new LinkUpdate();
        List<Long> users = new ArrayList<>();
        stubFor(post(urlEqualTo("/updates")).inScenario("update")
            .whenScenarioStateIs(STARTED)
            .withRequestBody(containing("tgChats"))
            .willReturn(aResponse().withStatus(500))
            .willSetStateTo(STARTED));

        Exception exception = null;
        try {
            for (int i = 0; i < 1000; i++) {
                client.sendUserUpdates(update, users);
            }

        } catch (Exception e) {
            exception = e;
        }
        assertThat(exception).isNotNull();
    }
}
