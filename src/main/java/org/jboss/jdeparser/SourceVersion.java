package org.jboss.jdeparser;

/**
 * Represents a target Java source version for code generation.
 * <p>
 * Each constant corresponds to a Java SE release.  The source version
 * controls which {@linkplain LanguageFeature language features} are
 * permitted during source generation; attempting to use a feature that
 * requires a higher version than the configured target will result in
 * an {@link IllegalStateException} at construction time.
 *
 * @see LanguageFeature
 */
public enum SourceVersion {
    /** Java SE 8. */
    JAVA_8(8),
    /** Java SE 9. */
    JAVA_9(9),
    /** Java SE 10. */
    JAVA_10(10),
    /** Java SE 11. */
    JAVA_11(11),
    /** Java SE 12. */
    JAVA_12(12),
    /** Java SE 13. */
    JAVA_13(13),
    /** Java SE 14. */
    JAVA_14(14),
    /** Java SE 15. */
    JAVA_15(15),
    /** Java SE 16. */
    JAVA_16(16),
    /** Java SE 17. */
    JAVA_17(17),
    /** Java SE 18. */
    JAVA_18(18),
    /** Java SE 19. */
    JAVA_19(19),
    /** Java SE 20. */
    JAVA_20(20),
    /** Java SE 21. */
    JAVA_21(21),
    /** Java SE 22. */
    JAVA_22(22),
    /** Java SE 23. */
    JAVA_23(23),
    /** Java SE 24. */
    JAVA_24(24),
    /** Java SE 25. */
    JAVA_25(25),
    /** Java SE 26. */
    JAVA_26(26),
    ;

    private final int version;

    SourceVersion(final int version) {
        this.version = version;
    }

    /**
     * Returns the numeric Java SE version number.
     *
     * @return the version number (e.g. {@code 17} for {@link #JAVA_17})
     */
    public int version() {
        return version;
    }

    /**
     * Tests whether this source version supports the given language feature.
     *
     * @param feature the language feature to test (must not be {@code null})
     * @return {@code true} if this version is at or above the feature's minimum version
     */
    public boolean supports(final LanguageFeature feature) {
        return version >= feature.minimumVersion().version();
    }

    /**
     * Requires that this source version supports the given language feature,
     * throwing an exception if it does not.
     *
     * @param feature the language feature to require (must not be {@code null})
     * @throws IllegalStateException if this version does not support the feature
     */
    public void require(final LanguageFeature feature) {
        if (!supports(feature)) {
            throw new IllegalStateException(
                feature.name() + " requires " + feature.minimumVersion() + " but target is " + this
            );
        }
    }
}
