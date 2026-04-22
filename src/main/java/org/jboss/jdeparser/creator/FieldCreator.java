package org.jboss.jdeparser.creator;

import org.jboss.jdeparser.JExpr;
import org.jboss.jdeparser.JType;
import org.jboss.jdeparser.impl.FieldCreatorImpl;

/**
 * A creator for configuring a field declaration.
 */
public sealed interface FieldCreator extends ModifiableCreator permits FieldCreatorImpl {

    /**
     * Sets the type of this field.
     *
     * @param type the field type
     */
    void type(JType type);

    /**
     * Sets the initializer expression for this field.
     *
     * @param init the initializer expression
     */
    void init(JExpr init);
}
