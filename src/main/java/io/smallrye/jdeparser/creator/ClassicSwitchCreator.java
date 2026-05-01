package io.smallrye.jdeparser.creator;

import java.util.function.Consumer;

import io.smallrye.jdeparser.Expr;
import io.smallrye.jdeparser.impl.ClassicSwitchCreatorImpl;

/**
 * A creator for configuring a classic {@code switch} statement using
 * colon ({@code :}) syntax with fall-through semantics.
 * <p>
 * Classic switch supports only single-value constant cases and a default case.
 * It does not support {@code null} cases, multi-value cases, or type pattern
 * matching. For those features, use the modern {@link SwitchCreator} via
 * {@link BlockCreator#switch_(Expr, Consumer)}.
 *
 * @see SwitchCreator
 * @see BlockCreator#switchClassic(Expr, Consumer)
 */
public sealed interface ClassicSwitchCreator permits SwitchCreator, ClassicSwitchCreatorImpl {

    /**
     * Adds a case with a single constant value using colon syntax: {@code case value: ...}.
     *
     * @param value the case constant expression (must not be {@code null} literal)
     * @param body the callback for the case body
     */
    void case_(Expr value, Consumer<BlockCreator> body);

    /**
     * Adds a {@code default} case using colon syntax: {@code default: ...}.
     *
     * @param body the callback for the default body
     */
    void default_(Consumer<BlockCreator> body);
}
