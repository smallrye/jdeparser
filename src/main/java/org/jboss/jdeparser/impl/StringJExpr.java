package org.jboss.jdeparser.impl;

import java.io.IOException;

/**
 * An expression representing a string literal.
 * <p>
 * The value is the raw string content; escape sequences are applied
 * during source generation by the formatting engine.
 */
public final class StringJExpr extends AbstractJExpr {

    private final String value;

    /**
     * Constructs a new string literal expression.
     *
     * @param value the string value (must not be {@code null})
     */
    public StringJExpr(final String value) {
        this.value = value;
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
     * Returns the string value of this literal.
     *
     * @return the string value (never {@code null})
     */
    public String value() {
        return value;
    }

    /** {@inheritDoc} */
    @Override
    public void write(final SourceFileWriter writer) throws IOException {
        writer.writeStringLiteral("\"" + escapeString(value) + "\"");
    }

    /**
     * Escapes a string for use in a Java string literal.
     *
     * @param s the raw string
     * @return the escaped string (without surrounding quotes)
     */
    static String escapeString(final String s) {
        final StringBuilder sb = new StringBuilder(s.length() + 8);
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                default -> {
                    if (c >= 0x20 && c < 0x7F) {
                        sb.append(c);
                    } else {
                        sb.append(String.format("\\u%04x", (int) c));
                    }
                }
            }
        }
        return sb.toString();
    }
}
