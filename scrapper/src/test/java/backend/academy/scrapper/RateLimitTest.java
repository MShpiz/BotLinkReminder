package backend.academy.scrapper;


import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import org.wiremock.spring.EnableWireMock;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@EnableWireMock
public class RateLimitTest extends ScrapperApplicationTests {

    @Test
    public void CheckRateLimiter() {
        try {
            WebClient client = WebClient.builder()
                .baseUrl("http://localhost:8081/links/")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
            ResponseEntity response = null;
            for (int i = 0; i < 10000; i++) {
                response = client.get()
                    .uri(String.join("", "/links?chatId=", Long.toString(1L)))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
                if (response == null) continue;
                if (!response.getStatusCode().is2xxSuccessful() && !response.getStatusCode().is4xxClientError()) break;
            }

            assert response != null;
            assertThat(response.getStatusCode()).isEqualTo(429);
        } catch (Exception e) {

        }
    }

}
