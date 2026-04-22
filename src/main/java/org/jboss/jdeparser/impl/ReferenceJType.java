package org.jboss.jdeparser.impl;

import java.io.IOException;

import org.jboss.jdeparser.JType;

/**
 * Represents a qualified reference (class or interface) type, such as
 * {@code java.lang.String} or {@code com.example.MyClass}.
 * <p>
 * This type supports unboxing: if the qualified name corresponds to one of the
 * standard wrapper classes in {@code java.lang}, {@link #unbox()} returns the
 * matching {@link PrimitiveJType} constant.
 */
public final class ReferenceJType extends AbstractJType {

    /** The {@code java.lang.String} type. */
    public static final ReferenceJType STRING = new ReferenceJType("java.lang.String");

    /** The {@code java.lang.Object} type. */
    public static final ReferenceJType OBJECT = new ReferenceJType("java.lang.Object");

    /** The fully qualified class name (e.g., {@code "java.lang.String"}). */
    private final String qualifiedName;

    /**
     * Constructs a new reference type for the given qualified class name.
     *
     * @param qualifiedName the fully qualified class name (e.g., {@code "java.lang.String"})
     */
    public ReferenceJType(final String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

    /**
     * Returns the fully qualified class name.
     *
     * @return the qualified name (e.g., {@code "java.lang.String"})
     */
    public String qualifiedName() {
        return qualifiedName;
    }

    /**
     * Returns the simple (unqualified) class name, which is the portion
     * after the last {@code '.'} in the qualified name.
     *
     * @return the simple name (e.g., {@code "String"} for {@code "java.lang.String"})
     */
    public String simpleName() {
        int dot = qualifiedName.lastIndexOf('.');
        return dot < 0 ? qualifiedName : qualifiedName.substring(dot + 1);
    }

    /**
     * {@inheritDoc}
     * <p>
     * A reference type is its own erasure.
     */
    @Override
    public JType erasure() {
        return this;
    }

    /**
     * {@inheritDoc}
     * <p>
     * If this type represents a standard wrapper class (e.g., {@code java.lang.Integer}),
     * returns the corresponding {@link PrimitiveJType} constant.  Otherwise returns
     * {@code this}.
     */
    @Override
    public JType unbox() {
        return switch (qualifiedName) {
            case "java.lang.Boolean" -> PrimitiveJType.BOOLEAN;
            case "java.lang.Byte" -> PrimitiveJType.BYTE;
            case "java.lang.Short" -> PrimitiveJType.SHORT;
            case "java.lang.Integer" -> PrimitiveJType.INT;
            case "java.lang.Long" -> PrimitiveJType.LONG;
            case "java.lang.Float" -> PrimitiveJType.FLOAT;
            case "java.lang.Double" -> PrimitiveJType.DOUBLE;
            case "java.lang.Character" -> PrimitiveJType.CHAR;
            default -> this;
        };
    }

    /** {@inheritDoc} */
    @Override
    public void write(SourceFileWriter writer) throws IOException {
        writer.writeClass(qualifiedName);
    }
}
