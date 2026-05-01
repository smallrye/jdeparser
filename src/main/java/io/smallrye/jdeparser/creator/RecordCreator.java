package io.smallrye.jdeparser.creator;

import java.util.function.Consumer;

import io.smallrye.jdeparser.Type;
import io.smallrye.jdeparser.Var;
import io.smallrye.jdeparser.impl.RecordCreatorImpl;

/**
 * A creator for building a record declaration (Java 16+).
 */
public sealed interface RecordCreator extends ModifiableCreator permits RecordCreatorImpl {

    /**
     * Adds an implemented interface to this record.
     *
     * @param interfaceType the interface type
     */
    void implements_(Type interfaceType);

    /**
     * Adds a type parameter to this record.
     *
     * @param name the type parameter name
     * @param builder the callback to configure the type parameter
     * @return a type reference for the declared type parameter
     */
    Type typeParam(String name, Consumer<TypeParamCreator> builder);

    /**
     * Defines a record component (simple form).
     *
     * @param name the component name
     * @param type the component type
     */
    void component(String name, Type type);

    /**
     * Defines a record component with configuration (annotations, documentation).
     *
     * @param name the component name
     * @param type the component type
     * @param builder the callback to configure the component
     */
    void component(String name, Type type, Consumer<RecordComponentCreator> builder);

    /**
     * Defines a compact constructor for this record.
     *
     * @param builder the callback to define the constructor body
     */
    void compactConstructor(Consumer<BlockCreator> builder);

    /**
     * Defines a method in this record.
     *
     * @param name the method name
     * @param builder the callback to configure the method
     */
    void method(String name, Consumer<MethodCreator> builder);

    /**
     * Defines a field in this record (static fields only).
     *
     * @param name the field name
     * @param builder the callback to configure the field
     * @return a variable expression referencing the declared field
     */
    Var field(String name, Consumer<FieldCreator> builder);
}
