package ru.kernelpunik.teradactyle.pdg.engine;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Конфигурация параметров для поиска зависимостей в PDG,
 * загружается из Spring Boot properties с префиксом "pdg".
 */
@Component
@ConfigurationProperties(prefix = "pdg")
@Validated
public class Settings {

    /** Желаемое количество строк (вершин) в каждой единице сравнения */
    private int unitSize;

    /** Максимальное отклонение (в вершинах) при поиске оптимальной границы */
    private int tolerance;

    /** Минимальная доля совпадающих меток между двумя фрагментами */
    private double labelSimilarityThreshold;

    /** Минимальная требуемая схожесть по характеристическим векторам */
    private double charVectorSimilarityThreshold;

    public int getUnitSize() {
        return unitSize;
    }

    public void setUnitSize(int unitSize) {
        this.unitSize = unitSize;
    }

    public int getTolerance() {
        return tolerance;
    }

    public void setTolerance(int tolerance) {
        this.tolerance = tolerance;
    }

    public double getLabelSimilarityThreshold() {
        return labelSimilarityThreshold;
    }

    public void setLabelSimilarityThreshold(double labelSimilarityThreshold) {
        this.labelSimilarityThreshold = labelSimilarityThreshold;
    }

    public double getCharVectorSimilarityThreshold() {
        return charVectorSimilarityThreshold;
    }

    public void setCharVectorSimilarityThreshold(double charVectorSimilarityThreshold) {
        this.charVectorSimilarityThreshold = charVectorSimilarityThreshold;
    }
}
