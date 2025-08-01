package backend.academy.bot;

import backend.academy.bot.services.BotStateMachine;
import backend.academy.bot.services.MessageSender;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;

public class StateMachineTest extends BotApplicationTests {
    @Mock
    Update update;

    @Mock
    Message message;

    @Mock
    MessageSender sender;

    @Autowired
    BotStateMachine machine;

    @Test
    public void unknownCommand_Test() {
        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn("AAA");

        NoSuchElementException exception =
            assertThrows(NoSuchElementException.class, () -> machine.processUpdate(update, sender));

        assertThat(exception).hasMessageContaining("unknown command");
    }
}
