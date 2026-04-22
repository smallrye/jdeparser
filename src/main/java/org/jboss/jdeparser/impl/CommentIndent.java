package org.jboss.jdeparser.impl;

import org.jboss.jdeparser.format.FormatPreferences;

/**
 * Indentation elements for comment regions.
 * <p>
 * When pushed onto the indent stack, these elements prepend comment
 * continuation prefixes ({@code "// "} for line comments, {@code " * "}
 * for block/Javadoc comments) at the start of each new line within the
 * comment body.
 * <p>
 * The {@link #BLOCK} variant also escapes {@code * /} sequences in
 * written content to prevent premature comment termination, using a
 * zero-width joiner (U+200D) to break the sequence.
 */
public enum CommentIndent implements Indent {

    /**
     * Line comment indentation: prepends {@code "// "} on continuation lines.
     */
    LINE("// "),

    /**
     * Block/Javadoc comment indentation: prepends {@code " * "} on
     * continuation lines and escapes {@code * /} sequences in content.
     */
    BLOCK(" * ") {
        /**
         * {@inheritDoc}
         * <p>
         * Scans the newly written content for {@code * /} sequences and
         * inserts a zero-width joiner (U+200D) between them to prevent
         * premature comment termination.
         */
        @Override
        public void escape(final Indent next, final StringBuilder b, final int idx) {
            // scan for */ sequences that would close the comment
            for (int i = Math.max(idx, 1); i < b.length(); i++) {
                if (b.charAt(i - 1) == '*' && b.charAt(i) == '/') {
                    b.insert(i, '\u200D');
                    i++; // skip past inserted character
                }
            }
            next.escape(next, b, idx);
        }
    },
    ;

    /** The comment continuation prefix text. */
    private final String text;

    /**
     * Constructs a comment indentation element with the given prefix.
     *
     * @param text the comment continuation prefix
     */
    CommentIndent(final String text) {
        this.text = text;
    }

    /**
     * Returns the comment continuation prefix text.
     *
     * @return the prefix (e.g., {@code "// "} or {@code " * "})
     */
    public String text() {
        return text;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Delegates to the next element to apply parent indentation, then
     * appends the comment continuation prefix and escapes it.
     */
    @Override
    public void addIndent(final Indent next, final FormatPreferences preferences, final StringBuilder lineBuffer) {
        next.addIndent(next, preferences, lineBuffer);
        final int idx = lineBuffer.length();
        lineBuffer.append(text);
        next.escape(next, lineBuffer, idx);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Delegates to the next element without modification.
     * The {@link #BLOCK} variant overrides this to escape {@code * /} sequences.
     */
    @Override
    public void escape(final Indent next, final StringBuilder b, final int idx) {
        next.escape(next, b, idx);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Delegates to the escape chain, since even "unescaped" content within
     * a comment may need structural escaping.
     */
    @Override
    public void unescaped(final Indent next, final StringBuilder b, final int idx) {
        next.escape(next, b, idx);
    }
}
