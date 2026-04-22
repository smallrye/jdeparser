package org.jboss.jdeparser;

/**
 * Java language features that are gated by source version.
 * <p>
 * Each constant identifies a language feature along with the minimum
 * {@link SourceVersion} required to use it.  The library checks these
 * at construction time to prevent generating source code that is invalid
 * for the configured target version.
 *
 * @see SourceVersion#supports(LanguageFeature)
 */
public enum LanguageFeature {

    // ---- Java 9 ----

    /** Module declarations ({@code module-info.java}). */
    MODULE_DECLARATIONS(SourceVersion.JAVA_9),
    /** Private methods in interfaces. */
    PRIVATE_INTERFACE_METHODS(SourceVersion.JAVA_9),
    /** Diamond operator ({@code <>}) in anonymous inner classes. */
    DIAMOND_ANONYMOUS_CLASSES(SourceVersion.JAVA_9),

    // ---- Java 10 ----

    /** Local variable type inference ({@code var}). */
    VAR_LOCAL_VARIABLE(SourceVersion.JAVA_10),

    // ---- Java 14 ----

    /** Switch expressions ({@code switch} as an expression with {@code yield}). */
    SWITCH_EXPRESSIONS(SourceVersion.JAVA_14),

    // ---- Java 15 ----

    /** Text blocks (multi-line string literals delimited by {@code """}). */
    TEXT_BLOCKS(SourceVersion.JAVA_15),

    // ---- Java 16 ----

    /** Record declarations. */
    RECORDS(SourceVersion.JAVA_16),
    /** Pattern matching for {@code instanceof} with binding variables. */
    PATTERN_MATCHING_INSTANCEOF(SourceVersion.JAVA_16),
    /** Local enum and interface declarations inside method bodies. */
    LOCAL_INTERFACES(SourceVersion.JAVA_16),

    // ---- Java 17 ----

    /** Sealed classes and interfaces ({@code sealed}, {@code non-sealed}, {@code permits}). */
    SEALED_CLASSES(SourceVersion.JAVA_17),

    // ---- Java 21 ----

    /** Pattern matching for {@code switch} (type patterns, guarded patterns). */
    PATTERN_MATCHING_SWITCH(SourceVersion.JAVA_21),
    /** Record patterns in {@code instanceof} and {@code switch}. */
    RECORD_PATTERNS(SourceVersion.JAVA_21),

    // ---- Java 22 ----

    /** Unnamed variables and patterns ({@code _}). */
    UNNAMED_VARIABLES(SourceVersion.JAVA_22),

    // ---- Java 25 ----

    /** Module import declarations ({@code import module java.base}). */
    MODULE_IMPORTS(SourceVersion.JAVA_25),
    /** Flexible constructor bodies (statements before {@code this()}/{@code super()}). */
    FLEXIBLE_CONSTRUCTOR_BODIES(SourceVersion.JAVA_25),
    /** Compact source files (implicitly declared classes). */
    COMPACT_SOURCE_FILES(SourceVersion.JAVA_25),
    ;

    private final SourceVersion minimumVersion;

    LanguageFeature(final SourceVersion minimumVersion) {
        this.minimumVersion = minimumVersion;
    }

    /**
     * Returns the minimum source version required to use this feature.
     *
     * @return the minimum source version (never {@code null})
     */
    public SourceVersion minimumVersion() {
        return minimumVersion;
    }
}
