package org.jboss.jdeparser;

import java.util.List;

import org.jboss.jdeparser.impl.AbstractJType;
import org.jboss.jdeparser.impl.IntersectionJType;
import org.jboss.jdeparser.impl.PrimitiveJType;
import org.jboss.jdeparser.impl.ReferenceJType;

/**
 * The core type interface for representing Java types in generated source code.
 * <p>
 * Types are composable: methods such as {@link #array()}, {@link #typeArg(JType...)}, and {@link #nestedType(String)}
 * return new {@link JType} instances representing the derived type. Predefined constants are available for
 * all primitive types and for commonly used reference types.
 */
public sealed interface JType permits AbstractJType {

    /**
     * The primitive type {@code void}.
     */
    JType VOID = PrimitiveJType.VOID;

    /**
     * The primitive type {@code boolean}.
     */
    JType BOOLEAN = PrimitiveJType.BOOLEAN;

    /**
     * The primitive type {@code byte}.
     */
    JType BYTE = PrimitiveJType.BYTE;

    /**
     * The primitive type {@code short}.
     */
    JType SHORT = PrimitiveJType.SHORT;

    /**
     * The primitive type {@code int}.
     */
    JType INT = PrimitiveJType.INT;

    /**
     * The primitive type {@code long}.
     */
    JType LONG = PrimitiveJType.LONG;

    /**
     * The primitive type {@code float}.
     */
    JType FLOAT = PrimitiveJType.FLOAT;

    /**
     * The primitive type {@code double}.
     */
    JType DOUBLE = PrimitiveJType.DOUBLE;

    /**
     * The primitive type {@code char}.
     */
    JType CHAR = PrimitiveJType.CHAR;

    /**
     * The reference type {@code java.lang.String}.
     */
    JType STRING = ReferenceJType.STRING;

    /**
     * The reference type {@code java.lang.Object}.
     */
    JType OBJECT = ReferenceJType.OBJECT;

    /**
     * Returns a type representing an array of this type ({@code this[]}).
     *
     * @return the array type
     */
    JType array();

    /**
     * Returns a parameterized type by applying the given type arguments to this type
     * (e.g., {@code This<A, B>}).
     *
     * @param args the type arguments to apply
     * @return the parameterized type
     */
    JType typeArg(JType... args);

    /**
     * Returns the boxed (wrapper) type corresponding to this primitive type.
     * For example, {@code int} becomes {@code java.lang.Integer}.
     *
     * @return the boxed type
     */
    JType box();

    /**
     * Returns the unboxed (primitive) type corresponding to this wrapper type.
     * For example, {@code java.lang.Integer} becomes {@code int}.
     *
     * @return the unboxed type
     */
    JType unbox();

    /**
     * Returns the type erasure of this type, stripping all type arguments.
     *
     * @return the erased type
     */
    JType erasure();

    /**
     * Returns a wildcard type with an upper bound of this type ({@code ? extends This}).
     *
     * @return the upper-bounded wildcard type
     */
    JType wildcardExtends();

    /**
     * Returns a wildcard type with a lower bound of this type ({@code ? super This}).
     *
     * @return the lower-bounded wildcard type
     */
    JType wildcardSuper();

    /**
     * Returns a nested (inner) type within this type ({@code This.Name}).
     *
     * @param name the simple name of the nested type
     * @return the nested type
     */
    JType nestedType(String name);

    /**
     * Returns a variable expression representing a static field reference on this type ({@code This.name}).
     *
     * @param name the field name
     * @return the static field access variable expression
     */
    JVar field(String name);

    /**
     * Returns an expression representing a static method call on this type ({@code This.name(args...)}).
     *
     * @param name the method name
     * @param args the method arguments
     * @return the static method call expression
     */
    JExpr call(String name, JExpr... args);

    /**
     * Returns an expression representing a static method call on this type ({@code This.name(args...)}).
     *
     * @param name the method name
     * @param args the method arguments as a list
     * @return the static method call expression
     */
    JExpr call(String name, List<JExpr> args);

    /**
     * Returns an expression representing the class literal of this type ({@code This.class}).
     *
     * @return the class literal expression
     */
    JExpr classLiteral();

    /**
     * Returns an intersection type composed of all the given types ({@code A & B & C}).
     *
     * @param types the types to intersect
     * @return the intersection type
     */
    static JType allOf(JType... types) {
        return new IntersectionJType(List.of(types));
    }
}
