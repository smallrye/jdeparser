package org.jboss.jdeparser.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import io.smallrye.common.constraint.Assert;

import org.jboss.jdeparser.JExpr;
import org.jboss.jdeparser.JType;
import org.jboss.jdeparser.SourceVersion;
import org.jboss.jdeparser.creator.AnnotationCreator;
import org.jboss.jdeparser.creator.ClassCreator;
import org.jboss.jdeparser.creator.DocCommentCreator;
import org.jboss.jdeparser.creator.EnumConstantCreator;
import org.jboss.jdeparser.format.FormatPreferences;

/**
 * Implementation of {@link EnumConstantCreator} that collects enum constant
 * configuration and writes the complete constant declaration.
 * <p>
 * Writes the form: {@code [annotations] NAME [(args)] [&#123; body &#125;]}.
 */
public final class EnumConstantCreatorImpl extends AbstractCreator implements EnumConstantCreator, Writable {

    /** The constant name. */
    private final String name;

    /** The annotation holder. */
    private final AnnotationHolder annotations = new AnnotationHolder();

    /** Optional doc comment. */
    private DocCommentCreatorImpl docComment;

    /** Constructor arguments. */
    private final List<JExpr> args = new ArrayList<>();

    /** Optional anonymous class body. */
    private ClassCreatorImpl body;

    /**
     * Constructs a new enum constant creator.
     *
     * @param version the source version
     * @param name    the constant name
     */
    public EnumConstantCreatorImpl(final SourceVersion version, final String name) {
        super(version);
        this.name = name;
    }

    /** {@inheritDoc} */
    @Override
    public void arg(final JExpr value) {
        checkActive();
        Assert.checkNotNullParam("value", value);
        args.add(value);
    }

    /** {@inheritDoc} */
    @Override
    public void body(final Consumer<ClassCreator> builder) {
        checkActive();
        Assert.checkNotNullParam("builder", builder);
        final ClassCreatorImpl cc = new ClassCreatorImpl(version(), name, false);
        cc.sourceFile(sourceFile());
        nest(() -> builder.accept(cc));
        cc.finish();
        this.body = cc;
    }

    /** {@inheritDoc} */
    @Override
    public void annotate(final JType annotationType, final Consumer<AnnotationCreator> builder) {
        checkActive();
        Assert.checkNotNullParam("annotationType", annotationType);
        Assert.checkNotNullParam("builder", builder);
        registerUsedType(annotationType);
        final AnnotationCreatorImpl ac = new AnnotationCreatorImpl(version(), annotationType);
        ac.sourceFile(sourceFile());
        nest(() -> builder.accept(ac));
        ac.finish();
        annotations.add(ac);
    }

    /** {@inheritDoc} */
    @Override
    public void annotate(final JType annotationType) {
        checkActive();
        Assert.checkNotNullParam("annotationType", annotationType);
        registerUsedType(annotationType);
        annotations.add(new AnnotationCreatorImpl(version(), annotationType));
    }

    /** {@inheritDoc} */
    @Override
    public void docComment(final Consumer<DocCommentCreator> builder) {
        checkActive();
        Assert.checkNotNullParam("builder", builder);
        final DocCommentCreatorImpl dc = getOrCreateDocComment();
        nest(() -> builder.accept(dc));
        dc.finish();
    }

    /**
     * Returns the existing doc comment creator, or creates one on demand.
     * <p>
     * If a creator already exists from a prior call, it is reopened
     * for further configuration.
     *
     * @return the doc comment creator
     */
    private DocCommentCreatorImpl getOrCreateDocComment() {
        DocCommentCreatorImpl dc = this.docComment;
        if (dc == null) {
            dc = new DocCommentCreatorImpl(version(), sourceFile(), DocContext.FIELD);
            this.docComment = dc;
        } else {
            dc.reopen();
        }
        return dc;
    }

    /** {@inheritDoc} */
    @Override
    public void write(final SourceFileWriter writer) throws IOException {
        // Write the complete enum constant: optional javadoc, annotations,
        // name, optional constructor arguments, and optional anonymous body.
        if (docComment != null) {
            docComment.write(writer);
        }
        annotations.writeWithNewlines(writer);
        writer.writeName(name);
        if (!args.isEmpty()) {
            writer.write(Tokens.$PAREN.OPEN);
            writer.write(FormatPreferences.Space.WITHIN_PAREN_METHOD_CALL);
            AbstractJExpr.writeList(writer, args, FormatPreferences.Space.AFTER_COMMA,
                FormatPreferences.Wrapping.ARGUMENT_LIST);
            writer.write(FormatPreferences.Space.WITHIN_PAREN_METHOD_CALL);
            writer.write(Tokens.$PAREN.CLOSE);
        } else if (writer.getFormat().hasOption(FormatPreferences.Opt.ENUM_EMPTY_PARENS)) {
            writer.write(Tokens.$PAREN.OPEN);
            writer.write(FormatPreferences.Space.WITHIN_PAREN_METHOD_CALL_EMPTY);
            writer.write(Tokens.$PAREN.CLOSE);
        }
        if (body != null) {
            writer.write(FormatPreferences.Space.BEFORE_BRACE_CLASS);
            writer.write(Tokens.$BRACE.OPEN);
            BlockCreatorImpl initBlock;
            if (writer.getFormat().hasOption(FormatPreferences.Opt.COMPACT_INIT_ONLY_CLASS)
                    && (initBlock = body.soleInitBlock()) != null) {
                initBlock.writeBlock(writer);
            } else if (body.hasMembers()) {
                writer.nl();
                writer.pushIndent(FormatPreferences.Indentation.MEMBERS_TOP_LEVEL);
                body.writeBody(writer);
                writer.popIndent(FormatPreferences.Indentation.MEMBERS_TOP_LEVEL);
            } else {
                writer.write(FormatPreferences.Space.WITHIN_BRACES_EMPTY);
            }
            writer.write(Tokens.$BRACE.CLOSE);
        }
    }
}
