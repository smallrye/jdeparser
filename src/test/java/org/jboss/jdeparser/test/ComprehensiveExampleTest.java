package org.jboss.jdeparser.test;

import java.io.IOException;

import org.jboss.jdeparser.JExpr;
import org.jboss.jdeparser.JExprs;
import org.jboss.jdeparser.JSources;
import org.jboss.jdeparser.JType;
import org.jboss.jdeparser.JTypes;
import org.jboss.jdeparser.SourceVersion;
import org.jboss.jdeparser.creator.ModifierFlag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Comprehensive integration test exercising the full API for a realistic class
 * with fields, constructor, methods, control flow, and nested constructs.
 */
class ComprehensiveExampleTest extends AbstractGeneratingTestCase {

    /**
     * Generates a class similar to the plan's example Greeter class with
     * field, constructor, and method.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void greeterClass() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Greeter", sf -> {
            sf.class_("Greeter", cc -> {
                cc.public_();
                cc.final_();

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

                cc.method("greet", mc -> {
                    mc.public_();
                    mc.returning(JType.STRING);
                    mc.body(b -> {
                        b.return_(JExprs.str("Hello, ").call("concat", JExprs.$v("name")));
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "Greeter");
        assertTrue(source.contains("public final class Greeter"), "class declaration");
        assertTrue(source.contains("private final java.lang.String name;"), "field");
        assertTrue(source.contains("public Greeter(java.lang.String name)"), "constructor");
        assertTrue(source.contains("this.name = name;"), "assignment");
        assertTrue(source.contains("public java.lang.String greet()"), "method");
        assertTrue(source.contains("return \"Hello, \".concat(name);"), "return");
    }

    /**
     * Generates a method with various control flow constructs.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void controlFlowMethod() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Flow", sf -> {
            sf.class_("Flow", cc -> {
                cc.method("process", mc -> {
                    mc.param("items", JType.INT.array());
                    mc.body(b -> {
                        // var total = 0
                        b.var(JType.INT, "total", JExpr.ZERO);

                        // for (int item : items)
                        b.forEach(JType.INT, "item", JExprs.$v("items"), loop -> {
                            loop.if_(JExprs.$v("item").gt(JExpr.ZERO), then -> {
                                then.emit(JExprs.$v("total").addAssign(JExprs.$v("item")));
                            });
                        });

                        // while (total > 100)
                        b.while_(JExprs.$v("total").gt(JExprs.decimal(100)), loop -> {
                            loop.emit(JExprs.$v("total").divAssign(JExprs.decimal(2)));
                        });

                        // do { ... } while (total < 0)
                        b.doWhile(
                            loop -> loop.emit(JExprs.$v("total").preInc()),
                            JExprs.$v("total").lt(JExpr.ZERO)
                        );

                        b.return_(JExprs.$v("total"));
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "Flow");
        assertTrue(source.contains("int total = 0;"), "var declaration");
        assertTrue(source.contains("for (int item : items)"), "for-each");
        assertTrue(source.contains("if (item > 0)"), "if");
        assertTrue(source.contains("total += item;"), "compound assignment");
        assertTrue(source.contains("while (total > 100)"), "while");
        assertTrue(source.contains("do"), "do-while");
        assertTrue(source.contains("return total;"), "return");
    }

    /**
     * Generates a try-catch-finally construct.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void tryCatchFinally() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "TryCatch", sf -> {
            sf.class_("TryCatch", cc -> {
                cc.method("read", mc -> {
                    mc.body(b -> {
                        b.try_(tb -> {
                            tb.body(tryBody -> {
                                tryBody.emit(JExprs.call("doWork"));
                            });
                            tb.catch_(JType.OBJECT, "e", catchBody -> {
                                catchBody.emit(JExprs.$v("e").call("printStackTrace"));
                            });
                            tb.finally_(finallyBody -> {
                                finallyBody.emit(JExprs.call("cleanup"));
                            });
                        });
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "TryCatch");
        assertTrue(source.contains("try"), "try");
        assertTrue(source.contains("doWork();"), "try body");
        assertTrue(source.contains("catch"), "catch");
        assertTrue(source.contains("e.printStackTrace();"), "catch body");
        assertTrue(source.contains("finally"), "finally");
        assertTrue(source.contains("cleanup();"), "finally body");
    }

    /**
     * Generates a class extending another and implementing an interface.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void classWithInheritance() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Child", sf -> {
            sf.class_("Child", cc -> {
                cc.public_();
                cc.extends_(JTypes.typeNamed("com.example.Parent"));
                cc.implements_(JTypes.typeNamed("java.io.Serializable"));
                cc.method("doStuff", mc -> {
                    mc.public_();
                    mc.body(b -> {
                        b.callSuper();
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "Child");
        assertTrue(source.contains("extends com.example.Parent"), "extends");
        assertTrue(source.contains("implements java.io.Serializable"), "implements");
        assertTrue(source.contains("super();"), "super call");
    }

    /**
     * Generates a sealed interface with permits clause.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void sealedInterface() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Shape", sf -> {
            sf.interface_("Shape", ic -> {
                ic.public_();
                ic.sealed_();
                ic.permits(JTypes.typeNamed("com.example.Circle"));
                ic.permits(JTypes.typeNamed("com.example.Rectangle"));
                ic.method("area", mc -> {
                    mc.returning(JType.DOUBLE);
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "Shape");
        assertTrue(source.contains("public sealed interface Shape"), "sealed interface");
        assertTrue(source.contains("permits"), "permits");
        assertTrue(source.contains("com.example.Circle"), "permitted type");
    }

    /**
     * Generates an enum with constructor, field, and method.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void enumWithMembers() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Direction", sf -> {
            sf.enum_("Direction", ec -> {
                ec.public_();
                ec.constant("NORTH", c -> {
                    c.arg(JExprs.decimal(0));
                    c.arg(JExprs.decimal(-1));
                });
                ec.constant("SOUTH", c -> {
                    c.arg(JExprs.decimal(0));
                    c.arg(JExprs.decimal(1));
                });
                ec.field("dx", fc -> {
                    fc.private_();
                    fc.final_();
                    fc.type(JType.INT);
                });
                ec.field("dy", fc -> {
                    fc.private_();
                    fc.final_();
                    fc.type(JType.INT);
                });
                ec.constructor(con -> {
                    con.param("dx", JType.INT);
                    con.param("dy", JType.INT);
                    con.body(b -> {
                        b.emit(JExpr.THIS.field("dx").assign(JExprs.$v("dx")));
                        b.emit(JExpr.THIS.field("dy").assign(JExprs.$v("dy")));
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "Direction");
        assertTrue(source.contains("public enum Direction"), "enum declaration");
        assertTrue(source.contains("NORTH(0, -1)"), "NORTH with args");
        assertTrue(source.contains("SOUTH(0, 1)"), "SOUTH with args");
        assertTrue(source.contains("private final int dx;"), "dx field");
        assertTrue(source.contains("Direction(int dx, int dy)"), "constructor");
    }

    /**
     * Generates a record with compact constructor.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void recordWithCompactConstructor() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Range", sf -> {
            sf.record_("Range", rc -> {
                rc.public_();
                rc.component("min", JType.INT);
                rc.component("max", JType.INT);
                rc.compactConstructor(b -> {
                    b.if_(JExprs.$v("min").gt(JExprs.$v("max")), then -> {
                        then.throw_(JExprs.new_(
                            JTypes.typeNamed("java.lang.IllegalArgumentException"),
                            JExprs.str("min > max")));
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "Range");
        assertTrue(source.contains("public record Range"), "record declaration");
        assertTrue(source.contains("int min"), "min component");
        assertTrue(source.contains("int max"), "max component");
        assertTrue(source.contains("Range"), "compact constructor name");
        assertTrue(source.contains("if (min > max)"), "validation");
    }

    /**
     * Generates a switch statement.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void switchStatement() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Switcher", sf -> {
            sf.class_("Switcher", cc -> {
                cc.method("describe", mc -> {
                    mc.param("x", JType.INT);
                    mc.returning(JType.STRING);
                    mc.body(b -> {
                        b.switch_(JExprs.$v("x"), sw -> {
                            sw.case_(JExpr.ZERO, body -> {
                                body.return_(JExprs.str("zero"));
                            });
                            sw.case_(JExpr.ONE, body -> {
                                body.return_(JExprs.str("one"));
                            });
                            sw.default_(body -> {
                                body.return_(JExprs.str("other"));
                            });
                        });
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "Switcher");
        assertTrue(source.contains("switch (x)"), "switch statement");
        assertTrue(source.contains("case 0:"), "case 0");
        assertTrue(source.contains("case 1:"), "case 1");
        assertTrue(source.contains("default:"), "default");
    }

    /**
     * Generates an interface with a default method.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void interfaceDefaultMethod() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Printable", sf -> {
            sf.interface_("Printable", ic -> {
                ic.public_();
                ic.method("print", mc -> {
                    mc.addFlag(ModifierFlag.DEFAULT);
                    mc.body(b -> {
                        b.emit(JExprs.call("println", JExpr.THIS.call("toString")));
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "Printable");
        assertTrue(source.contains("public interface Printable"), "interface declaration");
        assertTrue(source.contains("default void print()"), "default method");
    }
}
