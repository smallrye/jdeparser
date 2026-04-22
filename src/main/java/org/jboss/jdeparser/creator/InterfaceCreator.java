package org.jboss.jdeparser.creator;

import java.util.function.Consumer;

import org.jboss.jdeparser.JType;
import org.jboss.jdeparser.impl.InterfaceCreatorImpl;

/**
 * A creator for building an interface declaration.
 * <p>
 * All methods defined via this creator use the {@code DEFAULT} modifier flag
 * to indicate a default method.  Method body validation is based on the
 * modifier combination: abstract methods cannot have bodies, while default,
 * static, and private methods require bodies.
 */
public sealed interface InterfaceCreator extends ModifiableCreator permits InterfaceCreatorImpl {

    /**
     * Adds a superinterface to this interface.
     *
     * @param interfaceType the superinterface type
     */
    void extends_(JType interfaceType);

    /**
     * Adds a {@code permits} clause entry (Java 17+, for sealed interfaces).
     *
     * @param permittedType the permitted subtype
     */
    void permits(JType permittedType);

    /**
     * Adds a type parameter to this interface.
     *
     * @param name    the type parameter name
     * @param builder the callback to configure the type parameter
     */
    void typeParam(String name, Consumer<TypeParamCreator> builder);

    /**
     * Defines a method in this interface.
     *
     * @param name    the method name
     * @param builder the callback to configure the method
     */
    void method(String name, Consumer<MethodCreator> builder);

    /**
     * Defines a constant field in this interface.
     *
     * @param name    the field name
     * @param builder the callback to configure the field
     */
    void field(String name, Consumer<FieldCreator> builder);

    /**
     * Defines a nested class.
     *
     * @param name    the class name
     * @param builder the callback to define the class
     */
    void class_(String name, Consumer<ClassCreator> builder);

    /**
     * Defines a nested interface.
     *
     * @param name    the interface name
     * @param builder the callback to define the interface
     */
    void interface_(String name, Consumer<InterfaceCreator> builder);

    /**
     * Defines a nested enum.
     *
     * @param name    the enum name
     * @param builder the callback to define the enum
     */
    void enum_(String name, Consumer<EnumCreator> builder);

    /**
     * Defines a nested record.
     *
     * @param name    the record name
     * @param builder the callback to define the record
     */
    void record_(String name, Consumer<RecordCreator> builder);

    /**
     * Defines a nested annotation type.
     *
     * @param name    the annotation type name
     * @param builder the callback to define the annotation type
     */
    void annotationInterface_(String name, Consumer<AnnotationInterfaceCreator> builder);
}
