package backend.academy.scrapper.Controllers.linkrequest;

import backend.academy.scrapper.Controllers.linkrequest.requests.LinkListResponse;
import backend.academy.scrapper.Controllers.linkrequest.requests.ResponseLink;
import backend.academy.scrapper.RateLimit.RateLimitException;
import backend.academy.scrapper.RateLimit.WithRateLimitProtection;
import backend.academy.scrapper.models.ExceptionDescription;
import backend.academy.scrapper.models.Link;
import backend.academy.scrapper.services.LinkService;
import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.NoSuchElementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/links")
public class LinksController {

    private final LinkService service;

    public LinksController(@Autowired LinkService service) {
        this.service = service;
    }

    @GetMapping
    @WithRateLimitProtection
    public LinkListResponse getLinks(@RequestParam long chatId) {
        return service.getUserLinks(chatId);
    }

    @PostMapping
    @WithRateLimitProtection
    public ResponseLink addLink(@RequestParam long chatId, @Valid @RequestBody Link link) {
        return service.addLinkToUser(chatId, link);
    }

    @DeleteMapping
    @WithRateLimitProtection
    public void deleteLink(@RequestParam long chatId, @RequestBody String url) {
        service.deleteLinkFromUser(chatId, url);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ExceptionDescription> handleIllegalArgument(IllegalArgumentException exception) {
        return new ResponseEntity<>(
            new ExceptionDescription(
                exception.getMessage(),
                "400",
                IllegalArgumentException.class.getName(),
                exception.getMessage(),
                Arrays.stream(exception.getStackTrace())
                    .map(String::valueOf)
                    .toList()),
            HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ExceptionDescription> handleIllegalArgument(NoSuchElementException exception) {
        return new ResponseEntity<>(
            new ExceptionDescription(
                "no link",
                "404",
                IllegalArgumentException.class.getName(),
                exception.getMessage(),
                Arrays.stream(exception.getStackTrace())
                    .map(String::valueOf)
                    .toList()),
            HttpStatus.NOT_FOUND);
    }

    @Order(Ordered.HIGHEST_PRECEDENCE)
    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<ExceptionDescription> handleIllegalArgument(RateLimitException exception) {
        return new ResponseEntity<>(
            exception.getExceptionDescription("/links"),
            HttpStatus.TOO_MANY_REQUESTS);
    }
}
