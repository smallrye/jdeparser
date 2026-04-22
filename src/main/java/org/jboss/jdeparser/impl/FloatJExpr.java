package org.jboss.jdeparser.impl;

import java.io.IOException;

/**
 * An expression representing a {@code float} literal.
 * <p>
 * The literal can be rendered in either decimal or hexadecimal floating-point
 * notation, as determined by {@link #isHex()}.  The rendered literal always
 * includes the {@code f} or {@code F} suffix.
 */
public final class FloatJExpr extends AbstractJExpr {

    private final float value;
    private final boolean hex;

    /**
     * Constructs a new float literal expression.
     *
     * @param value the float value
     * @param hex   {@code true} to render in hexadecimal float notation,
     *              {@code false} for decimal notation
     */
    public FloatJExpr(final float value, final boolean hex) {
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
     * Returns the float value of this literal.
     *
     * @return the float value
     */
    public float value() {
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
        final String text = hex ? Float.toHexString(value) + "f" : Float.toString(value) + "f";
        writer.writeNumber(text);
    }
}
