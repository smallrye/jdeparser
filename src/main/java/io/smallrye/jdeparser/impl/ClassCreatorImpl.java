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
import io.smallrye.jdeparser.creator.AnnotationInterfaceCreator;
import io.smallrye.jdeparser.creator.BlockCreator;
import io.smallrye.jdeparser.creator.ClassCreator;
import io.smallrye.jdeparser.creator.ConstructorCreator;
import io.smallrye.jdeparser.creator.DocCommentCreator;
import io.smallrye.jdeparser.creator.EnumCreator;
import io.smallrye.jdeparser.creator.FieldCreator;
import io.smallrye.jdeparser.creator.InterfaceCreator;
import io.smallrye.jdeparser.creator.MethodCreator;
import io.smallrye.jdeparser.creator.ModifierFlag;
import io.smallrye.jdeparser.creator.ModifierLocation;
import io.smallrye.jdeparser.creator.RecordCreator;
import io.smallrye.jdeparser.creator.TypeParamCreator;
import io.smallrye.jdeparser.format.FormatPreferences;

/**
 * Implementation of {@link ClassCreator} that collects class configuration
 * and writes the complete class declaration.
 * <p>
 * Writes the form: {@code [javadoc] [annotations] [modifiers] class Name
 * [<TypeParams>] [extends Super] [implements I1, I2] [permits P1, P2]
 * &#123; members &#125;}.
 */
public final class ClassCreatorImpl extends AbstractCreator implements ClassCreator, Writable {

    /** The class name. */
    private final String name;

    /** The modifier holder. */
    private final ModifierHolder modifiers;

    /** The annotation holder. */
    private final AnnotationHolder annotations = new AnnotationHolder();

    /** Optional doc comment. */
    private DocCommentCreatorImpl docComment;

    /** Type parameters. */
    private final List<TypeParamCreatorImpl> typeParams = new ArrayList<>();

    /** The superclass, or {@code null}. */
    private Type superType;

    /** Implemented interfaces. */
    private final List<Type> interfaces = new ArrayList<>();

    /** Permitted subtypes (sealed classes). */
    private final List<Type> permits = new ArrayList<>();

    /** Class body members in declaration order. */
    private final List<Writable> members = new ArrayList<>();

    /**
     * Constructs a new class creator.
     *
     * @param version the source version
     * @param name the class name
     * @param topLevel whether this is a top-level class
     */
    public ClassCreatorImpl(final SourceVersion version, final String name, final boolean topLevel) {
        super(version);
        this.name = name;
        this.modifiers = new ModifierHolder(topLevel ? ModifierLocation.CLASS : ModifierLocation.CLASS);
    }

    /** {@inheritDoc} */
    @Override
    public ModifierLocation modifierLocation() {
        return modifiers.location();
    }

    /** {@inheritDoc} */
    @Override
    public void extends_(final Type superType) {
        checkActive();
        Assert.checkNotNullParam("superType", superType);
        registerUsedType(superType);
        this.superType = superType;
    }

    /** {@inheritDoc} */
    @Override
    public void implements_(final Type interfaceType) {
        checkActive();
        Assert.checkNotNullParam("interfaceType", interfaceType);
        registerUsedType(interfaceType);
        interfaces.add(interfaceType);
    }

    /** {@inheritDoc} */
    @Override
    public void permits(final Type permittedType) {
        checkActive();
        Assert.checkNotNullParam("permittedType", permittedType);
        registerUsedType(permittedType);
        permits.add(permittedType);
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
    public Var field(final String name, final Consumer<FieldCreator> builder) {
        checkActive();
        Assert.checkNotNullParam("name", name);
        Assert.checkNotEmptyParam("name", name);
        Assert.checkNotNullParam("builder", builder);
        final FieldCreatorImpl fc = new FieldCreatorImpl(version(), name, ModifierLocation.FIELD);
        fc.sourceFile(sourceFile());
        nest(() -> builder.accept(fc));
        fc.finish();
        members.add(fc);
        return new NamedVar(name);
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
    public void constructor(final Consumer<ConstructorCreator> builder) {
        checkActive();
        Assert.checkNotNullParam("builder", builder);
        final ConstructorCreatorImpl cc = new ConstructorCreatorImpl(version());
        cc.setClassName(name);
        cc.sourceFile(sourceFile());
        nest(() -> builder.accept(cc));
        cc.finish();
        members.add(cc);
    }

    /** {@inheritDoc} */
    @Override
    public void instanceInit(final Consumer<BlockCreator> builder) {
        checkActive();
        Assert.checkNotNullParam("builder", builder);
        final BlockCreatorImpl bc = new BlockCreatorImpl(version());
        bc.sourceFile(sourceFile());
        nest(() -> builder.accept(bc));
        bc.finish();
        members.add(new InitBlockMember(bc));
    }

    /** {@inheritDoc} */
    @Override
    public void staticInit(final Consumer<BlockCreator> builder) {
        checkActive();
        Assert.checkNotNullParam("builder", builder);
        final BlockCreatorImpl bc = new BlockCreatorImpl(version());
        bc.sourceFile(sourceFile());
        nest(() -> builder.accept(bc));
        bc.finish();
        members.add(w -> {
            w.write(Tokens.$KW.STATIC);
            w.addWordSpace();
            bc.writeBlock(w);
        });
    }

    /** {@inheritDoc} */
    @Override
    public void class_(final String name, final Consumer<ClassCreator> builder) {
        checkActive();
        Assert.checkNotNullParam("name", name);
        Assert.checkNotEmptyParam("name", name);
        Assert.checkNotNullParam("builder", builder);
        final ClassCreatorImpl cc = new ClassCreatorImpl(version(), name, false);
        cc.sourceFile(sourceFile());
        nest(() -> builder.accept(cc));
        cc.finish();
        members.add(cc);
    }

    /** {@inheritDoc} */
    @Override
    public void interface_(final String name, final Consumer<InterfaceCreator> builder) {
        checkActive();
        Assert.checkNotNullParam("name", name);
        Assert.checkNotEmptyParam("name", name);
        Assert.checkNotNullParam("builder", builder);
        final InterfaceCreatorImpl ic = new InterfaceCreatorImpl(version(), name);
        ic.sourceFile(sourceFile());
        nest(() -> builder.accept(ic));
        ic.finish();
        members.add(ic);
    }

    /** {@inheritDoc} */
    @Override
    public void enum_(final String name, final Consumer<EnumCreator> builder) {
        checkActive();
        Assert.checkNotNullParam("name", name);
        Assert.checkNotEmptyParam("name", name);
        Assert.checkNotNullParam("builder", builder);
        final EnumCreatorImpl ec = new EnumCreatorImpl(version(), name);
        ec.sourceFile(sourceFile());
        nest(() -> builder.accept(ec));
        ec.finish();
        members.add(ec);
    }

    /** {@inheritDoc} */
    @Override
    public void record_(final String name, final Consumer<RecordCreator> builder) {
        checkActive();
        Assert.checkNotNullParam("name", name);
        Assert.checkNotEmptyParam("name", name);
        Assert.checkNotNullParam("builder", builder);
        final RecordCreatorImpl rc = new RecordCreatorImpl(version(), name);
        rc.sourceFile(sourceFile());
        nest(() -> builder.accept(rc));
        rc.finish();
        members.add(rc);
    }

    /** {@inheritDoc} */
    @Override
    public void annotationInterface_(final String name, final Consumer<AnnotationInterfaceCreator> builder) {
        checkActive();
        Assert.checkNotNullParam("name", name);
        Assert.checkNotEmptyParam("name", name);
        Assert.checkNotNullParam("builder", builder);
        final AnnotationInterfaceCreatorImpl ac = new AnnotationInterfaceCreatorImpl(version(), name);
        ac.sourceFile(sourceFile());
        nest(() -> builder.accept(ac));
        ac.finish();
        members.add(ac);
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
     * parameter contributing a tag), it is reopened for further configuration.
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
    public FormatPreferences.Space memberSpacing() {
        return FormatPreferences.Space.BEFORE_CLASS;
    }

    /** {@inheritDoc} */
    @Override
    public void write(final SourceFileWriter writer) throws IOException {
        if (docComment != null) {
            docComment.write(writer);
        }
        annotations.writeWithNewlines(writer);
        modifiers.write(writer);
        writer.write(Tokens.$KW.CLASS);
        writer.writeClass(name);
        writeTypeParams(writer);
        if (superType != null) {
            writer.write(Tokens.$KW.EXTENDS);
            AbstractExpr.writeType(writer, superType);
        }
        if (!interfaces.isEmpty()) {
            writer.write(Tokens.$KW.IMPLEMENTS);
            writeTypeList(writer, interfaces, FormatPreferences.Wrapping.IMPLEMENTS_LIST);
        }
        if (!permits.isEmpty()) {
            writer.write(Tokens.$KW.PERMITS);
            writeTypeList(writer, permits, FormatPreferences.Wrapping.PERMITS_LIST);
        }
        writer.write(FormatPreferences.Space.BEFORE_BRACE_CLASS);
        writer.write(Tokens.$BRACE.OPEN);
        if (members.isEmpty()) {
            writer.write(FormatPreferences.Space.WITHIN_BRACES_EMPTY);
        } else {
            writer.nl();
            writer.pushIndent(FormatPreferences.Indentation.MEMBERS_TOP_LEVEL);
            writeBody(writer);
            writer.popIndent(FormatPreferences.Indentation.MEMBERS_TOP_LEVEL);
        }
        writer.write(Tokens.$BRACE.CLOSE);
    }

    /**
     * Returns whether this class has any members.
     *
     * @return {@code true} if the class has members, {@code false} if the body is empty
     */
    boolean hasMembers() {
        return !members.isEmpty();
    }

    /**
     * If the body consists of exactly one instance initializer, returns its
     * block for compact double-brace rendering. Otherwise returns {@code null}.
     *
     * @return the sole instance initializer block, or {@code null}
     */
    BlockCreatorImpl soleInitBlock() {
        if (members.size() == 1 && members.get(0) instanceof InitBlockMember ibm) {
            return ibm.block();
        }
        return null;
    }

    /**
     * A member writable that marks an instance initializer block, allowing
     * detection for compact double-brace formatting.
     *
     * @param block the instance initializer block
     */
    record InitBlockMember(BlockCreatorImpl block) implements Writable {
        /** {@inheritDoc} */
        @Override
        public void write(final SourceFileWriter writer) throws IOException {
            block.writeBlock(writer);
        }
    }

    /**
     * Writes only the body members of this class, without the surrounding
     * braces, class keyword, name, or any header elements.
     * <p>
     * This is used by {@link EnumConstantCreatorImpl} to write the anonymous
     * class body of an enum constant.
     *
     * @param writer the writer
     * @throws IOException if an I/O error occurs
     */
    void writeBody(final SourceFileWriter writer) throws IOException {
        boolean firstMember = true;
        for (Writable member : members) {
            if (!firstMember) {
                FormatPreferences.Space spacing = member.memberSpacing();
                if (spacing != null) {
                    writer.write(spacing);
                } else {
                    writer.nl();
                }
            }
            firstMember = false;
            member.write(writer);
            writer.nl();
        }
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
        AbstractExpr.writeList(writer, typeParams, FormatPreferences.Space.AFTER_COMMA_TYPE_ARGUMENT);
        writer.write(Tokens.$ANGLE.CLOSE);
    }

    /**
     * Writes a comma-separated list of types with wrapping support.
     *
     * @param writer the writer
     * @param types the types
     * @param wrapping the wrapping context
     * @throws IOException if an I/O error occurs
     */
    private static void writeTypeList(final SourceFileWriter writer, final List<Type> types,
            final FormatPreferences.Wrapping wrapping) throws IOException {
        AbstractExpr.writeList(writer, types, FormatPreferences.Space.AFTER_COMMA, wrapping);
    }
}
