package backend.academy.bot.services.commands;

import backend.academy.bot.models.BotUser;
import backend.academy.bot.services.MessageSender;
import backend.academy.bot.services.States;
import backend.academy.bot.storage.BotCallBack;
import backend.academy.bot.storage.ErrorCallBack;
import com.pengrad.telegrambot.model.Update;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StartCommand extends CommandCommand {

    @Override
    public States execute(Update update, MessageSender bot) {
        storage.addUser(
            new BotUser(update.message().chat().id()),
            new ErrorCallBack() {
                @Override
                public void execute(Throwable e) {
                    log.atError()
                        .setMessage("get links")
                        .addKeyValue("chat id", update.message().chat().id())
                        .addKeyValue("exception", e)
                        .log();
                    bot.sendMessage(update.message().chat().id(), "You are already authorised");
                }
            },
            new BotCallBack() {
                @Override
                public void execute() {
                    bot.sendMessage(
                        update.message().chat().id(),
                        "Start working with link-tracking. Send /help to see commands");
                }
            });

        return States.DEFAULT;
    }
}
