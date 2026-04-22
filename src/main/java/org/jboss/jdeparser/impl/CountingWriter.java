package org.jboss.jdeparser.impl;

import java.io.IOException;
import java.io.Writer;

/**
 * A writer wrapper that tracks line and column position as characters
 * are written, and buffers output for efficiency.
 * <p>
 * Line and column numbers are 1-indexed.  The column is reset to 1 on
 * each newline ({@code '\n'}) or carriage return ({@code '\r'}).  Line
 * numbers increment only on {@code '\n'}.
 * <p>
 * Internally, a 4 KB buffer reduces the number of system-level write
 * calls to the underlying writer.
 */
public final class CountingWriter extends Writer {

    /** Internal buffer size (4 KB). */
    private static final int BUFFER_SIZE = 4096;

    /** The underlying writer. */
    private final Writer out;

    /** Internal write buffer. */
    private final char[] buffer = new char[BUFFER_SIZE];

    /** Current line number (1-indexed). */
    private int line = 1;

    /** Current column number (1-indexed). */
    private int column = 1;

    /** Number of valid characters in the buffer. */
    private int bsz;

    /**
     * Constructs a counting writer wrapping the given writer.
     *
     * @param out the underlying writer
     */
    public CountingWriter(final Writer out) {
        this.out = out;
    }

    /**
     * Returns the current line number (1-indexed).
     *
     * @return the line number
     */
    public int getLine() {
        return line;
    }

    /**
     * Returns the current column number (1-indexed).
     *
     * @return the column number
     */
    public int getColumn() {
        return column;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(final int c) throws IOException {
        if (c == '\n') {
            line++;
            column = 1;
        } else if (c == '\r') {
            column = 1;
        } else {
            column++;
        }
        if (bsz == BUFFER_SIZE) {
            out.write(buffer, 0, bsz);
            bsz = 0;
        }
        buffer[bsz++] = (char) c;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(final char[] cbuf) throws IOException {
        write(cbuf, 0, cbuf.length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(final char[] chars, final int off, final int len) throws IOException {
        for (int i = 0; i < len; i++) {
            final char c = chars[off + i];
            if (c == '\n') {
                line++;
                column = 1;
            } else if (c == '\r') {
                column = 1;
            } else {
                column++;
            }
            if (bsz == BUFFER_SIZE) {
                out.write(buffer, 0, bsz);
                bsz = 0;
            }
            buffer[bsz++] = c;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(final String str) throws IOException {
        write(str, 0, str.length());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(final String str, final int off, final int len) throws IOException {
        for (int i = 0; i < len; i++) {
            final char c = str.charAt(off + i);
            if (c == '\n') {
                line++;
                column = 1;
            } else if (c == '\r') {
                column = 1;
            } else {
                column++;
            }
            if (bsz == BUFFER_SIZE) {
                out.write(buffer, 0, bsz);
                bsz = 0;
            }
            buffer[bsz++] = c;
        }
    }

    /**
     * Writes the contents of a {@link StringBuilder} to this writer.
     *
     * @param b the string builder whose contents to write
     * @throws IOException if an I/O error occurs
     */
    public void write(final StringBuilder b) throws IOException {
        write(b, 0, b.length());
    }

    /**
     * Writes a portion of a {@link StringBuilder} to this writer.
     *
     * @param b   the string builder
     * @param off the start offset
     * @param len the number of characters to write
     * @throws IOException if an I/O error occurs
     */
    public void write(final StringBuilder b, final int off, final int len) throws IOException {
        for (int i = 0; i < len; i++) {
            final char c = b.charAt(off + i);
            if (c == '\n') {
                line++;
                column = 1;
            } else if (c == '\r') {
                column = 1;
            } else {
                column++;
            }
            if (bsz == BUFFER_SIZE) {
                out.write(buffer, 0, bsz);
                bsz = 0;
            }
            buffer[bsz++] = c;
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Flushes the internal buffer and the underlying writer.
     */
    @Override
    public void flush() throws IOException {
        if (bsz > 0) {
            out.write(buffer, 0, bsz);
            bsz = 0;
        }
        out.flush();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Flushes the internal buffer and closes the underlying writer.
     */
    @Override
    public void close() throws IOException {
        if (bsz > 0) {
            out.write(buffer, 0, bsz);
            bsz = 0;
        }
        out.close();
    }
}
