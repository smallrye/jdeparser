package org.jboss.jdeparser.impl;

import java.io.IOException;

import org.jboss.jdeparser.JType;

/**
 * An expression representing a static field access on a type
 * (e.g. {@code TypeName.fieldName}).
 * <p>
 * This is an assignable expression (it extends {@link AbstractJVar}),
 * so it can appear on the left-hand side of an assignment.
 */
public final class TypeFieldRefJExpr extends AbstractJVar {

    private final JType type;
    private final String name;

    /**
     * Constructs a new static field reference expression.
     *
     * @param type the type on which the field is accessed (must not be {@code null})
     * @param name the field name (must not be {@code null})
     */
    public TypeFieldRefJExpr(final JType type, final String name) {
        this.type = type;
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
        // TypeName.fieldName
        writeType(writer, type);
        writer.write(Tokens.$PUNCT.DOT);
        writer.writeName(name);
    }

    /**
     * Returns the type on which the static field is accessed.
     *
     * @return the type (never {@code null})
     */
    public JType type() {
        return type;
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
