package org.jboss.jdeparser.impl;

import org.jboss.jdeparser.format.FormatPreferences;

/**
 * An element on the indentation stack that controls how lines are indented
 * and how content is escaped within indented regions.
 * <p>
 * The indentation model uses a delegation chain: when a method is called,
 * the implementation performs its own work and then delegates to the
 * {@code next} element in the chain.  The chain terminates at a sentinel
 * element in the {@link SourceFileWriter} that traverses the indent stack
 * backwards via an iterator.
 * <p>
 * Three operations are supported:
 * <ul>
 *   <li>{@link #addIndent} — appends indentation characters (spaces or tabs)
 *       to the line buffer at the start of a new line</li>
 *   <li>{@link #escape} — post-processes written content to prevent structural
 *       conflicts (e.g., escaping {@code * /} inside block comments)</li>
 *   <li>{@link #unescaped} — processes content that should not be escaped
 *       (e.g., string literals within comments)</li>
 * </ul>
 *
 * @see ConfigIndent
 * @see CommentIndent
 * @see SourceFileWriter
 */
public interface Indent {

    /**
     * Adds indentation to the line buffer for the start of a new line.
     * <p>
     * Implementations should delegate to {@code next.addIndent(next, preferences, lineBuffer)}
     * to continue the chain (unless using absolute indentation, which replaces
     * rather than accumulates).
     *
     * @param next        the next element in the delegation chain
     * @param preferences the formatting preferences (for indent size, tabs, etc.)
     * @param lineBuffer  the line buffer to append indentation to
     */
    void addIndent(Indent next, FormatPreferences preferences, StringBuilder lineBuffer);

    /**
     * Post-processes content that was just written to the line buffer,
     * applying any necessary escaping.
     * <p>
     * For example, block comment indentation escapes {@code * /} sequences
     * that would prematurely close the comment.  Implementations should
     * delegate to {@code next.escape(next, b, idx)} to continue the chain.
     *
     * @param next the next element in the delegation chain
     * @param b    the line buffer containing the written content
     * @param idx  the index in the buffer where the new content starts
     */
    void escape(Indent next, StringBuilder b, int idx);

    /**
     * Post-processes content that was written without escaping.
     * <p>
     * Implementations may choose to still apply some escaping (e.g.,
     * comment indentation may still need to escape structural sequences)
     * or may delegate directly to the escape chain.  Implementations should
     * delegate to {@code next.unescaped(next, b, idx)} to continue the chain.
     *
     * @param next the next element in the delegation chain
     * @param b    the line buffer containing the written content
     * @param idx  the index in the buffer where the new content starts
     */
    void unescaped(Indent next, StringBuilder b, int idx);
}
