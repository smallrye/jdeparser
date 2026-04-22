package org.jboss.jdeparser.test;

import java.io.IOException;

import org.jboss.jdeparser.JExpr;
import org.jboss.jdeparser.JExprs;
import org.jboss.jdeparser.JSources;
import org.jboss.jdeparser.JType;
import org.jboss.jdeparser.JTypes;
import org.jboss.jdeparser.SourceVersion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Basic tests verifying source file generation produces output.
 */
class BasicSourceFileTest extends AbstractGeneratingTestCase {

    /**
     * Verifies that a minimal empty class generates correct package and class declaration.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void emptyClass() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Empty", sf -> {
            sf.class_("Empty", cc -> {
                cc.public_();
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "Empty");
        assertNotNull(source);
        assertTrue(source.contains("package com.example;"), "should contain package declaration");
        assertTrue(source.contains("public class Empty"), "should contain class declaration");
    }

    /**
     * Verifies that a class with a field generates correct field declaration.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void classWithField() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Holder", sf -> {
            sf.class_("Holder", cc -> {
                cc.public_();
                cc.field("name", fc -> {
                    fc.private_();
                    fc.final_();
                    fc.type(JType.STRING);
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "Holder");
        assertTrue(source.contains("private final java.lang.String name;"), "should contain field");
    }

    /**
     * Verifies that a class with a method body generates correct output.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void classWithMethod() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Greeter", sf -> {
            sf.class_("Greeter", cc -> {
                cc.public_();
                cc.method("greet", mc -> {
                    mc.public_();
                    mc.returning(JType.STRING);
                    mc.body(b -> {
                        b.return_(JExprs.str("Hello"));
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "Greeter");
        assertTrue(source.contains("public java.lang.String greet()"), "should contain method signature");
        assertTrue(source.contains("return \"Hello\""), "should contain return statement");
    }

    /**
     * Verifies that a constructor with parameters generates correct output.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void classWithConstructor() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Person", sf -> {
            sf.class_("Person", cc -> {
                cc.public_();
                cc.field("name", fc -> {
                    fc.private_();
                    fc.final_();
                    fc.type(JType.STRING);
                });
                cc.constructor(con -> {
                    con.public_();
                    con.param("name", JType.STRING);
                    con.body(b -> {
                        b.emit(JExpr.THIS.field("name").assign(JExprs.$v("name")));
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "Person");
        assertTrue(source.contains("Person(java.lang.String name)"), "should contain constructor");
        assertTrue(source.contains("this.name = name;"), "should contain assignment");
    }

    /**
     * Verifies that if/else control flow generates correct output.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void ifElseStatement() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Flow", sf -> {
            sf.class_("Flow", cc -> {
                cc.method("check", mc -> {
                    mc.returning(JType.STRING);
                    mc.param("x", JType.INT);
                    mc.body(b -> {
                        b.ifElse(
                            JExprs.$v("x").gt(JExpr.ZERO),
                            then -> then.return_(JExprs.str("positive")),
                            else_ -> else_.return_(JExprs.str("non-positive"))
                        );
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "Flow");
        assertTrue(source.contains("if"), "should contain if");
        assertTrue(source.contains("x > 0"), "should contain condition");
        assertTrue(source.contains("else"), "should contain else");
    }

    /**
     * Verifies that a for loop generates correct output.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void forLoop() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Loop", sf -> {
            sf.class_("Loop", cc -> {
                cc.method("count", mc -> {
                    mc.param("n", JType.INT);
                    mc.body(b -> {
                        b.for_(fb -> {
                            fb.init(JType.INT, "i", JExpr.ZERO);
                            fb.condition(JExprs.$v("i").lt(JExprs.$v("n")));
                            fb.update(JExprs.$v("i").postInc());
                            fb.body(loop -> {
                                loop.emit(JExprs.call("println", JExprs.$v("i")));
                            });
                        });
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "Loop");
        assertTrue(source.contains("for"), "should contain for");
        assertTrue(source.contains("int i = 0"), "should contain init");
        assertTrue(source.contains("i < n"), "should contain condition");
        assertTrue(source.contains("i++"), "should contain update");
    }

    /**
     * Verifies that a lambda expression generates correct output.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void lambdaExpression() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Lambdas", sf -> {
            sf.class_("Lambdas", cc -> {
                cc.field("fn", fc -> {
                    fc.type(JType.OBJECT);
                    fc.init(JExprs.lambda("x", JExprs.$v("x").call("toString")));
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "Lambdas");
        assertTrue(source.contains("x -> x.toString()"), "should contain lambda expression");
    }
}
