package org.jboss.jdeparser.impl;

import org.jboss.jdeparser.SourceVersion;

/**
 * Base class for all creator implementations, providing the borrow-pattern
 * lifecycle state machine.
 * <p>
 * Every creator transitions through three states:
 * <ol>
 *   <li><strong>ACTIVE</strong> — the creator is currently being configured
 *       by its callback.  All configuration methods (adding members, setting
 *       modifiers, etc.) are allowed.</li>
 *   <li><strong>NESTED</strong> — a child callback is active.  The creator
 *       cannot be modified until the child callback returns.</li>
 *   <li><strong>DONE</strong> — the creator's callback has returned and
 *       the creator is finalized.  No further modifications are allowed.</li>
 * </ol>
 * <p>
 * Subclasses call {@link #checkActive()} at the start of every mutating
 * method, and {@link #nest(Runnable)} to safely execute child callbacks.
 */
public abstract class AbstractCreator {

    /** The creator is actively being configured. */
    private static final int ST_ACTIVE = 0;

    /** A child callback is running; modifications are blocked. */
    private static final int ST_NESTED = 1;

    /** The creator is finalized; no further changes allowed. */
    private static final int ST_DONE = 2;

    /** The source version for feature gating. */
    private final SourceVersion version;

    /** The current lifecycle state. */
    private int state = ST_ACTIVE;

    /**
     * Constructs a new creator with the given source version.
     *
     * @param version the source version for feature validation
     */
    protected AbstractCreator(final SourceVersion version) {
        this.version = version;
    }

    /**
     * Returns the source version used for feature validation.
     *
     * @return the source version
     */
    protected SourceVersion version() {
        return version;
    }

    /**
     * Verifies that this creator is in the ACTIVE state.
     *
     * @throws IllegalStateException if the creator is NESTED or DONE
     */
    protected void checkActive() {
        if (state != ST_ACTIVE) {
            throw new IllegalStateException(
                state == ST_NESTED
                    ? "Cannot modify creator while a nested callback is active"
                    : "Creator has already been completed"
            );
        }
    }

    /**
     * Executes a child callback while temporarily transitioning this
     * creator to the NESTED state.
     * <p>
     * While nested, any attempt to call {@link #checkActive()} on this
     * creator will fail, preventing concurrent modification.  After the
     * callback returns (normally or exceptionally), this creator returns
     * to the ACTIVE state.
     *
     * @param action the callback to execute
     */
    protected void nest(final Runnable action) {
        checkActive();
        state = ST_NESTED;
        try {
            action.run();
        } finally {
            state = ST_ACTIVE;
        }
    }

    /**
     * Marks this creator as finalized.
     * <p>
     * After this call, any attempt to modify the creator will throw
     * {@link IllegalStateException}.
     */
    public void finish() {
        checkActive();
        state = ST_DONE;
    }
}
