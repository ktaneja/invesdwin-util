package de.invesdwin.util.math.expression.eval.operation;

import javax.annotation.concurrent.Immutable;

import de.invesdwin.util.math.expression.eval.ConstantExpression;
import de.invesdwin.util.math.expression.eval.IParsedExpression;
import de.invesdwin.util.time.fdate.FDate;

@Immutable
public class AndOperation extends BinaryOperation {

    public AndOperation(final IParsedExpression left, final IParsedExpression right) {
        super(Op.AND, left, right);
    }

    @Override
    public double evaluateDouble(final FDate key) {
        final boolean check = left.evaluateBoolean(key) && right.evaluateBoolean(key);
        if (check) {
            return 1D;
        } else {
            return 0D;
        }
    }

    @Override
    public double evaluateDouble(final int key) {
        final boolean check = left.evaluateBoolean(key) && right.evaluateBoolean(key);
        if (check) {
            return 1D;
        } else {
            return 0D;
        }
    }

    @Override
    public double evaluateDouble() {
        final boolean check = left.evaluateBoolean() && right.evaluateBoolean();
        if (check) {
            return 1D;
        } else {
            return 0D;
        }
    }

    @Override
    public boolean evaluateBoolean(final FDate key) {
        return left.evaluateBoolean(key) && right.evaluateBoolean(key);
    }

    @Override
    public boolean evaluateBoolean(final int key) {
        return left.evaluateBoolean(key) && right.evaluateBoolean(key);
    }

    @Override
    public boolean evaluateBoolean() {
        return left.evaluateBoolean() && right.evaluateBoolean();
    }

    @Override
    public boolean isConstant() {
        return false;
    }

    @Override
    public IParsedExpression simplify() {
        final IParsedExpression newLeft = left.simplify();
        final IParsedExpression newRight = right.simplify();
        if (newLeft.isConstant()) {
            if (newLeft.evaluateBoolean()) {
                if (newRight.isConstant()) {
                    if (newRight.evaluateBoolean()) {
                        return new ConstantExpression(1D);
                    } else {
                        return new ConstantExpression(0D);
                    }
                } else {
                    return newRight;
                }
            } else {
                return new ConstantExpression(0D);
            }
        }
        if (newRight.isConstant()) {
            if (newRight.evaluateBoolean()) {
                if (newLeft.isConstant()) {
                    if (newLeft.evaluateBoolean()) {
                        return new ConstantExpression(1D);
                    } else {
                        return new ConstantExpression(0D);
                    }
                } else {
                    return newLeft;
                }
            } else {
                return new ConstantExpression(0D);
            }
        }
        return simplify(newLeft, newRight);
    }

    @Override
    protected BinaryOperation newBinaryOperation(final Op op, final IParsedExpression left,
            final IParsedExpression right) {
        return new AndOperation(left, right);
    }

}
