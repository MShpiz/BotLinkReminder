package backend.academy.scrapper;

import backend.academy.scrapper.Store.jpa.JPAStorage;
import backend.academy.scrapper.models.Link;
import backend.academy.scrapper.models.LinkUpdate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

public class ORMStorageTest extends ScrapperApplicationTests {
    @Autowired
    public JPAStorage storage;

    @Test
    public void AddUser_UserExistsTest() {
        long user = 1L;
        storage.addUser(user);
        IllegalArgumentException thrown =
            assertThrows(IllegalArgumentException.class, () -> storage.addUser(user));

        assertThat(thrown).hasMessage("user exists");
    }

    @Test
    public void AddUser_NewUserTest() {
        long user = 2L;

        storage.addUser(user);

        assertThat(storage.getUserLinks(user)).isEmpty();

    }

    @Test
    public void deleteUser_noUser() {
        IndexOutOfBoundsException thrown =
            assertThrows(IndexOutOfBoundsException.class, () -> storage.deleteUser(10));

        assertThat(thrown).hasMessage("No such user");
    }

    @Test
    public void deleteUser_userExists() {
        storage.addUser(3L);

        storage.deleteUser(3);

        NoSuchElementException thrown =
            assertThrows(NoSuchElementException.class, () -> storage.getUserLinks(10L));
        assertThat(thrown).hasMessage("No such user");
    }

    @Test
    public void AddLink_UserExistsTest() {
        Link link = new Link("aaa", List.of(), List.of());
        long user = 4L;
        storage.addUser(user);

        storage.addLink(user, link);

        assertThat(storage.getUserLinks(user)).contains(link);
    }

    @Test
    public void AddLink_UserDoesNotExistTest() {
        Link link = new Link("aaa", List.of(), List.of());
        long user = 5L;

        IllegalArgumentException thrown =
            assertThrows(IllegalArgumentException.class, () -> storage.addLink(user, link));

        assertThat(thrown).hasMessage("no user with such id");
    }

    @Test
    public void AddLink_UserExistsLinkExistTest() {
        Link link = new Link("aaa", List.of(), List.of());
        long user = 6L;
        storage.addUser(user);
        storage.addLink(user, link);

        IllegalArgumentException thrown =
            assertThrows(IllegalArgumentException.class, () -> storage.addLink(user, link));

        assertThat(thrown).hasMessage("link is already being tracked");
        assertThat(storage.getUserLinks(user)).size().isEqualTo(1);
    }

    @Test
    public void RemoveLink_UserExistsLinkExistsTest() {
        Link link = new Link("aaa", List.of(), List.of());
        long user = 7L;
        storage.addUser(user);
        storage.addLink(user, link);

        storage.removeLink(user, "aaa");

        assertThat(storage.getUserLinks(user).contains(link)).isFalse();
    }

    @Test
    public void RemoveLink_UserExistsLinkDoesNotExistTest() {
        long user = 8L;
        storage.addUser(user);

        NoSuchElementException thrown =
            assertThrows(NoSuchElementException.class, () -> storage.removeLink(user, "non existing link"));

        assertThat(thrown).hasMessageContaining("No value present");
    }

    @Test
    public void RemoveLink_UserDoesNotExistTest() {
        String link = "aaa";
        long user = 9L;

        IllegalArgumentException thrown =
            assertThrows(IllegalArgumentException.class, () -> storage.removeLink(user, link));

        assertThat(thrown).hasMessage("No such user");
    }

    @Test
    public void getLinkChats_NoChatsTest() {
        Link link = new Link("url", new ArrayList<>(), new ArrayList<>());
        long user = 10L;
        storage.addUser(user);
        storage.addLink(user, link);
        storage.deleteUser(user);

        List<Long> res = storage.getLinkChats(link);

        assertThat(res).isEmpty();
    }

    @Test
    public void getLinkChats_ChatsTest() {
        Link link = new Link("url", new ArrayList<>(), new ArrayList<>());
        storage.addUser(11L);
        storage.addUser(12L);
        storage.addLink(11, link);
        storage.addLink(12, link);

        List<Long> res = storage.getLinkChats(link);

        assertThat(res).contains(11L);
        assertThat(res).contains(12L);
    }

    @Test
    public void registerUpdate_Test() {
        Link link = new Link("updatelink", new ArrayList<>(), new ArrayList<>());
        long user = 13L;
        storage.addUser(user);
        storage.addLink(user, link);
        LinkUpdate update = new LinkUpdate(
            "updatelink",
            "topic",
            "preview",
            "username",
            LocalDateTime.now()
        );

        storage.registerUpdate(update);

        List<LinkUpdate> res = storage.getUpdatesAfter(LocalDateTime.of(2020, 12, 31, 0, 0));
        assertThat(res.getFirst().topic()).isEqualTo(update.topic());
        assertThat(res.getFirst().url()).isEqualTo(update.url());
        assertThat(res.getFirst().preview()).isEqualTo(update.preview());
        assertThat(res.getFirst().username()).isEqualTo(update.username());
        assertThat(res.getFirst().updateTime()).isBetween(update.updateTime().minusSeconds(5L), update.updateTime().plusSeconds(5L));
    }

}


