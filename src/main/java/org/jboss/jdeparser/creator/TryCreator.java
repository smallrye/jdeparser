package org.jboss.jdeparser.creator;

import java.util.List;
import java.util.function.Consumer;

import org.jboss.jdeparser.JExpr;
import org.jboss.jdeparser.JType;
import org.jboss.jdeparser.impl.TryCreatorImpl;

/**
 * A creator for configuring a {@code try} statement (with optional
 * resources, catch blocks, and finally block).
 */
public sealed interface TryCreator permits TryCreatorImpl {

    /**
     * Adds a try-with-resources resource declaration.
     *
     * @param type the resource type
     * @param name the resource variable name
     * @param init the resource initializer expression
     */
    void with(JType type, String name, JExpr init);

    /**
     * Defines the try body.
     *
     * @param body the callback for the try body statements
     */
    void body(Consumer<BlockCreator> body);

    /**
     * Adds a catch block for a single exception type.
     *
     * @param exceptionType the exception type
     * @param name          the exception variable name
     * @param body          the callback for the catch body
     */
    void catch_(JType exceptionType, String name, Consumer<BlockCreator> body);

    /**
     * Adds a multi-catch block for multiple exception types.
     *
     * @param exceptionTypes the exception types
     * @param name           the exception variable name
     * @param body           the callback for the catch body
     */
    void catch_(List<JType> exceptionTypes, String name, Consumer<BlockCreator> body);

    /**
     * Defines a finally block.
     *
     * @param body the callback for the finally body statements
     */
    void finally_(Consumer<BlockCreator> body);
}
