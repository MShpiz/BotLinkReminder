package backend.academy.scrapper.services;

import backend.academy.scrapper.kafkaConfig.KafkaMessageProducer;
import backend.academy.scrapper.models.LinkUpdate;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
public class KafkaBotClient implements BotClient {
    private final KafkaMessageProducer producer;
    private final UpdateJsonConverter converter;
    @Value(value = "${spring.kafka.template.default-topic}")
    private String kafkaTopic;

    public KafkaBotClient(@Autowired KafkaMessageProducer producer, @Autowired UpdateJsonConverter converter) {
        this.producer = producer;
        this.converter = converter;
    }

    @Override
    public boolean sendUserUpdates(LinkUpdate update, List<Long> users) {
        try {
            producer.sendMessage(kafkaTopic, converter.convert(update, users));
        } catch (Exception e) {
            log.atError().addKeyValue("error send updates to bot", "kafka failed").log();
            return false;
        }
        log.atInfo().addKeyValue("send updates to bot status", "sent").log();
        return true;
    }
}
