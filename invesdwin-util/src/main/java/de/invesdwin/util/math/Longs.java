package de.invesdwin.util.math;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.concurrent.Immutable;

import de.invesdwin.norva.apt.staticfacade.StaticFacadeDefinition;
import de.invesdwin.util.lang.ADelegateComparator;
import de.invesdwin.util.lang.Objects;
import de.invesdwin.util.math.internal.ALongsStaticFacade;
import de.invesdwin.util.math.internal.CheckedCastLongs;
import de.invesdwin.util.math.internal.CheckedCastLongsObj;

@StaticFacadeDefinition(name = "de.invesdwin.util.math.internal.ALongsStaticFacade", targets = { CheckedCastLongs.class,
        CheckedCastLongsObj.class,
        com.google.common.primitives.Longs.class }, filterMethodSignatureExpressions = { ".* toArray\\(.*" })
@Immutable
public final class Longs extends ALongsStaticFacade {

    public static final long DEFAULT_MISSING_VALUE = 0;
    public static final Long DEFAULT_MISSING_VALUE_OBJ = DEFAULT_MISSING_VALUE;
    public static final ADelegateComparator<Long> COMPARATOR = new ADelegateComparator<Long>() {
        @Override
        protected Comparable<?> getCompareCriteria(final Long e) {
            return e;
        }
    };

    private Longs() {}

    public static long[] toArray(final Collection<? extends Number> vector) {
        if (vector == null) {
            return null;
        }
        return com.google.common.primitives.Longs.toArray(vector);
    }

    public static long[] toArrayVector(final Collection<Long> vector) {
        return toArray(vector);
    }

    public static long[][] toArrayMatrix(final List<? extends List<Long>> matrix) {
        if (matrix == null) {
            return null;
        }
        final long[][] arrayMatrix = new long[matrix.size()][];
        for (int i = 0; i < matrix.size(); i++) {
            final List<Long> vector = matrix.get(i);
            arrayMatrix[i] = toArrayVector(vector);
        }
        return arrayMatrix;
    }

    public static List<Long> asList(final long... vector) {
        if (vector == null) {
            return null;
        } else {
            return ALongsStaticFacade.asList(vector);
        }
    }

    public static List<Long> asListVector(final long[] vector) {
        return asList(vector);
    }

    public static List<List<Long>> asListMatrix(final long[][] matrix) {
        if (matrix == null) {
            return null;
        }
        final List<List<Long>> matrixAsList = new ArrayList<List<Long>>(matrix.length);
        for (final long[] vector : matrix) {
            matrixAsList.add(asList(vector));
        }
        return matrixAsList;
    }

    public static Long min(final Long... times) {
        Long minTime = null;
        for (final Long time : times) {
            minTime = min(minTime, time);
        }
        return minTime;
    }

    public static Long min(final Long time1, final Long time2) {
        if (time1 == null) {
            return time2;
        } else if (time2 == null) {
            return time1;
        }

        if (time1 < time2) {
            return time1;
        } else {
            return time2;
        }
    }

    public static Long max(final Long... times) {
        Long maxTime = null;
        for (final Long time : times) {
            maxTime = max(maxTime, time);
        }
        return maxTime;
    }

    public static Long max(final Long time1, final Long time2) {
        if (time1 == null) {
            return time2;
        } else if (time2 == null) {
            return time1;
        }

        if (time1 > time2) {
            return time1;
        } else {
            return time2;
        }
    }

    public static Long between(final Long value, final Long min, final Long max) {
        return max(min(value, max), min);
    }

    public static <T> long[][] fixInconsistentMatrixDimensions(final long[][] matrix) {
        return fixInconsistentMatrixDimensions(matrix, DEFAULT_MISSING_VALUE);
    }

    public static long[][] fixInconsistentMatrixDimensions(final long[][] matrix, final long missingValue) {
        return fixInconsistentMatrixDimensions(matrix, missingValue, Objects.DEFAULT_APPEND_MISSING_VALUES);
    }

    public static <T> long[][] fixInconsistentMatrixDimensions(final long[][] matrix, final long missingValue,
            final boolean appendMissingValues) {
        final int rows = matrix.length;
        int cols = 0;
        boolean colsInconsistent = false;
        for (int i = 0; i < rows; i++) {
            final long[] vector = matrix[i];
            if (cols != 0 && cols != vector.length) {
                colsInconsistent = true;
            }
            cols = Integers.max(cols, vector.length);
        }
        if (!colsInconsistent) {
            return matrix;
        }
        final long[][] fixedMatrix = new long[rows][];
        for (int i = 0; i < matrix.length; i++) {
            final long[] vector = matrix[i];
            final long[] fixedVector;
            if (vector.length == cols) {
                fixedVector = vector.clone();
            } else {
                fixedVector = new long[cols];
                if (appendMissingValues) {
                    System.arraycopy(vector, 0, fixedVector, 0, vector.length);
                    if (missingValue != DEFAULT_MISSING_VALUE) {
                        for (int j = vector.length - 1; j < fixedVector.length; j++) {
                            fixedVector[j] = missingValue;
                        }
                    }
                } else {
                    //prepend
                    final int missingValues = fixedVector.length - vector.length;
                    if (missingValue != DEFAULT_MISSING_VALUE) {
                        for (int j = 0; j < missingValues; j++) {
                            fixedVector[j] = missingValue;
                        }
                    }
                    System.arraycopy(vector, 0, fixedVector, missingValues, vector.length);
                }
            }
            fixedMatrix[i] = fixedVector;
        }
        return fixedMatrix;
    }

    public static <T> Long[][] fixInconsistentMatrixDimensionsObj(final Long[][] matrix) {
        return fixInconsistentMatrixDimensionsObj(matrix, DEFAULT_MISSING_VALUE_OBJ);
    }

    public static Long[][] fixInconsistentMatrixDimensionsObj(final Long[][] matrix, final Long missingValue) {
        return fixInconsistentMatrixDimensionsObj(matrix, missingValue, Objects.DEFAULT_APPEND_MISSING_VALUES);
    }

    public static <T> Long[][] fixInconsistentMatrixDimensionsObj(final Long[][] matrix, final Long missingValue,
            final boolean appendMissingValues) {
        return Objects.fixInconsistentMatrixDimensions(matrix, missingValue, appendMissingValues);
    }

    public static List<List<Long>> fixInconsistentMatrixDimensionsAsList(
            final List<? extends List<? extends Long>> matrix) {
        return fixInconsistentMatrixDimensionsAsList(matrix, DEFAULT_MISSING_VALUE_OBJ);
    }

    public static List<List<Long>> fixInconsistentMatrixDimensionsAsList(
            final List<? extends List<? extends Long>> matrix, final Long missingValue) {
        return fixInconsistentMatrixDimensionsAsList(matrix, missingValue, Objects.DEFAULT_APPEND_MISSING_VALUES);
    }

    public static List<List<Long>> fixInconsistentMatrixDimensionsAsList(
            final List<? extends List<? extends Long>> matrix, final Long missingValue,
            final boolean appendMissingValues) {
        return Objects.fixInconsistentMatrixDimensionsAsList(matrix, missingValue, appendMissingValues);
    }

}
