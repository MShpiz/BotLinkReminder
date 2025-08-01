package backend.academy.scrapper.Store.jpa;

import backend.academy.scrapper.Store.jpa.entities.LinkEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LinkRepository extends JpaRepository<LinkEntity, Long> {
    List<LinkEntity> findAllByUrl(String url);

    LinkEntity getByUrl(String url);

}
