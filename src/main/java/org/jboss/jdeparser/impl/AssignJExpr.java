package org.jboss.jdeparser.impl;

import java.io.IOException;

import org.jboss.jdeparser.JExpr;

/**
 * An assignment expression, covering both simple ({@code =}) and compound
 * ({@code +=}, {@code -=}, {@code *=}, etc.) assignment operators.
 * <p>
 * Assignment expressions have {@link Prec#ASSIGNMENT} precedence and
 * {@link Assoc#RIGHT right-to-left} associativity, matching the Java
 * language specification.
 */
public final class AssignJExpr extends AbstractJExpr {

    /** The assignment target (left-hand side). */
    private final JExpr target;

    /** The assignment operator token. */
    private final Tokens.$BINOP token;

    /** The value being assigned (right-hand side). */
    private final JExpr value;

    /**
     * Constructs a new assignment expression.
     *
     * @param target the assignment target (never {@code null})
     * @param token  the operator token (never {@code null})
     * @param value  the value to assign (never {@code null})
     */
    public AssignJExpr(final JExpr target, final Tokens.$BINOP token, final JExpr value) {
        this.target = target;
        this.token = token;
        this.value = value;
    }

    /** {@inheritDoc} */
    @Override
    public Prec precedence() {
        return Prec.ASSIGNMENT;
    }

    /** {@inheritDoc} */
    @Override
    public Assoc associativity() {
        return Assoc.RIGHT;
    }

    /** {@inheritDoc} */
    @Override
    public void write(final SourceFileWriter writer) throws IOException {
        writeSubExpr(writer, target, Prec.ASSIGNMENT, Assoc.RIGHT, Assoc.LEFT);
        writer.write(token);
        writeSubExpr(writer, value, Prec.ASSIGNMENT, Assoc.RIGHT, Assoc.RIGHT);
    }

    /**
     * Returns the assignment target.
     *
     * @return the left-hand side expression (never {@code null})
     */
    public JExpr target() {
        return target;
    }

    /**
     * Returns the assignment operator token.
     *
     * @return the operator token (never {@code null})
     */
    public Tokens.$BINOP token() {
        return token;
    }

    /**
     * Returns the value being assigned.
     *
     * @return the right-hand side expression (never {@code null})
     */
    public JExpr value() {
        return value;
    }
}
