package backend.academy.bot;

import backend.academy.bot.services.States;
import backend.academy.bot.services.commands.AddFiltersCommandCommand;
import backend.academy.bot.services.commands.AddTagCommandCommand;
import backend.academy.bot.services.commands.CommandCommand;
import backend.academy.bot.services.commands.HelpCommand;
import backend.academy.bot.services.commands.ListCommand;
import backend.academy.bot.services.commands.StartCommand;
import backend.academy.bot.services.commands.TrackCommand;
import backend.academy.bot.services.commands.UntrackCommand;
import jakarta.validation.constraints.NotEmpty;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;

@Slf4j
@Validated
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public record BotConfig(
    @NotEmpty String telegramToken,
    int clientRetryNumber,
    int clientRetryTimeout,
    int clientTimeout,
    int rateLimit,
    long rateDurationinms
) {
    @Bean
    public WebClient provideClient() {
        return WebClient.builder()
            .baseUrl("http://localhost:8081")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .clientConnector(new ReactorClientHttpConnector(
                HttpClient.create()
                    .responseTimeout(Duration.ofSeconds(clientTimeout)))
            )
            .filter(retryFilter())
            .build();
    }

    private ExchangeFilterFunction retryFilter() {
        return (request, next) ->
            next.exchange(request)
                .filter(response -> response.statusCode() != HttpStatus.BAD_REQUEST
                    && response.statusCode() != HttpStatus.NOT_FOUND
                    && response.statusCode() != HttpStatus.OK)
                .retryWhen(Retry.fixedDelay(clientRetryNumber, Duration.ofSeconds(clientRetryTimeout))
                    .filter(this::retryCheck));
    }

    private boolean retryCheck(Throwable throwable) {
        return throwable instanceof WebClientResponseException &&
            ((WebClientResponseException) throwable).getStatusCode().is5xxServerError();
    }

    @Bean
    public Map<States, CommandCommand> provideStates(
        @Autowired HelpCommand helpController,
        @Autowired StartCommand startController,
        @Autowired ListCommand listController,
        @Autowired TrackCommand trackController,
        @Autowired UntrackCommand untrackController,
        @Autowired AddFiltersCommandCommand addFiltersController,
        @Autowired AddTagCommandCommand addTagCommand) {
        Map<States, CommandCommand> states = new HashMap<>();
        states.put(States.START, startController);
        states.put(States.HELP, helpController);
        states.put(States.LIST, listController);
        states.put(States.TRACK, trackController);
        states.put(States.UNTRACK, untrackController);
        states.put(States.ENTER_TAGS, addTagCommand);
        states.put(States.ENTER_FILTERS, addFiltersController);
        return states;
    }
}
