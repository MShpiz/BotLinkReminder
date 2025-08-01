package backend.academy.scrapper.Apis;

import backend.academy.scrapper.Apis.clients.GtClient;
import backend.academy.scrapper.models.Link;
import backend.academy.scrapper.models.LinkUpdate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
public class GitHubScrapper extends Scrapper {

    private final WebClient apiClient;

    public GitHubScrapper(@Autowired GtClient client) {
        this.apiClient = client.client();
    }

    @Override
    public LinkUpdate getUpdates(Link link, LocalDateTime time) {
        try {
            String result = apiClient
                .get()
                .uri(link.url())
                .retrieve()
                .onStatus(HttpStatusCode::isError, error -> error.bodyToMono(String.class)
                    .map(Exception::new))
                .bodyToMono(String.class)
                .block();

            if (time == null || result == null) return null;

            JSONArray arr = new JSONArray(result);
            JSONObject json = arr.getJSONObject(0);

            LocalDateTime updatedAt =
                OffsetDateTime.parse(json.getString("created_at")).toLocalDateTime();
            if (updatedAt.isAfter(time)) {
                LinkUpdate up = new LinkUpdate();
                up.updateTime(updatedAt);
                up.topic(json.getString("title"));
                up.preview(json.getString("title"));
                JSONObject user = json.getJSONObject("user");
                up.username(user.getString("login"));
                up.url(link.url());
                return up;
            }
            return null;
        } catch (Exception e) {
            log.atError()
                .addKeyValue("cant get updates from github", e)
                .log();
            return null;
        }
    }
}
