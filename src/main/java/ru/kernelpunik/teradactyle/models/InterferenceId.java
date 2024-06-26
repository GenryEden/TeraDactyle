package ru.kernelpunik.teradactyle.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InterferenceId {
    private Long solutionId;
    private Long interferedSolutionId;
}
