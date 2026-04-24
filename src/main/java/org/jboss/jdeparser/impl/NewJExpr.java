package org.jboss.jdeparser.impl;

import java.io.IOException;
import java.util.List;

import org.jboss.jdeparser.JExpr;
import org.jboss.jdeparser.JType;
import org.jboss.jdeparser.format.FormatPreferences;

/**
 * A constructor call expression: {@code new Type(args)}.
 */
public final class NewJExpr extends AbstractJExpr {

    private final JType type;
    private final List<JExpr> args;

    /**
     * Constructs a new constructor call expression.
     *
     * @param type the type being instantiated
     * @param args the constructor argument expressions
     */
    public NewJExpr(final JType type, final List<JExpr> args) {
        this.type = type;
        this.args = List.copyOf(args);
    }

    /**
     * Returns the type being instantiated.
     *
     * @return the type
     */
    public JType type() {
        return type;
    }

    /**
     * Returns the constructor argument expressions.
     *
     * @return an unmodifiable list of arguments
     */
    public List<JExpr> args() {
        return args;
    }

    /** {@inheritDoc} */
    @Override
    public Prec precedence() {
        return Prec.UNARY;
    }

    /** {@inheritDoc} */
    @Override
    public Assoc associativity() {
        return Assoc.RIGHT;
    }

    /** {@inheritDoc} */
    @Override
    public void write(final SourceFileWriter writer) throws IOException {
        // new Type(arg1, arg2)
        writer.write(Tokens.$KW.NEW);
        writeType(writer, type);
        writer.write(Tokens.$PAREN.OPEN);
        writeList(writer, args, FormatPreferences.Space.AFTER_COMMA,
            FormatPreferences.Wrapping.ARGUMENT_LIST);
        writer.write(Tokens.$PAREN.CLOSE);
    }
}
