package backend.academy.bot;

import backend.academy.bot.models.UpdateInfo;
import backend.academy.bot.services.UpdateInfoParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.ArrayList;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Slf4j
public class UpdateInfoParserTest {

    @Test
    public void correctDataTest() throws JsonProcessingException {
        String json = """
            {
                "url":"url",
                "topic":"topic",
                "preview":"preview",
                "user":"username",
                "tgChatIds":[]
            }
            """;

        UpdateInfo info = new UpdateInfo("url", "preview", "topic",
            "username", new ArrayList<>());
        UpdateInfoParser parser = new UpdateInfoParser();

        UpdateInfo result = parser.parse(json);

        assertThat(result).isEqualTo(info);
    }
}
