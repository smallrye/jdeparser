package io.smallrye.jdeparser.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import io.smallrye.common.constraint.Assert;
import io.smallrye.jdeparser.Expr;
import io.smallrye.jdeparser.Label;
import io.smallrye.jdeparser.LanguageFeature;
import io.smallrye.jdeparser.SourceVersion;
import io.smallrye.jdeparser.Type;
import io.smallrye.jdeparser.Var;
import io.smallrye.jdeparser.creator.BlockCreator;
import io.smallrye.jdeparser.creator.ClassCreator;
import io.smallrye.jdeparser.creator.ClassicSwitchCreator;
import io.smallrye.jdeparser.creator.ForCreator;
import io.smallrye.jdeparser.creator.InterfaceCreator;
import io.smallrye.jdeparser.creator.LocalVarCreator;
import io.smallrye.jdeparser.creator.SwitchCreator;
import io.smallrye.jdeparser.creator.TryCreator;
import io.smallrye.jdeparser.format.FormatPreferences;

/**
 * Implementation of {@link BlockCreator} that accumulates statements as
 * {@link Writable} content nodes and writes them as a brace-delimited block.
 * <p>
 * Each statement-producing method creates a {@link Writable} lambda or
 * content object and adds it to the internal content list. When written,
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
        if (content.isEmpty()) {
            writer.write(FormatPreferences.Space.WITHIN_BRACES_EMPTY);
        } else {
            writer.write(FormatPreferences.Space.WITHIN_BRACES_CODE);
            writer.pushIndent(FormatPreferences.Indentation.LINE);
            for (Writable item : content) {
                item.write(writer);
            }
            writer.popIndent(FormatPreferences.Indentation.LINE);
        }
        writer.write(Tokens.$BRACE.CLOSE);
    }

    /**
     * Writes this block as a statement body, using braces or a single indented
     * statement depending on the {@link FormatPreferences.Opt#SINGLE_STATEMENT_BRACES} option.
     * <p>
     * When the option is enabled (or the block has zero or more than one statement),
     * the block is rendered with braces as usual. When the option is disabled and
     * the block has exactly one statement, the single statement is rendered without
     * braces on an indented continuation line.
     * <p>
     * This method should only be called from statement positions where a single
     * statement is syntactically valid without braces (e.g., {@code if}, {@code while},
     * {@code for}).
     *
     * @param writer the writer
     * @param beforeBrace the space to write before the opening brace (used only when braces are rendered)
     * @throws IOException if an I/O error occurs
     */
    public void writeStatementBody(final SourceFileWriter writer,
            final FormatPreferences.Space beforeBrace) throws IOException {
        if (content.size() == 1
                && !writer.getFormat().hasOption(FormatPreferences.Opt.SINGLE_STATEMENT_BRACES)) {
            writer.nl();
            writer.pushIndent(FormatPreferences.Indentation.LINE);
            content.get(0).write(writer);
            writer.popIndent(FormatPreferences.Indentation.LINE);
        } else {
            writer.write(beforeBrace);
            writeBlock(writer);
        }
    }

    // ── Statement-expressions ──────────────────────────────────────────

    /** {@inheritDoc} */
    @Override
    public void emit(final Expr expr) {
        checkActive();
        Assert.checkNotNullParam("expr", expr);
        content.add(w -> {
            AbstractExpr.writeExpr(w, expr);
            w.write(Tokens.$PUNCT.SEMI);
            w.nl();
        });
    }

    // ── Declarations ───────────────────────────────────────────────────

    /** {@inheritDoc} */
    @Override
    public Var var(final Type type, final String name, final Expr init,
            final Consumer<LocalVarCreator> builder) {
        checkActive();
        Assert.checkNotNullParam("type", type);
        Assert.checkNotNullParam("name", name);
        Assert.checkNotEmptyParam("name", name);
        Assert.checkNotNullParam("init", init);
        Assert.checkNotNullParam("builder", builder);
        registerUsedType(type);
        final LocalVarCreatorImpl lvc = new LocalVarCreatorImpl(version(), name, type, init);
        lvc.sourceFile(sourceFile());
        nest(() -> builder.accept(lvc));
        lvc.finish();
        content.add(lvc);
        return new NamedVar(name);
    }

    /** {@inheritDoc} */
    @Override
    public Var var(final String name, final Expr init,
            final Consumer<LocalVarCreator> builder) {
        checkActive();
        Assert.checkNotNullParam("name", name);
        Assert.checkNotEmptyParam("name", name);
        Assert.checkNotNullParam("init", init);
        Assert.checkNotNullParam("builder", builder);
        version().require(LanguageFeature.VAR_LOCAL_VARIABLE);
        final LocalVarCreatorImpl lvc = new LocalVarCreatorImpl(version(), name, null, init);
        lvc.sourceFile(sourceFile());
        nest(() -> builder.accept(lvc));
        lvc.finish();
        content.add(lvc);
        return new NamedVar(name);
    }

    // ── Control flow ───────────────────────────────────────────────────

    /** {@inheritDoc} */
    @Override
    public void if_(final Expr condition, final Consumer<BlockCreator> body) {
        checkActive();
        Assert.checkNotNullParam("condition", condition);
        Assert.checkNotNullParam("body", body);
        final BlockCreatorImpl block = new BlockCreatorImpl(version());
        block.sourceFile(sourceFile());
        nest(() -> body.accept(block));
        block.finish();
        content.add(w -> {
            w.write(Tokens.$KW.IF);
            w.write(FormatPreferences.Space.BEFORE_PAREN_IF);
            w.write(Tokens.$PAREN.OPEN);
            w.write(FormatPreferences.Space.WITHIN_PAREN_IF);
            AbstractExpr.writeExpr(w, condition);
            w.write(FormatPreferences.Space.WITHIN_PAREN_IF);
            w.write(Tokens.$PAREN.CLOSE);
            block.writeStatementBody(w, FormatPreferences.Space.BEFORE_BRACE_IF);
            w.nl();
        });
    }

    /** {@inheritDoc} */
    @Override
    public void ifElse(final Expr condition, final Consumer<BlockCreator> ifBody,
            final Consumer<BlockCreator> elseBody) {
        checkActive();
        Assert.checkNotNullParam("condition", condition);
        Assert.checkNotNullParam("ifBody", ifBody);
        Assert.checkNotNullParam("elseBody", elseBody);
        final BlockCreatorImpl ifBlock = new BlockCreatorImpl(version());
        ifBlock.sourceFile(sourceFile());
        nest(() -> ifBody.accept(ifBlock));
        ifBlock.finish();
        final BlockCreatorImpl elseBlock = new BlockCreatorImpl(version());
        elseBlock.sourceFile(sourceFile());
        nest(() -> elseBody.accept(elseBlock));
        elseBlock.finish();
        content.add(w -> {
            w.write(Tokens.$KW.IF);
            w.write(FormatPreferences.Space.BEFORE_PAREN_IF);
            w.write(Tokens.$PAREN.OPEN);
            w.write(FormatPreferences.Space.WITHIN_PAREN_IF);
            AbstractExpr.writeExpr(w, condition);
            w.write(FormatPreferences.Space.WITHIN_PAREN_IF);
            w.write(Tokens.$PAREN.CLOSE);
            ifBlock.writeStatementBody(w, FormatPreferences.Space.BEFORE_BRACE_IF);
            w.write(Tokens.$KW.ELSE);
            elseBlock.writeStatementBody(w, FormatPreferences.Space.BEFORE_BRACE_ELSE);
            w.nl();
        });
    }

    /** {@inheritDoc} */
    @Override
    public void while_(final Expr condition, final Consumer<BlockCreator> body) {
        checkActive();
        Assert.checkNotNullParam("condition", condition);
        Assert.checkNotNullParam("body", body);
        final BlockCreatorImpl block = new BlockCreatorImpl(version());
        block.sourceFile(sourceFile());
        nest(() -> body.accept(block));
        block.finish();
        content.add(w -> {
            w.write(Tokens.$KW.WHILE);
            w.write(FormatPreferences.Space.BEFORE_PAREN_WHILE);
            w.write(Tokens.$PAREN.OPEN);
            w.write(FormatPreferences.Space.WITHIN_PAREN_WHILE);
            AbstractExpr.writeExpr(w, condition);
            w.write(FormatPreferences.Space.WITHIN_PAREN_WHILE);
            w.write(Tokens.$PAREN.CLOSE);
            block.writeStatementBody(w, FormatPreferences.Space.BEFORE_BRACE_WHILE);
            w.nl();
        });
    }

    /** {@inheritDoc} */
    @Override
    public void doWhile(final Consumer<BlockCreator> body, final Expr condition) {
        checkActive();
        Assert.checkNotNullParam("body", body);
        Assert.checkNotNullParam("condition", condition);
        final BlockCreatorImpl block = new BlockCreatorImpl(version());
        block.sourceFile(sourceFile());
        nest(() -> body.accept(block));
        block.finish();
        content.add(w -> {
            w.write(Tokens.$KW.DO);
            block.writeStatementBody(w, FormatPreferences.Space.BEFORE_BRACE_DO);
            w.write(Tokens.$KW.WHILE);
            w.write(FormatPreferences.Space.BEFORE_PAREN_WHILE);
            w.write(Tokens.$PAREN.OPEN);
            w.write(FormatPreferences.Space.WITHIN_PAREN_WHILE);
            AbstractExpr.writeExpr(w, condition);
            w.write(FormatPreferences.Space.WITHIN_PAREN_WHILE);
            w.write(Tokens.$PAREN.CLOSE);
            w.write(Tokens.$PUNCT.SEMI);
            w.nl();
        });
    }

    /** {@inheritDoc} */
    @Override
    public void for_(final Consumer<ForCreator> builder) {
        checkActive();
        Assert.checkNotNullParam("builder", builder);
        final ForCreatorImpl fc = new ForCreatorImpl(version());
        fc.sourceFile(sourceFile());
        nest(() -> builder.accept(fc));
        fc.finish();
        content.add(fc);
    }

    /** {@inheritDoc} */
    @Override
    public Var forEach(final Type type, final String name, final Expr iterable,
            final Consumer<BlockCreator> body) {
        checkActive();
        Assert.checkNotNullParam("type", type);
        Assert.checkNotNullParam("name", name);
        Assert.checkNotEmptyParam("name", name);
        Assert.checkNotNullParam("iterable", iterable);
        Assert.checkNotNullParam("body", body);
        registerUsedType(type);
        final NamedVar var = new NamedVar(name);
        final BlockCreatorImpl block = new BlockCreatorImpl(version());
        block.sourceFile(sourceFile());
        nest(() -> body.accept(block));
        block.finish();
        content.add(w -> {
            w.write(Tokens.$KW.FOR);
            w.write(FormatPreferences.Space.BEFORE_PAREN_FOR);
            w.write(Tokens.$PAREN.OPEN);
            w.write(FormatPreferences.Space.WITHIN_PAREN_FOR);
            AbstractExpr.writeType(w, type);
            w.sp();
            w.writeName(name);
            w.write(FormatPreferences.Space.BEFORE_COLON);
            w.write(Tokens.$PUNCT.COLON);
            w.write(FormatPreferences.Space.AFTER_COLON);
            AbstractExpr.writeExpr(w, iterable);
            w.write(FormatPreferences.Space.WITHIN_PAREN_FOR);
            w.write(Tokens.$PAREN.CLOSE);
            block.writeStatementBody(w, FormatPreferences.Space.BEFORE_BRACE_FOR);
            w.nl();
        });
        return var;
    }

    /** {@inheritDoc} */
    @Override
    public void switch_(final Expr selector, final Consumer<SwitchCreator> builder) {
        checkActive();
        Assert.checkNotNullParam("selector", selector);
        Assert.checkNotNullParam("builder", builder);
        version().require(LanguageFeature.SWITCH_ARROW_CASES);
        final SwitchCreatorImpl sc = new SwitchCreatorImpl(version());
        sc.sourceFile(sourceFile());
        nest(() -> builder.accept(sc));
        sc.finish();
        content.add(w -> {
            w.write(Tokens.$KW.SWITCH);
            w.write(FormatPreferences.Space.BEFORE_PAREN_SWITCH);
            w.write(Tokens.$PAREN.OPEN);
            w.write(FormatPreferences.Space.WITHIN_PAREN_SWITCH);
            AbstractExpr.writeExpr(w, selector);
            w.write(FormatPreferences.Space.WITHIN_PAREN_SWITCH);
            w.write(Tokens.$PAREN.CLOSE);
            w.write(FormatPreferences.Space.BEFORE_BRACE_SWITCH);
            sc.writeBlock(w);
            w.nl();
        });
    }

    /** {@inheritDoc} */
    @Override
    public void switchClassic(final Expr selector, final Consumer<ClassicSwitchCreator> builder) {
        checkActive();
        Assert.checkNotNullParam("selector", selector);
        Assert.checkNotNullParam("builder", builder);
        final ClassicSwitchCreatorImpl sc = new ClassicSwitchCreatorImpl(version());
        sc.sourceFile(sourceFile());
        nest(() -> builder.accept(sc));
        sc.finish();
        content.add(w -> {
            w.write(Tokens.$KW.SWITCH);
            w.write(FormatPreferences.Space.BEFORE_PAREN_SWITCH);
            w.write(Tokens.$PAREN.OPEN);
            w.write(FormatPreferences.Space.WITHIN_PAREN_SWITCH);
            AbstractExpr.writeExpr(w, selector);
            w.write(FormatPreferences.Space.WITHIN_PAREN_SWITCH);
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
        Assert.checkNotNullParam("builder", builder);
        final TryCreatorImpl tc = new TryCreatorImpl(version());
        tc.sourceFile(sourceFile());
        nest(() -> builder.accept(tc));
        tc.finish();
        content.add(tc);
    }

    /** {@inheritDoc} */
    @Override
    public void synchronized_(final Expr monitor, final Consumer<BlockCreator> body) {
        checkActive();
        Assert.checkNotNullParam("monitor", monitor);
        Assert.checkNotNullParam("body", body);
        final BlockCreatorImpl block = new BlockCreatorImpl(version());
        block.sourceFile(sourceFile());
        nest(() -> body.accept(block));
        block.finish();
        content.add(w -> {
            w.write(Tokens.$KW.SYNCHRONIZED);
            w.write(FormatPreferences.Space.BEFORE_PAREN_SYNCHRONIZED);
            w.write(Tokens.$PAREN.OPEN);
            w.write(FormatPreferences.Space.WITHIN_PAREN_SYNCHRONIZED);
            AbstractExpr.writeExpr(w, monitor);
            w.write(FormatPreferences.Space.WITHIN_PAREN_SYNCHRONIZED);
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
        Assert.checkNotNullParam("body", body);
        final BlockCreatorImpl block = new BlockCreatorImpl(version());
        block.sourceFile(sourceFile());
        nest(() -> body.accept(block));
        block.finish();
        content.add(w -> {
            block.writeBlock(w);
            w.nl();
        });
    }

    /** {@inheritDoc} */
    @Override
    public Label labeled(final String name, final BiConsumer<Label, BlockCreator> body) {
        checkActive();
        Assert.checkNotNullParam("name", name);
        Assert.checkNotEmptyParam("name", name);
        Assert.checkNotNullParam("body", body);
        final Label label = new LabelImpl(name);
        final BlockCreatorImpl block = new BlockCreatorImpl(version());
        block.sourceFile(sourceFile());
        nest(() -> body.accept(label, block));
        block.finish();
        content.add(w -> {
            w.pushIndent(FormatPreferences.Indentation.LABELS);
            w.writeName(name);
            w.write(Tokens.$PUNCT.COLON);
            w.write(FormatPreferences.Space.AFTER_LABEL);
            w.popIndent(FormatPreferences.Indentation.LABELS);
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
        content.add(new ReturnWritable(null));
    }

    /** {@inheritDoc} */
    @Override
    public void return_(final Expr value) {
        checkActive();
        Assert.checkNotNullParam("value", value);
        content.add(new ReturnWritable(value));
    }

    /**
     * Returns the expression from a single {@code return expr;} statement,
     * if the block body consists of exactly that, or {@code null} otherwise.
     * <p>
     * This is used by {@link LambdaExpr} to determine whether a block-body
     * lambda can be rendered in expression form.
     *
     * @return the single return expression, or {@code null}
     */
    Expr singleReturnExpr() {
        return content.size() == 1
                && content.get(0) instanceof ReturnWritable r
                && r.value() != null
                        ? r.value()
                        : null;
    }

    /**
     * A writable representing a {@code return} statement, optionally with a value.
     *
     * @param value the return value expression, or {@code null} for a bare {@code return;}
     */
    record ReturnWritable(Expr value) implements Writable {

        /** {@inheritDoc} */
        @Override
        public void write(final SourceFileWriter writer) throws IOException {
            writer.write(Tokens.$KW.RETURN);
            if (value != null) {
                writer.addWordSpace();
                AbstractExpr.writeExpr(writer, value);
            }
            writer.write(Tokens.$PUNCT.SEMI);
            writer.nl();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void throw_(final Expr exception) {
        checkActive();
        Assert.checkNotNullParam("exception", exception);
        content.add(w -> {
            w.write(Tokens.$KW.THROW);
            w.addWordSpace();
            AbstractExpr.writeExpr(w, exception);
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
    public void break_(final Label label) {
        checkActive();
        Assert.checkNotNullParam("label", label);
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
    public void continue_(final Label label) {
        checkActive();
        Assert.checkNotNullParam("label", label);
        content.add(w -> {
            w.write(Tokens.$KW.CONTINUE);
            w.writeName(label.name());
            w.write(Tokens.$PUNCT.SEMI);
            w.nl();
        });
    }

    /** {@inheritDoc} */
    @Override
    public void assert_(final Expr condition) {
        checkActive();
        Assert.checkNotNullParam("condition", condition);
        content.add(w -> {
            w.write(Tokens.$KW.ASSERT);
            w.addWordSpace();
            AbstractExpr.writeExpr(w, condition);
            w.write(Tokens.$PUNCT.SEMI);
            w.nl();
        });
    }

    /** {@inheritDoc} */
    @Override
    public void assert_(final Expr condition, final Expr message) {
        checkActive();
        Assert.checkNotNullParam("condition", condition);
        Assert.checkNotNullParam("message", message);
        content.add(w -> {
            w.write(Tokens.$KW.ASSERT);
            w.addWordSpace();
            AbstractExpr.writeExpr(w, condition);
            w.write(FormatPreferences.Space.BEFORE_COLON);
            w.write(Tokens.$PUNCT.COLON);
            w.write(FormatPreferences.Space.AFTER_COLON);
            AbstractExpr.writeExpr(w, message);
            w.write(Tokens.$PUNCT.SEMI);
            w.nl();
        });
    }

    /** {@inheritDoc} */
    @Override
    public void yield_(final Expr value) {
        checkActive();
        Assert.checkNotNullParam("value", value);
        version().require(LanguageFeature.SWITCH_EXPRESSIONS);
        content.add(new YieldWritable(value));
    }

    /**
     * Returns the expression from a single {@code yield expr;} statement,
     * if the block body consists of exactly that, or {@code null} otherwise.
     * <p>
     * This is used by {@link SwitchCreatorImpl} to determine whether an
     * arrow-style case body can strip the {@code yield} keyword.
     *
     * @return the single yield expression, or {@code null}
     */
    Expr singleYieldExpr() {
        return content.size() == 1
                && content.get(0) instanceof YieldWritable y
                        ? y.value()
                        : null;
    }

    /**
     * Returns the single content item if the block body consists of exactly
     * one statement, or {@code null} otherwise.
     * <p>
     * This is used by {@link SwitchCreatorImpl} to determine whether an
     * arrow-style case body can be rendered without braces.
     *
     * @return the single writable, or {@code null}
     */
    Writable singleStatement() {
        return content.size() == 1 ? content.get(0) : null;
    }

    /**
     * A writable representing a {@code yield} statement in a switch expression body.
     *
     * @param value the yield value expression (never {@code null})
     */
    record YieldWritable(Expr value) implements Writable {

        /** {@inheritDoc} */
        @Override
        public void write(final SourceFileWriter writer) throws IOException {
            writer.write(Tokens.$KW.YIELD);
            writer.addWordSpace();
            AbstractExpr.writeExpr(writer, value);
            writer.write(Tokens.$PUNCT.SEMI);
            writer.nl();
        }
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
    public void callThis(final List<Expr> args) {
        checkActive();
        Assert.checkNotNullParam("args", args);
        final List<Expr> argList = List.copyOf(args);
        content.add(w -> {
            w.write(Tokens.$KW.THIS);
            w.write(Tokens.$PAREN.OPEN);
            if (argList.isEmpty()) {
                w.write(FormatPreferences.Space.WITHIN_PAREN_METHOD_CALL_EMPTY);
            } else {
                w.write(FormatPreferences.Space.WITHIN_PAREN_METHOD_CALL);
                AbstractExpr.writeList(w, argList, FormatPreferences.Space.AFTER_COMMA,
                        FormatPreferences.Wrapping.ARGUMENT_LIST);
                w.write(FormatPreferences.Space.WITHIN_PAREN_METHOD_CALL);
            }
            w.write(Tokens.$PAREN.CLOSE);
            w.write(Tokens.$PUNCT.SEMI);
            w.nl();
        });
    }

    /** {@inheritDoc} */
    @Override
    public void callSuper(final List<Expr> args) {
        checkActive();
        Assert.checkNotNullParam("args", args);
        final List<Expr> argList = List.copyOf(args);
        content.add(w -> {
            w.write(Tokens.$KW.SUPER);
            w.write(Tokens.$PAREN.OPEN);
            if (argList.isEmpty()) {
                w.write(FormatPreferences.Space.WITHIN_PAREN_METHOD_CALL_EMPTY);
            } else {
                w.write(FormatPreferences.Space.WITHIN_PAREN_METHOD_CALL);
                AbstractExpr.writeList(w, argList, FormatPreferences.Space.AFTER_COMMA,
                        FormatPreferences.Wrapping.ARGUMENT_LIST);
                w.write(FormatPreferences.Space.WITHIN_PAREN_METHOD_CALL);
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
        Assert.checkNotNullParam("name", name);
        Assert.checkNotEmptyParam("name", name);
        Assert.checkNotNullParam("builder", builder);
        final ClassCreatorImpl cc = new ClassCreatorImpl(version(), name, false);
        cc.sourceFile(sourceFile());
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
        Assert.checkNotNullParam("name", name);
        Assert.checkNotEmptyParam("name", name);
        Assert.checkNotNullParam("builder", builder);
        version().require(LanguageFeature.LOCAL_INTERFACES);
        final InterfaceCreatorImpl ic = new InterfaceCreatorImpl(version(), name);
        ic.sourceFile(sourceFile());
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
        Assert.checkNotNullParam("comment", comment);
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
        Assert.checkNotNullParam("comment", comment);
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
        content.add(SourceFileWriter::nl);
    }
}
