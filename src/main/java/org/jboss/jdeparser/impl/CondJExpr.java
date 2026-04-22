package org.jboss.jdeparser.impl;

import java.io.IOException;

import org.jboss.jdeparser.JExpr;
import org.jboss.jdeparser.format.FormatPreferences;

/**
 * A ternary conditional expression of the form {@code condition ? ifTrue : ifFalse}.
 * <p>
 * The ternary operator has {@link Prec#TERNARY} precedence and
 * {@link Assoc#RIGHT right-to-left} associativity, matching the Java
 * language specification.
 */
public final class CondJExpr extends AbstractJExpr {

    /** The condition expression. */
    private final JExpr condition;

    /** The expression evaluated when the condition is {@code true}. */
    private final JExpr ifTrue;

    /** The expression evaluated when the condition is {@code false}. */
    private final JExpr ifFalse;

    /**
     * Constructs a new ternary conditional expression.
     *
     * @param condition the condition expression (never {@code null})
     * @param ifTrue    the true-branch expression (never {@code null})
     * @param ifFalse   the false-branch expression (never {@code null})
     */
    public CondJExpr(final JExpr condition, final JExpr ifTrue, final JExpr ifFalse) {
        this.condition = condition;
        this.ifTrue = ifTrue;
        this.ifFalse = ifFalse;
    }

    /** {@inheritDoc} */
    @Override
    public Prec precedence() {
        return Prec.TERNARY;
    }

    /** {@inheritDoc} */
    @Override
    public Assoc associativity() {
        return Assoc.RIGHT;
    }

    /** {@inheritDoc} */
    @Override
    public void write(final SourceFileWriter writer) throws IOException {
        // condition ? ifTrue : ifFalse
        writeSubExpr(writer, condition, Prec.TERNARY, Assoc.RIGHT, Assoc.LEFT);
        writer.write(FormatPreferences.Space.BEFORE_TERNARY_Q);
        writer.write(Tokens.$PUNCT.Q);
        writer.write(FormatPreferences.Space.AFTER_TERNARY_Q);
        writeSubExpr(writer, ifTrue, Prec.TERNARY, Assoc.RIGHT, Assoc.RIGHT);
        writer.write(FormatPreferences.Space.BEFORE_TERNARY_COLON);
        writer.write(Tokens.$PUNCT.COLON);
        writer.write(FormatPreferences.Space.AFTER_TERNARY_COLON);
        writeSubExpr(writer, ifFalse, Prec.TERNARY, Assoc.RIGHT, Assoc.RIGHT);
    }

    /**
     * Returns the condition expression.
     *
     * @return the condition (never {@code null})
     */
    public JExpr condition() {
        return condition;
    }

    /**
     * Returns the true-branch expression.
     *
     * @return the expression for the {@code true} case (never {@code null})
     */
    public JExpr ifTrue() {
        return ifTrue;
    }

    /**
     * Returns the false-branch expression.
     *
     * @return the expression for the {@code false} case (never {@code null})
     */
    public JExpr ifFalse() {
        return ifFalse;
    }
}
