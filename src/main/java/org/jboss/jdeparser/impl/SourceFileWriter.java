package org.jboss.jdeparser.impl;

import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.EnumMap;

import org.jboss.jdeparser.SourceVersion;
import org.jboss.jdeparser.format.FormatPreferences;
import org.jboss.jdeparser.format.FormatPreferences.Indentation;
import org.jboss.jdeparser.format.FormatPreferences.Space;
import org.jboss.jdeparser.format.FormatPreferences.SpaceType;

/**
 * The core formatting engine that writes Java source code with correct
 * spacing, indentation, and token state management.
 * <p>
 * This writer maintains three interacting state machines:
 * <ol>
 *   <li><strong>Token state</strong> — tracks which kind of token was last
 *       written (keyword, identifier, number, etc.) so that appropriate
 *       whitespace is inserted between adjacent tokens.</li>
 *   <li><strong>Spacing state</strong> — a 5-state machine that defers
 *       spacing decisions (space, newline, or nothing) until the next
 *       content write, preventing trailing whitespace on lines.</li>
 *   <li><strong>Indent stack</strong> — a stack of {@link Indent} elements
 *       that determines indentation at the start of each new line.
 *       Supports both relative (accumulating) and absolute (replacing)
 *       indentation levels.</li>
 * </ol>
 * <p>
 * Content is buffered line-by-line in a {@link StringBuilder}.  On each
 * newline, the buffer is flushed to the underlying {@link CountingWriter},
 * which tracks line and column positions.
 *
 * @see Token
 * @see Tokens
 * @see Indent
 * @see FormatPreferences
 */
public final class SourceFileWriter implements Closeable {

    // ── Spacing state constants ─────────────────────────────────────────

    /** Default: no space pending. */
    private static final int SS_NONE = 0;

    /** A space has been requested but not yet written. */
    private static final int SS_NEEDED = 1;

    /** A space has been physically added to the line buffer. */
    private static final int SS_ADDED = 2;

    /** A newline was just written; indentation is pending. */
    private static final int SS_NEW_LINE = 3;

    /** Two consecutive newlines written; further newlines are suppressed. */
    private static final int SS_2_NEW_LINE = 4;

    // ── Instance fields ─────────────────────────────────────────────────

    /** The formatting preferences. */
    private final FormatPreferences format;

    /** The source version for feature gating. */
    private final SourceVersion version;

    /** The column-tracking output writer. */
    private final CountingWriter countingWriter;

    /** Buffer for the current line being built. */
    private final StringBuilder lineBuffer = new StringBuilder(120);

    /** The platform line separator. */
    private final String lineSep = System.lineSeparator();

    /** The indentation stack. */
    private final ArrayList<Indent> indentStack = new ArrayList<>();

    /** Current traversal index into the indent stack (used during chain traversal). */
    private int traverseIdx;

    /** Pre-built ConfigIndent for each Indentation context. */
    private final EnumMap<Indentation, ConfigIndent> indentMap = new EnumMap<>(Indentation.class);

    /** The current token state. */
    private Token state = Tokens.$START;

    /** The current spacing state. */
    private int spaceState = SS_NEW_LINE;

    /**
     * Sentinel indent element that traverses the indent stack backwards
     * using an integer index, creating a delegation chain.
     */
    private final Indent nextIndent = new Indent() {
        @Override
        public void addIndent(final Indent next, final FormatPreferences preferences, final StringBuilder lineBuffer) {
            if (traverseIdx > 0) {
                final Indent indent = indentStack.get(--traverseIdx);
                indent.addIndent(this, preferences, lineBuffer);
            }
        }

        @Override
        public void escape(final Indent next, final StringBuilder b, final int idx) {
            if (traverseIdx > 0) {
                final Indent indent = indentStack.get(--traverseIdx);
                indent.escape(this, b, idx);
            }
        }

        @Override
        public void unescaped(final Indent next, final StringBuilder b, final int idx) {
            if (traverseIdx > 0) {
                final Indent indent = indentStack.get(--traverseIdx);
                indent.unescaped(this, b, idx);
            }
        }
    };

    // ── Constructor ─────────────────────────────────────────────────────

    /**
     * Constructs a source file writer.
     *
     * @param output  the underlying writer to emit source code to
     * @param format  the formatting preferences
     * @param version the source version for feature gating
     */
    public SourceFileWriter(final Writer output, final FormatPreferences format, final SourceVersion version) {
        this.countingWriter = output instanceof CountingWriter cw ? cw : new CountingWriter(output);
        this.format = format;
        this.version = version;
        for (final Indentation ind : Indentation.values()) {
            indentMap.put(ind, new ConfigIndent(ind));
        }
    }

    // ── Accessors ───────────────────────────────────────────────────────

    /**
     * Returns the formatting preferences.
     *
     * @return the format preferences
     */
    public FormatPreferences getFormat() {
        return format;
    }

    /**
     * Returns the source version.
     *
     * @return the source version
     */
    public SourceVersion getSourceVersion() {
        return version;
    }

    /**
     * Returns the current token state.
     *
     * @return the last written token (or {@link Tokens#$START} initially)
     */
    public Token getState() {
        return state;
    }

    /**
     * Returns the approximate current column position (length of
     * the line buffer, which includes indentation).
     *
     * @return the current column (0-indexed)
     */
    public int getColumn() {
        return lineBuffer.length();
    }

    /**
     * Returns the configured maximum line length.
     *
     * @return the max line length
     */
    public int getLineLength() {
        return format.lineLength();
    }

    // ── Token writing ───────────────────────────────────────────────────

    /**
     * Writes a token and updates the token state.
     * <p>
     * The token's {@link Token#write(SourceFileWriter)} method handles
     * content and spacing; this method then records the token as the
     * current state for subsequent spacing decisions.
     *
     * @param token the token to write
     * @throws IOException if an I/O error occurs
     */
    public void write(final Token token) throws IOException {
        token.write(this);
        state = token;
    }

    // ── Spacing ─────────────────────────────────────────────────────────

    /**
     * Forces a space character at the current position.
     * <p>
     * If the writer is in the {@link #SS_NEW_LINE} or {@link #SS_2_NEW_LINE}
     * state, this first adds indentation and then a space.
     */
    public void sp() {
        switch (spaceState) {
            case SS_NONE, SS_NEEDED -> {
                lineBuffer.append(' ');
                spaceState = SS_ADDED;
            }
            case SS_NEW_LINE, SS_2_NEW_LINE -> {
                addIndent();
                lineBuffer.append(' ');
                spaceState = SS_ADDED;
            }
            // SS_ADDED: space already present
        }
    }

    /**
     * Marks that a space is needed before the next content, but does not
     * emit it yet (non-trailing space).
     * <p>
     * The space is physically added by {@link #processSpacing()} when
     * the next content write occurs.  This prevents trailing spaces
     * on otherwise-empty lines.
     */
    public void ntsp() {
        if (spaceState == SS_NONE) {
            spaceState = SS_NEEDED;
        }
    }

    /**
     * Writes a newline, flushing the current line buffer to the output.
     * <p>
     * Consecutive newlines are limited to two (one blank line); a third
     * consecutive newline is suppressed to prevent excessive blank lines.
     *
     * @throws IOException if an I/O error occurs
     */
    public void nl() throws IOException {
        switch (spaceState) {
            case SS_2_NEW_LINE -> {
                // suppress third consecutive newline
                return;
            }
            case SS_NEW_LINE -> {
                // second consecutive newline: write blank line
                countingWriter.write(lineSep);
                spaceState = SS_2_NEW_LINE;
                return;
            }
            default -> {
                // flush line buffer and write newline
                if (lineBuffer.length() > 0) {
                    countingWriter.write(lineBuffer, 0, lineBuffer.length());
                    lineBuffer.setLength(0);
                }
                countingWriter.write(lineSep);
                spaceState = SS_NEW_LINE;
            }
        }
    }

    /**
     * Applies a spacing rule by looking up its configured {@link SpaceType}
     * in the format preferences.
     *
     * @param rule the spacing rule to apply
     * @throws IOException if a newline spacing type triggers I/O
     */
    public void write(final Space rule) throws IOException {
        switch (format.getSpaceType(rule)) {
            case NONE -> {}
            case SPACE -> ntsp();
            case NEWLINE -> nl();
        }
    }

    /**
     * Resolves any pending spacing state before content is written.
     * <p>
     * If a space was needed ({@link #SS_NEEDED}), a space is appended.
     * If a newline was written ({@link #SS_NEW_LINE} or {@link #SS_2_NEW_LINE}),
     * indentation is applied.
     */
    private void processSpacing() {
        switch (spaceState) {
            case SS_NEEDED -> {
                lineBuffer.append(' ');
                spaceState = SS_NONE;
            }
            case SS_NEW_LINE, SS_2_NEW_LINE -> {
                addIndent();
                spaceState = SS_NONE;
            }
            // SS_NONE, SS_ADDED: nothing to do
        }
    }

    // ── Content writing ─────────────────────────────────────────────────

    /**
     * Writes a string to the line buffer with escape chain processing.
     *
     * @param str the string to write
     */
    public void writeEscaped(final String str) {
        processSpacing();
        final int idx = lineBuffer.length();
        lineBuffer.append(str);
        traverseIdx = indentStack.size();
        nextIndent.escape(nextIndent, lineBuffer, idx);
        spaceState = SS_NONE;
    }

    /**
     * Writes a single character to the line buffer with escape chain processing.
     *
     * @param ch the character to write
     */
    public void writeEscaped(final char ch) {
        processSpacing();
        final int idx = lineBuffer.length();
        lineBuffer.append(ch);
        traverseIdx = indentStack.size();
        nextIndent.escape(nextIndent, lineBuffer, idx);
        spaceState = SS_NONE;
    }

    /**
     * Writes a string to the line buffer without escaping.
     *
     * @param str the string to write
     */
    public void writeUnescaped(final String str) {
        processSpacing();
        final int idx = lineBuffer.length();
        lineBuffer.append(str);
        traverseIdx = indentStack.size();
        nextIndent.unescaped(nextIndent, lineBuffer, idx);
        spaceState = SS_NONE;
    }

    /**
     * Writes a word (identifier, keyword text, etc.) to the line buffer
     * with escape chain processing.
     * <p>
     * This does not update the token state; the caller is responsible
     * for setting the state afterward (typically via {@link #write(Token)}).
     *
     * @param word the word to write
     */
    public void writeEscapedWord(final String word) {
        processSpacing();
        final int idx = lineBuffer.length();
        lineBuffer.append(word);
        traverseIdx = indentStack.size();
        nextIndent.escape(nextIndent, lineBuffer, idx);
        spaceState = SS_NONE;
    }

    /**
     * Writes a class/type name, adding a space if the previous token is
     * a word, keyword, or number.
     *
     * @param className the class name to write
     */
    public void writeClass(final String className) {
        addWordSpace();
        writeEscapedWord(className);
        state = Tokens.$WORD;
    }

    /**
     * Writes an identifier, adding a space if the previous token is
     * a word, keyword, or number.
     *
     * @param name the identifier to write
     */
    public void writeName(final String name) {
        addWordSpace();
        writeEscapedWord(name);
        state = Tokens.$WORD;
    }

    /**
     * Writes a number literal, adding a space if the previous token is
     * a word, keyword, or number.
     *
     * @param number the number literal text to write
     */
    public void writeNumber(final String number) {
        addWordSpace();
        writeEscapedWord(number);
        state = Tokens.$NUMBER;
    }

    /**
     * Writes a string literal, with proper quoting.
     *
     * @param literal the literal text (including quotes)
     */
    public void writeStringLiteral(final String literal) {
        addWordSpace();
        processSpacing();
        final int idx = lineBuffer.length();
        lineBuffer.append(literal);
        traverseIdx = indentStack.size();
        nextIndent.unescaped(nextIndent, lineBuffer, idx);
        state = Tokens.$STRING_LIT;
        spaceState = SS_NONE;
    }

    /**
     * Adds a space if the previous token was a word-like token
     * (identifier, keyword, or number literal), preventing adjacent
     * word tokens from running together.
     */
    public void addWordSpace() {
        if (state == Tokens.$WORD || state == Tokens.$NUMBER || state instanceof Tokens.$KW) {
            sp();
        }
    }

    // ── Indentation stack ───────────────────────────────────────────────

    /**
     * Pushes an indent element onto the indentation stack.
     *
     * @param indent the indent element to push
     */
    public void pushIndent(final Indent indent) {
        indentStack.add(indent);
    }

    /**
     * Pushes the {@link ConfigIndent} for the given indentation context
     * onto the indentation stack.
     *
     * @param indentation the indentation context
     */
    public void pushIndent(final Indentation indentation) {
        pushIndent(indentMap.get(indentation));
    }

    /**
     * Pops an indent element from the indentation stack.
     * <p>
     * Verifies that the popped element matches the expected element.
     *
     * @param indent the expected indent element
     * @throws IllegalStateException if the stack is empty or the top element
     *                               does not match the expected element
     */
    public void popIndent(final Indent indent) {
        if (indentStack.isEmpty()) {
            throw new IllegalStateException("Indent stack underflow");
        }
        final Indent popped = indentStack.remove(indentStack.size() - 1);
        if (popped != indent) {
            throw new IllegalStateException("Indent stack mismatch: expected " + indent + " but got " + popped);
        }
    }

    /**
     * Pops the {@link ConfigIndent} for the given indentation context
     * from the indentation stack.
     *
     * @param indentation the indentation context
     * @throws IllegalStateException if the stack is empty or the top element
     *                               does not match
     */
    public void popIndent(final Indentation indentation) {
        popIndent(indentMap.get(indentation));
    }

    /**
     * Applies indentation to the line buffer by traversing the indent
     * stack from top to bottom via the delegation chain.
     */
    private void addIndent() {
        traverseIdx = indentStack.size();
        nextIndent.addIndent(nextIndent, format, lineBuffer);
    }

    // ── Lifecycle ───────────────────────────────────────────────────────

    /**
     * Flushes any remaining content in the line buffer and the
     * underlying writer.
     *
     * @throws IOException if an I/O error occurs
     */
    public void flush() throws IOException {
        if (lineBuffer.length() > 0) {
            countingWriter.write(lineBuffer, 0, lineBuffer.length());
            lineBuffer.setLength(0);
        }
        countingWriter.flush();
    }

    /**
     * Flushes any remaining content and closes the underlying writer.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void close() throws IOException {
        flush();
        countingWriter.close();
    }
}
