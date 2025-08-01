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
@Table(name = "links")
@Getter
@Setter
@NoArgsConstructor
public class LinkEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
        generator = "link_id_gen")
    @SequenceGenerator(name = "link_id_gen",
        allocationSize = 1,
        sequenceName = "links_id_seq")
    private Long id;

    @Column(name = "url")
    private String url;
}
