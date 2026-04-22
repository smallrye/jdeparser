package org.jboss.jdeparser.impl;

import java.io.IOException;

import org.jboss.jdeparser.JExpr;

/**
 * A unary operator expression, supporting both prefix and postfix forms.
 * <p>
 * Prefix examples: {@code -x}, {@code !flag}, {@code ++i}.
 * Postfix examples: {@code i++}, {@code i--}.
 * <p>
 * The {@link #isPrefix()} flag determines the placement of the operator
 * token relative to the operand during formatting.
 */
public final class UnaryJExpr extends AbstractJExpr {

    /** The operator token. */
    private final Tokens.$UNOP token;

    /** The operand expression. */
    private final JExpr operand;

    /** {@code true} for prefix operators, {@code false} for postfix. */
    private final boolean prefix;

    /** The precedence level of this operator. */
    private final Prec prec;

    /**
     * Constructs a new unary expression.
     *
     * @param token   the operator token (never {@code null})
     * @param operand the operand expression (never {@code null})
     * @param prefix  {@code true} for prefix, {@code false} for postfix
     * @param prec    the precedence level (never {@code null})
     */
    public UnaryJExpr(final Tokens.$UNOP token, final JExpr operand, final boolean prefix, final Prec prec) {
        this.token = token;
        this.operand = operand;
        this.prefix = prefix;
        this.prec = prec;
    }

    /** {@inheritDoc} */
    @Override
    public Prec precedence() {
        return prec;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns {@link Assoc#RIGHT} for prefix operators and {@link Assoc#LEFT}
     * for postfix operators.
     */
    @Override
    public Assoc associativity() {
        return prefix ? Assoc.RIGHT : Assoc.LEFT;
    }

    /** {@inheritDoc} */
    @Override
    public void write(final SourceFileWriter writer) throws IOException {
        if (prefix) {
            writer.write(token);
            writeSubExpr(writer, operand, prec, Assoc.RIGHT, Assoc.RIGHT);
        } else {
            writeSubExpr(writer, operand, prec, Assoc.LEFT, Assoc.LEFT);
            writer.write(token);
        }
    }

    /**
     * Returns the operator token.
     *
     * @return the operator token (never {@code null})
     */
    public Tokens.$UNOP token() {
        return token;
    }

    /**
     * Returns the operand expression.
     *
     * @return the operand (never {@code null})
     */
    public JExpr operand() {
        return operand;
    }

    /**
     * Returns whether this is a prefix operator.
     *
     * @return {@code true} for prefix, {@code false} for postfix
     */
    public boolean isPrefix() {
        return prefix;
    }
}
