package backend.academy.scrapper.models;

import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.NullMarked;

@AllArgsConstructor
@NoArgsConstructor
@NullMarked
@Getter
public class Link {
    @Getter
    String url;

    @Setter
    List<String> filters;

    @Setter
    List<String> tags;

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
