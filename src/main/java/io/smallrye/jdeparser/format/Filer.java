package io.smallrye.jdeparser.format;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.lang.model.element.Element;

/**
 * Abstraction for opening output files during source generation.
 * <p>
 * Implementations handle the mapping from package name and file name
 * to an output {@link Writer}. Factory methods are provided for common
 * output targets: a directory on the filesystem, or an annotation
 * processing {@link javax.annotation.processing.Filer}.
 * <p>
 * Implementations only need to provide the two-argument
 * {@link #openWriter(String, String)} method; the three-argument overload
 * defaults to ignoring originating elements. Implementations that care
 * about originating elements (such as the one backed by an annotation
 * processing {@link javax.annotation.processing.Filer}) can override
 * {@link #openWriter(String, String, List)} as well.
 */
public interface Filer {

    /**
     * Opens a writer for the given source file.
     *
     * @param packageName the package name (e.g. {@code "com.example"})
     * @param fileName the file name without extension (e.g. {@code "MyClass"})
     * @return a writer to which the source file content should be written
     * @throws IOException if the writer cannot be opened
     */
    Writer openWriter(String packageName, String fileName) throws IOException;

    /**
     * Opens a writer for the given source file, associating it with the
     * given originating elements.
     * <p>
     * The default implementation ignores the originating elements and
     * delegates to {@link #openWriter(String, String)}. Implementations
     * that support originating elements should override this method as well.
     *
     * @param packageName the package name (e.g. {@code "com.example"})
     * @param fileName the file name without extension (e.g. {@code "MyClass"})
     * @param originatingElements the elements that caused this file to be generated
     * @return a writer to which the source file content should be written
     * @throws IOException if the writer cannot be opened
     */
    default Writer openWriter(String packageName, String fileName,
            List<Element> originatingElements) throws IOException {
        return openWriter(packageName, fileName);
    }

    /**
     * Creates a filer that writes source files under the given directory.
     *
     * @param outputDir the root output directory
     * @return a new filer instance
     */
    static Filer newInstance(final File outputDir) {
        return newInstance(outputDir.toPath());
    }

    /**
     * Creates a filer that writes source files under the given directory.
     *
     * @param outputDir the root output directory
     * @return a new filer instance
     */
    static Filer newInstance(final Path outputDir) {
        return (packageName, fileName) -> {
            final Path dir = packageName.isEmpty()
                    ? outputDir
                    : outputDir.resolve(packageName.replace('.', '/'));
            Files.createDirectories(dir);
            return Files.newBufferedWriter(dir.resolve(fileName + ".java"));
        };
    }

    /**
     * Creates a filer that delegates to an annotation processing {@link javax.annotation.processing.Filer}.
     *
     * @param filer the annotation processing filer
     * @return a new filer instance
     */
    static Filer newInstance(final javax.annotation.processing.Filer filer) {
        return new Filer() {
            @Override
            public Writer openWriter(final String packageName,
                    final String fileName) throws IOException {
                return openWriter(packageName, fileName, List.of());
            }

            @Override
            public Writer openWriter(final String packageName, final String fileName,
                    final List<Element> originatingElements) throws IOException {
                final String qualifiedName = packageName.isEmpty()
                        ? fileName
                        : packageName + "." + fileName;
                return filer.createSourceFile(qualifiedName,
                        originatingElements.toArray(new Element[0])).openWriter();
            }
        };
    }
}
