package org.jboss.jdeparser.impl;

import java.io.IOException;

import org.jboss.jdeparser.JExpr;

/**
 * An expression representing an array element access (e.g. {@code expr[index]}).
 * <p>
 * This is an assignable expression (it extends {@link AbstractJVar}),
 * so it can appear on the left-hand side of an assignment.
 */
public final class ArrayLookupJExpr extends AbstractJVar {

    private final JExpr array;
    private final JExpr index;

    /**
     * Constructs a new array element access expression.
     *
     * @param array the array expression (must not be {@code null})
     * @param index the index expression (must not be {@code null})
     */
    public ArrayLookupJExpr(final JExpr array, final JExpr index) {
        this.array = array;
        this.index = index;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link Prec#POSTFIX}
     */
    @Override
    public Prec precedence() {
        return Prec.POSTFIX;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link Assoc#LEFT}
     */
    @Override
    public Assoc associativity() {
        return Assoc.LEFT;
    }

    /** {@inheritDoc} */
    @Override
    public void write(final SourceFileWriter writer) throws IOException {
        // array[index]
        writeSubExpr(writer, array, Prec.POSTFIX, Assoc.LEFT, Assoc.LEFT);
        writer.write(Tokens.$BRACKET.OPEN);
        writeExpr(writer, index);
        writer.write(Tokens.$BRACKET.CLOSE);
    }

    /**
     * Returns the array expression being indexed.
     *
     * @return the array expression (never {@code null})
     */
    public JExpr array() {
        return array;
    }

    /**
     * Returns the index expression.
     *
     * @return the index expression (never {@code null})
     */
    public JExpr index() {
        return index;
    }
}
