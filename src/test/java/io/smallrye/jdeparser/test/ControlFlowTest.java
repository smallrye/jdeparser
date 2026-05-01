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
import io.smallrye.jdeparser.creator.BlockCreator;

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
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "WhileLoop", sf -> {
            sf.class_("WhileLoop", cc -> {
                cc.method("countdown", mc -> {
                    mc.param("x", Type.INT);
                    mc.body(b -> {
                        b.while_(Expr.$v("x").gt(Expr.ZERO), loop -> {
                            loop.emit(Expr.$v("x").dec());
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
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "DoWhileLoop", sf -> {
            sf.class_("DoWhileLoop", cc -> {
                cc.method("increment", mc -> {
                    mc.param("x", Type.INT);
                    mc.body(b -> {
                        b.doWhile(
                                loop -> loop.emit(Expr.inc(Expr.$v("x"))),
                                Expr.$v("x").gt(Expr.ZERO));
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
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "ForLoop", sf -> {
            sf.class_("ForLoop", cc -> {
                cc.method("iterate", mc -> {
                    mc.param("n", Type.INT);
                    mc.body(b -> {
                        b.for_(fb -> {
                            fb.init(Type.INT, "i", Expr.ZERO);
                            fb.condition(Expr.$v("i").lt(Expr.$v("n")));
                            fb.update(Expr.$v("i").inc());
                            fb.body(loop -> {
                                loop.emit(Expr.callPlain("println", Expr.$v("i")));
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
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "ForEachLoop", sf -> {
            sf.class_("ForEachLoop", cc -> {
                cc.method("process", mc -> {
                    mc.param("items", Type.INT.array());
                    mc.body(b -> {
                        b.forEach(Type.INT, "item", Expr.$v("items"), loop -> {
                            loop.emit(Expr.callPlain("println", Expr.$v("item")));
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
    void switchClassicStatement() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "SwitchStmt", sf -> {
            sf.class_("SwitchStmt", cc -> {
                cc.method("classify", mc -> {
                    mc.param("x", Type.INT);
                    mc.returning(Type.STRING);
                    mc.body(b -> {
                        b.switchClassic(Expr.$v("x"), sw -> {
                            sw.case_(Expr.ZERO, body -> {
                                body.return_(Expr.str("zero"));
                            });
                            sw.case_(Expr.ONE, body -> {
                                body.return_(Expr.str("one"));
                            });
                            sw.default_(body -> {
                                body.return_(Expr.str("other"));
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
     * Verifies that a modern arrow-style switch statement renders with arrow syntax
     * and single-statement bodies without braces.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void switchArrowStatement() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "SwitchArrow", sf -> {
            sf.class_("SwitchArrow", cc -> {
                cc.method("classify", mc -> {
                    mc.param("x", Type.INT);
                    mc.returning(Type.STRING);
                    mc.body(b -> {
                        b.switch_(Expr.$v("x"), sw -> {
                            sw.case_(Expr.ZERO, body -> {
                                body.return_(Expr.str("zero"));
                            });
                            sw.case_(Expr.ONE, body -> {
                                body.return_(Expr.str("one"));
                            });
                            sw.default_(body -> {
                                body.return_(Expr.str("other"));
                            });
                        });
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "SwitchArrow");
        assertTrue(source.contains("switch (x)"), "should contain switch selector");
        assertTrue(source.contains("case 0 ->"), "should contain arrow case 0 label");
        assertTrue(source.contains("case 1 ->"), "should contain arrow case 1 label");
        assertTrue(source.contains("default ->"), "should contain arrow default label");
        assertFalse(source.contains("case 0:"), "should not contain colon case label");
        assertTrue(source.contains("return \"zero\";"), "should contain case 0 body");
    }

    /**
     * Verifies that a modern switch with multi-value cases renders correctly.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void switchArrowMultiValueCase() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "SwitchMulti", sf -> {
            sf.class_("SwitchMulti", cc -> {
                cc.method("classify", mc -> {
                    mc.param("x", Type.INT);
                    mc.returning(Type.STRING);
                    mc.body(b -> {
                        b.switch_(Expr.$v("x"), sw -> {
                            sw.case_(java.util.List.of(Expr.decimal(1), Expr.decimal(2), Expr.decimal(3)), body -> {
                                body.return_(Expr.str("low"));
                            });
                            sw.default_(body -> {
                                body.return_(Expr.str("high"));
                            });
                        });
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "SwitchMulti");
        assertTrue(source.contains("case 1, 2, 3 ->"), "should contain multi-value arrow case");
    }

    /**
     * Verifies that arrow switch with a null case renders correctly.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void switchArrowNullCase() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "SwitchNull", sf -> {
            sf.class_("SwitchNull", cc -> {
                cc.method("classify", mc -> {
                    mc.param("x", Type.named("java.lang.String"));
                    mc.returning(Type.STRING);
                    mc.body(b -> {
                        b.switch_(Expr.$v("x"), sw -> {
                            sw.case_(Expr.NULL, body -> {
                                body.return_(Expr.str("null value"));
                            });
                            sw.default_(body -> {
                                body.return_(Expr.str("non-null"));
                            });
                        });
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "SwitchNull");
        assertTrue(source.contains("case null ->"), "should contain null arrow case");
    }

    /**
     * Verifies that classic switch rejects null case values.
     */
    @Test
    void switchClassicRejectsNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            final Sources sources = createSources(SourceVersion.JAVA_17);
            sources.createSourceFile("com.example", "SwitchNull", sf -> {
                sf.class_("SwitchNull", cc -> {
                    cc.method("test", mc -> {
                        mc.returning(Type.VOID);
                        mc.body(b -> {
                            b.switchClassic(Expr.$v("x"), sw -> {
                                sw.case_(Expr.NULL, body -> {
                                    body.break_();
                                });
                            });
                        });
                    });
                });
            });
        });
    }

    /**
     * Verifies that arrow switch with multi-statement body renders with braces.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void switchArrowBlockBody() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "SwitchBlock", sf -> {
            sf.class_("SwitchBlock", cc -> {
                cc.method("process", mc -> {
                    mc.param("x", Type.INT);
                    mc.returning(Type.VOID);
                    mc.body(b -> {
                        b.switch_(Expr.$v("x"), sw -> {
                            sw.case_(Expr.ZERO, body -> {
                                body.emit(Expr.$v("System").field("out").call("println", Expr.str("zero")));
                                body.break_();
                            });
                            sw.default_(body -> {
                                body.break_();
                            });
                        });
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "SwitchBlock");
        assertTrue(source.contains("case 0 -> {"), "multi-statement arrow case should use braces");
    }

    /**
     * Verifies that a {@code try-catch-finally} statement generates all three
     * clauses with correct structure.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void tryCatchFinally() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "TryCatchFinally", sf -> {
            sf.class_("TryCatchFinally", cc -> {
                cc.method("handle", mc -> {
                    mc.body(b -> {
                        b.try_(tb -> {
                            tb.body(tryBody -> {
                                tryBody.emit(Expr.callPlain("riskyOperation"));
                            });
                            tb.catch_(Type.named("java.lang.Exception"), "e", catchBody -> {
                                catchBody.emit(Expr.$v("e").call("printStackTrace"));
                            });
                            tb.finally_(finallyBody -> {
                                finallyBody.emit(Expr.callPlain("cleanup"));
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
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "TryWithResources", sf -> {
            sf.class_("TryWithResources", cc -> {
                cc.method("readFile", mc -> {
                    mc.body(b -> {
                        b.try_(tb -> {
                            tb.with(
                                    Type.named("java.io.InputStream"),
                                    "in",
                                    Expr.callPlain("openStream"));
                            tb.body(tryBody -> {
                                tryBody.emit(Expr.$v("in").call("read"));
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
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "SyncBlock", sf -> {
            sf.class_("SyncBlock", cc -> {
                cc.method("criticalSection", mc -> {
                    mc.body(b -> {
                        b.synchronized_(Expr.THIS, syncBody -> {
                            syncBody.emit(Expr.callPlain("doWork"));
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
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "LabeledBlock", sf -> {
            sf.class_("LabeledBlock", cc -> {
                cc.method("search", mc -> {
                    mc.body(b -> {
                        b.labeled("outer", (label, labeledBody) -> {
                            labeledBody.while_(Expr.TRUE, loop -> {
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
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "AssertStmt", sf -> {
            sf.class_("AssertStmt", cc -> {
                cc.method("validate", mc -> {
                    mc.param("x", Type.INT);
                    mc.body(b -> {
                        b.assert_(Expr.$v("x").gt(Expr.ZERO));
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
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "AssertMsg", sf -> {
            sf.class_("AssertMsg", cc -> {
                cc.method("validate", mc -> {
                    mc.param("x", Type.INT);
                    mc.body(b -> {
                        b.assert_(Expr.$v("x").gt(Expr.ZERO), Expr.str("must be positive"));
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
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "ThrowStmt", sf -> {
            sf.class_("ThrowStmt", cc -> {
                cc.method("fail", mc -> {
                    mc.body(b -> {
                        final Type type = Type.named("java.lang.Exception");
                        b.throw_(type.new_(List.of(new Expr[] {})));
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
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "BreakContinue", sf -> {
            sf.class_("BreakContinue", cc -> {
                cc.method("loop", mc -> {
                    mc.param("n", Type.INT);
                    mc.body(b -> {
                        b.for_(fb -> {
                            fb.init(Type.INT, "i", Expr.ZERO);
                            fb.condition(Expr.$v("i").lt(Expr.$v("n")));
                            fb.update(Expr.$v("i").inc());
                            fb.body(loop -> {
                                loop.if_(Expr.$v("i").eq(Expr.decimal(5)), BlockCreator::break_);
                                loop.if_(Expr.$v("i").eq(Expr.decimal(3)), BlockCreator::continue_);
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
        final Sources sources = createSources(SourceVersion.JAVA_17);
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
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "LocalVar", sf -> {
            sf.class_("LocalVar", cc -> {
                cc.method("init", mc -> {
                    mc.body(b -> {
                        b.var(Type.INT, "x", Expr.ZERO);
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
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "LocalVarInferred", sf -> {
            sf.class_("LocalVarInferred", cc -> {
                cc.method("init", mc -> {
                    mc.body(b -> {
                        b.var("x", Expr.decimal(42));
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
        final Sources sources = createSources(SourceVersion.JAVA_17);
        final Type suppressWarningsType = Type.named("java.lang.SuppressWarnings");
        sources.createSourceFile("com.example", "LocalVarAnnotated", sf -> {
            sf.class_("LocalVarAnnotated", cc -> {
                cc.method("init", mc -> {
                    mc.body(b -> {
                        b.var(Type.INT, "x", Expr.ZERO, lv -> {
                            lv.annotate(suppressWarningsType, a -> {
                                a.value(Expr.str("unchecked"));
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
        final Sources sources = createSources(SourceVersion.JAVA_17);
        final Type deprecatedType = Type.named("java.lang.Deprecated");
        sources.createSourceFile("com.example", "LocalVarMarker", sf -> {
            sf.class_("LocalVarMarker", cc -> {
                cc.method("init", mc -> {
                    mc.body(b -> {
                        b.var(Type.INT, "x", Expr.ZERO, lv -> {
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
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "LocalVarFinal", sf -> {
            sf.class_("LocalVarFinal", cc -> {
                cc.method("init", mc -> {
                    mc.body(b -> {
                        b.var(Type.INT, "x", Expr.ZERO, lv -> {
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
        final Sources sources = createSources(SourceVersion.JAVA_17);
        final Type suppressWarningsType = Type.named("java.lang.SuppressWarnings");
        sources.createSourceFile("com.example", "LocalVarAnnotatedFinal", sf -> {
            sf.class_("LocalVarAnnotatedFinal", cc -> {
                cc.method("init", mc -> {
                    mc.body(b -> {
                        b.var(Type.INT, "x", Expr.ZERO, lv -> {
                            lv.annotate(suppressWarningsType, a -> {
                                a.value(Expr.str("unchecked"));
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
        final Sources sources = createSources(SourceVersion.JAVA_17);
        final Type deprecatedType = Type.named("java.lang.Deprecated");
        sources.createSourceFile("com.example", "LocalVarInferredAnnotated", sf -> {
            sf.class_("LocalVarInferredAnnotated", cc -> {
                cc.method("init", mc -> {
                    mc.body(b -> {
                        b.var("x", Expr.decimal(42), lv -> {
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
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "LocalVarInferredFinal", sf -> {
            sf.class_("LocalVarInferredFinal", cc -> {
                cc.method("init", mc -> {
                    mc.body(b -> {
                        b.var("x", Expr.decimal(42), lv -> {
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
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "LocalVarBuilderNoOp", sf -> {
            sf.class_("LocalVarBuilderNoOp", cc -> {
                cc.method("init", mc -> {
                    mc.body(b -> {
                        b.var(Type.INT, "x", Expr.ZERO, lv -> {
                        });
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
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "NestedBlock", sf -> {
            sf.class_("NestedBlock", cc -> {
                cc.method("scoped", mc -> {
                    mc.body(b -> {
                        b.block(inner -> {
                            inner.var(Type.INT, "y", Expr.decimal(99));
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
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "BreakSpace", sf -> {
            sf.class_("BreakSpace", cc -> {
                cc.method("test", mc -> {
                    mc.body(b -> {
                        b.while_(Expr.TRUE, loop -> {
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
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "ContinueSpace", sf -> {
            sf.class_("ContinueSpace", cc -> {
                cc.method("test", mc -> {
                    mc.body(b -> {
                        b.while_(Expr.TRUE, loop -> {
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
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "IndentAfterCtor", sf -> {
            sf.class_("IndentAfterCtor", cc -> {
                cc.constructor(ctor -> {
                    ctor.body(b -> {
                    });
                });
                cc.method("foo", mc -> {
                    mc.body(b -> {
                    });
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
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Comments", sf -> {
            sf.class_("Comments", cc -> {
                cc.method("documented", mc -> {
                    mc.body(b -> {
                        b.lineComment("this is a line comment");
                        b.blockComment("this is a block comment");
                        b.emit(Expr.callPlain("doWork"));
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
