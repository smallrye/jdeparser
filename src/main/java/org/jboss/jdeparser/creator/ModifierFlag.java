package org.jboss.jdeparser.creator;

import java.util.EnumSet;
import java.util.function.Consumer;

/**
 * Non-access modifier flags for Java declarations.
 * <p>
 * Each constant represents a modifier keyword that can be applied to
 * various Java declarations.  Not every flag is valid in every context;
 * see {@link ModifierLocation} for context-specific validation.
 *
 * @see Modifier
 * @see ModifiableCreator#addFlag(ModifierFlag)
 */
public enum ModifierFlag implements Modifier {
    /** {@code abstract} modifier. */
    ABSTRACT("abstract"),
    /** {@code final} modifier. */
    FINAL("final"),
    /** {@code static} modifier. */
    STATIC("static"),
    /** {@code synchronized} modifier. */
    SYNCHRONIZED("synchronized"),
    /** {@code native} modifier. */
    NATIVE("native"),
    /** {@code strictfp} modifier. */
    STRICTFP("strictfp"),
    /** {@code transient} modifier. */
    TRANSIENT("transient"),
    /** {@code volatile} modifier. */
    VOLATILE("volatile"),
    /** {@code default} modifier (for interface methods). */
    DEFAULT("default"),
    /** {@code sealed} modifier (Java 17+). */
    SEALED("sealed"),
    /** {@code non-sealed} modifier (Java 17+). */
    NON_SEALED("non-sealed"),
    /** Variable arity (varargs) marker; rendered as {@code ...} in parameter declarations. */
    VARARGS("..."),
    ;

    private final String keyword;
    private EnumSet<ModifierFlag> exclusive;

    ModifierFlag(final String keyword) {
        this.keyword = keyword;
    }

    static {
        // Mutual exclusion relationships (symmetric; if A excludes B then B excludes A).
        // Context-specific validation is handled separately by ModifierLocation.
        ABSTRACT.exclusive = EnumSet.of(FINAL, NATIVE);
        FINAL.exclusive = EnumSet.of(ABSTRACT, VOLATILE, SEALED, NON_SEALED);
        STATIC.exclusive = EnumSet.noneOf(ModifierFlag.class);
        SYNCHRONIZED.exclusive = EnumSet.noneOf(ModifierFlag.class);
        NATIVE.exclusive = EnumSet.of(ABSTRACT);
        STRICTFP.exclusive = EnumSet.noneOf(ModifierFlag.class);
        TRANSIENT.exclusive = EnumSet.noneOf(ModifierFlag.class);
        VOLATILE.exclusive = EnumSet.of(FINAL);
        DEFAULT.exclusive = EnumSet.noneOf(ModifierFlag.class);
        SEALED.exclusive = EnumSet.of(FINAL, NON_SEALED);
        NON_SEALED.exclusive = EnumSet.of(FINAL, SEALED);
        VARARGS.exclusive = EnumSet.noneOf(ModifierFlag.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String keyword() {
        return keyword;
    }

    /**
     * Invokes the given action for each modifier flag that is mutually
     * exclusive with this flag.
     * <p>
     * This captures universal exclusion relationships (e.g. {@code abstract}
     * and {@code final} are always exclusive).  Context-specific restrictions
     * (e.g. which flags are valid on a method vs. a field) are handled
     * separately by {@link ModifierLocation}.
     *
     * @param action the action to perform for each mutually exclusive flag
     */
    public void forEachExclusive(final Consumer<ModifierFlag> action) {
        for (ModifierFlag flag : exclusive) {
            action.accept(flag);
        }
    }

    /**
     * Tests whether this flag is mutually exclusive with the given flag.
     *
     * @param other the other flag to test
     * @return {@code true} if the two flags cannot be used together
     */
    public boolean isExclusiveWith(final ModifierFlag other) {
        return exclusive.contains(other);
    }
}
