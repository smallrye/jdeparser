package org.jboss.jdeparser.creator;

import java.util.function.Consumer;

import org.jboss.jdeparser.JDocReference;
import org.jboss.jdeparser.JType;
import org.jboss.jdeparser.impl.DocCommentCreatorImpl;

/**
 * A creator for building Javadoc comment content.
 * <p>
 * Extends {@link DocInlineCreator} with block tag methods
 * ({@code @return}, {@code @throws}, {@code @see}, {@code @since},
 * {@code @deprecated}, etc.) that are only valid at the top level of a
 * doc comment.  Block tag body content may itself contain inline tags;
 * methods that accept a {@link Consumer Consumer&lt;DocInlineCreator&gt;}
 * enable this.
 * <p>
 * Note that {@code @param} tags for parameters, type parameters, and record
 * components are generated automatically from the documentation set on the
 * corresponding {@link ParamCreator}, {@link TypeParamCreator}, or record
 * component creator &mdash; they are not set directly on this interface.
 */
public sealed interface DocCommentCreator extends DocInlineCreator permits DocCommentCreatorImpl {

    /**
     * Adds an {@code @author} tag.
     * <p>
     * Only valid in module, package, type, and overview documentation comments.
     *
     * @param nameText the author name text
     */
    void author(String nameText);

    /**
     * Sets the return documentation using the inline {@code {@return ...}} tag
     * with plain text content, which serves as both the first summary sentence
     * and the {@code @return} block tag.
     * <p>
     * Only valid in method documentation comments.
     * Requires source version {@linkplain org.jboss.jdeparser.SourceVersion#JAVA_16 16}
     * or later.
     * This is a convenience overload that delegates to
     * {@link #returnInline(Consumer)} by wrapping the description in a
     * {@link DocInlineCreator#text(String) text} call.
     *
     * @param description the return description
     */
    default void returnInline(final String description) {
        returnInline(c -> c.text(description));
    }

    /**
     * Sets the return documentation using the inline {@code {@return ...}} tag
     * with rich inline content, which serves as both the first summary sentence
     * and the {@code @return} block tag.
     * <p>
     * Only valid in method documentation comments.
     * Requires source version {@linkplain org.jboss.jdeparser.SourceVersion#JAVA_16 16}
     * or later.
     *
     * @param builder the builder for the inline tag body content
     */
    void returnInline(Consumer<DocInlineCreator> builder);

    /**
     * Adds a {@code @return} block tag with plain text content.
     * <p>
     * Only valid in method documentation comments.
     * This is a convenience overload that delegates to
     * {@link #return_(Consumer)} by wrapping the description in a
     * {@link DocInlineCreator#text(String) text} call.
     *
     * @param description the return description
     */
    default void return_(final String description) {
        return_(c -> c.text(description));
    }

    /**
     * Adds a {@code @return} block tag with rich inline content.
     * <p>
     * Only valid in method documentation comments.
     *
     * @param builder the builder for the block tag body content
     */
    void return_(Consumer<DocInlineCreator> builder);

    /**
     * Adds a {@code @throws} tag with plain text content.
     * <p>
     * Only valid in method and constructor documentation comments.
     * This is a convenience overload that delegates to
     * {@link #throws_(JType, Consumer)} by wrapping the description in a
     * {@link DocInlineCreator#text(String) text} call.
     *
     * @param exceptionType the exception type
     * @param description   the description of when this exception is thrown
     */
    default void throws_(final JType exceptionType, final String description) {
        throws_(exceptionType, c -> c.text(description));
    }

    /**
     * Adds a {@code @throws} tag with rich inline content.
     * <p>
     * Only valid in method and constructor documentation comments.
     *
     * @param exceptionType the exception type
     * @param builder       the builder for the block tag body content
     */
    void throws_(JType exceptionType, Consumer<DocInlineCreator> builder);

    /**
     * Adds a {@code @see} tag referencing a type.
     * <p>
     * The type name is resolved according to import rules at write time.
     *
     * @param type the type to reference
     */
    void see(JType type);

    /**
     * Adds a {@code @see} tag referencing a program element.
     * <p>
     * The type name is resolved according to import rules at write time.
     *
     * @param ref the program element reference
     */
    void see(JDocReference ref);

    /**
     * Adds a {@code @since} tag.
     *
     * @param version the version string
     */
    void since(String version);

    /**
     * Adds a {@code @deprecated} tag with plain text content.
     * <p>
     * Only valid in module, type, constructor, method, and field
     * documentation comments.
     * This is a convenience overload that delegates to
     * {@link #deprecated(Consumer)} by wrapping the description in a
     * {@link DocInlineCreator#text(String) text} call.
     *
     * @param description the deprecation description
     */
    default void deprecated(final String description) {
        deprecated(c -> c.text(description));
    }

    /**
     * Adds a {@code @deprecated} tag with rich inline content.
     * <p>
     * Only valid in module, type, constructor, method, and field
     * documentation comments.
     *
     * @param builder the builder for the block tag body content
     */
    void deprecated(Consumer<DocInlineCreator> builder);

    /**
     * Adds a {@code @hidden} tag to hide the element from generated documentation.
     * <p>
     * Only valid in type, method, and field documentation comments.
     * Requires source version {@linkplain org.jboss.jdeparser.SourceVersion#JAVA_9 9}
     * or later.
     */
    void hidden();

    /**
     * Adds a {@code @provides} tag documenting a service implementation.
     * <p>
     * Only valid in module documentation comments.
     * Requires source version {@linkplain org.jboss.jdeparser.SourceVersion#JAVA_9 9}
     * or later.
     * This is a convenience overload that delegates to
     * {@link #provides(JType, Consumer)}.
     *
     * @param serviceType the service type
     * @param description the description
     */
    default void provides(final JType serviceType, final String description) {
        provides(serviceType, c -> c.text(description));
    }

    /**
     * Adds a {@code @provides} tag with rich inline content.
     * <p>
     * Only valid in module documentation comments.
     * Requires source version {@linkplain org.jboss.jdeparser.SourceVersion#JAVA_9 9}
     * or later.
     *
     * @param serviceType the service type
     * @param builder     the builder for the block tag body content
     */
    void provides(JType serviceType, Consumer<DocInlineCreator> builder);

    /**
     * Adds a {@code @serial} tag with plain text content describing the
     * serialized form of a field.
     * <p>
     * Only valid in package, type, and field documentation comments.
     * This is a convenience overload that delegates to
     * {@link #serial(Consumer)} by wrapping the text in a
     * {@link DocInlineCreator#text(String) text} call.
     * <p>
     * For the special forms {@code @serial include} and {@code @serial exclude},
     * use {@link #serialInclude()} and {@link #serialExclude()} instead.
     *
     * @param text the serial tag text
     */
    default void serial(final String text) {
        serial(c -> c.text(text));
    }

    /**
     * Adds a {@code @serial} tag with rich inline content describing the
     * serialized form of a field.
     * <p>
     * Only valid in package, type, and field documentation comments.
     * <p>
     * For the special forms {@code @serial include} and {@code @serial exclude},
     * use {@link #serialInclude()} and {@link #serialExclude()} instead.
     *
     * @param builder the builder for the block tag body content
     */
    void serial(Consumer<DocInlineCreator> builder);

    /**
     * Adds a {@code @serial include} tag to include a class in the
     * serialized form documentation.
     * <p>
     * Only valid in package and type documentation comments.
     */
    void serialInclude();

    /**
     * Adds a {@code @serial exclude} tag to exclude a class from the
     * serialized form documentation.
     * <p>
     * Only valid in package and type documentation comments.
     */
    void serialExclude();

    /**
     * Adds a {@code @serialData} tag with plain text content.
     * <p>
     * Only valid in method documentation comments (specifically
     * serialization methods such as {@code writeObject}).
     * This is a convenience overload that delegates to
     * {@link #serialData(Consumer)}.
     *
     * @param description the data description
     */
    default void serialData(final String description) {
        serialData(c -> c.text(description));
    }

    /**
     * Adds a {@code @serialData} tag with rich inline content.
     * <p>
     * Only valid in method documentation comments (specifically
     * serialization methods such as {@code writeObject}).
     *
     * @param builder the builder for the block tag body content
     */
    void serialData(Consumer<DocInlineCreator> builder);

    /**
     * Adds a {@code @serialField} tag with plain text content.
     * <p>
     * Only valid in field documentation comments (specifically the
     * {@code serialPersistentFields} field).
     * This is a convenience overload that delegates to
     * {@link #serialField(String, JType, Consumer)}.
     *
     * @param fieldName   the field name
     * @param fieldType   the field type
     * @param description the field description
     */
    default void serialField(final String fieldName, final JType fieldType, final String description) {
        serialField(fieldName, fieldType, c -> c.text(description));
    }

    /**
     * Adds a {@code @serialField} tag with rich inline content.
     * <p>
     * Only valid in field documentation comments (specifically the
     * {@code serialPersistentFields} field).
     *
     * @param fieldName the field name
     * @param fieldType the field type
     * @param builder   the builder for the block tag body content
     */
    void serialField(String fieldName, JType fieldType, Consumer<DocInlineCreator> builder);

    /**
     * Adds a {@code @uses} tag documenting a service dependency.
     * <p>
     * Only valid in module documentation comments.
     * Requires source version {@linkplain org.jboss.jdeparser.SourceVersion#JAVA_9 9}
     * or later.
     * This is a convenience overload that delegates to
     * {@link #uses(JType, Consumer)}.
     *
     * @param serviceType the service type
     * @param description the description
     */
    default void uses(final JType serviceType, final String description) {
        uses(serviceType, c -> c.text(description));
    }

    /**
     * Adds a {@code @uses} tag with rich inline content.
     * <p>
     * Only valid in module documentation comments.
     * Requires source version {@linkplain org.jboss.jdeparser.SourceVersion#JAVA_9 9}
     * or later.
     *
     * @param serviceType the service type
     * @param builder     the builder for the block tag body content
     */
    void uses(JType serviceType, Consumer<DocInlineCreator> builder);

    /**
     * Adds a {@code @version} tag.
     * <p>
     * Only valid in module, package, type, and overview documentation comments.
     *
     * @param versionText the version text
     */
    void version(String versionText);
}
