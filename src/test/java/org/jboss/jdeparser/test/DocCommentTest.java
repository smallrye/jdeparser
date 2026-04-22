package org.jboss.jdeparser.test;

import java.io.IOException;

import org.jboss.jdeparser.JExpr;
import org.jboss.jdeparser.JExprs;
import org.jboss.jdeparser.JSources;
import org.jboss.jdeparser.JType;
import org.jboss.jdeparser.JTypes;
import org.jboss.jdeparser.SourceVersion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for all Javadoc/doc comment generation constructs.
 * <p>
 * Each test method exercises a specific documentation feature by generating
 * a source file containing that feature, then verifying the expected Javadoc
 * output appears in the generated source.
 */
class DocCommentTest extends AbstractGeneratingTestCase {

    /**
     * Verifies that a class-level doc comment produces correct Javadoc output
     * with opening and closing delimiters.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void classDocComment() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "ClassDoc", sf -> {
            sf.class_("ClassDoc", cc -> {
                cc.public_();
                cc.docComment(dc -> {
                    dc.text("A simple class.");
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "ClassDoc");
        assertTrue(source.contains("/**"), "should contain doc comment opening");
        assertTrue(source.contains("* A simple class."), "should contain doc comment body text");
        assertTrue(source.contains("*/"), "should contain doc comment closing");
    }

    /**
     * Verifies that a method-level doc comment produces Javadoc output
     * that appears before the method declaration.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void methodDocComment() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "MethodDoc", sf -> {
            sf.class_("MethodDoc", cc -> {
                cc.public_();
                cc.method("doSomething", mc -> {
                    mc.public_();
                    mc.docComment(dc -> {
                        dc.text("Does something useful.");
                    });
                    mc.body(b -> {});
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "MethodDoc");
        assertTrue(source.contains("/**"), "should contain doc comment opening");
        assertTrue(source.contains("Does something useful."), "should contain doc comment text");
        // The doc comment must appear before the method declaration
        final int docIndex = source.indexOf("/**");
        final int methodIndex = source.indexOf("public void doSomething()");
        assertTrue(docIndex < methodIndex, "doc comment should appear before method declaration");
    }

    /**
     * Verifies that a field-level doc comment produces Javadoc output
     * that appears before the field declaration.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void fieldDocComment() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "FieldDoc", sf -> {
            sf.class_("FieldDoc", cc -> {
                cc.public_();
                cc.field("value", fc -> {
                    fc.private_();
                    fc.type(JType.INT);
                    fc.docComment(dc -> {
                        dc.text("The stored value.");
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "FieldDoc");
        assertTrue(source.contains("/**"), "should contain doc comment opening");
        assertTrue(source.contains("The stored value."), "should contain doc comment text");
        // The doc comment must appear before the field declaration
        final int docIndex = source.indexOf("/**");
        final int fieldIndex = source.indexOf("private int value;");
        assertTrue(docIndex < fieldIndex, "doc comment should appear before field declaration");
    }

    /**
     * Verifies that the inline {@code {@return ...}} tag is generated correctly,
     * producing an inline return documentation tag.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void returnDocInline() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "ReturnInline", sf -> {
            sf.class_("ReturnInline", cc -> {
                cc.public_();
                cc.method("getValue", mc -> {
                    mc.public_();
                    mc.returning(JType.INT);
                    mc.docComment(dc -> {
                        dc.returnDoc("the result");
                    });
                    mc.body(b -> {
                        b.return_(JExpr.ZERO);
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "ReturnInline");
        assertTrue(source.contains("{@return the result}"), "should contain inline @return tag");
    }

    /**
     * Verifies that the {@code @return} block tag is generated correctly
     * as a traditional block tag rather than an inline tag.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void returnBlockTag() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "ReturnBlock", sf -> {
            sf.class_("ReturnBlock", cc -> {
                cc.public_();
                cc.method("getCount", mc -> {
                    mc.public_();
                    mc.returning(JType.INT);
                    mc.docComment(dc -> {
                        dc.returnBlockTag("the result");
                    });
                    mc.body(b -> {
                        b.return_(JExpr.ZERO);
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "ReturnBlock");
        assertTrue(source.contains("@return the result"), "should contain @return block tag");
    }

    /**
     * Verifies that the {@code @throws} tag is generated correctly with
     * the exception type and description.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void throwsTag() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "ThrowsDoc", sf -> {
            sf.class_("ThrowsDoc", cc -> {
                cc.public_();
                cc.method("readFile", mc -> {
                    mc.public_();
                    mc.throws_(JTypes.typeNamed("java.io.IOException"));
                    mc.docComment(dc -> {
                        dc.text("Reads a file.");
                        dc.throws_(JTypes.typeNamed("java.io.IOException"), "if I/O fails");
                    });
                    mc.body(b -> {});
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "ThrowsDoc");
        assertTrue(source.contains("@throws java.io.IOException if I/O fails"),
                "should contain @throws tag with type and description");
    }

    /**
     * Verifies that the {@code @see} tag is generated correctly with
     * the reference text.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void seeTag() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "SeeDoc", sf -> {
            sf.class_("SeeDoc", cc -> {
                cc.public_();
                cc.docComment(dc -> {
                    dc.text("A documented class.");
                    dc.see("OtherClass#method()");
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "SeeDoc");
        assertTrue(source.contains("@see OtherClass#method()"), "should contain @see tag");
    }

    /**
     * Verifies that the {@code @since} tag is generated correctly with
     * the version string.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void sinceTag() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "SinceDoc", sf -> {
            sf.class_("SinceDoc", cc -> {
                cc.public_();
                cc.docComment(dc -> {
                    dc.text("A versioned class.");
                    dc.since("1.0");
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "SinceDoc");
        assertTrue(source.contains("@since 1.0"), "should contain @since tag");
    }

    /**
     * Verifies that the {@code @deprecated} tag is generated correctly with
     * the deprecation description.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void deprecatedTag() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "DeprecatedDoc", sf -> {
            sf.class_("DeprecatedDoc", cc -> {
                cc.public_();
                cc.method("oldMethod", mc -> {
                    mc.public_();
                    mc.docComment(dc -> {
                        dc.text("An old method.");
                        dc.deprecated("Use newMethod instead");
                    });
                    mc.body(b -> {});
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "DeprecatedDoc");
        assertTrue(source.contains("@deprecated Use newMethod instead"),
                "should contain @deprecated tag with description");
    }

    /**
     * Verifies that the inline {@code {@code ...}} tag is generated correctly
     * within the doc comment body text.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void inlineCode() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "InlineCodeDoc", sf -> {
            sf.class_("InlineCodeDoc", cc -> {
                cc.public_();
                cc.docComment(dc -> {
                    dc.text("Returns ");
                    dc.inlineCode("null");
                    dc.text(" if not found.");
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "InlineCodeDoc");
        assertTrue(source.contains("{@code null}"), "should contain inline {@code} tag");
    }

    /**
     * Verifies that the inline {@code {@link ...}} tag is generated correctly
     * with a type reference and no label.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void inlineLink() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "InlineLinkDoc", sf -> {
            sf.class_("InlineLinkDoc", cc -> {
                cc.public_();
                cc.docComment(dc -> {
                    dc.text("See ");
                    dc.inlineLink(JType.STRING);
                    dc.text(" for details.");
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "InlineLinkDoc");
        assertTrue(source.contains("{@link java.lang.String}"), "should contain inline {@link} tag");
    }

    /**
     * Verifies that the inline {@code {@link ...}} tag is generated correctly
     * with both a type reference and a display label.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void inlineLinkWithLabel() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "InlineLinkLabelDoc", sf -> {
            sf.class_("InlineLinkLabelDoc", cc -> {
                cc.public_();
                cc.docComment(dc -> {
                    dc.text("See ");
                    dc.inlineLink(JType.STRING, "string type");
                    dc.text(" for details.");
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "InlineLinkLabelDoc");
        assertTrue(source.contains("{@link java.lang.String string type}"),
                "should contain inline {@link} tag with label");
    }

    /**
     * Verifies that documenting a parameter via {@code ParamCreator.docComment()}
     * causes an {@code @param} tag to appear in the enclosing method's Javadoc.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void paramDocAggregation() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "ParamDoc", sf -> {
            sf.class_("ParamDoc", cc -> {
                cc.public_();
                cc.method("compute", mc -> {
                    mc.public_();
                    mc.returning(JType.INT);
                    mc.docComment(dc -> {
                        dc.text("Computes a value.");
                    });
                    mc.param("x", JType.INT, p -> {
                        p.docComment(dc -> dc.text("the x"));
                    });
                    mc.body(b -> {
                        b.return_(JExprs.$v("x"));
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "ParamDoc");
        assertTrue(source.contains("@param x"), "should contain @param tag for parameter x");
    }

    /**
     * Verifies that a doc comment with multiple tags (text, return, throws, since)
     * produces all tags in the correct order in the generated Javadoc output.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void multipleTagsCombined() throws IOException {
        final JSources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "MultiTagDoc", sf -> {
            sf.class_("MultiTagDoc", cc -> {
                cc.public_();
                cc.method("process", mc -> {
                    mc.public_();
                    mc.returning(JType.STRING);
                    mc.throws_(JTypes.typeNamed("java.io.IOException"));
                    mc.docComment(dc -> {
                        dc.text("Processes the input data.");
                        dc.returnBlockTag("the processed result");
                        dc.throws_(JTypes.typeNamed("java.io.IOException"), "if processing fails");
                        dc.since("2.0");
                    });
                    mc.body(b -> {
                        b.return_(JExprs.str("done"));
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "MultiTagDoc");
        assertTrue(source.contains("Processes the input data."), "should contain body text");
        assertTrue(source.contains("@return the processed result"), "should contain @return tag");
        assertTrue(source.contains("@throws java.io.IOException if processing fails"),
                "should contain @throws tag");
        assertTrue(source.contains("@since 2.0"), "should contain @since tag");

        // Verify ordering: body text before all block tags
        final int textIndex = source.indexOf("Processes the input data.");
        final int returnIndex = source.indexOf("@return the processed result");
        final int throwsIndex = source.indexOf("@throws java.io.IOException if processing fails");
        final int sinceIndex = source.indexOf("@since 2.0");
        assertTrue(textIndex < returnIndex, "body text should appear before @return");
        assertTrue(returnIndex < throwsIndex, "@return should appear before @throws");
        assertTrue(throwsIndex < sinceIndex, "@throws should appear before @since");
    }
}
