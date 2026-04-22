package org.jboss.jdeparser;

import org.jboss.jdeparser.format.FormatPreferences;
import org.jboss.jdeparser.format.JFiler;
import org.jboss.jdeparser.impl.JSourcesImpl;

/**
 * Entry point for the JDeparser 3 source code generation library.
 * <p>
 * Use {@link #createSources} to obtain a {@link JSources} instance,
 * then define source files using the borrow-pattern API.
 *
 * <pre>{@code
 * JSources sources = JDeparser.createSources(filer, format, SourceVersion.JAVA_17);
 *
 * sources.createSourceFile("com.example", "Greeter", sf -> {
 *     sf.class_("Greeter", cc -> {
 *         cc.public_();
 *         // ... define class members
 *     });
 * });
 *
 * sources.writeSources();
 * }</pre>
 */
public final class JDeparser {

    private JDeparser() {
    }

    /**
     * Creates a new source file collection with the given configuration.
     *
     * @param filer         the filer used to create output files
     * @param preferences   the formatting preferences
     * @param sourceVersion the target Java source version for feature validation
     * @return a new source file collection
     */
    public static JSources createSources(final JFiler filer, final FormatPreferences preferences,
                                         final SourceVersion sourceVersion) {
        return new JSourcesImpl(filer, preferences, sourceVersion);
    }
}
