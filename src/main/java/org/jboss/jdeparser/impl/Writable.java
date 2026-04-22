package org.jboss.jdeparser.impl;

import java.io.IOException;

/**
 * An element that can write itself to a {@link SourceFileWriter}.
 * <p>
 * Implemented by all AST nodes (types, expressions, statements) and tokens.
 * The formatting engine calls {@link #write(SourceFileWriter)} to emit source
 * code; implementations use the writer's spacing, indentation, and token
 * state machine methods to produce correctly formatted output.
 */
@FunctionalInterface
public interface Writable {

    /**
     * Writes this element to the given source file writer.
     *
     * @param writer the writer to emit source code to
     * @throws IOException if an I/O error occurs
     */
    void write(SourceFileWriter writer) throws IOException;
}
