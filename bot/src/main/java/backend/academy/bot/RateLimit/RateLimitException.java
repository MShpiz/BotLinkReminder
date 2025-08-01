package backend.academy.bot.RateLimit;

import java.util.Arrays;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.TOO_MANY_REQUESTS)
public class RateLimitException extends RuntimeException {

    public RateLimitException(final String message) {
        super(message);
    }

    public ExceptionDescription getExceptionDescription(final String path) {
        return new ExceptionDescription(
            "too many requests sent from this ip address to" + path,
            String.valueOf(HttpStatus.TOO_MANY_REQUESTS.value()),
            HttpStatus.TOO_MANY_REQUESTS.name(),
            this.getMessage(),
            Arrays.stream(this.getStackTrace())
                .map(String::valueOf)
                .toList()
        );
    }
}
