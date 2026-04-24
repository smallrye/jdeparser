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
        Assert.checkNotNullParam("interfaceType", interfaceType);
        registerUsedType(interfaceType);
        interfaces.add(interfaceType);
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
    public void component(final String name, final JType type) {
        checkActive();
        Assert.checkNotNullParam("name", name);
        Assert.checkNotEmptyParam("name", name);
        Assert.checkNotNullParam("type", type);
        registerUsedType(type);
        components.add(new RecordComponentCreatorImpl(version(), name, type));
    }

    /** {@inheritDoc} */
    @Override
    public void component(final String name, final JType type, final Consumer<RecordComponentCreator> builder) {
        checkActive();
        Assert.checkNotNullParam("name", name);
        Assert.checkNotEmptyParam("name", name);
        Assert.checkNotNullParam("type", type);
        Assert.checkNotNullParam("builder", builder);
        registerUsedType(type);
        final RecordComponentCreatorImpl rc = new RecordComponentCreatorImpl(version(), name, type);
        rc.sourceFile(sourceFile());
        nest(() -> builder.accept(rc));
        rc.finish();
        components.add(rc);
        if (rc.docComment() != null) {
            getOrCreateDocComment().addParamTag(name, rc.docComment());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void compactConstructor(final Consumer<BlockCreator> builder) {
        checkActive();
        Assert.checkNotNullParam("builder", builder);
        final BlockCreatorImpl bc = new BlockCreatorImpl(version());
        bc.sourceFile(sourceFile());
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
        Assert.checkNotNullParam("name", name);
        Assert.checkNotEmptyParam("name", name);
        Assert.checkNotNullParam("builder", builder);
        final MethodCreatorImpl mc = new MethodCreatorImpl(version(), name, ModifierLocation.METHOD);
        mc.sourceFile(sourceFile());
        nest(() -> builder.accept(mc));
        mc.finish();
        members.add(mc);
    }

    /** {@inheritDoc} */
    @Override
    public void field(final String name, final Consumer<FieldCreator> builder) {
        checkActive();
        Assert.checkNotNullParam("name", name);
        Assert.checkNotEmptyParam("name", name);
        Assert.checkNotNullParam("builder", builder);
        final FieldCreatorImpl fc = new FieldCreatorImpl(version(), name, ModifierLocation.FIELD);
        fc.sourceFile(sourceFile());
        nest(() -> builder.accept(fc));
        fc.finish();
        members.add(fc);
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
     * parameter or component contributing a tag), it is reopened for
     * further configuration.
     *
     * @return the doc comment creator
     */
    private DocCommentCreatorImpl getOrCreateDocComment() {
        DocCommentCreatorImpl dc = this.docComment;
        if (dc == null) {
            dc = new DocCommentCreatorImpl(version(), sourceFile(), DocContext.TYPE);
            this.docComment = dc;
        } else {
            dc.reopen();
        }
        return dc;
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
            writeTypeList(writer, interfaces, FormatPreferences.Wrapping.IMPLEMENTS_LIST);
        }
        writer.write(FormatPreferences.Space.BEFORE_BRACE_RECORD);
        writer.write(Tokens.$BRACE.OPEN);
        if (members.isEmpty()) {
            writer.write(FormatPreferences.Space.WITHIN_BRACES_EMPTY);
        } else {
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
        }
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
        AbstractJExpr.writeList(writer, typeParams, FormatPreferences.Space.AFTER_COMMA_TYPE_ARGUMENT);
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
        AbstractJExpr.writeList(writer, components, FormatPreferences.Space.AFTER_COMMA_RECORD_COMPONENT,
            FormatPreferences.Wrapping.RECORD_COMPONENT_LIST);
        writer.write(Tokens.$PAREN.CLOSE);
    }

    /**
     * Writes a comma-separated list of types with wrapping support.
     *
     * @param writer   the writer
     * @param types    the types
     * @param wrapping the wrapping context
     * @throws IOException if an I/O error occurs
     */
    private static void writeTypeList(final SourceFileWriter writer, final List<JType> types,
                                      final FormatPreferences.Wrapping wrapping) throws IOException {
        AbstractJExpr.writeList(writer, types, FormatPreferences.Space.AFTER_COMMA, wrapping);
    }
}
