package org.jboss.jdeparser.impl;

import java.io.IOException;
import java.util.List;

import org.jboss.jdeparser.JExpr;
import org.jboss.jdeparser.format.FormatPreferences;

/**
 * A method call expression: {@code receiver.method(args)} or {@code method(args)}.
 * <p>
 * When the receiver is {@code null}, this represents an unqualified method call.
 */
public final class CallJExpr extends AbstractJExpr {

    private final JExpr receiver;
    private final String method;
    private final List<JExpr> args;

    /**
     * Constructs a new method call expression.
     *
     * @param receiver the receiver expression, or {@code null} for unqualified calls
     * @param method   the method name
     * @param args     the argument expressions
     */
    public CallJExpr(final JExpr receiver, final String method, final List<JExpr> args) {
        this.receiver = receiver;
        this.method = method;
        this.args = List.copyOf(args);
    }

    /**
     * Returns the receiver expression.
     *
     * @return the receiver, or {@code null} for unqualified calls
     */
    public JExpr receiver() {
        return receiver;
    }

    /**
     * Returns the method name.
     *
     * @return the method name
     */
    public String method() {
        return method;
    }

    /**
     * Returns the argument expressions.
     *
     * @return an unmodifiable list of arguments
     */
    public List<JExpr> args() {
        return args;
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
        // receiver.method(arg1, arg2) or method(arg1, arg2)
        if (receiver != null) {
            writeSubExpr(writer, receiver, Prec.POSTFIX, Assoc.LEFT, Assoc.LEFT);
            writer.write(Tokens.$PUNCT.DOT);
        }
        writer.writeName(method);
        writer.write(Tokens.$PAREN.OPEN);
        writeList(writer, args, FormatPreferences.Space.AFTER_COMMA,
            FormatPreferences.Wrapping.ARGUMENT_LIST);
        writer.write(Tokens.$PAREN.CLOSE);
    }
}
