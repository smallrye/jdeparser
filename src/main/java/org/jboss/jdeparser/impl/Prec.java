package org.jboss.jdeparser.impl;

/**
 * Java operator precedence levels, ordered from lowest to highest.
 * <p>
 * Used internally to determine when parentheses must be emitted around
 * sub-expressions during source generation.  Higher ordinal values
 * indicate tighter binding.
 *
 * @see Assoc
 */
public enum Prec {
    /** Assignment operators ({@code =}, {@code +=}, {@code -=}, etc.). */
    ASSIGNMENT,
    /** Ternary conditional ({@code ? :}). */
    TERNARY,
    /** Logical OR ({@code ||}). */
    LOGICAL_OR,
    /** Logical AND ({@code &&}). */
    LOGICAL_AND,
    /** Bitwise OR ({@code |}). */
    BITWISE_OR,
    /** Bitwise XOR ({@code ^}). */
    BITWISE_XOR,
    /** Bitwise AND ({@code &}). */
    BITWISE_AND,
    /** Equality operators ({@code ==}, {@code !=}). */
    EQUALITY,
    /** Relational operators ({@code <}, {@code >}, {@code <=}, {@code >=}, {@code instanceof}). */
    RELATIONAL,
    /** Shift operators ({@code <<}, {@code >>}, {@code >>>}). */
    SHIFT,
    /** Additive operators ({@code +}, {@code -}). */
    ADDITIVE,
    /** Multiplicative operators ({@code *}, {@code /}, {@code %}). */
    MULTIPLICATIVE,
    /** Unary operators ({@code ++}, {@code --}, {@code +}, {@code -}, {@code ~}, {@code !}, cast). */
    UNARY,
    /** Postfix operators ({@code ++}, {@code --}) and member access. */
    POSTFIX,
    /** Primary expressions (literals, {@code this}, {@code super}, {@code new}, etc.). */
    PRIMARY,
    ;
}
