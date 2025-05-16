package ru.kernelpunik.teradactyle.models;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FingerprintId {
    private int value;
    private Long componentId;
    private int languageId;
}
