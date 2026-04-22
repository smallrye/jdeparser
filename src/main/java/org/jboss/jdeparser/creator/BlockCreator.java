package org.jboss.jdeparser.creator;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.jboss.jdeparser.JExpr;
import org.jboss.jdeparser.JLabel;
import org.jboss.jdeparser.JType;
import org.jboss.jdeparser.JVar;
import org.jboss.jdeparser.impl.BlockCreatorImpl;

/**
 * A creator for building statements within a block (method body, if-body, loop body, etc.).
 * <p>
 * This is the core of the borrow-pattern API.  A {@code BlockCreator} is valid
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
    void emit(JExpr expr);

    // ---- Declarations ----

    /**
     * Declares a local variable with an explicit type.
     *
     * @param type the variable type
     * @param name the variable name
     * @param init the initializer expression
     * @return the variable, for use in subsequent expressions
     */
    JVar var(JType type, String name, JExpr init);

    /**
     * Declares a local variable with inferred type ({@code var}, Java 10+).
     *
     * @param name the variable name
     * @param init the initializer expression
     * @return the variable, for use in subsequent expressions
     */
    JVar var(String name, JExpr init);

    // ---- Control flow ----

    /**
     * Adds an {@code if} statement.
     *
     * @param condition the condition expression
     * @param body      the callback to define the if-body
     */
    void if_(JExpr condition, Consumer<BlockCreator> body);

    /**
     * Adds an {@code if-else} statement.
     *
     * @param condition the condition expression
     * @param ifBody    the callback for the if-body
     * @param elseBody  the callback for the else-body
     */
    void ifElse(JExpr condition, Consumer<BlockCreator> ifBody, Consumer<BlockCreator> elseBody);

    /**
     * Adds a {@code while} loop.
     *
     * @param condition the loop condition
     * @param body      the callback for the loop body
     */
    void while_(JExpr condition, Consumer<BlockCreator> body);

    /**
     * Adds a {@code do-while} loop.
     *
     * @param body      the callback for the loop body
     * @param condition the loop condition
     */
    void doWhile(Consumer<BlockCreator> body, JExpr condition);

    /**
     * Adds a traditional {@code for} loop.
     *
     * @param builder the callback to configure the for-loop
     */
    void for_(Consumer<ForCreator> builder);

    /**
     * Adds an enhanced {@code for-each} loop.
     *
     * @param type       the loop variable type
     * @param name       the loop variable name
     * @param iterable   the iterable expression
     * @param body       the callback for the loop body
     * @return the loop variable, for use within the body
     */
    JVar forEach(JType type, String name, JExpr iterable, Consumer<BlockCreator> body);

    /**
     * Adds a {@code switch} statement.
     *
     * @param selector the switch selector expression
     * @param builder  the callback to configure the switch cases
     */
    void switch_(JExpr selector, Consumer<SwitchCreator> builder);

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
     * @param body    the callback for the synchronized body
     */
    void synchronized_(JExpr monitor, Consumer<BlockCreator> body);

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
     * @return the label, for use with {@link #break_(JLabel)} or {@link #continue_(JLabel)}
     */
    JLabel labeled(String name, BiConsumer<JLabel, BlockCreator> body);

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
    void return_(JExpr value);

    /**
     * Adds a {@code throw} statement.
     *
     * @param exception the exception expression
     */
    void throw_(JExpr exception);

    /**
     * Adds an unlabeled {@code break} statement.
     */
    void break_();

    /**
     * Adds a labeled {@code break} statement.
     *
     * @param label the target label
     */
    void break_(JLabel label);

    /**
     * Adds an unlabeled {@code continue} statement.
     */
    void continue_();

    /**
     * Adds a labeled {@code continue} statement.
     *
     * @param label the target label
     */
    void continue_(JLabel label);

    /**
     * Adds an {@code assert} statement.
     *
     * @param condition the assertion condition
     */
    void assert_(JExpr condition);

    /**
     * Adds an {@code assert} statement with a detail message.
     *
     * @param condition the assertion condition
     * @param message   the detail message expression
     */
    void assert_(JExpr condition, JExpr message);

    /**
     * Adds a {@code yield} statement (in switch expression bodies, Java 14+).
     *
     * @param value the yield value expression
     */
    void yield_(JExpr value);

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
    void callThis(JExpr... args);

    /**
     * Adds a {@code super(...)} constructor delegation call.
     *
     * @param args the constructor arguments
     */
    void callSuper(JExpr... args);

    // ---- Local types ----

    /**
     * Defines a local class within this block.
     *
     * @param name    the class name
     * @param builder the callback to define the class
     */
    void localClass(String name, Consumer<ClassCreator> builder);

    /**
     * Defines a local interface within this block (Java 16+).
     *
     * @param name    the interface name
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
