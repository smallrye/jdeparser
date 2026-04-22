package org.jboss.jdeparser.creator;

import java.util.function.Consumer;

import org.jboss.jdeparser.JType;
import org.jboss.jdeparser.impl.ConstructorCreatorImpl;

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
     */
    void param(String name, JType type);

    /**
     * Adds a parameter to this constructor with configuration.
     *
     * @param name    the parameter name
     * @param type    the parameter type
     * @param builder the callback to configure the parameter
     */
    void param(String name, JType type, Consumer<ParamCreator> builder);

    /**
     * Adds a varargs parameter to this constructor.
     *
     * @param name    the parameter name
     * @param type    the array element type
     * @param builder the callback to configure the parameter
     */
    void varargParam(String name, JType type, Consumer<ParamCreator> builder);

    /**
     * Adds a thrown exception type to this constructor.
     *
     * @param exceptionType the exception type
     */
    void throws_(JType exceptionType);

    /**
     * Adds a type parameter to this constructor.
     *
     * @param name    the type parameter name
     * @param builder the callback to configure the type parameter
     */
    void typeParam(String name, Consumer<TypeParamCreator> builder);

    /**
     * Defines the constructor body.
     *
     * @param builder the callback to define the body statements
     */
    void body(Consumer<BlockCreator> builder);
}
