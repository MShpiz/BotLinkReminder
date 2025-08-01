package backend.academy.bot.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Link {
    String url;

    @Setter
    List<String> filters = new ArrayList<>();

    @Setter
    List<String> tags = new ArrayList<>();

    public Link(String link) {
        url = link;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (!(other instanceof Link)) {
            return false;
        }
        return Objects.equals(this.url, ((Link) other).url);
    }

    @Override
    public int hashCode() {
        return url.hashCode();
    }
}
