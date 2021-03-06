package de.invesdwin.util.math.stream.decimal;

import javax.annotation.concurrent.NotThreadSafe;

import de.invesdwin.util.math.decimal.ADecimal;
import de.invesdwin.util.math.stream.IStreamAlgorithm;

@NotThreadSafe
public class DecimalStreamAvgWeightedAsc<E extends ADecimal<E>> implements IStreamAlgorithm<E, Void> {

    private final E converter;
    private int sumOfWeights = 0;
    private double sumOfWeightedValues = 0D;
    private int weight = 1;

    public DecimalStreamAvgWeightedAsc(final E converter) {
        this.converter = converter;
    }

    @Override
    public Void process(final E value) {
        final double weightedValue = value.getDefaultValue() * weight;
        sumOfWeights += weight;
        sumOfWeightedValues += weightedValue;
        weight++;
        return null;
    }

    public E getAvgWeightedAsc() {
        if (sumOfWeights == 0) {
            return converter.zero();
        } else {
            return converter.fromDefaultValue(sumOfWeightedValues / sumOfWeights);
        }
    }

}
