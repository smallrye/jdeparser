package org.jboss.jdeparser.creator;

import java.util.EnumSet;
import java.util.Set;

/**
 * Enumeration of declaration contexts where modifiers may appear.
 * <p>
 * Each constant encodes which {@link AccessLevel access levels} and
 * {@link ModifierFlag modifier flags} are valid in that context.  This is
 * used internally to validate modifier combinations at construction time.
 */
public enum ModifierLocation {

    /** A top-level or nested class declaration. */
    CLASS(
        EnumSet.of(AccessLevel.PUBLIC, AccessLevel.PROTECTED, AccessLevel.PACKAGE_PRIVATE, AccessLevel.PRIVATE),
        EnumSet.of(ModifierFlag.ABSTRACT, ModifierFlag.FINAL, ModifierFlag.STATIC, ModifierFlag.STRICTFP,
                   ModifierFlag.SEALED, ModifierFlag.NON_SEALED)
    ),
    /** A top-level or nested interface declaration. */
    INTERFACE(
        EnumSet.of(AccessLevel.PUBLIC, AccessLevel.PROTECTED, AccessLevel.PACKAGE_PRIVATE, AccessLevel.PRIVATE),
        EnumSet.of(ModifierFlag.ABSTRACT, ModifierFlag.STATIC, ModifierFlag.STRICTFP,
                   ModifierFlag.SEALED, ModifierFlag.NON_SEALED)
    ),
    /** An enum declaration. */
    ENUM(
        EnumSet.of(AccessLevel.PUBLIC, AccessLevel.PROTECTED, AccessLevel.PACKAGE_PRIVATE, AccessLevel.PRIVATE),
        EnumSet.of(ModifierFlag.STATIC, ModifierFlag.STRICTFP)
    ),
    /** A record declaration. */
    RECORD(
        EnumSet.of(AccessLevel.PUBLIC, AccessLevel.PROTECTED, AccessLevel.PACKAGE_PRIVATE, AccessLevel.PRIVATE),
        EnumSet.of(ModifierFlag.STATIC, ModifierFlag.FINAL, ModifierFlag.SEALED, ModifierFlag.NON_SEALED)
    ),
    /** An annotation type declaration. */
    ANNOTATION(
        EnumSet.of(AccessLevel.PUBLIC, AccessLevel.PROTECTED, AccessLevel.PACKAGE_PRIVATE, AccessLevel.PRIVATE),
        EnumSet.of(ModifierFlag.ABSTRACT, ModifierFlag.STATIC)
    ),
    /** A concrete instance method. */
    METHOD(
        EnumSet.of(AccessLevel.PUBLIC, AccessLevel.PROTECTED, AccessLevel.PACKAGE_PRIVATE, AccessLevel.PRIVATE),
        EnumSet.of(ModifierFlag.ABSTRACT, ModifierFlag.FINAL, ModifierFlag.STATIC, ModifierFlag.SYNCHRONIZED,
                   ModifierFlag.NATIVE, ModifierFlag.STRICTFP, ModifierFlag.DEFAULT)
    ),
    /** A constructor. */
    CONSTRUCTOR(
        EnumSet.of(AccessLevel.PUBLIC, AccessLevel.PROTECTED, AccessLevel.PACKAGE_PRIVATE, AccessLevel.PRIVATE),
        EnumSet.noneOf(ModifierFlag.class)
    ),
    /** An instance field. */
    FIELD(
        EnumSet.of(AccessLevel.PUBLIC, AccessLevel.PROTECTED, AccessLevel.PACKAGE_PRIVATE, AccessLevel.PRIVATE),
        EnumSet.of(ModifierFlag.FINAL, ModifierFlag.STATIC, ModifierFlag.TRANSIENT, ModifierFlag.VOLATILE)
    ),
    /** A method declared inside an interface. */
    INTERFACE_METHOD(
        EnumSet.of(AccessLevel.PUBLIC, AccessLevel.PRIVATE, AccessLevel.PACKAGE_PRIVATE),
        EnumSet.of(ModifierFlag.ABSTRACT, ModifierFlag.STATIC, ModifierFlag.DEFAULT, ModifierFlag.STRICTFP)
    ),
    /** A field declared inside an interface or annotation type (implicitly {@code public static final}). */
    INTERFACE_FIELD(
        EnumSet.of(AccessLevel.PUBLIC, AccessLevel.PACKAGE_PRIVATE),
        EnumSet.of(ModifierFlag.FINAL, ModifierFlag.STATIC)
    ),
    /** A method or constructor parameter. */
    PARAMETER(
        EnumSet.noneOf(AccessLevel.class),
        EnumSet.of(ModifierFlag.FINAL, ModifierFlag.VARARGS)
    ),
    /** A local variable declaration. */
    LOCAL_VARIABLE(
        EnumSet.noneOf(AccessLevel.class),
        EnumSet.of(ModifierFlag.FINAL)
    ),
    ;

    private final EnumSet<AccessLevel> validAccesses;
    private final EnumSet<ModifierFlag> validFlags;

    ModifierLocation(final EnumSet<AccessLevel> validAccesses, final EnumSet<ModifierFlag> validFlags) {
        this.validAccesses = validAccesses;
        this.validFlags = validFlags;
    }
    /**
     * Tests whether the given modifier is valid in this location.
     *
     * @param modifier the modifier to test
     * @return {@code true} if the modifier is allowed
     */
    public boolean supports(final Modifier modifier) {
        if (modifier instanceof AccessLevel a) {
            return validAccesses.contains(a);
        } else if (modifier instanceof ModifierFlag f) {
            return validFlags.contains(f);
        } else {
            throw new AssertionError("Unknown modifier type: " + modifier);
        }
    }

    /**
     * Tests whether the given access level is valid in this location.
     *
     * @param level the access level to test
     * @return {@code true} if the access level is allowed
     */
    public boolean supports(final AccessLevel level) {
        return validAccesses.contains(level);
    }

    /**
     * Tests whether the given modifier flag is valid in this location.
     *
     * @param flag the flag to test
     * @return {@code true} if the flag is allowed
     */
    public boolean supports(final ModifierFlag flag) {
        return validFlags.contains(flag);
    }

    /**
     * Returns the set of valid access levels for this location.
     *
     * @return an unmodifiable set of valid access levels
     */
    public Set<AccessLevel> validAccesses() {
        return Set.copyOf(validAccesses);
    }

    /**
     * Returns the set of valid modifier flags for this location.
     *
     * @return an unmodifiable set of valid flags
     */
    public Set<ModifierFlag> validFlags() {
        return Set.copyOf(validFlags);
    }
}
