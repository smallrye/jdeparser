package org.jboss.jdeparser.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jboss.jdeparser.JType;
import org.jboss.jdeparser.SourceVersion;
import org.jboss.jdeparser.creator.DocCommentCreator;

/**
 * Implementation of {@link DocCommentCreator} that collects Javadoc content
 * and writes the complete comment block.
 * <p>
 * The generated comment consists of body text (including inline tags),
 * followed by block tags ({@code @param}, {@code @return}, {@code @throws},
 * {@code @see}, {@code @since}, {@code @deprecated}) in conventional order.
 * <p>
 * {@code @param} tags for method parameters, type parameters, and record
 * components are aggregated from sub-creators and added via
 * {@link #addParamTag(String, String)} and {@link #addTypeParamTag(String, String)}.
 */
public final class DocCommentCreatorImpl extends AbstractCreator implements DocCommentCreator, Writable {

    /** Body text fragments and inline tags, written before block tags. */
    private final List<String> bodyParts = new ArrayList<>();

    /** Accumulated block tags in order. */
    private final List<String> blockTags = new ArrayList<>();

    /** Whether a {@code {@return ...}} inline tag was used. */
    private boolean hasInlineReturn;

    /**
     * Constructs a new doc comment creator.
     *
     * @param version the source version
     */
    public DocCommentCreatorImpl(final SourceVersion version) {
        super(version);
    }

    /** {@inheritDoc} */
    @Override
    public void text(final String text) {
        checkActive();
        bodyParts.add(text);
    }

    /** {@inheritDoc} */
    @Override
    public void returnDoc(final String description) {
        checkActive();
        bodyParts.add("{@return " + description + "}");
        hasInlineReturn = true;
    }

    /** {@inheritDoc} */
    @Override
    public void returnBlockTag(final String description) {
        checkActive();
        blockTags.add("@return " + description);
    }

    /** {@inheritDoc} */
    @Override
    public void throws_(final JType exceptionType, final String description) {
        checkActive();
        blockTags.add("@throws " + typeName(exceptionType) + " " + description);
    }

    /** {@inheritDoc} */
    @Override
    public void see(final String reference) {
        checkActive();
        blockTags.add("@see " + reference);
    }

    /** {@inheritDoc} */
    @Override
    public void since(final String version) {
        checkActive();
        blockTags.add("@since " + version);
    }

    /** {@inheritDoc} */
    @Override
    public void deprecated(final String description) {
        checkActive();
        blockTags.add("@deprecated " + description);
    }

    /** {@inheritDoc} */
    @Override
    public void inlineCode(final String code) {
        checkActive();
        bodyParts.add("{@code " + code + "}");
    }

    /** {@inheritDoc} */
    @Override
    public void inlineLink(final JType type) {
        checkActive();
        bodyParts.add("{@link " + typeName(type) + "}");
    }

    /** {@inheritDoc} */
    @Override
    public void inlineLink(final JType type, final String label) {
        checkActive();
        bodyParts.add("{@link " + typeName(type) + " " + label + "}");
    }

    /**
     * Adds a {@code @param} tag from a sub-creator (parameter or record component).
     * <p>
     * This method is called by the enclosing method/constructor/record creator
     * after the sub-creator callback has finished.
     *
     * @param paramName   the parameter name
     * @param description the parameter description
     */
    public void addParamTag(final String paramName, final String description) {
        blockTags.add("@param " + paramName + " " + description);
    }

    /**
     * Adds a {@code @param <T>} tag from a type parameter sub-creator.
     *
     * @param typeParamName the type parameter name
     * @param description   the type parameter description
     */
    public void addTypeParamTag(final String typeParamName, final String description) {
        blockTags.add("@param <" + typeParamName + "> " + description);
    }

    /**
     * Returns whether this doc comment has any content.
     *
     * @return {@code true} if there are body parts or block tags
     */
    public boolean hasContent() {
        return !bodyParts.isEmpty() || !blockTags.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public void write(final SourceFileWriter writer) throws IOException {
        if (!hasContent()) {
            return;
        }
        // /** opening
        writer.write(Tokens.$COMMENT_TOK.OPEN_DOC);
        writer.nl();
        writer.pushIndent(CommentIndent.BLOCK);

        // body text
        for (String part : bodyParts) {
            writer.writeUnescaped(part);
        }
        if (!bodyParts.isEmpty() && !blockTags.isEmpty()) {
            writer.nl();
        }

        // block tags, each on its own line
        for (String tag : blockTags) {
            writer.nl();
            writer.writeUnescaped(tag);
        }

        writer.nl();
        writer.popIndent(CommentIndent.BLOCK);
        writer.write(Tokens.$COMMENT_TOK.CLOSE);
        writer.nl();
    }

    /**
     * Extracts a type name string from a JType for use in doc comment text.
     *
     * @param type the type
     * @return the type name string
     */
    private static String typeName(final JType type) {
        if (type instanceof ReferenceJType ref) {
            return ref.qualifiedName();
        }
        return type.toString();
    }
}
