package backend.academy.bot.models;

import java.util.List;

public record UpdateInfo(
    String url,
    String preview,
    String topic,
    String user,
    List<Long> tgChatIds) {
}
