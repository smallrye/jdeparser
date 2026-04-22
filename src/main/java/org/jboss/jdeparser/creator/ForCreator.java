package org.jboss.jdeparser.creator;

import java.util.function.Consumer;

import org.jboss.jdeparser.JExpr;
import org.jboss.jdeparser.JType;
import org.jboss.jdeparser.JVar;
import org.jboss.jdeparser.impl.ForCreatorImpl;

/**
 * A creator for configuring a traditional {@code for} loop.
 * <p>
 * The loop is structured as: {@code for (init; condition; update) body}.
 */
public sealed interface ForCreator permits ForCreatorImpl {

    /**
     * Sets the loop initializer, declaring a new variable.
     *
     * @param type the variable type
     * @param name the variable name
     * @param init the initializer expression
     * @return the loop variable, for use in the condition, update, and body
     */
    JVar init(JType type, String name, JExpr init);

    /**
     * Sets the loop condition.
     *
     * @param condition the condition expression
     */
    void condition(JExpr condition);

    /**
     * Sets the loop update expression.
     *
     * @param update the update expression
     */
    void update(JExpr update);

    /**
     * Defines the loop body.
     *
     * @param body the callback for the body statements
     */
    void body(Consumer<BlockCreator> body);
}
