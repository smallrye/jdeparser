package org.jboss.jdeparser.impl;

import java.io.IOException;

import org.jboss.jdeparser.JType;

/**
 * Represents a Java array type, wrapping an element type.
 * <p>
 * Array types cannot be parameterized with type arguments.  Both
 * {@link #box()} and {@link #unbox()} return {@code this}, since
 * array types are reference types that have no primitive/boxed duality.
 */
public final class ArrayJType extends AbstractJType {

    /** The element type of this array. */
    private final JType elementType;

    /**
     * Constructs a new array type with the given element type.
     *
     * @param elementType the element type of the array (e.g., {@code int} for {@code int[]})
     */
    public ArrayJType(final JType elementType) {
        this.elementType = elementType;
    }

    /**
     * Returns the element type of this array.
     *
     * @return the element type
     */
    public JType elementType() {
        return elementType;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException always, since array types cannot have type arguments
     */
    @Override
    public JType typeArg(final JType... args) {
        throw new IllegalStateException("Array types cannot have type arguments");
    }

    /**
     * {@inheritDoc}
     * <p>
     * Array types are already reference types, so boxing returns {@code this}.
     */
    @Override
    public JType box() {
        return this;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Array types do not have a primitive counterpart, so unboxing returns {@code this}.
     */
    @Override
    public JType unbox() {
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public void write(SourceFileWriter writer) throws IOException {
        AbstractJExpr.writeType(writer, elementType);
        writer.write(Tokens.$BRACKET.OPEN);
        writer.write(Tokens.$BRACKET.CLOSE);
    }
}
