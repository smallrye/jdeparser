package org.jboss.jdeparser.impl;

import java.io.IOException;

/**
 * An expression representing a {@code long} literal with an associated numeric base.
 * <p>
 * The {@link #base()} determines how the literal is rendered in the generated source:
 * base 2 produces a {@code 0b} prefix, base 8 produces a {@code 0} prefix,
 * base 10 produces no prefix, and base 16 produces a {@code 0x} prefix.
 * The rendered literal always includes the {@code L} suffix.
 */
public final class LongJExpr extends AbstractJExpr {

    private final long value;
    private final int base;

    /**
     * Constructs a new long literal expression.
     *
     * @param value the long value
     * @param base  the numeric base for rendering (2, 8, 10, or 16)
     */
    public LongJExpr(final long value, final int base) {
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
     * Returns the long value of this literal.
     *
     * @return the long value
     */
    public long value() {
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
            case 16 -> "0x" + Long.toHexString(value) + "L";
            case 10 -> Long.toUnsignedString(value) + "L";
            case 8 -> "0" + Long.toOctalString(value) + "L";
            case 2 -> "0b" + Long.toBinaryString(value) + "L";
        };
        writer.writeNumber(text);
    }
}
