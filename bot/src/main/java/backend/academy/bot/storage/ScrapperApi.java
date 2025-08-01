package backend.academy.bot.storage;

import backend.academy.bot.models.BotUser;
import backend.academy.bot.models.Link;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class ScrapperApi {

    private final WebClient client;

    private final Map<BotUser, Link> links;

    public ScrapperApi(@Autowired WebClient client) {
        links = new HashMap<>();
        this.client = client;
    }

    @CircuitBreaker(name = "breaker")
    @Retry(name = "retry")
    public void addUser(BotUser user, ErrorCallBack errorCallback, BotCallBack okCallback) {
        client.post()
            .uri(String.join("", "/tg-chat/", Long.toString(user.id())))
            .retrieve()
            .toBodilessEntity()
            .subscribe(
                responseEntity -> {
                    HttpStatusCode status = responseEntity.getStatusCode();
                    if (status.is2xxSuccessful()) {
                        okCallback.execute();
                    }
                },
                errorCallback::execute);
    }

    @CircuitBreaker(name = "breaker")
    @Retry(name = "retry")
    public List<LinkResponse> getUserLinks(BotUser user) {

        GetLinksResponse response;
        try {
            response = client.get()
                .uri(String.join("", "/links?chatId=", Long.toString(user.id())))
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals, error -> error.bodyToMono(String.class)
                    .map(IllegalArgumentException::new))
                .bodyToMono(GetLinksResponse.class)
                .block();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("you are not authorised");
        }
        if (response == null) {
            throw new IllegalArgumentException("no body");
        }
        return response.links();
    }

    public void addLink(BotUser user, Link link) {
        links.put(user, link);
    }

    public boolean addTags(BotUser user, List<String> tags) {
        if (!links.containsKey(user)) return false;
        Link link = links.get(user);
        if (link == null) {
            return false;
        }
        link.tags().addAll(tags);
        return true;
    }

    @CircuitBreaker(name = "breaker")
    @Retry(name = "retry")
    public void addFilters(BotUser user, List<String> tags, ErrorCallBack errorCallBack, BotCallBack okCallback) {
        if (!links.containsKey(user)) return;
        Link link = links.get(user);
        if (link == null) {
            return;
        }
        link.filters().addAll(tags);
        sendLink(user, link, errorCallBack, okCallback);
    }

    @CircuitBreaker(name = "breaker")
    @Retry(name = "retry")
    private void sendLink(BotUser user, Link link, ErrorCallBack errorCallback, BotCallBack okCallback) {
        try {
            String body = new JSONObject()
                .put("url", link.url())
                .put("tags", new JSONArray(link.tags()))
                .put("filters", new JSONArray(link.filters()))
                .toString();

            client.post()
                .uri(String.join("", "/links?chatId=", Long.toString(user.id())))
                .body(BodyInserters.fromProducer(Mono.just(body), String.class))
                .retrieve()
                .toBodilessEntity()
                .subscribe(
                    responseEntity -> {
                        HttpStatusCode status = responseEntity.getStatusCode();
                        if (status.is2xxSuccessful()) {
                            links.remove(user);
                            okCallback.execute();
                        }
                    },
                    errorCallback::execute);
        } catch (JSONException e) {
        }
    }

    @CircuitBreaker(name = "breaker")
    @Retry(name = "retry")
    public void removeLink(BotUser botUser, String last, ErrorCallBack errorCallback, BotCallBack okCallback) {
        client.method(HttpMethod.DELETE)
            .uri(String.join("", "/links?chatId=", Long.toString(botUser.id())))
            .bodyValue(last)
            .retrieve()
            .toBodilessEntity()
            .subscribe(
                responseEntity -> {
                    HttpStatusCode status = responseEntity.getStatusCode();
                    if (status.is2xxSuccessful()) {
                        okCallback.execute();
                    }
                },
                errorCallback::execute);
    }
}
