package backend.academy.bot.services;

import backend.academy.bot.BotConfig;
import com.pengrad.telegrambot.ExceptionHandler;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.TelegramException;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

@Slf4j
@EnableConfigurationProperties(BotConfig.class)
@Service
public class BotService implements MessageSender {
    private final TelegramBot bot;
    private final BotStateMachine states;

    public BotService(@Autowired BotConfig properties, @Autowired BotStateMachine states) {
        this.states = states;
        bot = new TelegramBot(properties.telegramToken());
        createBot();
    }

    public void createBot() {
        BotService service = this;
        bot.setUpdatesListener(
            new UpdatesListener() {
                @Override
                public int process(List<Update> updates) {

                    for (Update update : updates) {
                        try {
                            states.processUpdate(update, service);
                        } catch (NoSuchElementException e) {
                            log.atError()
                                .setMessage("unknown command {e}")
                                .addKeyValue("unknown command", e)
                                .log();
                            sendMessage(
                                update.message().chat().id(),
                                "An unknown command"
                                    + update.message().text() + "\nCause: " + e.getMessage());
                        } catch (Exception e) {
                            log.atError()
                                .setMessage(e.getMessage())
                                .addKeyValue("command_process_exception", e)
                                .log();
                            sendMessage(
                                update.message().chat().id(),
                                "An error occured, while processing: "
                                    + update.message().text() + "\nCause: " + e.getMessage());
                        }
                    }

                    return UpdatesListener.CONFIRMED_UPDATES_ALL;
                }
            },
            new ExceptionHandler() {
                @Override
                public void onException(TelegramException e) {
                    if (e.response() != null) {
                        log.atError()
                            .setMessage(e.response().description())
                            .addKeyValue("code", e.response().errorCode())
                            .addKeyValue("description", e.response().description())
                            .log();
                    } else {
                        // probably network error
                        log.atError()
                            .setMessage(e.response().description())
                            .addKeyValue("response", e.response())
                            .log();
                    }
                }
            });
    }

    public void sendMessage(long chatId, String string) {
        SendMessage sendMessage = new SendMessage(chatId, string);

        try {
            bot.execute(sendMessage);
        } catch (Exception e) {
            log.atError()
                .setMessage("")
                .addKeyValue("cant send message", e)
                .log();
        }
    }
}
