package org.jboss.jdeparser.impl;

import java.io.IOException;

import org.jboss.jdeparser.JExpr;
import org.jboss.jdeparser.JType;

/**
 * An {@code instanceof} expression, optionally with a pattern-matching binding variable.
 * <p>
 * Supports both the classic form {@code expr instanceof Type} and the
 * pattern-matching form {@code expr instanceof Type name} introduced in
 * Java 16.  The presence of a binding variable is indicated by
 * {@link #hasBindingVar()}.
 * <p>
 * The {@code instanceof} operator has {@link Prec#RELATIONAL} precedence
 * and is {@link Assoc#NONE non-associative}.
 */
public final class InstanceOfJExpr extends AbstractJExpr {

    /** The expression being tested. */
    private final JExpr operand;

    /** The type to test against. */
    private final JType type;

    /** The optional binding variable name, or {@code null} for classic {@code instanceof}. */
    private final String bindingVar;

    /**
     * Constructs a new {@code instanceof} expression.
     *
     * @param operand    the expression being tested (never {@code null})
     * @param type       the type to test against (never {@code null})
     * @param bindingVar the binding variable name, or {@code null} for the classic form
     */
    public InstanceOfJExpr(final JExpr operand, final JType type, final String bindingVar) {
        this.operand = operand;
        this.type = type;
        this.bindingVar = bindingVar;
    }

    /** {@inheritDoc} */
    @Override
    public Prec precedence() {
        return Prec.RELATIONAL;
    }

    /** {@inheritDoc} */
    @Override
    public Assoc associativity() {
        return Assoc.NONE;
    }

    /** {@inheritDoc} */
    @Override
    public void write(final SourceFileWriter writer) throws IOException {
        // expr instanceof Type [name]
        writeSubExpr(writer, operand, Prec.RELATIONAL, Assoc.NONE, Assoc.LEFT);
        writer.write(Tokens.$KW.INSTANCEOF);
        writeType(writer, type);
        if (bindingVar != null) {
            writer.sp();
            writer.writeName(bindingVar);
        }
    }

    /**
     * Returns the expression being tested.
     *
     * @return the operand (never {@code null})
     */
    public JExpr operand() {
        return operand;
    }

    /**
     * Returns the type to test against.
     *
     * @return the type (never {@code null})
     */
    public JType type() {
        return type;
    }

    /**
     * Returns whether this expression has a pattern-matching binding variable.
     *
     * @return {@code true} if a binding variable is present, {@code false} otherwise
     */
    public boolean hasBindingVar() {
        return bindingVar != null;
    }

    /**
     * Returns the binding variable name.
     *
     * @return the binding variable name, or {@code null} if not present
     */
    public String bindingVar() {
        return bindingVar;
    }
}
