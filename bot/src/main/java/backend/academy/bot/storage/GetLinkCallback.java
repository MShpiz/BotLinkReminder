package backend.academy.bot.storage;

import java.util.List;

public interface GetLinkCallback {
    void execute(List<LinkResponse> q);
}
