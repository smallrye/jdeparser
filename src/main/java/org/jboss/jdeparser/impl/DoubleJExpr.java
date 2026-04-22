package org.jboss.jdeparser.impl;

import java.io.IOException;

/**
 * An expression representing a {@code double} literal.
 * <p>
 * The literal can be rendered in either decimal or hexadecimal floating-point
 * notation, as determined by {@link #isHex()}.
 */
public final class DoubleJExpr extends AbstractJExpr {

    private final double value;
    private final boolean hex;

    /**
     * Constructs a new double literal expression.
     *
     * @param value the double value
     * @param hex   {@code true} to render in hexadecimal float notation,
     *              {@code false} for decimal notation
     */
    public DoubleJExpr(final double value, final boolean hex) {
        this.value = value;
        this.hex = hex;
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
     * Returns the double value of this literal.
     *
     * @return the double value
     */
    public double value() {
        return value;
    }

    /**
     * Returns whether this literal uses hexadecimal float notation.
     *
     * @return {@code true} for hexadecimal notation, {@code false} for decimal
     */
    public boolean isHex() {
        return hex;
    }

    /** {@inheritDoc} */
    @Override
    public void write(final SourceFileWriter writer) throws IOException {
        final String text = hex ? Double.toHexString(value) : Double.toString(value);
        writer.writeNumber(text);
    }
}
