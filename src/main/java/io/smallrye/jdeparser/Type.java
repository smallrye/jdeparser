package io.smallrye.jdeparser;

import java.util.List;
import java.util.function.Consumer;

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import io.smallrye.common.constraint.Assert;
import io.smallrye.jdeparser.creator.ClassCreator;
import io.smallrye.jdeparser.creator.DocCommentCreator;
import io.smallrye.jdeparser.impl.AbstractType;
import io.smallrye.jdeparser.impl.AnonymousClassExpr;
import io.smallrye.jdeparser.impl.ClassCreatorImpl;
import io.smallrye.jdeparser.impl.IntersectionType;
import io.smallrye.jdeparser.impl.MethodRefExpr;
import io.smallrye.jdeparser.impl.NewExpr;
import io.smallrye.jdeparser.impl.PrimitiveType;
import io.smallrye.jdeparser.impl.ReferenceType;
import io.smallrye.jdeparser.impl.WildcardType;

/**
 * The core type interface for representing Java types in generated source code.
 * <p>
 * Types are composable: methods such as {@link #array()}, {@link #typeArg(Type...)}, and {@link #nestedType(String)}
 * return new {@link Type} instances representing the derived type. Predefined constants are available for
 * all primitive types and for commonly used reference types.
 */
public sealed interface Type permits AbstractType {

    /**
     * The primitive type {@code void}.
     */
    Type VOID = PrimitiveType.VOID;

    /**
     * The primitive type {@code boolean}.
     */
    Type BOOLEAN = PrimitiveType.BOOLEAN;

    /**
     * The primitive type {@code byte}.
     */
    Type BYTE = PrimitiveType.BYTE;

    /**
     * The primitive type {@code short}.
     */
    Type SHORT = PrimitiveType.SHORT;

    /**
     * The primitive type {@code int}.
     */
    Type INT = PrimitiveType.INT;

    /**
     * The primitive type {@code long}.
     */
    Type LONG = PrimitiveType.LONG;

    /**
     * The primitive type {@code float}.
     */
    Type FLOAT = PrimitiveType.FLOAT;

    /**
     * The primitive type {@code double}.
     */
    Type DOUBLE = PrimitiveType.DOUBLE;

    /**
     * The primitive type {@code char}.
     */
    Type CHAR = PrimitiveType.CHAR;

    /**
     * The reference type {@code java.lang.String}.
     */
    Type STRING = ReferenceType.STRING;

    /**
     * The reference type {@code java.lang.Object}.
     */
    Type OBJECT = ReferenceType.OBJECT;

    /**
     * The unbounded wildcard type ({@code ?}).
     */
    Type WILDCARD = WildcardType.UNBOUNDED;

    /**
     * Creates a type from a {@link Class} object.
     *
     * @param clazz the class (must not be {@code null})
     * @return the corresponding type
     */
    static Type of(final Class<?> clazz) {
        Assert.checkNotNullParam("clazz", clazz);
        if (clazz.isPrimitive()) {
            if (clazz == void.class)
                return VOID;
            if (clazz == boolean.class)
                return BOOLEAN;
            if (clazz == byte.class)
                return BYTE;
            if (clazz == short.class)
                return SHORT;
            if (clazz == int.class)
                return INT;
            if (clazz == long.class)
                return LONG;
            if (clazz == float.class)
                return FLOAT;
            if (clazz == double.class)
                return DOUBLE;
            if (clazz == char.class)
                return CHAR;
            throw new IllegalArgumentException("Unknown primitive type: " + clazz);
        }
        if (clazz.isArray()) {
            return of(clazz.getComponentType()).array();
        }
        return new ReferenceType(clazz.getCanonicalName());
    }

    /**
     * Creates a type from a fully qualified class name string or type variable name.
     *
     * @param qualifiedName the fully qualified class name (e.g., {@code "com.example.MyClass"})
     *        or type variable name (e.g., {@code "T"}) (must not be {@code null} or empty)
     * @return the corresponding type
     */
    static Type named(final String qualifiedName) {
        Assert.checkNotNullParam("qualifiedName", qualifiedName);
        Assert.checkNotEmptyParam("qualifiedName", qualifiedName);
        return new ReferenceType(qualifiedName);
    }

    /**
     * Creates a type from a {@link TypeMirror}, typically used in annotation processors.
     *
     * @param mirror the type mirror (must not be {@code null})
     * @return the corresponding type
     * @throws IllegalArgumentException if the mirror kind is not supported
     */
    static Type of(final TypeMirror mirror) {
        Assert.checkNotNullParam("mirror", mirror);
        return switch (mirror.getKind()) {
            case VOID, NONE -> VOID;
            case BOOLEAN -> BOOLEAN;
            case BYTE -> BYTE;
            case SHORT -> SHORT;
            case INT -> INT;
            case LONG -> LONG;
            case FLOAT -> FLOAT;
            case DOUBLE -> DOUBLE;
            case CHAR -> CHAR;
            case ARRAY -> of(((ArrayType) mirror).getComponentType()).array();
            case DECLARED -> {
                var declared = (DeclaredType) mirror;
                var raw = new ReferenceType(
                        declared.asElement().toString());
                var typeArgs = declared.getTypeArguments();
                if (typeArgs.isEmpty()) {
                    yield raw;
                }
                yield raw.typeArg(typeArgs.stream().map(Type::of).toArray(Type[]::new));
            }
            default -> throw new IllegalArgumentException("Unsupported type mirror kind: " + mirror.getKind());
        };
    }

    /**
     * Returns a type representing an array of this type ({@code this[]}).
     *
     * @return the array type
     */
    Type array();

    /**
     * Returns a parameterized type by applying the given type arguments to this type
     * (e.g., {@code This<A, B>}).
     *
     * @param args the type arguments to apply
     * @return the parameterized type
     */
    default Type typeArg(Type... args) {
        return typeArg(List.of(args));
    }

    /**
     * Returns a parameterized type by applying the given type arguments to this type
     * (e.g., {@code This<A, B>}).
     * If the list is empty, a "diamond operator" will be produced.
     *
     * @param args the type arguments to apply as a list
     * @return the parameterized type
     */
    Type typeArg(List<Type> args);

    /**
     * Returns the boxed (wrapper) type corresponding to this primitive type.
     * For example, {@code int} becomes {@code java.lang.Integer}.
     *
     * @return the boxed type
     */
    Type box();

    /**
     * Returns the unboxed (primitive) type corresponding to this wrapper type.
     * For example, {@code java.lang.Integer} becomes {@code int}.
     *
     * @return the unboxed type
     */
    Type unbox();

    /**
     * Returns the type erasure of this type, stripping all type arguments.
     *
     * @return the erased type
     */
    Type erasure();

    /**
     * Returns a wildcard type with an upper bound of this type ({@code ? extends This}).
     *
     * @return the upper-bounded wildcard type
     */
    Type wildcardExtends();

    /**
     * Returns a wildcard type with a lower bound of this type ({@code ? super This}).
     *
     * @return the lower-bounded wildcard type
     */
    Type wildcardSuper();

    /**
     * Returns a nested (inner) type within this type ({@code This.Name}).
     *
     * @param name the simple name of the nested type
     * @return the nested type
     */
    Type nestedType(String name);

    /**
     * Returns a variable expression representing a static field reference on this type ({@code This.name}).
     *
     * @param name the field name
     * @return the static field access variable expression
     */
    Var field(String name);

    /**
     * Returns an expression representing a static method call on this type ({@code This.name(args...)}).
     *
     * @param name the method name
     * @param args the method arguments
     * @return the static method call expression
     */
    default Expr call(String name, Expr... args) {
        return call(name, List.of(args));
    }

    /**
     * Returns an expression representing a static method call on this type ({@code This.name(args...)}).
     *
     * @param name the method name
     * @param args the method arguments as a list
     * @return the static method call expression
     */
    Expr call(String name, List<Expr> args);

    /**
     * Returns an expression representing the class literal of this type ({@code This.class}).
     *
     * @return the class literal expression
     */
    Expr class_();

    /**
     * Creates a qualified {@code this} expression: {@code Type.this}.
     *
     * @return the qualified this expression
     */
    Expr this_();

    /**
     * Creates a qualified {@code super} expression: {@code Type.super}.
     *
     * @return the qualified this expression
     */
    Expr super_();

    /**
     * {@return the number of dimensions of this type, if it is an array type, or 0 if it is not an array type}
     */
    default int dimensions() {
        return 0;
    }

    /**
     * Creates a constructor call: {@code new Type(args)}.
     * For array types, this creates a call of the form: {@code new ComponentType[arg0][arg1]...};
     * in this case, the number of arguments must be less than or equal to the number of array dimensions.
     *
     * @param args the constructor argument expressions
     * @return the new expression
     */
    default Expr new_(final Expr... args) {
        return new_(List.of(args));
    }

    /**
     * Creates a constructor call: {@code new Type(args)}.
     * For array types, this creates a call of the form: {@code new ComponentType[arg0][arg1]...};
     * in this case, the number of arguments must be less than or equal to the number of array dimensions.
     *
     * @param args the constructor argument expressions as a list
     * @return the new expression
     */
    default Expr new_(final List<Expr> args) {
        Assert.checkNotNullParam("args", args);
        return new NewExpr(this, args);
    }

    /**
     * Creates an anonymous class creation expression: {@code new Type(args) &#123; body &#125;}.
     *
     * @param version the source version for feature validation
     * @param args the constructor argument expressions
     * @param builder the anonymous class body builder
     * @return the anonymous class expression
     */
    default Expr new_(final SourceVersion version, final List<Expr> args,
            final Consumer<ClassCreator> builder) {
        Assert.checkNotNullParam("version", version);
        Assert.checkNotNullParam("args", args);
        Assert.checkNotNullParam("builder", builder);
        final ClassCreatorImpl cc = new ClassCreatorImpl(version, "", false);
        builder.accept(cc);
        cc.finish();
        return new AnonymousClassExpr(this, args, cc);
    }

    /**
     * Creates an array creation expression with an initializer: {@code new Type[] {e1, e2, ...}}.
     * Only valid for array types.
     *
     * @param elements the initializer element expressions as a list
     * @return the array initializer expression
     */
    default Expr newArrayInit(Expr... elements) {
        return newArrayInit(List.of(elements));
    }

    /**
     * Creates an array creation expression with an initializer: {@code new Type[] {e1, e2, ...}}.
     * Only valid for array types.
     *
     * @param elements the initializer element expressions as a list
     * @return the array initializer expression
     */
    Expr newArrayInit(List<Expr> elements);

    /**
     * Creates a method reference on a type: {@code Type::method}.
     *
     * @param name the method name (or {@code "new"} for constructor references)
     * @return the method reference expression
     */
    default Expr methodRef(final String name) {
        Assert.checkNotNullParam("name", name);
        Assert.checkNotEmptyParam("name", name);
        return new MethodRefExpr(this, name);
    }

    /**
     * Returns a Javadoc program element reference for a member of this type
     * ({@code This#member}).
     * <p>
     * The returned reference can be used in {@code {@link}}, {@code {@linkplain}},
     * and {@code @see} tags via the {@link DocCommentCreator}
     * API.
     *
     * @param member the member identifier (e.g., {@code "length()"},
     *        {@code "CASE_INSENSITIVE_ORDER"})
     * @return the doc reference
     */
    DocReference docRef(String member);

    /**
     * Returns an intersection type composed of all the given types ({@code A & B & C}).
     *
     * @param types the types to intersect
     * @return the intersection type
     */
    static Type allOf(Type... types) {
        return allOf(List.of(types));
    }

    /**
     * Returns an intersection type composed of all the given types ({@code A & B & C}).
     *
     * @param types the types to intersect as a list
     * @return the intersection type
     */
    static Type allOf(List<Type> types) {
        return new IntersectionType(types);
    }
}
