package org.jboss.jdeparser.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import io.smallrye.common.constraint.Assert;

import org.jboss.jdeparser.JDocReference;
import org.jboss.jdeparser.JType;
import org.jboss.jdeparser.LanguageFeature;
import org.jboss.jdeparser.SourceVersion;
import org.jboss.jdeparser.creator.DocInlineCreator;

/**
 * Implementation of {@link DocInlineCreator} that collects inline Javadoc
 * content (text, {@code {@code}}, {@code {@link}}, {@code {@linkplain}} tags)
 * and writes them sequentially.
 * <p>
 * This class serves two roles:
 * <ol>
 *   <li>Base class for {@link DocCommentCreatorImpl}, providing all inline
 *       method implementations via inheritance.</li>
 *   <li>Standalone inline content collector used for block tag body content
 *       (e.g., the description in {@code @return} or {@code @throws} tags).</li>
 * </ol>
 * <p>
 * Type names in {@code {@link}} and {@code {@linkplain}} tags are resolved
 * at write time via the
 * {@linkplain SourceFileWriter#resolveClassName(String) class name resolver}.
 */
public non-sealed class DocInlineCreatorImpl extends AbstractCreator implements DocInlineCreator, Writable {

    /** The declaration context, or {@code null} for standalone inline content. */
    private final DocContext context;

    /** Inline content fragments collected during the builder phase. */
    final List<Writable> parts = new ArrayList<>();

    /**
     * Constructs a new inline content creator.
     *
     * @param version    the source version
     * @param sourceFile the enclosing source file creator, or {@code null}
     * @param context    the declaration context for tag validation, or {@code null}
     */
    public DocInlineCreatorImpl(final SourceVersion version, final SourceFileCreatorImpl sourceFile, final DocContext context) {
        super(version);
        this.context = context;
        sourceFile(sourceFile);
    }

    /**
     * Returns the declaration context, or {@code null} for standalone inline content.
     *
     * @return the declaration context
     */
    DocContext context() {
        return context;
    }

    /**
     * Validates that the current context is one of the allowed contexts for a tag.
     *
     * @param tagName         the tag name for error messages
     * @param allowedContexts the set of contexts in which the tag is valid
     * @throws IllegalStateException if the context is not allowed
     */
    void requireContext(final String tagName, final Set<DocContext> allowedContexts) {
        if (context != null && !allowedContexts.contains(context)) {
            throw new IllegalStateException(
                "@" + tagName + " is not valid in " + context.displayName() + " documentation comments"
            );
        }
    }

    /** {@inheritDoc} */
    @Override
    public void text(final String text) {
        checkActive();
        Assert.checkNotNullParam("text", text);
        parts.add(w -> w.writeUnescaped(text));
    }

    /** {@inheritDoc} */
    @Override
    public void code(final String code) {
        checkActive();
        Assert.checkNotNullParam("code", code);
        parts.add(w -> w.writeUnescaped("{@code " + code + "}"));
    }

    /** {@inheritDoc} */
    @Override
    public void link(final JType type) {
        checkActive();
        Assert.checkNotNullParam("type", type);
        registerUsedType(type);
        final String qualifiedName = typeName(type);
        parts.add(w -> w.writeUnescaped("{@link " + w.resolveClassName(qualifiedName) + "}"));
    }

    /** {@inheritDoc} */
    @Override
    public void link(final JType type, final String label) {
        checkActive();
        Assert.checkNotNullParam("type", type);
        Assert.checkNotNullParam("label", label);
        registerUsedType(type);
        final String qualifiedName = typeName(type);
        parts.add(w -> w.writeUnescaped("{@link " + w.resolveClassName(qualifiedName) + " " + label + "}"));
    }

    /** {@inheritDoc} */
    @Override
    public void link(final JDocReference ref) {
        checkActive();
        Assert.checkNotNullParam("ref", ref);
        final DocReferenceImpl impl = (DocReferenceImpl) ref;
        registerUsedType(impl.type());
        final String qualifiedName = typeName(impl.type());
        final String member = impl.member();
        parts.add(w -> w.writeUnescaped("{@link " + w.resolveClassName(qualifiedName) + "#" + member + "}"));
    }

    /** {@inheritDoc} */
    @Override
    public void link(final JDocReference ref, final String label) {
        checkActive();
        Assert.checkNotNullParam("ref", ref);
        Assert.checkNotNullParam("label", label);
        final DocReferenceImpl impl = (DocReferenceImpl) ref;
        registerUsedType(impl.type());
        final String qualifiedName = typeName(impl.type());
        final String member = impl.member();
        parts.add(w -> w.writeUnescaped("{@link " + w.resolveClassName(qualifiedName) + "#" + member + " " + label + "}"));
    }

    /** {@inheritDoc} */
    @Override
    public void linkPlain(final JType type) {
        checkActive();
        Assert.checkNotNullParam("type", type);
        registerUsedType(type);
        final String qualifiedName = typeName(type);
        parts.add(w -> w.writeUnescaped("{@linkplain " + w.resolveClassName(qualifiedName) + "}"));
    }

    /** {@inheritDoc} */
    @Override
    public void linkPlain(final JType type, final String label) {
        checkActive();
        Assert.checkNotNullParam("type", type);
        Assert.checkNotNullParam("label", label);
        registerUsedType(type);
        final String qualifiedName = typeName(type);
        parts.add(w -> w.writeUnescaped("{@linkplain " + w.resolveClassName(qualifiedName) + " " + label + "}"));
    }

    /** {@inheritDoc} */
    @Override
    public void linkPlain(final JDocReference ref) {
        checkActive();
        Assert.checkNotNullParam("ref", ref);
        final DocReferenceImpl impl = (DocReferenceImpl) ref;
        registerUsedType(impl.type());
        final String qualifiedName = typeName(impl.type());
        final String member = impl.member();
        parts.add(w -> w.writeUnescaped("{@linkplain " + w.resolveClassName(qualifiedName) + "#" + member + "}"));
    }

    /** {@inheritDoc} */
    @Override
    public void linkPlain(final JDocReference ref, final String label) {
        checkActive();
        Assert.checkNotNullParam("ref", ref);
        Assert.checkNotNullParam("label", label);
        final DocReferenceImpl impl = (DocReferenceImpl) ref;
        registerUsedType(impl.type());
        final String qualifiedName = typeName(impl.type());
        final String member = impl.member();
        parts.add(w -> w.writeUnescaped("{@linkplain " + w.resolveClassName(qualifiedName) + "#" + member + " " + label + "}"));
    }

    /** {@inheritDoc} */
    @Override
    public void docRoot() {
        checkActive();
        parts.add(w -> w.writeUnescaped("{@docRoot}"));
    }

    /** {@inheritDoc} */
    @Override
    public void index(final String term) {
        checkActive();
        Assert.checkNotNullParam("term", term);
        Assert.checkNotEmptyParam("term", term);
        version().require(LanguageFeature.DOC_INDEX);
        parts.add(w -> w.writeUnescaped("{@index " + term + "}"));
    }

    /** {@inheritDoc} */
    @Override
    public void index(final String term, final String description) {
        checkActive();
        Assert.checkNotNullParam("term", term);
        Assert.checkNotEmptyParam("term", term);
        Assert.checkNotNullParam("description", description);
        version().require(LanguageFeature.DOC_INDEX);
        parts.add(w -> w.writeUnescaped("{@index " + term + " " + description + "}"));
    }

    /** {@inheritDoc} */
    @Override
    public void inheritDoc() {
        checkActive();
        requireContext("inheritDoc", CONTEXT_METHOD);
        parts.add(w -> w.writeUnescaped("{@inheritDoc}"));
    }

    /** {@inheritDoc} */
    @Override
    public void inheritDoc(final JType supertype) {
        checkActive();
        Assert.checkNotNullParam("supertype", supertype);
        requireContext("inheritDoc", CONTEXT_METHOD);
        registerUsedType(supertype);
        final String qualifiedName = typeName(supertype);
        parts.add(w -> w.writeUnescaped("{@inheritDoc " + w.resolveClassName(qualifiedName) + "}"));
    }

    /** {@inheritDoc} */
    @Override
    public void literal(final String text) {
        checkActive();
        Assert.checkNotNullParam("text", text);
        parts.add(w -> w.writeUnescaped("{@literal " + text + "}"));
    }

    /** {@inheritDoc} */
    @Override
    public void summary(final String text) {
        checkActive();
        Assert.checkNotNullParam("text", text);
        version().require(LanguageFeature.DOC_SUMMARY);
        parts.add(w -> w.writeUnescaped("{@summary " + text + "}"));
    }

    /** {@inheritDoc} */
    @Override
    public void systemProperty(final String propertyName) {
        checkActive();
        Assert.checkNotNullParam("propertyName", propertyName);
        Assert.checkNotEmptyParam("propertyName", propertyName);
        version().require(LanguageFeature.DOC_SYSTEM_PROPERTY);
        parts.add(w -> w.writeUnescaped("{@systemProperty " + propertyName + "}"));
    }

    /** {@inheritDoc} */
    @Override
    public void value() {
        checkActive();
        parts.add(w -> w.writeUnescaped("{@value}"));
    }

    /** {@inheritDoc} */
    @Override
    public void value(final JDocReference ref) {
        checkActive();
        Assert.checkNotNullParam("ref", ref);
        final DocReferenceImpl impl = (DocReferenceImpl) ref;
        registerUsedType(impl.type());
        final String qualifiedName = typeName(impl.type());
        final String member = impl.member();
        parts.add(w -> w.writeUnescaped("{@value " + w.resolveClassName(qualifiedName) + "#" + member + "}"));
    }

    // -- Context sets for tag validation --

    /** Methods only. */
    static final Set<DocContext> CONTEXT_METHOD = EnumSet.of(DocContext.METHOD);

    /** Methods and constructors. */
    static final Set<DocContext> CONTEXT_METHOD_CTOR = EnumSet.of(DocContext.METHOD, DocContext.CONSTRUCTOR);

    /** Module, package, type, and overview. */
    static final Set<DocContext> CONTEXT_AUTHOR_VERSION = EnumSet.of(
        DocContext.MODULE, DocContext.PACKAGE, DocContext.TYPE, DocContext.OTHER
    );

    /** Type, method, and field. */
    static final Set<DocContext> CONTEXT_HIDDEN = EnumSet.of(DocContext.TYPE, DocContext.METHOD, DocContext.FIELD);

    /** Module only. */
    static final Set<DocContext> CONTEXT_MODULE = EnumSet.of(DocContext.MODULE);

    /** Package, type, and field. */
    static final Set<DocContext> CONTEXT_SERIAL = EnumSet.of(DocContext.PACKAGE, DocContext.TYPE, DocContext.FIELD);

    /** Package and type only (for {@code @serial include} and {@code @serial exclude}). */
    static final Set<DocContext> CONTEXT_SERIAL_INCLUDE_EXCLUDE = EnumSet.of(DocContext.PACKAGE, DocContext.TYPE);

    /** Field only. */
    static final Set<DocContext> CONTEXT_FIELD = EnumSet.of(DocContext.FIELD);

    /** Module, type, constructor, method, and field. */
    static final Set<DocContext> CONTEXT_DEPRECATED = EnumSet.of(
        DocContext.MODULE, DocContext.TYPE, DocContext.CONSTRUCTOR, DocContext.METHOD, DocContext.FIELD
    );

    /**
     * Returns whether this creator has any inline content fragments.
     *
     * @return {@code true} if there are inline content fragments
     */
    public boolean hasInlineContent() {
        return !parts.isEmpty();
    }

    /**
     * Writes only the inline content fragments, ignoring any block-level
     * structure that a subclass may add.
     * <p>
     * This method is {@code final} so that it always writes just the inline
     * parts, even when called on a {@link DocCommentCreatorImpl} (which
     * overrides {@link #write(SourceFileWriter)} to emit the full comment
     * block).  It is used by {@code @param} tag writers to embed inline
     * content within a block tag.
     *
     * @param writer the source file writer
     * @throws IOException if an I/O error occurs
     */
    public final void writeInline(final SourceFileWriter writer) throws IOException {
        for (Writable part : parts) {
            part.write(writer);
        }
    }

    /**
     * Writes all collected inline content fragments sequentially.
     *
     * @param writer the source file writer
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void write(final SourceFileWriter writer) throws IOException {
        writeInline(writer);
    }

    /**
     * Extracts a type name string from a JType for use in doc comment text.
     *
     * @param type the type
     * @return the type name string
     */
    static String typeName(final JType type) {
        if (type instanceof ReferenceJType ref) {
            return ref.qualifiedName();
        }
        return type.toString();
    }
}
