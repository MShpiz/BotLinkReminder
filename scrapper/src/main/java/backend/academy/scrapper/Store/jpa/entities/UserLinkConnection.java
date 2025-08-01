package backend.academy.scrapper.Store.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "userlinks")
@Getter
@Setter
@NoArgsConstructor
@Entity
public class UserLinkConnection {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
        generator = "gen")
    @SequenceGenerator(name = "gen",
        allocationSize = 1,
        sequenceName = "userlinks_id_seq")
    long id;

    @ManyToOne
    @JoinColumn(name = "linkid")
    LinkEntity link;

    @ManyToOne
    @JoinColumn(name = "userid")
    User user;

    @Column(name = "tags")
    String[] tags;

    @Column(name = "filters")
    String[] filters;

}
