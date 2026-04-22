package org.jboss.jdeparser.impl;

import java.io.IOException;
import java.util.List;

import org.jboss.jdeparser.JExpr;
import org.jboss.jdeparser.JType;
import org.jboss.jdeparser.JVar;

/**
 * Abstract base class for all expression nodes.
 * <p>
 * Provides default implementations for every operator method defined on {@link JExpr}.
 * Each method constructs the appropriate concrete expression node, preserving the
 * operator's precedence and associativity so that the formatting engine can later
 * emit the minimum necessary parentheses.
 */
public abstract non-sealed class AbstractJExpr implements JExpr, Writable {

    /**
     * Sole constructor for subclasses.
     */
    protected AbstractJExpr() {
    }

    /**
     * Writes this expression to the given source file writer.
     *
     * @param writer the writer to emit source code to
     * @throws IOException if an I/O error occurs
     */
    @Override
    public abstract void write(SourceFileWriter writer) throws IOException;

    /**
     * Writes a sub-expression, inserting parentheses if the sub-expression's
     * precedence requires it relative to the parent operator.
     * <p>
     * Parentheses are added when:
     * <ul>
     *   <li>The sub-expression has lower precedence than the parent</li>
     *   <li>The sub-expression has equal precedence but is on the
     *       non-associative side (e.g., right operand of a left-associative
     *       operator)</li>
     * </ul>
     *
     * @param writer     the source file writer
     * @param sub        the sub-expression to write
     * @param parentPrec the precedence of the enclosing operator
     * @param parentAssoc the associativity of the enclosing operator
     * @param side       which side of the parent this sub-expression is on
     * @throws IOException if an I/O error occurs
     */
    protected static void writeSubExpr(final SourceFileWriter writer, final JExpr sub,
                                       final Prec parentPrec, final Assoc parentAssoc,
                                       final Assoc side) throws IOException {
        final AbstractJExpr asub = (AbstractJExpr) sub;
        final boolean needParens = asub.precedence().ordinal() < parentPrec.ordinal()
            || (asub.precedence() == parentPrec && parentAssoc != side);
        if (needParens) {
            writer.write(Tokens.$PAREN.OPEN);
            asub.write(writer);
            writer.write(Tokens.$PAREN.CLOSE);
        } else {
            asub.write(writer);
        }
    }

    /**
     * Writes an expression, casting to Writable and invoking write().
     *
     * @param writer the source file writer
     * @param expr   the expression to write
     * @throws IOException if an I/O error occurs
     */
    protected static void writeExpr(final SourceFileWriter writer, final JExpr expr) throws IOException {
        ((AbstractJExpr) expr).write(writer);
    }

    /**
     * Writes a type, casting to Writable and invoking write().
     *
     * @param writer the source file writer
     * @param type   the type to write
     * @throws IOException if an I/O error occurs
     */
    protected static void writeType(final SourceFileWriter writer, final JType type) throws IOException {
        ((AbstractJType) type).write(writer);
    }

    // ── Precedence and associativity ──────────────────────────────────────

    /**
     * Returns the precedence level of this expression.
     *
     * @return the precedence (never {@code null})
     */
    public abstract Prec precedence();

    /**
     * Returns the associativity of this expression.
     *
     * @return the associativity (never {@code null})
     */
    public abstract Assoc associativity();

    // ── Arithmetic ───────────────────────────────────────────────────────

    /** {@inheritDoc} */
    @Override
    public JExpr add(final JExpr other) {
        return new BinaryJExpr(this, Tokens.$BINOP.PLUS, other, Prec.ADDITIVE, Assoc.LEFT);
    }

    /** {@inheritDoc} */
    @Override
    public JExpr sub(final JExpr other) {
        return new BinaryJExpr(this, Tokens.$BINOP.MINUS, other, Prec.ADDITIVE, Assoc.LEFT);
    }

    /** {@inheritDoc} */
    @Override
    public JExpr mul(final JExpr other) {
        return new BinaryJExpr(this, Tokens.$BINOP.TIMES, other, Prec.MULTIPLICATIVE, Assoc.LEFT);
    }

    /** {@inheritDoc} */
    @Override
    public JExpr div(final JExpr other) {
        return new BinaryJExpr(this, Tokens.$BINOP.DIV, other, Prec.MULTIPLICATIVE, Assoc.LEFT);
    }

    /** {@inheritDoc} */
    @Override
    public JExpr mod(final JExpr other) {
        return new BinaryJExpr(this, Tokens.$BINOP.MOD, other, Prec.MULTIPLICATIVE, Assoc.LEFT);
    }

    /** {@inheritDoc} */
    @Override
    public JExpr neg() {
        return new UnaryJExpr(Tokens.$UNOP.MINUS, this, true, Prec.UNARY);
    }

    /** {@inheritDoc} */
    @Override
    public JExpr pos() {
        return new UnaryJExpr(Tokens.$UNOP.PLUS, this, true, Prec.UNARY);
    }

    // ── Bitwise ──────────────────────────────────────────────────────────

    /** {@inheritDoc} */
    @Override
    public JExpr bitAnd(final JExpr other) {
        return new BinaryJExpr(this, Tokens.$BINOP.BIT_AND, other, Prec.BITWISE_AND, Assoc.LEFT);
    }

    /** {@inheritDoc} */
    @Override
    public JExpr bitOr(final JExpr other) {
        return new BinaryJExpr(this, Tokens.$BINOP.BIT_OR, other, Prec.BITWISE_OR, Assoc.LEFT);
    }

    /** {@inheritDoc} */
    @Override
    public JExpr bitXor(final JExpr other) {
        return new BinaryJExpr(this, Tokens.$BINOP.BIT_XOR, other, Prec.BITWISE_XOR, Assoc.LEFT);
    }

    /** {@inheritDoc} */
    @Override
    public JExpr comp() {
        return new UnaryJExpr(Tokens.$UNOP.COMP, this, true, Prec.UNARY);
    }

    // ── Shift ────────────────────────────────────────────────────────────

    /** {@inheritDoc} */
    @Override
    public JExpr shl(final JExpr other) {
        return new BinaryJExpr(this, Tokens.$BINOP.SHL, other, Prec.SHIFT, Assoc.LEFT);
    }

    /** {@inheritDoc} */
    @Override
    public JExpr shr(final JExpr other) {
        return new BinaryJExpr(this, Tokens.$BINOP.SHR, other, Prec.SHIFT, Assoc.LEFT);
    }

    /** {@inheritDoc} */
    @Override
    public JExpr ushr(final JExpr other) {
        return new BinaryJExpr(this, Tokens.$BINOP.USHR, other, Prec.SHIFT, Assoc.LEFT);
    }

    // ── Equality ─────────────────────────────────────────────────────────

    /** {@inheritDoc} */
    @Override
    public JExpr eq(final JExpr other) {
        return new BinaryJExpr(this, Tokens.$BINOP.EQ, other, Prec.EQUALITY, Assoc.NONE);
    }

    /** {@inheritDoc} */
    @Override
    public JExpr ne(final JExpr other) {
        return new BinaryJExpr(this, Tokens.$BINOP.NE, other, Prec.EQUALITY, Assoc.NONE);
    }

    // ── Relational ───────────────────────────────────────────────────────

    /** {@inheritDoc} */
    @Override
    public JExpr lt(final JExpr other) {
        return new BinaryJExpr(this, Tokens.$BINOP.LT, other, Prec.RELATIONAL, Assoc.NONE);
    }

    /** {@inheritDoc} */
    @Override
    public JExpr gt(final JExpr other) {
        return new BinaryJExpr(this, Tokens.$BINOP.GT, other, Prec.RELATIONAL, Assoc.NONE);
    }

    /** {@inheritDoc} */
    @Override
    public JExpr le(final JExpr other) {
        return new BinaryJExpr(this, Tokens.$BINOP.LE, other, Prec.RELATIONAL, Assoc.NONE);
    }

    /** {@inheritDoc} */
    @Override
    public JExpr ge(final JExpr other) {
        return new BinaryJExpr(this, Tokens.$BINOP.GE, other, Prec.RELATIONAL, Assoc.NONE);
    }

    // ── Logical ──────────────────────────────────────────────────────────

    /** {@inheritDoc} */
    @Override
    public JExpr and(final JExpr other) {
        return new BinaryJExpr(this, Tokens.$BINOP.LOGICAL_AND, other, Prec.LOGICAL_AND, Assoc.LEFT);
    }

    /** {@inheritDoc} */
    @Override
    public JExpr or(final JExpr other) {
        return new BinaryJExpr(this, Tokens.$BINOP.LOGICAL_OR, other, Prec.LOGICAL_OR, Assoc.LEFT);
    }

    /** {@inheritDoc} */
    @Override
    public JExpr not() {
        return new UnaryJExpr(Tokens.$UNOP.NOT, this, true, Prec.UNARY);
    }

    // ── Type operations ──────────────────────────────────────────────────

    /** {@inheritDoc} */
    @Override
    public JExpr cast(final JType type) {
        return new CastJExpr(type, this);
    }

    /** {@inheritDoc} */
    @Override
    public JExpr instanceof_(final JType type) {
        return new InstanceOfJExpr(this, type, null);
    }

    /** {@inheritDoc} */
    @Override
    public JExpr instanceof_(final JType type, final String bindingVar) {
        return new InstanceOfJExpr(this, type, bindingVar);
    }

    // ── Ternary ──────────────────────────────────────────────────────────

    /** {@inheritDoc} */
    @Override
    public JExpr cond(final JExpr ifTrue, final JExpr ifFalse) {
        return new CondJExpr(this, ifTrue, ifFalse);
    }

    // ── Grouping ─────────────────────────────────────────────────────────

    /** {@inheritDoc} */
    @Override
    public JExpr paren() {
        return new ParenJExpr(this);
    }

    // ── Member access ────────────────────────────────────────────────────

    /** {@inheritDoc} */
    @Override
    public JVar field(final String name) {
        return new FieldRefJExpr(this, name);
    }

    // ── Method call ──────────────────────────────────────────────────────

    /** {@inheritDoc} */
    @Override
    public JExpr call(final String name, final JExpr... args) {
        return call(name, List.of(args));
    }

    /** {@inheritDoc} */
    @Override
    public JExpr call(final String name, final List<JExpr> args) {
        return new CallJExpr(this, name, args);
    }

    // ── Array access ─────────────────────────────────────────────────────

    /** {@inheritDoc} */
    @Override
    public JVar idx(final JExpr index) {
        return new ArrayLookupJExpr(this, index);
    }

    // ── Increment / decrement ────────────────────────────────────────────

    /** {@inheritDoc} */
    @Override
    public JExpr postInc() {
        return new UnaryJExpr(Tokens.$UNOP.PP, this, false, Prec.POSTFIX);
    }

    /** {@inheritDoc} */
    @Override
    public JExpr postDec() {
        return new UnaryJExpr(Tokens.$UNOP.MM, this, false, Prec.POSTFIX);
    }

    /** {@inheritDoc} */
    @Override
    public JExpr preInc() {
        return new UnaryJExpr(Tokens.$UNOP.PP, this, true, Prec.UNARY);
    }

    /** {@inheritDoc} */
    @Override
    public JExpr preDec() {
        return new UnaryJExpr(Tokens.$UNOP.MM, this, true, Prec.UNARY);
    }
}
