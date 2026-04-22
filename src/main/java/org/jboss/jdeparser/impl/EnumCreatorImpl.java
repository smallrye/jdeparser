package org.jboss.jdeparser.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.jboss.jdeparser.JType;
import org.jboss.jdeparser.SourceVersion;
import org.jboss.jdeparser.creator.AccessLevel;
import org.jboss.jdeparser.creator.AnnotationCreator;
import org.jboss.jdeparser.creator.ConstructorCreator;
import org.jboss.jdeparser.creator.DocCommentCreator;
import org.jboss.jdeparser.creator.EnumConstantCreator;
import org.jboss.jdeparser.creator.EnumCreator;
import org.jboss.jdeparser.creator.FieldCreator;
import org.jboss.jdeparser.creator.MethodCreator;
import org.jboss.jdeparser.creator.ModifierFlag;
import org.jboss.jdeparser.creator.ModifierLocation;
import org.jboss.jdeparser.format.FormatPreferences;

/**
 * Implementation of {@link EnumCreator} that collects enum configuration
 * and writes the complete enum declaration.
 * <p>
 * Writes the form: {@code [javadoc] [annotations] [modifiers] enum Name
 * [implements I1, I2] &#123; CONST1, CONST2; members &#125;}.
 */
public final class EnumCreatorImpl extends AbstractCreator implements EnumCreator, Writable {

    /** The enum name. */
    private final String name;

    /** The modifier holder. */
    private final ModifierHolder modifiers;

    /** The annotation holder. */
    private final AnnotationHolder annotations = new AnnotationHolder();

    /** Optional doc comment. */
    private DocCommentCreatorImpl docComment;

    /** Implemented interfaces. */
    private final List<JType> interfaces = new ArrayList<>();

    /** Enum constants in declaration order. */
    private final List<EnumConstantCreatorImpl> constants = new ArrayList<>();

    /** Enum body members (fields, methods, constructors) in declaration order. */
    private final List<Writable> members = new ArrayList<>();

    /**
     * Constructs a new enum creator.
     *
     * @param version the source version
     * @param name    the enum name
     */
    public EnumCreatorImpl(final SourceVersion version, final String name) {
        super(version);
        this.name = name;
        this.modifiers = new ModifierHolder(ModifierLocation.ENUM);
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
    public void constant(final String name, final Consumer<EnumConstantCreator> builder) {
        checkActive();
        final EnumConstantCreatorImpl ec = new EnumConstantCreatorImpl(version(), name);
        nest(() -> builder.accept(ec));
        ec.finish();
        constants.add(ec);
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
    public void method(final String name, final Consumer<MethodCreator> builder) {
        checkActive();
        final MethodCreatorImpl mc = new MethodCreatorImpl(version(), name, ModifierLocation.METHOD);
        nest(() -> builder.accept(mc));
        mc.finish();
        members.add(mc);
    }

    /** {@inheritDoc} */
    @Override
    public void constructor(final Consumer<ConstructorCreator> builder) {
        checkActive();
        final ConstructorCreatorImpl cc = new ConstructorCreatorImpl(version());
        cc.setClassName(name);
        nest(() -> builder.accept(cc));
        cc.finish();
        members.add(cc);
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
        // Write the complete enum declaration: javadoc, annotations, modifiers,
        // enum keyword, name, implements clause, and body with constants and members.
        if (docComment != null) {
            docComment.write(writer);
        }
        annotations.writeWithNewlines(writer);
        modifiers.write(writer);
        writer.write(Tokens.$KW.ENUM);
        writer.writeClass(name);
        if (!interfaces.isEmpty()) {
            writer.write(Tokens.$KW.IMPLEMENTS);
            writeTypeList(writer, interfaces);
        }
        writer.write(FormatPreferences.Space.BEFORE_BRACE_ENUM);
        writer.write(Tokens.$BRACE.OPEN);
        writer.nl();
        writer.pushIndent(FormatPreferences.Indentation.MEMBERS_TOP_LEVEL);
        // write constants separated by commas
        boolean firstConstant = true;
        for (EnumConstantCreatorImpl constant : constants) {
            if (!firstConstant) {
                writer.write(Tokens.$PUNCT.COMMA);
                writer.write(FormatPreferences.Space.COMMA_ENUM_CONSTANT);
            }
            firstConstant = false;
            constant.write(writer);
        }
        // semicolon after last constant
        if (!constants.isEmpty()) {
            writer.write(Tokens.$PUNCT.SEMI);
            writer.nl();
        }
        // members (fields, methods, constructors) after a blank line
        if (!members.isEmpty()) {
            writer.nl();
            boolean firstMember = true;
            for (Writable member : members) {
                if (!firstMember) {
                    writer.nl();
                }
                firstMember = false;
                member.write(writer);
                writer.nl();
            }
        }
        writer.popIndent(FormatPreferences.Indentation.MEMBERS_TOP_LEVEL);
        writer.write(Tokens.$BRACE.CLOSE);
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
