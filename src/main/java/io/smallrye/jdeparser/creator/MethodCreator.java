package io.smallrye.jdeparser.creator;

import java.util.function.Consumer;

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
