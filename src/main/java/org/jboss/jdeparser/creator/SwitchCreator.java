package org.jboss.jdeparser.creator;

import java.util.List;
import java.util.function.Consumer;

import org.jboss.jdeparser.JExpr;
import org.jboss.jdeparser.JType;
import org.jboss.jdeparser.impl.SwitchCreatorImpl;

/**
 * A creator for configuring a {@code switch} statement or expression.
 * <p>
 * Supports constant cases, multi-value cases, type pattern cases (Java 21+),
 * null cases, and default cases.
 */
public sealed interface SwitchCreator permits SwitchCreatorImpl {

    /**
     * Adds a case with a single constant value.
     *
     * @param value the case constant expression
     * @param body  the callback for the case body
     */
    void case_(JExpr value, Consumer<BlockCreator> body);

    /**
     * Adds a case with multiple constant values (e.g., {@code case 3, 5, 6:}).
     *
     * @param values the case constant expressions
     * @param body   the callback for the case body
     */
    void case_(List<JExpr> values, Consumer<BlockCreator> body);

    /**
     * Adds a type pattern case (Java 21+): {@code case Type name -> ...}.
     *
     * @param type    the pattern type
     * @param name    the binding variable name
     * @param builder the callback to configure the case (may include guard via {@link CaseCreator#when_(JExpr)})
     */
    void case_(JType type, String name, Consumer<CaseCreator> builder);

    /**
     * Adds a {@code case null} (Java 21+).
     *
     * @param body the callback for the case body
     */
    void caseNull(Consumer<BlockCreator> body);

    /**
     * Adds a {@code default} case.
     *
     * @param body the callback for the default body
     */
    void default_(Consumer<BlockCreator> body);
}
