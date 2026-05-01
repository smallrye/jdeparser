package io.smallrye.jdeparser.test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.smallrye.jdeparser.Expr;
import io.smallrye.jdeparser.SourceVersion;
import io.smallrye.jdeparser.Sources;
import io.smallrye.jdeparser.Type;
import io.smallrye.jdeparser.Var;
import io.smallrye.jdeparser.format.FormatPreferences;

/**
 * Tests for expression generation: arithmetic, logical, comparisons,
 * method calls, field access, array access, instanceof, cast, ternary.
 */
class ExpressionTest extends AbstractGeneratingTestCase {

    /**
     * Verifies arithmetic expression precedence and parenthesization.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void arithmeticExpression() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Math", sf -> {
            sf.class_("Math", cc -> {
                cc.field("result", fc -> {
                    fc.type(Type.INT);
                    // (a + b) * c
                    fc.init(Expr.$v("a").add(Expr.$v("b")).paren().mul(Expr.$v("c")));
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "Math");
        assertTrue(source.contains("(a + b) * c"), "should contain parenthesized arithmetic");
    }

    /**
     * Verifies comparison and logical operators.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void logicalExpression() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Logic", sf -> {
            sf.class_("Logic", cc -> {
                cc.field("flag", fc -> {
                    fc.type(Type.BOOLEAN);
                    // x > 0 && y < 10
                    fc.init(Expr.$v("x").gt(Expr.ZERO).and(Expr.$v("y").lt(Expr.decimal(10))));
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "Logic");
        assertTrue(source.contains("x > 0 && y < 10"), "should contain logical expression");
    }

    /**
     * Verifies ternary expression output.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void ternaryExpression() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Ternary", sf -> {
            sf.class_("Ternary", cc -> {
                cc.field("val", fc -> {
                    fc.type(Type.INT);
                    fc.init(Expr.$v("x").gt(Expr.ZERO).cond(Expr.ONE, Expr.ZERO));
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "Ternary");
        assertTrue(source.contains("x > 0 ? 1 : 0"), "should contain ternary expression");
    }

    /**
     * Verifies cast expression output.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void castExpression() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Cast", sf -> {
            sf.class_("Cast", cc -> {
                cc.field("val", fc -> {
                    fc.type(Type.INT);
                    fc.init(Expr.$v("x").cast(Type.INT));
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "Cast");
        assertTrue(source.contains("(int) x"), "should contain cast expression");
    }

    /**
     * Verifies instanceof expression output.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void instanceofExpression() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Check", sf -> {
            sf.class_("Check", cc -> {
                cc.field("isStr", fc -> {
                    fc.type(Type.BOOLEAN);
                    fc.init(Expr.$v("obj").instanceof_(Type.STRING));
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "Check");
        assertTrue(source.contains("obj instanceof String"),
                "should contain instanceof expression");
    }

    /**
     * Verifies method call chain output.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void methodCallChain() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Chain", sf -> {
            sf.class_("Chain", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.emit(Expr.$v("builder").call("append", Expr.str("hello"))
                                .call("append", Expr.str("world")).call("toString"));
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "Chain");
        assertTrue(source.contains(".append(\"hello\").append(\"world\").toString()"),
                "should contain chained calls");
    }

    /**
     * Verifies new expression output.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void newExpression() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Create", sf -> {
            sf.class_("Create", cc -> {
                cc.field("obj", fc -> {
                    fc.type(Type.OBJECT);
                    fc.init(Type.OBJECT.new_(List.of(new Expr[] {})));
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "Create");
        assertTrue(source.contains("new Object()"), "should contain new expression");
    }

    /**
     * Verifies array creation and access.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void arrayCreation() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Arr", sf -> {
            sf.class_("Arr", cc -> {
                cc.field("nums", fc -> {
                    fc.type(Type.INT.array());
                    fc.init(Type.INT.array().newArrayInit(Expr.decimal(1), Expr.decimal(2), Expr.decimal(3)));
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "Arr");
        assertTrue(source.contains("new int[]"), "should contain array creation");
        assertTrue(source.contains("{ 1, 2, 3 }"), "should contain initializer");
    }

    /**
     * Verifies method reference expression.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void methodReference() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Ref", sf -> {
            sf.class_("Ref", cc -> {
                cc.field("fn", fc -> {
                    fc.type(Type.OBJECT);
                    fc.init(Type.STRING.methodRef("valueOf"));
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "Ref");
        assertTrue(source.contains("String::valueOf"), "should contain method reference");
    }

    /**
     * Verifies assignment expressions.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void assignmentExpression() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Assign", sf -> {
            sf.class_("Assign", cc -> {
                cc.method("update", mc -> {
                    mc.body(b -> {
                        b.emit(Expr.$v("x").assign(Expr.decimal(42)));
                        b.emit(Expr.$v("x").addAssign(Expr.ONE));
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "Assign");
        assertTrue(source.contains("x = 42;"), "should contain simple assignment");
        assertTrue(source.contains("x += 1;"), "should contain compound assignment");
    }

    // ---- Numeric literal coverage ----

    /**
     * Verifies that a decimal {@code long} literal includes the {@code L} suffix.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void decimalLong() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "DecLong", sf -> {
            sf.class_("DecLong", cc -> {
                cc.field("val", fc -> {
                    fc.type(Type.LONG);
                    fc.init(Expr.decimal(100L));
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "DecLong");
        assertTrue(source.contains("100L"), "should contain long literal with L suffix");
    }

    /**
     * Verifies that a negative decimal {@code long} literal is rendered
     * as a unary negation.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void decimalLongNegative() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "DecLongNeg", sf -> {
            sf.class_("DecLongNeg", cc -> {
                cc.field("val", fc -> {
                    fc.type(Type.LONG);
                    fc.init(Expr.decimal(-42L));
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "DecLongNeg");
        assertTrue(source.contains("-42L"), "should contain negative long literal");
    }

    /**
     * Verifies that a decimal {@code float} literal includes the {@code f} suffix.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void decimalFloat() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "DecFloat", sf -> {
            sf.class_("DecFloat", cc -> {
                cc.field("val", fc -> {
                    fc.type(Type.FLOAT);
                    fc.init(Expr.decimal(3.14f));
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "DecFloat");
        assertTrue(source.contains("3.14f"), "should contain float literal with f suffix");
    }

    /**
     * Verifies that a negative decimal {@code float} literal is rendered
     * as a unary negation.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void decimalFloatNegative() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "DecFloatNeg", sf -> {
            sf.class_("DecFloatNeg", cc -> {
                cc.field("val", fc -> {
                    fc.type(Type.FLOAT);
                    fc.init(Expr.decimal(-1.5f));
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "DecFloatNeg");
        assertTrue(source.contains("-1.5f"), "should contain negative float literal");
    }

    /**
     * Verifies that {@code Float.NaN} is rejected as a float literal.
     */
    @Test
    void decimalFloatNaN() {
        assertThrows(IllegalArgumentException.class, () -> Expr.decimal(Float.NaN));
    }

    /**
     * Verifies that {@code Float.POSITIVE_INFINITY} is rejected as a float literal.
     */
    @Test
    void decimalFloatInfinity() {
        assertThrows(IllegalArgumentException.class, () -> Expr.decimal(Float.POSITIVE_INFINITY));
    }

    /**
     * Verifies that a decimal {@code double} literal is rendered correctly.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void decimalDouble() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "DecDouble", sf -> {
            sf.class_("DecDouble", cc -> {
                cc.field("val", fc -> {
                    fc.type(Type.DOUBLE);
                    fc.init(Expr.decimal(2.718));
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "DecDouble");
        assertTrue(source.contains("2.718"), "should contain double literal");
    }

    /**
     * Verifies that a negative decimal {@code double} literal is rendered
     * as a unary negation.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void decimalDoubleNegative() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "DecDoubleNeg", sf -> {
            sf.class_("DecDoubleNeg", cc -> {
                cc.field("val", fc -> {
                    fc.type(Type.DOUBLE);
                    fc.init(Expr.decimal(-9.81));
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "DecDoubleNeg");
        assertTrue(source.contains("-9.81"), "should contain negative double literal");
    }

    /**
     * Verifies that {@code Double.NaN} is rejected as a double literal.
     */
    @Test
    void decimalDoubleNaN() {
        assertThrows(IllegalArgumentException.class, () -> Expr.decimal(Double.NaN));
    }

    /**
     * Verifies that {@code Double.NEGATIVE_INFINITY} is rejected as a double literal.
     */
    @Test
    void decimalDoubleInfinity() {
        assertThrows(IllegalArgumentException.class, () -> Expr.decimal(Double.NEGATIVE_INFINITY));
    }

    /**
     * Verifies that a negative decimal {@code int} literal is rendered
     * as a unary negation.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void decimalIntNegative() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "DecIntNeg", sf -> {
            sf.class_("DecIntNeg", cc -> {
                cc.field("val", fc -> {
                    fc.type(Type.INT);
                    fc.init(Expr.decimal(-7));
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "DecIntNeg");
        assertTrue(source.contains("-7"), "should contain negative int literal");
    }

    /**
     * Verifies that a hexadecimal {@code int} literal includes the {@code 0x} prefix.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void hexInt() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "HexInt", sf -> {
            sf.class_("HexInt", cc -> {
                cc.field("val", fc -> {
                    fc.type(Type.INT);
                    fc.init(Expr.hex(0xFF));
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "HexInt");
        assertTrue(source.contains("0xff"), "should contain hex int literal with 0x prefix");
    }

    /**
     * Verifies that a hexadecimal {@code long} literal includes the {@code 0x}
     * prefix and {@code L} suffix.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void hexLong() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "HexLong", sf -> {
            sf.class_("HexLong", cc -> {
                cc.field("val", fc -> {
                    fc.type(Type.LONG);
                    fc.init(Expr.hex(0xCAFEBABEL));
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "HexLong");
        assertTrue(source.contains("0xcafebabeL"), "should contain hex long literal with 0x prefix and L suffix");
    }

    /**
     * Verifies that an octal {@code int} literal includes the {@code 0} prefix.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void octalInt() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "OctInt", sf -> {
            sf.class_("OctInt", cc -> {
                cc.field("val", fc -> {
                    fc.type(Type.INT);
                    fc.init(Expr.octal(077));
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "OctInt");
        assertTrue(source.contains("077"), "should contain octal int literal with 0 prefix");
    }

    /**
     * Verifies that an octal {@code long} literal includes the {@code 0} prefix
     * and {@code L} suffix.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void octalLong() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "OctLong", sf -> {
            sf.class_("OctLong", cc -> {
                cc.field("val", fc -> {
                    fc.type(Type.LONG);
                    fc.init(Expr.octal(0777L));
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "OctLong");
        assertTrue(source.contains("0777L"), "should contain octal long literal with 0 prefix and L suffix");
    }

    /**
     * Verifies that a binary {@code int} literal includes the {@code 0b} prefix.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void binaryInt() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "BinInt", sf -> {
            sf.class_("BinInt", cc -> {
                cc.field("val", fc -> {
                    fc.type(Type.INT);
                    fc.init(Expr.binary(0b1010));
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "BinInt");
        assertTrue(source.contains("0b1010"), "should contain binary int literal with 0b prefix");
    }

    /**
     * Verifies that a binary {@code long} literal includes the {@code 0b} prefix
     * and {@code L} suffix.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void binaryLong() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "BinLong", sf -> {
            sf.class_("BinLong", cc -> {
                cc.field("val", fc -> {
                    fc.type(Type.LONG);
                    fc.init(Expr.binary(0b11001100L));
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "BinLong");
        assertTrue(source.contains("0b11001100L"), "should contain binary long literal with 0b prefix and L suffix");
    }

    /**
     * Verifies that a hexadecimal {@code float} literal is rendered in
     * hex floating-point notation with the {@code f} suffix.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void hexFloat() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "HexFloat", sf -> {
            sf.class_("HexFloat", cc -> {
                cc.field("val", fc -> {
                    fc.type(Type.FLOAT);
                    fc.init(Expr.hex(1.0f));
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "HexFloat");
        assertTrue(source.contains("0x1.0p0f"), "should contain hex float literal");
    }

    /**
     * Verifies that {@code Float.NaN} is rejected for hex float literals.
     */
    @Test
    void hexFloatNaN() {
        assertThrows(IllegalArgumentException.class, () -> Expr.hex(Float.NaN));
    }

    /**
     * Verifies that a hexadecimal {@code double} literal is rendered in
     * hex floating-point notation.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void hexDouble() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "HexDouble", sf -> {
            sf.class_("HexDouble", cc -> {
                cc.field("val", fc -> {
                    fc.type(Type.DOUBLE);
                    fc.init(Expr.hex(1.0));
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "HexDouble");
        assertTrue(source.contains("0x1.0p0"), "should contain hex double literal");
    }

    /**
     * Verifies that {@code Double.POSITIVE_INFINITY} is rejected for hex double literals.
     */
    @Test
    void hexDoubleInfinity() {
        assertThrows(IllegalArgumentException.class, () -> Expr.hex(Double.POSITIVE_INFINITY));
    }

    /**
     * Verifies that negative zero float ({@code -0.0f}) is rendered correctly
     * as a literal without going through the unary negation path, since
     * {@code -0.0f < 0} is {@code false} in IEEE 754.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void decimalFloatNegativeZero() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "NegZeroFloat", sf -> {
            sf.class_("NegZeroFloat", cc -> {
                cc.field("val", fc -> {
                    fc.type(Type.FLOAT);
                    fc.init(Expr.decimal(-0.0f));
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "NegZeroFloat");
        assertTrue(source.contains("-0.0f"), "should contain negative zero float literal");
    }

    /**
     * Verifies that negative zero double ({@code -0.0}) is rendered correctly
     * as a literal without going through the unary negation path, since
     * {@code -0.0 < 0} is {@code false} in IEEE 754.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void decimalDoubleNegativeZero() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "NegZeroDouble", sf -> {
            sf.class_("NegZeroDouble", cc -> {
                cc.field("val", fc -> {
                    fc.type(Type.DOUBLE);
                    fc.init(Expr.decimal(-0.0));
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "NegZeroDouble");
        assertTrue(source.contains("-0.0"), "should contain negative zero double literal");
    }

    // ---- Other literal coverage ----

    /**
     * Verifies that a character literal is rendered with single quotes and
     * proper content.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void charLiteral() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "CharLit", sf -> {
            sf.class_("CharLit", cc -> {
                cc.field("ch", fc -> {
                    fc.type(Type.CHAR);
                    fc.init(Expr.ch('A'));
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "CharLit");
        assertTrue(source.contains("'A'"), "should contain char literal");
    }

    /**
     * Verifies that special characters in char literals are properly escaped.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void charLiteralEscaped() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "CharEsc", sf -> {
            sf.class_("CharEsc", cc -> {
                cc.field("nl", fc -> {
                    fc.type(Type.CHAR);
                    fc.init(Expr.ch('\n'));
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "CharEsc");
        assertTrue(source.contains("'\\n'"), "should contain escaped newline char literal");
    }

    /**
     * Verifies that a text block literal is rendered with triple-quote
     * delimiters.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void textBlockLiteral() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "TextBlock", sf -> {
            sf.class_("TextBlock", cc -> {
                cc.field("json", fc -> {
                    fc.type(Type.STRING);
                    fc.init(Expr.textBlock("{ \"key\": \"value\" }\n"));
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "TextBlock");
        assertTrue(source.contains("\"\"\""), "should contain text block delimiters");
        assertTrue(source.contains("{ \"key\": \"value\" }"), "should contain text block content");
    }

    // ---- Variable reference coverage ----

    /**
     * Verifies that a qualified {@code this} expression is rendered as
     * {@code Outer.this}.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void qualifiedThis() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "QualThis", sf -> {
            sf.class_("QualThis", cc -> {
                cc.field("ref", fc -> {
                    fc.type(Type.OBJECT);
                    fc.init(Type.named("com.example.QualThis").this_());
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "QualThis");
        assertTrue(source.contains("QualThis.this"), "should contain qualified this expression");
    }

    // ---- Method call coverage ----

    /**
     * Verifies that a static method call is rendered as {@code Type.method(args)}.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void staticMethodCall() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "StaticCall", sf -> {
            sf.class_("StaticCall", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.emit(Type.STRING.call("valueOf", Expr.decimal(42)));
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "StaticCall");
        assertTrue(source.contains("String.valueOf(42)"), "should contain static method call");
    }

    // ---- Object creation coverage ----

    /**
     * Verifies that an array creation with dimension sizes is rendered as
     * {@code new Type[n]}.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void newArrayWithDimensions() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "NewArr", sf -> {
            sf.class_("NewArr", cc -> {
                cc.field("arr", fc -> {
                    fc.type(Type.INT.array());
                    fc.init(Type.INT.array().new_(Expr.decimal(10)));
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "NewArr");
        assertTrue(source.contains("new int[10]"), "should contain array creation with dimension");
    }

    /**
     * Verifies that an array initializer created from a {@link List} renders
     * the same as the varargs variant.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void newArrayInitFromList() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "NewArrList", sf -> {
            sf.class_("NewArrList", cc -> {
                cc.field("arr", fc -> {
                    fc.type(Type.INT.array());
                    fc.init(Type.INT.array().newArrayInit(List.of(Expr.decimal(1), Expr.decimal(2))));
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "NewArrList");
        assertTrue(source.contains("new int[]"), "should contain array creation");
        assertTrue(source.contains("{ 1, 2 }"), "should contain initializer elements");
    }

    // ---- Method reference coverage ----

    /**
     * Verifies that a method reference on an expression is rendered as
     * {@code expr::method}.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void methodRefOnExpression() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "ExprRef", sf -> {
            sf.class_("ExprRef", cc -> {
                cc.field("fn", fc -> {
                    fc.type(Type.OBJECT);
                    final Expr receiver = Expr.$v("obj");
                    fc.init(receiver.methodRef("toString"));
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "ExprRef");
        assertTrue(source.contains("obj::toString"), "should contain method reference on expression");
    }

    // ---- Anonymous class coverage ----

    /**
     * Verifies that an anonymous class expression is rendered as
     * {@code new Type() { ... }}.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void anonymousClass() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "AnonClass", sf -> {
            sf.class_("AnonClass", cc -> {
                cc.field("r", fc -> {
                    fc.type(Type.named("java.lang.Runnable"));
                    final Type type = Type.named("java.lang.Runnable");
                    fc.init(type.new_(SourceVersion.JAVA_17, List.of(), acc -> {
                        acc.method("run", mc -> {
                            mc.public_();
                            mc.body(b -> {
                            });
                        });
                    }));
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "AnonClass");
        assertTrue(source.contains("new Runnable()"), "should contain anonymous class creation");
        assertTrue(source.contains("public void run()"), "should contain overridden method");
    }

    // ---- Switch expression coverage ----

    /**
     * Verifies that a switch expression is rendered with {@code switch (selector) { cases }}.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void switchExpression() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "SwitchExpr", sf -> {
            sf.class_("SwitchExpr", cc -> {
                cc.method("classify", mc -> {
                    mc.param("x", Type.INT);
                    mc.returning(Type.STRING);
                    mc.body(b -> {
                        final Expr selector = Expr.$v("x");
                        b.return_(selector.switch_(SourceVersion.JAVA_17, sw -> {
                            sw.case_(Expr.ZERO, body -> {
                                body.yield_(Expr.str("zero"));
                            });
                            sw.default_(body -> {
                                body.yield_(Expr.str("other"));
                            });
                        }));
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "SwitchExpr");
        assertTrue(source.contains("switch (x)"), "should contain switch expression selector");
        assertTrue(source.contains("case 0 ->"), "should contain arrow case label");
        assertFalse(source.contains("case 0:"), "should not contain colon case label");
        assertTrue(source.contains("\"zero\""), "should contain yield expression value");
        assertFalse(source.contains("yield"), "single-yield should be stripped in arrow form");
    }

    // ---- Lambda expression coverage ----

    /**
     * Verifies that an expression-body lambda with a single untyped parameter
     * is rendered as {@code x -> expr}.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void lambdaSingleParamExpression() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Lambda1", sf -> {
            sf.class_("Lambda1", cc -> {
                cc.field("fn", fc -> {
                    fc.type(Type.OBJECT);
                    fc.init(Expr.lambda("x", Expr.$v("x").mul(Expr.decimal(2))));
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "Lambda1");
        assertTrue(source.contains("x -> x * 2"), "should contain single-param expression lambda");
    }

    /**
     * Verifies that an expression-body lambda with multiple untyped parameters
     * is rendered as {@code (x, y) -> expr}.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void lambdaMultiParamExpression() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Lambda2", sf -> {
            sf.class_("Lambda2", cc -> {
                cc.field("fn", fc -> {
                    fc.type(Type.OBJECT);
                    fc.init(Expr.lambda(List.of("a", "b"), Expr.$v("a").add(Expr.$v("b"))));
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "Lambda2");
        assertTrue(source.contains("(a, b) -> a + b"), "should contain multi-param expression lambda");
    }

    /**
     * Verifies that a block-body lambda with a single untyped parameter
     * and multiple statements is rendered as {@code x -> { stmts }}.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void lambdaSingleParamBlock() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Lambda3", sf -> {
            sf.class_("Lambda3", cc -> {
                cc.field("fn", fc -> {
                    fc.type(Type.OBJECT);
                    fc.init(Expr.lambda(lc -> {
                        Var x = lc.param("x");
                        lc.body(b -> {
                            b.emit(x.call("toString"));
                            b.return_(x);
                        });
                    }));
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "Lambda3");
        assertTrue(source.contains("x -> {"), "should contain single-param block lambda");
        assertTrue(source.contains("return x;"), "should contain block body return statement");
    }

    /**
     * Verifies that a block-body lambda with multiple untyped parameters
     * and multiple statements is rendered as {@code (x, y) -> { stmts }}.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void lambdaMultiParamBlock() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Lambda4", sf -> {
            sf.class_("Lambda4", cc -> {
                cc.field("fn", fc -> {
                    fc.type(Type.OBJECT);
                    fc.init(Expr.lambda(lc -> {
                        Var a = lc.param("a");
                        Var b = lc.param("b");
                        lc.body(bc -> {
                            bc.var(Type.INT, "sum", a.add(b));
                            bc.return_(Expr.$v("sum"));
                        });
                    }));
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "Lambda4");
        assertTrue(source.contains("(a, b) -> {"), "should contain multi-param block lambda");
        assertTrue(source.contains("return sum;"), "should contain block body return statement");
    }

    /**
     * Verifies that an expression-body lambda with typed parameters
     * is rendered as {@code (Type1 x) -> expr}.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void lambdaTypedExpression() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "LambdaTyped1", sf -> {
            sf.class_("LambdaTyped1", cc -> {
                cc.field("fn", fc -> {
                    fc.type(Type.OBJECT);
                    fc.init(Expr.lambda(Type.INT, "x",
                            Expr.$v("x").mul(Expr.decimal(2))));
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "LambdaTyped1");
        assertTrue(source.contains("(int x) -> x * 2"),
                "should contain typed-param expression lambda");
    }

    // ---- Wildcard type coverage ----

    /**
     * Verifies that an unbounded wildcard type argument is rendered as
     * {@code List<?>}.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void unboundedWildcard() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "WildcardTest", sf -> {
            sf.class_("WildcardTest", cc -> {
                cc.field("items", fc -> {
                    fc.type(Type.named("java.util.List").typeArg(Type.WILDCARD));
                    fc.init(Expr.NULL);
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "WildcardTest");
        assertTrue(source.contains("List<?> items"), "should contain unbounded wildcard type");
    }

    /**
     * Verifies that an upper-bounded wildcard type argument is rendered as
     * {@code List<? extends Number>}.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void upperBoundedWildcard() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "UpperWild", sf -> {
            sf.class_("UpperWild", cc -> {
                cc.field("nums", fc -> {
                    fc.type(Type.named("java.util.List").typeArg(
                            Type.named("java.lang.Number").wildcardExtends()));
                    fc.init(Expr.NULL);
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "UpperWild");
        assertTrue(source.contains("List<? extends Number> nums"),
                "should contain upper-bounded wildcard type");
    }

    /**
     * Verifies that a lower-bounded wildcard type argument is rendered as
     * {@code List<? super Integer>}.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void lowerBoundedWildcard() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "LowerWild", sf -> {
            sf.class_("LowerWild", cc -> {
                cc.field("nums", fc -> {
                    fc.type(Type.named("java.util.List").typeArg(
                            Type.named("java.lang.Integer").wildcardSuper()));
                    fc.init(Expr.NULL);
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "LowerWild");
        assertTrue(source.contains("List<? super Integer> nums"),
                "should contain lower-bounded wildcard type");
    }

    /**
     * Verifies that an expression-body lambda with two typed parameters
     * is rendered as {@code (String s, int n) -> expr}.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void lambdaTypedBlock() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "LambdaTyped2", sf -> {
            sf.class_("LambdaTyped2", cc -> {
                cc.field("fn", fc -> {
                    fc.type(Type.OBJECT);
                    fc.init(Expr.lambda(Type.STRING, "s", Type.INT, "n",
                            Expr.$v("s").call("substring", Expr.$v("n"))));
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "LambdaTyped2");
        assertTrue(source.contains("(String s, int n) -> s.substring(n)"),
                "should contain typed-param expression lambda");
    }

    /**
     * Verifies that a block-body lambda with typed parameters and multiple
     * statements is rendered as {@code (String s, int n) -> { stmts }}.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void lambdaTypedMultiStatementBlock() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "LambdaTyped3", sf -> {
            sf.class_("LambdaTyped3", cc -> {
                cc.field("fn", fc -> {
                    fc.type(Type.OBJECT);
                    fc.init(Expr.lambda(lc -> {
                        Var s = lc.param("s", Type.STRING);
                        Var n = lc.param("n", Type.INT);
                        lc.body(bc -> {
                            bc.var(Type.STRING, "result", s.call("substring", n));
                            bc.return_(Expr.$v("result"));
                        });
                    }));
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "LambdaTyped3");
        assertTrue(source.contains("(String s, int n) -> {"),
                "should contain typed-param block lambda");
        assertTrue(source.contains("return result;"),
                "should contain block body return");
    }

    /**
     * Verifies that a zero-parameter expression lambda
     * is rendered as {@code () -> expr}.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void lambdaZeroParam() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "LambdaZero", sf -> {
            sf.class_("LambdaZero", cc -> {
                cc.field("fn", fc -> {
                    fc.type(Type.OBJECT);
                    fc.init(Expr.lambda(Expr.decimal(42)));
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "LambdaZero");
        assertTrue(source.contains("() -> 42"),
                "should contain zero-param expression lambda");
    }

    /**
     * Verifies that the {@link FormatPreferences.Opt#LAMBDA_ALWAYS_BLOCK_BODY}
     * option forces block rendering even for single-return lambdas.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void lambdaAlwaysBlockBody() throws IOException {
        final FormatPreferences prefs = FormatPreferences.builder()
                .addOption(FormatPreferences.Opt.LAMBDA_ALWAYS_BLOCK_BODY)
                .build();
        final Sources sources = createSources(prefs, SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "LambdaBlock", sf -> {
            sf.class_("LambdaBlock", cc -> {
                cc.field("fn", fc -> {
                    fc.type(Type.OBJECT);
                    fc.init(Expr.lambda("x", Expr.$v("x").mul(Expr.decimal(2))));
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "LambdaBlock");
        assertTrue(source.contains("x -> {"), "should contain block lambda opening");
        assertTrue(source.contains("return x * 2;"), "should contain return statement");
    }

    /**
     * Verifies that mixing typed and untyped parameters throws
     * {@link IllegalStateException}.
     */
    @Test
    void lambdaMixedParamsRejected() {
        assertThrows(IllegalStateException.class, () -> {
            Expr.lambda(lc -> {
                lc.param("x");
                lc.param("y", Type.INT);
                lc.body(b -> b.return_(Expr.$v("x")));
            });
        });
    }

    /**
     * Verifies that omitting the {@code body()} call on a {@link io.smallrye.jdeparser.creator.LambdaCreator}
     * throws {@link IllegalStateException}.
     */
    @Test
    void lambdaMissingBodyRejected() {
        assertThrows(IllegalStateException.class, () -> {
            Expr.lambda(lc -> {
                lc.param("x");
            });
        });
    }
}
