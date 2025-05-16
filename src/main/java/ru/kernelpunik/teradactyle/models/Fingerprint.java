package ru.kernelpunik.teradactyle.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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
    @Column(name = "component_id")
    private Long componentId;

    @Id
    @Column(name = "language_id")
    private int languageId;

    @MapsId
    @ManyToOne
    @JoinColumn(
            name = "component_id",
            referencedColumnName = "component_id"
    )
    private Component component;

}
