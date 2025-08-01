package backend.academy.bot.services.commands;

import backend.academy.bot.services.MessageSender;
import backend.academy.bot.services.States;
import backend.academy.bot.storage.ScrapperApi;
import com.pengrad.telegrambot.model.Update;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class CommandCommand {

    @Autowired
    protected ScrapperApi storage;

    public abstract States execute(Update update, MessageSender bot);
}
