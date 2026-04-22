package org.jboss.jdeparser.impl;

import java.io.IOException;
import java.util.List;

import org.jboss.jdeparser.JExpr;
import org.jboss.jdeparser.JType;
import org.jboss.jdeparser.format.FormatPreferences;

/**
 * A lambda expression: {@code (params) -> body}.
 * <p>
 * Supports both expression-body lambdas ({@code x -> x + 1}) and
 * statement-body lambdas ({@code x -> &#123; return x + 1; &#125;}).
 * Parameters may be untyped ({@code x}) or typed ({@code int x}).
 * A single untyped parameter omits the parentheses.
 */
public final class LambdaJExpr extends AbstractJExpr {

    private final List<LambdaParam> params;
    private final JExpr exprBody;
    private final BlockCreatorImpl blockBody;

    /**
     * Constructs a lambda expression with an expression body.
     *
     * @param params   the lambda parameters
     * @param exprBody the expression body
     */
    public LambdaJExpr(final List<LambdaParam> params, final JExpr exprBody) {
        this.params = List.copyOf(params);
        this.exprBody = exprBody;
        this.blockBody = null;
    }

    /**
     * Constructs a lambda expression with a block body.
     *
     * @param params    the lambda parameters
     * @param blockBody the block body creator
     */
    public LambdaJExpr(final List<LambdaParam> params, final BlockCreatorImpl blockBody) {
        this.params = List.copyOf(params);
        this.exprBody = null;
        this.blockBody = blockBody;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@link Prec#ASSIGNMENT} (lambda has very low precedence)
     */
    @Override
    public Prec precedence() {
        return Prec.ASSIGNMENT;
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
        // single untyped parameter: no parens needed
        if (params.size() == 1 && params.get(0).type() == null) {
            writer.writeName(params.get(0).name());
        } else {
            writer.write(Tokens.$PAREN.OPEN);
            boolean first = true;
            for (LambdaParam p : params) {
                if (!first) {
                    writer.write(Tokens.$PUNCT.COMMA);
                    writer.write(FormatPreferences.Space.AFTER_COMMA);
                }
                first = false;
                if (p.type() != null) {
                    writeType(writer, p.type());
                    writer.sp();
                }
                writer.writeName(p.name());
            }
            writer.write(Tokens.$PAREN.CLOSE);
        }
        writer.write(Tokens.$BINOP.ARROW);
        if (exprBody != null) {
            writeExpr(writer, exprBody);
        } else {
            writer.write(FormatPreferences.Space.BEFORE_BRACE_LAMBDA);
            blockBody.writeBlock(writer);
        }
    }

    /**
     * A lambda parameter with an optional type.
     *
     * @param name the parameter name
     * @param type the parameter type, or {@code null} for an inferred-type parameter
     */
    public record LambdaParam(String name, JType type) {

        /**
         * Creates an untyped lambda parameter.
         *
         * @param name the parameter name
         */
        public LambdaParam(final String name) {
            this(name, null);
        }
    }
}
