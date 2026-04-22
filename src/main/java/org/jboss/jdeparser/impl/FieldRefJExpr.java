package org.jboss.jdeparser.impl;

import java.io.IOException;

import org.jboss.jdeparser.JExpr;

/**
 * An expression representing a field access on a receiver expression
 * (e.g. {@code expr.fieldName}).
 * <p>
 * This is an assignable expression (it extends {@link AbstractJVar}),
 * so it can appear on the left-hand side of an assignment.
 */
public final class FieldRefJExpr extends AbstractJVar {

    private final JExpr receiver;
    private final String name;

    /**
     * Constructs a new field reference expression.
     *
     * @param receiver the receiver expression on which the field is accessed
     *                 (must not be {@code null})
     * @param name     the field name (must not be {@code null})
     */
    public FieldRefJExpr(final JExpr receiver, final String name) {
        this.receiver = receiver;
        this.name = name;
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
        // receiver.fieldName
        writeSubExpr(writer, receiver, Prec.POSTFIX, Assoc.LEFT, Assoc.LEFT);
        writer.write(Tokens.$PUNCT.DOT);
        writer.writeName(name);
    }

    /**
     * Returns the receiver expression on which the field is accessed.
     *
     * @return the receiver expression (never {@code null})
     */
    public JExpr receiver() {
        return receiver;
    }

    /**
     * Returns the name of the accessed field.
     *
     * @return the field name (never {@code null})
     */
    public String name() {
        return name;
    }
}
