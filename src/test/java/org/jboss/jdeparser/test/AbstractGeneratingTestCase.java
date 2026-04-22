package org.jboss.jdeparser.test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jboss.jdeparser.JDeparser;
import org.jboss.jdeparser.JSources;
import org.jboss.jdeparser.SourceVersion;
import org.jboss.jdeparser.format.FormatPreferences;
import org.jboss.jdeparser.format.JFiler;

/**
 * Base class for source generation tests.
 * <p>
 * Provides an in-memory {@link JFiler} that captures generated source
 * content into {@link StringWriter} instances, retrievable by package
 * and file name after {@link JSources#writeSources()} completes.
 */
public abstract class AbstractGeneratingTestCase {

    /** In-memory store mapping (package, file) keys to captured source content. */
    private final ConcurrentMap<Key, StringWriter> sourceFiles = new ConcurrentHashMap<>();

    /** In-memory filer that captures output into StringWriters. */
    private final JFiler filer = (packageName, fileName) -> {
        final Key key = new Key(packageName, fileName);
        final StringWriter writer = new StringWriter();
        if (sourceFiles.putIfAbsent(key, writer) != null) {
            throw new IOException("File already exists: " + packageName + "." + fileName);
        }
        return writer;
    };

    /**
     * Creates a new {@link JSources} instance with default format preferences
     * and the given source version.
     *
     * @param version the target source version
     * @return a new sources instance backed by the in-memory filer
     */
    protected JSources createSources(final SourceVersion version) {
        return JDeparser.createSources(filer, FormatPreferences.defaults(), version);
    }

    /**
     * Creates a new {@link JSources} instance with the given format preferences
     * and source version.
     *
     * @param preferences the format preferences
     * @param version     the target source version
     * @return a new sources instance backed by the in-memory filer
     */
    protected JSources createSources(final FormatPreferences preferences, final SourceVersion version) {
        return JDeparser.createSources(filer, preferences, version);
    }

    /**
     * Retrieves the generated source content for the given package and file name.
     *
     * @param packageName the package name (e.g. {@code "com.example"})
     * @param fileName    the file name without extension (e.g. {@code "MyClass"})
     * @return the generated source content as a string
     * @throws IllegalArgumentException if no file was generated with the given key
     */
    protected String getSource(final String packageName, final String fileName) {
        final StringWriter writer = sourceFiles.get(new Key(packageName, fileName));
        if (writer == null) {
            throw new IllegalArgumentException(
                "No generated file for package '" + packageName + "', file '" + fileName + "'");
        }
        return writer.toString();
    }

    /**
     * Clears all captured source files, allowing the test to generate fresh output.
     */
    protected void clearSources() {
        sourceFiles.clear();
    }

    /**
     * Key for the in-memory source file map.
     *
     * @param packageName the package name
     * @param fileName    the file name (without extension)
     */
    private record Key(String packageName, String fileName) {
    }
}
