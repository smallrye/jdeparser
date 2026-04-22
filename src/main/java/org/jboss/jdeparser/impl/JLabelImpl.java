package org.jboss.jdeparser.impl;

import org.jboss.jdeparser.JLabel;

/**
 * Implementation of {@link JLabel} as a simple record holding the label name.
 *
 * @param name the label name (never {@code null})
 */
public record JLabelImpl(String name) implements JLabel {

    /**
     * Constructs a new label with the given name.
     *
     * @param name the label name
     * @throws IllegalArgumentException if {@code name} is {@code null}
     */
    public JLabelImpl {
        if (name == null) {
            throw new IllegalArgumentException("Label name must not be null");
        }
    }
}
