package org.jboss.jdeparser;

import java.util.function.BiConsumer;

import org.jboss.jdeparser.creator.BlockCreator;
import org.jboss.jdeparser.impl.JLabelImpl;

/**
 * A statement label that can be used as a target for {@code break} and {@code continue}.
 * <p>
 * Labels are created by {@link BlockCreator#labeled(String, BiConsumer)
 * BlockCreator.labeled()} and can be referenced by
 * {@link BlockCreator#break_(JLabel) break_()} and
 * {@link BlockCreator#continue_(JLabel) continue_()}.
 * <p>
 * This interface is sealed; the sole implementation is {@link JLabelImpl}.
 */
public sealed interface JLabel permits JLabelImpl {
    /**
     * Returns the name of this label.
     *
     * @return the label name (never {@code null})
     */
    String name();
}
