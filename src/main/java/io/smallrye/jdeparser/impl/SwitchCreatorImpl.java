package io.smallrye.jdeparser.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import io.smallrye.common.constraint.Assert;
import io.smallrye.jdeparser.Expr;
import io.smallrye.jdeparser.SourceVersion;
import io.smallrye.jdeparser.Type;
import io.smallrye.jdeparser.creator.BlockCreator;
import io.smallrye.jdeparser.creator.CaseCreator;
import io.smallrye.jdeparser.creator.SwitchCreator;
import io.smallrye.jdeparser.format.FormatPreferences;

/**
 * Implementation of {@link SwitchCreator} that renders switch cases
 * using arrow ({@code ->}) syntax with no fall-through.
 * <p>
 * Arrow case bodies are rendered in one of three forms, controlled by the
 * {@link FormatPreferences.Opt#SWITCH_ARROW_ALWAYS_BLOCK_BODY} option:
 * <ul>
 * <li>Single yield expression (in switch expressions): {@code case X -> expr;}</li>
 * <li>Single statement: {@code case X -> stmt;}</li>
 * <li>Block (multiple statements or forced by format option):
 * {@code case X -> { stmts... }}</li>
 * </ul>
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
    public void case_(final List<Expr> values, final Consumer<BlockCreator> body) {
        checkActive();
        Assert.checkNotNullParam("values", values);
        Assert.checkNotEmptyParam("values", values);
        Assert.checkNotNullParam("body", body);
        final BlockCreatorImpl block = new BlockCreatorImpl(version());
        block.sourceFile(sourceFile());
        nest(() -> body.accept(block));
        block.finish();
        cases.add(w -> {
            w.write(Tokens.$KW.CASE);
            AbstractExpr.writeList(w, values, FormatPreferences.Space.AFTER_COMMA);
            writeArrowBody(w, block);
        });
    }

    /** {@inheritDoc} */
    @Override
    public void case_(final Type type, final String name, final Consumer<CaseCreator> builder) {
        checkActive();
        Assert.checkNotNullParam("type", type);
        Assert.checkNotNullParam("name", name);
        Assert.checkNotEmptyParam("name", name);
        Assert.checkNotNullParam("builder", builder);
        registerUsedType(type);
        final CaseCreatorImpl cc = new CaseCreatorImpl(version());
        cc.sourceFile(sourceFile());
        nest(() -> builder.accept(cc));
        cc.finish();
        if (cc.body() == null) {
            throw new IllegalStateException("Case body was not defined; body() must be called");
        }
        cases.add(w -> {
            w.write(Tokens.$KW.CASE);
            AbstractExpr.writeType(w, type);
            w.sp();
            w.writeName(name);
            if (cc.guard() != null) {
                w.write(Tokens.$KW.WHEN);
                AbstractExpr.writeExpr(w, cc.guard());
            }
            writeArrowBody(w, cc.body());
        });
    }

    /** {@inheritDoc} */
    @Override
    public void default_(final Consumer<BlockCreator> body) {
        checkActive();
        Assert.checkNotNullParam("body", body);
        final BlockCreatorImpl block = new BlockCreatorImpl(version());
        block.sourceFile(sourceFile());
        nest(() -> body.accept(block));
        block.finish();
        cases.add(w -> {
            w.write(Tokens.$KW.DEFAULT);
            writeArrowBody(w, block);
        });
    }

    /**
     * Writes an arrow case body, choosing between expression, single-statement,
     * and block forms based on the body content and format preferences.
     *
     * @param w the writer
     * @param block the case body block
     * @throws IOException if an I/O error occurs
     */
    private static void writeArrowBody(final SourceFileWriter w,
            final BlockCreatorImpl block) throws IOException {
        w.write(Tokens.$BINOP.ARROW);
        boolean forceBlock = w.getFormat().hasOption(
                FormatPreferences.Opt.SWITCH_ARROW_ALWAYS_BLOCK_BODY);
        if (!forceBlock) {
            // strip yield for switch expression single-yield bodies
            Expr yieldExpr = block.singleYieldExpr();
            if (yieldExpr != null) {
                AbstractExpr.writeExpr(w, yieldExpr);
                w.write(Tokens.$PUNCT.SEMI);
                w.nl();
                return;
            }
            // single statement rendered without braces
            Writable single = block.singleStatement();
            if (single != null) {
                single.write(w);
                return;
            }
        }
        // block form
        block.writeBlock(w);
        w.nl();
    }

    /**
     * Writes the switch block body (braces enclosing case labels).
     *
     * @param writer the writer
     * @throws IOException if an I/O error occurs
     */
    public void writeBlock(final SourceFileWriter writer) throws IOException {
        writer.write(Tokens.$BRACE.OPEN);
        if (cases.isEmpty()) {
            writer.write(FormatPreferences.Space.WITHIN_BRACES_EMPTY);
        } else {
            writer.nl();
            writer.pushIndent(FormatPreferences.Indentation.CASE_LABELS);
            for (Writable c : cases) {
                c.write(writer);
            }
            writer.popIndent(FormatPreferences.Indentation.CASE_LABELS);
        }
        writer.write(Tokens.$BRACE.CLOSE);
    }
}
