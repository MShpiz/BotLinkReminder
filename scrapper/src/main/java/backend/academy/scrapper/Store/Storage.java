package backend.academy.scrapper.Store;

import backend.academy.scrapper.models.Link;
import backend.academy.scrapper.models.LinkUpdate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public interface Storage {
    void addUser(Long user) throws IllegalArgumentException;

    List<Link> getUserLinks(Long user) throws NoSuchElementException;

    void addLink(long user, Link link) throws IllegalArgumentException;

    void deleteUser(long user) throws IndexOutOfBoundsException;

    void removeLink(long botUser, String link) throws IllegalArgumentException;

    Map<Link, LocalDateTime> getTimeLinks();

    List<Long> getLinkChats(Link link);

    void registerUpdate(LinkUpdate update);

    List<LinkUpdate> getUpdatesAfter(LocalDateTime prevUpdateTime);
}
