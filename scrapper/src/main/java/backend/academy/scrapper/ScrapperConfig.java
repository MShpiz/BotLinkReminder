package backend.academy.scrapper;

import backend.academy.scrapper.Apis.clients.GtClient;
import backend.academy.scrapper.Apis.clients.SOClient;
import backend.academy.scrapper.Controllers.linkrequest.requests.LinkListResponse;
import backend.academy.scrapper.Store.JDBCStorage;
import backend.academy.scrapper.Store.Storage;
import backend.academy.scrapper.Store.jpa.JPAStorage;
import backend.academy.scrapper.kafkaConfig.KafkaMessageProducer;
import backend.academy.scrapper.services.BotClient;
import backend.academy.scrapper.services.HttpBotClient;
import backend.academy.scrapper.services.KafkaBotClient;
import backend.academy.scrapper.services.UpdateJsonConverter;
import jakarta.validation.constraints.NotEmpty;
import java.sql.SQLException;
import java.time.Duration;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
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

@Validated
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public record ScrapperConfig(
    @NotEmpty String githubToken,
    StackOverflowCredentials stackOverflow,
    String accessType,
    String messageTransport,
    int clientRetryNumber,
    int clientRetryTimeout,
    int clientTimeout,
    int rateLimit,
    int rateDurationinms,
    List<Integer> failCodes

) {
    @Value("${DB_PASS}")
    private static String dbpass = "postgres";
    @Value("${DB_LOGIN}")
    private static String dblogin = "postgres";

    @Bean
    public GtClient provideGtClient() {
        return new GtClient(WebClient.builder()
            .baseUrl("https://api.github.com/repos")
            .defaultHeaders(httpHeaders -> {
                httpHeaders.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                httpHeaders.set("Accept", "application/json");
                httpHeaders.set("User-Agent", "WebClient");
                httpHeaders.set("Authorization", githubToken);
            })
            .clientConnector(new ReactorClientHttpConnector(
                HttpClient.create()
                    .responseTimeout(Duration.ofSeconds(clientTimeout)))
            )
            .filter(retryFilter())
            .build());
    }

    @Bean
    public SOClient provideSoClient() {
        return new SOClient(WebClient.builder()
            .baseUrl("https://api.stackexchange.com/2.3/questions/")
            .clientConnector(new ReactorClientHttpConnector(
                HttpClient.create()
                    .responseTimeout(Duration.ofSeconds(clientTimeout)))
            )
            .filter(retryFilter())
            .build());
    }

    @Bean
    public WebClient provideBot() {
        return WebClient.builder()
            .baseUrl("http://localhost:8080/updates")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .clientConnector(new ReactorClientHttpConnector(
                HttpClient.create()
                    .responseTimeout(Duration.ofSeconds(clientTimeout)))
            )
            .filter(retryFilter())
            .build();
    }

    @Bean

    public Storage provideStorage() {
        if (accessType.equalsIgnoreCase("SQL")) {
            try {
                return new JDBCStorage(
                    "jdbc:postgresql://localhost:5433/tgbot",
                    dblogin,
                    dbpass
                );
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else if (accessType.equalsIgnoreCase("ORM")) {
            return new JPAStorage();
        } else {
            throw new RuntimeException("Unknown access type");
        }
    }

    @Bean
    public BotClient provideUpdateService(@Autowired WebClient client, @Autowired KafkaMessageProducer producer, @Autowired UpdateJsonConverter converter) {
        if (messageTransport.equalsIgnoreCase("HTTP")) {
            return new HttpBotClient(client, converter);
        } else if (messageTransport.equalsIgnoreCase("Kafka")) {
            return new KafkaBotClient(producer, converter);
        } else {
            throw new RuntimeException("Unknown transport type");
        }
    }

    @Bean
    public HttpBotClient provideHttpBotClient(@Autowired WebClient client, @Autowired UpdateJsonConverter converter) {
        return new HttpBotClient(client, converter);
    }

    @Bean
    public KafkaBotClient provideKafkaBotClient(@Autowired KafkaMessageProducer producer, @Autowired UpdateJsonConverter converter) {
        return new KafkaBotClient(producer, converter);
    }

    @Bean
    public boolean provideCurrClientHttp() {
        return messageTransport.equalsIgnoreCase("HTTP");
    }



    @Bean
    @Primary
    public LettuceConnectionFactory lettuceConnectionFactory() {
        LettuceConnectionFactory factory = new LettuceConnectionFactory();
        factory.start();
        return factory;
    }

    @Bean
    @Primary
    public RedisTemplate<Long, LinkListResponse> redisTemplate() {
        RedisTemplate<Long, LinkListResponse> template = new RedisTemplate<>();
        template.setKeySerializer(new GenericToStringSerializer<>(Long.class));
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setConnectionFactory(lettuceConnectionFactory());
        return template;
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
            failCodes.contains(((WebClientResponseException) throwable).getStatusCode().value());
    }

    public record StackOverflowCredentials(@NotEmpty String key, @NotEmpty String accessToken) {
    }
}
