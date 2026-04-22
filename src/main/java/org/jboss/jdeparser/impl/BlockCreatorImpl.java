package org.jboss.jdeparser.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.jboss.jdeparser.JExpr;
import org.jboss.jdeparser.JLabel;
import org.jboss.jdeparser.JType;
import org.jboss.jdeparser.JVar;
import org.jboss.jdeparser.LanguageFeature;
import org.jboss.jdeparser.SourceVersion;
import org.jboss.jdeparser.creator.BlockCreator;
import org.jboss.jdeparser.creator.ClassCreator;
import org.jboss.jdeparser.creator.ForCreator;
import org.jboss.jdeparser.creator.InterfaceCreator;
import org.jboss.jdeparser.creator.SwitchCreator;
import org.jboss.jdeparser.creator.TryCreator;
import org.jboss.jdeparser.format.FormatPreferences;

/**
 * Implementation of {@link BlockCreator} that accumulates statements as
 * {@link Writable} content nodes and writes them as a brace-delimited block.
 * <p>
 * Each statement-producing method creates a {@link Writable} lambda or
 * content object and adds it to the internal content list.  When written,
 * the block emits {@code &#123;}, indented content, and {@code &#125;}.
 */
public final class BlockCreatorImpl extends AbstractCreator implements BlockCreator {

    /** The accumulated block content (statements, declarations, comments). */
    private final List<Writable> content = new ArrayList<>();

    /**
     * Constructs a new block creator.
     *
     * @param version the source version
     */
    public BlockCreatorImpl(final SourceVersion version) {
        super(version);
    }

    /**
     * Writes this block as a brace-delimited statement block: {@code &#123; ... &#125;}.
     *
     * @param writer the writer
     * @throws IOException if an I/O error occurs
     */
    public void writeBlock(final SourceFileWriter writer) throws IOException {
        writer.write(Tokens.$BRACE.OPEN);
        writer.nl();
        writer.pushIndent(FormatPreferences.Indentation.LINE);
        for (Writable item : content) {
            item.write(writer);
        }
        writer.popIndent(FormatPreferences.Indentation.LINE);
        writer.write(Tokens.$BRACE.CLOSE);
    }

    // ── Statement-expressions ──────────────────────────────────────────

    /** {@inheritDoc} */
    @Override
    public void emit(final JExpr expr) {
        checkActive();
        content.add(w -> {
            AbstractJExpr.writeExpr(w, expr);
            w.write(Tokens.$PUNCT.SEMI);
            w.nl();
        });
    }

    // ── Declarations ───────────────────────────────────────────────────

    /** {@inheritDoc} */
    @Override
    public JVar var(final JType type, final String name, final JExpr init) {
        checkActive();
        final NameJExpr var = new NameJExpr(name);
        content.add(w -> {
            AbstractJExpr.writeType(w, type);
            w.sp();
            w.writeName(name);
            w.write(Tokens.$BINOP.ASSIGN);
            AbstractJExpr.writeExpr(w, init);
            w.write(Tokens.$PUNCT.SEMI);
            w.nl();
        });
        return var;
    }

    /** {@inheritDoc} */
    @Override
    public JVar var(final String name, final JExpr init) {
        checkActive();
        version().require(LanguageFeature.VAR_LOCAL_VARIABLE);
        final NameJExpr var = new NameJExpr(name);
        content.add(w -> {
            w.write(Tokens.$KW.VAR);
            w.sp();
            w.writeName(name);
            w.write(Tokens.$BINOP.ASSIGN);
            AbstractJExpr.writeExpr(w, init);
            w.write(Tokens.$PUNCT.SEMI);
            w.nl();
        });
        return var;
    }

    // ── Control flow ───────────────────────────────────────────────────

    /** {@inheritDoc} */
    @Override
    public void if_(final JExpr condition, final Consumer<BlockCreator> body) {
        checkActive();
        final BlockCreatorImpl block = new BlockCreatorImpl(version());
        nest(() -> body.accept(block));
        block.finish();
        content.add(w -> {
            w.write(Tokens.$KW.IF);
            w.write(FormatPreferences.Space.BEFORE_PAREN_IF);
            w.write(Tokens.$PAREN.OPEN);
            AbstractJExpr.writeExpr(w, condition);
            w.write(Tokens.$PAREN.CLOSE);
            w.write(FormatPreferences.Space.BEFORE_BRACE_IF);
            block.writeBlock(w);
            w.nl();
        });
    }

    /** {@inheritDoc} */
    @Override
    public void ifElse(final JExpr condition, final Consumer<BlockCreator> ifBody,
                        final Consumer<BlockCreator> elseBody) {
        checkActive();
        final BlockCreatorImpl ifBlock = new BlockCreatorImpl(version());
        nest(() -> ifBody.accept(ifBlock));
        ifBlock.finish();
        final BlockCreatorImpl elseBlock = new BlockCreatorImpl(version());
        nest(() -> elseBody.accept(elseBlock));
        elseBlock.finish();
        content.add(w -> {
            w.write(Tokens.$KW.IF);
            w.write(FormatPreferences.Space.BEFORE_PAREN_IF);
            w.write(Tokens.$PAREN.OPEN);
            AbstractJExpr.writeExpr(w, condition);
            w.write(Tokens.$PAREN.CLOSE);
            w.write(FormatPreferences.Space.BEFORE_BRACE_IF);
            ifBlock.writeBlock(w);
            w.write(Tokens.$KW.ELSE);
            w.write(FormatPreferences.Space.BEFORE_BRACE_ELSE);
            elseBlock.writeBlock(w);
            w.nl();
        });
    }

    /** {@inheritDoc} */
    @Override
    public void while_(final JExpr condition, final Consumer<BlockCreator> body) {
        checkActive();
        final BlockCreatorImpl block = new BlockCreatorImpl(version());
        nest(() -> body.accept(block));
        block.finish();
        content.add(w -> {
            w.write(Tokens.$KW.WHILE);
            w.write(FormatPreferences.Space.BEFORE_PAREN_WHILE);
            w.write(Tokens.$PAREN.OPEN);
            AbstractJExpr.writeExpr(w, condition);
            w.write(Tokens.$PAREN.CLOSE);
            w.write(FormatPreferences.Space.BEFORE_BRACE_WHILE);
            block.writeBlock(w);
            w.nl();
        });
    }

    /** {@inheritDoc} */
    @Override
    public void doWhile(final Consumer<BlockCreator> body, final JExpr condition) {
        checkActive();
        final BlockCreatorImpl block = new BlockCreatorImpl(version());
        nest(() -> body.accept(block));
        block.finish();
        content.add(w -> {
            w.write(Tokens.$KW.DO);
            w.write(FormatPreferences.Space.BEFORE_BRACE_DO);
            block.writeBlock(w);
            w.write(Tokens.$KW.WHILE);
            w.write(FormatPreferences.Space.BEFORE_PAREN_WHILE);
            w.write(Tokens.$PAREN.OPEN);
            AbstractJExpr.writeExpr(w, condition);
            w.write(Tokens.$PAREN.CLOSE);
            w.write(Tokens.$PUNCT.SEMI);
            w.nl();
        });
    }

    /** {@inheritDoc} */
    @Override
    public void for_(final Consumer<ForCreator> builder) {
        checkActive();
        final ForCreatorImpl fc = new ForCreatorImpl(version());
        nest(() -> builder.accept(fc));
        fc.finish();
        content.add(fc);
    }

    /** {@inheritDoc} */
    @Override
    public JVar forEach(final JType type, final String name, final JExpr iterable,
                         final Consumer<BlockCreator> body) {
        checkActive();
        final NameJExpr var = new NameJExpr(name);
        final BlockCreatorImpl block = new BlockCreatorImpl(version());
        nest(() -> body.accept(block));
        block.finish();
        content.add(w -> {
            w.write(Tokens.$KW.FOR);
            w.write(FormatPreferences.Space.BEFORE_PAREN_FOR);
            w.write(Tokens.$PAREN.OPEN);
            AbstractJExpr.writeType(w, type);
            w.sp();
            w.writeName(name);
            w.write(FormatPreferences.Space.BEFORE_COLON);
            w.write(Tokens.$PUNCT.COLON);
            w.write(FormatPreferences.Space.AFTER_COLON);
            AbstractJExpr.writeExpr(w, iterable);
            w.write(Tokens.$PAREN.CLOSE);
            w.write(FormatPreferences.Space.BEFORE_BRACE_FOR);
            block.writeBlock(w);
            w.nl();
        });
        return var;
    }

    /** {@inheritDoc} */
    @Override
    public void switch_(final JExpr selector, final Consumer<SwitchCreator> builder) {
        checkActive();
        final SwitchCreatorImpl sc = new SwitchCreatorImpl(version());
        nest(() -> builder.accept(sc));
        sc.finish();
        content.add(w -> {
            w.write(Tokens.$KW.SWITCH);
            w.write(FormatPreferences.Space.BEFORE_PAREN_SWITCH);
            w.write(Tokens.$PAREN.OPEN);
            AbstractJExpr.writeExpr(w, selector);
            w.write(Tokens.$PAREN.CLOSE);
            w.write(FormatPreferences.Space.BEFORE_BRACE_SWITCH);
            sc.writeBlock(w);
            w.nl();
        });
    }

    /** {@inheritDoc} */
    @Override
    public void try_(final Consumer<TryCreator> builder) {
        checkActive();
        final TryCreatorImpl tc = new TryCreatorImpl(version());
        nest(() -> builder.accept(tc));
        tc.finish();
        content.add(tc);
    }

    /** {@inheritDoc} */
    @Override
    public void synchronized_(final JExpr monitor, final Consumer<BlockCreator> body) {
        checkActive();
        final BlockCreatorImpl block = new BlockCreatorImpl(version());
        nest(() -> body.accept(block));
        block.finish();
        content.add(w -> {
            w.write(Tokens.$KW.SYNCHRONIZED);
            w.write(FormatPreferences.Space.BEFORE_PAREN_SYNCHRONIZED);
            w.write(Tokens.$PAREN.OPEN);
            AbstractJExpr.writeExpr(w, monitor);
            w.write(Tokens.$PAREN.CLOSE);
            w.write(FormatPreferences.Space.BEFORE_BRACE_SYNCHRONIZED);
            block.writeBlock(w);
            w.nl();
        });
    }

    /** {@inheritDoc} */
    @Override
    public void block(final Consumer<BlockCreator> body) {
        checkActive();
        final BlockCreatorImpl block = new BlockCreatorImpl(version());
        nest(() -> body.accept(block));
        block.finish();
        content.add(w -> {
            block.writeBlock(w);
            w.nl();
        });
    }

    /** {@inheritDoc} */
    @Override
    public JLabel labeled(final String name, final BiConsumer<JLabel, BlockCreator> body) {
        checkActive();
        final JLabel label = new JLabelImpl(name);
        final BlockCreatorImpl block = new BlockCreatorImpl(version());
        nest(() -> body.accept(label, block));
        block.finish();
        content.add(w -> {
            w.writeName(name);
            w.write(Tokens.$PUNCT.COLON);
            w.write(FormatPreferences.Space.AFTER_LABEL);
            block.writeBlock(w);
            w.nl();
        });
        return label;
    }

    // ── Jump statements ────────────────────────────────────────────────

    /** {@inheritDoc} */
    @Override
    public void return_() {
        checkActive();
        content.add(w -> {
            w.write(Tokens.$KW.RETURN);
            w.write(Tokens.$PUNCT.SEMI);
            w.nl();
        });
    }

    /** {@inheritDoc} */
    @Override
    public void return_(final JExpr value) {
        checkActive();
        content.add(w -> {
            w.write(Tokens.$KW.RETURN);
            AbstractJExpr.writeExpr(w, value);
            w.write(Tokens.$PUNCT.SEMI);
            w.nl();
        });
    }

    /** {@inheritDoc} */
    @Override
    public void throw_(final JExpr exception) {
        checkActive();
        content.add(w -> {
            w.write(Tokens.$KW.THROW);
            AbstractJExpr.writeExpr(w, exception);
            w.write(Tokens.$PUNCT.SEMI);
            w.nl();
        });
    }

    /** {@inheritDoc} */
    @Override
    public void break_() {
        checkActive();
        content.add(w -> {
            w.write(Tokens.$KW.BREAK);
            w.write(Tokens.$PUNCT.SEMI);
            w.nl();
        });
    }

    /** {@inheritDoc} */
    @Override
    public void break_(final JLabel label) {
        checkActive();
        content.add(w -> {
            w.write(Tokens.$KW.BREAK);
            w.writeName(label.name());
            w.write(Tokens.$PUNCT.SEMI);
            w.nl();
        });
    }

    /** {@inheritDoc} */
    @Override
    public void continue_() {
        checkActive();
        content.add(w -> {
            w.write(Tokens.$KW.CONTINUE);
            w.write(Tokens.$PUNCT.SEMI);
            w.nl();
        });
    }

    /** {@inheritDoc} */
    @Override
    public void continue_(final JLabel label) {
        checkActive();
        content.add(w -> {
            w.write(Tokens.$KW.CONTINUE);
            w.writeName(label.name());
            w.write(Tokens.$PUNCT.SEMI);
            w.nl();
        });
    }

    /** {@inheritDoc} */
    @Override
    public void assert_(final JExpr condition) {
        checkActive();
        content.add(w -> {
            w.write(Tokens.$KW.ASSERT);
            AbstractJExpr.writeExpr(w, condition);
            w.write(Tokens.$PUNCT.SEMI);
            w.nl();
        });
    }

    /** {@inheritDoc} */
    @Override
    public void assert_(final JExpr condition, final JExpr message) {
        checkActive();
        content.add(w -> {
            w.write(Tokens.$KW.ASSERT);
            AbstractJExpr.writeExpr(w, condition);
            w.write(FormatPreferences.Space.BEFORE_COLON);
            w.write(Tokens.$PUNCT.COLON);
            w.write(FormatPreferences.Space.AFTER_COLON);
            AbstractJExpr.writeExpr(w, message);
            w.write(Tokens.$PUNCT.SEMI);
            w.nl();
        });
    }

    /** {@inheritDoc} */
    @Override
    public void yield_(final JExpr value) {
        checkActive();
        version().require(LanguageFeature.SWITCH_EXPRESSIONS);
        content.add(w -> {
            w.write(Tokens.$KW.YIELD);
            AbstractJExpr.writeExpr(w, value);
            w.write(Tokens.$PUNCT.SEMI);
            w.nl();
        });
    }

    /** {@inheritDoc} */
    @Override
    public void empty() {
        checkActive();
        content.add(w -> {
            w.write(Tokens.$PUNCT.SEMI);
            w.nl();
        });
    }

    // ── Constructor delegation ─────────────────────────────────────────

    /** {@inheritDoc} */
    @Override
    public void callThis(final JExpr... args) {
        checkActive();
        final List<JExpr> argList = List.of(args);
        content.add(w -> {
            w.write(Tokens.$KW.THIS);
            w.write(Tokens.$PAREN.OPEN);
            boolean first = true;
            for (JExpr arg : argList) {
                if (!first) {
                    w.write(Tokens.$PUNCT.COMMA);
                    w.write(FormatPreferences.Space.AFTER_COMMA);
                }
                first = false;
                AbstractJExpr.writeExpr(w, arg);
            }
            w.write(Tokens.$PAREN.CLOSE);
            w.write(Tokens.$PUNCT.SEMI);
            w.nl();
        });
    }

    /** {@inheritDoc} */
    @Override
    public void callSuper(final JExpr... args) {
        checkActive();
        final List<JExpr> argList = List.of(args);
        content.add(w -> {
            w.write(Tokens.$KW.SUPER);
            w.write(Tokens.$PAREN.OPEN);
            boolean first = true;
            for (JExpr arg : argList) {
                if (!first) {
                    w.write(Tokens.$PUNCT.COMMA);
                    w.write(FormatPreferences.Space.AFTER_COMMA);
                }
                first = false;
                AbstractJExpr.writeExpr(w, arg);
            }
            w.write(Tokens.$PAREN.CLOSE);
            w.write(Tokens.$PUNCT.SEMI);
            w.nl();
        });
    }

    // ── Local types ────────────────────────────────────────────────────

    /** {@inheritDoc} */
    @Override
    public void localClass(final String name, final Consumer<ClassCreator> builder) {
        checkActive();
        final ClassCreatorImpl cc = new ClassCreatorImpl(version(), name, false);
        nest(() -> builder.accept(cc));
        cc.finish();
        content.add(w -> {
            cc.write(w);
            w.nl();
        });
    }

    /** {@inheritDoc} */
    @Override
    public void localInterface(final String name, final Consumer<InterfaceCreator> builder) {
        checkActive();
        version().require(LanguageFeature.LOCAL_INTERFACES);
        final InterfaceCreatorImpl ic = new InterfaceCreatorImpl(version(), name);
        nest(() -> builder.accept(ic));
        ic.finish();
        content.add(w -> {
            ic.write(w);
            w.nl();
        });
    }

    // ── Comments ───────────────────────────────────────────────────────

    /** {@inheritDoc} */
    @Override
    public void lineComment(final String comment) {
        checkActive();
        content.add(w -> {
            w.write(Tokens.$COMMENT_TOK.LINE);
            w.sp();
            w.writeUnescaped(comment);
            w.nl();
        });
    }

    /** {@inheritDoc} */
    @Override
    public void blockComment(final String comment) {
        checkActive();
        content.add(w -> {
            w.write(Tokens.$COMMENT_TOK.OPEN);
            w.sp();
            w.writeUnescaped(comment);
            w.sp();
            w.write(Tokens.$COMMENT_TOK.CLOSE);
            w.nl();
        });
    }

    /** {@inheritDoc} */
    @Override
    public void blankLine() {
        checkActive();
        content.add(w -> w.nl());
    }
}
