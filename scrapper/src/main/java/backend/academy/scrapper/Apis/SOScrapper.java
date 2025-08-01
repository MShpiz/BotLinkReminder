package backend.academy.scrapper.Apis;

import backend.academy.scrapper.Apis.clients.SOClient;
import backend.academy.scrapper.ScrapperConfig;
import backend.academy.scrapper.models.Link;
import backend.academy.scrapper.models.LinkUpdate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
public class SOScrapper extends Scrapper {

    private final WebClient apiClient;
    private final String keyAndToken;

    public SOScrapper(@Autowired SOClient client, @Autowired ScrapperConfig config) {
        apiClient = client.client();
        keyAndToken = "&access_token=" + config.stackOverflow().accessToken() + "&key="
            + config.stackOverflow().key();
    }

    @Override
    public LinkUpdate getUpdates(Link link, LocalDateTime time) {
        String finalUrl =
            String.join("", link.url() + "?",
                String.join("&", link.filters()),
                "&site=stackoverflow", keyAndToken);
        try {
            String result = apiClient
                .get()
                .uri(finalUrl)
                .retrieve()
                .onStatus(HttpStatusCode::isError, error -> error.bodyToMono(String.class)
                    .map(Exception::new))
                .bodyToMono(String.class)
                .block();

            if (result == null) return null;

            JSONObject json = new JSONObject(result);
            JSONObject question =
                new JSONObject(json.getJSONArray("items").get(0).toString());
            long updatedAt = Long.parseLong(question.getString("creation_date"));
            LocalDateTime updateTime = LocalDateTime.ofEpochSecond(updatedAt, 0, ZoneOffset.UTC);

            if (time == null || updateTime.isAfter(time)) {
                JSONObject lastCommit = getLastCommit(link);
                JSONObject lastComment = getLastComment(link);
                if (lastComment == null && lastCommit == null) return null;
                JSONObject obj;
                if (lastComment == null || lastCommit != null && Long.parseLong(lastCommit.getString("creation_date")) > Long.parseLong(lastComment.getString("creation_date"))) {
                    obj = lastCommit;
                } else {
                    obj = lastComment;
                }
                LinkUpdate up = new LinkUpdate();
                up.updateTime(LocalDateTime.ofEpochSecond(
                    Long.parseLong(obj.getString("creation_date")),
                    0,
                    ZoneOffset.UTC)
                );
                up.topic(question.getString("title"));
                up.preview(obj.getString("body").substring(0, 200));
                up.username(obj.getJSONObject("owner").getString("display_name"));
                return up;
            }
            return null;
        } catch (Exception e) {
            log.atError()
                .addKeyValue("exception get updates from stackOverflow", e)
                .log();
            return null;
        }
    }

    private JSONObject getLastComment(Link link) {
        try {
            String url = "/answers" + String.join("", link.url() + "?",
                String.join("&", link.filters()),
                "&site=stackoverflow", keyAndToken, "&filter=withbody");
            String result = apiClient
                .get()
                .uri(url)
                .retrieve()
                .onStatus(HttpStatusCode::isError, error -> error.bodyToMono(String.class)
                    .map(Exception::new))
                .bodyToMono(String.class)
                .block();
            JSONObject json = new JSONObject(result);
            return json.getJSONArray("items").getJSONObject(0);
        } catch (Exception e) {
            log.atError()
                .addKeyValue("exception get answers from stackOverflow", e)
                .log();
            return null;
        }
    }

    private JSONObject getLastCommit(Link link) {
        try {
            String url = "/comments" + String.join("", link.url() + "?",
                String.join("&", link.filters()),
                "&site=stackoverflow", keyAndToken, "&filter=withbody");
            String result = apiClient
                .get()
                .uri(url)
                .retrieve()
                .onStatus(HttpStatusCode::isError, error -> error.bodyToMono(String.class)
                    .map(Exception::new))
                .bodyToMono(String.class)
                .block();
            JSONObject json = new JSONObject(result);
            return json.getJSONArray("items").getJSONObject(0);
        } catch (Exception e) {
            log.atError()
                .addKeyValue("exception get comments from stackOverflow", e)
                .log();
            return null;
        }
    }
}
