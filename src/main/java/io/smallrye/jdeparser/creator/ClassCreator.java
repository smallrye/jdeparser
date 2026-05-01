package io.smallrye.jdeparser.creator;

import java.util.function.Consumer;

import io.smallrye.jdeparser.Type;
import io.smallrye.jdeparser.Var;
import io.smallrye.jdeparser.impl.ClassCreatorImpl;

/**
 * A creator for building a class declaration.
 * <p>
 * Provides methods for specifying the superclass, implemented interfaces,
 * fields, methods, constructors, nested types, and initializer blocks.
 */
public sealed interface ClassCreator extends ModifiableCreator permits ClassCreatorImpl {

    /**
     * Sets the superclass of this class.
     *
     * @param superType the superclass type
     */
    void extends_(Type superType);

    /**
     * Adds an implemented interface to this class.
     *
     * @param interfaceType the interface type
     */
    void implements_(Type interfaceType);

    /**
     * Adds a {@code permits} clause entry (Java 17+, for sealed classes).
     *
     * @param permittedType the permitted subtype
     */
    void permits(Type permittedType);

    /**
     * Adds a type parameter to this class.
     *
     * @param name the type parameter name (e.g., {@code "T"})
     * @param builder the callback to configure the type parameter
     * @return a type reference for the declared type parameter
     */
    Type typeParam(String name, Consumer<TypeParamCreator> builder);

    /**
     * Defines a field in this class.
     *
     * @param name the field name
     * @param builder the callback to configure the field
     * @return a variable expression referencing the declared field
     */
    Var field(String name, Consumer<FieldCreator> builder);

    /**
     * Defines a method in this class.
     *
     * @param name the method name
     * @param builder the callback to configure the method
     */
    void method(String name, Consumer<MethodCreator> builder);

    /**
     * Defines a constructor in this class.
     *
     * @param builder the callback to configure the constructor
     */
    void constructor(Consumer<ConstructorCreator> builder);

    /**
     * Defines an instance initializer block.
     *
     * @param builder the callback to define the block body
     */
    void instanceInit(Consumer<BlockCreator> builder);

    /**
     * Defines a static initializer block.
     *
     * @param builder the callback to define the block body
     */
    void staticInit(Consumer<BlockCreator> builder);

    /**
     * Defines a nested class.
     *
     * @param name the class name
     * @param builder the callback to define the class
     */
    void class_(String name, Consumer<ClassCreator> builder);

    /**
     * Defines a nested interface.
     *
     * @param name the interface name
     * @param builder the callback to define the interface
     */
    void interface_(String name, Consumer<InterfaceCreator> builder);

    /**
     * Defines a nested enum.
     *
     * @param name the enum name
     * @param builder the callback to define the enum
     */
    void enum_(String name, Consumer<EnumCreator> builder);

    /**
     * Defines a nested record.
     *
     * @param name the record name
     * @param builder the callback to define the record
     */
    void record_(String name, Consumer<RecordCreator> builder);

    /**
     * Defines a nested annotation type.
     *
     * @param name the annotation type name
     * @param builder the callback to define the annotation type
     */
    void annotationInterface_(String name, Consumer<AnnotationInterfaceCreator> builder);
}
