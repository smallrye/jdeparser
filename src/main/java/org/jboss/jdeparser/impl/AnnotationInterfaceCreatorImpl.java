package org.jboss.jdeparser.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.jboss.jdeparser.JExpr;
import org.jboss.jdeparser.JType;
import org.jboss.jdeparser.SourceVersion;
import org.jboss.jdeparser.creator.AccessLevel;
import org.jboss.jdeparser.creator.AnnotationCreator;
import org.jboss.jdeparser.creator.AnnotationInterfaceCreator;
import org.jboss.jdeparser.creator.DocCommentCreator;
import org.jboss.jdeparser.creator.FieldCreator;
import org.jboss.jdeparser.creator.ModifierFlag;
import org.jboss.jdeparser.creator.ModifierLocation;
import org.jboss.jdeparser.format.FormatPreferences;

/**
 * Implementation of {@link AnnotationInterfaceCreator} that collects annotation
 * type configuration and writes the complete annotation type declaration.
 * <p>
 * Writes the form: {@code [javadoc] [annotations] [modifiers] @interface Name
 * &#123; elements &#125;}.
 * <p>
 * Annotation elements are written as: {@code Type name() [default value];}.
 * Constant fields are written using {@link FieldCreatorImpl}.
 */
public final class AnnotationInterfaceCreatorImpl extends AbstractCreator implements AnnotationInterfaceCreator, Writable {

    /** The annotation type name. */
    private final String name;

    /** The modifier holder. */
    private final ModifierHolder modifiers;

    /** The annotation holder. */
    private final AnnotationHolder annotations = new AnnotationHolder();

    /** Optional doc comment. */
    private DocCommentCreatorImpl docComment;

    /** Annotation body members (elements and constants) in declaration order. */
    private final List<Writable> members = new ArrayList<>();

    /**
     * Constructs a new annotation interface creator.
     *
     * @param version the source version
     * @param name    the annotation type name
     */
    public AnnotationInterfaceCreatorImpl(final SourceVersion version, final String name) {
        super(version);
        this.name = name;
        this.modifiers = new ModifierHolder(ModifierLocation.ANNOTATION);
    }

    /** {@inheritDoc} */
    @Override
    public ModifierLocation modifierLocation() {
        return modifiers.location();
    }

    /** {@inheritDoc} */
    @Override
    public void element(final String name, final JType type, final JExpr defaultValue) {
        checkActive();
        members.add(new AnnotationElement(name, type, defaultValue));
    }

    /** {@inheritDoc} */
    @Override
    public void element(final String name, final JType type) {
        checkActive();
        members.add(new AnnotationElement(name, type, null));
    }

    /** {@inheritDoc} */
    @Override
    public void constant(final String name, final Consumer<FieldCreator> builder) {
        checkActive();
        final FieldCreatorImpl fc = new FieldCreatorImpl(version(), name, ModifierLocation.INTERFACE_FIELD);
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
        // [javadoc] [annotations] [modifiers] @interface Name { elements }
        if (docComment != null) {
            docComment.write(writer);
        }
        annotations.writeWithNewlines(writer);
        modifiers.write(writer);
        writer.addWordSpace();
        writer.write(Tokens.$PUNCT.AT);
        writer.write(Tokens.$KW.INTERFACE);
        writer.writeClass(name);
        writer.write(FormatPreferences.Space.BEFORE_BRACE_ANNOTATION_TYPE);
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
     * An annotation element declaration: {@code Type name() [default value];}.
     * <p>
     * This is a lightweight inner record that captures the element's name,
     * type, and optional default value, then writes the element as a method-like
     * declaration within the annotation body.
     *
     * @param name         the element name
     * @param type         the element return type
     * @param defaultValue the default value expression, or {@code null} for no default
     */
    private record AnnotationElement(String name, JType type, JExpr defaultValue) implements Writable {

        /**
         * {@inheritDoc}
         * <p>
         * Writes: {@code Type name() [default value];}.
         */
        @Override
        public void write(final SourceFileWriter writer) throws IOException {
            AbstractJExpr.writeType(writer, type);
            writer.sp();
            writer.writeName(name);
            writer.write(Tokens.$PAREN.OPEN);
            writer.write(Tokens.$PAREN.CLOSE);
            if (defaultValue != null) {
                writer.write(Tokens.$KW.DEFAULT);
                AbstractJExpr.writeExpr(writer, defaultValue);
            }
            writer.write(Tokens.$PUNCT.SEMI);
        }
    }
}
