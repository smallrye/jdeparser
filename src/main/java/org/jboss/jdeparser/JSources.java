package org.jboss.jdeparser;

import java.io.IOException;
import java.util.function.Consumer;

import org.jboss.jdeparser.creator.SourceFileCreator;
import org.jboss.jdeparser.impl.JSourcesImpl;

/**
 * A collection of source files to be generated.
 * <p>
 * Obtained from {@link JDeparser#createSources}, this is the top-level
 * container for defining source files.  After all source files have been
 * defined via {@link #createSourceFile}, call {@link #writeSources()} to
 * write all generated source files to the configured output.
 *
 * @see JDeparser#createSources
 */
public sealed interface JSources permits JSourcesImpl {

    /**
     * Defines a new source file in the given package.
     *
     * @param packageName the package name (e.g., {@code "com.example"})
     * @param fileName    the file name without the {@code .java} extension (e.g., {@code "MyClass"})
     * @param builder     the callback to define the source file contents
     */
    void createSourceFile(String packageName, String fileName, Consumer<SourceFileCreator> builder);

    /**
     * Writes all defined source files to the configured output.
     *
     * @throws IOException if an I/O error occurs while writing
     */
    void writeSources() throws IOException;
}
