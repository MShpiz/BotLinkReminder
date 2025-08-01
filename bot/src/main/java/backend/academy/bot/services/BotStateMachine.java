package backend.academy.bot.services;

import backend.academy.bot.services.commands.CommandCommand;
import com.pengrad.telegrambot.model.Update;
import java.util.Arrays;
import java.util.Map;
import java.util.NoSuchElementException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BotStateMachine {
    private final Map<States, CommandCommand> states;

    @Getter
    private States currentState = States.DEFAULT;

    BotStateMachine(@Autowired Map<States, CommandCommand> states) {
        this.states = states;
    }

    public void processUpdate(Update update, MessageSender messageSender) {

        if (currentState == States.DEFAULT) {
            String command;

            command = Arrays.stream(update.message().text().trim().toLowerCase().split(" "))
                .findFirst()
                .orElse(" ");

            try {
                currentState = States.findByCommand(command);
            } catch (NoSuchElementException e) {
                log.atError()
                    .addKeyValue("unknown command", e)
                    .log();
                currentState = States.DEFAULT;
                throw new NoSuchElementException("unknown command", e.fillInStackTrace());
            } catch (Exception e) {
                log.atError()
                    .addKeyValue("exception while choosing bot controller", e)
                    .log();
                currentState = States.DEFAULT;
            }
        }
        currentState = states.get(currentState).execute(update, messageSender);
    }
}
