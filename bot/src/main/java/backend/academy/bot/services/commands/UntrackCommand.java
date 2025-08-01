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
public class UntrackCommand extends CommandCommand {

    @Override
    public States execute(Update update, MessageSender bot) {
        List<String> message =
            Arrays.stream(update.message().text().trim().split(" ")).toList();
        if (message.size() != 2) {
            bot.sendMessage(update.message().chat().id(), "no link provided");
        } else {
            storage.removeLink(
                new BotUser(update.message().chat().id()),
                message.getLast(),
                new ErrorCallBack() {
                    @Override
                    public void execute(Throwable e) {
                        log.atError()
                            .addKeyValue("cant remove link", e)
                            .log();
                        bot.sendMessage(update.message().chat().id(), "no such link");
                    }
                },
                new BotCallBack() {
                    @Override
                    public void execute() {
                        bot.sendMessage(update.message().chat().id(), "stopped tracking");
                    }
                });
        }
        return States.DEFAULT;
    }
}
