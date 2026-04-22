package org.jboss.jdeparser.impl;

import java.io.IOException;

import org.jboss.jdeparser.JExpr;
import org.jboss.jdeparser.LanguageFeature;
import org.jboss.jdeparser.format.FormatPreferences;

/**
 * A switch expression used in expression context (Java 14+):
 * {@code switch (selector) &#123; case ... &#125;}.
 * <p>
 * Switch expressions produce a value and can be used anywhere an expression
 * is expected.  The cases typically use arrow syntax ({@code ->}) and/or
 * {@code yield} to produce the value.
 *
 * @see LanguageFeature#SWITCH_EXPRESSIONS
 */
public final class SwitchJExpr extends AbstractJExpr {

    private final JExpr selector;
    private final SwitchCreatorImpl cases;

    /**
     * Constructs a new switch expression.
     *
     * @param selector the selector expression
     * @param cases    the switch cases creator
     */
    public SwitchJExpr(final JExpr selector, final SwitchCreatorImpl cases) {
        this.selector = selector;
        this.cases = cases;
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

    /** {@inheritDoc} */
    @Override
    public void write(final SourceFileWriter writer) throws IOException {
        // switch (selector) { cases }
        writer.write(Tokens.$KW.SWITCH);
        writer.write(FormatPreferences.Space.BEFORE_PAREN_SWITCH);
        writer.write(Tokens.$PAREN.OPEN);
        writeExpr(writer, selector);
        writer.write(Tokens.$PAREN.CLOSE);
        writer.write(FormatPreferences.Space.BEFORE_BRACE_SWITCH);
        cases.writeBlock(writer);
    }
}
