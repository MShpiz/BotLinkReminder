package backend.academy.scrapper.Apis.clients;

import org.springframework.web.reactive.function.client.WebClient;

public record SOClient(WebClient client) {
}
