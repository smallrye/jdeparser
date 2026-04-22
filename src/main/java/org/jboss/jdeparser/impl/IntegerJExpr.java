package org.jboss.jdeparser.impl;

import java.io.IOException;

/**
 * An expression representing an {@code int} literal with an associated numeric base.
 * <p>
 * The {@link #base()} determines how the literal is rendered in the generated source:
 * base 2 produces a {@code 0b} prefix, base 8 produces a {@code 0} prefix,
 * base 10 produces no prefix, and base 16 produces a {@code 0x} prefix.
 */
public final class IntegerJExpr extends AbstractJExpr {

    /** The integer literal {@code 0} in base 10. */
    public static final IntegerJExpr ZERO = new IntegerJExpr(0, 10);

    /** The integer literal {@code 1} in base 10. */
    public static final IntegerJExpr ONE = new IntegerJExpr(1, 10);

    private final int value;
    private final int base;

    /**
     * Constructs a new integer literal expression.
     *
     * @param value the integer value
     * @param base  the numeric base for rendering (2, 8, 10, or 16)
     */
    public IntegerJExpr(final int value, final int base) {
        this.value = value;
        this.base = base;
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
     * Returns the integer value of this literal.
     *
     * @return the integer value
     */
    public int value() {
        return value;
    }

    /**
     * Returns the numeric base used for rendering this literal.
     *
     * @return the base (2, 8, 10, or 16)
     */
    public int base() {
        return base;
    }

    /** {@inheritDoc} */
    @Override
    public void write(final SourceFileWriter writer) throws IOException {
        final String text = switch (base) {
            case 16 -> "0x" + Integer.toHexString(value);
            case 10 -> Integer.toUnsignedString(value);
            case 8 -> "0" + Integer.toOctalString(value);
            case 2 -> "0b" + Integer.toBinaryString(value);
            default -> throw new IllegalStateException();
        };
        writer.writeNumber(text);
    }
}
