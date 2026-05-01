package io.smallrye.jdeparser.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import io.smallrye.common.constraint.Assert;
import io.smallrye.jdeparser.Expr;
import io.smallrye.jdeparser.SourceVersion;
import io.smallrye.jdeparser.creator.BlockCreator;
import io.smallrye.jdeparser.creator.ClassicSwitchCreator;
import io.smallrye.jdeparser.format.FormatPreferences;

/**
 * Implementation of {@link ClassicSwitchCreator} that renders switch cases
 * using colon ({@code :}) syntax with fall-through semantics.
 * <p>
 * Classic switch enforces the following restrictions:
 * <ul>
 * <li>No {@code null} cases (use modern switch for null support)</li>
 * <li>Single constant value per case (no multi-value cases)</li>
 * <li>No type pattern matching</li>
 * </ul>
 */
public final class ClassicSwitchCreatorImpl extends AbstractCreator implements ClassicSwitchCreator {

    /** The collected case entries. */
    private final List<Writable> cases = new ArrayList<>();

    /**
     * Constructs a new classic switch creator.
     *
     * @param version the source version
     */
    public ClassicSwitchCreatorImpl(final SourceVersion version) {
        super(version);
    }

    /** {@inheritDoc} */
    @Override
    public void case_(final Expr value, final Consumer<BlockCreator> body) {
        checkActive();
        Assert.checkNotNullParam("value", value);
        if (value == Expr.NULL) {
            throw new IllegalArgumentException(
                    "Classic switch does not support null cases; use switch_() for enhanced switch");
        }
        Assert.checkNotNullParam("body", body);
        final BlockCreatorImpl block = new BlockCreatorImpl(version());
        block.sourceFile(sourceFile());
        nest(() -> body.accept(block));
        block.finish();
        cases.add(w -> {
            w.write(Tokens.$KW.CASE);
            AbstractExpr.writeExpr(w, value);
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
        Assert.checkNotNullParam("body", body);
        final BlockCreatorImpl block = new BlockCreatorImpl(version());
        block.sourceFile(sourceFile());
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
