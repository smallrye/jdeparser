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
        if (docComment != null) {
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
        annotations.writeWithNewlines(writer);
        modifiers.write(writer);
        if (!typeParams.isEmpty()) {
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
        writer.writeClass(className);
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
        if (!throwsTypes.isEmpty()) {
            writer.write(Tokens.$KW.THROWS);
            first = true;
            for (JType t : throwsTypes) {
                if (!first) {
                    writer.write(Tokens.$PUNCT.COMMA);
                    writer.write(FormatPreferences.Space.AFTER_COMMA);
                }
                first = false;
                AbstractJExpr.writeType(writer, t);
            }
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
