package org.jboss.jdeparser.impl;

import java.io.IOException;

import org.jboss.jdeparser.JExpr;

/**
 * A parenthesized expression of the form {@code (inner)}.
 * <p>
 * Wrapping an expression in parentheses elevates it to
 * {@link Prec#PRIMARY} precedence, preventing any surrounding
 * operator from breaking into the grouping.
 */
public final class ParenJExpr extends AbstractJExpr {

    /** The inner expression wrapped by the parentheses. */
    private final JExpr inner;

    /**
     * Constructs a new parenthesized expression.
     *
     * @param inner the expression to wrap (never {@code null})
     */
    public ParenJExpr(final JExpr inner) {
        this.inner = inner;
    }

    /** {@inheritDoc} */
    @Override
    public Prec precedence() {
        return Prec.PRIMARY;
    }

    /** {@inheritDoc} */
    @Override
    public Assoc associativity() {
        return Assoc.NONE;
    }

    /** {@inheritDoc} */
    @Override
    public void write(final SourceFileWriter writer) throws IOException {
        writer.write(Tokens.$PAREN.OPEN);
        writeExpr(writer, inner);
        writer.write(Tokens.$PAREN.CLOSE);
    }

    /**
     * Returns the inner expression.
     *
     * @return the wrapped expression (never {@code null})
     */
    public JExpr inner() {
        return inner;
    }
}
