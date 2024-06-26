package ru.kernelpunik.teradactyle.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FingerprintId {
    private int value;
    private Long solutionId;
}
