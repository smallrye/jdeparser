package org.jboss.jdeparser.creator;

import java.util.function.Consumer;

import org.jboss.jdeparser.JExpr;
import org.jboss.jdeparser.impl.CaseCreatorImpl;

/**
 * A creator for configuring a type-pattern {@code case} in a {@code switch}.
 * <p>
 * Used for pattern matching cases (Java 21+) where a guard condition
 * ({@code when}) may be applied.
 */
public sealed interface CaseCreator permits CaseCreatorImpl {

    /**
     * Adds a guard condition: {@code case Type name when condition -> ...}.
     *
     * @param guard the guard expression
     */
    void when_(JExpr guard);

    /**
     * Defines the case body.
     *
     * @param body the callback for the body statements
     */
    void body(Consumer<BlockCreator> body);
}
