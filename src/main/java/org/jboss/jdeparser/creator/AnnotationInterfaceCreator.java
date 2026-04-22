package org.jboss.jdeparser.creator;

import java.util.function.Consumer;

import org.jboss.jdeparser.JExpr;
import org.jboss.jdeparser.JType;
import org.jboss.jdeparser.impl.AnnotationInterfaceCreatorImpl;

/**
 * A creator for building an annotation type declaration.
 */
public sealed interface AnnotationInterfaceCreator extends ModifiableCreator permits AnnotationInterfaceCreatorImpl {

    /**
     * Defines an annotation element (method-like member).
     *
     * @param name         the element name
     * @param type         the element type
     * @param defaultValue the default value expression, or {@code null} for no default
     */
    void element(String name, JType type, JExpr defaultValue);

    /**
     * Defines an annotation element without a default value.
     *
     * @param name the element name
     * @param type the element type
     */
    void element(String name, JType type);

    /**
     * Defines a constant in this annotation type.
     *
     * @param name    the constant name
     * @param builder the callback to configure the constant field
     */
    void constant(String name, Consumer<FieldCreator> builder);
}
