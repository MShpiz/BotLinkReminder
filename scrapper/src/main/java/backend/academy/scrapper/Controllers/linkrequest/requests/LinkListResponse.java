package backend.academy.scrapper.Controllers.linkrequest.requests;

import java.util.List;

public record LinkListResponse(List<ResponseLink> links, long size) {
}
