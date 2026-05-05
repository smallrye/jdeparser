package io.smallrye.jdeparser.impl;

import io.smallrye.common.constraint.Assert;
import io.smallrye.jdeparser.SourceVersion;
import io.smallrye.jdeparser.Type;

/**
 * Base class for all creator implementations, providing the borrow-pattern
 * lifecycle state machine.
 * <p>
 * Every creator transitions through three states:
 * <ol>
 * <li><strong>ACTIVE</strong> — the creator is currently being configured
 * by its callback. All configuration methods (adding members, setting
 * modifiers, etc.) are allowed.</li>
 * <li><strong>NESTED</strong> — a child callback is active. The creator
 * cannot be modified until the child callback returns.</li>
 * <li><strong>DONE</strong> — the creator's callback has returned and
 * the creator is finalized. No further modifications are allowed.</li>
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

    /** The enclosing source file creator, for type registration. */
    private SourceFileCreatorImpl sourceFile;

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
     * Verifies that this creator has not been finalized.
     * <p>
     * Unlike {@link #checkActive()}, this permits calls while in the
     * NESTED state. Use this for operations that are safe to perform
     * concurrently with a child callback (e.g., adding imports).
     *
     * @throws IllegalStateException if the creator is DONE
     */
    protected void checkNotDone() {
        if (state == ST_DONE) {
            throw new IllegalStateException("Creator has already been completed");
        }
    }

    /**
     * Verifies that this creator is in the ACTIVE state.
     *
     * @throws IllegalStateException if the creator is NESTED or DONE
     */
    protected void checkActive() {
        switch (state) {
            case ST_ACTIVE -> {
            }
            case ST_NESTED -> throw new IllegalStateException("Cannot modify creator while a nested callback is active");
            case ST_DONE -> throw new IllegalStateException("Creator has already been completed");
            default -> throw Assert.impossibleSwitchCase(state);
        }
    }

    /**
     * Executes a child callback while temporarily transitioning this
     * creator to the NESTED state.
     * <p>
     * While nested, any attempt to call {@link #checkActive()} on this
     * creator will fail, preventing concurrent modification. After the
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

    /**
     * Transitions this creator from the DONE state back to ACTIVE,
     * allowing it to accept further configuration.
     * <p>
     * This is used internally to allow on-demand doc comment creators to
     * be reused across multiple configuration calls (e.g., when a type
     * parameter's doc comment contributes a tag to an already-finished
     * parent doc comment).
     * <p>
     * Has no effect if the creator is already ACTIVE or NESTED.
     */
    void reopen() {
        if (state == ST_DONE) {
            state = ST_ACTIVE;
        }
    }

    /**
     * Sets the enclosing source file creator for type registration.
     *
     * @param sourceFile the source file creator
     */
    void sourceFile(final SourceFileCreatorImpl sourceFile) {
        this.sourceFile = sourceFile;
    }

    /**
     * Returns the enclosing source file creator.
     *
     * @return the source file creator, or {@code null} if not set
     */
    SourceFileCreatorImpl sourceFile() {
        return sourceFile;
    }

    /**
     * Registers a type as used within the enclosing source file,
     * enabling correct import resolution and conflict detection.
     *
     * @param type the type to register
     */
    protected void registerUsedType(final Type type) {
        if (sourceFile != null) {
            sourceFile.registerUsedType(type);
        }
    }
}
