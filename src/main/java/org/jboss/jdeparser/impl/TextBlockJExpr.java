package org.jboss.jdeparser.impl;

import java.io.IOException;

import org.jboss.jdeparser.LanguageFeature;

/**
 * An expression representing a text block literal (Java 15+).
 * <p>
 * Text blocks are multi-line string literals delimited by triple double-quotes
 * ({@code """}).  The value is the raw string content; formatting and
 * indentation handling are applied during source generation.
 *
 * @see LanguageFeature#TEXT_BLOCKS
 */
public final class TextBlockJExpr extends AbstractJExpr {

    private final String value;

    /**
     * Constructs a new text block literal expression.
     *
     * @param value the text block content (must not be {@code null})
     */
    public TextBlockJExpr(final String value) {
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
     * Returns the text block content.
     *
     * @return the text block value (never {@code null})
     */
    public String value() {
        return value;
    }

    /** {@inheritDoc} */
    @Override
    public void write(final SourceFileWriter writer) throws IOException {
        writer.writeStringLiteral("\"\"\"\n" + value + "\"\"\"");
    }
}
