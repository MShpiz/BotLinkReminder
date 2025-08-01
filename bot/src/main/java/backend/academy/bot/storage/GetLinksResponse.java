package backend.academy.bot.storage;

import java.util.List;

public record GetLinksResponse(List<LinkResponse> links, long size) {
}
