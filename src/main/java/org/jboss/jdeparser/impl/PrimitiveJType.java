package org.jboss.jdeparser.impl;

import java.io.IOException;
import java.util.List;

import org.jboss.jdeparser.JExpr;
import org.jboss.jdeparser.JType;
import org.jboss.jdeparser.JVar;

/**
 * Represents a Java primitive type ({@code int}, {@code boolean}, {@code void}, etc.).
 * <p>
 * Instances are pre-allocated as static constants and should be accessed via
 * those constants rather than being constructed directly.  Most type operations
 * that are only meaningful for reference types (e.g., {@link #typeArg},
 * {@link #nestedType}, {@link #field}) throw {@link IllegalStateException}.
 */
public final class PrimitiveJType extends AbstractJType {

    /** The {@code void} pseudo-type. */
    public static final PrimitiveJType VOID = new PrimitiveJType("void", null);

    /** The {@code boolean} primitive type. */
    public static final PrimitiveJType BOOLEAN = new PrimitiveJType("boolean", "java.lang.Boolean");

    /** The {@code byte} primitive type. */
    public static final PrimitiveJType BYTE = new PrimitiveJType("byte", "java.lang.Byte");

    /** The {@code short} primitive type. */
    public static final PrimitiveJType SHORT = new PrimitiveJType("short", "java.lang.Short");

    /** The {@code int} primitive type. */
    public static final PrimitiveJType INT = new PrimitiveJType("int", "java.lang.Integer");

    /** The {@code long} primitive type. */
    public static final PrimitiveJType LONG = new PrimitiveJType("long", "java.lang.Long");

    /** The {@code float} primitive type. */
    public static final PrimitiveJType FLOAT = new PrimitiveJType("float", "java.lang.Float");

    /** The {@code double} primitive type. */
    public static final PrimitiveJType DOUBLE = new PrimitiveJType("double", "java.lang.Double");

    /** The {@code char} primitive type. */
    public static final PrimitiveJType CHAR = new PrimitiveJType("char", "java.lang.Character");

    /** The Java keyword for this primitive type (e.g., {@code "int"}). */
    private final String keyword;

    /**
     * The fully qualified name of the corresponding wrapper class,
     * or {@code null} for {@link #VOID} which has no boxed form.
     */
    private final String boxedName;

    /**
     * Constructs a new primitive type representation.
     *
     * @param keyword   the Java keyword (e.g., {@code "int"})
     * @param boxedName the qualified name of the wrapper class (e.g., {@code "java.lang.Integer"}),
     *                  or {@code null} for {@code void}
     */
    private PrimitiveJType(final String keyword, final String boxedName) {
        this.keyword = keyword;
        this.boxedName = boxedName;
    }

    /**
     * Returns the Java keyword for this primitive type.
     *
     * @return the keyword string (e.g., {@code "int"}, {@code "boolean"})
     */
    public String keyword() {
        return keyword;
    }

    /**
     * {@inheritDoc}
     * <p>
     * If this primitive has a corresponding wrapper class, returns a new
     * {@link ReferenceJType} for that wrapper; otherwise (for {@code void})
     * returns {@code this}.
     */
    @Override
    public JType box() {
        if (boxedName != null) {
            return new ReferenceJType(boxedName);
        }
        return this;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Primitive types are already unboxed, so this returns {@code this}.
     */
    @Override
    public JType unbox() {
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException always, since primitive types cannot have type arguments
     */
    @Override
    public JType typeArg(final JType... args) {
        throw new IllegalStateException("Primitive types cannot have type arguments");
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException always, since primitive types cannot have nested types
     */
    @Override
    public JType nestedType(final String name) {
        throw new IllegalStateException("Primitive types cannot have nested types");
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException always, since primitive types cannot be used as wildcard bounds
     */
    @Override
    public JType wildcardExtends() {
        throw new IllegalStateException("Primitive types cannot be used as wildcard bounds");
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException always, since primitive types cannot be used as wildcard bounds
     */
    @Override
    public JType wildcardSuper() {
        throw new IllegalStateException("Primitive types cannot be used as wildcard bounds");
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException always, since primitive types do not have fields
     */
    @Override
    public JVar field(final String name) {
        throw new IllegalStateException("Primitive types do not have fields");
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException always, since primitive types do not have methods
     */
    @Override
    public JExpr call(final String name, final List<JExpr> args) {
        throw new IllegalStateException("Primitive types do not have methods");
    }

    /** {@inheritDoc} */
    @Override
    public void write(SourceFileWriter writer) throws IOException {
        writer.write(switch (keyword) {
            case "void" -> Tokens.$KW.VOID;
            case "boolean" -> Tokens.$KW.BOOLEAN;
            case "byte" -> Tokens.$KW.BYTE;
            case "short" -> Tokens.$KW.SHORT;
            case "int" -> Tokens.$KW.INT;
            case "long" -> Tokens.$KW.LONG;
            case "float" -> Tokens.$KW.FLOAT;
            case "double" -> Tokens.$KW.DOUBLE;
            case "char" -> Tokens.$KW.CHAR;
            default -> throw new AssertionError("Unknown primitive: " + keyword);
        });
    }
}
