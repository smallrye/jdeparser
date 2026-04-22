package org.jboss.jdeparser.impl;

import java.io.IOException;
import java.util.List;

import org.jboss.jdeparser.JExpr;
import org.jboss.jdeparser.JType;
import org.jboss.jdeparser.format.FormatPreferences;

/**
 * A static method call on a type: {@code TypeName.method(args)}.
 */
public final class TypeCallJExpr extends AbstractJExpr {

    private final JType type;
    private final String method;
    private final List<JExpr> args;

    /**
     * Constructs a new static method call expression.
     *
     * @param type   the type on which the method is called
     * @param method the method name
     * @param args   the argument expressions
     */
    public TypeCallJExpr(final JType type, final String method, final List<JExpr> args) {
        this.type = type;
        this.method = method;
        this.args = List.copyOf(args);
    }

    /**
     * Returns the type on which the method is called.
     *
     * @return the target type
     */
    public JType type() {
        return type;
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
        // TypeName.method(arg1, arg2)
        writeType(writer, type);
        writer.write(Tokens.$PUNCT.DOT);
        writer.writeName(method);
        writer.write(Tokens.$PAREN.OPEN);
        boolean first = true;
        for (JExpr arg : args) {
            if (!first) {
                writer.write(Tokens.$PUNCT.COMMA);
                writer.write(FormatPreferences.Space.AFTER_COMMA);
            }
            first = false;
            writeExpr(writer, arg);
        }
        writer.write(Tokens.$PAREN.CLOSE);
    }
}
