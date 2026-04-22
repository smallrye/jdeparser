package org.jboss.jdeparser.test;

import java.io.IOException;

import org.jboss.jdeparser.JExpr;
import org.jboss.jdeparser.JExprs;
import org.jboss.jdeparser.JSources;
import org.jboss.jdeparser.JType;
import org.jboss.jdeparser.SourceVersion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

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
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Math", sf -> {
            sf.class_("Math", cc -> {
                cc.field("result", fc -> {
                    fc.type(JType.INT);
                    // (a + b) * c
                    fc.init(JExprs.$v("a").add(JExprs.$v("b")).paren().mul(JExprs.$v("c")));
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
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Logic", sf -> {
            sf.class_("Logic", cc -> {
                cc.field("flag", fc -> {
                    fc.type(JType.BOOLEAN);
                    // x > 0 && y < 10
                    fc.init(JExprs.$v("x").gt(JExpr.ZERO).and(JExprs.$v("y").lt(JExprs.decimal(10))));
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
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Ternary", sf -> {
            sf.class_("Ternary", cc -> {
                cc.field("val", fc -> {
                    fc.type(JType.INT);
                    fc.init(JExprs.$v("x").gt(JExpr.ZERO).cond(JExpr.ONE, JExpr.ZERO));
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
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Cast", sf -> {
            sf.class_("Cast", cc -> {
                cc.field("val", fc -> {
                    fc.type(JType.INT);
                    fc.init(JExprs.$v("x").cast(JType.INT));
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
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Check", sf -> {
            sf.class_("Check", cc -> {
                cc.field("isStr", fc -> {
                    fc.type(JType.BOOLEAN);
                    fc.init(JExprs.$v("obj").instanceof_(JType.STRING));
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "Check");
        assertTrue(source.contains("obj instanceof java.lang.String"),
            "should contain instanceof expression");
    }

    /**
     * Verifies method call chain output.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void methodCallChain() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Chain", sf -> {
            sf.class_("Chain", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.emit(JExprs.$v("builder").call("append", JExprs.str("hello"))
                            .call("append", JExprs.str("world")).call("toString"));
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
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Create", sf -> {
            sf.class_("Create", cc -> {
                cc.field("obj", fc -> {
                    fc.type(JType.OBJECT);
                    fc.init(JExprs.new_(JType.OBJECT));
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "Create");
        assertTrue(source.contains("new java.lang.Object()"), "should contain new expression");
    }

    /**
     * Verifies array creation and access.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void arrayCreation() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Arr", sf -> {
            sf.class_("Arr", cc -> {
                cc.field("nums", fc -> {
                    fc.type(JType.INT.array());
                    fc.init(JExprs.newArrayInit(JType.INT, JExprs.decimal(1), JExprs.decimal(2), JExprs.decimal(3)));
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
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Ref", sf -> {
            sf.class_("Ref", cc -> {
                cc.field("fn", fc -> {
                    fc.type(JType.OBJECT);
                    fc.init(JExprs.methodRef(JType.STRING, "valueOf"));
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "Ref");
        assertTrue(source.contains("java.lang.String::valueOf"), "should contain method reference");
    }

    /**
     * Verifies assignment expressions.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void assignmentExpression() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Assign", sf -> {
            sf.class_("Assign", cc -> {
                cc.method("update", mc -> {
                    mc.body(b -> {
                        b.emit(JExprs.$v("x").assign(JExprs.decimal(42)));
                        b.emit(JExprs.$v("x").addAssign(JExpr.ONE));
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "Assign");
        assertTrue(source.contains("x = 42;"), "should contain simple assignment");
        assertTrue(source.contains("x += 1;"), "should contain compound assignment");
    }
}
