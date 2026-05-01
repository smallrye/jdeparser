package io.smallrye.jdeparser.creator;

import java.util.function.Consumer;

import io.smallrye.jdeparser.Type;
import io.smallrye.jdeparser.Var;
import io.smallrye.jdeparser.impl.EnumCreatorImpl;

/**
 * A creator for building an enum declaration.
 */
public sealed interface EnumCreator extends ModifiableCreator permits EnumCreatorImpl {

    /**
     * Adds an implemented interface to this enum.
     *
     * @param interfaceType the interface type
     */
    void implements_(Type interfaceType);

    /**
     * Defines an enum constant.
     *
     * @param name the constant name
     * @param builder the callback to configure the constant
     */
    void constant(String name, Consumer<EnumConstantCreator> builder);

    /**
     * Defines a field in this enum.
     *
     * @param name the field name
     * @param builder the callback to configure the field
     * @return a variable expression referencing the declared field
     */
    Var field(String name, Consumer<FieldCreator> builder);

    /**
     * Defines a method in this enum.
     *
     * @param name the method name
     * @param builder the callback to configure the method
     */
    void method(String name, Consumer<MethodCreator> builder);

    /**
     * Defines a constructor in this enum.
     *
     * @param builder the callback to configure the constructor
     */
    void constructor(Consumer<ConstructorCreator> builder);
}
