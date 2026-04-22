package org.jboss.jdeparser.impl;

import java.io.IOException;

/**
 * An expression representing a Java keyword used as an expression
 * ({@code this}, {@code super}, or {@code null}).
 * <p>
 * Instances are obtained from the {@link #THIS}, {@link #SUPER}, and
 * {@link #NULL} constants; the constructor is private to enforce a
 * fixed set of instances.
 */
public final class KeywordJExpr extends AbstractJExpr {

    /** The {@code this} keyword expression. */
    public static final KeywordJExpr THIS = new KeywordJExpr("this");

    /** The {@code super} keyword expression. */
    public static final KeywordJExpr SUPER = new KeywordJExpr("super");

    /** The {@code null} literal expression. */
    public static final KeywordJExpr NULL = new KeywordJExpr("null");

    private final String keyword;

    /**
     * Constructs a new keyword expression.
     *
     * @param keyword the keyword text (must not be {@code null})
     */
    private KeywordJExpr(final String keyword) {
        this.keyword = keyword;
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
     * Returns the keyword text of this expression.
     *
     * @return the keyword (never {@code null})
     */
    public String keyword() {
        return keyword;
    }

    /** {@inheritDoc} */
    @Override
    public void write(final SourceFileWriter writer) throws IOException {
        writer.write(switch (keyword) {
            case "this" -> Tokens.$KW.THIS;
            case "super" -> Tokens.$KW.SUPER;
            case "null" -> Tokens.$KW.NULL;
            default -> throw new AssertionError("Unknown keyword: " + keyword);
        });
    }
}
