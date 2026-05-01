package io.smallrye.jdeparser.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import io.smallrye.common.constraint.Assert;
import io.smallrye.jdeparser.SourceVersion;
import io.smallrye.jdeparser.Type;
import io.smallrye.jdeparser.Var;
import io.smallrye.jdeparser.creator.AccessLevel;
import io.smallrye.jdeparser.creator.AnnotationCreator;
import io.smallrye.jdeparser.creator.BlockCreator;
import io.smallrye.jdeparser.creator.DocCommentCreator;
import io.smallrye.jdeparser.creator.MethodCreator;
import io.smallrye.jdeparser.creator.ModifierFlag;
import io.smallrye.jdeparser.creator.ModifierLocation;
import io.smallrye.jdeparser.creator.ParamCreator;
import io.smallrye.jdeparser.creator.TypeParamCreator;
import io.smallrye.jdeparser.format.FormatPreferences;

/**
 * Implementation of {@link MethodCreator} that collects method configuration
 * and writes the complete method declaration.
 * <p>
 * Writes the form: {@code [javadoc] [annotations] [modifiers] [<typeParams>]
 * ReturnType name([params]) [throws Ex1, Ex2] { body }}.
 */
public final class MethodCreatorImpl extends AbstractCreator implements MethodCreator, Writable {

    /** The method name. */
    private final String name;

    /** The modifier location for validation. */
    private final ModifierLocation location;

    /** The modifier holder. */
    private final ModifierHolder modifiers;

    /** The annotation holder. */
    private final AnnotationHolder annotations = new AnnotationHolder();

    /** Optional doc comment. */
    private DocCommentCreatorImpl docComment;

    /** The return type (defaults to void). */
    private Type returnType;

    /** The parameters. */
    private final List<ParamCreatorImpl> params = new ArrayList<>();

    /** The type parameters. */
    private final List<TypeParamCreatorImpl> typeParams = new ArrayList<>();

    /** The thrown exception types. */
    private final List<Type> throwsTypes = new ArrayList<>();

    /** The method body, or {@code null} for abstract/native methods. */
    private BlockCreatorImpl body;

    /**
     * Constructs a new method creator.
     *
     * @param version the source version
     * @param name the method name
     * @param location the modifier location (e.g., {@link ModifierLocation#METHOD} or
     *        {@link ModifierLocation#INTERFACE_METHOD})
     */
    public MethodCreatorImpl(final SourceVersion version, final String name, final ModifierLocation location) {
        super(version);
        this.name = name;
        this.location = location;
        this.modifiers = new ModifierHolder(location);
    }

    /** {@inheritDoc} */
    @Override
    public ModifierLocation modifierLocation() {
        return location;
    }

    /** {@inheritDoc} */
    @Override
    public void returning(final Type type) {
        checkActive();
        Assert.checkNotNullParam("type", type);
        registerUsedType(type);
        this.returnType = type;
    }

    /** {@inheritDoc} */
    @Override
    public Var param(final String name, final Type type) {
        checkActive();
        Assert.checkNotNullParam("name", name);
        Assert.checkNotEmptyParam("name", name);
        Assert.checkNotNullParam("type", type);
        registerUsedType(type);
        params.add(new ParamCreatorImpl(version(), name, type, false));
        return new NamedVar(name);
    }

    /** {@inheritDoc} */
    @Override
    public Var param(final String name, final Type type, final Consumer<ParamCreator> builder) {
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
        return new NamedVar(name);
    }

    /** {@inheritDoc} */
    @Override
    public Var varargParam(final String name, final Type type, final Consumer<ParamCreator> builder) {
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
        return new NamedVar(name);
    }

    /** {@inheritDoc} */
    @Override
    public void throws_(final Type exceptionType) {
        checkActive();
        Assert.checkNotNullParam("exceptionType", exceptionType);
        registerUsedType(exceptionType);
        throwsTypes.add(exceptionType);
    }

    /** {@inheritDoc} */
    @Override
    public Type typeParam(final String name, final Consumer<TypeParamCreator> builder) {
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
        return new ReferenceType(name);
    }

    /** {@inheritDoc} */
    @Override
    public void body(final Consumer<BlockCreator> builder) {
        checkActive();
        Assert.checkNotNullParam("builder", builder);
        if (modifiers.hasFlag(ModifierFlag.ABSTRACT)) {
            throw new IllegalStateException("Abstract methods cannot have a body");
        }
        if (modifiers.hasFlag(ModifierFlag.NATIVE)) {
            throw new IllegalStateException("Native methods cannot have a body");
        }
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
    public void annotate(final Type annotationType, final Consumer<AnnotationCreator> builder) {
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
    public void annotate(final Type annotationType) {
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
     * parameter or method parameter contributing a tag), it is reopened
     * for further configuration.
     *
     * @return the doc comment creator
     */
    private DocCommentCreatorImpl getOrCreateDocComment() {
        DocCommentCreatorImpl dc = this.docComment;
        if (dc == null) {
            dc = new DocCommentCreatorImpl(version(), sourceFile(), DocContext.METHOD);
            this.docComment = dc;
        } else {
            dc.reopen();
        }
        return dc;
    }

    /** {@inheritDoc} */
    @Override
    public FormatPreferences.Space memberSpacing() {
        return FormatPreferences.Space.BEFORE_METHOD;
    }

    /** {@inheritDoc} */
    @Override
    public void write(final SourceFileWriter writer) throws IOException {
        if (docComment != null) {
            docComment.write(writer);
        }
        annotations.writeWithNewlines(writer);
        modifiers.write(writer);
        writeTypeParams(writer);
        if (returnType != null) {
            AbstractExpr.writeType(writer, returnType);
        } else {
            writer.write(Tokens.$KW.VOID);
        }
        writer.sp();
        writer.writeName(name);
        writeParams(writer);
        writeThrows(writer);
        if (body != null) {
            writer.write(FormatPreferences.Space.BEFORE_BRACE_METHOD);
            body.writeBlock(writer);
        } else {
            writer.write(Tokens.$PUNCT.SEMI);
        }
    }

    /**
     * Writes the type parameter list: {@code <T, U extends Bound>}.
     *
     * @param writer the writer
     * @throws IOException if an I/O error occurs
     */
    private void writeTypeParams(final SourceFileWriter writer) throws IOException {
        if (typeParams.isEmpty()) {
            return;
        }
        writer.addWordSpace();
        writer.write(Tokens.$ANGLE.OPEN);
        AbstractExpr.writeList(writer, typeParams, FormatPreferences.Space.AFTER_COMMA_TYPE_ARGUMENT);
        writer.write(Tokens.$ANGLE.CLOSE);
        writer.sp();
    }

    /**
     * Writes the parameter list: {@code (param1, param2)}.
     *
     * @param writer the writer
     * @throws IOException if an I/O error occurs
     */
    private void writeParams(final SourceFileWriter writer) throws IOException {
        writer.write(FormatPreferences.Space.BEFORE_PAREN_METHOD_DECLARATION);
        writer.write(Tokens.$PAREN.OPEN);
        if (params.isEmpty()) {
            writer.write(FormatPreferences.Space.WITHIN_PAREN_METHOD_DECLARATION_EMPTY);
        } else {
            writer.write(FormatPreferences.Space.WITHIN_PAREN_METHOD_DECLARATION);
            AbstractExpr.writeList(writer, params, FormatPreferences.Space.AFTER_COMMA,
                    FormatPreferences.Wrapping.PARAMETER_LIST);
            writer.write(FormatPreferences.Space.WITHIN_PAREN_METHOD_DECLARATION);
        }
        writer.write(Tokens.$PAREN.CLOSE);
    }

    /**
     * Writes the throws clause: {@code throws Ex1, Ex2}.
     *
     * @param writer the writer
     * @throws IOException if an I/O error occurs
     */
    private void writeThrows(final SourceFileWriter writer) throws IOException {
        if (throwsTypes.isEmpty()) {
            return;
        }
        writer.write(Tokens.$KW.THROWS);
        AbstractExpr.writeList(writer, throwsTypes, FormatPreferences.Space.AFTER_COMMA,
                FormatPreferences.Wrapping.EXCEPTION_LIST);
    }
}
