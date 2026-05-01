package io.smallrye.jdeparser.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.smallrye.jdeparser.Expr;
import io.smallrye.jdeparser.SourceVersion;
import io.smallrye.jdeparser.Sources;
import io.smallrye.jdeparser.Type;
import io.smallrye.jdeparser.creator.BlockCreator;
import io.smallrye.jdeparser.creator.ModifierFlag;

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
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Greeter", sf -> {
            sf.class_("Greeter", cc -> {
                cc.public_();
                cc.final_();

                cc.field("name", fc -> {
                    fc.private_();
                    fc.final_();
                    fc.type(Type.STRING);
                });

                cc.constructor(con -> {
                    con.public_();
                    con.param("name", Type.STRING);
                    con.body(b -> {
                        b.emit(Expr.THIS.field("name").assign(Expr.$v("name")));
                    });
                });

                cc.method("greet", mc -> {
                    mc.public_();
                    mc.returning(Type.STRING);
                    mc.body(b -> {
                        b.return_(Expr.str("Hello, ").call("concat", Expr.$v("name")));
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "Greeter");
        assertTrue(source.contains("public final class Greeter"), "class declaration");
        assertTrue(source.contains("private final String name;"), "field");
        assertTrue(source.contains("public Greeter(String name)"), "constructor");
        assertTrue(source.contains("this.name = name;"), "assignment");
        assertTrue(source.contains("public String greet()"), "method");
        assertTrue(source.contains("return \"Hello, \".concat(name);"), "return");
    }

    /**
     * Generates a method with various control flow constructs.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void controlFlowMethod() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Flow", sf -> {
            sf.class_("Flow", cc -> {
                cc.method("process", mc -> {
                    mc.param("items", Type.INT.array());
                    mc.body(b -> {
                        // var total = 0
                        b.var(Type.INT, "total", Expr.ZERO);

                        // for (int item : items)
                        b.forEach(Type.INT, "item", Expr.$v("items"), loop -> {
                            loop.if_(Expr.$v("item").gt(Expr.ZERO), then -> {
                                then.emit(Expr.$v("total").addAssign(Expr.$v("item")));
                            });
                        });

                        // while (total > 100)
                        b.while_(Expr.$v("total").gt(Expr.decimal(100)), loop -> {
                            loop.emit(Expr.$v("total").divAssign(Expr.decimal(2)));
                        });

                        // do { ... } while (total < 0)
                        b.doWhile(
                                loop -> loop.emit(Expr.inc(Expr.$v("total"))),
                                Expr.$v("total").lt(Expr.ZERO));

                        b.return_(Expr.$v("total"));
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
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "TryCatch", sf -> {
            sf.class_("TryCatch", cc -> {
                cc.method("read", mc -> {
                    mc.body(b -> {
                        b.try_(tb -> {
                            tb.body(tryBody -> {
                                tryBody.emit(Expr.callPlain("doWork"));
                            });
                            tb.catch_(Type.OBJECT, "e", catchBody -> {
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
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Child", sf -> {
            sf.class_("Child", cc -> {
                cc.public_();
                cc.extends_(Type.named("com.example.Parent"));
                cc.implements_(Type.named("java.io.Serializable"));
                cc.method("doStuff", mc -> {
                    mc.public_();
                    mc.body(BlockCreator::callSuper);
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "Child");
        assertTrue(source.contains("extends Parent"), "extends");
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
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Shape", sf -> {
            sf.interface_("Shape", ic -> {
                ic.public_();
                ic.sealed_();
                ic.permits(Type.named("com.example.Circle"));
                ic.permits(Type.named("com.example.Rectangle"));
                ic.method("area", mc -> {
                    mc.returning(Type.DOUBLE);
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "Shape");
        assertTrue(source.contains("public sealed interface Shape"), "sealed interface");
        assertTrue(source.contains("permits"), "permits");
        assertTrue(source.contains("Circle"), "permitted type");
    }

    /**
     * Generates an enum with constructor, field, and method.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void enumWithMembers() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Direction", sf -> {
            sf.enum_("Direction", ec -> {
                ec.public_();
                ec.constant("NORTH", c -> {
                    c.arg(Expr.decimal(0));
                    c.arg(Expr.decimal(-1));
                });
                ec.constant("SOUTH", c -> {
                    c.arg(Expr.decimal(0));
                    c.arg(Expr.decimal(1));
                });
                ec.field("dx", fc -> {
                    fc.private_();
                    fc.final_();
                    fc.type(Type.INT);
                });
                ec.field("dy", fc -> {
                    fc.private_();
                    fc.final_();
                    fc.type(Type.INT);
                });
                ec.constructor(con -> {
                    con.param("dx", Type.INT);
                    con.param("dy", Type.INT);
                    con.body(b -> {
                        b.emit(Expr.THIS.field("dx").assign(Expr.$v("dx")));
                        b.emit(Expr.THIS.field("dy").assign(Expr.$v("dy")));
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
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Range", sf -> {
            sf.record_("Range", rc -> {
                rc.public_();
                rc.component("min", Type.INT);
                rc.component("max", Type.INT);
                rc.compactConstructor(b -> {
                    b.if_(Expr.$v("min").gt(Expr.$v("max")), then -> {
                        final Type type = Type.named("java.lang.IllegalArgumentException");
                        then.throw_(type.new_(List.of(new Expr[] { Expr.str("min > max") })));
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
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Switcher", sf -> {
            sf.class_("Switcher", cc -> {
                cc.method("describe", mc -> {
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
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Printable", sf -> {
            sf.interface_("Printable", ic -> {
                ic.public_();
                ic.method("print", mc -> {
                    mc.addFlag(ModifierFlag.DEFAULT);
                    mc.body(b -> {
                        b.emit(Expr.callPlain("println", Expr.THIS.call("toString")));
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
