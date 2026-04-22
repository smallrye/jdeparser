package org.jboss.jdeparser.test;

import java.io.IOException;

import org.jboss.jdeparser.JExprs;
import org.jboss.jdeparser.JSources;
import org.jboss.jdeparser.JType;
import org.jboss.jdeparser.SourceVersion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for various type declarations: enum, interface, record, annotation.
 */
class TypeDeclarationTest extends AbstractGeneratingTestCase {

    /**
     * Verifies that an enum with constants generates correct output.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void enumDeclaration() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Color", sf -> {
            sf.enum_("Color", ec -> {
                ec.public_();
                ec.constant("RED", c -> {});
                ec.constant("GREEN", c -> {});
                ec.constant("BLUE", c -> {});
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "Color");
        assertTrue(source.contains("public enum Color"), "should contain enum declaration");
        assertTrue(source.contains("RED"), "should contain RED constant");
        assertTrue(source.contains("GREEN"), "should contain GREEN constant");
        assertTrue(source.contains("BLUE"), "should contain BLUE constant");
    }

    /**
     * Verifies that an enum constant with arguments generates correct output.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void enumConstantWithArgs() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Planet", sf -> {
            sf.enum_("Planet", ec -> {
                ec.public_();
                ec.constant("MERCURY", c -> {
                    c.arg(JExprs.decimal(3));
                    c.arg(JExprs.decimal(2440));
                });
                ec.constant("VENUS", c -> {
                    c.arg(JExprs.decimal(5));
                    c.arg(JExprs.decimal(6052));
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "Planet");
        assertTrue(source.contains("MERCURY(3, 2440)"), "should contain MERCURY with args");
        assertTrue(source.contains("VENUS(5, 6052)"), "should contain VENUS with args");
    }

    /**
     * Verifies that an interface with methods generates correct output.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void interfaceDeclaration() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Greeter", sf -> {
            sf.interface_("Greeter", ic -> {
                ic.public_();
                ic.method("greet", mc -> {
                    mc.returning(JType.STRING);
                    mc.param("name", JType.STRING);
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "Greeter");
        assertTrue(source.contains("public interface Greeter"), "should contain interface declaration");
        assertTrue(source.contains("java.lang.String greet(java.lang.String name);"),
            "should contain abstract method");
    }

    /**
     * Verifies that a record with components generates correct output.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void recordDeclaration() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Point", sf -> {
            sf.record_("Point", rc -> {
                rc.public_();
                rc.component("x", JType.INT);
                rc.component("y", JType.INT);
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "Point");
        assertTrue(source.contains("public record Point"), "should contain record declaration");
        assertTrue(source.contains("int x"), "should contain x component");
        assertTrue(source.contains("int y"), "should contain y component");
    }

    /**
     * Verifies that an annotation type declaration generates correct output.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void annotationTypeDeclaration() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "MyAnnotation", sf -> {
            sf.annotationInterface_("MyAnnotation", ac -> {
                ac.public_();
                ac.element("value", JType.STRING);
                ac.element("count", JType.INT, JExprs.decimal(0));
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "MyAnnotation");
        assertTrue(source.contains("public @interface MyAnnotation"),
            "should contain annotation type");
        assertTrue(source.contains("java.lang.String value();"), "should contain value element");
        assertTrue(source.contains("int count() default 0;"), "should contain count element with default");
    }
}
