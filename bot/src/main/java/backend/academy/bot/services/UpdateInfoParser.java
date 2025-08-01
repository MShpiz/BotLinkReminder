package backend.academy.bot.services;

import backend.academy.bot.models.UpdateInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class UpdateInfoParser {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public UpdateInfo parse(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, UpdateInfo.class);
    }
}
