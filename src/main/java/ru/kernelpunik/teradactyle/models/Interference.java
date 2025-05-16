package ru.kernelpunik.teradactyle.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import ru.kernelpunik.teradactyle.repositories.ComponentRepository;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="interference")
@IdClass(InterferenceId.class)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public class Interference {
    @Id
    @Column(name = "solution_id")
    private long solutionId;

    @MapsId
    @ManyToOne
    @JoinColumn(
            name = "solution_id",
            referencedColumnName = "solution_id"
    )
    @JsonIgnore
    private Solution solution;

    @Id
    @Column(name = "interfered_component_id")
    private long interferedComponentId;

    @Id
    @Column(name="path")
    private String path;

    @MapsId
    @ManyToOne
    @JoinColumn(
            name = "interfered_component_id",
            referencedColumnName = "component_id"
    )
    @JsonIgnore
    private Component component;

    @Column(name = "interference_fraction")
    private double interferenceFraction;
}
