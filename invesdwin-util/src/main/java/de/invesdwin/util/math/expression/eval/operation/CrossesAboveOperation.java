package de.invesdwin.util.math.expression.eval.operation;

import javax.annotation.concurrent.Immutable;

import de.invesdwin.util.math.expression.IPreviousKeyFunction;
import de.invesdwin.util.math.expression.eval.ConstantExpression;
import de.invesdwin.util.math.expression.eval.IParsedExpression;
import de.invesdwin.util.time.fdate.FDate;

@Immutable
public class CrossesAboveOperation extends BinaryOperation {

    private final IPreviousKeyFunction leftPreviousKeyFunction;
    private final IPreviousKeyFunction rightPreviousKeyFunction;

    public CrossesAboveOperation(final IParsedExpression left, final IParsedExpression right,
            final IPreviousKeyFunction leftPreviousKeyFunction, final IPreviousKeyFunction rightPreviousKeyFunction) {
        super(Op.CROSSES_ABOVE, left, right);
        this.leftPreviousKeyFunction = leftPreviousKeyFunction;
        this.rightPreviousKeyFunction = rightPreviousKeyFunction;
    }

    @Override
    public double evaluateDouble(final FDate key) {
        //crosses above => left was below but went above right

        final double leftValue0 = left.evaluateDouble(key);
        final double rightValue0 = left.evaluateDouble(key);
        //left is above or equal to right
        if (leftValue0 >= rightValue0) {
            final FDate leftPreviousKey = leftPreviousKeyFunction.getPreviousKey(key, 1);
            final double leftValue1 = leftPreviousKeyFunction.evaluateDouble(left, leftPreviousKey);
            final FDate rightPreviousKey = rightPreviousKeyFunction.getPreviousKey(key, 1);
            final double rightValue1 = rightPreviousKeyFunction.evaluateDouble(right, rightPreviousKey);
            //previous left is below previous right
            if (leftValue1 < rightValue1) {
                return 1D;
            }
        }

        return 0D;
    }

    @Override
    public double evaluateDouble(final int key) {
        //crosses above => left was below but went above right

        final double leftValue0 = left.evaluateDouble(key);
        final double rightValue0 = left.evaluateDouble(key);
        //left is above or equal to right
        if (leftValue0 >= rightValue0) {
            final int leftPreviousKey = leftPreviousKeyFunction.getPreviousKey(key, 1);
            final double leftValue1 = leftPreviousKeyFunction.evaluateDouble(left, leftPreviousKey);
            final int rightPreviousKey = rightPreviousKeyFunction.getPreviousKey(key, 1);
            final double rightValue1 = rightPreviousKeyFunction.evaluateDouble(right, rightPreviousKey);
            //previous left is below previous right
            if (leftValue1 < rightValue1) {
                return 1D;
            }
        }

        return 0D;
    }

    @Override
    public double evaluateDouble() {
        throw new UnsupportedOperationException("crosses below operation is only supported with time or int index");
    }

    @Override
    public boolean evaluateBoolean(final FDate key) {
        //crosses above => left was below but went above right

        final double leftValue0 = left.evaluateDouble(key);
        final double rightValue0 = left.evaluateDouble(key);
        //left is above or equal to right
        if (leftValue0 >= rightValue0) {
            final FDate leftPreviousKey = leftPreviousKeyFunction.getPreviousKey(key, 1);
            final double leftValue1 = leftPreviousKeyFunction.evaluateDouble(left, leftPreviousKey);
            final FDate rightPreviousKey = rightPreviousKeyFunction.getPreviousKey(key, 1);
            final double rightValue1 = rightPreviousKeyFunction.evaluateDouble(right, rightPreviousKey);
            //previous left is below previous right
            if (leftValue1 < rightValue1) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean evaluateBoolean(final int key) {
        //crosses above => left was below but went above right

        final double leftValue0 = left.evaluateDouble(key);
        final double rightValue0 = left.evaluateDouble(key);
        //left is above or equal to right
        if (leftValue0 >= rightValue0) {
            final int leftPreviousKey = leftPreviousKeyFunction.getPreviousKey(key, 1);
            final double leftValue1 = leftPreviousKeyFunction.evaluateDouble(left, leftPreviousKey);
            final int rightPreviousKey = rightPreviousKeyFunction.getPreviousKey(key, 1);
            final double rightValue1 = rightPreviousKeyFunction.evaluateDouble(right, rightPreviousKey);
            //previous left is below previous right
            if (leftValue1 < rightValue1) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean evaluateBoolean() {
        throw new UnsupportedOperationException("crosses below operation is only supported with time or int index");
    }

    @Override
    public boolean isConstant() {
        return false;
    }

    @Override
    protected IParsedExpression newConstantExpression() {
        //expression will never be true
        return new ConstantExpression(0D);
    }

    @Override
    protected BinaryOperation newBinaryOperation(final Op op, final IParsedExpression left,
            final IParsedExpression right) {
        return new CrossesAboveOperation(left, right, leftPreviousKeyFunction, rightPreviousKeyFunction);
    }

    @Override
    public IParsedExpression simplify() {
        if (leftPreviousKeyFunction == rightPreviousKeyFunction) {
            return new SimpleCrossesAboveOperation(left, right, leftPreviousKeyFunction).simplify();
        } else {
            return super.simplify();
        }
    }

}
