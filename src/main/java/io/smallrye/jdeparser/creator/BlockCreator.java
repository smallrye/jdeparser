package io.smallrye.jdeparser.creator;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import io.smallrye.jdeparser.Expr;
import io.smallrye.jdeparser.Label;
import io.smallrye.jdeparser.Type;
import io.smallrye.jdeparser.Var;
import io.smallrye.jdeparser.impl.BlockCreatorImpl;

/**
 * A creator for building statements within a block (method body, if-body, loop body, etc.).
 * <p>
 * This is the core of the borrow-pattern API. A {@code BlockCreator} is valid
 * only during the callback in which it was provided; any attempt to use it after
 * the callback returns will throw {@link IllegalStateException}.
 */
public sealed interface BlockCreator permits BlockCreatorImpl {

    // ---- Statement-expressions ----

    /**
     * Adds a statement-expression to this block.
     * <p>
     * The expression must be a legal statement per JLS 14.8: method call,
     * object creation, assignment, or increment/decrement.
     *
     * @param expr the expression to emit as a statement
     * @throws IllegalArgumentException if the expression is not a valid statement expression
     */
    void emit(Expr expr);

    // ---- Declarations ----

    /**
     * Declares a local variable with an explicit type.
     *
     * @param type the variable type
     * @param name the variable name
     * @param init the initializer expression
     * @return the variable, for use in subsequent expressions
     */
    default Var var(Type type, String name, Expr init) {
        return var(type, name, init, lvc -> {
        });
    }

    /**
     * Declares a local variable with inferred type ({@code var}, Java 10+).
     *
     * @param name the variable name
     * @param init the initializer expression
     * @return the variable, for use in subsequent expressions
     */
    default Var var(String name, Expr init) {
        return var(name, init, lvc -> {
        });
    }

    /**
     * Declares a local variable with an explicit type, configured via callback.
     * <p>
     * The callback can add annotations and the {@code final} modifier.
     *
     * @param type the variable type
     * @param name the variable name
     * @param init the initializer expression
     * @param builder the callback to configure the variable declaration
     * @return the variable, for use in subsequent expressions
     */
    Var var(Type type, String name, Expr init, Consumer<LocalVarCreator> builder);

    /**
     * Declares a local variable with inferred type ({@code var}, Java 10+),
     * configured via callback.
     * <p>
     * The callback can add annotations and the {@code final} modifier.
     *
     * @param name the variable name
     * @param init the initializer expression
     * @param builder the callback to configure the variable declaration
     * @return the variable, for use in subsequent expressions
     */
    Var var(String name, Expr init, Consumer<LocalVarCreator> builder);

    // ---- Control flow ----

    /**
     * Adds an {@code if} statement.
     *
     * @param condition the condition expression
     * @param body the callback to define the if-body
     */
    void if_(Expr condition, Consumer<BlockCreator> body);

    /**
     * Adds an {@code if-else} statement.
     *
     * @param condition the condition expression
     * @param ifBody the callback for the if-body
     * @param elseBody the callback for the else-body
     */
    void ifElse(Expr condition, Consumer<BlockCreator> ifBody, Consumer<BlockCreator> elseBody);

    /**
     * Adds a {@code while} loop.
     *
     * @param condition the loop condition
     * @param body the callback for the loop body
     */
    void while_(Expr condition, Consumer<BlockCreator> body);

    /**
     * Adds a {@code do-while} loop.
     *
     * @param body the callback for the loop body
     * @param condition the loop condition
     */
    void doWhile(Consumer<BlockCreator> body, Expr condition);

    /**
     * Adds a traditional {@code for} loop.
     *
     * @param builder the callback to configure the for-loop
     */
    void for_(Consumer<ForCreator> builder);

    /**
     * Adds an enhanced {@code for-each} loop.
     *
     * @param type the loop variable type
     * @param name the loop variable name
     * @param iterable the iterable expression
     * @param body the callback for the loop body
     * @return the loop variable, for use within the body
     */
    Var forEach(Type type, String name, Expr iterable, Consumer<BlockCreator> body);

    /**
     * Adds a modern {@code switch} statement using arrow ({@code ->}) syntax
     * with no fall-through (Java 14+).
     * <p>
     * Arrow cases support multi-value cases, type pattern matching (Java 21+),
     * and {@code null} cases. For classic colon-style switch with fall-through,
     * use {@link #switchClassic(Expr, Consumer)}.
     *
     * @param selector the switch selector expression
     * @param builder the callback to configure the switch cases
     * @see #switchClassic(Expr, Consumer)
     */
    void switch_(Expr selector, Consumer<SwitchCreator> builder);

    /**
     * Adds a classic {@code switch} statement using colon ({@code :}) syntax
     * with fall-through semantics.
     * <p>
     * Classic switch supports only single-value constant cases. It does not
     * support {@code null} cases, multi-value cases, or type pattern matching.
     * For those features, use {@link #switch_(Expr, Consumer)}.
     *
     * @param selector the switch selector expression
     * @param builder the callback to configure the switch cases
     * @see #switch_(Expr, Consumer)
     */
    void switchClassic(Expr selector, Consumer<ClassicSwitchCreator> builder);

    /**
     * Adds a {@code try} statement.
     *
     * @param builder the callback to configure the try block
     */
    void try_(Consumer<TryCreator> builder);

    /**
     * Adds a {@code synchronized} block.
     *
     * @param monitor the monitor expression
     * @param body the callback for the synchronized body
     */
    void synchronized_(Expr monitor, Consumer<BlockCreator> body);

    /**
     * Adds a nested block.
     *
     * @param body the callback for the block body
     */
    void block(Consumer<BlockCreator> body);

    /**
     * Adds a labeled statement, providing the label to the callback so that it can be
     * referenced from within the labeled body (e.g. for {@code break label} or
     * {@code continue label} targeting the enclosing labeled statement).
     *
     * @param name the label name
     * @param body the callback for the labeled statement body, receiving the label as its first argument
     * @return the label, for use with {@link #break_(Label)} or {@link #continue_(Label)}
     */
    Label labeled(String name, BiConsumer<Label, BlockCreator> body);

    // ---- Jump statements ----

    /**
     * Adds a {@code return} statement with no value.
     */
    void return_();

    /**
     * Adds a {@code return} statement with a value.
     *
     * @param value the return value expression
     */
    void return_(Expr value);

    /**
     * Adds a {@code throw} statement.
     *
     * @param exception the exception expression
     */
    void throw_(Expr exception);

    /**
     * Adds an unlabeled {@code break} statement.
     */
    void break_();

    /**
     * Adds a labeled {@code break} statement.
     *
     * @param label the target label
     */
    void break_(Label label);

    /**
     * Adds an unlabeled {@code continue} statement.
     */
    void continue_();

    /**
     * Adds a labeled {@code continue} statement.
     *
     * @param label the target label
     */
    void continue_(Label label);

    /**
     * Adds an {@code assert} statement.
     *
     * @param condition the assertion condition
     */
    void assert_(Expr condition);

    /**
     * Adds an {@code assert} statement with a detail message.
     *
     * @param condition the assertion condition
     * @param message the detail message expression
     */
    void assert_(Expr condition, Expr message);

    /**
     * Adds a {@code yield} statement (in switch expression bodies, Java 14+).
     *
     * @param value the yield value expression
     */
    void yield_(Expr value);

    /**
     * Adds an empty statement ({@code ;}).
     */
    void empty();

    // ---- Constructor delegation ----

    /**
     * Adds a {@code this(...)} constructor delegation call.
     *
     * @param args the constructor arguments
     */
    default void callThis(Expr... args) {
        callThis(List.of(args));
    }

    /**
     * Adds a {@code this(...)} constructor delegation call.
     *
     * @param args the constructor arguments as a list
     */
    void callThis(List<Expr> args);

    /**
     * Adds a {@code super(...)} constructor delegation call.
     *
     * @param args the constructor arguments
     */
    default void callSuper(Expr... args) {
        callSuper(List.of(args));
    }

    /**
     * Adds a {@code super(...)} constructor delegation call.
     *
     * @param args the constructor arguments as a list
     */
    void callSuper(List<Expr> args);

    // ---- Local types ----

    /**
     * Defines a local class within this block.
     *
     * @param name the class name
     * @param builder the callback to define the class
     */
    void localClass(String name, Consumer<ClassCreator> builder);

    /**
     * Defines a local interface within this block (Java 16+).
     *
     * @param name the interface name
     * @param builder the callback to define the interface
     */
    void localInterface(String name, Consumer<InterfaceCreator> builder);

    // ---- Comments ----

    /**
     * Inserts a line comment.
     *
     * @param comment the comment text
     */
    void lineComment(String comment);

    /**
     * Inserts a block comment.
     *
     * @param comment the comment text
     */
    void blockComment(String comment);

    /**
     * Inserts a blank line.
     */
    void blankLine();
}
