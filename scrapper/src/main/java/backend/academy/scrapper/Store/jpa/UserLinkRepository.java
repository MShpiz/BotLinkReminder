package backend.academy.scrapper.Store.jpa;

import backend.academy.scrapper.Store.jpa.entities.LinkEntity;
import backend.academy.scrapper.Store.jpa.entities.User;
import backend.academy.scrapper.Store.jpa.entities.UserLinkConnection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UserLinkRepository extends JpaRepository<UserLinkConnection, Long> {
    List<UserLinkConnection> findAllByLinkAndUser(LinkEntity link, User user);

    List<UserLinkConnection> findAllByUserId(Long userId);

    List<UserLinkConnection> findAllByLinkId(Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserLinkConnection WHERE link = :linkId AND user = :userId")
    void deleteUserByUserIdAndLinkId(@Param("userId") User userId, @Param("linkId") LinkEntity linkId);

}
