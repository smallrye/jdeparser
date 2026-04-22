package org.jboss.jdeparser;

import org.jboss.jdeparser.impl.AbstractJVar;

/**
 * An expression that represents an assignable variable reference, such as a local variable, parameter,
 * field reference, or array element access.
 * <p>
 * This interface extends {@link JExpr} to add compound and simple assignment operations. Each assignment
 * method returns a {@link JExpr} representing the assignment expression as a whole.
 */
public sealed interface JVar extends JExpr permits AbstractJVar {

    /**
     * Returns an expression representing a simple assignment to this variable ({@code this = value}).
     *
     * @param value the value to assign
     * @return the assignment expression
     */
    JExpr assign(JExpr value);

    /**
     * Returns an expression representing an addition assignment to this variable ({@code this += value}).
     *
     * @param value the value to add and assign
     * @return the compound assignment expression
     */
    JExpr addAssign(JExpr value);

    /**
     * Returns an expression representing a subtraction assignment to this variable ({@code this -= value}).
     *
     * @param value the value to subtract and assign
     * @return the compound assignment expression
     */
    JExpr subAssign(JExpr value);

    /**
     * Returns an expression representing a multiplication assignment to this variable ({@code this *= value}).
     *
     * @param value the value to multiply and assign
     * @return the compound assignment expression
     */
    JExpr mulAssign(JExpr value);

    /**
     * Returns an expression representing a division assignment to this variable ({@code this /= value}).
     *
     * @param value the value to divide by and assign
     * @return the compound assignment expression
     */
    JExpr divAssign(JExpr value);

    /**
     * Returns an expression representing a remainder assignment to this variable ({@code this %= value}).
     *
     * @param value the value to compute the remainder with and assign
     * @return the compound assignment expression
     */
    JExpr modAssign(JExpr value);

    /**
     * Returns an expression representing a bitwise AND assignment to this variable ({@code this &= value}).
     *
     * @param value the value to bitwise AND with and assign
     * @return the compound assignment expression
     */
    JExpr bitAndAssign(JExpr value);

    /**
     * Returns an expression representing a bitwise OR assignment to this variable ({@code this |= value}).
     *
     * @param value the value to bitwise OR with and assign
     * @return the compound assignment expression
     */
    JExpr bitOrAssign(JExpr value);

    /**
     * Returns an expression representing a bitwise XOR assignment to this variable ({@code this ^= value}).
     *
     * @param value the value to bitwise XOR with and assign
     * @return the compound assignment expression
     */
    JExpr bitXorAssign(JExpr value);

    /**
     * Returns an expression representing a left shift assignment to this variable ({@code this <<= value}).
     *
     * @param value the shift distance to left shift by and assign
     * @return the compound assignment expression
     */
    JExpr shlAssign(JExpr value);

    /**
     * Returns an expression representing a signed right shift assignment to this variable ({@code this >>= value}).
     *
     * @param value the shift distance to signed right shift by and assign
     * @return the compound assignment expression
     */
    JExpr shrAssign(JExpr value);

    /**
     * Returns an expression representing an unsigned right shift assignment to this variable ({@code this >>>= value}).
     *
     * @param value the shift distance to unsigned right shift by and assign
     * @return the compound assignment expression
     */
    JExpr ushrAssign(JExpr value);
}
