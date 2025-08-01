package backend.academy.bot;

import backend.academy.bot.KafkaConfig.KafkaMessageProducer;
import backend.academy.bot.controllers.KafkaUpdateController;
import backend.academy.bot.models.UpdateInfo;
import backend.academy.bot.services.UpdateInfoParser;
import backend.academy.bot.services.UpdateService;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.ArrayList;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

@Slf4j
public class KafkaUpdateControllerTest extends BotApplicationTests {
    @MockitoBean
    UpdateService service;
    @MockitoBean
    UpdateInfoParser parser;

    @Autowired
    KafkaMessageProducer producer;

    @Test
    public void getUpdates_correctJson() throws JsonProcessingException {
        KafkaUpdateController controller = new KafkaUpdateController(service, parser);
        String json = """
            {
                "url":"url",
                "topic":"topic",
                "preview":"preview",
                "user":"username",
                "tgChatIds":[]
            }
            """;
        UpdateInfo update = new UpdateInfo("url", "preview", "topic", "username", new ArrayList<>());
        doReturn(update).when(parser).parse(json);
        ArgumentCaptor<UpdateInfo> linkCaptor = ArgumentCaptor.forClass(UpdateInfo.class);

        controller.getUpdates(json);

        Mockito.verify(service).update(linkCaptor.capture());
        assertThat(linkCaptor.getValue()).isEqualTo(update);
    }

    @Test
    public void getUpdates_Exception() throws JsonProcessingException {
        KafkaUpdateController controller = new KafkaUpdateController(service, parser);
        String json = """
            {
                "url":"url",
                "topic":"topic",
                "preview":"preview",
                "user":"username",
                "tgChatIds":[]
            }
            """;
        doThrow(new RuntimeException("aaa")).when(parser).parse(json);
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);

        producer.sendMessage("update-links", json);
    }

}
