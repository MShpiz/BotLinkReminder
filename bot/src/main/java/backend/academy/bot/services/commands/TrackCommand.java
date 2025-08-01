package backend.academy.bot.services.commands;

import backend.academy.bot.models.BotUser;
import backend.academy.bot.models.Link;
import backend.academy.bot.services.MessageSender;
import backend.academy.bot.services.States;
import com.pengrad.telegrambot.model.Update;
import java.util.Arrays;
import org.springframework.stereotype.Component;

@Component
public class TrackCommand extends CommandCommand {

    @Override
    public States execute(Update update, MessageSender bot) {
        String[] message = update.message().text().split(" ");
        if (message.length != 2) {
            bot.sendMessage(update.message().chat().id(), "no link provided");
        } else {
            try {
                storage.addLink(
                    new BotUser(update.message().chat().id()),
                    new Link(Arrays.stream(message).toList().getLast()));
            } catch (IllegalArgumentException e) {
                bot.sendMessage(update.message().chat().id(), "authorize with /start");
                return States.DEFAULT;
            }
            bot.sendMessage(update.message().chat().id(), "got link \nenter tags");
            return States.ENTER_TAGS;
        }
        return States.DEFAULT;
    }
}
