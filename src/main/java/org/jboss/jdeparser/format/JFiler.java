package org.jboss.jdeparser.format;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.annotation.processing.Filer;

/**
 * Abstraction for opening output files during source generation.
 * <p>
 * Implementations handle the mapping from package name and file name
 * to an output {@link Writer}.  Factory methods are provided for common
 * output targets: a directory on the filesystem, or an annotation
 * processing {@link Filer}.
 */
public interface JFiler {

    /**
     * Opens a writer for the given source file.
     *
     * @param packageName the package name (e.g. {@code "com.example"})
     * @param fileName    the file name without extension (e.g. {@code "MyClass"})
     * @return a writer to which the source file content should be written
     * @throws IOException if the writer cannot be opened
     */
    Writer openWriter(String packageName, String fileName) throws IOException;

    /**
     * Creates a filer that writes source files under the given directory.
     *
     * @param outputDir the root output directory
     * @return a new filer instance
     */
    static JFiler newInstance(final File outputDir) {
        return newInstance(outputDir.toPath());
    }

    /**
     * Creates a filer that writes source files under the given directory.
     *
     * @param outputDir the root output directory
     * @return a new filer instance
     */
    static JFiler newInstance(final Path outputDir) {
        return (packageName, fileName) -> {
            final Path dir = packageName.isEmpty()
                ? outputDir
                : outputDir.resolve(packageName.replace('.', '/'));
            Files.createDirectories(dir);
            return Files.newBufferedWriter(dir.resolve(fileName + ".java"));
        };
    }

    /**
     * Creates a filer that delegates to an annotation processing {@link Filer}.
     *
     * @param filer the annotation processing filer
     * @return a new filer instance
     */
    static JFiler newInstance(final Filer filer) {
        return (packageName, fileName) -> {
            final String qualifiedName = packageName.isEmpty()
                ? fileName
                : packageName + "." + fileName;
            return filer.createSourceFile(qualifiedName).openWriter();
        };
    }
}
