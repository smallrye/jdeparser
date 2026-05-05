package io.smallrye.jdeparser.creator;

import java.util.function.Consumer;

import io.smallrye.jdeparser.Sources;
import io.smallrye.jdeparser.Type;
import io.smallrye.jdeparser.impl.SourceFileCreatorImpl;

/**
 * A creator for the contents of a Java source file.
 * <p>
 * Provides methods for adding imports and top-level type declarations
 * within a source file. The source file's package declaration is set
 * when the file is created via
 * {@link Sources#createSourceFile(String, String, Consumer)}.
 * <p>
 * Import methods ({@link #import_(Type)}, {@link #import_(Class)},
 * {@link #importStatic(Type, String)}, {@link #importModule(String)})
 * may be called at any time before the source file is written, including
 * from within nested creator callbacks (e.g., inside a
 * {@link ClassCreator} or {@link MethodCreator} body).
 */
public sealed interface SourceFileCreator permits SourceFileCreatorImpl {

    /**
     * Adds a type import to this source file.
     *
     * @param type the type to import
     */
    void import_(Type type);

    /**
     * Adds a type import from a {@link Class}.
     *
     * @param clazz the class to import
     */
    void import_(Class<?> clazz);

    /**
     * Adds a static import to this source file.
     *
     * @param type the type containing the static member
     * @param member the member name (or {@code "*"} for wildcard)
     */
    void importStatic(Type type, String member);

    /**
     * Adds a module import to this source file (Java 25+).
     *
     * @param moduleName the module name to import
     */
    void importModule(String moduleName);

    /**
     * Defines a top-level class in this source file.
     *
     * @param name the class name
     * @param builder the callback to define the class body
     */
    void class_(String name, Consumer<ClassCreator> builder);

    /**
     * Defines a top-level enum in this source file.
     *
     * @param name the enum name
     * @param builder the callback to define the enum body
     */
    void enum_(String name, Consumer<EnumCreator> builder);

    /**
     * Defines a top-level interface in this source file.
     *
     * @param name the interface name
     * @param builder the callback to define the interface body
     */
    void interface_(String name, Consumer<InterfaceCreator> builder);

    /**
     * Defines a top-level record in this source file.
     *
     * @param name the record name
     * @param builder the callback to define the record body
     */
    void record_(String name, Consumer<RecordCreator> builder);

    /**
     * Defines a top-level annotation type in this source file.
     *
     * @param name the annotation type name
     * @param builder the callback to define the annotation type body
     */
    void annotationInterface_(String name, Consumer<AnnotationInterfaceCreator> builder);

    /**
     * Inserts a blank line at the current position in the source file.
     */
    void blankLine();

    /**
     * Inserts a line comment at the current position.
     *
     * @param comment the comment text (without the leading {@code //})
     */
    void lineComment(String comment);

    /**
     * Inserts a block comment at the current position.
     *
     * @param comment the comment text (without the delimiters)
     */
    void blockComment(String comment);
}
