package org.jboss.jdeparser.impl;

import java.io.IOException;
import java.util.function.Consumer;

import org.jboss.jdeparser.JExpr;
import org.jboss.jdeparser.JType;
import org.jboss.jdeparser.JVar;
import org.jboss.jdeparser.SourceVersion;
import org.jboss.jdeparser.creator.BlockCreator;
import org.jboss.jdeparser.creator.ForCreator;
import org.jboss.jdeparser.format.FormatPreferences;

/**
 * Implementation of {@link ForCreator} that collects the for-loop components
 * (initializer, condition, update, body) and writes the complete statement.
 * <p>
 * Writes the form: {@code for (Type name = init; condition; update) &#123; body &#125;}.
 */
public final class ForCreatorImpl extends AbstractCreator implements ForCreator, Writable {

    /** The initializer variable type, or {@code null}. */
    private JType initType;

    /** The initializer variable name. */
    private String initName;

    /** The initializer expression. */
    private JExpr initExpr;

    /** The loop condition expression. */
    private JExpr condition;

    /** The loop update expression. */
    private JExpr update;

    /** The loop body. */
    private BlockCreatorImpl body;

    /**
     * Constructs a new for-loop creator.
     *
     * @param version the source version
     */
    public ForCreatorImpl(final SourceVersion version) {
        super(version);
    }

    /** {@inheritDoc} */
    @Override
    public JVar init(final JType type, final String name, final JExpr init) {
        checkActive();
        this.initType = type;
        this.initName = name;
        this.initExpr = init;
        return new NameJExpr(name);
    }

    /** {@inheritDoc} */
    @Override
    public void condition(final JExpr condition) {
        checkActive();
        this.condition = condition;
    }

    /** {@inheritDoc} */
    @Override
    public void update(final JExpr update) {
        checkActive();
        this.update = update;
    }

    /** {@inheritDoc} */
    @Override
    public void body(final Consumer<BlockCreator> body) {
        checkActive();
        final BlockCreatorImpl bc = new BlockCreatorImpl(version());
        nest(() -> body.accept(bc));
        bc.finish();
        this.body = bc;
    }

    /** {@inheritDoc} */
    @Override
    public void write(final SourceFileWriter writer) throws IOException {
        writer.write(Tokens.$KW.FOR);
        writer.write(FormatPreferences.Space.BEFORE_PAREN_FOR);
        writer.write(Tokens.$PAREN.OPEN);
        // init
        if (initType != null) {
            AbstractJExpr.writeType(writer, initType);
            writer.sp();
            writer.writeName(initName);
            writer.write(Tokens.$BINOP.ASSIGN);
            AbstractJExpr.writeExpr(writer, initExpr);
        }
        writer.write(Tokens.$PUNCT.SEMI);
        writer.write(FormatPreferences.Space.AFTER_SEMICOLON);
        // condition
        if (condition != null) {
            AbstractJExpr.writeExpr(writer, condition);
        }
        writer.write(Tokens.$PUNCT.SEMI);
        writer.write(FormatPreferences.Space.AFTER_SEMICOLON);
        // update
        if (update != null) {
            AbstractJExpr.writeExpr(writer, update);
        }
        writer.write(Tokens.$PAREN.CLOSE);
        writer.write(FormatPreferences.Space.BEFORE_BRACE_FOR);
        if (body != null) {
            body.writeBlock(writer);
        }
        writer.nl();
    }
}
