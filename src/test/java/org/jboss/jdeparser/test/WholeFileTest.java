package org.jboss.jdeparser.test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.jboss.jdeparser.JExpr;
import org.jboss.jdeparser.JExprs;
import org.jboss.jdeparser.JSources;
import org.jboss.jdeparser.JType;
import org.jboss.jdeparser.JTypes;
import org.jboss.jdeparser.SourceVersion;
import org.jboss.jdeparser.creator.ModifierFlag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests that compare complete generated source files against pre-created
 * expected output files stored in test resources.
 */
class WholeFileTest extends AbstractGeneratingTestCase {

    /**
     * Generates a simple POJO class with field, constructor, and getter,
     * then compares against the expected output.
     *
     * @throws IOException if source generation or resource loading fails
     */
    @Test
    void personClass() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Person", sf -> {
            sf.class_("Person", cc -> {
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
                cc.method("getName", mc -> {
                    mc.public_();
                    mc.returning(JType.STRING);
                    mc.body(b -> {
                        b.return_(JExprs.$v("name"));
                    });
                });
            });
        });
        sources.writeSources();
        assertMatchesExpected("Person");
    }

    /**
     * Generates an enum with constants, fields, constructor, and method,
     * then compares against the expected output.
     *
     * @throws IOException if source generation or resource loading fails
     */
    @Test
    void seasonEnum() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Season", sf -> {
            sf.enum_("Season", ec -> {
                ec.public_();
                ec.constant("SPRING", c -> c.arg(JExprs.str("warm")));
                ec.constant("SUMMER", c -> c.arg(JExprs.str("hot")));
                ec.constant("AUTUMN", c -> c.arg(JExprs.str("cool")));
                ec.constant("WINTER", c -> c.arg(JExprs.str("cold")));
                ec.field("description", fc -> {
                    fc.private_();
                    fc.final_();
                    fc.type(JType.STRING);
                });
                ec.constructor(con -> {
                    con.param("description", JType.STRING);
                    con.body(b -> {
                        b.emit(JExpr.THIS.field("description").assign(JExprs.$v("description")));
                    });
                });
                ec.method("getDescription", mc -> {
                    mc.public_();
                    mc.returning(JType.STRING);
                    mc.body(b -> {
                        b.return_(JExprs.$v("description"));
                    });
                });
            });
        });
        sources.writeSources();
        assertMatchesExpected("Season");
    }

    /**
     * Generates a generic interface with an abstract method and a default method,
     * then compares against the expected output.
     *
     * @throws IOException if source generation or resource loading fails
     */
    @Test
    void converterInterface() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Converter", sf -> {
            sf.interface_("Converter", ic -> {
                ic.public_();
                ic.typeParam("F", tp -> {});
                ic.typeParam("T", tp -> {});
                ic.method("convert", mc -> {
                    mc.returning(JTypes.typeNamed("T"));
                    mc.param("input", JTypes.typeNamed("F"));
                });
                ic.method("identity", mc -> {
                    mc.addFlag(ModifierFlag.DEFAULT);
                    mc.returning(JTypes.typeNamed("F"));
                    mc.param("input", JTypes.typeNamed("F"));
                    mc.body(b -> {
                        b.return_(JExprs.$v("input"));
                    });
                });
            });
        });
        sources.writeSources();
        assertMatchesExpected("Converter");
    }

    /**
     * Generates a record with compact constructor and method,
     * then compares against the expected output.
     *
     * @throws IOException if source generation or resource loading fails
     */
    @Test
    void rangeRecord() throws IOException {
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
                rc.method("size", mc -> {
                    mc.public_();
                    mc.returning(JType.INT);
                    mc.body(b -> {
                        b.return_(JExprs.$v("max").sub(JExprs.$v("min")));
                    });
                });
            });
        });
        sources.writeSources();
        assertMatchesExpected("Range");
    }

    /**
     * Loads an expected output file from test resources and compares it
     * against the generated source.
     *
     * @param fileName the file name (without extension for the generated file,
     *                 with .java suffix for the resource)
     * @throws IOException if the resource cannot be loaded
     */
    private void assertMatchesExpected(final String fileName) throws IOException {
        final String actual = getSource("com.example", fileName);
        final String expected = loadExpected(fileName + ".java");
        assertEquals(expected, actual, "Generated output for " + fileName + " should match expected file");
    }

    /**
     * Loads an expected file from the {@code expected/} test resource directory.
     *
     * @param resourceName the file name within the {@code expected/} directory
     * @return the file contents as a string
     * @throws IOException if the resource cannot be read
     */
    private static String loadExpected(final String resourceName) throws IOException {
        final String path = "expected/" + resourceName;
        try (InputStream is = WholeFileTest.class.getModule().getResourceAsStream(path)) {
            if (is == null) {
                throw new IOException("Expected resource not found: " + path);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
