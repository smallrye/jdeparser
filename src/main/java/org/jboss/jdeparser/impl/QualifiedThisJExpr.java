package org.jboss.jdeparser.impl;

import java.io.IOException;

import org.jboss.jdeparser.JType;

/**
 * A qualified {@code this} expression: {@code Outer.this}.
 */
public final class QualifiedThisJExpr extends AbstractJExpr {

    private final JType qualifier;

    /**
     * Constructs a new qualified this expression.
     *
     * @param qualifier the enclosing type qualifier
     */
    public QualifiedThisJExpr(final JType qualifier) {
        this.qualifier = qualifier;
    }

    /**
     * Returns the enclosing type qualifier.
     *
     * @return the qualifier type
     */
    public JType qualifier() {
        return qualifier;
    }

    /** {@inheritDoc} */
    @Override
    public Prec precedence() {
        return Prec.PRIMARY;
    }

    /** {@inheritDoc} */
    @Override
    public Assoc associativity() {
        return Assoc.NONE;
    }

    /** {@inheritDoc} */
    @Override
    public void write(final SourceFileWriter writer) throws IOException {
        // Outer.this
        writeType(writer, qualifier);
        writer.write(Tokens.$PUNCT.DOT);
        writer.write(Tokens.$KW.THIS);
    }
}
