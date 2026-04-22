package org.jboss.jdeparser.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.jboss.jdeparser.JExpr;
import org.jboss.jdeparser.JType;
import org.jboss.jdeparser.SourceVersion;
import org.jboss.jdeparser.creator.BlockCreator;
import org.jboss.jdeparser.creator.CaseCreator;
import org.jboss.jdeparser.creator.SwitchCreator;
import org.jboss.jdeparser.format.FormatPreferences;

/**
 * Implementation of {@link SwitchCreator} that collects switch cases
 * and writes the complete switch block.
 */
public final class SwitchCreatorImpl extends AbstractCreator implements SwitchCreator {

    /** The collected case entries. */
    private final List<Writable> cases = new ArrayList<>();

    /**
     * Constructs a new switch creator.
     *
     * @param version the source version
     */
    public SwitchCreatorImpl(final SourceVersion version) {
        super(version);
    }

    /** {@inheritDoc} */
    @Override
    public void case_(final JExpr value, final Consumer<BlockCreator> body) {
        checkActive();
        final BlockCreatorImpl block = new BlockCreatorImpl(version());
        nest(() -> body.accept(block));
        block.finish();
        cases.add(w -> {
            w.write(Tokens.$KW.CASE);
            AbstractJExpr.writeExpr(w, value);
            w.write(Tokens.$PUNCT.COLON);
            w.nl();
            w.pushIndent(FormatPreferences.Indentation.LINE);
            block.writeBlock(w);
            w.nl();
            w.popIndent(FormatPreferences.Indentation.LINE);
        });
    }

    /** {@inheritDoc} */
    @Override
    public void case_(final List<JExpr> values, final Consumer<BlockCreator> body) {
        checkActive();
        final BlockCreatorImpl block = new BlockCreatorImpl(version());
        nest(() -> body.accept(block));
        block.finish();
        cases.add(w -> {
            w.write(Tokens.$KW.CASE);
            boolean first = true;
            for (JExpr v : values) {
                if (!first) {
                    w.write(Tokens.$PUNCT.COMMA);
                    w.write(FormatPreferences.Space.AFTER_COMMA);
                }
                first = false;
                AbstractJExpr.writeExpr(w, v);
            }
            w.write(Tokens.$PUNCT.COLON);
            w.nl();
            w.pushIndent(FormatPreferences.Indentation.LINE);
            block.writeBlock(w);
            w.nl();
            w.popIndent(FormatPreferences.Indentation.LINE);
        });
    }

    /** {@inheritDoc} */
    @Override
    public void case_(final JType type, final String name, final Consumer<CaseCreator> builder) {
        checkActive();
        final CaseCreatorImpl cc = new CaseCreatorImpl(version());
        nest(() -> builder.accept(cc));
        cc.finish();
        cases.add(w -> {
            w.write(Tokens.$KW.CASE);
            AbstractJExpr.writeType(w, type);
            w.sp();
            w.writeName(name);
            if (cc.guard() != null) {
                w.write(Tokens.$KW.WHEN);
                AbstractJExpr.writeExpr(w, cc.guard());
            }
            w.write(Tokens.$PUNCT.COLON);
            w.nl();
            if (cc.body() != null) {
                w.pushIndent(FormatPreferences.Indentation.LINE);
                cc.body().writeBlock(w);
                w.nl();
                w.popIndent(FormatPreferences.Indentation.LINE);
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void caseNull(final Consumer<BlockCreator> body) {
        checkActive();
        final BlockCreatorImpl block = new BlockCreatorImpl(version());
        nest(() -> body.accept(block));
        block.finish();
        cases.add(w -> {
            w.write(Tokens.$KW.CASE);
            w.write(Tokens.$KW.NULL);
            w.write(Tokens.$PUNCT.COLON);
            w.nl();
            w.pushIndent(FormatPreferences.Indentation.LINE);
            block.writeBlock(w);
            w.nl();
            w.popIndent(FormatPreferences.Indentation.LINE);
        });
    }

    /** {@inheritDoc} */
    @Override
    public void default_(final Consumer<BlockCreator> body) {
        checkActive();
        final BlockCreatorImpl block = new BlockCreatorImpl(version());
        nest(() -> body.accept(block));
        block.finish();
        cases.add(w -> {
            w.write(Tokens.$KW.DEFAULT);
            w.write(Tokens.$PUNCT.COLON);
            w.nl();
            w.pushIndent(FormatPreferences.Indentation.LINE);
            block.writeBlock(w);
            w.nl();
            w.popIndent(FormatPreferences.Indentation.LINE);
        });
    }

    /**
     * Writes the switch block body (braces enclosing case labels).
     *
     * @param writer the writer
     * @throws IOException if an I/O error occurs
     */
    public void writeBlock(final SourceFileWriter writer) throws IOException {
        writer.write(Tokens.$BRACE.OPEN);
        writer.nl();
        writer.pushIndent(FormatPreferences.Indentation.CASE_LABELS);
        for (Writable c : cases) {
            c.write(writer);
        }
        writer.popIndent(FormatPreferences.Indentation.CASE_LABELS);
        writer.write(Tokens.$BRACE.CLOSE);
    }
}
