package org.jboss.jdeparser.impl;

import java.io.IOException;
import java.util.List;

import org.jboss.jdeparser.JExpr;
import org.jboss.jdeparser.JType;
import org.jboss.jdeparser.JVar;

/**
 * Represents a wildcard type with a bound: either {@code ? extends X}
 * (upper-bounded) or {@code ? super X} (lower-bounded).
 * <p>
 * Wildcard types are used exclusively as type arguments and cannot be
 * used directly as types for most operations.  Consequently, most methods
 * inherited from {@link AbstractJType} are overridden to throw
 * {@link IllegalStateException}.
 */
public final class WildcardJType extends AbstractJType {

    /**
     * {@code true} for an upper-bounded wildcard ({@code ? extends X}),
     * {@code false} for a lower-bounded wildcard ({@code ? super X}).
     */
    private final boolean extends_;

    /** The bound type. */
    private final JType bound;

    /**
     * Constructs a new wildcard type.
     *
     * @param extends_ {@code true} for {@code ? extends bound},
     *                 {@code false} for {@code ? super bound}
     * @param bound    the bound type
     */
    public WildcardJType(final boolean extends_, final JType bound) {
        this.extends_ = extends_;
        this.bound = bound;
    }

    /**
     * Returns whether this is an upper-bounded wildcard ({@code ? extends X}).
     *
     * @return {@code true} for extends, {@code false} for super
     */
    public boolean isExtends() {
        return extends_;
    }

    /**
     * Returns the bound type.
     *
     * @return the bound type
     */
    public JType bound() {
        return bound;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException always, since wildcard types cannot form array types
     */
    @Override
    public JType array() {
        throw new IllegalStateException("Wildcard types cannot form array types");
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException always, since wildcard types cannot have type arguments
     */
    @Override
    public JType typeArg(final JType... args) {
        throw new IllegalStateException("Wildcard types cannot have type arguments");
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException always, since wildcard types cannot be boxed
     */
    @Override
    public JType box() {
        throw new IllegalStateException("Wildcard types cannot be boxed");
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException always, since wildcard types cannot be unboxed
     */
    @Override
    public JType unbox() {
        throw new IllegalStateException("Wildcard types cannot be unboxed");
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException always, since wildcard types do not have an erasure
     */
    @Override
    public JType erasure() {
        throw new IllegalStateException("Wildcard types do not have an erasure");
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException always, since wildcard types cannot be further bounded
     */
    @Override
    public JType wildcardExtends() {
        throw new IllegalStateException("Wildcard types cannot be further bounded");
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException always, since wildcard types cannot be further bounded
     */
    @Override
    public JType wildcardSuper() {
        throw new IllegalStateException("Wildcard types cannot be further bounded");
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException always, since wildcard types cannot have nested types
     */
    @Override
    public JType nestedType(final String name) {
        throw new IllegalStateException("Wildcard types cannot have nested types");
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException always, since wildcard types do not have fields
     */
    @Override
    public JVar field(final String name) {
        throw new IllegalStateException("Wildcard types do not have fields");
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException always, since wildcard types do not have methods
     */
    @Override
    public JExpr call(final String name, final List<JExpr> args) {
        throw new IllegalStateException("Wildcard types do not have methods");
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException always, since wildcard types cannot have class literals
     */
    @Override
    public JExpr classLiteral() {
        throw new IllegalStateException("Wildcard types cannot have class literals");
    }

    /** {@inheritDoc} */
    @Override
    public void write(SourceFileWriter writer) throws IOException {
        writer.write(Tokens.$PUNCT.Q);
        if (extends_) {
            writer.write(Tokens.$KW.EXTENDS);
        } else {
            writer.write(Tokens.$KW.SUPER);
        }
        AbstractJExpr.writeType(writer, bound);
    }
}
