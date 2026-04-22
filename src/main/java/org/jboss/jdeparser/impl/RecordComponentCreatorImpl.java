package org.jboss.jdeparser.impl;

import java.io.IOException;
import java.util.function.Consumer;

import org.jboss.jdeparser.JType;
import org.jboss.jdeparser.SourceVersion;
import org.jboss.jdeparser.creator.AnnotationCreator;
import org.jboss.jdeparser.creator.DocCommentCreator;
import org.jboss.jdeparser.creator.RecordComponentCreator;

/**
 * Implementation of {@link RecordComponentCreator} that collects component
 * configuration and writes the component declaration within a record's
 * component list.
 * <p>
 * Writes the form: {@code [annotations] Type name}.
 * <p>
 * Record components support annotations and documentation but do not
 * have modifiers of their own.
 */
public final class RecordComponentCreatorImpl extends AbstractCreator implements RecordComponentCreator, Writable {

    /** The component name. */
    private final String name;

    /** The component type. */
    private final JType type;

    /** The annotation holder. */
    private final AnnotationHolder annotations = new AnnotationHolder();

    /** Optional doc comment. */
    private DocCommentCreatorImpl docComment;

    /**
     * Constructs a new record component creator.
     *
     * @param version the source version
     * @param name    the component name
     * @param type    the component type
     */
    public RecordComponentCreatorImpl(final SourceVersion version, final String name, final JType type) {
        super(version);
        this.name = name;
        this.type = type;
    }

    /**
     * Returns the component name.
     *
     * @return the component name
     */
    public String name() {
        return name;
    }

    /**
     * Returns the optional doc comment creator, if one was configured.
     *
     * @return the doc comment creator, or {@code null} if none
     */
    public DocCommentCreatorImpl docComment() {
        return docComment;
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
        // [annotations] Type name
        annotations.writeWithSpaces(writer);
        AbstractJExpr.writeType(writer, type);
        writer.sp();
        writer.writeName(name);
    }
}
