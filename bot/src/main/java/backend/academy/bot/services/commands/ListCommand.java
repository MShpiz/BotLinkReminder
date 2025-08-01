package backend.academy.bot.services.commands;

import backend.academy.bot.models.BotUser;
import backend.academy.bot.services.MessageSender;
import backend.academy.bot.services.States;
import backend.academy.bot.storage.LinkResponse;
import com.pengrad.telegrambot.model.Update;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ListCommand extends CommandCommand {

    @Override
    public States execute(Update update, MessageSender bot) {
        Long chatId = update.message().chat().id();
        try {

            List<LinkResponse> response = storage.getUserLinks(new BotUser(chatId));
            if (response.isEmpty()) {
                bot.sendMessage(chatId, "no links are being tracked");
                return States.DEFAULT;
            }
            StringBuilder linkMessage = new StringBuilder();
            for (int i = 0; i < response.size(); i++) {
                String tags = String.join(", ", response.get(i).tags());
                String filters = String.join(", ", response.get(i).filters());
                linkMessage.append(String.format(
                    "%d. %s tags: %s, filters: %s\n", i + 1, response.get(i).url(), tags, filters));
            }
            bot.sendMessage(chatId, linkMessage.toString());
        } catch (Exception e) {
            log.atError()
                .setMessage("")
                .addKeyValue("cant get links", e)
                .log();
            bot.sendMessage(chatId, e.getMessage());
        }
        return States.DEFAULT;
    }
}
