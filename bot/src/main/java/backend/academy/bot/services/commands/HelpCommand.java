package backend.academy.bot.services.commands;

import backend.academy.bot.services.MessageSender;
import backend.academy.bot.services.States;
import com.pengrad.telegrambot.model.Update;
import org.springframework.stereotype.Component;

@Component
public class HelpCommand extends CommandCommand {
    private static final String helpList =
        """
            /help - see commands
            /track {link} - track a link
            /untrack {link} - stop tracking the link
            /list - see the list of tracked links
            """;

    @Override
    public States execute(Update update, MessageSender bot) {
        bot.sendMessage(update.message().chat().id(), helpList);
        return States.DEFAULT;
    }
}
