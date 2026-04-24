package org.jboss.jdeparser.impl;

import java.io.IOException;
import java.util.List;

import org.jboss.jdeparser.JExpr;
import org.jboss.jdeparser.JType;
import org.jboss.jdeparser.format.FormatPreferences;

/**
 * An anonymous class creation expression: {@code new Type(args) &#123; body &#125;}.
 * <p>
 * The expression produces a new instance of an anonymous subclass of the given
 * type, with constructor arguments and a class body containing method overrides,
 * additional fields, or other member declarations.
 */
public final class AnonymousClassJExpr extends AbstractJExpr {

    private final JType type;
    private final List<JExpr> args;
    private final ClassCreatorImpl body;

    /**
     * Constructs a new anonymous class expression.
     *
     * @param type the type being extended or implemented
     * @param args the constructor argument expressions
     * @param body the anonymous class body creator
     */
    public AnonymousClassJExpr(final JType type, final List<JExpr> args, final ClassCreatorImpl body) {
        this.type = type;
        this.args = List.copyOf(args);
        this.body = body;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link Prec#UNARY}
     */
    @Override
    public Prec precedence() {
        return Prec.UNARY;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link Assoc#RIGHT}
     */
    @Override
    public Assoc associativity() {
        return Assoc.RIGHT;
    }

    /** {@inheritDoc} */
    @Override
    public void write(final SourceFileWriter writer) throws IOException {
        // new Type(args) { body }
        writer.write(Tokens.$KW.NEW);
        writeType(writer, type);
        writer.write(Tokens.$PAREN.OPEN);
        writeList(writer, args, FormatPreferences.Space.AFTER_COMMA,
            FormatPreferences.Wrapping.ARGUMENT_LIST);
        writer.write(Tokens.$PAREN.CLOSE);
        writer.write(FormatPreferences.Space.BEFORE_BRACE_CLASS);
        writer.write(Tokens.$BRACE.OPEN);
        BlockCreatorImpl initBlock;
        if (writer.getFormat().hasOption(FormatPreferences.Opt.COMPACT_INIT_ONLY_CLASS)
                && (initBlock = body.soleInitBlock()) != null) {
            // compact double-brace: {{ content }}
            initBlock.writeBlock(writer);
        } else if (body.hasMembers()) {
            writer.nl();
            writer.pushIndent(FormatPreferences.Indentation.MEMBERS_TOP_LEVEL);
            body.writeBody(writer);
            writer.popIndent(FormatPreferences.Indentation.MEMBERS_TOP_LEVEL);
        } else {
            writer.write(FormatPreferences.Space.WITHIN_BRACES_EMPTY);
        }
        writer.write(Tokens.$BRACE.CLOSE);
    }
}
