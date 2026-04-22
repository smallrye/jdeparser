package org.jboss.jdeparser.impl;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.jboss.jdeparser.JSources;
import org.jboss.jdeparser.SourceVersion;
import org.jboss.jdeparser.creator.SourceFileCreator;
import org.jboss.jdeparser.format.FormatPreferences;
import org.jboss.jdeparser.format.JFiler;

/**
 * Implementation of {@link JSources} that manages a collection of source files
 * and writes them via a {@link JFiler}.
 */
public final class JSourcesImpl implements JSources {

    /** The file output abstraction. */
    private final JFiler filer;

    /** The formatting preferences. */
    private final FormatPreferences preferences;

    /** The target source version. */
    private final SourceVersion sourceVersion;

    /** The collected source files. */
    private final List<SourceFileCreatorImpl> sourceFiles = new ArrayList<>();

    /**
     * Constructs a new source file collection.
     *
     * @param filer         the filer for creating output files
     * @param preferences   the formatting preferences
     * @param sourceVersion the target source version
     */
    public JSourcesImpl(final JFiler filer, final FormatPreferences preferences, final SourceVersion sourceVersion) {
        this.filer = filer;
        this.preferences = preferences;
        this.sourceVersion = sourceVersion;
    }

    /** {@inheritDoc} */
    @Override
    public void createSourceFile(final String packageName, final String fileName,
                                  final Consumer<SourceFileCreator> builder) {
        final SourceFileCreatorImpl sf = new SourceFileCreatorImpl(sourceVersion, packageName, fileName);
        builder.accept(sf);
        sf.finish();
        sourceFiles.add(sf);
    }

    /** {@inheritDoc} */
    @Override
    public void writeSources() throws IOException {
        for (SourceFileCreatorImpl sf : sourceFiles) {
            try (Writer out = filer.openWriter(sf.packageName(), sf.fileName());
                 SourceFileWriter writer = new SourceFileWriter(out, preferences, sourceVersion)) {
                sf.write(writer);
            }
        }
    }
}
