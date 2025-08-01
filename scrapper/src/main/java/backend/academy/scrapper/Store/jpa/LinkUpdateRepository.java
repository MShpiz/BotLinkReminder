package backend.academy.scrapper.Store.jpa;

import backend.academy.scrapper.Store.jpa.entities.LinkEntity;
import backend.academy.scrapper.Store.jpa.entities.LinkUpdateEntity;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LinkUpdateRepository extends JpaRepository<LinkUpdateEntity, Long> {
    LinkUpdateEntity getByLink(LinkEntity link);

    List<LinkUpdateEntity> findAllByUpdatedAtGreaterThan(LocalDateTime time);
}
