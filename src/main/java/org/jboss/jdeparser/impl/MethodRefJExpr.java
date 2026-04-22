package org.jboss.jdeparser.impl;

import java.io.IOException;

import org.jboss.jdeparser.JExpr;
import org.jboss.jdeparser.JType;

/**
 * A method reference expression: {@code expr::method} or {@code Type::method}.
 * <p>
 * Exactly one of {@link #receiver()} or {@link #type()} is non-null;
 * the other is always {@code null}.
 */
public final class MethodRefJExpr extends AbstractJExpr {

    private final JExpr receiver;
    private final JType type;
    private final String method;

    /**
     * Constructs a method reference on an expression receiver: {@code expr::method}.
     *
     * @param receiver the receiver expression
     * @param method   the method name (or {@code "new"} for constructor references)
     */
    public MethodRefJExpr(final JExpr receiver, final String method) {
        this.receiver = receiver;
        this.type = null;
        this.method = method;
    }

    /**
     * Constructs a method reference on a type: {@code Type::method}.
     *
     * @param type   the type
     * @param method the method name (or {@code "new"} for constructor references)
     */
    public MethodRefJExpr(final JType type, final String method) {
        this.receiver = null;
        this.type = type;
        this.method = method;
    }

    /**
     * Returns the receiver expression, if this is an expression-based method reference.
     *
     * @return the receiver, or {@code null} if this is a type-based reference
     */
    public JExpr receiver() {
        return receiver;
    }

    /**
     * Returns the type, if this is a type-based method reference.
     *
     * @return the type, or {@code null} if this is an expression-based reference
     */
    public JType type() {
        return type;
    }

    /**
     * Returns the referenced method name.
     *
     * @return the method name (or {@code "new"} for constructor references)
     */
    public String method() {
        return method;
    }

    /** {@inheritDoc} */
    @Override
    public Prec precedence() {
        return Prec.POSTFIX;
    }

    /** {@inheritDoc} */
    @Override
    public Assoc associativity() {
        return Assoc.LEFT;
    }

    /** {@inheritDoc} */
    @Override
    public void write(final SourceFileWriter writer) throws IOException {
        // expr::method or Type::method
        if (receiver != null) {
            writeExpr(writer, receiver);
        } else {
            writeType(writer, type);
        }
        writer.write(Tokens.$BINOP.DBL_COLON);
        if ("new".equals(method)) {
            writer.write(Tokens.$KW.NEW);
        } else {
            writer.writeName(method);
        }
    }
}
