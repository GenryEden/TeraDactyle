package ru.kernelpunik.teradactyle.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@Table(name="solution")
public class Solution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "solution_id")
    private long solutionId;

    @Column(columnDefinition = "text")
    private String code;

    @Column(name = "language_id")
    private int languageId;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Solution solution = (Solution) o;
        return solutionId == solution.solutionId;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(solutionId);
    }
}
