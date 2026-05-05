package io.smallrye.jdeparser.creator;

import java.util.function.Consumer;

import io.smallrye.jdeparser.SourceVersion;
import io.smallrye.jdeparser.Type;
import io.smallrye.jdeparser.Var;
import io.smallrye.jdeparser.impl.MethodCreatorImpl;

/**
 * A creator for configuring a method declaration.
 * <p>
 * Modifiers must be set before calling {@link #body(Consumer)}.
 * Abstract methods and interface methods without {@code default}, {@code static},
 * or {@code private} modifiers cannot have a body.
 */
public sealed interface MethodCreator extends ModifiableCreator permits MethodCreatorImpl {

    /**
     * Sets the return type of this method.
     *
     * @param type the return type (use {@link Type#VOID} for void methods)
     */
    void returning(Type type);

    /**
     * Sets the return type of this method with documentation.
     * <p>
     * The documentation provided by the builder is contributed as a
     * {@code @return} block tag in this method's Javadoc comment.
     *
     * @param type the return type (use {@link Type#VOID} for void methods)
     * @param builder the callback to provide the {@code @return} tag content
     */
    void returning(Type type, Consumer<DocInlineCreator> builder);

    /**
     * Sets the return type of this method with inline return documentation.
     * <p>
     * The documentation provided by the builder is contributed as an
     * inline {@code {@return ...}} tag in this method's Javadoc comment,
     * which serves as both the first summary sentence and the {@code @return}
     * block tag.
     * <p>
     * Requires source version {@linkplain SourceVersion#JAVA_16 16}
     * or later.
     *
     * @param type the return type (use {@link Type#VOID} for void methods)
     * @param builder the callback to provide the {@code {@return}} tag content
     */
    void returningInline(Type type, Consumer<DocInlineCreator> builder);

    /**
     * Adds a parameter to this method (simple form).
     *
     * @param name the parameter name
     * @param type the parameter type
     * @return a variable expression referencing the declared parameter
     */
    Var param(String name, Type type);

    /**
     * Adds a parameter to this method with configuration.
     *
     * @param name the parameter name
     * @param type the parameter type
     * @param builder the callback to configure the parameter (annotations, docs, etc.)
     * @return a variable expression referencing the declared parameter
     */
    Var param(String name, Type type, Consumer<ParamCreator> builder);

    /**
     * Adds a varargs parameter to this method.
     *
     * @param name the parameter name
     * @param type the array element type (not the array type)
     * @param builder the callback to configure the parameter
     * @return a variable expression referencing the declared parameter
     */
    Var varargParam(String name, Type type, Consumer<ParamCreator> builder);

    /**
     * Adds a thrown exception type to this method.
     *
     * @param exceptionType the exception type
     */
    void throws_(Type exceptionType);

    /**
     * Adds a thrown exception type to this method with documentation.
     * <p>
     * The documentation provided by the builder is contributed as a
     * {@code @throws} tag in this method's Javadoc comment.
     *
     * @param exceptionType the exception type
     * @param builder the callback to provide the {@code @throws} tag content
     */
    void throws_(Type exceptionType, Consumer<DocInlineCreator> builder);

    /**
     * Adds a type parameter to this method.
     *
     * @param name the type parameter name
     * @param builder the callback to configure the type parameter
     * @return a type reference for the declared type parameter
     */
    Type typeParam(String name, Consumer<TypeParamCreator> builder);

    /**
     * Defines the method body.
     *
     * @param builder the callback to define the body statements
     * @throws IllegalStateException if the method is abstract or otherwise cannot have a body
     */
    void body(Consumer<BlockCreator> builder);
}
