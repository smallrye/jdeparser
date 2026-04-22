package org.jboss.jdeparser.impl;

import java.io.IOException;

/**
 * An expression representing a local variable or parameter reference by name.
 * <p>
 * This is an assignable expression (it extends {@link AbstractJVar}),
 * so it can appear on the left-hand side of an assignment.
 */
public final class NameJExpr extends AbstractJVar {

    private final String name;

    /**
     * Constructs a new name expression.
     *
     * @param name the variable or parameter name (must not be {@code null})
     */
    public NameJExpr(final String name) {
        this.name = name;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link Prec#PRIMARY}
     */
    @Override
    public Prec precedence() {
        return Prec.PRIMARY;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link Assoc#NONE}
     */
    @Override
    public Assoc associativity() {
        return Assoc.NONE;
    }

    /**
     * Returns the variable or parameter name.
     *
     * @return the name (never {@code null})
     */
    public String name() {
        return name;
    }

    /** {@inheritDoc} */
    @Override
    public void write(final SourceFileWriter writer) throws IOException {
        writer.writeName(name);
    }
}
