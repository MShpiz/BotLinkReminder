package backend.academy.scrapper.Controllers.linkrequest.requests;

import java.util.List;

public record ResponseLink(long id, String url, List<String> filters, List<String> tags) {
}
