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
import org.jboss.jdeparser.creator.FieldCreator;
import org.jboss.jdeparser.creator.MethodCreator;
import org.jboss.jdeparser.creator.ModifierFlag;
import org.jboss.jdeparser.creator.ModifierLocation;
import org.jboss.jdeparser.creator.RecordComponentCreator;
import org.jboss.jdeparser.creator.RecordCreator;
import org.jboss.jdeparser.creator.TypeParamCreator;
import org.jboss.jdeparser.format.FormatPreferences;

/**
 * Implementation of {@link RecordCreator} that collects record configuration
 * and writes the complete record declaration.
 * <p>
 * Writes the form: {@code [javadoc] [annotations] [modifiers] record Name
 * [<TypeParams>](comp1, comp2) [implements I1, I2] &#123; members &#125;}.
 */
public final class RecordCreatorImpl extends AbstractCreator implements RecordCreator, Writable {

    /** The record name. */
    private final String name;

    /** The modifier holder. */
    private final ModifierHolder modifiers;

    /** The annotation holder. */
    private final AnnotationHolder annotations = new AnnotationHolder();

    /** Optional doc comment. */
    private DocCommentCreatorImpl docComment;

    /** Type parameters. */
    private final List<TypeParamCreatorImpl> typeParams = new ArrayList<>();

    /** Record components in declaration order. */
    private final List<RecordComponentCreatorImpl> components = new ArrayList<>();

    /** Implemented interfaces. */
    private final List<JType> interfaces = new ArrayList<>();

    /** Record body members in declaration order. */
    private final List<Writable> members = new ArrayList<>();

    /**
     * Constructs a new record creator.
     *
     * @param version the source version
     * @param name    the record name
     */
    public RecordCreatorImpl(final SourceVersion version, final String name) {
        super(version);
        this.name = name;
        this.modifiers = new ModifierHolder(ModifierLocation.RECORD);
    }

    /** {@inheritDoc} */
    @Override
    public ModifierLocation modifierLocation() {
        return modifiers.location();
    }

    /** {@inheritDoc} */
    @Override
    public void implements_(final JType interfaceType) {
        checkActive();
        interfaces.add(interfaceType);
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
    public void component(final String name, final JType type) {
        checkActive();
        components.add(new RecordComponentCreatorImpl(version(), name, type));
    }

    /** {@inheritDoc} */
    @Override
    public void component(final String name, final JType type, final Consumer<RecordComponentCreator> builder) {
        checkActive();
        final RecordComponentCreatorImpl rc = new RecordComponentCreatorImpl(version(), name, type);
        nest(() -> builder.accept(rc));
        rc.finish();
        components.add(rc);
    }

    /** {@inheritDoc} */
    @Override
    public void compactConstructor(final Consumer<BlockCreator> builder) {
        checkActive();
        final BlockCreatorImpl bc = new BlockCreatorImpl(version());
        nest(() -> builder.accept(bc));
        bc.finish();
        // compact constructor: ClassName { body } (no parameter list)
        members.add(w -> {
            w.writeClass(name);
            w.write(FormatPreferences.Space.BEFORE_BRACE_METHOD);
            bc.writeBlock(w);
        });
    }

    /** {@inheritDoc} */
    @Override
    public void method(final String name, final Consumer<MethodCreator> builder) {
        checkActive();
        final MethodCreatorImpl mc = new MethodCreatorImpl(version(), name, ModifierLocation.METHOD);
        nest(() -> builder.accept(mc));
        mc.finish();
        members.add(mc);
    }

    /** {@inheritDoc} */
    @Override
    public void field(final String name, final Consumer<FieldCreator> builder) {
        checkActive();
        final FieldCreatorImpl fc = new FieldCreatorImpl(version(), name, ModifierLocation.FIELD);
        nest(() -> builder.accept(fc));
        fc.finish();
        members.add(fc);
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
        // [javadoc] [annotations] [modifiers] record Name [<TypeParams>](comp1, comp2) [implements I1] { members }
        if (docComment != null) {
            docComment.write(writer);
        }
        annotations.writeWithNewlines(writer);
        modifiers.write(writer);
        writer.write(Tokens.$KW.RECORD);
        writer.writeClass(name);
        writeTypeParams(writer);
        writeComponents(writer);
        if (!interfaces.isEmpty()) {
            writer.write(Tokens.$KW.IMPLEMENTS);
            writeTypeList(writer, interfaces);
        }
        writer.write(FormatPreferences.Space.BEFORE_BRACE_RECORD);
        writer.write(Tokens.$BRACE.OPEN);
        writer.nl();
        writer.pushIndent(FormatPreferences.Indentation.MEMBERS_TOP_LEVEL);
        boolean firstMember = true;
        for (Writable member : members) {
            if (!firstMember) {
                writer.nl();
            }
            firstMember = false;
            member.write(writer);
            writer.nl();
        }
        writer.popIndent(FormatPreferences.Indentation.MEMBERS_TOP_LEVEL);
        writer.write(Tokens.$BRACE.CLOSE);
    }

    /**
     * Writes the type parameter list: {@code <T, U>}.
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
    }

    /**
     * Writes the record component list: {@code (comp1, comp2)}.
     *
     * @param writer the writer
     * @throws IOException if an I/O error occurs
     */
    private void writeComponents(final SourceFileWriter writer) throws IOException {
        writer.write(FormatPreferences.Space.BEFORE_PAREN_RECORD);
        writer.write(Tokens.$PAREN.OPEN);
        boolean first = true;
        for (RecordComponentCreatorImpl comp : components) {
            if (!first) {
                writer.write(Tokens.$PUNCT.COMMA);
                writer.write(FormatPreferences.Space.COMMA_RECORD_COMPONENT);
            }
            first = false;
            comp.write(writer);
        }
        writer.write(Tokens.$PAREN.CLOSE);
    }

    /**
     * Writes a comma-separated list of types.
     *
     * @param writer the writer
     * @param types  the types
     * @throws IOException if an I/O error occurs
     */
    private static void writeTypeList(final SourceFileWriter writer, final List<JType> types) throws IOException {
        boolean first = true;
        for (JType t : types) {
            if (!first) {
                writer.write(Tokens.$PUNCT.COMMA);
                writer.write(FormatPreferences.Space.AFTER_COMMA);
            }
            first = false;
            AbstractJExpr.writeType(writer, t);
        }
    }
}
