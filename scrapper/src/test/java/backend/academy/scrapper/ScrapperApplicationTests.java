package backend.academy.scrapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(classes = ScrapperApplication.class)
@ExtendWith(MockitoExtension.class)
class ScrapperApplicationTests {

    @Test
    void contextLoads() {
    }
}
