package org.jboss.jdeparser.impl;

import java.io.IOException;
import java.util.List;

import org.jboss.jdeparser.JExpr;
import org.jboss.jdeparser.JType;
import org.jboss.jdeparser.format.FormatPreferences;

/**
 * An array creation expression with an initializer: {@code new int[] {1, 2, 3}}.
 */
public final class ArrayInitJExpr extends AbstractJExpr {

    private final JType elementType;
    private final List<JExpr> elements;

    /**
     * Constructs a new array initializer expression.
     *
     * @param elementType the array element type
     * @param elements    the initializer element expressions
     */
    public ArrayInitJExpr(final JType elementType, final List<JExpr> elements) {
        this.elementType = elementType;
        this.elements = List.copyOf(elements);
    }

    /**
     * Returns the array element type.
     *
     * @return the element type
     */
    public JType elementType() {
        return elementType;
    }

    /**
     * Returns the initializer element expressions.
     *
     * @return an unmodifiable list of elements
     */
    public List<JExpr> elements() {
        return elements;
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
        // new Type[] { e1, e2, e3 }
        writer.write(Tokens.$KW.NEW);
        writeType(writer, elementType);
        writer.write(Tokens.$BRACKET.OPEN);
        writer.write(Tokens.$BRACKET.CLOSE);
        writer.write(FormatPreferences.Space.BEFORE_BRACE_ARRAY_INIT);
        writer.write(Tokens.$BRACE.OPEN);
        writer.write(FormatPreferences.Space.WITHIN_BRACES_ARRAY_INIT);
        boolean first = true;
        for (JExpr elem : elements) {
            if (!first) {
                writer.write(Tokens.$PUNCT.COMMA);
                writer.write(FormatPreferences.Space.AFTER_COMMA);
            }
            first = false;
            writeExpr(writer, elem);
        }
        writer.write(FormatPreferences.Space.WITHIN_BRACES_ARRAY_INIT);
        writer.write(Tokens.$BRACE.CLOSE);
    }
}
