package org.jboss.jdeparser.creator;

/**
 * A creator for declarations that can have access levels and modifier flags.
 * <p>
 * Provides methods to set access level, add/remove modifier flags, and
 * convenience methods for commonly used modifiers.
 *
 * @see AccessLevel
 * @see ModifierFlag
 * @see ModifierLocation
 */
public sealed interface ModifiableCreator extends AnnotatableCreator, DocCommentableCreator permits AnnotationInterfaceCreator, ClassCreator, ConstructorCreator,
                                                                                                    EnumCreator, FieldCreator, InterfaceCreator, MethodCreator,
                                                                                                    ParamCreator, RecordCreator {

    /**
     * Returns the modifier location that determines which modifiers are valid.
     *
     * @return the modifier location for this declaration
     */
    ModifierLocation modifierLocation();

    /**
     * Sets the access level for this declaration.
     *
     * @param access the access level
     * @throws IllegalArgumentException if the access level is not valid for this location
     */
    void setAccess(AccessLevel access);

    /**
     * Adds a modifier flag to this declaration.
     *
     * @param flag the flag to add
     * @throws IllegalArgumentException if the flag is not valid for this location,
     *                                  or if it conflicts with an existing flag
     */
    void addFlag(ModifierFlag flag);

    /**
     * Removes a modifier flag from this declaration.
     *
     * @param flag the flag to remove
     */
    void removeFlag(ModifierFlag flag);

    // ---- Convenience methods for access levels ----

    /**
     * Sets the access level to {@code public}.
     */
    default void public_() {
        setAccess(AccessLevel.PUBLIC);
    }

    /**
     * Sets the access level to {@code protected}.
     */
    default void protected_() {
        setAccess(AccessLevel.PROTECTED);
    }

    /**
     * Sets the access level to package-private (default).
     */
    default void packagePrivate() {
        setAccess(AccessLevel.PACKAGE_PRIVATE);
    }

    /**
     * Sets the access level to {@code private}.
     */
    default void private_() {
        setAccess(AccessLevel.PRIVATE);
    }

    // ---- Convenience methods for modifier flags ----

    /**
     * Adds the {@code final} modifier.
     */
    default void final_() {
        addFlag(ModifierFlag.FINAL);
    }

    /**
     * Adds the {@code static} modifier.
     */
    default void static_() {
        addFlag(ModifierFlag.STATIC);
    }

    /**
     * Adds the {@code abstract} modifier.
     */
    default void abstract_() {
        addFlag(ModifierFlag.ABSTRACT);
    }

    /**
     * Adds the {@code synchronized} modifier.
     */
    default void synchronized_() {
        addFlag(ModifierFlag.SYNCHRONIZED);
    }

    /**
     * Adds the {@code native} modifier.
     */
    default void native_() {
        addFlag(ModifierFlag.NATIVE);
    }

    /**
     * Adds the {@code sealed} modifier (Java 17+).
     */
    default void sealed_() {
        addFlag(ModifierFlag.SEALED);
    }

    /**
     * Adds the {@code non-sealed} modifier (Java 17+).
     */
    default void nonSealed_() {
        addFlag(ModifierFlag.NON_SEALED);
    }
}
