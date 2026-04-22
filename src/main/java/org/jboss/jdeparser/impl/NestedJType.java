package org.jboss.jdeparser.impl;

import java.io.IOException;

import org.jboss.jdeparser.JType;

/**
 * Represents a nested (inner or static member) type reference,
 * such as {@code Outer.Inner}.
 * <p>
 * A nested type is formed by qualifying an inner type name with its
 * enclosing type.  Standard type operations like {@link #array()},
 * {@link #typeArg}, and {@link #nestedType} are inherited from
 * {@link AbstractJType} and work as expected.
 */
public final class NestedJType extends AbstractJType {

    /** The enclosing (outer) type. */
    private final JType outer;

    /** The simple name of the nested type. */
    private final String name;

    /**
     * Constructs a new nested type reference.
     *
     * @param outer the enclosing type
     * @param name  the simple name of the nested type (e.g., {@code "Entry"})
     */
    public NestedJType(final JType outer, final String name) {
        this.outer = outer;
        this.name = name;
    }

    /**
     * Returns the enclosing (outer) type.
     *
     * @return the outer type
     */
    public JType outer() {
        return outer;
    }

    /**
     * Returns the simple name of the nested type.
     *
     * @return the nested type name (e.g., {@code "Entry"})
     */
    public String name() {
        return name;
    }

    /** {@inheritDoc} */
    @Override
    public void write(SourceFileWriter writer) throws IOException {
        AbstractJExpr.writeType(writer, outer);
        writer.write(Tokens.$PUNCT.DOT);
        writer.writeClass(name);
    }
}
