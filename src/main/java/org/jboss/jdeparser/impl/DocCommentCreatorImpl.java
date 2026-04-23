package org.jboss.jdeparser.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import io.smallrye.common.constraint.Assert;

import org.jboss.jdeparser.JDocReference;
import org.jboss.jdeparser.JType;
import org.jboss.jdeparser.LanguageFeature;
import org.jboss.jdeparser.SourceVersion;
import org.jboss.jdeparser.creator.DocCommentCreator;
import org.jboss.jdeparser.creator.DocInlineCreator;

/**
 * Implementation of {@link DocCommentCreator} that collects Javadoc content
 * and writes the complete comment block.
 * <p>
 * The generated comment consists of body text (including inline tags,
 * inherited from {@link DocInlineCreatorImpl}), followed by block tags
 * ({@code @param}, {@code @return}, {@code @throws}, {@code @see},
 * {@code @since}, {@code @deprecated}) in conventional order.
 * <p>
 * {@code @param} tags for method parameters, type parameters, and record
 * components are aggregated from sub-creators and added via
 * {@link #addParamTag(String, String)} and {@link #addTypeParamTag(String, String)}.
 * <p>
 * Type names in {@code {@link}}, {@code {@linkplain}}, {@code @throws},
 * and {@code @see} tags are resolved at write time via the
 * {@linkplain SourceFileWriter#resolveClassName(String) class name resolver},
 * so that import resolution applies consistently to type references in
 * doc comments.
 */
public final class DocCommentCreatorImpl extends DocInlineCreatorImpl implements DocCommentCreator {

    /** Accumulated block tags in order. */
    private final List<Writable> blockTags = new ArrayList<>();

    /** Whether a {@code {@return ...}} inline tag was used. */
    private boolean hasInlineReturn;

    /**
     * Constructs a new doc comment creator.
     *
     * @param version    the source version
     * @param sourceFile the enclosing source file creator, or {@code null}
     * @param context    the declaration context for tag validation, or {@code null}
     */
    public DocCommentCreatorImpl(final SourceVersion version, final SourceFileCreatorImpl sourceFile, final DocContext context) {
        super(version, sourceFile, context);
    }

    /** {@inheritDoc} */
    @Override
    public void author(final String nameText) {
        checkActive();
        Assert.checkNotNullParam("nameText", nameText);
        requireContext("author", CONTEXT_AUTHOR_VERSION);
        blockTags.add(w -> w.writeUnescaped("@author " + nameText));
    }

    /** {@inheritDoc} */
    @Override
    public void returnInline(final String description) {
        checkActive();
        Assert.checkNotNullParam("description", description);
        requireContext("return", CONTEXT_METHOD);
        version().require(LanguageFeature.DOC_RETURN_INLINE);
        parts.add(w -> w.writeUnescaped("{@return " + description + "}"));
        hasInlineReturn = true;
    }

    /** {@inheritDoc} */
    @Override
    public void return_(final Consumer<DocInlineCreator> builder) {
        checkActive();
        Assert.checkNotNullParam("builder", builder);
        requireContext("return", CONTEXT_METHOD);
        final DocInlineCreatorImpl ic = new DocInlineCreatorImpl(version(), sourceFile(), context());
        nest(() -> builder.accept(ic));
        ic.finish();
        blockTags.add(w -> {
            w.writeUnescaped("@return ");
            ic.write(w);
        });
    }

    /** {@inheritDoc} */
    @Override
    public void throws_(final JType exceptionType, final Consumer<DocInlineCreator> builder) {
        checkActive();
        Assert.checkNotNullParam("exceptionType", exceptionType);
        Assert.checkNotNullParam("builder", builder);
        requireContext("throws", CONTEXT_METHOD_CTOR);
        registerUsedType(exceptionType);
        final String qualifiedName = typeName(exceptionType);
        final DocInlineCreatorImpl ic = new DocInlineCreatorImpl(version(), sourceFile(), context());
        nest(() -> builder.accept(ic));
        ic.finish();
        blockTags.add(w -> {
            w.writeUnescaped("@throws " + w.resolveClassName(qualifiedName) + " ");
            ic.write(w);
        });
    }

    /** {@inheritDoc} */
    @Override
    public void see(final JType type) {
        checkActive();
        Assert.checkNotNullParam("type", type);
        registerUsedType(type);
        final String qualifiedName = typeName(type);
        blockTags.add(w -> w.writeUnescaped("@see " + w.resolveClassName(qualifiedName)));
    }

    /** {@inheritDoc} */
    @Override
    public void see(final JDocReference ref) {
        checkActive();
        Assert.checkNotNullParam("ref", ref);
        final DocReferenceImpl impl = (DocReferenceImpl) ref;
        registerUsedType(impl.type());
        final String qualifiedName = typeName(impl.type());
        final String member = impl.member();
        blockTags.add(w -> w.writeUnescaped("@see " + w.resolveClassName(qualifiedName) + "#" + member));
    }

    /** {@inheritDoc} */
    @Override
    public void since(final String version) {
        checkActive();
        Assert.checkNotNullParam("version", version);
        blockTags.add(w -> w.writeUnescaped("@since " + version));
    }

    /** {@inheritDoc} */
    @Override
    public void deprecated(final Consumer<DocInlineCreator> builder) {
        checkActive();
        Assert.checkNotNullParam("builder", builder);
        requireContext("deprecated", CONTEXT_DEPRECATED);
        final DocInlineCreatorImpl ic = new DocInlineCreatorImpl(version(), sourceFile(), context());
        nest(() -> builder.accept(ic));
        ic.finish();
        blockTags.add(w -> {
            w.writeUnescaped("@deprecated ");
            ic.write(w);
        });
    }

    /** {@inheritDoc} */
    @Override
    public void hidden() {
        checkActive();
        version().require(LanguageFeature.DOC_HIDDEN);
        requireContext("hidden", CONTEXT_HIDDEN);
        blockTags.add(w -> w.writeUnescaped("@hidden"));
    }

    /** {@inheritDoc} */
    @Override
    public void provides(final JType serviceType, final Consumer<DocInlineCreator> builder) {
        checkActive();
        Assert.checkNotNullParam("serviceType", serviceType);
        Assert.checkNotNullParam("builder", builder);
        version().require(LanguageFeature.DOC_PROVIDES);
        requireContext("provides", CONTEXT_MODULE);
        registerUsedType(serviceType);
        final String qualifiedName = typeName(serviceType);
        final DocInlineCreatorImpl ic = new DocInlineCreatorImpl(version(), sourceFile(), context());
        nest(() -> builder.accept(ic));
        ic.finish();
        blockTags.add(w -> {
            w.writeUnescaped("@provides " + w.resolveClassName(qualifiedName) + " ");
            ic.write(w);
        });
    }

    /** {@inheritDoc} */
    @Override
    public void serial(final String text) {
        checkActive();
        Assert.checkNotNullParam("text", text);
        requireContext("serial", CONTEXT_SERIAL);
        blockTags.add(w -> w.writeUnescaped("@serial " + text));
    }

    /** {@inheritDoc} */
    @Override
    public void serialData(final Consumer<DocInlineCreator> builder) {
        checkActive();
        Assert.checkNotNullParam("builder", builder);
        requireContext("serialData", CONTEXT_METHOD);
        final DocInlineCreatorImpl ic = new DocInlineCreatorImpl(version(), sourceFile(), context());
        nest(() -> builder.accept(ic));
        ic.finish();
        blockTags.add(w -> {
            w.writeUnescaped("@serialData ");
            ic.write(w);
        });
    }

    /** {@inheritDoc} */
    @Override
    public void serialField(final String fieldName, final JType fieldType, final Consumer<DocInlineCreator> builder) {
        checkActive();
        Assert.checkNotNullParam("fieldName", fieldName);
        Assert.checkNotEmptyParam("fieldName", fieldName);
        Assert.checkNotNullParam("fieldType", fieldType);
        Assert.checkNotNullParam("builder", builder);
        requireContext("serialField", CONTEXT_FIELD);
        registerUsedType(fieldType);
        final String qualifiedName = typeName(fieldType);
        final DocInlineCreatorImpl ic = new DocInlineCreatorImpl(version(), sourceFile(), context());
        nest(() -> builder.accept(ic));
        ic.finish();
        blockTags.add(w -> {
            w.writeUnescaped("@serialField " + fieldName + " " + w.resolveClassName(qualifiedName) + " ");
            ic.write(w);
        });
    }

    /** {@inheritDoc} */
    @Override
    public void uses(final JType serviceType, final Consumer<DocInlineCreator> builder) {
        checkActive();
        Assert.checkNotNullParam("serviceType", serviceType);
        Assert.checkNotNullParam("builder", builder);
        version().require(LanguageFeature.DOC_USES);
        requireContext("uses", CONTEXT_MODULE);
        registerUsedType(serviceType);
        final String qualifiedName = typeName(serviceType);
        final DocInlineCreatorImpl ic = new DocInlineCreatorImpl(version(), sourceFile(), context());
        nest(() -> builder.accept(ic));
        ic.finish();
        blockTags.add(w -> {
            w.writeUnescaped("@uses " + w.resolveClassName(qualifiedName) + " ");
            ic.write(w);
        });
    }

    /** {@inheritDoc} */
    @Override
    public void version(final String versionText) {
        checkActive();
        Assert.checkNotNullParam("versionText", versionText);
        requireContext("version", CONTEXT_AUTHOR_VERSION);
        blockTags.add(w -> w.writeUnescaped("@version " + versionText));
    }

    /**
     * Adds a {@code @param} tag from a sub-creator (parameter or record component).
     * <p>
     * This method is called eagerly by the enclosing method/constructor/record
     * creator when a parameter or record component has documentation, so that
     * write-time aggregation is not needed.
     *
     * @param paramName   the parameter name
     * @param description the inline content for the parameter description, or {@code null}
     */
    public void addParamTag(final String paramName, final DocInlineCreatorImpl description) {
        blockTags.add(w -> {
            w.writeUnescaped("@param " + paramName);
            if (description != null && description.hasInlineContent()) {
                w.ntsp();
                description.writeInline(w);
            }
        });
    }

    /**
     * Adds a {@code @param <T>} tag from a type parameter sub-creator.
     * <p>
     * This method is called eagerly by the enclosing type/method/constructor
     * creator when a type parameter has documentation, so that write-time
     * aggregation is not needed.
     *
     * @param typeParamName the type parameter name
     * @param description   the inline content for the type parameter description, or {@code null}
     */
    public void addTypeParamTag(final String typeParamName, final DocInlineCreatorImpl description) {
        blockTags.add(w -> {
            w.writeUnescaped("@param <" + typeParamName + ">");
            if (description != null && description.hasInlineContent()) {
                w.ntsp();
                description.writeInline(w);
            }
        });
    }

    /**
     * Returns whether this doc comment has any content.
     *
     * @return {@code true} if there are body parts or block tags
     */
    public boolean hasContent() {
        return !parts.isEmpty() || !blockTags.isEmpty();
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

        // body text (inline content from inherited parts list)
        for (Writable part : parts) {
            part.write(writer);
        }
        if (!parts.isEmpty() && !blockTags.isEmpty()) {
            writer.nl();
        }

        // block tags, each on its own line
        for (Writable tag : blockTags) {
            writer.nl();
            tag.write(writer);
        }

        writer.nl();
        writer.popIndent(CommentIndent.BLOCK);
        writer.pushIndent(CommentIndent.BLOCK_CLOSE);
        writer.write(Tokens.$COMMENT_TOK.CLOSE);
        writer.popIndent(CommentIndent.BLOCK_CLOSE);
        writer.nl();
    }
}
