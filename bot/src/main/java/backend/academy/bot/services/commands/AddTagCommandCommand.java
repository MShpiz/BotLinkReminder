package backend.academy.bot.services.commands;

import backend.academy.bot.models.BotUser;
import backend.academy.bot.services.MessageSender;
import backend.academy.bot.services.States;
import com.pengrad.telegrambot.model.Update;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class AddTagCommandCommand extends CommandCommand {

    @Override
    public States execute(final Update update, final MessageSender bot) {
        List<String> message = Arrays.stream(update.message().text().split(" ")).toList();
        if (message.isEmpty()) {
            bot.sendMessage(update.message().chat().id(), "no tags provided");
            return States.DEFAULT;
        }
        boolean res = storage.addTags(new BotUser(update.message().chat().id()), message);

        if (res) {
            bot.sendMessage(update.message().chat().id(), "tags added\nEnter filters");
            return States.ENTER_FILTERS;
        } else {
            bot.sendMessage(update.message().chat().id(), "did not find user or link");
            return States.DEFAULT;
        }
    }
}
