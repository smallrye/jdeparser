package io.smallrye.jdeparser.test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import io.smallrye.jdeparser.DocReference;
import io.smallrye.jdeparser.Expr;
import io.smallrye.jdeparser.SourceVersion;
import io.smallrye.jdeparser.Sources;
import io.smallrye.jdeparser.Type;
import io.smallrye.jdeparser.creator.DocCommentCreator;
import io.smallrye.jdeparser.creator.DocInlineCreator;

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
        final Sources sources = createSources(SourceVersion.JAVA_17);
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
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "MethodDoc", sf -> {
            sf.class_("MethodDoc", cc -> {
                cc.public_();
                cc.method("doSomething", mc -> {
                    mc.public_();
                    mc.docComment(dc -> {
                        dc.text("Does something useful.");
                    });
                    mc.body(b -> {
                    });
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
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "FieldDoc", sf -> {
            sf.class_("FieldDoc", cc -> {
                cc.public_();
                cc.field("value", fc -> {
                    fc.private_();
                    fc.type(Type.INT);
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
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "ReturnInline", sf -> {
            sf.class_("ReturnInline", cc -> {
                cc.public_();
                cc.method("getValue", mc -> {
                    mc.public_();
                    mc.returning(Type.INT);
                    mc.docComment(dc -> {
                        dc.returnInline("the result");
                    });
                    mc.body(b -> {
                        b.return_(Expr.ZERO);
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "ReturnInline");
        assertTrue(source.contains("{@return the result}"), "should contain inline @return tag");
    }

    /**
     * Verifies that the inline {@code {@return ...}} tag supports rich
     * inline content such as {@code {@link}} and {@code {@code}} tags.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void returnInlineWithInlineContent() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "ReturnInlineRich", sf -> {
            sf.class_("ReturnInlineRich", cc -> {
                cc.public_();
                cc.method("getName", mc -> {
                    mc.public_();
                    mc.returning(Type.STRING);
                    mc.docComment(dc -> {
                        dc.returnInline(ic -> {
                            ic.text("the name, or ");
                            ic.code("null");
                            ic.text(" if not set (see ");
                            ic.link(Type.STRING);
                            ic.text(")");
                        });
                    });
                    mc.body(b -> {
                        b.return_(Expr.NULL);
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "ReturnInlineRich");
        assertTrue(source.contains("{@return the name, or {@code null} if not set (see {@link String})}"),
                "should contain inline @return tag with nested inline content");
    }

    /**
     * Verifies that the {@code @return} block tag is generated correctly
     * as a traditional block tag rather than an inline tag.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void returnBlockTag() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "ReturnBlock", sf -> {
            sf.class_("ReturnBlock", cc -> {
                cc.public_();
                cc.method("getCount", mc -> {
                    mc.public_();
                    mc.returning(Type.INT);
                    mc.docComment(dc -> {
                        dc.return_("the result");
                    });
                    mc.body(b -> {
                        b.return_(Expr.ZERO);
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
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "ThrowsDoc", sf -> {
            sf.class_("ThrowsDoc", cc -> {
                cc.public_();
                cc.method("readFile", mc -> {
                    mc.public_();
                    mc.throws_(Type.named("java.io.IOException"));
                    mc.docComment(dc -> {
                        dc.text("Reads a file.");
                        dc.throws_(Type.named("java.io.IOException"), "if I/O fails");
                    });
                    mc.body(b -> {
                    });
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
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "SeeDoc", sf -> {
            sf.class_("SeeDoc", cc -> {
                cc.public_();
                cc.docComment(dc -> {
                    dc.text("A documented class.");
                    dc.see(Type.named("OtherClass").docRef("method()"));
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
        final Sources sources = createSources(SourceVersion.JAVA_17);
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
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "DeprecatedDoc", sf -> {
            sf.class_("DeprecatedDoc", cc -> {
                cc.public_();
                cc.method("oldMethod", mc -> {
                    mc.public_();
                    mc.docComment(dc -> {
                        dc.text("An old method.");
                        dc.deprecated("Use newMethod instead");
                    });
                    mc.body(b -> {
                    });
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
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "InlineCodeDoc", sf -> {
            sf.class_("InlineCodeDoc", cc -> {
                cc.public_();
                cc.docComment(dc -> {
                    dc.text("Returns ");
                    dc.code("null");
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
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "InlineLinkDoc", sf -> {
            sf.class_("InlineLinkDoc", cc -> {
                cc.public_();
                cc.docComment(dc -> {
                    dc.text("See ");
                    dc.link(Type.STRING);
                    dc.text(" for details.");
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "InlineLinkDoc");
        assertTrue(source.contains("{@link String}"), "should contain inline {@link} tag with resolved name");
    }

    /**
     * Verifies that the inline {@code {@link ...}} tag is generated correctly
     * with both a type reference and a display label.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void inlineLinkWithLabel() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "InlineLinkLabelDoc", sf -> {
            sf.class_("InlineLinkLabelDoc", cc -> {
                cc.public_();
                cc.docComment(dc -> {
                    dc.text("See ");
                    dc.link(Type.STRING, "string type");
                    dc.text(" for details.");
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "InlineLinkLabelDoc");
        assertTrue(source.contains("{@link String string type}"),
                "should contain inline {@link} tag with resolved name and label");
    }

    /**
     * Verifies that the inline {@code {@linkplain ...}} tag is generated correctly
     * with a type reference and no label, using resolved type names.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void linkPlain() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "LinkPlainDoc", sf -> {
            sf.class_("LinkPlainDoc", cc -> {
                cc.public_();
                cc.docComment(dc -> {
                    dc.text("See ");
                    dc.linkPlain(Type.STRING);
                    dc.text(" for details.");
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "LinkPlainDoc");
        assertTrue(source.contains("{@linkplain String}"),
                "should contain inline {@linkplain} tag with resolved name");
    }

    /**
     * Verifies that the inline {@code {@linkplain ...}} tag is generated correctly
     * with both a type reference and a display label.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void linkPlainWithLabel() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "LinkPlainLabelDoc", sf -> {
            sf.class_("LinkPlainLabelDoc", cc -> {
                cc.public_();
                cc.docComment(dc -> {
                    dc.text("See ");
                    dc.linkPlain(Type.STRING, "string type");
                    dc.text(" for details.");
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "LinkPlainLabelDoc");
        assertTrue(source.contains("{@linkplain String string type}"),
                "should contain inline {@linkplain} tag with resolved name and label");
    }

    /**
     * Verifies that documenting a parameter via {@code ParamCreator.docComment()}
     * causes an {@code @param} tag to appear in the enclosing method's Javadoc.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void paramDocAggregation() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "ParamDoc", sf -> {
            sf.class_("ParamDoc", cc -> {
                cc.public_();
                cc.method("compute", mc -> {
                    mc.public_();
                    mc.returning(Type.INT);
                    mc.docComment(dc -> {
                        dc.text("Computes a value.");
                    });
                    mc.param("x", Type.INT, p -> {
                        p.docComment(dc -> dc.text("the x"));
                    });
                    mc.body(b -> {
                        b.return_(Expr.$v("x"));
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
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "MultiTagDoc", sf -> {
            sf.class_("MultiTagDoc", cc -> {
                cc.public_();
                cc.method("process", mc -> {
                    mc.public_();
                    mc.returning(Type.STRING);
                    mc.throws_(Type.named("java.io.IOException"));
                    mc.docComment(dc -> {
                        dc.text("Processes the input data.");
                        dc.return_("the processed result");
                        dc.throws_(Type.named("java.io.IOException"), "if processing fails");
                        dc.since("2.0");
                    });
                    mc.body(b -> {
                        b.return_(Expr.str("done"));
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

    /**
     * Verifies that {@code {@link Type#member}} is generated correctly
     * using a {@link DocReference} with resolved type names.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void inlineLinkWithDocRef() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "LinkRefDoc", sf -> {
            sf.class_("LinkRefDoc", cc -> {
                cc.public_();
                cc.docComment(dc -> {
                    dc.text("See ");
                    dc.link(Type.STRING.docRef("length()"));
                    dc.text(".");
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "LinkRefDoc");
        assertTrue(source.contains("{@link String#length()}"),
                "should contain inline {@link} tag with resolved type and member");
    }

    /**
     * Verifies that {@code {@link Type#member label}} is generated correctly
     * using a {@link DocReference} with a display label.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void inlineLinkWithDocRefAndLabel() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "LinkRefLabelDoc", sf -> {
            sf.class_("LinkRefLabelDoc", cc -> {
                cc.public_();
                cc.docComment(dc -> {
                    dc.text("See ");
                    dc.link(Type.STRING.docRef("length()"), "string length");
                    dc.text(".");
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "LinkRefLabelDoc");
        assertTrue(source.contains("{@link String#length() string length}"),
                "should contain inline {@link} tag with resolved type, member, and label");
    }

    /**
     * Verifies that {@code {@linkplain Type#member}} is generated correctly
     * using a {@link DocReference}.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void linkPlainWithDocRef() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "LinkPlainRefDoc", sf -> {
            sf.class_("LinkPlainRefDoc", cc -> {
                cc.public_();
                cc.docComment(dc -> {
                    dc.text("See ");
                    dc.linkPlain(Type.STRING.docRef("length()"));
                    dc.text(".");
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "LinkPlainRefDoc");
        assertTrue(source.contains("{@linkplain String#length()}"),
                "should contain inline {@linkplain} tag with resolved type and member");
    }

    /**
     * Verifies that {@code {@linkplain Type#member label}} is generated correctly
     * using a {@link DocReference} with a display label.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void linkPlainWithDocRefAndLabel() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "LinkPlainRefLabelDoc", sf -> {
            sf.class_("LinkPlainRefLabelDoc", cc -> {
                cc.public_();
                cc.docComment(dc -> {
                    dc.text("See ");
                    dc.linkPlain(Type.STRING.docRef("length()"), "string length");
                    dc.text(".");
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "LinkPlainRefLabelDoc");
        assertTrue(source.contains("{@linkplain String#length() string length}"),
                "should contain inline {@linkplain} tag with resolved type, member, and label");
    }

    /**
     * Verifies that {@code @see Type} is generated correctly with a resolved
     * type name when using the {@link Type} overload.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void seeTagWithType() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "SeeTypeDoc", sf -> {
            sf.class_("SeeTypeDoc", cc -> {
                cc.public_();
                cc.docComment(dc -> {
                    dc.text("A documented class.");
                    dc.see(Type.STRING);
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "SeeTypeDoc");
        assertTrue(source.contains("@see String"),
                "should contain @see tag with resolved type name");
    }

    /**
     * Verifies that {@code @see Type#member} is generated correctly with a
     * resolved type name when using the {@link DocReference} overload.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void seeTagWithDocRef() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "SeeRefDoc", sf -> {
            sf.class_("SeeRefDoc", cc -> {
                cc.public_();
                cc.docComment(dc -> {
                    dc.text("A documented class.");
                    dc.see(Type.STRING.docRef("length()"));
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "SeeRefDoc");
        assertTrue(source.contains("@see String#length()"),
                "should contain @see tag with resolved type and member");
    }

    /**
     * Verifies that a non-{@code java.lang} type in a doc reference is
     * fully qualified when not explicitly imported.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void docRefWithNonImportedType() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        final Type ioException = Type.named("java.io.IOException");
        sources.createSourceFile("com.example", "DocRefNonImported", sf -> {
            sf.class_("DocRefNonImported", cc -> {
                cc.public_();
                cc.docComment(dc -> {
                    dc.text("See ");
                    dc.link(ioException.docRef("getMessage()"));
                    dc.text(".");
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "DocRefNonImported");
        assertTrue(source.contains("{@link java.io.IOException#getMessage()}"),
                "should fully qualify non-imported type in doc reference");
    }

    /**
     * Verifies that the {@code @return} block tag supports rich inline content
     * via the {@code Consumer<DocInlineCreator>} overload.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void returnWithInlineContent() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "ReturnRichDoc", sf -> {
            sf.class_("ReturnRichDoc", cc -> {
                cc.public_();
                cc.method("getName", mc -> {
                    mc.public_();
                    mc.returning(Type.STRING);
                    mc.docComment(dc -> {
                        dc.text("Gets the name.");
                        dc.return_(c -> {
                            c.text("the name as a ");
                            c.link(Type.STRING);
                        });
                    });
                    mc.body(b -> {
                        b.return_(Expr.str("name"));
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "ReturnRichDoc");
        assertTrue(source.contains("@return the name as a {@link String}"),
                "should contain @return tag with inline {@link} content");
    }

    /**
     * Verifies that the {@code @throws} block tag supports rich inline content
     * via the {@code Consumer<DocInlineCreator>} overload.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void throwsWithInlineContent() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        final Type ioException = Type.named("java.io.IOException");
        sources.createSourceFile("com.example", "ThrowsRichDoc", sf -> {
            sf.class_("ThrowsRichDoc", cc -> {
                cc.public_();
                cc.method("readFile", mc -> {
                    mc.public_();
                    mc.throws_(ioException);
                    mc.docComment(dc -> {
                        dc.text("Reads a file.");
                        dc.throws_(ioException, c -> {
                            c.text("if the file is not ");
                            c.code("readable");
                        });
                    });
                    mc.body(b -> {
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "ThrowsRichDoc");
        assertTrue(source.contains("@throws java.io.IOException if the file is not {@code readable}"),
                "should contain @throws tag with inline {@code} content");
    }

    /**
     * Verifies that the {@code @deprecated} block tag supports rich inline content
     * via the {@code Consumer<DocInlineCreator>} overload.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void deprecatedWithInlineContent() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "DeprecatedRichDoc", sf -> {
            sf.class_("DeprecatedRichDoc", cc -> {
                cc.public_();
                cc.method("oldMethod", mc -> {
                    mc.public_();
                    mc.docComment(dc -> {
                        dc.text("An old method.");
                        dc.deprecated(c -> {
                            c.text("Use ");
                            c.link(Type.STRING.docRef("strip()"));
                            c.text(" instead.");
                        });
                    });
                    mc.body(b -> {
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "DeprecatedRichDoc");
        assertTrue(source.contains("@deprecated Use {@link String#strip()} instead."),
                "should contain @deprecated tag with inline {@link} content");
    }

    // ---- Tests for new standard doclet tags ----

    /**
     * Verifies that the {@code @author} tag is generated correctly on a class.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void authorTag() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "AuthorDoc", sf -> {
            sf.class_("AuthorDoc", cc -> {
                cc.public_();
                cc.docComment(dc -> {
                    dc.text("A documented class.");
                    dc.author("John Doe");
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "AuthorDoc");
        assertTrue(source.contains("@author John Doe"), "should contain @author tag");
    }

    /**
     * Verifies that the {@code @author} tag is rejected on a method.
     */
    @Test
    void authorTagRejectedOnMethod() {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        assertThrows(IllegalStateException.class, () -> {
            sources.createSourceFile("com.example", "AuthorBad", sf -> {
                sf.class_("AuthorBad", cc -> {
                    cc.public_();
                    cc.method("foo", mc -> {
                        mc.public_();
                        mc.docComment(dc -> {
                            dc.author("Nobody");
                        });
                        mc.body(b -> {
                        });
                    });
                });
            });
        });
    }

    /**
     * Verifies that the inline {@code {@docRoot}} tag is generated correctly.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void docRootTag() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "DocRootDoc", sf -> {
            sf.class_("DocRootDoc", cc -> {
                cc.public_();
                cc.docComment(dc -> {
                    dc.text("See ");
                    dc.docRoot();
                    dc.text("/overview.html");
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "DocRootDoc");
        assertTrue(source.contains("{@docRoot}"), "should contain {@docRoot} inline tag");
    }

    /**
     * Verifies that the {@code @hidden} tag is generated correctly on a method.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void hiddenTag() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "HiddenDoc", sf -> {
            sf.class_("HiddenDoc", cc -> {
                cc.public_();
                cc.method("internalMethod", mc -> {
                    mc.public_();
                    mc.docComment(dc -> {
                        dc.text("Internal method.");
                        dc.hidden();
                    });
                    mc.body(b -> {
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "HiddenDoc");
        assertTrue(source.contains("@hidden"), "should contain @hidden tag");
    }

    /**
     * Verifies that the {@code @hidden} tag is rejected when the source version
     * does not support it.
     */
    @Test
    void hiddenTagRejectedBeforeJava9() {
        final Sources sources = createSources(SourceVersion.JAVA_8);
        assertThrows(IllegalStateException.class, () -> {
            sources.createSourceFile("com.example", "HiddenBad", sf -> {
                sf.class_("HiddenBad", cc -> {
                    cc.public_();
                    cc.docComment(DocCommentCreator::hidden);
                });
            });
        });
    }

    /**
     * Verifies that the {@code @hidden} tag is rejected on a constructor.
     */
    @Test
    void hiddenTagRejectedOnConstructor() {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        assertThrows(IllegalStateException.class, () -> {
            sources.createSourceFile("com.example", "HiddenCtorBad", sf -> {
                sf.class_("HiddenCtorBad", cc -> {
                    cc.public_();
                    cc.constructor(ctor -> {
                        ctor.public_();
                        ctor.docComment(DocCommentCreator::hidden);
                        ctor.body(b -> {
                        });
                    });
                });
            });
        });
    }

    /**
     * Verifies that the inline {@code {@index ...}} tag is generated correctly.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void indexTag() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "IndexDoc", sf -> {
            sf.class_("IndexDoc", cc -> {
                cc.public_();
                cc.docComment(dc -> {
                    dc.text("The ");
                    dc.index("foobar");
                    dc.text(" concept.");
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "IndexDoc");
        assertTrue(source.contains("{@index foobar}"), "should contain {@index} inline tag");
    }

    /**
     * Verifies that the inline {@code {@index ...}} tag with a description
     * is generated correctly.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void indexTagWithDescription() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "IndexDescDoc", sf -> {
            sf.class_("IndexDescDoc", cc -> {
                cc.public_();
                cc.docComment(dc -> {
                    dc.index("foobar", "a key concept");
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "IndexDescDoc");
        assertTrue(source.contains("{@index foobar a key concept}"),
                "should contain {@index} tag with description");
    }

    /**
     * Verifies that the inline {@code {@inheritDoc}} tag is generated correctly
     * on a method.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void inheritDocTag() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "InheritDocDoc", sf -> {
            sf.class_("InheritDocDoc", cc -> {
                cc.public_();
                cc.method("toString", mc -> {
                    mc.public_();
                    mc.returning(Type.STRING);
                    mc.docComment(DocInlineCreator::inheritDoc);
                    mc.body(b -> {
                        b.return_(Expr.str(""));
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "InheritDocDoc");
        assertTrue(source.contains("{@inheritDoc}"), "should contain {@inheritDoc} tag");
    }

    /**
     * Verifies that the {@code {@inheritDoc supertype}} tag is generated correctly
     * with a resolved supertype name.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void inheritDocWithSupertype() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "InheritDocSuperDoc", sf -> {
            sf.class_("InheritDocSuperDoc", cc -> {
                cc.public_();
                cc.method("hashCode", mc -> {
                    mc.public_();
                    mc.returning(Type.INT);
                    mc.docComment(dc -> {
                        dc.inheritDoc(Type.named("java.lang.Object"));
                    });
                    mc.body(b -> {
                        b.return_(Expr.ZERO);
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "InheritDocSuperDoc");
        assertTrue(source.contains("{@inheritDoc Object}"),
                "should contain {@inheritDoc} with resolved supertype name");
    }

    /**
     * Verifies that the {@code {@inheritDoc}} tag is rejected on a class.
     */
    @Test
    void inheritDocRejectedOnClass() {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        assertThrows(IllegalStateException.class, () -> {
            sources.createSourceFile("com.example", "InheritDocBad", sf -> {
                sf.class_("InheritDocBad", cc -> {
                    cc.public_();
                    cc.docComment(DocInlineCreator::inheritDoc);
                });
            });
        });
    }

    /**
     * Verifies that the inline {@code {@literal ...}} tag is generated correctly.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void literalTag() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "LiteralDoc", sf -> {
            sf.class_("LiteralDoc", cc -> {
                cc.public_();
                cc.docComment(dc -> {
                    dc.text("Use ");
                    dc.literal("<em>this</em>");
                    dc.text(" carefully.");
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "LiteralDoc");
        assertTrue(source.contains("{@literal <em>this</em>}"),
                "should contain {@literal} inline tag");
    }

    /**
     * Verifies that the {@code @serial} tag is generated correctly on a field.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void serialTag() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "SerialDoc", sf -> {
            sf.class_("SerialDoc", cc -> {
                cc.public_();
                cc.field("serialVersionUID", fc -> {
                    fc.private_();
                    fc.type(Type.LONG);
                    fc.docComment(dc -> {
                        dc.text("Serial version UID.");
                        dc.serial("include");
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "SerialDoc");
        assertTrue(source.contains("@serial include"), "should contain @serial tag");
    }

    /**
     * Verifies that the {@code @serial} tag supports rich inline content
     * for describing the serialized form of a field.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void serialTagWithInlineContent() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "SerialRich", sf -> {
            sf.class_("SerialRich", cc -> {
                cc.public_();
                cc.field("name", fc -> {
                    fc.private_();
                    fc.type(Type.STRING);
                    fc.docComment(dc -> {
                        dc.text("The person's name.");
                        dc.serial(ic -> {
                            ic.text("A non-null ");
                            ic.link(Type.STRING);
                            ic.text(" value.");
                        });
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "SerialRich");
        assertTrue(source.contains("@serial A non-null {@link String} value."),
                "should contain @serial tag with inline content");
    }

    /**
     * Verifies that the {@code @serial include} tag is generated correctly
     * on a type declaration.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void serialIncludeTag() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "SerialInc", sf -> {
            sf.class_("SerialInc", cc -> {
                cc.public_();
                cc.docComment(dc -> {
                    dc.text("A serializable class.");
                    dc.serialInclude();
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "SerialInc");
        assertTrue(source.contains("@serial include"),
                "should contain @serial include tag");
    }

    /**
     * Verifies that the {@code @serial exclude} tag is generated correctly
     * on a type declaration.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void serialExcludeTag() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "SerialExc", sf -> {
            sf.class_("SerialExc", cc -> {
                cc.public_();
                cc.docComment(dc -> {
                    dc.text("A non-serializable class.");
                    dc.serialExclude();
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "SerialExc");
        assertTrue(source.contains("@serial exclude"),
                "should contain @serial exclude tag");
    }

    /**
     * Verifies that {@code serialInclude()} is rejected on a field
     * (only valid on package and type).
     */
    @Test
    void serialIncludeRejectedOnField() {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        assertThrows(IllegalStateException.class, () -> {
            sources.createSourceFile("com.example", "SerialIncBad", sf -> {
                sf.class_("SerialIncBad", cc -> {
                    cc.public_();
                    cc.field("x", fc -> {
                        fc.type(Type.INT);
                        fc.docComment(dc -> {
                            dc.serialInclude();
                        });
                    });
                });
            });
        });
    }

    /**
     * Verifies that {@code serialExclude()} is rejected on a method
     * (only valid on package and type).
     */
    @Test
    void serialExcludeRejectedOnMethod() {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        assertThrows(IllegalStateException.class, () -> {
            sources.createSourceFile("com.example", "SerialExcBad", sf -> {
                sf.class_("SerialExcBad", cc -> {
                    cc.public_();
                    cc.method("foo", mc -> {
                        mc.public_();
                        mc.docComment(dc -> {
                            dc.serialExclude();
                        });
                        mc.body(b -> {
                        });
                    });
                });
            });
        });
    }

    /**
     * Verifies that the {@code @serialData} tag is generated correctly on a method.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void serialDataTag() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "SerialDataDoc", sf -> {
            sf.class_("SerialDataDoc", cc -> {
                cc.public_();
                cc.method("writeObject", mc -> {
                    mc.private_();
                    mc.docComment(dc -> {
                        dc.text("Custom serialization.");
                        dc.serialData("The default fields followed by a count.");
                    });
                    mc.body(b -> {
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "SerialDataDoc");
        assertTrue(source.contains("@serialData The default fields followed by a count."),
                "should contain @serialData tag");
    }

    /**
     * Verifies that the {@code @serialField} tag is generated correctly on a field.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void serialFieldTag() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "SerialFieldDoc", sf -> {
            sf.class_("SerialFieldDoc", cc -> {
                cc.public_();
                cc.field("serialPersistentFields", fc -> {
                    fc.private_();
                    fc.type(Type.named("java.io.ObjectStreamField").array());
                    fc.docComment(dc -> {
                        dc.text("Serializable fields.");
                        dc.serialField("name", Type.STRING, "the person's name");
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "SerialFieldDoc");
        assertTrue(source.contains("@serialField name String the person's name"),
                "should contain @serialField tag with field name, type, and description");
    }

    /**
     * Verifies that the inline {@code {@summary ...}} tag is generated correctly.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void summaryTag() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "SummaryDoc", sf -> {
            sf.class_("SummaryDoc", cc -> {
                cc.public_();
                cc.docComment(dc -> {
                    dc.summary("A brief summary.");
                    dc.text(" More details follow.");
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "SummaryDoc");
        assertTrue(source.contains("{@summary A brief summary.}"),
                "should contain {@summary} inline tag");
    }

    /**
     * Verifies that the inline {@code {@systemProperty ...}} tag is generated correctly.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void systemPropertyTag() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "SysPropDoc", sf -> {
            sf.class_("SysPropDoc", cc -> {
                cc.public_();
                cc.docComment(dc -> {
                    dc.text("The property ");
                    dc.systemProperty("java.home");
                    dc.text(" points to the JDK.");
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "SysPropDoc");
        assertTrue(source.contains("{@systemProperty java.home}"),
                "should contain {@systemProperty} inline tag");
    }

    /**
     * Verifies that the inline {@code {@value}} tag (no argument) is generated correctly.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void valueTag() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "ValueDoc", sf -> {
            sf.class_("ValueDoc", cc -> {
                cc.public_();
                cc.field("MAX", fc -> {
                    fc.public_();
                    fc.static_();
                    fc.final_();
                    fc.type(Type.INT);
                    fc.init(Expr.decimal(100));
                    fc.docComment(dc -> {
                        dc.text("The maximum value is ");
                        dc.value();
                        dc.text(".");
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "ValueDoc");
        assertTrue(source.contains("{@value}"), "should contain {@value} inline tag");
    }

    /**
     * Verifies that the inline {@code {@value ref}} tag with a reference
     * is generated correctly.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void valueTagWithRef() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "ValueRefDoc", sf -> {
            sf.class_("ValueRefDoc", cc -> {
                cc.public_();
                cc.docComment(dc -> {
                    dc.text("Max is ");
                    dc.value(Type.named("java.lang.Integer").docRef("MAX_VALUE"));
                    dc.text(".");
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "ValueRefDoc");
        assertTrue(source.contains("{@value Integer#MAX_VALUE}"),
                "should contain {@value} tag with resolved type and member reference");
    }

    /**
     * Verifies that the {@code @version} tag is generated correctly on a class.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void versionTag() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "VersionDoc", sf -> {
            sf.class_("VersionDoc", cc -> {
                cc.public_();
                cc.docComment(dc -> {
                    dc.text("A versioned class.");
                    dc.version("3.1.0");
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "VersionDoc");
        assertTrue(source.contains("@version 3.1.0"), "should contain @version tag");
    }

    /**
     * Verifies that the {@code @return} block tag is rejected on a class.
     */
    @Test
    void returnTagRejectedOnClass() {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        assertThrows(IllegalStateException.class, () -> {
            sources.createSourceFile("com.example", "ReturnBad", sf -> {
                sf.class_("ReturnBad", cc -> {
                    cc.public_();
                    cc.docComment(dc -> {
                        dc.return_("not valid here");
                    });
                });
            });
        });
    }

    /**
     * Verifies that the {@code @throws} block tag is rejected on a field.
     */
    @Test
    void throwsTagRejectedOnField() {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        assertThrows(IllegalStateException.class, () -> {
            sources.createSourceFile("com.example", "ThrowsBad", sf -> {
                sf.class_("ThrowsBad", cc -> {
                    cc.public_();
                    cc.field("x", fc -> {
                        fc.type(Type.INT);
                        fc.docComment(dc -> {
                            dc.throws_(Type.named("java.lang.Exception"), "never");
                        });
                    });
                });
            });
        });
    }

    /**
     * Verifies that the {@code @serial} tag is rejected on a method.
     */
    @Test
    void serialTagRejectedOnMethod() {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        assertThrows(IllegalStateException.class, () -> {
            sources.createSourceFile("com.example", "SerialBad", sf -> {
                sf.class_("SerialBad", cc -> {
                    cc.public_();
                    cc.method("foo", mc -> {
                        mc.public_();
                        mc.docComment(dc -> {
                            dc.serial("include");
                        });
                        mc.body(b -> {
                        });
                    });
                });
            });
        });
    }

    /**
     * Verifies that the inline {@code {@return ...}} tag requires Java 16+.
     */
    @Test
    void returnInlineRejectedBeforeJava16() {
        final Sources sources = createSources(SourceVersion.JAVA_15);
        assertThrows(IllegalStateException.class, () -> {
            sources.createSourceFile("com.example", "ReturnInlineBad", sf -> {
                sf.class_("ReturnInlineBad", cc -> {
                    cc.public_();
                    cc.method("foo", mc -> {
                        mc.public_();
                        mc.returning(Type.INT);
                        mc.docComment(dc -> {
                            dc.returnInline("the value");
                        });
                        mc.body(b -> {
                            b.return_(Expr.ZERO);
                        });
                    });
                });
            });
        });
    }

    // ---- Tests for co-located returning/throws documentation ----

    /**
     * Verifies that the {@code returning(Type, Consumer)} overload generates
     * a {@code @return} block tag in the method's Javadoc.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void returningWithDoc() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "ReturningDoc", sf -> {
            sf.class_("ReturningDoc", cc -> {
                cc.public_();
                cc.method("getValue", mc -> {
                    mc.public_();
                    mc.returning(Type.INT, dc -> dc.text("the computed value"));
                    mc.body(b -> {
                        b.return_(Expr.ZERO);
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "ReturningDoc");
        assertTrue(source.contains("@return"), "should contain @return tag");
        assertTrue(source.contains("the computed value"), "should contain return description");
    }

    /**
     * Verifies that the {@code returningInline(Type, Consumer)} overload generates
     * an inline {@code {@return ...}} tag in the method's Javadoc.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void returningInlineWithDoc() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "ReturningInlineDoc", sf -> {
            sf.class_("ReturningInlineDoc", cc -> {
                cc.public_();
                cc.method("getName", mc -> {
                    mc.public_();
                    mc.returningInline(Type.STRING, dc -> dc.text("the name"));
                    mc.body(b -> {
                        b.return_(Expr.str(""));
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "ReturningInlineDoc");
        assertTrue(source.contains("{@return the name}"), "should contain inline {@return} tag");
    }

    /**
     * Verifies that the {@code returningInline} overload requires Java 16+.
     */
    @Test
    void returningInlineDocRejectedBeforeJava16() {
        final Sources sources = createSources(SourceVersion.JAVA_15);
        assertThrows(IllegalStateException.class, () -> {
            sources.createSourceFile("com.example", "ReturningInlineBad", sf -> {
                sf.class_("ReturningInlineBad", cc -> {
                    cc.public_();
                    cc.method("foo", mc -> {
                        mc.public_();
                        mc.returningInline(Type.INT, dc -> dc.text("the value"));
                        mc.body(b -> {
                            b.return_(Expr.ZERO);
                        });
                    });
                });
            });
        });
    }

    /**
     * Verifies that the {@code throws_(Type, Consumer)} overload generates
     * a {@code @throws} block tag in the method's Javadoc.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void throwsWithDoc() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        final Type ioException = Type.named("java.io.IOException");
        sources.createSourceFile("com.example", "ThrowsWithDoc", sf -> {
            sf.class_("ThrowsWithDoc", cc -> {
                cc.public_();
                cc.method("readFile", mc -> {
                    mc.public_();
                    mc.throws_(ioException, dc -> dc.text("if I/O fails"));
                    mc.body(b -> {
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "ThrowsWithDoc");
        assertTrue(source.contains("@throws java.io.IOException"), "should contain @throws tag with type");
        assertTrue(source.contains("if I/O fails"), "should contain throws description");
        assertTrue(source.contains("throws java.io.IOException"),
                "should contain throws clause in method signature");
    }

    /**
     * Verifies that the {@code throws_(Type, Consumer)} overload on a constructor
     * generates a {@code @throws} block tag in the constructor's Javadoc.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void constructorThrowsWithDoc() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        final Type illegalArg = Type.named("java.lang.IllegalArgumentException");
        sources.createSourceFile("com.example", "CtorThrowsDoc", sf -> {
            sf.class_("CtorThrowsDoc", cc -> {
                cc.public_();
                cc.constructor(ctor -> {
                    ctor.public_();
                    ctor.param("name", Type.STRING);
                    ctor.throws_(illegalArg, dc -> dc.text("if name is null"));
                    ctor.body(b -> {
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "CtorThrowsDoc");
        assertTrue(source.contains("@throws IllegalArgumentException"),
                "should contain @throws tag with resolved type");
        assertTrue(source.contains("if name is null"), "should contain throws description");
    }

    /**
     * Verifies that the {@code returning} doc overload supports rich inline content
     * including {@code {@code}} and {@code {@link}} tags.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void returningDocWithRichContent() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "ReturningRich", sf -> {
            sf.class_("ReturningRich", cc -> {
                cc.public_();
                cc.method("findName", mc -> {
                    mc.public_();
                    mc.returning(Type.STRING, dc -> {
                        dc.text("the name, or ");
                        dc.code("null");
                        dc.text(" if not found");
                    });
                    mc.body(b -> {
                        b.return_(Expr.NULL);
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "ReturningRich");
        assertTrue(source.contains("@return"), "should contain @return tag");
        assertTrue(source.contains("the name, or {@code null} if not found"),
                "should contain rich inline content in @return tag");
    }

    /**
     * Verifies that the {@code throws_} doc overload supports rich inline content.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void throwsDocWithRichContent() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        final Type ioException = Type.named("java.io.IOException");
        sources.createSourceFile("com.example", "ThrowsRich", sf -> {
            sf.class_("ThrowsRich", cc -> {
                cc.public_();
                cc.method("readFile", mc -> {
                    mc.public_();
                    mc.throws_(ioException, dc -> {
                        dc.text("if the file is not ");
                        dc.code("readable");
                    });
                    mc.body(b -> {
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "ThrowsRich");
        assertTrue(source.contains("@throws java.io.IOException if the file is not {@code readable}"),
                "should contain @throws tag with rich inline content");
    }

    /**
     * Verifies that using the {@code returning} doc overload without an explicit
     * {@code docComment()} call still produces a valid Javadoc comment
     * containing the {@code @return} tag.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void returningDocAlone() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "ReturnAlone", sf -> {
            sf.class_("ReturnAlone", cc -> {
                cc.public_();
                cc.method("getCount", mc -> {
                    mc.public_();
                    mc.returning(Type.INT, dc -> dc.text("the count"));
                    mc.body(b -> {
                        b.return_(Expr.ZERO);
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "ReturnAlone");
        assertTrue(source.contains("/**"), "should produce a doc comment");
        assertTrue(source.contains("@return"), "should contain @return tag");
        assertTrue(source.contains("the count"), "should contain return description");
        assertTrue(source.contains("*/"), "should close the doc comment");
    }

    /**
     * Verifies that the {@code returning} and {@code throws_} doc overloads
     * combine correctly with an explicit method-level {@code docComment()} call,
     * producing body text followed by block tags in call order.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void returningAndThrowsDocCombinedWithMethodDoc() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        final Type ioException = Type.named("java.io.IOException");
        sources.createSourceFile("com.example", "CombinedDoc", sf -> {
            sf.class_("CombinedDoc", cc -> {
                cc.public_();
                cc.method("process", mc -> {
                    mc.public_();
                    mc.docComment(dc -> {
                        dc.text("Processes data.");
                    });
                    mc.returning(Type.STRING, dc -> dc.text("the processed result"));
                    mc.param("input", Type.STRING, p -> {
                        p.docComment(dc -> dc.text("the input data"));
                    });
                    mc.throws_(ioException, dc -> dc.text("if processing fails"));
                    mc.body(b -> {
                        b.return_(Expr.$v("input"));
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "CombinedDoc");
        assertTrue(source.contains("Processes data."), "should contain body text");
        assertTrue(source.contains("@return"), "should contain @return tag");
        assertTrue(source.contains("the processed result"), "should contain return description");
        assertTrue(source.contains("@param input"), "should contain @param tag");
        assertTrue(source.contains("@throws java.io.IOException"), "should contain @throws tag");

        // Verify ordering: body text before all block tags
        final int textIndex = source.indexOf("Processes data.");
        final int returnIndex = source.indexOf("@return");
        final int paramIndex = source.indexOf("@param input");
        final int throwsIndex = source.indexOf("@throws java.io.IOException");
        assertTrue(textIndex < returnIndex, "body text should appear before @return");
        assertTrue(returnIndex < paramIndex, "@return should appear before @param (call order)");
        assertTrue(paramIndex < throwsIndex, "@param should appear before @throws (call order)");
    }

    /**
     * Verifies that calling {@code throws_(Type, Consumer)} multiple times
     * produces multiple {@code @throws} tags in the generated Javadoc.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void multipleThrowsWithDoc() throws IOException {
        final Sources sources = createSources(SourceVersion.JAVA_17);
        final Type ioException = Type.named("java.io.IOException");
        final Type illegalArg = Type.named("java.lang.IllegalArgumentException");
        sources.createSourceFile("com.example", "MultiThrowsDoc", sf -> {
            sf.class_("MultiThrowsDoc", cc -> {
                cc.public_();
                cc.method("process", mc -> {
                    mc.public_();
                    mc.throws_(ioException, dc -> dc.text("if I/O fails"));
                    mc.throws_(illegalArg, dc -> dc.text("if the argument is invalid"));
                    mc.body(b -> {
                    });
                });
            });
        });
        sources.writeSources();
        final String source = getSource("com.example", "MultiThrowsDoc");
        assertTrue(source.contains("@throws java.io.IOException"),
                "should contain first @throws tag");
        assertTrue(source.contains("if I/O fails"), "should contain first throws description");
        assertTrue(source.contains("@throws IllegalArgumentException"),
                "should contain second @throws tag");
        assertTrue(source.contains("if the argument is invalid"),
                "should contain second throws description");
    }
}
