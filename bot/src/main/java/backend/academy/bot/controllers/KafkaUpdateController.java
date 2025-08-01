package backend.academy.bot.controllers;

import backend.academy.bot.models.UpdateInfo;
import backend.academy.bot.services.UpdateInfoParser;
import backend.academy.bot.services.UpdateService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaUpdateController {

    private final UpdateService service;
    private final UpdateInfoParser parser;


    public KafkaUpdateController(@Autowired UpdateService service, @Autowired UpdateInfoParser parser) {
        this.service = service;
        this.parser = parser;
    }

    @KafkaListener(topics = {"#{'${spring.kafka.template.default-topic}'}"}, groupId = "group1")
    @RetryableTopic(attempts = "1", kafkaTemplate = "kafkaProducerTemplate", dltStrategy = DltStrategy.FAIL_ON_ERROR)
    @SneakyThrows
    public void getUpdates(String updates) {
        UpdateInfo info;
        try {
            info = parser.parse(updates);
        } catch (JsonProcessingException | RuntimeException e) {
            throw new DeserializationException(e.getMessage(), null, false, e);
        }
        service.update(info);
    }

    @DltHandler
    public void handleDltUpdate(
        String json, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.error("Event on dlt topic={}, payload={}", topic, json);
    }
}
