package org.jboss.jdeparser.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import io.smallrye.common.constraint.Assert;

import org.jboss.jdeparser.JType;
import org.jboss.jdeparser.SourceVersion;
import org.jboss.jdeparser.creator.AccessLevel;
import org.jboss.jdeparser.creator.AnnotationCreator;
import org.jboss.jdeparser.creator.BlockCreator;
import org.jboss.jdeparser.creator.ConstructorCreator;
import org.jboss.jdeparser.creator.DocCommentCreator;
import org.jboss.jdeparser.creator.ModifierFlag;
import org.jboss.jdeparser.creator.ModifierLocation;
import org.jboss.jdeparser.creator.ParamCreator;
import org.jboss.jdeparser.creator.TypeParamCreator;
import org.jboss.jdeparser.format.FormatPreferences;

/**
 * Implementation of {@link ConstructorCreator} that collects constructor
 * configuration and writes the complete constructor declaration.
 * <p>
 * Writes the form: {@code [javadoc] [annotations] [modifiers]
 * ClassName([params]) [throws Ex1, Ex2] { body }}.
 * <p>
 * The constructor name (the enclosing class name) is provided when writing.
 */
public final class ConstructorCreatorImpl extends AbstractCreator implements ConstructorCreator, Writable {

    /** The enclosing class name, set by the parent type creator. */
    private String className;

    /** The modifier holder. */
    private final ModifierHolder modifiers = new ModifierHolder(ModifierLocation.CONSTRUCTOR);

    /** The annotation holder. */
    private final AnnotationHolder annotations = new AnnotationHolder();

    /** Optional doc comment. */
    private DocCommentCreatorImpl docComment;

    /** The parameters. */
    private final List<ParamCreatorImpl> params = new ArrayList<>();

    /** The type parameters. */
    private final List<TypeParamCreatorImpl> typeParams = new ArrayList<>();

    /** The thrown exception types. */
    private final List<JType> throwsTypes = new ArrayList<>();

    /** The constructor body. */
    private BlockCreatorImpl body;

    /**
     * Constructs a new constructor creator.
     *
     * @param version the source version
     */
    public ConstructorCreatorImpl(final SourceVersion version) {
        super(version);
    }

    /**
     * Sets the enclosing class name for this constructor.
     *
     * @param className the class name
     */
    public void setClassName(final String className) {
        this.className = className;
    }

    /** {@inheritDoc} */
    @Override
    public ModifierLocation modifierLocation() {
        return ModifierLocation.CONSTRUCTOR;
    }

    /** {@inheritDoc} */
    @Override
    public void param(final String name, final JType type) {
        checkActive();
        Assert.checkNotNullParam("name", name);
        Assert.checkNotEmptyParam("name", name);
        Assert.checkNotNullParam("type", type);
        registerUsedType(type);
        params.add(new ParamCreatorImpl(version(), name, type, false));
    }

    /** {@inheritDoc} */
    @Override
    public void param(final String name, final JType type, final Consumer<ParamCreator> builder) {
        checkActive();
        Assert.checkNotNullParam("name", name);
        Assert.checkNotEmptyParam("name", name);
        Assert.checkNotNullParam("type", type);
        Assert.checkNotNullParam("builder", builder);
        registerUsedType(type);
        final ParamCreatorImpl pc = new ParamCreatorImpl(version(), name, type, false);
        pc.sourceFile(sourceFile());
        nest(() -> builder.accept(pc));
        pc.finish();
        params.add(pc);
        if (pc.docComment() != null) {
            getOrCreateDocComment().addParamTag(name, pc.docComment());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void varargParam(final String name, final JType type, final Consumer<ParamCreator> builder) {
        checkActive();
        Assert.checkNotNullParam("name", name);
        Assert.checkNotEmptyParam("name", name);
        Assert.checkNotNullParam("type", type);
        Assert.checkNotNullParam("builder", builder);
        registerUsedType(type);
        final ParamCreatorImpl pc = new ParamCreatorImpl(version(), name, type, true);
        pc.sourceFile(sourceFile());
        nest(() -> builder.accept(pc));
        pc.finish();
        params.add(pc);
        if (pc.docComment() != null) {
            getOrCreateDocComment().addParamTag(name, pc.docComment());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void throws_(final JType exceptionType) {
        checkActive();
        Assert.checkNotNullParam("exceptionType", exceptionType);
        registerUsedType(exceptionType);
        throwsTypes.add(exceptionType);
    }

    /** {@inheritDoc} */
    @Override
    public void typeParam(final String name, final Consumer<TypeParamCreator> builder) {
        checkActive();
        Assert.checkNotNullParam("name", name);
        Assert.checkNotEmptyParam("name", name);
        Assert.checkNotNullParam("builder", builder);
        final TypeParamCreatorImpl tp = new TypeParamCreatorImpl(version(), name);
        tp.sourceFile(sourceFile());
        nest(() -> builder.accept(tp));
        tp.finish();
        typeParams.add(tp);
        if (tp.docComment() != null) {
            getOrCreateDocComment().addTypeParamTag(name, tp.docComment());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void body(final Consumer<BlockCreator> builder) {
        checkActive();
        Assert.checkNotNullParam("builder", builder);
        final BlockCreatorImpl bc = new BlockCreatorImpl(version());
        bc.sourceFile(sourceFile());
        nest(() -> builder.accept(bc));
        bc.finish();
        this.body = bc;
    }

    /** {@inheritDoc} */
    @Override
    public void setAccess(final AccessLevel access) {
        checkActive();
        Assert.checkNotNullParam("access", access);
        modifiers.setAccess(access);
    }

    /** {@inheritDoc} */
    @Override
    public void addFlag(final ModifierFlag flag) {
        checkActive();
        Assert.checkNotNullParam("flag", flag);
        modifiers.addFlag(flag);
    }

    /** {@inheritDoc} */
    @Override
    public void removeFlag(final ModifierFlag flag) {
        checkActive();
        Assert.checkNotNullParam("flag", flag);
        modifiers.removeFlag(flag);
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
     * If a creator already exists from a prior call (e.g., from a type
     * parameter or constructor parameter contributing a tag), it is reopened
     * for further configuration.
     *
     * @return the doc comment creator
     */
    private DocCommentCreatorImpl getOrCreateDocComment() {
        DocCommentCreatorImpl dc = this.docComment;
        if (dc == null) {
            dc = new DocCommentCreatorImpl(version(), sourceFile(), DocContext.CONSTRUCTOR);
            this.docComment = dc;
        } else {
            dc.reopen();
        }
        return dc;
    }

    /** {@inheritDoc} */
    @Override
    public void write(final SourceFileWriter writer) throws IOException {
        if (docComment != null) {
            docComment.write(writer);
        }
        annotations.writeWithNewlines(writer);
        modifiers.write(writer);
        if (!typeParams.isEmpty()) {
            writer.addWordSpace();
            writer.write(Tokens.$ANGLE.OPEN);
            AbstractJExpr.writeList(writer, typeParams, FormatPreferences.Space.AFTER_COMMA_TYPE_ARGUMENT);
            writer.write(Tokens.$ANGLE.CLOSE);
            writer.sp();
        }
        writer.writeClass(className);
        writer.write(Tokens.$PAREN.OPEN);
        AbstractJExpr.writeList(writer, params, FormatPreferences.Space.AFTER_COMMA,
            FormatPreferences.Wrapping.PARAMETER_LIST);
        writer.write(Tokens.$PAREN.CLOSE);
        if (!throwsTypes.isEmpty()) {
            writer.write(Tokens.$KW.THROWS);
            AbstractJExpr.writeList(writer, throwsTypes, FormatPreferences.Space.AFTER_COMMA,
                FormatPreferences.Wrapping.EXCEPTION_LIST);
        }
        writer.write(FormatPreferences.Space.BEFORE_BRACE_METHOD);
        if (body != null) {
            body.writeBlock(writer);
        } else {
            writer.write(Tokens.$BRACE.OPEN);
            writer.write(Tokens.$BRACE.CLOSE);
        }
    }
}
