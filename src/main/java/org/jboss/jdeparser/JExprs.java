package org.jboss.jdeparser;

import java.util.List;
import java.util.function.Consumer;

import org.jboss.jdeparser.creator.BlockCreator;
import org.jboss.jdeparser.creator.ClassCreator;
import org.jboss.jdeparser.creator.SwitchCreator;
import org.jboss.jdeparser.impl.AnonymousClassJExpr;
import org.jboss.jdeparser.impl.ArrayInitJExpr;
import org.jboss.jdeparser.impl.BlockCreatorImpl;
import org.jboss.jdeparser.impl.CallJExpr;
import org.jboss.jdeparser.impl.CharJExpr;
import org.jboss.jdeparser.impl.ClassCreatorImpl;
import org.jboss.jdeparser.impl.DoubleJExpr;
import org.jboss.jdeparser.impl.FloatJExpr;
import org.jboss.jdeparser.impl.IntegerJExpr;
import org.jboss.jdeparser.impl.LambdaJExpr;
import org.jboss.jdeparser.impl.LongJExpr;
import org.jboss.jdeparser.impl.MethodRefJExpr;
import org.jboss.jdeparser.impl.NameJExpr;
import org.jboss.jdeparser.impl.NewArrayJExpr;
import org.jboss.jdeparser.impl.NewJExpr;
import org.jboss.jdeparser.impl.QualifiedThisJExpr;
import org.jboss.jdeparser.impl.StringJExpr;
import org.jboss.jdeparser.impl.SwitchCreatorImpl;
import org.jboss.jdeparser.impl.SwitchJExpr;
import org.jboss.jdeparser.impl.TextBlockJExpr;
import org.jboss.jdeparser.impl.TypeCallJExpr;

/**
 * Static factory methods for creating {@link JExpr} instances.
 * <p>
 * Factory methods that accept creator callbacks (anonymous classes, lambda bodies,
 * switch expressions) will be added once the corresponding creator interfaces
 * are defined.
 */
public final class JExprs {

    private JExprs() {
    }

    // ---- Numeric literals ----

    /**
     * Creates a decimal {@code int} literal.
     *
     * @param value the integer value
     * @return the literal expression
     */
    public static JExpr decimal(final int value) {
        return Integer.MIN_VALUE < value && value < 0 ? decimal(-value).neg() : switch (value) {
            case 0 -> IntegerJExpr.ZERO;
            case 1 -> IntegerJExpr.ONE;
            default -> new IntegerJExpr(value, 10);
        };
    }

    /**
     * Creates a decimal {@code long} literal.
     *
     * @param value the long value
     * @return the literal expression
     */
    public static JExpr decimal(final long value) {
        return Long.MIN_VALUE < value && value < 0 ? decimal(-value).neg() : new LongJExpr(value, 10);
    }

    /**
     * Creates a decimal {@code float} literal.
     *
     * @param value the float value
     * @return the literal expression
     */
    public static JExpr decimal(final float value) {
        if (Float.isInfinite(value) || Float.isNaN(value)) {
            throw new IllegalArgumentException("Floating point value cannot be represented as a literal");
        }
        return value < 0 ? decimal(-value).neg() : new FloatJExpr(value, false);
    }

    /**
     * Creates a decimal {@code double} literal.
     *
     * @param value the double value
     * @return the literal expression
     */
    public static JExpr decimal(final double value) {
        if (Double.isInfinite(value) || Double.isNaN(value)) {
            throw new IllegalArgumentException("Floating point value cannot be represented as a literal");
        }
        return value < 0 ? decimal(-value).neg() : new DoubleJExpr(value, false);
    }

    /**
     * Creates a hexadecimal {@code int} literal (e.g., {@code 0xFF}).
     * Note that hexadecimal literals are never negative (use with {@link JExpr#neg} to get a negative literal).
     *
     * @param value the integer value
     * @return the literal expression
     */
    public static JExpr hex(final int value) {
        return new IntegerJExpr(value, 16);
    }

    /**
     * Creates a hexadecimal {@code long} literal (e.g., {@code 0xFFL}).
     * Note that hexadecimal literals are never negative (use with {@link JExpr#neg} to get a negative literal).
     *
     * @param value the long value
     * @return the literal expression
     */
    public static JExpr hex(final long value) {
        return new LongJExpr(value, 16);
    }

    /**
     * Creates an octal {@code int} literal (e.g., {@code 077}).
     *
     * @param value the integer value
     * @return the literal expression
     */
    public static JExpr octal(final int value) {
        return Integer.MIN_VALUE < value && value < 0 ? octal(-value).neg() : new IntegerJExpr(value, 8);
    }

    /**
     * Creates an octal {@code long} literal (e.g., {@code 077L}).
     *
     * @param value the long value
     * @return the literal expression
     */
    public static JExpr octal(final long value) {
        return Long.MIN_VALUE < value && value < 0 ? octal(-value).neg() : new LongJExpr(value, 8);
    }

    /**
     * Creates a binary {@code int} literal (e.g., {@code 0b1010}).
     * Note that binary literals are never negative (use with {@link JExpr#neg} to get a negative literal).
     *
     * @param value the integer value
     * @return the literal expression
     */
    public static JExpr binary(final int value) {
        return new IntegerJExpr(value, 2);
    }

    /**
     * Creates a binary {@code long} literal (e.g., {@code 0b1010L}).
     * Note that binary literals are never negative (use with {@link JExpr#neg} to get a negative literal).
     *
     * @param value the long value
     * @return the literal expression
     */
    public static JExpr binary(final long value) {
        return new LongJExpr(value, 2);
    }

    /**
     * Creates a hexadecimal {@code float} literal (e.g., {@code 0x1.0p0f}).
     *
     * @param value the float value
     * @return the literal expression
     */
    public static JExpr hex(final float value) {
        if (Float.isNaN(value) || Float.isInfinite(value)) {
            throw new IllegalArgumentException("Floating point value cannot be represented as a literal");
        }
        return value < 0 ? hex(-value).neg() : new FloatJExpr(value, true);
    }

    /**
     * Creates a hexadecimal {@code double} literal (e.g., {@code 0x1.0p0}).
     *
     * @param value the double value
     * @return the literal expression
     */
    public static JExpr hex(final double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new IllegalArgumentException("Floating point value cannot be represented as a literal");
        }
        return value < 0 ? hex(-value).neg() : new DoubleJExpr(value, true);
    }

    // ---- Other literals ----

    /**
     * Creates a {@code String} literal.
     *
     * @param value the string value (will be properly escaped in the output)
     * @return the literal expression
     */
    public static JExpr str(final String value) {
        return new StringJExpr(value);
    }

    /**
     * Creates a text block literal (Java 15+).
     *
     * @param value the text block content
     * @return the literal expression
     */
    public static JExpr textBlock(final String value) {
        return new TextBlockJExpr(value);
    }

    /**
     * Creates a {@code char} literal from a Unicode code point.
     *
     * @param codePoint the Unicode code point
     * @return the literal expression
     */
    public static JExpr ch(final int codePoint) {
        return new CharJExpr(codePoint);
    }

    // ---- Variable references ----

    /**
     * Creates a variable expression referencing a local variable or parameter by name.
     *
     * @param name the variable name
     * @return the variable expression
     */
    public static JVar $v(final String name) {
        return new NameJExpr(name);
    }

    /**
     * Creates a qualified {@code this} expression: {@code Outer.this}.
     *
     * @param qualifier the enclosing type
     * @return the qualified this expression
     */
    public static JExpr qualifiedThis(final JType qualifier) {
        return new QualifiedThisJExpr(qualifier);
    }

    // ---- Method calls ----

    /**
     * Creates an unqualified method call: {@code method(args)}.
     *
     * @param name the method name
     * @param args the argument expressions
     * @return the call expression
     */
    public static JExpr call(final String name, final JExpr... args) {
        return new CallJExpr(null, name, List.of(args));
    }

    /**
     * Creates a static method call: {@code Type.method(args)}.
     *
     * @param type the type on which the method is called
     * @param name the method name
     * @param args the argument expressions
     * @return the call expression
     */
    public static JExpr callStatic(final JType type, final String name, final JExpr... args) {
        return new TypeCallJExpr(type, name, List.of(args));
    }

    // ---- Object creation ----

    /**
     * Creates a constructor call: {@code new Type(args)}.
     *
     * @param type the type to instantiate
     * @param args the constructor argument expressions
     * @return the new expression
     */
    public static JExpr new_(final JType type, final JExpr... args) {
        return new NewJExpr(type, List.of(args));
    }

    /**
     * Creates an array creation expression with dimension sizes: {@code new Type[n]}.
     *
     * @param type       the array element type
     * @param dimensions the dimension size expressions
     * @return the new array expression
     */
    public static JExpr newArray(final JType type, final JExpr... dimensions) {
        return new NewArrayJExpr(type, List.of(dimensions));
    }

    /**
     * Creates an array creation expression with an initializer: {@code new Type[] {e1, e2, ...}}.
     *
     * @param type     the array element type
     * @param elements the initializer element expressions
     * @return the array initializer expression
     */
    public static JExpr newArrayInit(final JType type, final JExpr... elements) {
        return new ArrayInitJExpr(type, List.of(elements));
    }

    /**
     * Creates an array creation expression with an initializer: {@code new Type[] {e1, e2, ...}}.
     *
     * @param type     the array element type
     * @param elements the initializer element expressions
     * @return the array initializer expression
     */
    public static JExpr newArrayInit(final JType type, final List<JExpr> elements) {
        return new ArrayInitJExpr(type, elements);
    }

    // ---- Method references ----

    /**
     * Creates a method reference on an expression: {@code expr::method}.
     *
     * @param receiver the receiver expression
     * @param name     the method name (or {@code "new"} for constructor references)
     * @return the method reference expression
     */
    public static JExpr methodRef(final JExpr receiver, final String name) {
        return new MethodRefJExpr(receiver, name);
    }

    /**
     * Creates a method reference on a type: {@code Type::method}.
     *
     * @param type the type
     * @param name the method name (or {@code "new"} for constructor references)
     * @return the method reference expression
     */
    public static JExpr methodRef(final JType type, final String name) {
        return new MethodRefJExpr(type, name);
    }

    // ---- Anonymous class creation ----

    /**
     * Creates an anonymous class creation expression: {@code new Type(args) &#123; body &#125;}.
     *
     * @param version the source version for feature validation
     * @param type    the type being extended or implemented
     * @param args    the constructor argument expressions
     * @param builder the anonymous class body builder
     * @return the anonymous class expression
     */
    public static JExpr new_(final SourceVersion version, final JType type, final List<JExpr> args,
                             final Consumer<ClassCreator> builder) {
        final ClassCreatorImpl cc = new ClassCreatorImpl(version, "", false);
        builder.accept(cc);
        cc.finish();
        return new AnonymousClassJExpr(type, args, cc);
    }

    // ---- Switch expression ----

    /**
     * Creates a switch expression (Java 14+): {@code switch (selector) &#123; cases &#125;}.
     *
     * @param version  the source version for feature validation
     * @param selector the selector expression
     * @param builder  the switch case builder
     * @return the switch expression
     */
    public static JExpr switchExpr(final SourceVersion version, final JExpr selector,
                                   final Consumer<SwitchCreator> builder) {
        version.require(LanguageFeature.SWITCH_EXPRESSIONS);
        final SwitchCreatorImpl sc = new SwitchCreatorImpl(version);
        builder.accept(sc);
        sc.finish();
        return new SwitchJExpr(selector, sc);
    }

    // ---- Lambda expressions ----

    /**
     * Creates an expression-body lambda with a single untyped parameter: {@code x -> expr}.
     *
     * @param param the parameter name
     * @param body  the expression body
     * @return the lambda expression
     */
    public static JExpr lambda(final String param, final JExpr body) {
        return new LambdaJExpr(List.of(new LambdaJExpr.LambdaParam(param)), body);
    }

    /**
     * Creates an expression-body lambda with multiple untyped parameters: {@code (x, y) -> expr}.
     *
     * @param params the parameter names
     * @param body   the expression body
     * @return the lambda expression
     */
    public static JExpr lambda(final List<String> params, final JExpr body) {
        return new LambdaJExpr(
            params.stream().map(LambdaJExpr.LambdaParam::new).toList(),
            body
        );
    }

    /**
     * Creates a block-body lambda with a single untyped parameter: {@code x -> &#123; stmts &#125;}.
     *
     * @param version the source version for feature validation
     * @param param   the parameter name
     * @param body    the block body builder
     * @return the lambda expression
     */
    public static JExpr lambda(final SourceVersion version, final String param,
                               final Consumer<BlockCreator> body) {
        final BlockCreatorImpl bc = new BlockCreatorImpl(version);
        body.accept(bc);
        bc.finish();
        return new LambdaJExpr(List.of(new LambdaJExpr.LambdaParam(param)), bc);
    }

    /**
     * Creates a block-body lambda with multiple untyped parameters: {@code (x, y) -> &#123; stmts &#125;}.
     *
     * @param version the source version for feature validation
     * @param params  the parameter names
     * @param body    the block body builder
     * @return the lambda expression
     */
    public static JExpr lambda(final SourceVersion version, final List<String> params,
                               final Consumer<BlockCreator> body) {
        final BlockCreatorImpl bc = new BlockCreatorImpl(version);
        body.accept(bc);
        bc.finish();
        return new LambdaJExpr(
            params.stream().map(LambdaJExpr.LambdaParam::new).toList(),
            bc
        );
    }

    /**
     * Creates an expression-body lambda with typed parameters: {@code (Type1 x, Type2 y) -> expr}.
     *
     * @param params the typed parameters (name-type pairs)
     * @param body   the expression body
     * @return the lambda expression
     */
    public static JExpr lambdaTyped(final List<LambdaJExpr.LambdaParam> params, final JExpr body) {
        return new LambdaJExpr(params, body);
    }

    /**
     * Creates a block-body lambda with typed parameters: {@code (Type1 x, Type2 y) -> &#123; stmts &#125;}.
     *
     * @param version the source version for feature validation
     * @param params  the typed parameters (name-type pairs)
     * @param body    the block body builder
     * @return the lambda expression
     */
    public static JExpr lambdaTyped(final SourceVersion version, final List<LambdaJExpr.LambdaParam> params,
                                    final Consumer<BlockCreator> body) {
        final BlockCreatorImpl bc = new BlockCreatorImpl(version);
        body.accept(bc);
        bc.finish();
        return new LambdaJExpr(params, bc);
    }
}
