package org.jboss.jdeparser.impl;

import java.io.IOException;
import java.util.List;

import org.jboss.jdeparser.JExpr;
import org.jboss.jdeparser.JType;

/**
 * An array creation expression with dimension expressions: {@code new int[n]}.
 */
public final class NewArrayJExpr extends AbstractJExpr {

    private final JType elementType;
    private final List<JExpr> dimensions;

    /**
     * Constructs a new array creation expression.
     *
     * @param elementType the array element type
     * @param dimensions  the dimension size expressions
     */
    public NewArrayJExpr(final JType elementType, final List<JExpr> dimensions) {
        this.elementType = elementType;
        this.dimensions = List.copyOf(dimensions);
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
     * Returns the dimension size expressions.
     *
     * @return an unmodifiable list of dimension expressions
     */
    public List<JExpr> dimensions() {
        return dimensions;
    }

    /** {@inheritDoc} */
    @Override
    public Prec precedence() {
        return Prec.UNARY;
    }

    /** {@inheritDoc} */
    @Override
    public Assoc associativity() {
        return Assoc.RIGHT;
    }

    /** {@inheritDoc} */
    @Override
    public void write(final SourceFileWriter writer) throws IOException {
        // new Type[dim1][dim2]
        writer.write(Tokens.$KW.NEW);
        writeType(writer, elementType);
        for (JExpr dim : dimensions) {
            writer.write(Tokens.$BRACKET.OPEN);
            writeExpr(writer, dim);
            writer.write(Tokens.$BRACKET.CLOSE);
        }
    }
}
