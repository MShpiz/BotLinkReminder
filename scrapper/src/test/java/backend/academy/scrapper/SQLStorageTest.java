package backend.academy.scrapper;

import backend.academy.scrapper.Store.JDBCStorage;
import backend.academy.scrapper.models.Link;
import backend.academy.scrapper.models.LinkUpdate;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.testcontainers.containers.PostgreSQLContainer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;


public class SQLStorageTest extends ScrapperApplicationTests {
    String url;
    private JDBCStorage storage;

    public SQLStorageTest(@Autowired PostgreSQLContainer<?> container) {
        url = container.getJdbcUrl();
    }

    @BeforeEach
    public void before() throws SQLException {
        storage = new JDBCStorage(url,
            "postgres",
            "postgres");
    }

    @Test
    public void AddUser_UserExistsTest() {
        long user = 31L;
        storage.addUser(user);
        IllegalArgumentException thrown =
            assertThrows(IllegalArgumentException.class, () -> storage.addUser(user));

        assertThat(thrown).hasMessage("user exists");
    }

    @Test
    public void AddUser_NewUserTest() {
        long user = 32L;

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
        storage.addUser(33L);

        storage.deleteUser(33);

        NoSuchElementException thrown =
            assertThrows(NoSuchElementException.class, () -> storage.getUserLinks(10L));
        assertThat(thrown).hasMessage("No such user");
    }

    @Test
    public void AddLink_UserExistsTest() {
        Link link = new Link("aaa", List.of(), List.of());
        long user = 34L;
        storage.addUser(user);

        storage.addLink(user, link);

        assertThat(storage.getUserLinks(user)).contains(link);
    }

    @Test
    public void AddLink_UserDoesNotExistTest() {
        Link link = new Link("bbb", List.of(), List.of());
        long user = 35L;

        IllegalArgumentException thrown =
            assertThrows(IllegalArgumentException.class, () -> storage.addLink(user, link));

        assertThat(thrown).hasMessage("no user with such id");
    }

    @Test
    public void AddLink_UserExistsLinkExistTest() {
        Link link = new Link("aaa", List.of(), List.of());
        long user = 36L;
        storage.addUser(user);
        storage.addLink(user, link);

        IllegalArgumentException thrown =
            assertThrows(IllegalArgumentException.class, () -> storage.addLink(user, link));

        assertThat(thrown).hasMessage("link is already being tracked");
        assertThat(storage.getUserLinks(user)).size().isEqualTo(1);
    }

    @Test
    public void RemoveLink_UserExistsLinkExistsTest() {
        Link link = new Link("3aaa", List.of(), List.of());
        long user = 37L;
        storage.addUser(user);
        storage.addLink(user, link);

        storage.removeLink(user, "3aaa");

        assertThat(storage.getUserLinks(user).contains(link)).isFalse();
    }

    @Test
    public void RemoveLink_UserExistsLinkDoesNotExistTest() {
        long user = 38L;
        storage.addUser(user);

        NoSuchElementException thrown =
            assertThrows(NoSuchElementException.class, () -> storage.removeLink(user, "non existing link"));

        assertThat(thrown).hasMessageContaining("No value present");
    }

    @Test
    public void RemoveLink_UserDoesNotExistTest() {
        String link = "3aaa";
        long user = 39L;

        IllegalArgumentException thrown =
            assertThrows(IllegalArgumentException.class, () -> storage.removeLink(user, link));

        assertThat(thrown).hasMessage("No such user");
    }

    @Test
    public void getLinkChats_NoChatsTest() {
        Link link = new Link("new_url", new ArrayList<>(), new ArrayList<>());
        long user = 310L;
        storage.addUser(user);
        storage.addLink(user, link);
        storage.deleteUser(user);

        List<Long> res = storage.getLinkChats(link);

        assertThat(res).isEmpty();
    }

    @Test
    public void getLinkChats_ChatsTest() {
        Link link = new Link("url", new ArrayList<>(), new ArrayList<>());
        storage.addUser(311L);
        storage.addUser(312L);
        storage.addLink(311, link);
        storage.addLink(312, link);

        List<Long> res = storage.getLinkChats(link);

        assertThat(res).contains(311L);
        assertThat(res).contains(312L);
    }

    @Test
    public void registerUpdate_Test() {
        Link link = new Link("updatelink", new ArrayList<>(), new ArrayList<>());
        long user = 313L;
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
