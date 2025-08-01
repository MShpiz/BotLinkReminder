package backend.academy.scrapper.Store.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
        generator = "user_id_gen")
    @SequenceGenerator(name = "user_id_gen",
        allocationSize = 1,
        sequenceName = "users_id_seq")
    private Long id;

    @Column(name = "chatid")
    private Long chatId;
}
