package org.jboss.jdeparser.creator;

import org.jboss.jdeparser.JType;
import org.jboss.jdeparser.impl.DocCommentCreatorImpl;

/**
 * A creator for building Javadoc comment content.
 * <p>
 * Note that {@code @param} tags for parameters, type parameters, and record
 * components are generated automatically from the documentation set on the
 * corresponding {@link ParamCreator}, {@link TypeParamCreator}, or record
 * component creator &mdash; they are not set directly on this interface.
 */
public sealed interface DocCommentCreator permits DocCommentCreatorImpl {

    /**
     * Adds a block of text to the doc comment body.
     *
     * @param text the text (may contain HTML)
     */
    void text(String text);

    /**
     * Sets the return documentation using the inline {@code {@return ...}} tag,
     * which serves as both the first summary sentence and the {@code @return} block tag.
     *
     * @param description the return description
     */
    void returnDoc(String description);

    /**
     * Sets only the {@code @return} block tag (traditional style, without the inline tag).
     *
     * @param description the return description
     */
    void returnBlockTag(String description);

    /**
     * Adds a {@code @throws} tag.
     *
     * @param exceptionType the exception type
     * @param description   the description of when this exception is thrown
     */
    void throws_(JType exceptionType, String description);

    /**
     * Adds a {@code @see} tag.
     *
     * @param reference the see reference (e.g., {@code "OtherClass#method()"})
     */
    void see(String reference);

    /**
     * Adds a {@code @since} tag.
     *
     * @param version the version string
     */
    void since(String version);

    /**
     * Adds a {@code @deprecated} tag.
     *
     * @param description the deprecation description
     */
    void deprecated(String description);

    /**
     * Adds an inline {@code {@code ...}} tag to the current text.
     *
     * @param code the code text
     */
    void inlineCode(String code);

    /**
     * Adds an inline {@code {@link ...}} tag referencing a type.
     *
     * @param type the type to link to
     */
    void inlineLink(JType type);

    /**
     * Adds an inline {@code {@link ...}} tag referencing a type with display text.
     *
     * @param type  the type to link to
     * @param label the display text for the link
     */
    void inlineLink(JType type, String label);
}
