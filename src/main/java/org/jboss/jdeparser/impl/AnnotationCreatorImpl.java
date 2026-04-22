package org.jboss.jdeparser.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jboss.jdeparser.JExpr;
import org.jboss.jdeparser.JType;
import org.jboss.jdeparser.SourceVersion;
import org.jboss.jdeparser.creator.AnnotationCreator;
import org.jboss.jdeparser.format.FormatPreferences;

/**
 * Implementation of {@link AnnotationCreator} that collects annotation member
 * values and writes the annotation in source form.
 * <p>
 * Supports three forms:
 * <ul>
 *   <li>Marker annotation: {@code @Type}</li>
 *   <li>Single-value annotation: {@code @Type(value)}</li>
 *   <li>Multi-member annotation: {@code @Type(name1 = value1, name2 = value2)}</li>
 * </ul>
 */
public final class AnnotationCreatorImpl extends AbstractCreator implements AnnotationCreator, Writable {

    /** The annotation type. */
    private final JType annotationType;

    /** The collected annotation member entries. */
    private final List<Writable> entries = new ArrayList<>();

    /**
     * Constructs a new annotation creator.
     *
     * @param version        the source version
     * @param annotationType the annotation type
     */
    public AnnotationCreatorImpl(final SourceVersion version, final JType annotationType) {
        super(version);
        this.annotationType = annotationType;
    }

    /** {@inheritDoc} */
    @Override
    public void value(final JExpr value) {
        checkActive();
        entries.add(w -> AbstractJExpr.writeExpr(w, value));
    }

    /** {@inheritDoc} */
    @Override
    public void member(final String name, final JExpr value) {
        checkActive();
        entries.add(w -> {
            w.writeName(name);
            w.write(Tokens.$BINOP.ASSIGN);
            AbstractJExpr.writeExpr(w, value);
        });
    }

    /** {@inheritDoc} */
    @Override
    public void memberArray(final String name, final JExpr... values) {
        checkActive();
        final List<JExpr> vals = List.of(values);
        entries.add(w -> {
            w.writeName(name);
            w.write(Tokens.$BINOP.ASSIGN);
            w.write(Tokens.$BRACE.OPEN);
            boolean first = true;
            for (JExpr val : vals) {
                if (!first) {
                    w.write(Tokens.$PUNCT.COMMA);
                    w.write(FormatPreferences.Space.AFTER_COMMA);
                }
                first = false;
                AbstractJExpr.writeExpr(w, val);
            }
            w.write(Tokens.$BRACE.CLOSE);
        });
    }

    /** {@inheritDoc} */
    @Override
    public void write(final SourceFileWriter writer) throws IOException {
        writer.write(Tokens.$PUNCT.AT);
        AbstractJExpr.writeType(writer, annotationType);
        if (!entries.isEmpty()) {
            writer.write(Tokens.$PAREN.OPEN);
            boolean first = true;
            for (Writable entry : entries) {
                if (!first) {
                    writer.write(Tokens.$PUNCT.COMMA);
                    writer.write(FormatPreferences.Space.AFTER_COMMA);
                }
                first = false;
                entry.write(writer);
            }
            writer.write(Tokens.$PAREN.CLOSE);
        }
    }
}
