package org.jboss.jdeparser.test;

import java.io.IOException;
import java.util.List;

import org.jboss.jdeparser.JExpr;
import org.jboss.jdeparser.JSources;
import org.jboss.jdeparser.JType;
import org.jboss.jdeparser.SourceVersion;
import org.jboss.jdeparser.creator.BlockCreator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Comprehensive tests for all control flow statement constructs generated
 * by the jdeparser3 borrow-pattern API.
 * <p>
 * Each test method generates a source file containing a class with a method
 * that exercises a specific control flow construct, writes the source, and
 * asserts that the generated output contains the expected Java syntax.
 */
class ControlFlowTest extends AbstractGeneratingTestCase {

    /**
     * Verifies that a {@code while} loop generates the correct condition and body.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void whileLoop() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "WhileLoop", sf -> {
            sf.class_("WhileLoop", cc -> {
                cc.method("countdown", mc -> {
                    mc.param("x", JType.INT);
                    mc.body(b -> {
                        b.while_(JExpr.$v("x").gt(JExpr.ZERO), loop -> {
                            loop.emit(JExpr.$v("x").dec());
                        });
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "WhileLoop");
        assertTrue(source.contains("while (x > 0)"), "should contain while condition");
        assertTrue(source.contains("x--;"), "should contain loop body statement");
    }

    /**
     * Verifies that a {@code do-while} loop generates the correct structure
     * with the condition after the body.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void doWhileLoop() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "DoWhileLoop", sf -> {
            sf.class_("DoWhileLoop", cc -> {
                cc.method("increment", mc -> {
                    mc.param("x", JType.INT);
                    mc.body(b -> {
                        b.doWhile(
                            loop -> loop.emit(JExpr.inc(JExpr.$v("x"))),
                            JExpr.$v("x").gt(JExpr.ZERO)
                        );
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "DoWhileLoop");
        assertTrue(source.contains("do {"), "should contain do keyword with opening brace");
        assertTrue(source.contains("} while (x > 0);"), "should contain closing brace with while condition");
    }

    /**
     * Verifies that a traditional {@code for} loop generates the correct
     * initializer, condition, and update expressions.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void forLoop() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "ForLoop", sf -> {
            sf.class_("ForLoop", cc -> {
                cc.method("iterate", mc -> {
                    mc.param("n", JType.INT);
                    mc.body(b -> {
                        b.for_(fb -> {
                            fb.init(JType.INT, "i", JExpr.ZERO);
                            fb.condition(JExpr.$v("i").lt(JExpr.$v("n")));
                            fb.update(JExpr.$v("i").inc());
                            fb.body(loop -> {
                                loop.emit(JExpr.callPlain("println", JExpr.$v("i")));
                            });
                        });
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "ForLoop");
        assertTrue(source.contains("for (int i = 0; i < n; i++)"),
            "should contain for loop header with init, condition, and update");
        assertTrue(source.contains("println(i);"), "should contain loop body statement");
    }

    /**
     * Verifies that an enhanced {@code for-each} loop generates the correct
     * variable declaration and iterable expression.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void forEachLoop() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "ForEachLoop", sf -> {
            sf.class_("ForEachLoop", cc -> {
                cc.method("process", mc -> {
                    mc.param("items", JType.INT.array());
                    mc.body(b -> {
                        b.forEach(JType.INT, "item", JExpr.$v("items"), loop -> {
                            loop.emit(JExpr.callPlain("println", JExpr.$v("item")));
                        });
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "ForEachLoop");
        assertTrue(source.contains("for (int item : items)"),
            "should contain for-each loop with type, variable, and iterable");
        assertTrue(source.contains("println(item);"), "should contain loop body statement");
    }

    /**
     * Verifies that a {@code switch} statement generates the correct selector,
     * case labels, and default clause.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void switchStatement() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "SwitchStmt", sf -> {
            sf.class_("SwitchStmt", cc -> {
                cc.method("classify", mc -> {
                    mc.param("x", JType.INT);
                    mc.returning(JType.STRING);
                    mc.body(b -> {
                        b.switch_(JExpr.$v("x"), sw -> {
                            sw.case_(JExpr.ZERO, body -> {
                                body.return_(JExpr.str("zero"));
                            });
                            sw.case_(JExpr.ONE, body -> {
                                body.return_(JExpr.str("one"));
                            });
                            sw.default_(body -> {
                                body.return_(JExpr.str("other"));
                            });
                        });
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "SwitchStmt");
        assertTrue(source.contains("switch (x)"), "should contain switch selector");
        assertTrue(source.contains("case 0:"), "should contain case 0 label");
        assertTrue(source.contains("case 1:"), "should contain case 1 label");
        assertTrue(source.contains("default:"), "should contain default label");
        assertTrue(source.contains("return \"zero\";"), "should contain case 0 body");
        assertTrue(source.contains("return \"other\";"), "should contain default body");
    }

    /**
     * Verifies that a {@code try-catch-finally} statement generates all three
     * clauses with correct structure.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void tryCatchFinally() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "TryCatchFinally", sf -> {
            sf.class_("TryCatchFinally", cc -> {
                cc.method("handle", mc -> {
                    mc.body(b -> {
                        b.try_(tb -> {
                            tb.body(tryBody -> {
                                tryBody.emit(JExpr.callPlain("riskyOperation"));
                            });
                            tb.catch_(JType.named("java.lang.Exception"), "e", catchBody -> {
                                catchBody.emit(JExpr.$v("e").call("printStackTrace"));
                            });
                            tb.finally_(finallyBody -> {
                                finallyBody.emit(JExpr.callPlain("cleanup"));
                            });
                        });
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "TryCatchFinally");
        assertTrue(source.contains("try"), "should contain try keyword");
        assertTrue(source.contains("riskyOperation();"), "should contain try body");
        assertTrue(source.contains("catch"), "should contain catch keyword");
        assertTrue(source.contains("Exception e"), "should contain catch parameter");
        assertTrue(source.contains("e.printStackTrace();"), "should contain catch body");
        assertTrue(source.contains("finally"), "should contain finally keyword");
        assertTrue(source.contains("cleanup();"), "should contain finally body");
    }

    /**
     * Verifies that a try-with-resources statement generates the resource
     * declaration within the try header.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void tryWithResources() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "TryWithResources", sf -> {
            sf.class_("TryWithResources", cc -> {
                cc.method("readFile", mc -> {
                    mc.body(b -> {
                        b.try_(tb -> {
                            tb.with(
                                JType.named("java.io.InputStream"),
                                "in",
                                JExpr.callPlain("openStream")
                            );
                            tb.body(tryBody -> {
                                tryBody.emit(JExpr.$v("in").call("read"));
                            });
                        });
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "TryWithResources");
        assertTrue(source.contains("try ("), "should contain try-with-resources opening");
        assertTrue(source.contains("java.io.InputStream in"), "should contain resource type and name");
        assertTrue(source.contains("openStream()"), "should contain resource initializer");
        assertTrue(source.contains("in.read();"), "should contain try body");
    }

    /**
     * Verifies that a {@code synchronized} block generates the correct
     * monitor expression and body.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void synchronizedBlock() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "SyncBlock", sf -> {
            sf.class_("SyncBlock", cc -> {
                cc.method("criticalSection", mc -> {
                    mc.body(b -> {
                        b.synchronized_(JExpr.THIS, syncBody -> {
                            syncBody.emit(JExpr.callPlain("doWork"));
                        });
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "SyncBlock");
        assertTrue(source.contains("synchronized (this)"), "should contain synchronized with monitor expression");
        assertTrue(source.contains("doWork();"), "should contain synchronized body");
    }

    /**
     * Verifies that a labeled block generates the label name and that a
     * {@code break} statement can reference the label.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void labeledBlock() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "LabeledBlock", sf -> {
            sf.class_("LabeledBlock", cc -> {
                cc.method("search", mc -> {
                    mc.body(b -> {
                        b.labeled("outer", (label, labeledBody) -> {
                            labeledBody.while_(JExpr.TRUE, loop -> {
                                loop.break_(label);
                            });
                        });
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "LabeledBlock");
        assertTrue(source.contains("outer:"), "should contain label name");
        assertTrue(source.contains("break outer;"), "should contain break with label");
    }

    /**
     * Verifies that an {@code assert} statement without a message generates
     * the correct syntax.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void assertStatement() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "AssertStmt", sf -> {
            sf.class_("AssertStmt", cc -> {
                cc.method("validate", mc -> {
                    mc.param("x", JType.INT);
                    mc.body(b -> {
                        b.assert_(JExpr.$v("x").gt(JExpr.ZERO));
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "AssertStmt");
        assertTrue(source.contains("assert x > 0;"), "should contain assert with condition");
    }

    /**
     * Verifies that an {@code assert} statement with a detail message generates
     * the condition and message separated by a colon.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void assertWithMessage() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "AssertMsg", sf -> {
            sf.class_("AssertMsg", cc -> {
                cc.method("validate", mc -> {
                    mc.param("x", JType.INT);
                    mc.body(b -> {
                        b.assert_(JExpr.$v("x").gt(JExpr.ZERO), JExpr.str("must be positive"));
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "AssertMsg");
        assertTrue(source.contains("assert x > 0 : \"must be positive\";"),
            "should contain assert with condition and message");
    }

    /**
     * Verifies that a {@code throw} statement generates the correct exception
     * expression.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void throwStatement() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "ThrowStmt", sf -> {
            sf.class_("ThrowStmt", cc -> {
                cc.method("fail", mc -> {
                    mc.body(b -> {
                        final JType type = JType.named("java.lang.Exception");
                        b.throw_(type.new_(List.of(new JExpr[] {})));
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "ThrowStmt");
        assertTrue(source.contains("throw new Exception();"),
            "should contain throw with new exception");
    }

    /**
     * Verifies that unlabeled {@code break} and {@code continue} statements
     * generate the correct syntax within a loop.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void breakContinue() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "BreakContinue", sf -> {
            sf.class_("BreakContinue", cc -> {
                cc.method("loop", mc -> {
                    mc.param("n", JType.INT);
                    mc.body(b -> {
                        b.for_(fb -> {
                            fb.init(JType.INT, "i", JExpr.ZERO);
                            fb.condition(JExpr.$v("i").lt(JExpr.$v("n")));
                            fb.update(JExpr.$v("i").inc());
                            fb.body(loop -> {
                                loop.if_(JExpr.$v("i").eq(JExpr.decimal(5)), BlockCreator::break_);
                                loop.if_(JExpr.$v("i").eq(JExpr.decimal(3)), BlockCreator::continue_);
                            });
                        });
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "BreakContinue");
        assertTrue(source.contains("break;"), "should contain unlabeled break");
        assertTrue(source.contains("continue;"), "should contain unlabeled continue");
    }

    /**
     * Verifies that an empty statement generates a lone semicolon.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void emptyStatement() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "EmptyStmt", sf -> {
            sf.class_("EmptyStmt", cc -> {
                cc.method("noop", mc -> {
                    mc.body(BlockCreator::empty);
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "EmptyStmt");
        assertTrue(source.contains(";"), "should contain empty statement semicolon");
    }

    /**
     * Verifies that a local variable declaration with an explicit type
     * generates the correct type, name, and initializer.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void localVarDeclaration() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "LocalVar", sf -> {
            sf.class_("LocalVar", cc -> {
                cc.method("init", mc -> {
                    mc.body(b -> {
                        b.var(JType.INT, "x", JExpr.ZERO);
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "LocalVar");
        assertTrue(source.contains("int x = 0;"), "should contain typed local variable declaration");
    }

    /**
     * Verifies that a local variable declaration with inferred type ({@code var})
     * generates the correct syntax.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void localVarInferred() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "LocalVarInferred", sf -> {
            sf.class_("LocalVarInferred", cc -> {
                cc.method("init", mc -> {
                    mc.body(b -> {
                        b.var("x", JExpr.decimal(42));
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "LocalVarInferred");
        assertTrue(source.contains("var x = 42;"), "should contain var-inferred local variable declaration");
    }

    /**
     * Verifies that a local variable declaration with an annotation generates
     * the annotation on the line before the declaration.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void localVarAnnotated() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        final JType suppressWarningsType = JType.named("java.lang.SuppressWarnings");
        sources.createSourceFile("com.example", "LocalVarAnnotated", sf -> {
            sf.class_("LocalVarAnnotated", cc -> {
                cc.method("init", mc -> {
                    mc.body(b -> {
                        b.var(JType.INT, "x", JExpr.ZERO, lv -> {
                            lv.annotate(suppressWarningsType, a -> {
                                a.value(JExpr.str("unchecked"));
                            });
                        });
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "LocalVarAnnotated");
        assertTrue(source.contains("@SuppressWarnings(\"unchecked\")"),
            "should contain annotation");
        assertTrue(source.contains("int x = 0;"),
            "should contain typed local variable declaration");
    }

    /**
     * Verifies that a local variable declaration with a marker annotation
     * generates the annotation on the line before the declaration.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void localVarMarkerAnnotation() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        final JType deprecatedType = JType.named("java.lang.Deprecated");
        sources.createSourceFile("com.example", "LocalVarMarker", sf -> {
            sf.class_("LocalVarMarker", cc -> {
                cc.method("init", mc -> {
                    mc.body(b -> {
                        b.var(JType.INT, "x", JExpr.ZERO, lv -> {
                            lv.annotate(deprecatedType);
                        });
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "LocalVarMarker");
        assertTrue(source.contains("@Deprecated"),
            "should contain marker annotation");
        assertTrue(source.contains("int x = 0;"),
            "should contain typed local variable declaration");
    }

    /**
     * Verifies that a local variable declaration with the {@code final}
     * modifier generates the correct syntax.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void localVarFinal() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "LocalVarFinal", sf -> {
            sf.class_("LocalVarFinal", cc -> {
                cc.method("init", mc -> {
                    mc.body(b -> {
                        b.var(JType.INT, "x", JExpr.ZERO, lv -> {
                            lv.final_();
                        });
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "LocalVarFinal");
        assertTrue(source.contains("final int x = 0;"),
            "should contain final typed local variable declaration");
    }

    /**
     * Verifies that a local variable declaration with both an annotation
     * and the {@code final} modifier generates both correctly.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void localVarAnnotatedFinal() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        final JType suppressWarningsType = JType.named("java.lang.SuppressWarnings");
        sources.createSourceFile("com.example", "LocalVarAnnotatedFinal", sf -> {
            sf.class_("LocalVarAnnotatedFinal", cc -> {
                cc.method("init", mc -> {
                    mc.body(b -> {
                        b.var(JType.INT, "x", JExpr.ZERO, lv -> {
                            lv.annotate(suppressWarningsType, a -> {
                                a.value(JExpr.str("unchecked"));
                            });
                            lv.final_();
                        });
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "LocalVarAnnotatedFinal");
        assertTrue(source.contains("@SuppressWarnings(\"unchecked\")"),
            "should contain annotation");
        assertTrue(source.contains("final int x = 0;"),
            "should contain final typed local variable declaration");
    }

    /**
     * Verifies that an inferred-type local variable declaration with an
     * annotation generates the annotation before the {@code var} keyword.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void localVarInferredAnnotated() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        final JType deprecatedType = JType.named("java.lang.Deprecated");
        sources.createSourceFile("com.example", "LocalVarInferredAnnotated", sf -> {
            sf.class_("LocalVarInferredAnnotated", cc -> {
                cc.method("init", mc -> {
                    mc.body(b -> {
                        b.var("x", JExpr.decimal(42), lv -> {
                            lv.annotate(deprecatedType);
                        });
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "LocalVarInferredAnnotated");
        assertTrue(source.contains("@Deprecated"),
            "should contain annotation");
        assertTrue(source.contains("var x = 42;"),
            "should contain var-inferred local variable declaration");
    }

    /**
     * Verifies that an inferred-type local variable declaration with
     * the {@code final} modifier generates the correct syntax.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void localVarInferredFinal() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "LocalVarInferredFinal", sf -> {
            sf.class_("LocalVarInferredFinal", cc -> {
                cc.method("init", mc -> {
                    mc.body(b -> {
                        b.var("x", JExpr.decimal(42), lv -> {
                            lv.final_();
                        });
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "LocalVarInferredFinal");
        assertTrue(source.contains("final var x = 42;"),
            "should contain final var-inferred local variable declaration");
    }

    /**
     * Verifies that the builder overload with no modifications produces
     * identical output to the non-builder overload.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void localVarBuilderNoOp() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "LocalVarBuilderNoOp", sf -> {
            sf.class_("LocalVarBuilderNoOp", cc -> {
                cc.method("init", mc -> {
                    mc.body(b -> {
                        b.var(JType.INT, "x", JExpr.ZERO, lv -> {});
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "LocalVarBuilderNoOp");
        assertTrue(source.contains("int x = 0;"),
            "no-op builder should produce same output as non-builder overload");
    }

    /**
     * Verifies that a nested block generates opening and closing braces
     * surrounding the block body.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void nestedBlock() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "NestedBlock", sf -> {
            sf.class_("NestedBlock", cc -> {
                cc.method("scoped", mc -> {
                    mc.body(b -> {
                        b.block(inner -> {
                            inner.var(JType.INT, "y", JExpr.decimal(99));
                        });
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "NestedBlock");
        assertTrue(source.contains("{"), "should contain opening brace for nested block");
        assertTrue(source.contains("}"), "should contain closing brace for nested block");
        assertTrue(source.contains("int y = 99;"), "should contain statement inside nested block");
    }

    /**
     * Verifies that {@code break;} has no spurious space between the keyword
     * and the semicolon.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void breakNoExtraSpace() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "BreakSpace", sf -> {
            sf.class_("BreakSpace", cc -> {
                cc.method("test", mc -> {
                    mc.body(b -> {
                        b.while_(JExpr.TRUE, loop -> {
                            loop.break_();
                        });
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "BreakSpace");
        assertTrue(source.contains("break;"), "should contain break; without space");
        assertFalse(source.contains("break ;"), "should not contain space before semicolon in break");
    }

    /**
     * Verifies that {@code continue;} has no spurious space between the keyword
     * and the semicolon.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void continueNoExtraSpace() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "ContinueSpace", sf -> {
            sf.class_("ContinueSpace", cc -> {
                cc.method("test", mc -> {
                    mc.body(b -> {
                        b.while_(JExpr.TRUE, loop -> {
                            loop.continue_();
                        });
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "ContinueSpace");
        assertTrue(source.contains("continue;"), "should contain continue; without space");
        assertFalse(source.contains("continue ;"), "should not contain space before semicolon in continue");
    }

    /**
     * Verifies that the first method after a constructor body has correct
     * 4-space indentation, not 5-space indentation caused by a stale
     * token state from the closing brace.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void noExtraIndentAfterConstructor() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "IndentAfterCtor", sf -> {
            sf.class_("IndentAfterCtor", cc -> {
                cc.constructor(ctor -> {
                    ctor.body(b -> {});
                });
                cc.method("foo", mc -> {
                    mc.body(b -> {});
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "IndentAfterCtor");
        assertTrue(source.contains("    void foo()"),
                "method after constructor should have 4-space indentation");
        assertFalse(source.contains("     void foo()"),
                "method after constructor should not have 5-space indentation");
    }

    /**
     * Verifies that line comments and block comments generate the correct
     * comment syntax.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void lineAndBlockComments() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Comments", sf -> {
            sf.class_("Comments", cc -> {
                cc.method("documented", mc -> {
                    mc.body(b -> {
                        b.lineComment("this is a line comment");
                        b.blockComment("this is a block comment");
                        b.emit(JExpr.callPlain("doWork"));
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "Comments");
        assertTrue(source.contains("// this is a line comment"),
            "should contain line comment with correct text");
        assertTrue(source.contains("/* this is a block comment */"),
            "should contain block comment with correct text");
    }
}
