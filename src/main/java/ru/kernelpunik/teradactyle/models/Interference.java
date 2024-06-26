package ru.kernelpunik.teradactyle.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

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
    private Long solutionId;

    @MapsId
    @ManyToOne
    @JoinColumn(
            name = "solution_id",
            referencedColumnName = "solution_id"
    )
    @JsonIgnore
    private Solution solution;

    @Id
    @Column(name = "interfered_solution_id")
    private Long interferedSolutionId;

    @MapsId
    @ManyToOne
    @JoinColumn(
            name = "interfered_solution_id",
            referencedColumnName = "solution_id"
    )
    @JsonIgnore
    private Solution interferedSolution;

    @Column(name = "interference_fraction")
    private double interferenceFraction;
}
