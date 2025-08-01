package backend.academy.bot.storage;

import java.util.List;

public record LinkResponse(long id, String url, List<String> filters, List<String> tags) {
}
