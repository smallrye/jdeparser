package io.smallrye.jdeparser.impl;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import io.smallrye.common.constraint.Assert;
import io.smallrye.jdeparser.SourceVersion;
import io.smallrye.jdeparser.Sources;
import io.smallrye.jdeparser.creator.SourceFileCreator;
import io.smallrye.jdeparser.format.Filer;
import io.smallrye.jdeparser.format.FormatPreferences;

/**
 * Implementation of {@link Sources} that manages a collection of source files
 * and writes them via a {@link Filer}.
 */
public final class SourcesImpl implements Sources {

    /** The file output abstraction. */
    private final Filer filer;

    /** The formatting preferences. */
    private final FormatPreferences preferences;

    /** The target source version. */
    private final SourceVersion sourceVersion;

    /** The collected source files. */
    private final List<SourceFileCreatorImpl> sourceFiles = new ArrayList<>();

    /** Defined type simple names indexed by package name. */
    private final Map<String, Set<String>> definedTypesByPackage = new LinkedHashMap<>();

    /**
     * Constructs a new source file collection.
     *
     * @param filer the filer for creating output files
     * @param preferences the formatting preferences
     * @param sourceVersion the target source version
     */
    public SourcesImpl(final Filer filer, final FormatPreferences preferences, final SourceVersion sourceVersion) {
        this.filer = filer;
        this.preferences = preferences;
        this.sourceVersion = sourceVersion;
    }

    /** {@inheritDoc} */
    @Override
    public void createSourceFile(final String packageName, final String fileName,
            final Consumer<SourceFileCreator> builder) {
        Assert.checkNotNullParam("packageName", packageName);
        Assert.checkNotNullParam("fileName", fileName);
        Assert.checkNotEmptyParam("fileName", fileName);
        Assert.checkNotNullParam("builder", builder);
        final SourceFileCreatorImpl sf = new SourceFileCreatorImpl(this, sourceVersion, packageName, fileName);
        definedTypesByPackage.computeIfAbsent(packageName, k -> new LinkedHashSet<>()).add(fileName);
        builder.accept(sf);
        sf.finish();
        sourceFiles.add(sf);
    }

    /**
     * Returns the set of simple type names defined in the given package.
     *
     * @param packageName the package name
     * @return the set of simple names, or an empty set if none
     */
    Set<String> getDefinedSimpleNames(final String packageName) {
        return definedTypesByPackage.getOrDefault(packageName, Collections.emptySet());
    }

    /** {@inheritDoc} */
    @Override
    public void writeSources() throws IOException {
        for (SourceFileCreatorImpl sf : sourceFiles) {
            try (Writer out = filer.openWriter(sf.packageName(), sf.fileName(), sf.originatingElements());
                    SourceFileWriter writer = new SourceFileWriter(out, preferences, sourceVersion)) {
                sf.write(writer);
            }
        }
    }
}
