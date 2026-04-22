package org.jboss.jdeparser.impl;

import java.io.IOException;
import java.util.List;

import org.jboss.jdeparser.JExpr;
import org.jboss.jdeparser.JType;
import org.jboss.jdeparser.JVar;

/**
 * Abstract base implementation of {@link JType} that provides sensible defaults
 * for most type operations.
 * <p>
 * Concrete subclasses override methods whose default behavior is inappropriate
 * for their specific type kind (e.g., primitive types override {@link #typeArg}
 * to throw, since primitives cannot be parameterized).
 */
public abstract non-sealed class AbstractJType implements JType, Writable {

    /**
     * Constructs a new abstract type.
     */
    protected AbstractJType() {
    }

    /**
     * Writes this type to the given source file writer.
     *
     * @param writer the writer to emit source code to
     * @throws IOException if an I/O error occurs
     */
    @Override
    public abstract void write(SourceFileWriter writer) throws IOException;

    /**
     * {@inheritDoc}
     * <p>
     * Returns a new array type whose element type is this type.
     */
    @Override
    public JType array() {
        return new ArrayJType(this);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns a new parameterized type applying the given type arguments to this type.
     */
    @Override
    public JType typeArg(final JType... args) {
        return new NarrowedJType(this, List.of(args));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Reference types are already boxed, so this returns {@code this}.
     */
    @Override
    public JType box() {
        return this;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Reference types do not unbox by default, so this returns {@code this}.
     */
    @Override
    public JType unbox() {
        return this;
    }

    /**
     * {@inheritDoc}
     * <p>
     * By default, the erasure of a type is itself.
     */
    @Override
    public JType erasure() {
        return this;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns a new upper-bounded wildcard type ({@code ? extends this}).
     */
    @Override
    public JType wildcardExtends() {
        return new WildcardJType(true, this);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns a new lower-bounded wildcard type ({@code ? super this}).
     */
    @Override
    public JType wildcardSuper() {
        return new WildcardJType(false, this);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns a new nested (inner) type within this enclosing type.
     */
    @Override
    public JType nestedType(final String name) {
        return new NestedJType(this, name);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns a field access expression on this type.
     */
    @Override
    public JVar field(final String name) {
        return new TypeFieldRefJExpr(this, name);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Delegates to {@link #call(String, List)} after wrapping the varargs array.
     */
    @Override
    public JExpr call(final String name, final JExpr... args) {
        return call(name, List.of(args));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns a static method call expression on this type.
     */
    @Override
    public JExpr call(final String name, final List<JExpr> args) {
        return new TypeCallJExpr(this, name, args);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns a class literal expression ({@code Type.class}) for this type.
     */
    @Override
    public JExpr classLiteral() {
        return new ClassLiteralJExpr(this);
    }
}
