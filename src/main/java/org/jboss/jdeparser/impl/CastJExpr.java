package org.jboss.jdeparser.impl;

import java.io.IOException;

import org.jboss.jdeparser.JExpr;
import org.jboss.jdeparser.JType;
import org.jboss.jdeparser.format.FormatPreferences;

/**
 * A type cast expression of the form {@code (Type) expr}.
 * <p>
 * Cast expressions have {@link Prec#UNARY} precedence and
 * {@link Assoc#RIGHT right-to-left} associativity.
 */
public final class CastJExpr extends AbstractJExpr {

    /** The target type of the cast. */
    private final JType type;

    /** The expression being cast. */
    private final JExpr operand;

    /**
     * Constructs a new cast expression.
     *
     * @param type    the target type (never {@code null})
     * @param operand the expression to cast (never {@code null})
     */
    public CastJExpr(final JType type, final JExpr operand) {
        this.type = type;
        this.operand = operand;
    }

    /** {@inheritDoc} */
    @Override
    public Prec precedence() {
        return Prec.UNARY;
    }

    /** {@inheritDoc} */
    @Override
    public Assoc associativity() {
        return Assoc.RIGHT;
    }

    /** {@inheritDoc} */
    @Override
    public void write(final SourceFileWriter writer) throws IOException {
        // (Type) operand
        writer.write(Tokens.$PAREN.OPEN);
        writeType(writer, type);
        writer.write(Tokens.$PAREN.CLOSE);
        writer.write(FormatPreferences.Space.AFTER_CAST);
        writeSubExpr(writer, operand, Prec.UNARY, Assoc.RIGHT, Assoc.RIGHT);
    }

    /**
     * Returns the target type of the cast.
     *
     * @return the cast type (never {@code null})
     */
    public JType type() {
        return type;
    }

    /**
     * Returns the expression being cast.
     *
     * @return the operand (never {@code null})
     */
    public JExpr operand() {
        return operand;
    }
}
