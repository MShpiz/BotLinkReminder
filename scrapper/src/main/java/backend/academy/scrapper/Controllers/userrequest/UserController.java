package backend.academy.scrapper.Controllers.userrequest;

import backend.academy.scrapper.RateLimit.WithRateLimitProtection;
import backend.academy.scrapper.models.ExceptionDescription;
import backend.academy.scrapper.services.UserService;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/tg-chat")
public class UserController {
    private final UserService service;

    public UserController(@Autowired UserService service) {
        this.service = service;
    }

    @PostMapping("/{id}")
    @WithRateLimitProtection
    public void addUser(@PathVariable long id) {
        service.addUser(id);
    }

    @DeleteMapping("/{id}")
    @WithRateLimitProtection
    public void deleteUser(@PathVariable long id) {
        service.deleteUser(id);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ExceptionDescription> handleIllegalArgument(IllegalArgumentException exception) {
        log.atError()
            .addKeyValue("userController exception", exception)
            .log();
        return new ResponseEntity<>(
            new ExceptionDescription(
                "wrong chat id",
                "400",
                IllegalArgumentException.class.getName(),
                exception.getMessage(),
                Arrays.stream(exception.getStackTrace())
                    .map(String::valueOf)
                    .toList()),
            HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IndexOutOfBoundsException.class)
    public ResponseEntity<ExceptionDescription> handleOutOfBounds(IndexOutOfBoundsException exception) {
        log.atError()
            .addKeyValue("exception userController", exception)
            .log();
        return new ResponseEntity<>(
            new ExceptionDescription(
                "wrong chat id",
                "404",
                IllegalArgumentException.class.getName(),
                exception.getMessage(),
                Arrays.stream(exception.getStackTrace())
                    .map(String::valueOf)
                    .toList()),
            HttpStatus.NOT_FOUND);
    }
}
