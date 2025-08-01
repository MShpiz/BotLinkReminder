package backend.academy.scrapper.Store.jpa;

import backend.academy.scrapper.Store.Storage;
import backend.academy.scrapper.Store.jpa.entities.LinkEntity;
import backend.academy.scrapper.Store.jpa.entities.LinkUpdateEntity;
import backend.academy.scrapper.Store.jpa.entities.User;
import backend.academy.scrapper.Store.jpa.entities.UserLinkConnection;
import backend.academy.scrapper.models.Link;
import backend.academy.scrapper.models.LinkUpdate;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.beans.factory.annotation.Autowired;

public class JPAStorage implements Storage {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserLinkRepository userLinkRepository;
    @Autowired
    private LinkRepository linkRepository;
    @Autowired
    private LinkUpdateRepository linkUpdateRepository;
    @Autowired
    private EntityManager manager;

    public JPAStorage() {

    }

    public JPAStorage(@Autowired UserRepository userRepository,
                      @Autowired
                      UserLinkRepository userLinkRepository,
                      @Autowired
                      LinkRepository linkRepository,
                      @Autowired
                      LinkUpdateRepository linkUpdateRepository,
                      @Autowired
                      EntityManager manager) {
        this.linkUpdateRepository = linkUpdateRepository;
        this.manager = manager;
        this.userRepository = userRepository;
        this.userLinkRepository = userLinkRepository;
        this.linkRepository = linkRepository;
    }

    @Override
    @Transactional
    public void addUser(Long user) throws IllegalArgumentException {
        if (!userRepository.findAllByChatId(user).isEmpty()) {
            throw new IllegalArgumentException("user exists");
        }
        User usr = new User();
        usr.chatId(user);
        manager.persist(usr);
    }

    @Override
    public List<Link> getUserLinks(Long user) throws NoSuchElementException {

        User usr = userRepository.getByChatId(user);
        if (usr == null) {
            throw new NoSuchElementException("No such user");
        }
        return userLinkRepository.findAllByUserId(usr.id()).stream()
            .map(it ->
                new Link(
                    it.link().url(),
                    Arrays.stream(it.filters()).toList(),
                    Arrays.stream(it.tags()).toList()
                )
            ).toList();
    }

    @Override
    @Transactional
    public void addLink(long user, Link link) throws IllegalArgumentException {
        User usr = userRepository.getByChatId(user);
        if (usr == null) {
            throw new IllegalArgumentException("no user with such id");
        }
        if (linkRepository.findAllByUrl(link.url()).isEmpty()) {
            LinkEntity linkEntity = new LinkEntity();
            linkEntity.url(link.url());
            manager.persist(linkEntity);
            manager.flush();
        }
        LinkEntity lnk = linkRepository.getByUrl(link.url());

        if (userLinkRepository.findAllByLinkAndUser(lnk, usr).isEmpty()) {
            UserLinkConnection con = new UserLinkConnection();
            con.link(lnk);
            con.user(usr);
            con.tags(link.tags().toArray(new String[0]));
            con.filters(link.filters().toArray(new String[0]));
            manager.persist(con);
            manager.flush();
        } else {
            throw new IllegalArgumentException("link is already being tracked");
        }
    }

    @Override
    @Transactional
    public void deleteUser(long user) throws IndexOutOfBoundsException {
        User usr = userRepository.getByChatId(user);
        if (usr == null) {
            throw new IndexOutOfBoundsException("No such user");
        }
        userRepository.deleteUserByChatId(user);
    }

    @Override
    @Transactional
    public void removeLink(long botUser, String link) throws IllegalArgumentException {
        User usr = userRepository.getByChatId(botUser);
        if (usr == null) {
            throw new IllegalArgumentException("No such user");
        }
        LinkEntity lnk = linkRepository.getByUrl(link);
        if (lnk != null) {
            userLinkRepository.deleteUserByUserIdAndLinkId(usr, lnk);
        } else {
            throw new NoSuchElementException("No value present");
        }
    }

    @Override
    public Map<Link, LocalDateTime> getTimeLinks() {
        Map<Link, LocalDateTime> res = new HashMap<>();
        userLinkRepository
            .findAll()
            .forEach(it -> res.put(
                    new Link(it.link().url(), Arrays.stream(it.filters()).toList(), Arrays.stream(it.tags()).toList()),
                    linkUpdateRepository.getByLink(it.link()).updatedAt()
                )
            );
        return res;
    }

    @Override
    public List<Long> getLinkChats(Link link) {
        List<Long> res = new ArrayList<>();
        LinkEntity lnk = linkRepository.getByUrl(link.url());
        userLinkRepository
            .findAllByLinkId(lnk.id())
            .forEach(it -> res.add(it.user().chatId()));
        return res;
    }

    @Override
    @Transactional
    public void registerUpdate(LinkUpdate update) {
        LinkEntity link = linkRepository.getByUrl(update.url());
        if (link == null) {
            return;
        }
        LinkUpdateEntity updateEntity = new LinkUpdateEntity();
        updateEntity.preview(update.preview());
        updateEntity.topic(update.topic());
        updateEntity.username(update.username());
        updateEntity.updatedAt(update.updateTime());
        updateEntity.link(link);
        manager.persist(updateEntity);
        manager.flush();
    }

    @Override
    public List<LinkUpdate> getUpdatesAfter(LocalDateTime prevUpdateTime) {
        return linkUpdateRepository.findAllByUpdatedAtGreaterThan(prevUpdateTime)
            .stream().map(it ->
                new LinkUpdate(
                    it.link().url(),
                    it.topic(),
                    it.preview(),
                    it.username(),
                    it.updatedAt()
                )
            ).toList();
    }
}
