package backend.academy.bot.services;

import backend.academy.bot.models.UpdateInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UpdateService {
    private final BotService bot;

    public UpdateService(@Autowired BotService bot) {
        this.bot = bot;
    }

    public void update(UpdateInfo info) {
        for (Long chat : info.tgChatIds()) {
            bot.sendMessage(chat, "*link* " + info.url() + " *updated by* " + info.user() + "\n"
                + "*title:* " + info.topic() + "\n" +
                info.preview()
            );
        }
    }
}
