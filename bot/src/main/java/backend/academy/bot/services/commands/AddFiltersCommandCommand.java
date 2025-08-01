package backend.academy.bot.services.commands;

import backend.academy.bot.models.BotUser;
import backend.academy.bot.services.MessageSender;
import backend.academy.bot.services.States;
import backend.academy.bot.storage.BotCallBack;
import backend.academy.bot.storage.ErrorCallBack;
import com.pengrad.telegrambot.model.Update;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AddFiltersCommandCommand extends CommandCommand {

    @Override
    public States execute(final Update update, final MessageSender bot) {
        List<String> message = Arrays.stream(update.message().text().split(" ")).toList();
        if (message.isEmpty()) {
            bot.sendMessage(update.message().chat().id(), "no filters provided");
            return States.DEFAULT;
        }
        storage.addFilters(
            new BotUser(update.message().chat().id()),
            message,
            new ErrorCallBack() {
                @Override
                public void execute(Throwable exception) {
                    log.atError()
                        .setMessage("send link exception" + exception.getMessage())
                        .addKeyValue("exception", exception)
                        .log();
                    bot.sendMessage(
                        update.message().chat().id(), "error occured, while tracking link. Try again later");
                }
            },
            new BotCallBack() {
                @Override
                public void execute() {
                    bot.sendMessage(update.message().chat().id(), "filters added. The link is being tracked");
                }
            });
        return States.DEFAULT;
    }
}
