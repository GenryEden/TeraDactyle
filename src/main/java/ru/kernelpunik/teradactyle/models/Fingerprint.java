package ru.kernelpunik.teradactyle.models;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "fingerprint")
@IdClass(FingerprintId.class)
public class Fingerprint {
    @Id
    @Column(name = "fingerprint_value")
    private int value;

    @Id
    @Column(name = "solution_id")
    private Long solutionId;

    @MapsId
    @ManyToOne
    @JoinColumn(
            name = "solution_id",
            referencedColumnName = "solution_id"
    )
    private Solution solution;

}
