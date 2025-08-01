package backend.academy.scrapper.models;

import java.util.List;

public record ExceptionDescription(
    String description, String code, String exceptionName, String exceptionMessage, List<String> stacktrace) {
}
