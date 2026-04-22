package org.jboss.jdeparser.impl;

import java.io.IOException;

/**
 * An expression representing a character literal.
 * <p>
 * The code point is stored as an {@code int} to support the full range
 * of Unicode code points, including supplementary characters beyond the
 * Basic Multilingual Plane.
 */
public final class CharJExpr extends AbstractJExpr {

    private final int codePoint;

    /**
     * Constructs a new character literal expression.
     *
     * @param codePoint the Unicode code point of the character
     */
    public CharJExpr(final int codePoint) {
        this.codePoint = codePoint;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link Prec#PRIMARY}
     */
    @Override
    public Prec precedence() {
        return Prec.PRIMARY;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link Assoc#NONE}
     */
    @Override
    public Assoc associativity() {
        return Assoc.NONE;
    }

    /**
     * Returns the Unicode code point of this character literal.
     *
     * @return the code point
     */
    public int codePoint() {
        return codePoint;
    }

    /** {@inheritDoc} */
    @Override
    public void write(final SourceFileWriter writer) throws IOException {
        writer.writeStringLiteral("'" + escapeChar(codePoint) + "'");
    }

    /**
     * Escapes a code point for use in a character literal.
     *
     * @param cp the code point
     * @return the escaped string representation
     */
    private static String escapeChar(final int cp) {
        return switch (cp) {
            case '\'' -> "\\'";
            case '\\' -> "\\\\";
            case '\n' -> "\\n";
            case '\r' -> "\\r";
            case '\t' -> "\\t";
            case '\b' -> "\\b";
            case '\f' -> "\\f";
            default -> {
                if (cp >= 0x20 && cp < 0x7F) {
                    yield String.valueOf((char) cp);
                } else if (cp <= 0xFFFF) {
                    yield String.format("\\u%04x", cp);
                } else {
                    final char[] chars = Character.toChars(cp);
                    yield String.format("\\u%04x\\u%04x", (int) chars[0], (int) chars[1]);
                }
            }
        };
    }
}
