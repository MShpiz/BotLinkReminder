package backend.academy.scrapper.Store.jpa;

import backend.academy.scrapper.Store.jpa.entities.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User getByChatId(Long chatId);

    List<User> findAllByChatId(Long chatId);

    @Modifying
    @Transactional
    @Query("DELETE FROM User u WHERE u.chatId = :chatId")
    int deleteUserByChatId(@Param("chatId") Long chatId);

}
