package org.jboss.jdeparser.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.jboss.jdeparser.JType;
import org.jboss.jdeparser.SourceVersion;
import org.jboss.jdeparser.creator.AccessLevel;
import org.jboss.jdeparser.creator.AnnotationCreator;
import org.jboss.jdeparser.creator.BlockCreator;
import org.jboss.jdeparser.creator.DocCommentCreator;
import org.jboss.jdeparser.creator.MethodCreator;
import org.jboss.jdeparser.creator.ModifierFlag;
import org.jboss.jdeparser.creator.ModifierLocation;
import org.jboss.jdeparser.creator.ParamCreator;
import org.jboss.jdeparser.creator.TypeParamCreator;
import org.jboss.jdeparser.format.FormatPreferences;

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
    private JType returnType;

    /** The parameters. */
    private final List<ParamCreatorImpl> params = new ArrayList<>();

    /** The type parameters. */
    private final List<TypeParamCreatorImpl> typeParams = new ArrayList<>();

    /** The thrown exception types. */
    private final List<JType> throwsTypes = new ArrayList<>();

    /** The method body, or {@code null} for abstract/native methods. */
    private BlockCreatorImpl body;

    /**
     * Constructs a new method creator.
     *
     * @param version  the source version
     * @param name     the method name
     * @param location the modifier location (e.g., {@link ModifierLocation#METHOD} or
     *                 {@link ModifierLocation#INTERFACE_METHOD})
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
    public void returning(final JType type) {
        checkActive();
        this.returnType = type;
    }

    /** {@inheritDoc} */
    @Override
    public void param(final String name, final JType type) {
        checkActive();
        params.add(new ParamCreatorImpl(version(), name, type, false));
    }

    /** {@inheritDoc} */
    @Override
    public void param(final String name, final JType type, final Consumer<ParamCreator> builder) {
        checkActive();
        final ParamCreatorImpl pc = new ParamCreatorImpl(version(), name, type, false);
        nest(() -> builder.accept(pc));
        pc.finish();
        params.add(pc);
    }

    /** {@inheritDoc} */
    @Override
    public void varargParam(final String name, final JType type, final Consumer<ParamCreator> builder) {
        checkActive();
        final ParamCreatorImpl pc = new ParamCreatorImpl(version(), name, type, true);
        nest(() -> builder.accept(pc));
        pc.finish();
        params.add(pc);
    }

    /** {@inheritDoc} */
    @Override
    public void throws_(final JType exceptionType) {
        checkActive();
        throwsTypes.add(exceptionType);
    }

    /** {@inheritDoc} */
    @Override
    public void typeParam(final String name, final Consumer<TypeParamCreator> builder) {
        checkActive();
        final TypeParamCreatorImpl tp = new TypeParamCreatorImpl(version(), name);
        nest(() -> builder.accept(tp));
        tp.finish();
        typeParams.add(tp);
    }

    /** {@inheritDoc} */
    @Override
    public void body(final Consumer<BlockCreator> builder) {
        checkActive();
        if (modifiers.hasFlag(ModifierFlag.ABSTRACT)) {
            throw new IllegalStateException("Abstract methods cannot have a body");
        }
        if (modifiers.hasFlag(ModifierFlag.NATIVE)) {
            throw new IllegalStateException("Native methods cannot have a body");
        }
        final BlockCreatorImpl bc = new BlockCreatorImpl(version());
        nest(() -> builder.accept(bc));
        bc.finish();
        this.body = bc;
    }

    /** {@inheritDoc} */
    @Override
    public void setAccess(final AccessLevel access) {
        checkActive();
        modifiers.setAccess(access);
    }

    /** {@inheritDoc} */
    @Override
    public void addFlag(final ModifierFlag flag) {
        checkActive();
        modifiers.addFlag(flag);
    }

    /** {@inheritDoc} */
    @Override
    public void removeFlag(final ModifierFlag flag) {
        checkActive();
        modifiers.removeFlag(flag);
    }

    /** {@inheritDoc} */
    @Override
    public void annotate(final JType annotationType, final Consumer<AnnotationCreator> builder) {
        checkActive();
        final AnnotationCreatorImpl ac = new AnnotationCreatorImpl(version(), annotationType);
        nest(() -> builder.accept(ac));
        ac.finish();
        annotations.add(ac);
    }

    /** {@inheritDoc} */
    @Override
    public void annotate(final JType annotationType) {
        checkActive();
        annotations.add(new AnnotationCreatorImpl(version(), annotationType));
    }

    /** {@inheritDoc} */
    @Override
    public void docComment(final Consumer<DocCommentCreator> builder) {
        checkActive();
        final DocCommentCreatorImpl dc = new DocCommentCreatorImpl(version());
        nest(() -> builder.accept(dc));
        dc.finish();
        this.docComment = dc;
    }

    /** {@inheritDoc} */
    @Override
    public void write(final SourceFileWriter writer) throws IOException {
        // aggregate @param tags from sub-creators into the doc comment
        writeDocComment(writer);
        annotations.writeWithNewlines(writer);
        modifiers.write(writer);
        writeTypeParams(writer);
        if (returnType != null) {
            AbstractJExpr.writeType(writer, returnType);
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
     * Writes the Javadoc, aggregating {@code @param} tags from parameters
     * and type parameters.
     *
     * @param writer the writer
     * @throws IOException if an I/O error occurs
     */
    private void writeDocComment(final SourceFileWriter writer) throws IOException {
        if (docComment == null) {
            return;
        }
        for (TypeParamCreatorImpl tp : typeParams) {
            if (tp.docComment() != null) {
                docComment.addTypeParamTag(tp.name(), "");
            }
        }
        for (ParamCreatorImpl p : params) {
            if (p.docComment() != null) {
                docComment.addParamTag(p.name(), "");
            }
        }
        docComment.write(writer);
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
        writer.write(Tokens.$ANGLE.OPEN);
        boolean first = true;
        for (TypeParamCreatorImpl tp : typeParams) {
            if (!first) {
                writer.write(Tokens.$PUNCT.COMMA);
                writer.write(FormatPreferences.Space.COMMA_TYPE_ARGUMENT);
            }
            first = false;
            tp.write(writer);
        }
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
        writer.write(Tokens.$PAREN.OPEN);
        boolean first = true;
        for (ParamCreatorImpl p : params) {
            if (!first) {
                writer.write(Tokens.$PUNCT.COMMA);
                writer.write(FormatPreferences.Space.AFTER_COMMA);
            }
            first = false;
            p.write(writer);
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
        boolean first = true;
        for (JType t : throwsTypes) {
            if (!first) {
                writer.write(Tokens.$PUNCT.COMMA);
                writer.write(FormatPreferences.Space.AFTER_COMMA);
            }
            first = false;
            AbstractJExpr.writeType(writer, t);
        }
    }
}
