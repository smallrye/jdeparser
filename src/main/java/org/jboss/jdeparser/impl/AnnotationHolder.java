package org.jboss.jdeparser.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jboss.jdeparser.format.FormatPreferences;

/**
 * Accumulates annotations on a declaration and writes them in order.
 * <p>
 * Each annotation is stored as an {@link AnnotationCreatorImpl} that was
 * collected during the creator's callback.  When written, each annotation
 * is emitted followed by a newline (for declarations) or a space (for
 * parameter-level annotations).
 */
public final class AnnotationHolder {

    /** The collected annotations. */
    private final List<AnnotationCreatorImpl> annotations = new ArrayList<>();

    /**
     * Constructs an empty annotation holder.
     */
    public AnnotationHolder() {
    }

    /**
     * Adds an annotation.
     *
     * @param annotation the annotation creator
     */
    public void add(final AnnotationCreatorImpl annotation) {
        annotations.add(annotation);
    }

    /**
     * Returns whether any annotations have been added.
     *
     * @return {@code true} if non-empty
     */
    public boolean isEmpty() {
        return annotations.isEmpty();
    }

    /**
     * Writes each annotation followed by a newline, suitable for
     * declaration-level annotations (types, methods, fields).
     *
     * @param writer the writer
     * @throws IOException if an I/O error occurs
     */
    public void writeWithNewlines(final SourceFileWriter writer) throws IOException {
        for (AnnotationCreatorImpl annotation : annotations) {
            annotation.write(writer);
            writer.nl();
        }
    }

    /**
     * Writes each annotation followed by a space, suitable for
     * parameter-level annotations.
     *
     * @param writer the writer
     * @throws IOException if an I/O error occurs
     */
    public void writeWithSpaces(final SourceFileWriter writer) throws IOException {
        for (AnnotationCreatorImpl annotation : annotations) {
            annotation.write(writer);
            writer.write(FormatPreferences.Space.AFTER_PARAM_ANNOTATION);
        }
    }
}
