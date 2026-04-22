package org.jboss.jdeparser.impl;

import java.io.IOException;
import java.util.List;

import org.jboss.jdeparser.JExpr;
import org.jboss.jdeparser.JType;
import org.jboss.jdeparser.JVar;
import org.jboss.jdeparser.format.FormatPreferences;

/**
 * Represents an intersection type ({@code A & B}), used in cast expressions
 * and type parameter bounds.
 * <p>
 * Intersection types have restricted usage in Java: they may only appear
 * in cast expressions and as upper bounds of type parameters.  Consequently,
 * most type operations are overridden to throw {@link IllegalStateException}.
 * <p>
 * The constructor validates that at least two types are provided and that
 * none of the constituent types are themselves intersection types (no nesting).
 */
public final class IntersectionJType extends AbstractJType {

    /** The constituent types, stored as an unmodifiable list. */
    private final List<JType> types;

    /**
     * Constructs a new intersection type from the given constituent types.
     *
     * @param types the constituent types (defensively copied to an unmodifiable list)
     * @throws IllegalArgumentException if fewer than two types are provided,
     *                                  or if any of the types is itself an {@link IntersectionJType}
     */
    public IntersectionJType(final List<JType> types) {
        if (types.size() < 2) {
            throw new IllegalArgumentException("Intersection types require at least 2 types");
        }
        for (JType type : types) {
            if (type instanceof IntersectionJType) {
                throw new IllegalArgumentException("Intersection types cannot be nested");
            }
        }
        this.types = List.copyOf(types);
    }

    /**
     * Returns the constituent types as an unmodifiable list.
     *
     * @return the types forming this intersection (at least two elements)
     */
    public List<JType> types() {
        return types;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException always, since intersection types cannot form array types
     */
    @Override
    public JType array() {
        throw new IllegalStateException("Intersection types cannot form array types");
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException always, since intersection types cannot have type arguments
     */
    @Override
    public JType typeArg(final JType... args) {
        throw new IllegalStateException("Intersection types cannot have type arguments");
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException always, since intersection types cannot be boxed
     */
    @Override
    public JType box() {
        throw new IllegalStateException("Intersection types cannot be boxed");
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException always, since intersection types cannot be unboxed
     */
    @Override
    public JType unbox() {
        throw new IllegalStateException("Intersection types cannot be unboxed");
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException always, since intersection types do not have a single erasure
     */
    @Override
    public JType erasure() {
        throw new IllegalStateException("Intersection types do not have a single erasure");
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException always, since intersection types cannot be used as wildcard bounds
     */
    @Override
    public JType wildcardExtends() {
        throw new IllegalStateException("Intersection types cannot be used as wildcard bounds");
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException always, since intersection types cannot be used as wildcard bounds
     */
    @Override
    public JType wildcardSuper() {
        throw new IllegalStateException("Intersection types cannot be used as wildcard bounds");
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException always, since intersection types cannot have nested types
     */
    @Override
    public JType nestedType(final String name) {
        throw new IllegalStateException("Intersection types cannot have nested types");
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException always, since intersection types do not have fields
     */
    @Override
    public JVar field(final String name) {
        throw new IllegalStateException("Intersection types do not have fields");
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException always, since intersection types do not have methods
     */
    @Override
    public JExpr call(final String name, final List<JExpr> args) {
        throw new IllegalStateException("Intersection types do not have methods");
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException always, since intersection types cannot have class literals
     */
    @Override
    public JExpr classLiteral() {
        throw new IllegalStateException("Intersection types cannot have class literals");
    }

    /** {@inheritDoc} */
    @Override
    public void write(SourceFileWriter writer) throws IOException {
        boolean first = true;
        for (JType type : types) {
            if (!first) {
                writer.write(FormatPreferences.Space.AROUND_TYPE_BOUND_AND);
                writer.writeEscaped('&');
                writer.write(FormatPreferences.Space.AROUND_TYPE_BOUND_AND);
            }
            first = false;
            AbstractJExpr.writeType(writer, type);
        }
    }
}
