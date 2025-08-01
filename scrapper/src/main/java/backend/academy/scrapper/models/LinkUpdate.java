package backend.academy.scrapper.models;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.NullMarked;

@AllArgsConstructor
@NoArgsConstructor
@NullMarked
@Getter
@Setter
@EqualsAndHashCode
public class LinkUpdate {
    String url;
    String topic;
    String preview;
    String username;
    LocalDateTime updateTime;
}
