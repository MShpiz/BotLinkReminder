package backend.academy.scrapper.services;

import backend.academy.scrapper.models.LinkUpdate;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
public class HttpBotClient implements BotClient {
    private final WebClient client;
    private final UpdateJsonConverter converter;

    public HttpBotClient(@Autowired WebClient botClient, @Autowired UpdateJsonConverter converter) {
        client = botClient;
        this.converter = converter;
    }

    @CircuitBreaker(name = "breaker")
    @Retry(name = "retry")
    private boolean sendMessage(String body) {
        HttpStatusCode code;
        try {
            code = client.post()
                .body(BodyInserters.fromProducer(Mono.just(body), String.class))
                .retrieve()
                .toBodilessEntity()
                .block()
                .getStatusCode();
        } catch (NullPointerException e) {
            log.atError()
                .addKeyValue("error send updates to bot", "no status code")
                .log();
            return false;
        }
        log.atInfo()
            .addKeyValue("send updates to bot status", code.value())
            .log();

        return true;
    }

    @Override
    public boolean sendUserUpdates(LinkUpdate update, List<Long> users) {
        String body = converter.convert(update, users);
        return sendMessage(body);
    }
}
