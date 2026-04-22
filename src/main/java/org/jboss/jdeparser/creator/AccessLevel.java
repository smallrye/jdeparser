package org.jboss.jdeparser.creator;

/**
 * Access level modifiers for Java declarations.
 * <p>
 * Each constant represents one of the four Java access levels.
 * {@link #PACKAGE_PRIVATE} represents the default (no keyword) access level;
 * no keyword is emitted in the generated source for this level.
 *
 * @see Modifier
 * @see ModifiableCreator#setAccess(AccessLevel)
 */
public enum AccessLevel implements Modifier {
    /** {@code public} access. */
    PUBLIC("public"),
    /** {@code protected} access. */
    PROTECTED("protected"),
    /** Package-private (default) access; no keyword is emitted. */
    PACKAGE_PRIVATE(""),
    /** {@code private} access. */
    PRIVATE("private"),
    ;

    private final String keyword;

    AccessLevel(final String keyword) {
        this.keyword = keyword;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns the keyword for this access level, or an empty string
     * for {@link #PACKAGE_PRIVATE}.
     */
    @Override
    public String keyword() {
        return keyword;
    }
}
