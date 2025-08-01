package backend.academy.scrapper;

import backend.academy.scrapper.Store.JDBCStorage;
import backend.academy.scrapper.Store.Storage;
import backend.academy.scrapper.Store.jpa.JPAStorage;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ConfigTest {

    @Test
    public void SQLStorageTest() {
        ScrapperConfig config = new ScrapperConfig("", null, "sql", "",
            1, 1, 1, 1, 1, null);

        Storage storage = config.provideStorage();

        assertThat(storage).isInstanceOf(JDBCStorage.class);

    }


    @Test
    public void ORMStorageTest() {
        ScrapperConfig config = new ScrapperConfig("", null, "orm", "",
            1, 1, 1, 1, 1, null);

        Storage storage = config.provideStorage();

        assertThat(storage).isInstanceOf(JPAStorage.class);
    }
}
