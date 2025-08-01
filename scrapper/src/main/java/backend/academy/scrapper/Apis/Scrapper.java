package backend.academy.scrapper.Apis;

import backend.academy.scrapper.models.Link;
import backend.academy.scrapper.models.LinkUpdate;
import java.time.LocalDateTime;

public abstract class Scrapper {

    public abstract LinkUpdate getUpdates(Link link, LocalDateTime time);
}
