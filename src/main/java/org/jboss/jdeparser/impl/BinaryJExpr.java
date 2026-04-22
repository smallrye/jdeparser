package org.jboss.jdeparser.impl;

import java.io.IOException;

import org.jboss.jdeparser.JExpr;

/**
 * A binary operator expression of the form {@code left op right}.
 * <p>
 * Captures the left and right operands along with the operator token
 * and the operator's precedence and associativity, which are used
 * by the formatting engine to determine whether surrounding parentheses
 * are required.
 */
public final class BinaryJExpr extends AbstractJExpr {

    /** The left-hand operand. */
    private final JExpr left;

    /** The operator token. */
    private final Tokens.$BINOP token;

    /** The right-hand operand. */
    private final JExpr right;

    /** The precedence level of this operator. */
    private final Prec prec;

    /** The associativity of this operator. */
    private final Assoc assoc;

    /**
     * Constructs a new binary expression.
     *
     * @param left  the left-hand operand (never {@code null})
     * @param token the operator token (never {@code null})
     * @param right the right-hand operand (never {@code null})
     * @param prec  the precedence level (never {@code null})
     * @param assoc the associativity (never {@code null})
     */
    public BinaryJExpr(final JExpr left, final Tokens.$BINOP token, final JExpr right, final Prec prec, final Assoc assoc) {
        this.left = left;
        this.token = token;
        this.right = right;
        this.prec = prec;
        this.assoc = assoc;
    }

    /** {@inheritDoc} */
    @Override
    public Prec precedence() {
        return prec;
    }

    /** {@inheritDoc} */
    @Override
    public Assoc associativity() {
        return assoc;
    }

    /** {@inheritDoc} */
    @Override
    public void write(final SourceFileWriter writer) throws IOException {
        writeSubExpr(writer, left, prec, assoc, Assoc.LEFT);
        writer.write(token);
        writeSubExpr(writer, right, prec, assoc, Assoc.RIGHT);
    }

    /**
     * Returns the left-hand operand.
     *
     * @return the left operand (never {@code null})
     */
    public JExpr left() {
        return left;
    }

    /**
     * Returns the operator token.
     *
     * @return the operator token (never {@code null})
     */
    public Tokens.$BINOP token() {
        return token;
    }

    /**
     * Returns the right-hand operand.
     *
     * @return the right operand (never {@code null})
     */
    public JExpr right() {
        return right;
    }
}
