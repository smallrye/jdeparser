package org.jboss.jdeparser.impl;

import java.io.IOException;
import java.util.EnumSet;

import org.jboss.jdeparser.creator.AccessLevel;
import org.jboss.jdeparser.creator.ModifierFlag;
import org.jboss.jdeparser.creator.ModifierLocation;

/**
 * Tracks and validates modifier state (access level and flags) for a
 * declaration, and writes modifiers in JLS recommended order.
 * <p>
 * The holder validates modifiers against a {@link ModifierLocation} to
 * ensure only valid modifiers are applied (e.g., {@code transient} is
 * not valid on a method).  It also enforces mutual exclusion rules
 * defined by {@link ModifierFlag#isExclusiveWith(ModifierFlag)}.
 *
 * @see ModifierLocation
 * @see ModifierFlag
 */
public final class ModifierHolder {

    /** The declaration context that determines valid modifiers. */
    private final ModifierLocation location;

    /** The current access level. */
    private AccessLevel access = AccessLevel.PACKAGE_PRIVATE;

    /** The set of active modifier flags. */
    private final EnumSet<ModifierFlag> flags = EnumSet.noneOf(ModifierFlag.class);

    /**
     * Constructs a modifier holder for the given declaration context.
     *
     * @param location the modifier location for validation
     */
    public ModifierHolder(final ModifierLocation location) {
        this.location = location;
    }

    /**
     * Returns the modifier location.
     *
     * @return the modifier location
     */
    public ModifierLocation location() {
        return location;
    }

    /**
     * Returns the current access level.
     *
     * @return the access level
     */
    public AccessLevel access() {
        return access;
    }

    /**
     * Returns the set of active modifier flags.
     *
     * @return the flags (unmodifiable view)
     */
    public EnumSet<ModifierFlag> flags() {
        return flags;
    }

    /**
     * Sets the access level, validating against the location.
     *
     * @param level the access level to set
     * @throws IllegalArgumentException if the level is not valid for this location
     */
    public void setAccess(final AccessLevel level) {
        if (!location.supports(level)) {
            throw new IllegalArgumentException(level + " is not valid for " + location);
        }
        this.access = level;
    }

    /**
     * Adds a modifier flag, validating against the location and
     * checking for mutual exclusion conflicts.
     *
     * @param flag the flag to add
     * @throws IllegalArgumentException if the flag is not valid for this location,
     *                                  or conflicts with an existing flag
     */
    public void addFlag(final ModifierFlag flag) {
        if (!location.supports(flag)) {
            throw new IllegalArgumentException(flag + " is not valid for " + location);
        }
        for (final ModifierFlag existing : flags) {
            if (flag.isExclusiveWith(existing)) {
                throw new IllegalArgumentException(flag + " conflicts with " + existing);
            }
        }
        flags.add(flag);
    }

    /**
     * Removes a modifier flag.
     *
     * @param flag the flag to remove
     */
    public void removeFlag(final ModifierFlag flag) {
        flags.remove(flag);
    }

    /**
     * Tests whether a modifier flag is set.
     *
     * @param flag the flag to test
     * @return {@code true} if the flag is set
     */
    public boolean hasFlag(final ModifierFlag flag) {
        return flags.contains(flag);
    }

    /**
     * Writes the modifiers in JLS recommended order to the given writer.
     * <p>
     * Order: access level, then flags in the order: {@code abstract},
     * {@code static}, {@code sealed}, {@code non-sealed}, {@code final},
     * {@code synchronized}, {@code native}, {@code strictfp},
     * {@code transient}, {@code volatile}, {@code default}.
     * The {@code varargs} flag is not written here; it is handled
     * separately in parameter writing.
     *
     * @param writer the source file writer
     * @throws IOException if an I/O error occurs
     */
    public void write(final SourceFileWriter writer) throws IOException {
        // access level
        switch (access) {
            case PUBLIC -> writer.write(Tokens.$KW.PUBLIC);
            case PROTECTED -> writer.write(Tokens.$KW.PROTECTED);
            case PRIVATE -> writer.write(Tokens.$KW.PRIVATE);
            case PACKAGE_PRIVATE -> {}
        }
        // flags in JLS recommended order
        writeIf(writer, ModifierFlag.ABSTRACT, Tokens.$KW.ABSTRACT);
        writeIf(writer, ModifierFlag.STATIC, Tokens.$KW.STATIC);
        writeIf(writer, ModifierFlag.SEALED, Tokens.$KW.SEALED);
        writeIf(writer, ModifierFlag.NON_SEALED, Tokens.$KW.NON_SEALED);
        writeIf(writer, ModifierFlag.FINAL, Tokens.$KW.FINAL);
        writeIf(writer, ModifierFlag.SYNCHRONIZED, Tokens.$KW.SYNCHRONIZED);
        writeIf(writer, ModifierFlag.NATIVE, Tokens.$KW.NATIVE);
        writeIf(writer, ModifierFlag.STRICTFP, Tokens.$KW.STRICTFP);
        writeIf(writer, ModifierFlag.TRANSIENT, Tokens.$KW.TRANSIENT);
        writeIf(writer, ModifierFlag.VOLATILE, Tokens.$KW.VOLATILE);
        writeIf(writer, ModifierFlag.DEFAULT, Tokens.$KW.DEFAULT);
    }

    /**
     * Writes the keyword for a modifier flag if it is set.
     *
     * @param writer the writer
     * @param flag   the flag to check
     * @param token  the keyword token to write
     * @throws IOException if an I/O error occurs
     */
    private void writeIf(final SourceFileWriter writer, final ModifierFlag flag, final Tokens.$KW token)
            throws IOException {
        if (flags.contains(flag)) {
            writer.write(token);
        }
    }
}
