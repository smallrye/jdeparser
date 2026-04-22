package org.jboss.jdeparser.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.jboss.jdeparser.JExpr;
import org.jboss.jdeparser.JType;
import org.jboss.jdeparser.SourceVersion;
import org.jboss.jdeparser.creator.BlockCreator;
import org.jboss.jdeparser.creator.TryCreator;
import org.jboss.jdeparser.format.FormatPreferences;

/**
 * Implementation of {@link TryCreator} that collects try-with-resources,
 * catch blocks, and an optional finally block.
 * <p>
 * Writes the form: {@code try ([resources]) &#123; body &#125;
 * catch (ExType name) &#123; ... &#125; finally &#123; ... &#125;}.
 */
public final class TryCreatorImpl extends AbstractCreator implements TryCreator, Writable {

    /** The try-with-resources resource declarations. */
    private final List<Resource> resources = new ArrayList<>();

    /** The try body. */
    private BlockCreatorImpl body;

    /** The catch clauses. */
    private final List<CatchClause> catches = new ArrayList<>();

    /** The optional finally body. */
    private BlockCreatorImpl finallyBody;

    /**
     * Constructs a new try creator.
     *
     * @param version the source version
     */
    public TryCreatorImpl(final SourceVersion version) {
        super(version);
    }

    /** {@inheritDoc} */
    @Override
    public void with(final JType type, final String name, final JExpr init) {
        checkActive();
        resources.add(new Resource(type, name, init));
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
    public void catch_(final JType exceptionType, final String name, final Consumer<BlockCreator> body) {
        checkActive();
        final BlockCreatorImpl bc = new BlockCreatorImpl(version());
        nest(() -> body.accept(bc));
        bc.finish();
        catches.add(new CatchClause(List.of(exceptionType), name, bc));
    }

    /** {@inheritDoc} */
    @Override
    public void catch_(final List<JType> exceptionTypes, final String name, final Consumer<BlockCreator> body) {
        checkActive();
        final BlockCreatorImpl bc = new BlockCreatorImpl(version());
        nest(() -> body.accept(bc));
        bc.finish();
        catches.add(new CatchClause(List.copyOf(exceptionTypes), name, bc));
    }

    /** {@inheritDoc} */
    @Override
    public void finally_(final Consumer<BlockCreator> body) {
        checkActive();
        final BlockCreatorImpl bc = new BlockCreatorImpl(version());
        nest(() -> body.accept(bc));
        bc.finish();
        this.finallyBody = bc;
    }

    /** {@inheritDoc} */
    @Override
    public void write(final SourceFileWriter writer) throws IOException {
        writer.write(Tokens.$KW.TRY);
        // resources
        if (!resources.isEmpty()) {
            writer.write(FormatPreferences.Space.BEFORE_PAREN_TRY);
            writer.write(Tokens.$PAREN.OPEN);
            boolean first = true;
            for (Resource r : resources) {
                if (!first) {
                    writer.write(Tokens.$PUNCT.SEMI);
                    writer.write(FormatPreferences.Space.AFTER_SEMICOLON);
                }
                first = false;
                AbstractJExpr.writeType(writer, r.type);
                writer.sp();
                writer.writeName(r.name);
                writer.write(Tokens.$BINOP.ASSIGN);
                AbstractJExpr.writeExpr(writer, r.init);
            }
            writer.write(Tokens.$PAREN.CLOSE);
        }
        // body
        writer.write(FormatPreferences.Space.BEFORE_BRACE_TRY);
        if (body != null) {
            body.writeBlock(writer);
        }
        // catches
        for (CatchClause c : catches) {
            writer.write(Tokens.$KW.CATCH);
            writer.write(FormatPreferences.Space.BEFORE_PAREN_CATCH);
            writer.write(Tokens.$PAREN.OPEN);
            boolean first = true;
            for (JType t : c.types) {
                if (!first) {
                    writer.write(FormatPreferences.Space.AROUND_MULTI_CATCH_OR);
                    writer.writeEscaped('|');
                    writer.write(FormatPreferences.Space.AROUND_MULTI_CATCH_OR);
                }
                first = false;
                AbstractJExpr.writeType(writer, t);
            }
            writer.sp();
            writer.writeName(c.name);
            writer.write(Tokens.$PAREN.CLOSE);
            writer.write(FormatPreferences.Space.BEFORE_BRACE_CATCH);
            c.body.writeBlock(writer);
        }
        // finally
        if (finallyBody != null) {
            writer.write(Tokens.$KW.FINALLY);
            writer.write(FormatPreferences.Space.BEFORE_BRACE_FINALLY);
            finallyBody.writeBlock(writer);
        }
        writer.nl();
    }

    /**
     * A try-with-resources resource declaration.
     *
     * @param type the resource type
     * @param name the resource variable name
     * @param init the initializer expression
     */
    private record Resource(JType type, String name, JExpr init) {
    }

    /**
     * A catch clause with exception types, variable name, and body.
     *
     * @param types the exception types (at least one; multiple for multi-catch)
     * @param name  the exception variable name
     * @param body  the catch body
     */
    private record CatchClause(List<JType> types, String name, BlockCreatorImpl body) {
    }
}
