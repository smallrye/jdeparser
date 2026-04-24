package org.jboss.jdeparser.impl;

import java.io.IOException;
import java.util.function.Consumer;

import io.smallrye.common.constraint.Assert;

import org.jboss.jdeparser.JExpr;
import org.jboss.jdeparser.JType;
import org.jboss.jdeparser.SourceVersion;
import org.jboss.jdeparser.creator.AnnotationCreator;
import org.jboss.jdeparser.creator.LocalVarCreator;

/**
 * Implementation of {@link LocalVarCreator} that collects local variable
 * configuration and writes the variable declaration within a block.
 * <p>
 * Writes the form: {@code [annotations] [final] Type name = init;} or
 * {@code [annotations] [final] var name = init;} when the type is inferred.
 */
public final class LocalVarCreatorImpl extends AbstractCreator implements LocalVarCreator, Writable {

    /** The variable name. */
    private final String name;

    /** The variable type, or {@code null} for inferred type ({@code var}). */
    private final JType type;

    /** The initializer expression. */
    private final JExpr init;

    /** The annotation holder. */
    private final AnnotationHolder annotations = new AnnotationHolder();

    /** Whether the variable is declared {@code final}. */
    private boolean isFinal;

    /**
     * Constructs a new local variable creator.
     *
     * @param version the source version
     * @param name    the variable name
     * @param type    the variable type, or {@code null} for inferred type
     * @param init    the initializer expression
     */
    public LocalVarCreatorImpl(final SourceVersion version, final String name, final JType type, final JExpr init) {
        super(version);
        this.name = name;
        this.type = type;
        this.init = init;
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
    public void final_() {
        checkActive();
        isFinal = true;
    }

    /** {@inheritDoc} */
    @Override
    public void write(final SourceFileWriter writer) throws IOException {
        // [annotations] [final] Type|var name = init;
        annotations.writeWithNewlines(writer);
        if (isFinal) {
            writer.write(Tokens.$KW.FINAL);
        }
        if (type != null) {
            AbstractJExpr.writeType(writer, type);
        } else {
            writer.write(Tokens.$KW.VAR);
        }
        writer.sp();
        writer.writeName(name);
        writer.write(Tokens.$BINOP.ASSIGN);
        AbstractJExpr.writeExpr(writer, init);
        writer.write(Tokens.$PUNCT.SEMI);
        writer.nl();
    }
}
