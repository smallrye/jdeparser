package org.jboss.jdeparser;

import java.util.List;

import org.jboss.jdeparser.impl.AbstractJExpr;
import org.jboss.jdeparser.impl.BooleanJExpr;
import org.jboss.jdeparser.impl.IntegerJExpr;
import org.jboss.jdeparser.impl.KeywordJExpr;

/**
 * The core expression interface for representing Java expressions in generated source code.
 * <p>
 * Expressions are composable values: each method returns a new {@link JExpr} (or {@link JVar})
 * representing the compound expression. Predefined constant expressions are available for
 * common literals and keywords.
 */
public sealed interface JExpr permits JVar, AbstractJExpr {

    /**
     * The boolean literal {@code true}.
     */
    JExpr TRUE = BooleanJExpr.TRUE;

    /**
     * The boolean literal {@code false}.
     */
    JExpr FALSE = BooleanJExpr.FALSE;

    /**
     * The keyword expression {@code this}.
     */
    JExpr THIS = KeywordJExpr.THIS;

    /**
     * The keyword expression {@code super}.
     */
    JExpr SUPER = KeywordJExpr.SUPER;

    /**
     * The null literal {@code null}.
     */
    JExpr NULL = KeywordJExpr.NULL;

    /**
     * The integer literal {@code 0}.
     */
    JExpr ZERO = IntegerJExpr.ZERO;

    /**
     * The integer literal {@code 1}.
     */
    JExpr ONE = IntegerJExpr.ONE;

    // Arithmetic operations

    /**
     * Returns an expression representing the addition of this expression and the given operand ({@code this + operand}).
     *
     * @param operand the right-hand operand
     * @return the addition expression
     */
    JExpr add(JExpr operand);

    /**
     * Returns an expression representing the subtraction of the given operand from this expression ({@code this - operand}).
     *
     * @param operand the right-hand operand
     * @return the subtraction expression
     */
    JExpr sub(JExpr operand);

    /**
     * Returns an expression representing the multiplication of this expression and the given operand ({@code this * operand}).
     *
     * @param operand the right-hand operand
     * @return the multiplication expression
     */
    JExpr mul(JExpr operand);

    /**
     * Returns an expression representing the division of this expression by the given operand ({@code this / operand}).
     *
     * @param operand the right-hand operand
     * @return the division expression
     */
    JExpr div(JExpr operand);

    /**
     * Returns an expression representing the remainder of this expression divided by the given operand ({@code this % operand}).
     *
     * @param operand the right-hand operand
     * @return the remainder expression
     */
    JExpr mod(JExpr operand);

    /**
     * Returns an expression representing the arithmetic negation of this expression ({@code -this}).
     *
     * @return the negation expression
     */
    JExpr neg();

    /**
     * Returns an expression representing the unary plus of this expression ({@code +this}).
     *
     * @return the unary plus expression
     */
    JExpr pos();

    // Bitwise operations

    /**
     * Returns an expression representing the bitwise AND of this expression and the given operand ({@code this & operand}).
     *
     * @param operand the right-hand operand
     * @return the bitwise AND expression
     */
    JExpr bitAnd(JExpr operand);

    /**
     * Returns an expression representing the bitwise OR of this expression and the given operand ({@code this | operand}).
     *
     * @param operand the right-hand operand
     * @return the bitwise OR expression
     */
    JExpr bitOr(JExpr operand);

    /**
     * Returns an expression representing the bitwise XOR of this expression and the given operand ({@code this ^ operand}).
     *
     * @param operand the right-hand operand
     * @return the bitwise XOR expression
     */
    JExpr bitXor(JExpr operand);

    /**
     * Returns an expression representing the bitwise complement of this expression ({@code ~this}).
     *
     * @return the bitwise complement expression
     */
    JExpr comp();

    // Shift operations

    /**
     * Returns an expression representing the left shift of this expression by the given operand ({@code this << operand}).
     *
     * @param operand the shift distance
     * @return the left shift expression
     */
    JExpr shl(JExpr operand);

    /**
     * Returns an expression representing the signed right shift of this expression by the given operand ({@code this >> operand}).
     *
     * @param operand the shift distance
     * @return the signed right shift expression
     */
    JExpr shr(JExpr operand);

    /**
     * Returns an expression representing the unsigned right shift of this expression by the given operand ({@code this >>> operand}).
     *
     * @param operand the shift distance
     * @return the unsigned right shift expression
     */
    JExpr ushr(JExpr operand);

    // Relational operations

    /**
     * Returns an expression representing the equality comparison of this expression and the given operand ({@code this == operand}).
     *
     * @param operand the right-hand operand
     * @return the equality expression
     */
    JExpr eq(JExpr operand);

    /**
     * Returns an expression representing the inequality comparison of this expression and the given operand ({@code this != operand}).
     *
     * @param operand the right-hand operand
     * @return the inequality expression
     */
    JExpr ne(JExpr operand);

    /**
     * Returns an expression representing the less-than comparison of this expression and the given operand ({@code this < operand}).
     *
     * @param operand the right-hand operand
     * @return the less-than expression
     */
    JExpr lt(JExpr operand);

    /**
     * Returns an expression representing the greater-than comparison of this expression and the given operand ({@code this > operand}).
     *
     * @param operand the right-hand operand
     * @return the greater-than expression
     */
    JExpr gt(JExpr operand);

    /**
     * Returns an expression representing the less-than-or-equal comparison of this expression and the given operand ({@code this <= operand}).
     *
     * @param operand the right-hand operand
     * @return the less-than-or-equal expression
     */
    JExpr le(JExpr operand);

    /**
     * Returns an expression representing the greater-than-or-equal comparison of this expression and the given operand ({@code this >= operand}).
     *
     * @param operand the right-hand operand
     * @return the greater-than-or-equal expression
     */
    JExpr ge(JExpr operand);

    // Logical operations

    /**
     * Returns an expression representing the logical AND of this expression and the given operand ({@code this && operand}).
     *
     * @param operand the right-hand operand
     * @return the logical AND expression
     */
    JExpr and(JExpr operand);

    /**
     * Returns an expression representing the logical OR of this expression and the given operand ({@code this || operand}).
     *
     * @param operand the right-hand operand
     * @return the logical OR expression
     */
    JExpr or(JExpr operand);

    /**
     * Returns an expression representing the logical negation of this expression ({@code !this}).
     *
     * @return the logical negation expression
     */
    JExpr not();

    // Cast

    /**
     * Returns an expression representing a type cast of this expression to the given type ({@code (type) this}).
     *
     * @param type the target type to cast to
     * @return the cast expression
     */
    JExpr cast(JType type);

    // Instanceof

    /**
     * Returns an expression representing a plain {@code instanceof} test ({@code this instanceof type}).
     *
     * @param type the type to test against
     * @return the {@code instanceof} expression
     */
    JExpr instanceof_(JType type);

    /**
     * Returns an expression representing a pattern-matching {@code instanceof} test with a binding variable
     * ({@code this instanceof type bindingVar}).
     * <p>
     * This form requires Java 16 or later.
     *
     * @param type the type to test against
     * @param bindingVar the name of the pattern binding variable
     * @return the pattern-matching {@code instanceof} expression
     */
    JExpr instanceof_(JType type, String bindingVar);

    // Ternary

    /**
     * Returns an expression representing the ternary conditional operator ({@code this ? ifTrue : ifFalse}).
     *
     * @param ifTrue the expression to evaluate if this expression is {@code true}
     * @param ifFalse the expression to evaluate if this expression is {@code false}
     * @return the ternary conditional expression
     */
    JExpr cond(JExpr ifTrue, JExpr ifFalse);

    // Grouping

    /**
     * Returns an expression representing this expression enclosed in parentheses ({@code (this)}).
     *
     * @return the parenthesized expression
     */
    JExpr paren();

    // Member access

    /**
     * Returns a variable expression representing an instance field access on this expression ({@code this.name}).
     *
     * @param name the field name
     * @return the field access variable expression
     */
    JVar field(String name);

    /**
     * Returns an expression representing an instance method call on this expression ({@code this.name(args...)}).
     *
     * @param name the method name
     * @param args the method arguments
     * @return the method call expression
     */
    JExpr call(String name, JExpr... args);

    /**
     * Returns an expression representing an instance method call on this expression ({@code this.name(args...)}).
     *
     * @param name the method name
     * @param args the method arguments as a list
     * @return the method call expression
     */
    JExpr call(String name, List<JExpr> args);

    // Array access

    /**
     * Returns a variable expression representing an array element access on this expression ({@code this[index]}).
     *
     * @param index the index expression
     * @return the array element access variable expression
     */
    JVar idx(JExpr index);

    // Increment and decrement

    /**
     * Returns an expression representing the postfix increment of this expression ({@code this++}).
     *
     * @return the postfix increment expression
     */
    JExpr postInc();

    /**
     * Returns an expression representing the postfix decrement of this expression ({@code this--}).
     *
     * @return the postfix decrement expression
     */
    JExpr postDec();

    /**
     * Returns an expression representing the prefix increment of this expression ({@code ++this}).
     *
     * @return the prefix increment expression
     */
    JExpr preInc();

    /**
     * Returns an expression representing the prefix decrement of this expression ({@code --this}).
     *
     * @return the prefix decrement expression
     */
    JExpr preDec();
}
