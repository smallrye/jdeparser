package io.smallrye.jdeparser.creator;

import java.util.function.Consumer;

import io.smallrye.jdeparser.Type;
import io.smallrye.jdeparser.Var;
import io.smallrye.jdeparser.impl.ConstructorCreatorImpl;

/**
 * A creator for configuring a constructor declaration.
 * <p>
 * Similar to {@link MethodCreator} but without a return type.
 */
public sealed interface ConstructorCreator extends ModifiableCreator permits ConstructorCreatorImpl {

    /**
     * Adds a parameter to this constructor (simple form).
     *
     * @param name the parameter name
     * @param type the parameter type
     * @return a variable expression referencing the declared parameter
     */
    Var param(String name, Type type);

    /**
     * Adds a parameter to this constructor with configuration.
     *
     * @param name the parameter name
     * @param type the parameter type
     * @param builder the callback to configure the parameter
     * @return a variable expression referencing the declared parameter
     */
    Var param(String name, Type type, Consumer<ParamCreator> builder);

    /**
     * Adds a varargs parameter to this constructor.
     *
     * @param name the parameter name
     * @param type the array element type
     * @param builder the callback to configure the parameter
     * @return a variable expression referencing the declared parameter
     */
    Var varargParam(String name, Type type, Consumer<ParamCreator> builder);

    /**
     * Adds a thrown exception type to this constructor.
     *
     * @param exceptionType the exception type
     */
    void throws_(Type exceptionType);

    /**
     * Adds a thrown exception type to this constructor with documentation.
     * <p>
     * The documentation provided by the builder is contributed as a
     * {@code @throws} tag in this constructor's Javadoc comment.
     *
     * @param exceptionType the exception type
     * @param builder the callback to provide the {@code @throws} tag content
     */
    void throws_(Type exceptionType, Consumer<DocInlineCreator> builder);

    /**
     * Adds a type parameter to this constructor.
     *
     * @param name the type parameter name
     * @param builder the callback to configure the type parameter
     * @return a type reference for the declared type parameter
     */
    Type typeParam(String name, Consumer<TypeParamCreator> builder);

    /**
     * Defines the constructor body.
     *
     * @param builder the callback to define the body statements
     */
    void body(Consumer<BlockCreator> builder);
}
