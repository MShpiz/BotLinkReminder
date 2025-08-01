package backend.academy.scrapper.services;

import backend.academy.scrapper.models.LinkUpdate;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UpdateJsonConverter {
    public String convert(LinkUpdate update, List<Long> users) {
        try {
            JSONObject up = new JSONObject();
            up.put("url", update.url());
            up.put("topic", update.topic());
            up.put("preview", update.preview());
            up.put("user", update.username());

            JSONArray us = new JSONArray();
            for (Long i : users) {
                us.put(i);
            }
            up.put("tgChatIds", us);
            return up.toString();
        } catch (JSONException e) {
            log.atError()
                .setMessage(e.getMessage())
                .addKeyValue("JSONException", e)
                .log();
            return "";
        }
    }
}
