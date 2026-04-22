package org.jboss.jdeparser.creator;

/**
 * A modifier that can be applied to a Java declaration.
 * <p>
 * This is a sealed interface with two permitted subtypes:
 * <ul>
 *     <li>{@link AccessLevel} &mdash; access modifiers ({@code public}, {@code protected},
 *         {@code private}, or package-private)</li>
 *     <li>{@link ModifierFlag} &mdash; non-access modifier flags ({@code static}, {@code final},
 *         {@code abstract}, etc.)</li>
 * </ul>
 *
 * @see ModifiableCreator
 */
public sealed interface Modifier permits AccessLevel, ModifierFlag {
    /**
     * Returns the Java source keyword for this modifier.
     * <p>
     * For {@link AccessLevel#PACKAGE_PRIVATE}, this returns an empty string
     * since no keyword is emitted in the generated source.
     *
     * @return the keyword string (e.g. {@code "public"}, {@code "static"}),
     *         or an empty string if no keyword is emitted
     */
    String keyword();
}
