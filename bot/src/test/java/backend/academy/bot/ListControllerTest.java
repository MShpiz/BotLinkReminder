package backend.academy.bot;

import backend.academy.bot.models.BotUser;
import backend.academy.bot.services.MessageSender;
import backend.academy.bot.services.commands.ListCommand;
import backend.academy.bot.storage.LinkResponse;
import backend.academy.bot.storage.ScrapperApi;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ListControllerTest extends BotApplicationTests {
    private static final long chatId = 1L;

    @Mock
    MessageSender sender;

    @Mock
    Update update;

    @Mock
    Chat chat;

    @Mock
    Message message;

    @MockitoBean
    ScrapperApi storage;

    @InjectMocks
    ListCommand controller;

    private ArgumentCaptor<Long> idCaptor;
    private ArgumentCaptor<String> messageCaptor;

    @BeforeEach
    public void beforeEach() {
        idCaptor = ArgumentCaptor.forClass(Long.class);
        messageCaptor = ArgumentCaptor.forClass(String.class);
    }

    @Test
    public void listFormating_2links_Test() {
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        String resultString =
            "1. a tags: t1, t2, filters: f1, f2\n2. b tags: t1, t2, filters: f1, f2\n";
        List<LinkResponse> links = List.of(
            new LinkResponse(1, "a", List.of("f1", "f2"), List.of("t1", "t2")),
            new LinkResponse(2, "b", List.of("f1", "f2"), List.of("t1", "t2")));
        when(storage.getUserLinks(new BotUser(chatId))).thenReturn(links);

        controller.execute(update, sender);

        verify(sender).sendMessage(idCaptor.capture(), messageCaptor.capture());
        assertThat(messageCaptor.getValue()).isEqualTo(resultString);
    }

    @Test
    public void listFormating_1link_Test() {
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        String resultString = "1. a tags: t1, t2, filters: f1, f2\n";
        List<LinkResponse> links = List.of(new LinkResponse(1, "a", List.of("f1", "f2"), List.of("t1", "t2")));
        when(storage.getUserLinks(new BotUser(chatId))).thenReturn(links);

        controller.execute(update, sender);

        verify(sender).sendMessage(idCaptor.capture(), messageCaptor.capture());
        assertThat(messageCaptor.getValue()).isEqualTo(resultString);
    }

    @Test
    public void listFormating_0link_Test() {
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        String resultString = "no links are being tracked";
        long chatId = 1L;
        List<LinkResponse> links = List.of();
        when(storage.getUserLinks(new BotUser(chatId))).thenReturn(links);

        controller.execute(update, sender);

        verify(sender).sendMessage(idCaptor.capture(), messageCaptor.capture());
        assertThat(messageCaptor.getValue()).isEqualTo(resultString);
    }
}
