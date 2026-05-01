package io.smallrye.jdeparser.creator;

import java.util.List;
import java.util.function.Consumer;

import io.smallrye.jdeparser.Expr;
import io.smallrye.jdeparser.Type;
import io.smallrye.jdeparser.impl.SwitchCreatorImpl;

/**
 * A creator for configuring a modern {@code switch} statement or expression
 * using arrow ({@code ->}) syntax with no fall-through.
 * <p>
 * Extends {@link ClassicSwitchCreator} with support for multi-value cases,
 * type pattern cases (Java 21+), and {@code null} cases.
 * The single-value {@link #case_(Expr, Consumer)} method is provided as a
 * convenience that delegates to {@link #case_(List, Consumer)}.
 *
 * @see ClassicSwitchCreator
 * @see BlockCreator#switch_(Expr, Consumer)
 */
public sealed interface SwitchCreator extends ClassicSwitchCreator permits SwitchCreatorImpl {

    /**
     * {@inheritDoc}
     * <p>
     * This convenience method delegates to {@link #case_(List, Consumer)}
     * with a single-element list.
     */
    @Override
    default void case_(final Expr value, final Consumer<BlockCreator> body) {
        case_(List.of(value), body);
    }

    /**
     * Adds a case with one or more constant values using arrow syntax:
     * {@code case val1, val2 -> ...}.
     *
     * @param values the case constant expressions (must not be empty)
     * @param body the callback for the case body
     */
    void case_(List<Expr> values, Consumer<BlockCreator> body);

    /**
     * Adds a type pattern case (Java 21+): {@code case Type name -> ...}.
     *
     * @param type the pattern type
     * @param name the binding variable name
     * @param builder the callback to configure the case (may include guard via {@link CaseCreator#when_(Expr)})
     */
    void case_(Type type, String name, Consumer<CaseCreator> builder);
}
