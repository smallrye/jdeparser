package io.smallrye.jdeparser.impl;

import java.io.IOException;
import java.util.List;

import io.smallrye.common.constraint.Assert;
import io.smallrye.jdeparser.DocReference;
import io.smallrye.jdeparser.Expr;
import io.smallrye.jdeparser.Type;
import io.smallrye.jdeparser.Var;

/**
 * Abstract base implementation of {@link Type} that provides sensible defaults
 * for most type operations.
 * <p>
 * Concrete subclasses override methods whose default behavior is inappropriate
 * for their specific type kind (e.g., primitive types override {@link #typeArg}
 * to throw, since primitives cannot be parameterized).
 */
public abstract non-sealed class AbstractType implements Type, Writable {
    /**
     * Cache for array type.
     */
    private ArrayType arrayType;

    /**
     * Constructs a new abstract type.
     */
    protected AbstractType() {
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
    public Type array() {
        ArrayType arrayType = this.arrayType;
        if (arrayType == null) {
            arrayType = this.arrayType = new ArrayType(this);
        }
        return arrayType;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns a new parameterized type applying the given type arguments to this type.
     */
    @Override
    public Type typeArg(final List<Type> args) {
        Assert.checkNotNullParam("args", args);
        return new NarrowedType(this, args);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Reference types are already boxed, so this returns {@code this}.
     */
    @Override
    public Type box() {
        return this;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Reference types do not unbox by default, so this returns {@code this}.
     */
    @Override
    public Type unbox() {
        return this;
    }

    /**
     * {@inheritDoc}
     * <p>
     * By default, the erasure of a type is itself.
     */
    @Override
    public Type erasure() {
        return this;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns a new upper-bounded wildcard type ({@code ? extends this}).
     */
    @Override
    public Type wildcardExtends() {
        return new WildcardType(true, this);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns a new lower-bounded wildcard type ({@code ? super this}).
     */
    @Override
    public Type wildcardSuper() {
        return new WildcardType(false, this);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns a new nested (inner) type within this enclosing type.
     */
    @Override
    public Type nestedType(final String name) {
        Assert.checkNotNullParam("name", name);
        Assert.checkNotEmptyParam("name", name);
        return new NestedType(this, name);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns a field access expression on this type.
     */
    @Override
    public Var field(final String name) {
        Assert.checkNotNullParam("name", name);
        Assert.checkNotEmptyParam("name", name);
        return new TypeFieldRefVar(this, name);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns a static method call expression on this type.
     */
    @Override
    public Expr call(final String name, final List<Expr> args) {
        Assert.checkNotNullParam("name", name);
        Assert.checkNotEmptyParam("name", name);
        Assert.checkNotNullParam("args", args);
        return new TypeCallExpr(this, name, args);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns a class literal expression ({@code Type.class}) for this type.
     */
    @Override
    public Expr class_() {
        return new ClassLiteralExpr(this);
    }

    @Override
    public Expr this_() {
        throw new UnsupportedOperationException("`this_` may not be called on this kind of type");
    }

    @Override
    public Expr super_() {
        throw new UnsupportedOperationException("`super_` may not be called on this kind of type");
    }

    public Expr newArrayInit(final List<Expr> elements) {
        throw new UnsupportedOperationException("`newArrayInit` may not be called on this kind of type");
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns a doc reference combining this type with a member identifier.
     */
    @Override
    public DocReference docRef(final String member) {
        Assert.checkNotNullParam("member", member);
        Assert.checkNotEmptyParam("member", member);
        return new DocReferenceImpl(this, member);
    }
}
