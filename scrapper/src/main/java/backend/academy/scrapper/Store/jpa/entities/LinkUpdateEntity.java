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
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "linkupdates")
public class LinkUpdateEntity {
    @ManyToOne
    @JoinColumn(name = "linkid")
    LinkEntity link;
    @Column(name = "preview")
    String preview;
    @Column(name = "topic")
    String topic;
    @Column(name = "username")
    String username;
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
        generator = "linkupdates_id_gen")
    @SequenceGenerator(name = "linkupdates_id_gen",
        allocationSize = 1,
        sequenceName = "linkupdates_id_seq")
    private Long id;
    @Column(name = "updatedat")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime updatedAt;
}
