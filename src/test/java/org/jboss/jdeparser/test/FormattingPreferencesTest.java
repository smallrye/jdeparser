package org.jboss.jdeparser.test;

import java.io.IOException;
import java.util.List;

import org.jboss.jdeparser.JExpr;
import org.jboss.jdeparser.JSources;
import org.jboss.jdeparser.JType;
import org.jboss.jdeparser.SourceVersion;
import org.jboss.jdeparser.creator.BlockCreator;
import org.jboss.jdeparser.format.FormatPreferences;
import org.jboss.jdeparser.format.FormatPreferences.Indentation;
import org.jboss.jdeparser.format.FormatPreferences.Opt;
import org.jboss.jdeparser.format.FormatPreferences.Space;
import org.jboss.jdeparser.format.FormatPreferences.SpaceType;
import org.jboss.jdeparser.format.FormatPreferences.Wrapping;
import org.jboss.jdeparser.format.FormatPreferences.WrappingMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests that verify changing {@link FormatPreferences} actually changes the
 * generated source output.
 * <p>
 * Each test generates code with default preferences and asserts the expected
 * spaced form, then regenerates the same code with a modified preference and
 * asserts the output has changed accordingly.
 */
class FormattingPreferencesTest extends AbstractGeneratingTestCase {

    /**
     * Verifies that setting {@link Space#BEFORE_PAREN_IF} to {@link SpaceType#NONE}
     * removes the space between {@code if} and its opening parenthesis.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void noSpaceBeforeIfParen() throws IOException {
        // Generate with default preferences: "if (x > 0)"
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("test", mc -> {
                    mc.param("x", JType.INT);
                    mc.body(b -> {
                        b.if_(JExpr.$v("x").gt(JExpr.ZERO), BlockCreator::return_);
                    });
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains("if ("), "default should contain 'if ('");

        // Generate with BEFORE_PAREN_IF set to NONE: "if(x > 0)"
        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.BEFORE_PAREN_IF, SpaceType.NONE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("test", mc -> {
                    mc.param("x", JType.INT);
                    mc.body(b -> {
                        b.if_(JExpr.$v("x").gt(JExpr.ZERO), BlockCreator::return_);
                    });
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertTrue(modifiedOutput.contains("if("), "modified should contain 'if('");
        assertFalse(modifiedOutput.contains("if ("), "modified should not contain 'if ('");
    }

    /**
     * Verifies that setting {@link Space#BEFORE_BRACE_METHOD} to {@link SpaceType#NONE}
     * removes the space before the opening brace of a method body.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void noSpaceBeforeMethodBodyBrace() throws IOException {
        // Generate with default preferences: method declaration then " {"
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(BlockCreator::return_);
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains("run() {"), "default should contain 'run() {'");

        // Generate with BEFORE_BRACE_METHOD set to NONE: no space before "{"
        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.BEFORE_BRACE_METHOD, SpaceType.NONE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(BlockCreator::return_);
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertTrue(modifiedOutput.contains("run(){"), "modified should contain 'run(){'");
        assertFalse(modifiedOutput.contains("run() {"), "modified should not contain 'run() {'");
    }

    /**
     * Verifies that setting {@link Space#AROUND_ASSIGN} to {@link SpaceType#NONE}
     * removes spaces around the assignment operator.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void noSpaceAroundAssignment() throws IOException {
        // Generate with default preferences: "x = 0"
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.emit(JExpr.$v("x").assign(JExpr.ZERO));
                    });
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains("x = 0"), "default should contain 'x = 0'");

        // Generate with AROUND_ASSIGN set to NONE: "x=0"
        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.AROUND_ASSIGN, SpaceType.NONE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.emit(JExpr.$v("x").assign(JExpr.ZERO));
                    });
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertTrue(modifiedOutput.contains("x=0"), "modified should contain 'x=0'");
        assertFalse(modifiedOutput.contains("x = 0"), "modified should not contain 'x = 0'");
    }

    /**
     * Verifies that setting {@link Space#WITHIN_BRACES_ARRAY_INIT} to {@link SpaceType#NONE}
     * removes spaces within array initializer braces.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void noSpaceWithinArrayInit() throws IOException {
        // Generate with default preferences: "{ 1, 2 }"
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.field("nums", fc -> {
                    fc.type(JType.INT.array());
                    fc.init(JType.INT.array().newArrayInit(JExpr.decimal(1), JExpr.decimal(2)));
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains("{ 1, 2 }"), "default should contain '{ 1, 2 }'");

        // Generate with WITHIN_BRACES_ARRAY_INIT set to NONE: "{1, 2}"
        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.WITHIN_BRACES_ARRAY_INIT, SpaceType.NONE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.field("nums", fc -> {
                    fc.type(JType.INT.array());
                    fc.init(JType.INT.array().newArrayInit(JExpr.decimal(1), JExpr.decimal(2)));
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertTrue(modifiedOutput.contains("{1, 2}"), "modified should contain '{1, 2}'");
        assertFalse(modifiedOutput.contains("{ 1, 2 }"), "modified should not contain '{ 1, 2 }'");
    }

    /**
     * Verifies that setting {@link Space#AFTER_COMMA} to {@link SpaceType#NONE}
     * removes the space after commas in method call arguments.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void noSpaceAfterComma() throws IOException {
        // Generate with default preferences: "method(a, b)"
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.emit(JExpr.callPlain("method", JExpr.$v("a"), JExpr.$v("b")));
                    });
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains("method(a, b)"), "default should contain 'method(a, b)'");

        // Generate with AFTER_COMMA set to NONE: "method(a,b)"
        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.AFTER_COMMA, SpaceType.NONE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.emit(JExpr.callPlain("method", JExpr.$v("a"), JExpr.$v("b")));
                    });
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertTrue(modifiedOutput.contains("method(a,b)"), "modified should contain 'method(a,b)'");
        assertFalse(modifiedOutput.contains("method(a, b)"), "modified should not contain 'method(a, b)'");
    }

    /**
     * Verifies that setting {@link Space#AROUND_ARROW} to {@link SpaceType#NONE}
     * removes spaces around the arrow operator in lambda expressions.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void noSpaceAroundArrow() throws IOException {
        // Generate with default preferences: "x -> x"
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.field("fn", fc -> {
                    fc.type(JType.OBJECT);
                    fc.init(JExpr.lambda("x", JExpr.$v("x")));
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains("x -> x"), "default should contain 'x -> x'");

        // Generate with AROUND_ARROW set to NONE: "x->x"
        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.AROUND_ARROW, SpaceType.NONE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.field("fn", fc -> {
                    fc.type(JType.OBJECT);
                    fc.init(JExpr.lambda("x", JExpr.$v("x")));
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertTrue(modifiedOutput.contains("x->x"), "modified should contain 'x->x'");
        assertFalse(modifiedOutput.contains("x -> x"), "modified should not contain 'x -> x'");
    }

    /**
     * Verifies that setting {@link Space#BEFORE_PAREN_FOR} to {@link SpaceType#NONE}
     * removes the space between {@code for} and its opening parenthesis.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void noSpaceBeforeForParen() throws IOException {
        // Generate with default preferences: "for (...)"
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.param("n", JType.INT);
                    mc.body(b -> {
                        b.for_(fb -> {
                            fb.init(JType.INT, "i", JExpr.ZERO);
                            fb.condition(JExpr.$v("i").lt(JExpr.$v("n")));
                            fb.update(JExpr.$v("i").inc());
                            fb.body(BlockCreator::empty);
                        });
                    });
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains("for ("), "default should contain 'for ('");

        // Generate with BEFORE_PAREN_FOR set to NONE: "for(...)"
        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.BEFORE_PAREN_FOR, SpaceType.NONE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.param("n", JType.INT);
                    mc.body(b -> {
                        b.for_(fb -> {
                            fb.init(JType.INT, "i", JExpr.ZERO);
                            fb.condition(JExpr.$v("i").lt(JExpr.$v("n")));
                            fb.update(JExpr.$v("i").inc());
                            fb.body(BlockCreator::empty);
                        });
                    });
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertTrue(modifiedOutput.contains("for("), "modified should contain 'for('");
        assertFalse(modifiedOutput.contains("for ("), "modified should not contain 'for ('");
    }

    /**
     * Verifies that setting {@link Space#BEFORE_PAREN_WHILE} to {@link SpaceType#NONE}
     * removes the space between {@code while} and its opening parenthesis.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void noSpaceBeforeWhileParen() throws IOException {
        // Generate with default preferences: "while (...)"
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.while_(JExpr.$v("x").gt(JExpr.ZERO), BlockCreator::empty);
                    });
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains("while ("), "default should contain 'while ('");

        // Generate with BEFORE_PAREN_WHILE set to NONE: "while(...)"
        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.BEFORE_PAREN_WHILE, SpaceType.NONE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.while_(JExpr.$v("x").gt(JExpr.ZERO), BlockCreator::empty);
                    });
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertTrue(modifiedOutput.contains("while("), "modified should contain 'while('");
        assertFalse(modifiedOutput.contains("while ("), "modified should not contain 'while ('");
    }

    /**
     * Verifies that setting {@link Space#AFTER_SEMICOLON} to {@link SpaceType#NONE}
     * removes the space after semicolons in a {@code for} loop header.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void noSpaceAfterSemicolon() throws IOException {
        // Generate with default preferences: "for (int i = 0; i < n; i++)"
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.param("n", JType.INT);
                    mc.body(b -> {
                        b.for_(fb -> {
                            fb.init(JType.INT, "i", JExpr.ZERO);
                            fb.condition(JExpr.$v("i").lt(JExpr.$v("n")));
                            fb.update(JExpr.$v("i").inc());
                            fb.body(BlockCreator::empty);
                        });
                    });
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains("; "), "default should contain '; ' (semicolon followed by space)");

        // Generate with AFTER_SEMICOLON set to NONE: semicolons not followed by space
        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.AFTER_SEMICOLON, SpaceType.NONE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.param("n", JType.INT);
                    mc.body(b -> {
                        b.for_(fb -> {
                            fb.init(JType.INT, "i", JExpr.ZERO);
                            fb.condition(JExpr.$v("i").lt(JExpr.$v("n")));
                            fb.update(JExpr.$v("i").inc());
                            fb.body(BlockCreator::empty);
                        });
                    });
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        // In the for-loop header, the semicolons should not be followed by a space
        assertTrue(modifiedOutput.contains(";i"), "modified should contain ';i' (semicolon directly before identifier)");
    }

    /**
     * Verifies that setting {@link Space#AROUND_RELATIONAL} to {@link SpaceType#NONE}
     * removes spaces around relational operators.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void noSpaceAroundRelational() throws IOException {
        // Generate with default preferences: "x > 0"
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.field("flag", fc -> {
                    fc.type(JType.BOOLEAN);
                    fc.init(JExpr.$v("x").gt(JExpr.ZERO));
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains("x > 0"), "default should contain 'x > 0'");

        // Generate with AROUND_RELATIONAL set to NONE: "x>0"
        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.AROUND_RELATIONAL, SpaceType.NONE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.field("flag", fc -> {
                    fc.type(JType.BOOLEAN);
                    fc.init(JExpr.$v("x").gt(JExpr.ZERO));
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertTrue(modifiedOutput.contains("x>0"), "modified should contain 'x>0'");
        assertFalse(modifiedOutput.contains("x > 0"), "modified should not contain 'x > 0'");
    }

    // ── Batch 1: Operator Spacing ───────────────────────────────────────

    /**
     * Verifies that setting {@link Space#AROUND_LOGICAL} to {@link SpaceType#NONE}
     * removes spaces around the logical AND operator.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void noSpaceAroundLogical() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.field("flag", fc -> {
                    fc.type(JType.BOOLEAN);
                    fc.init(JExpr.$v("a").and(JExpr.$v("b")));
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains("a && b"), "default should contain 'a && b'");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.AROUND_LOGICAL, SpaceType.NONE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.field("flag", fc -> {
                    fc.type(JType.BOOLEAN);
                    fc.init(JExpr.$v("a").and(JExpr.$v("b")));
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertTrue(modifiedOutput.contains("a&&b"), "modified should contain 'a&&b'");
        assertFalse(modifiedOutput.contains("a && b"), "modified should not contain 'a && b'");
    }

    /**
     * Verifies that setting {@link Space#AROUND_EQUALITY} to {@link SpaceType#NONE}
     * removes spaces around the equality operator.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void noSpaceAroundEquality() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.field("flag", fc -> {
                    fc.type(JType.BOOLEAN);
                    fc.init(JExpr.$v("x").eq(JExpr.$v("y")));
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains("x == y"), "default should contain 'x == y'");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.AROUND_EQUALITY, SpaceType.NONE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.field("flag", fc -> {
                    fc.type(JType.BOOLEAN);
                    fc.init(JExpr.$v("x").eq(JExpr.$v("y")));
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertTrue(modifiedOutput.contains("x==y"), "modified should contain 'x==y'");
        assertFalse(modifiedOutput.contains("x == y"), "modified should not contain 'x == y'");
    }

    /**
     * Verifies that setting {@link Space#AROUND_BITWISE} to {@link SpaceType#NONE}
     * removes spaces around the bitwise OR operator.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void noSpaceAroundBitwise() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.field("val", fc -> {
                    fc.type(JType.INT);
                    fc.init(JExpr.$v("a").bitOr(JExpr.$v("b")));
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains("a | b"), "default should contain 'a | b'");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.AROUND_BITWISE, SpaceType.NONE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.field("val", fc -> {
                    fc.type(JType.INT);
                    fc.init(JExpr.$v("a").bitOr(JExpr.$v("b")));
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertTrue(modifiedOutput.contains("a|b"), "modified should contain 'a|b'");
        assertFalse(modifiedOutput.contains("a | b"), "modified should not contain 'a | b'");
    }

    /**
     * Verifies that setting {@link Space#AROUND_ADDITIVE} to {@link SpaceType#NONE}
     * removes spaces around the addition operator.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void noSpaceAroundAdditive() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.field("val", fc -> {
                    fc.type(JType.INT);
                    fc.init(JExpr.$v("a").add(JExpr.$v("b")));
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains("a + b"), "default should contain 'a + b'");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.AROUND_ADDITIVE, SpaceType.NONE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.field("val", fc -> {
                    fc.type(JType.INT);
                    fc.init(JExpr.$v("a").add(JExpr.$v("b")));
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertTrue(modifiedOutput.contains("a+b"), "modified should contain 'a+b'");
        assertFalse(modifiedOutput.contains("a + b"), "modified should not contain 'a + b'");
    }

    /**
     * Verifies that setting {@link Space#AROUND_MULTIPLICATIVE} to {@link SpaceType#NONE}
     * removes spaces around the multiplication operator.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void noSpaceAroundMultiplicative() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.field("val", fc -> {
                    fc.type(JType.INT);
                    fc.init(JExpr.$v("a").mul(JExpr.$v("b")));
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains("a * b"), "default should contain 'a * b'");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.AROUND_MULTIPLICATIVE, SpaceType.NONE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.field("val", fc -> {
                    fc.type(JType.INT);
                    fc.init(JExpr.$v("a").mul(JExpr.$v("b")));
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertTrue(modifiedOutput.contains("a*b"), "modified should contain 'a*b'");
        assertFalse(modifiedOutput.contains("a * b"), "modified should not contain 'a * b'");
    }

    /**
     * Verifies that setting {@link Space#AROUND_SHIFT} to {@link SpaceType#NONE}
     * removes spaces around the left-shift operator.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void noSpaceAroundShift() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.field("val", fc -> {
                    fc.type(JType.INT);
                    fc.init(JExpr.$v("a").shl(JExpr.$v("b")));
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains("a << b"), "default should contain 'a << b'");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.AROUND_SHIFT, SpaceType.NONE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.field("val", fc -> {
                    fc.type(JType.INT);
                    fc.init(JExpr.$v("a").shl(JExpr.$v("b")));
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertTrue(modifiedOutput.contains("a<<b"), "modified should contain 'a<<b'");
        assertFalse(modifiedOutput.contains("a << b"), "modified should not contain 'a << b'");
    }

    // ── Batch 2: Method Reference ───────────────────────────────────────

    /**
     * Verifies that setting {@link Space#AROUND_METHOD_REF} to {@link SpaceType#SPACE}
     * adds spaces around the method reference operator (default is no space).
     *
     * @throws IOException if source generation fails
     */
    @Test
    void spaceAroundMethodRef() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.field("fn", fc -> {
                    fc.type(JType.OBJECT);
                    fc.init(JType.STRING.methodRef("valueOf"));
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains("String::valueOf"), "default should contain 'String::valueOf'");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.AROUND_METHOD_REF, SpaceType.SPACE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.field("fn", fc -> {
                    fc.type(JType.OBJECT);
                    fc.init(JType.STRING.methodRef("valueOf"));
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertTrue(modifiedOutput.contains("String :: valueOf"), "modified should contain 'String :: valueOf'");
        assertFalse(modifiedOutput.contains("String::valueOf"), "modified should not contain 'String::valueOf'");
    }

    // ── Batch 3: Ternary and Cast ───────────────────────────────────────

    /**
     * Verifies that setting {@link Space#BEFORE_TERNARY_Q} to {@link SpaceType#NONE}
     * removes the space before the {@code ?} in a ternary expression.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void noSpaceBeforeTernaryQ() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.field("val", fc -> {
                    fc.type(JType.OBJECT);
                    fc.init(JExpr.$v("x").cond(JExpr.$v("a"), JExpr.$v("b")));
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains("x ?"), "default should contain 'x ?'");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.BEFORE_TERNARY_Q, SpaceType.NONE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.field("val", fc -> {
                    fc.type(JType.OBJECT);
                    fc.init(JExpr.$v("x").cond(JExpr.$v("a"), JExpr.$v("b")));
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertTrue(modifiedOutput.contains("x?"), "modified should contain 'x?'");
        assertFalse(modifiedOutput.contains("x ?"), "modified should not contain 'x ?'");
    }

    /**
     * Verifies that setting {@link Space#AFTER_TERNARY_Q} to {@link SpaceType#NONE}
     * removes the space after the {@code ?} in a ternary expression.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void noSpaceAfterTernaryQ() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.field("val", fc -> {
                    fc.type(JType.OBJECT);
                    fc.init(JExpr.$v("x").cond(JExpr.$v("a"), JExpr.$v("b")));
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains("? a"), "default should contain '? a'");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.AFTER_TERNARY_Q, SpaceType.NONE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.field("val", fc -> {
                    fc.type(JType.OBJECT);
                    fc.init(JExpr.$v("x").cond(JExpr.$v("a"), JExpr.$v("b")));
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertTrue(modifiedOutput.contains("?a"), "modified should contain '?a'");
        assertFalse(modifiedOutput.contains("? a"), "modified should not contain '? a'");
    }

    /**
     * Verifies that setting {@link Space#BEFORE_TERNARY_COLON} to {@link SpaceType#NONE}
     * removes the space before the {@code :} in a ternary expression.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void noSpaceBeforeTernaryColon() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.field("val", fc -> {
                    fc.type(JType.OBJECT);
                    fc.init(JExpr.$v("x").cond(JExpr.$v("a"), JExpr.$v("b")));
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains("a :"), "default should contain 'a :'");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.BEFORE_TERNARY_COLON, SpaceType.NONE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.field("val", fc -> {
                    fc.type(JType.OBJECT);
                    fc.init(JExpr.$v("x").cond(JExpr.$v("a"), JExpr.$v("b")));
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertTrue(modifiedOutput.contains("a:"), "modified should contain 'a:'");
        assertFalse(modifiedOutput.contains("a :"), "modified should not contain 'a :'");
    }

    /**
     * Verifies that setting {@link Space#AFTER_TERNARY_COLON} to {@link SpaceType#NONE}
     * removes the space after the {@code :} in a ternary expression.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void noSpaceAfterTernaryColon() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.field("val", fc -> {
                    fc.type(JType.OBJECT);
                    fc.init(JExpr.$v("x").cond(JExpr.$v("a"), JExpr.$v("b")));
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains(": b"), "default should contain ': b'");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.AFTER_TERNARY_COLON, SpaceType.NONE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.field("val", fc -> {
                    fc.type(JType.OBJECT);
                    fc.init(JExpr.$v("x").cond(JExpr.$v("a"), JExpr.$v("b")));
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertTrue(modifiedOutput.contains(":b"), "modified should contain ':b'");
        assertFalse(modifiedOutput.contains(": b"), "modified should not contain ': b'");
    }

    /**
     * Verifies that setting {@link Space#AFTER_CAST} to {@link SpaceType#NONE}
     * removes the space after a cast expression's closing parenthesis.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void noSpaceAfterCast() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.field("val", fc -> {
                    fc.type(JType.STRING);
                    fc.init(JExpr.$v("obj").cast(JType.STRING));
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains("(String) obj"), "default should contain '(String) obj'");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.AFTER_CAST, SpaceType.NONE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.field("val", fc -> {
                    fc.type(JType.STRING);
                    fc.init(JExpr.$v("obj").cast(JType.STRING));
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertTrue(modifiedOutput.contains("(String)obj"), "modified should contain '(String)obj'");
        assertFalse(modifiedOutput.contains("(String) obj"), "modified should not contain '(String) obj'");
    }

    // ── Batch 4: Type-Bound and Multi-Catch ─────────────────────────────

    /**
     * Verifies that setting {@link Space#AROUND_TYPE_BOUND_AND} to {@link SpaceType#NONE}
     * removes spaces around the {@code &} in type bounds.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void noSpaceAroundTypeBoundAnd() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.typeParam("T", tp -> {
                    tp.extends_(JType.named("java.io.Serializable"), JType.named("java.lang.Comparable"));
                });
                cc.method("run", mc -> {
                    mc.body(BlockCreator::return_);
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains("Serializable & Comparable"), "default should contain 'Serializable & Comparable' (space on both sides)");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.AROUND_TYPE_BOUND_AND, SpaceType.NONE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.typeParam("T", tp -> {
                    tp.extends_(JType.named("java.io.Serializable"), JType.named("java.lang.Comparable"));
                });
                cc.method("run", mc -> {
                    mc.body(BlockCreator::return_);
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertTrue(modifiedOutput.contains("Serializable&Comparable"), "modified should contain 'Serializable&Comparable'");
        assertFalse(modifiedOutput.contains("Serializable & Comparable"), "modified should not contain 'Serializable & Comparable'");
    }

    /**
     * Verifies that setting {@link Space#AROUND_MULTI_CATCH_OR} to {@link SpaceType#NONE}
     * removes spaces around the {@code |} in multi-catch clauses.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void noSpaceAroundMultiCatchOr() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.try_(tc -> {
                            tc.body(BlockCreator::empty);
                            tc.catch_(List.of(
                                JType.named("java.io.IOException"),
                                JType.named("java.sql.SQLException")),
                                "e", BlockCreator::empty);
                        });
                    });
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains("IOException | "), "default should contain 'IOException | ' (space on both sides)");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.AROUND_MULTI_CATCH_OR, SpaceType.NONE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.try_(tc -> {
                            tc.body(BlockCreator::empty);
                            tc.catch_(List.of(
                                JType.named("java.io.IOException"),
                                JType.named("java.sql.SQLException")),
                                "e", BlockCreator::empty);
                        });
                    });
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertTrue(modifiedOutput.contains("IOException|"), "modified should contain 'IOException|'");
        assertFalse(modifiedOutput.contains("IOException | "), "modified should not contain 'IOException | '");
    }

    // ── Batch 5: Before-Brace for Type Declarations ─────────────────────

    /**
     * Verifies that setting {@link Space#BEFORE_BRACE_CLASS} to {@link SpaceType#NONE}
     * removes the space before the opening brace of a class body.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void noSpaceBeforeBraceClass() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(BlockCreator::return_);
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains("Cls1 {"), "default should contain 'Cls1 {'");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.BEFORE_BRACE_CLASS, SpaceType.NONE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(BlockCreator::return_);
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertTrue(modifiedOutput.contains("Cls2{"), "modified should contain 'Cls2{'");
        assertFalse(modifiedOutput.contains("Cls2 {"), "modified should not contain 'Cls2 {'");
    }

    /**
     * Verifies that setting {@link Space#BEFORE_BRACE_ENUM} to {@link SpaceType#NONE}
     * removes the space before the opening brace of an enum body.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void noSpaceBeforeBraceEnum() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Color", sf -> {
            sf.enum_("Color", ec -> {
                ec.constant("RED", c -> {});
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Color");
        assertTrue(defaultOutput.contains("Color {"), "default should contain 'Color {'");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.BEFORE_BRACE_ENUM, SpaceType.NONE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Color2", sf -> {
            sf.enum_("Color2", ec -> {
                ec.constant("RED", c -> {});
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Color2");
        assertTrue(modifiedOutput.contains("Color2{"), "modified should contain 'Color2{'");
        assertFalse(modifiedOutput.contains("Color2 {"), "modified should not contain 'Color2 {'");
    }

    /**
     * Verifies that setting {@link Space#BEFORE_BRACE_RECORD} to {@link SpaceType#NONE}
     * removes the space before the opening brace of a record body.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void noSpaceBeforeBraceRecord() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Point", sf -> {
            sf.record_("Point", rc -> {
                rc.component("x", JType.INT);
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Point");
        assertTrue(defaultOutput.contains(") {"), "default should contain ') {'");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.BEFORE_BRACE_RECORD, SpaceType.NONE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Point2", sf -> {
            sf.record_("Point2", rc -> {
                rc.component("x", JType.INT);
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Point2");
        assertTrue(modifiedOutput.contains("){"), "modified should contain '){'");
    }

    /**
     * Verifies that setting {@link Space#BEFORE_BRACE_ANNOTATION_TYPE} to {@link SpaceType#NONE}
     * removes the space before the opening brace of an annotation type body.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void noSpaceBeforeBraceAnnotationType() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "MyAnno", sf -> {
            sf.annotationInterface_("MyAnno", ac -> {
                ac.element("value", JType.STRING);
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "MyAnno");
        assertTrue(defaultOutput.contains("MyAnno {"), "default should contain 'MyAnno {'");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.BEFORE_BRACE_ANNOTATION_TYPE, SpaceType.NONE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "MyAnno2", sf -> {
            sf.annotationInterface_("MyAnno2", ac -> {
                ac.element("value", JType.STRING);
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "MyAnno2");
        assertTrue(modifiedOutput.contains("MyAnno2{"), "modified should contain 'MyAnno2{'");
        assertFalse(modifiedOutput.contains("MyAnno2 {"), "modified should not contain 'MyAnno2 {'");
    }

    // ── Batch 6: Before-Brace for Control Flow ──────────────────────────

    /**
     * Verifies that setting {@link Space#BEFORE_BRACE_IF} to {@link SpaceType#NONE}
     * removes the space before the opening brace of an {@code if} body.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void noSpaceBeforeBraceIf() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.if_(JExpr.$v("x").gt(JExpr.ZERO), BlockCreator::return_);
                    });
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains(") {"), "default should contain ') {'");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.BEFORE_BRACE_IF, SpaceType.NONE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.if_(JExpr.$v("x").gt(JExpr.ZERO), BlockCreator::return_);
                    });
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertTrue(modifiedOutput.contains("){"), "modified should contain '){'");
    }

    /**
     * Verifies that setting {@link Space#BEFORE_BRACE_ELSE} to {@link SpaceType#NONE}
     * removes the space before the opening brace of an {@code else} body.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void noSpaceBeforeBraceElse() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.ifElse(JExpr.$v("x").gt(JExpr.ZERO), BlockCreator::return_, BlockCreator::return_);
                    });
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains("else {"), "default should contain 'else {'");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.BEFORE_BRACE_ELSE, SpaceType.NONE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.ifElse(JExpr.$v("x").gt(JExpr.ZERO), BlockCreator::return_, BlockCreator::return_);
                    });
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertTrue(modifiedOutput.contains("else{"), "modified should contain 'else{'");
        assertFalse(modifiedOutput.contains("else {"), "modified should not contain 'else {'");
    }

    /**
     * Verifies that setting {@link Space#BEFORE_BRACE_FOR} to {@link SpaceType#NONE}
     * removes the space before the opening brace of a {@code for} body.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void noSpaceBeforeBraceFor() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.for_(fb -> {
                            fb.init(JType.INT, "i", JExpr.ZERO);
                            fb.condition(JExpr.$v("i").lt(JExpr.$v("n")));
                            fb.update(JExpr.$v("i").inc());
                            fb.body(BlockCreator::empty);
                        });
                    });
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains("i++) {"), "default should contain 'i++) {'");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.BEFORE_BRACE_FOR, SpaceType.NONE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.for_(fb -> {
                            fb.init(JType.INT, "i", JExpr.ZERO);
                            fb.condition(JExpr.$v("i").lt(JExpr.$v("n")));
                            fb.update(JExpr.$v("i").inc());
                            fb.body(BlockCreator::empty);
                        });
                    });
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertTrue(modifiedOutput.contains("i++){"), "modified should contain 'i++){'");
    }

    /**
     * Verifies that setting {@link Space#BEFORE_BRACE_WHILE} to {@link SpaceType#NONE}
     * removes the space before the opening brace of a {@code while} body.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void noSpaceBeforeBraceWhile() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.while_(JExpr.$v("x").gt(JExpr.ZERO), BlockCreator::empty);
                    });
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains("0) {"), "default should contain '0) {'");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.BEFORE_BRACE_WHILE, SpaceType.NONE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.while_(JExpr.$v("x").gt(JExpr.ZERO), BlockCreator::empty);
                    });
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertTrue(modifiedOutput.contains("0){"), "modified should contain '0){'");
    }

    /**
     * Verifies that setting {@link Space#BEFORE_BRACE_DO} to {@link SpaceType#NONE}
     * removes the space before the opening brace of a {@code do} body.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void noSpaceBeforeBraceDo() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.doWhile(BlockCreator::empty, JExpr.$v("x").gt(JExpr.ZERO));
                    });
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains("do {"), "default should contain 'do {'");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.BEFORE_BRACE_DO, SpaceType.NONE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.doWhile(BlockCreator::empty, JExpr.$v("x").gt(JExpr.ZERO));
                    });
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertTrue(modifiedOutput.contains("do{"), "modified should contain 'do{'");
        assertFalse(modifiedOutput.contains("do {"), "modified should not contain 'do {'");
    }

    /**
     * Verifies that setting {@link Space#BEFORE_BRACE_SWITCH} to {@link SpaceType#NONE}
     * removes the space before the opening brace of a {@code switch} body.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void noSpaceBeforeBraceSwitch() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.switch_(JExpr.$v("x"), sc -> {
                            sc.default_(BlockCreator::empty);
                        });
                    });
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains("x) {"), "default should contain 'x) {'");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.BEFORE_BRACE_SWITCH, SpaceType.NONE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.switch_(JExpr.$v("x"), sc -> {
                            sc.default_(BlockCreator::empty);
                        });
                    });
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertTrue(modifiedOutput.contains("x){"), "modified should contain 'x){'");
    }

    /**
     * Verifies that setting {@link Space#BEFORE_BRACE_TRY} to {@link SpaceType#NONE}
     * removes the space before the opening brace of a {@code try} body.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void noSpaceBeforeBraceTry() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.try_(tc -> {
                            tc.body(BlockCreator::empty);
                            tc.finally_(BlockCreator::empty);
                        });
                    });
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains("try {"), "default should contain 'try {'");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.BEFORE_BRACE_TRY, SpaceType.NONE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.try_(tc -> {
                            tc.body(BlockCreator::empty);
                            tc.finally_(BlockCreator::empty);
                        });
                    });
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertTrue(modifiedOutput.contains("try{"), "modified should contain 'try{'");
        assertFalse(modifiedOutput.contains("try {"), "modified should not contain 'try {'");
    }

    /**
     * Verifies that setting {@link Space#BEFORE_BRACE_CATCH} to {@link SpaceType#NONE}
     * removes the space before the opening brace of a {@code catch} body.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void noSpaceBeforeBraceCatch() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.try_(tc -> {
                            tc.body(BlockCreator::empty);
                            tc.catch_(JType.named("java.lang.Exception"), "e", BlockCreator::empty);
                        });
                    });
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains("e) {"), "default should contain 'e) {'");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.BEFORE_BRACE_CATCH, SpaceType.NONE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.try_(tc -> {
                            tc.body(BlockCreator::empty);
                            tc.catch_(JType.named("java.lang.Exception"), "e", BlockCreator::empty);
                        });
                    });
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertTrue(modifiedOutput.contains("e){"), "modified should contain 'e){'");
    }

    /**
     * Verifies that setting {@link Space#BEFORE_BRACE_FINALLY} to {@link SpaceType#NONE}
     * removes the space before the opening brace of a {@code finally} body.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void noSpaceBeforeBraceFinally() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.try_(tc -> {
                            tc.body(BlockCreator::empty);
                            tc.finally_(BlockCreator::empty);
                        });
                    });
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains("finally {"), "default should contain 'finally {'");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.BEFORE_BRACE_FINALLY, SpaceType.NONE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.try_(tc -> {
                            tc.body(BlockCreator::empty);
                            tc.finally_(BlockCreator::empty);
                        });
                    });
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertTrue(modifiedOutput.contains("finally{"), "modified should contain 'finally{'");
        assertFalse(modifiedOutput.contains("finally {"), "modified should not contain 'finally {'");
    }

    /**
     * Verifies that setting {@link Space#BEFORE_BRACE_SYNCHRONIZED} to {@link SpaceType#NONE}
     * removes the space before the opening brace of a {@code synchronized} body.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void noSpaceBeforeBraceSynchronized() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.synchronized_(JExpr.$v("lock"), BlockCreator::empty);
                    });
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains("lock) {"), "default should contain 'lock) {'");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.BEFORE_BRACE_SYNCHRONIZED, SpaceType.NONE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.synchronized_(JExpr.$v("lock"), BlockCreator::empty);
                    });
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertTrue(modifiedOutput.contains("lock){"), "modified should contain 'lock){'");
    }

    /**
     * Verifies that setting both {@link Space#AROUND_ARROW} and {@link Space#BEFORE_BRACE_LAMBDA}
     * to {@link SpaceType#NONE} removes the space before the opening brace of a block-body lambda.
     * Both must be set because the two Space rules overlap at this position and the greatest wins.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void noSpaceBeforeBraceLambda() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.field("fn", fc -> {
                    fc.type(JType.OBJECT);
                    fc.init(JExpr.lambda(SourceVersion.JAVA_17, "x", body -> body.return_(JExpr.$v("x"))));
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains("-> {"), "default should contain '-> {'");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.AROUND_ARROW, SpaceType.NONE)
            .space(Space.BEFORE_BRACE_LAMBDA, SpaceType.NONE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.field("fn", fc -> {
                    fc.type(JType.OBJECT);
                    fc.init(JExpr.lambda(SourceVersion.JAVA_17, "x", body -> body.return_(JExpr.$v("x"))));
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertTrue(modifiedOutput.contains("->{"), "modified should contain '->{'");
        assertFalse(modifiedOutput.contains("-> {"), "modified should not contain '-> {'");
    }

    // ── Batch 7: Before-Brace for Array Init ────────────────────────────

    /**
     * Verifies that setting {@link Space#BEFORE_BRACE_ARRAY_INIT} to {@link SpaceType#NONE}
     * removes the space before the opening brace of an array initializer.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void noSpaceBeforeBraceArrayInit() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.field("nums", fc -> {
                    fc.type(JType.INT.array());
                    fc.init(JType.INT.array().newArrayInit(JExpr.decimal(1)));
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains("[] {"), "default should contain '[] {'");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.BEFORE_BRACE_ARRAY_INIT, SpaceType.NONE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.field("nums", fc -> {
                    fc.type(JType.INT.array());
                    fc.init(JType.INT.array().newArrayInit(JExpr.decimal(1)));
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertTrue(modifiedOutput.contains("[]{"), "modified should contain '[]{'");
        assertFalse(modifiedOutput.contains("[] {"), "modified should not contain '[] {'");
    }

    // ── Batch 8: Before-Paren for Control Flow ──────────────────────────

    /**
     * Verifies that setting {@link Space#BEFORE_PAREN_SWITCH} to {@link SpaceType#NONE}
     * removes the space between {@code switch} and its opening parenthesis.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void noSpaceBeforeParenSwitch() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.switch_(JExpr.$v("x"), sc -> sc.default_(BlockCreator::empty));
                    });
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains("switch ("), "default should contain 'switch ('");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.BEFORE_PAREN_SWITCH, SpaceType.NONE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.switch_(JExpr.$v("x"), sc -> sc.default_(BlockCreator::empty));
                    });
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertTrue(modifiedOutput.contains("switch("), "modified should contain 'switch('");
        assertFalse(modifiedOutput.contains("switch ("), "modified should not contain 'switch ('");
    }

    /**
     * Verifies that setting {@link Space#BEFORE_PAREN_TRY} to {@link SpaceType#NONE}
     * removes the space between {@code try} and its opening parenthesis in try-with-resources.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void noSpaceBeforeParenTry() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.try_(tc -> {
                            tc.with(JType.named("java.io.InputStream"), "is", JExpr.$v("getStream").call("open"));
                            tc.body(BlockCreator::empty);
                        });
                    });
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains("try ("), "default should contain 'try ('");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.BEFORE_PAREN_TRY, SpaceType.NONE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.try_(tc -> {
                            tc.with(JType.named("java.io.InputStream"), "is", JExpr.$v("getStream").call("open"));
                            tc.body(BlockCreator::empty);
                        });
                    });
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertTrue(modifiedOutput.contains("try("), "modified should contain 'try('");
        assertFalse(modifiedOutput.contains("try ("), "modified should not contain 'try ('");
    }

    /**
     * Verifies that setting {@link Space#BEFORE_PAREN_CATCH} to {@link SpaceType#NONE}
     * removes the space between {@code catch} and its opening parenthesis.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void noSpaceBeforeParenCatch() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.try_(tc -> {
                            tc.body(BlockCreator::empty);
                            tc.catch_(JType.named("java.lang.Exception"), "e", BlockCreator::empty);
                        });
                    });
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains("catch ("), "default should contain 'catch ('");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.BEFORE_PAREN_CATCH, SpaceType.NONE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.try_(tc -> {
                            tc.body(BlockCreator::empty);
                            tc.catch_(JType.named("java.lang.Exception"), "e", BlockCreator::empty);
                        });
                    });
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertTrue(modifiedOutput.contains("catch("), "modified should contain 'catch('");
        assertFalse(modifiedOutput.contains("catch ("), "modified should not contain 'catch ('");
    }

    /**
     * Verifies that setting {@link Space#BEFORE_PAREN_SYNCHRONIZED} to {@link SpaceType#NONE}
     * removes the space between {@code synchronized} and its opening parenthesis.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void noSpaceBeforeParenSynchronized() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.synchronized_(JExpr.$v("lock"), BlockCreator::empty);
                    });
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains("synchronized ("), "default should contain 'synchronized ('");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.BEFORE_PAREN_SYNCHRONIZED, SpaceType.NONE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.synchronized_(JExpr.$v("lock"), BlockCreator::empty);
                    });
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertTrue(modifiedOutput.contains("synchronized("), "modified should contain 'synchronized('");
        assertFalse(modifiedOutput.contains("synchronized ("), "modified should not contain 'synchronized ('");
    }

    /**
     * Verifies that setting {@link Space#BEFORE_PAREN_RECORD} to {@link SpaceType#SPACE}
     * adds a space before the record component list parenthesis (default is no space).
     *
     * @throws IOException if source generation fails
     */
    @Test
    void spaceBeforeParenRecord() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Point", sf -> {
            sf.record_("Point", rc -> {
                rc.component("x", JType.INT);
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Point");
        assertTrue(defaultOutput.contains("Point("), "default should contain 'Point('");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.BEFORE_PAREN_RECORD, SpaceType.SPACE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Point2", sf -> {
            sf.record_("Point2", rc -> {
                rc.component("x", JType.INT);
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Point2");
        assertTrue(modifiedOutput.contains("Point2 ("), "modified should contain 'Point2 ('");
        assertFalse(modifiedOutput.contains("Point2("), "modified should not contain 'Point2('");
    }

    // ── Batch 9: Before-Keyword ─────────────────────────────────────────

    /**
     * Verifies that setting {@link Space#BEFORE_KEYWORD_ELSE} to {@link SpaceType#NEWLINE}
     * places the {@code else} keyword on a new line after the if-block's closing brace.
     * NONE cannot downgrade below the keyword's auto-spacing (SPACE level), so NEWLINE is
     * the only non-default value that has an observable effect here.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void newlineBeforeKeywordElse2() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.ifElse(JExpr.$v("x").gt(JExpr.ZERO), BlockCreator::return_, BlockCreator::return_);
                    });
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains("} else"), "default should contain '} else'");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.BEFORE_KEYWORD_ELSE, SpaceType.NEWLINE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.ifElse(JExpr.$v("x").gt(JExpr.ZERO), BlockCreator::return_, BlockCreator::return_);
                    });
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertFalse(modifiedOutput.contains("} else"), "modified should not contain '} else' on one line");
    }

    /**
     * Verifies that setting {@link Space#BEFORE_KEYWORD_WHILE} to {@link SpaceType#NEWLINE}
     * places the {@code while} keyword on a new line after the do-block's closing brace.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void newlineBeforeKeywordWhile() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.doWhile(BlockCreator::empty, JExpr.$v("x").gt(JExpr.ZERO));
                    });
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains("} while"), "default should contain '} while'");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.BEFORE_KEYWORD_WHILE, SpaceType.NEWLINE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.doWhile(BlockCreator::empty, JExpr.$v("x").gt(JExpr.ZERO));
                    });
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertFalse(modifiedOutput.contains("} while"), "modified should not contain '} while' on one line");
    }

    /**
     * Verifies that setting {@link Space#BEFORE_KEYWORD_CATCH} to {@link SpaceType#NEWLINE}
     * places the {@code catch} keyword on a new line after the try-block's closing brace.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void newlineBeforeKeywordCatch() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.try_(tc -> {
                            tc.body(BlockCreator::empty);
                            tc.catch_(JType.named("java.lang.Exception"), "e", BlockCreator::empty);
                        });
                    });
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains("} catch"), "default should contain '} catch'");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.BEFORE_KEYWORD_CATCH, SpaceType.NEWLINE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.try_(tc -> {
                            tc.body(BlockCreator::empty);
                            tc.catch_(JType.named("java.lang.Exception"), "e", BlockCreator::empty);
                        });
                    });
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertFalse(modifiedOutput.contains("} catch"), "modified should not contain '} catch' on one line");
    }

    /**
     * Verifies that setting {@link Space#BEFORE_KEYWORD_FINALLY} to {@link SpaceType#NEWLINE}
     * places the {@code finally} keyword on a new line after the try-block's closing brace.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void newlineBeforeKeywordFinally() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.try_(tc -> {
                            tc.body(BlockCreator::empty);
                            tc.finally_(BlockCreator::empty);
                        });
                    });
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains("} finally"), "default should contain '} finally'");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.BEFORE_KEYWORD_FINALLY, SpaceType.NEWLINE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.try_(tc -> {
                            tc.body(BlockCreator::empty);
                            tc.finally_(BlockCreator::empty);
                        });
                    });
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertFalse(modifiedOutput.contains("} finally"), "modified should not contain '} finally' on one line");
    }

    // ── Batch 10: Colon Spacing and Labels ──────────────────────────────

    /**
     * Verifies that setting {@link Space#BEFORE_COLON} to {@link SpaceType#NONE}
     * removes the space before the colon in an enhanced for-each loop.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void noSpaceBeforeColon() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.forEach(JType.STRING, "item", JExpr.$v("list"), BlockCreator::empty);
                    });
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains("item :"), "default should contain 'item :'");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.BEFORE_COLON, SpaceType.NONE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.forEach(JType.STRING, "item", JExpr.$v("list"), BlockCreator::empty);
                    });
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertTrue(modifiedOutput.contains("item:"), "modified should contain 'item:'");
        assertFalse(modifiedOutput.contains("item :"), "modified should not contain 'item :'");
    }

    /**
     * Verifies that setting {@link Space#AFTER_COLON} to {@link SpaceType#NONE}
     * removes the space after the colon in an enhanced for-each loop.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void noSpaceAfterColon() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.forEach(JType.STRING, "item", JExpr.$v("list"), BlockCreator::empty);
                    });
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains(": list"), "default should contain ': list'");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.AFTER_COLON, SpaceType.NONE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.forEach(JType.STRING, "item", JExpr.$v("list"), BlockCreator::empty);
                    });
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertTrue(modifiedOutput.contains(":list"), "modified should contain ':list'");
        assertFalse(modifiedOutput.contains(": list"), "modified should not contain ': list'");
    }

    /**
     * Verifies that setting {@link Space#AFTER_LABEL} to {@link SpaceType#SPACE}
     * places the labeled content on the same line as the label (default is {@link SpaceType#NEWLINE}).
     *
     * @throws IOException if source generation fails
     */
    @Test
    void spaceAfterLabel() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.labeled("outer", (label, body) -> {
                            body.while_(JExpr.$v("x").gt(JExpr.ZERO), loop -> loop.break_(label));
                        });
                    });
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertFalse(defaultOutput.contains("outer: {"), "default should not contain 'outer: {' on one line (NEWLINE is default)");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.AFTER_LABEL, SpaceType.SPACE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.labeled("outer", (label, body) -> {
                            body.while_(JExpr.$v("x").gt(JExpr.ZERO), loop -> loop.break_(label));
                        });
                    });
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertTrue(modifiedOutput.contains("outer: {"), "modified should contain 'outer: {' on one line");
    }

    // ── Batch 11: Comma Variants ────────────────────────────────────────

    /**
     * Verifies that setting {@link Space#AFTER_COMMA_TYPE_ARGUMENT} to {@link SpaceType#NONE}
     * removes the space after commas in type parameter lists.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void noSpaceCommaTypeArgument() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.typeParam("K", tp -> {});
                cc.typeParam("V", tp -> {});
                cc.method("run", mc -> {
                    mc.body(BlockCreator::return_);
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains("<K, V>"), "default should contain '<K, V>'");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.AFTER_COMMA_TYPE_ARGUMENT, SpaceType.NONE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.typeParam("K", tp -> {});
                cc.typeParam("V", tp -> {});
                cc.method("run", mc -> {
                    mc.body(BlockCreator::return_);
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertTrue(modifiedOutput.contains("<K,V>"), "modified should contain '<K,V>'");
        assertFalse(modifiedOutput.contains("<K, V>"), "modified should not contain '<K, V>'");
    }

    /**
     * Verifies that setting {@link Space#COMMA_ENUM_CONSTANT} to {@link SpaceType#SPACE}
     * places enum constants on the same line separated by commas (default is {@link SpaceType#NEWLINE}).
     *
     * @throws IOException if source generation fails
     */
    @Test
    void spaceCommaEnumConstant() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Dir", sf -> {
            sf.enum_("Dir", ec -> {
                ec.constant("N", c -> {});
                ec.constant("S", c -> {});
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Dir");
        assertFalse(defaultOutput.contains("N, S"), "default should not contain 'N, S' on one line");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.COMMA_ENUM_CONSTANT, SpaceType.SPACE)
            .wrapMode(Wrapping.ENUM_CONSTANT_LIST, WrappingMode.NEVER)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Dir2", sf -> {
            sf.enum_("Dir2", ec -> {
                ec.constant("N", c -> {});
                ec.constant("S", c -> {});
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Dir2");
        assertTrue(modifiedOutput.contains("N, S"), "modified should contain 'N, S' on one line");
    }

    /**
     * Verifies that setting {@link Space#AFTER_COMMA_RECORD_COMPONENT} to {@link SpaceType#NONE}
     * removes the space after commas between record components.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void noSpaceCommaRecordComponent() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Point", sf -> {
            sf.record_("Point", rc -> {
                rc.component("x", JType.INT);
                rc.component("y", JType.INT);
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Point");
        assertTrue(defaultOutput.contains("int x, int y"), "default should contain 'int x, int y'");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.AFTER_COMMA_RECORD_COMPONENT, SpaceType.NONE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Point2", sf -> {
            sf.record_("Point2", rc -> {
                rc.component("x", JType.INT);
                rc.component("y", JType.INT);
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Point2");
        assertTrue(modifiedOutput.contains("int x,int y"), "modified should contain 'int x,int y'");
        assertFalse(modifiedOutput.contains("int x, int y"), "modified should not contain 'int x, int y'");
    }

    // ── Batch 12: Param Annotation ──────────────────────────────────────

    /**
     * Verifies that setting {@link Space#AFTER_PARAM_ANNOTATION} to {@link SpaceType#NEWLINE}
     * places the parameter type on a new line after its annotation.
     * NONE cannot downgrade below the keyword's auto-spacing (SPACE level), so NEWLINE is
     * the only non-default value that has an observable effect here.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void newlineAfterParamAnnotation() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.param("x", JType.INT, pc -> {
                        pc.annotate(JType.named("javax.annotation.Nonnull"));
                    });
                    mc.body(BlockCreator::return_);
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains("Nonnull int"), "default should contain 'Nonnull int' on one line");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.AFTER_PARAM_ANNOTATION, SpaceType.NEWLINE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.param("x", JType.INT, pc -> {
                        pc.annotate(JType.named("javax.annotation.Nonnull"));
                    });
                    mc.body(BlockCreator::return_);
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertFalse(modifiedOutput.contains("Nonnull int"), "modified should not contain 'Nonnull int' on one line");
    }

    // ── Batch 13: Indentation ───────────────────────────────────────────

    /**
     * Verifies that changing {@link Indentation#MEMBERS_TOP_LEVEL} from 4 to 8
     * increases the indentation of class members.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void customMembersTopLevelIndent() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.field("x", fc -> {
                    fc.type(JType.INT);
                    fc.init(JExpr.ZERO);
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains("    int x"), "default should indent field by 4 spaces");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .indent(Indentation.MEMBERS_TOP_LEVEL, 8)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.field("x", fc -> {
                    fc.type(JType.INT);
                    fc.init(JExpr.ZERO);
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertTrue(modifiedOutput.contains("        int x"), "modified should indent field by 8 spaces");
        assertFalse(modifiedOutput.contains("\n    int x"), "modified should not indent field by only 4 spaces");
    }

    /**
     * Verifies that changing {@link Indentation#LINE} from 4 to 2
     * reduces the indentation of block-level content.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void customLineIndent() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.return_();
                    });
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains("        return;"), "default should indent return by 8 spaces (4 members + 4 line)");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .indent(Indentation.LINE, 2)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.return_();
                    });
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertTrue(modifiedOutput.contains("      return;"), "modified should indent return by 6 spaces (4 members + 2 line)");
    }

    /**
     * Verifies that changing {@link Indentation#CASE_LABELS} from 0 to 4
     * increases the indentation of case labels within a switch.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void customCaseLabelsIndent() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.switch_(JExpr.$v("x"), sc -> {
                            sc.case_(JExpr.ONE, BlockCreator::empty);
                            sc.default_(BlockCreator::empty);
                        });
                    });
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .indent(Indentation.CASE_LABELS, 4)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.switch_(JExpr.$v("x"), sc -> {
                            sc.case_(JExpr.ONE, BlockCreator::empty);
                            sc.default_(BlockCreator::empty);
                        });
                    });
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertFalse(defaultOutput.equals(modifiedOutput), "changing CASE_LABELS indent should produce different output");
    }

    // ── Batch 14: SpaceType.NEWLINE ─────────────────────────────────────

    /**
     * Verifies that setting {@link Space#BEFORE_BRACE_METHOD} to {@link SpaceType#NEWLINE}
     * places the opening brace on a new line (Allman style).
     *
     * @throws IOException if source generation fails
     */
    @Test
    void newlineBeforeBraceMethod() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(BlockCreator::return_);
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains("run() {"), "default should have brace on same line");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.BEFORE_BRACE_METHOD, SpaceType.NEWLINE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(BlockCreator::return_);
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertFalse(modifiedOutput.contains("run() {"), "modified should not have brace on same line");
    }

    /**
     * Verifies that setting {@link Space#BEFORE_BRACE_IF} to {@link SpaceType#NEWLINE}
     * places the opening brace of an {@code if} body on a new line.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void newlineBeforeBraceIf() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.if_(JExpr.$v("x").gt(JExpr.ZERO), BlockCreator::return_);
                    });
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains("0) {"), "default should have brace on same line as if condition");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.BEFORE_BRACE_IF, SpaceType.NEWLINE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.if_(JExpr.$v("x").gt(JExpr.ZERO), BlockCreator::return_);
                    });
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertFalse(modifiedOutput.contains("0) {"), "modified should not have if brace on same line as condition");
    }

    /**
     * Verifies that setting {@link Space#BEFORE_KEYWORD_ELSE} to {@link SpaceType#NEWLINE}
     * places the {@code else} keyword on a new line.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void newlineBeforeKeywordElse() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.ifElse(JExpr.$v("x").gt(JExpr.ZERO), BlockCreator::return_, BlockCreator::return_);
                    });
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains("} else"), "default should have else on same line as }");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.BEFORE_KEYWORD_ELSE, SpaceType.NEWLINE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.ifElse(JExpr.$v("x").gt(JExpr.ZERO), BlockCreator::return_, BlockCreator::return_);
                    });
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertFalse(modifiedOutput.contains("} else"), "modified should not have else on same line as }");
    }

    /**
     * Verifies that setting {@link Space#AFTER_COMMA} to {@link SpaceType#NEWLINE}
     * inserts a newline after each comma in a method call argument list.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void newlineAfterComma() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.emit(JExpr.callPlain("method", JExpr.$v("a"), JExpr.$v("b")));
                    });
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains("method(a, b)"), "default should have args on same line");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.AFTER_COMMA, SpaceType.NEWLINE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.emit(JExpr.callPlain("method", JExpr.$v("a"), JExpr.$v("b")));
                    });
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertFalse(modifiedOutput.contains("method(a, b)"), "modified should not have args on same line");
    }

    /**
     * Verifies that setting {@link Space#AROUND_ASSIGN} to {@link SpaceType#NEWLINE}
     * inserts newlines around the assignment operator.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void newlineAroundAssign() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.emit(JExpr.$v("x").assign(JExpr.ZERO));
                    });
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains("x = 0"), "default should have assignment on one line");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.AROUND_ASSIGN, SpaceType.NEWLINE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.emit(JExpr.$v("x").assign(JExpr.ZERO));
                    });
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertFalse(modifiedOutput.contains("x = 0"), "modified should not have assignment on one line");
    }

    // ── Batch 15: WITHIN_BRACES_EMPTY ──────────────────────────────────

    /**
     * Verifies that {@link Space#WITHIN_BRACES_EMPTY} is respected for empty array
     * initializers: default ({@link SpaceType#NONE}) produces {@code {}} and
     * {@link SpaceType#SPACE} produces {@code { }}.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void withinBracesEmptyArrayInit() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.field("nums", fc -> {
                    fc.type(JType.INT.array());
                    fc.init(JType.INT.array().newArrayInit());
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains("[] {}"), "default (NONE) should produce empty braces with no space inside");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.WITHIN_BRACES_EMPTY, SpaceType.SPACE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.field("nums", fc -> {
                    fc.type(JType.INT.array());
                    fc.init(JType.INT.array().newArrayInit());
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertTrue(modifiedOutput.contains("[] { }"), "SPACE should produce empty braces with space inside");
        assertFalse(modifiedOutput.contains("[] {}"), "SPACE should not produce empty braces without space");
    }

    /**
     * Verifies that {@link Space#WITHIN_BRACES_EMPTY} is respected for empty
     * control structure blocks: when the {@code if} body is empty and the
     * preference is {@link SpaceType#NONE} (default), the braces should be
     * {@code {}} with no content inside; when set to {@link SpaceType#SPACE},
     * the braces should be {@code { }} on a single line.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void withinBracesEmptyBlock() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.if_(JExpr.$v("x").gt(JExpr.ZERO), ifBody -> {});
                    });
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains(") {}"), "default (NONE) should produce empty block braces with no content inside");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.WITHIN_BRACES_EMPTY, SpaceType.SPACE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.if_(JExpr.$v("x").gt(JExpr.ZERO), ifBody -> {});
                    });
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertTrue(modifiedOutput.contains(") { }"), "SPACE should produce empty block braces with space inside");
    }

    /**
     * Verifies that {@link Space#WITHIN_BRACES_EMPTY} is respected for empty
     * anonymous class bodies: when the anonymous class has no members and the
     * preference is {@link SpaceType#NONE} (default), the braces should be
     * {@code {}} with no content; when set to {@link SpaceType#SPACE}, they
     * should be {@code { }} on a single line.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void withinBracesEmptyAnonymousClass() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.field("obj", fc -> {
                    fc.type(JType.OBJECT);
                    fc.init(JType.OBJECT.new_(SourceVersion.JAVA_17, List.of(), body -> {}));
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains(") {}"), "default (NONE) should produce empty anonymous class braces with no content");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.WITHIN_BRACES_EMPTY, SpaceType.SPACE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.field("obj", fc -> {
                    fc.type(JType.OBJECT);
                    fc.init(JType.OBJECT.new_(SourceVersion.JAVA_17, List.of(), body -> {}));
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertTrue(modifiedOutput.contains(") { }"), "SPACE should produce empty anonymous class braces with space inside");
    }

    /**
     * Verifies that {@link Space#WITHIN_BRACES_EMPTY} is respected for empty
     * lambda block bodies: when the lambda block has no statements and the
     * preference is {@link SpaceType#NONE} (default), the braces should be
     * {@code {}} with no content; when set to {@link SpaceType#SPACE}, they
     * should be {@code { }} on a single line.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void withinBracesEmptyLambda() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.field("fn", fc -> {
                    fc.type(JType.OBJECT);
                    fc.init(JExpr.lambda(SourceVersion.JAVA_17, "x", body -> {}));
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains("-> {}"), "default (NONE) should produce empty lambda braces with no content");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.WITHIN_BRACES_EMPTY, SpaceType.SPACE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.field("fn", fc -> {
                    fc.type(JType.OBJECT);
                    fc.init(JExpr.lambda(SourceVersion.JAVA_17, "x", body -> {}));
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertTrue(modifiedOutput.contains("-> { }"), "SPACE should produce empty lambda braces with space inside");
    }

    /**
     * Verifies that {@link Space#WITHIN_BRACES_EMPTY} is respected for empty
     * class bodies: when the class has no members and the preference is
     * {@link SpaceType#NONE} (default), the braces should be {@code {}} with
     * no content; when set to {@link SpaceType#SPACE}, they should be
     * {@code { }} on a single line.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void withinBracesEmptyClassBody() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Empty", sf -> {
            sf.class_("Empty", cc -> {});
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Empty");
        assertTrue(defaultOutput.contains("Empty {}"), "default (NONE) should produce empty class braces with no content");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.WITHIN_BRACES_EMPTY, SpaceType.SPACE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Empty2", sf -> {
            sf.class_("Empty2", cc -> {});
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Empty2");
        assertTrue(modifiedOutput.contains("Empty2 { }"), "SPACE should produce empty class braces with space inside");
    }

    // ── Batch 16: Opt ──────────────────────────────────────────────────

    /**
     * Verifies that enabling {@link Opt#ENUM_TRAILING_COMMA} adds a comma
     * after the last enum constant before the semicolon.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void enumTrailingComma() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Color", sf -> {
            sf.enum_("Color", ec -> {
                ec.constant("RED", c -> {});
                ec.constant("GREEN", c -> {});
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Color");
        assertTrue(defaultOutput.contains("GREEN;"), "default should not have trailing comma");
        assertFalse(defaultOutput.contains("GREEN,;"), "default should not have trailing comma before semicolon");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .addOption(Opt.ENUM_TRAILING_COMMA)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Color2", sf -> {
            sf.enum_("Color2", ec -> {
                ec.constant("RED", c -> {});
                ec.constant("GREEN", c -> {});
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Color2");
        assertTrue(modifiedOutput.contains("GREEN,;"), "modified should have trailing comma before semicolon");
    }

    /**
     * Verifies that enabling {@link Opt#ENUM_EMPTY_PARENS} adds empty
     * parentheses to enum constants with no constructor arguments.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void enumEmptyParens() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Color", sf -> {
            sf.enum_("Color", ec -> {
                ec.constant("RED", c -> {});
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Color");
        assertFalse(defaultOutput.contains("RED()"), "default should not have empty parens");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .addOption(Opt.ENUM_EMPTY_PARENS)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Color2", sf -> {
            sf.enum_("Color2", ec -> {
                ec.constant("RED", c -> {});
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Color2");
        assertTrue(modifiedOutput.contains("RED()"), "modified should have empty parens on constant");
    }

    /**
     * Verifies that {@link Opt#COMPACT_INIT_ONLY_CLASS} (enabled by default) produces
     * compact double-brace formatting for anonymous classes with a single instance
     * initializer, and that disabling it produces the expanded form.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void compactInitOnlyClass() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.field("obj", fc -> {
                    fc.type(JType.OBJECT);
                    fc.init(JType.OBJECT.new_(SourceVersion.JAVA_17, List.of(), body -> {
                        body.instanceInit(init -> {
                            init.emit(JExpr.callPlain("init"));
                        });
                    }));
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains("() {{"), "default (compact) should have adjacent opening braces");
        assertTrue(defaultOutput.contains("}}"), "default (compact) should have adjacent closing braces");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .removeOption(Opt.COMPACT_INIT_ONLY_CLASS)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.field("obj", fc -> {
                    fc.type(JType.OBJECT);
                    fc.init(JType.OBJECT.new_(SourceVersion.JAVA_17, List.of(), body -> {
                        body.instanceInit(init -> {
                            init.emit(JExpr.callPlain("init"));
                        });
                    }));
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertFalse(modifiedOutput.contains("() {{"), "non-compact should not have adjacent opening braces");
        assertFalse(modifiedOutput.contains("}}"), "non-compact should not have adjacent closing braces");
    }

    // ── Batch 17: Enum constant WITHIN_PAREN ───────────────────────────

    /**
     * Verifies that setting {@link Space#WITHIN_PAREN_METHOD_CALL} to {@link SpaceType#SPACE}
     * adds spaces inside the parentheses of enum constant constructor arguments.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void spaceWithinParenEnumConstantArgs() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Color", sf -> {
            sf.enum_("Color", ec -> {
                ec.constant("RED", c -> {
                    c.arg(JExpr.decimal(1));
                    c.arg(JExpr.decimal(2));
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Color");
        assertTrue(defaultOutput.contains("RED(1, 2)"), "default should have no space inside parens");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.WITHIN_PAREN_METHOD_CALL, SpaceType.SPACE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Color2", sf -> {
            sf.enum_("Color2", ec -> {
                ec.constant("RED", c -> {
                    c.arg(JExpr.decimal(1));
                    c.arg(JExpr.decimal(2));
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Color2");
        assertTrue(modifiedOutput.contains("RED( 1, 2 )"), "modified should have space inside parens");
    }

    /**
     * Verifies that setting {@link Space#WITHIN_PAREN_METHOD_CALL_EMPTY} to {@link SpaceType#SPACE}
     * adds a space inside the empty parentheses of enum constants when
     * {@link Opt#ENUM_EMPTY_PARENS} is enabled.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void spaceWithinParenEnumConstantEmpty() throws IOException {
        final FormatPreferences defaultPrefs = FormatPreferences.builder()
            .addOption(Opt.ENUM_EMPTY_PARENS)
            .build();
        final JSources defaultSources = createSources(defaultPrefs, SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Color", sf -> {
            sf.enum_("Color", ec -> {
                ec.constant("RED", c -> {});
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Color");
        assertTrue(defaultOutput.contains("RED()"), "default should have empty parens with no space inside");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .addOption(Opt.ENUM_EMPTY_PARENS)
            .space(Space.WITHIN_PAREN_METHOD_CALL_EMPTY, SpaceType.SPACE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Color2", sf -> {
            sf.enum_("Color2", ec -> {
                ec.constant("RED", c -> {});
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Color2");
        assertTrue(modifiedOutput.contains("RED( )"), "modified should have space inside empty parens");
    }

    // ── Batch 18: AFTER_SEMICOLON_EMPTY ────────────────────────────────

    /**
     * Verifies that {@link Space#AFTER_SEMICOLON_EMPTY} (default {@link SpaceType#NONE})
     * controls the space after semicolons in {@code for(;;)} loops when the following
     * part is absent.  With the default, {@code for(;;)} has no spaces between
     * semicolons; setting it to {@link SpaceType#SPACE} produces {@code for(; ; )}.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void afterSemicolonEmptyForLoop() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.for_(fc -> {
                            fc.body(body -> {
                                body.break_();
                            });
                        });
                    });
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains("for (;;)"),
            "default (NONE) should produce for (;;) with no spaces between semicolons");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .space(Space.AFTER_SEMICOLON_EMPTY, SpaceType.SPACE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.for_(fc -> {
                            fc.body(body -> {
                                body.break_();
                            });
                        });
                    });
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertTrue(modifiedOutput.contains("for (; ; )"),
            "SPACE should produce for (; ; ) with spaces between semicolons");
    }

    /**
     * Verifies that {@link Space#AFTER_SEMICOLON_EMPTY} only affects empty for-loop parts.
     * When a for loop has a condition but no update, only the second semicolon uses
     * {@link Space#AFTER_SEMICOLON_EMPTY}; the first semicolon still uses
     * {@link Space#AFTER_SEMICOLON}.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void afterSemicolonEmptyMixed() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.for_(fc -> {
                            fc.condition(JExpr.$v("x").gt(JExpr.ZERO));
                            fc.body(body -> {
                                body.break_();
                            });
                        });
                    });
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        // first semicolon: condition present → AFTER_SEMICOLON (SPACE)
        // second semicolon: update absent → AFTER_SEMICOLON_EMPTY (NONE)
        assertTrue(defaultOutput.contains("; x > 0;)"),
            "default should have space after first ; (AFTER_SEMICOLON) but not after second ; (AFTER_SEMICOLON_EMPTY)");
    }

    // ── Batch 19: Wrapping ─────────────────────────────────────────────

    /**
     * Verifies that setting {@link Wrapping#ARGUMENT_LIST} to
     * {@link WrappingMode#ALWAYS_WRAP} places each method call argument on its own line.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void alwaysWrapArgumentList() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.emit(JExpr.callPlain("method", JExpr.$v("a"), JExpr.$v("b")));
                    });
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains("method(a, b)"), "default should have args on same line");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .wrapMode(Wrapping.ARGUMENT_LIST, WrappingMode.ALWAYS_WRAP)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.emit(JExpr.callPlain("method", JExpr.$v("a"), JExpr.$v("b")));
                    });
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertFalse(modifiedOutput.contains("method(a, b)"),
            "ALWAYS_WRAP should not have both args on same line");
        assertFalse(modifiedOutput.contains(", b"),
            "ALWAYS_WRAP should wrap after comma instead of adding space");
    }

    /**
     * Verifies that setting {@link Wrapping#PARAMETER_LIST} to
     * {@link WrappingMode#ALWAYS_WRAP} places each method parameter on its own line.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void alwaysWrapParameterList() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.param("a", JType.INT);
                    mc.param("b", JType.INT);
                    mc.body(b -> {});
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains("int a, int b"),
            "default should have params on same line");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .wrapMode(Wrapping.PARAMETER_LIST, WrappingMode.ALWAYS_WRAP)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.param("a", JType.INT);
                    mc.param("b", JType.INT);
                    mc.body(b -> {});
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertFalse(modifiedOutput.contains("int a, int b"),
            "ALWAYS_WRAP should not have params on same line");
    }

    /**
     * Verifies that setting {@link Wrapping#IMPLEMENTS_LIST} to
     * {@link WrappingMode#ALWAYS_WRAP} places each implemented interface on its own line.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void alwaysWrapImplementsList() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.implements_(JType.named("java.io.Serializable"));
                cc.implements_(JType.named("java.lang.Comparable"));
                cc.method("compareTo", mc -> {
                    mc.param("o", JType.OBJECT);
                    mc.returning(JType.INT);
                    mc.body(b -> {
                        b.return_(JExpr.ZERO);
                    });
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains("Serializable, Comparable"),
            "default should have interfaces on same line");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .wrapMode(Wrapping.IMPLEMENTS_LIST, WrappingMode.ALWAYS_WRAP)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.implements_(JType.named("java.io.Serializable"));
                cc.implements_(JType.named("java.lang.Comparable"));
                cc.method("compareTo", mc -> {
                    mc.param("o", JType.OBJECT);
                    mc.returning(JType.INT);
                    mc.body(b -> {
                        b.return_(JExpr.ZERO);
                    });
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertFalse(modifiedOutput.contains("Serializable, Comparable"),
            "ALWAYS_WRAP should not have interfaces on same line");
    }

    /**
     * Verifies that setting {@link Wrapping#EXCEPTION_LIST} to
     * {@link WrappingMode#ALWAYS_WRAP} places each thrown exception type on its own line.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void alwaysWrapExceptionList() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.throws_(JType.named("java.io.IOException"));
                    mc.throws_(JType.named("java.sql.SQLException"));
                    mc.body(b -> {});
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains("IOException, "),
            "default should have exceptions on same line");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .wrapMode(Wrapping.EXCEPTION_LIST, WrappingMode.ALWAYS_WRAP)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.throws_(JType.named("java.io.IOException"));
                    mc.throws_(JType.named("java.sql.SQLException"));
                    mc.body(b -> {});
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertFalse(modifiedOutput.contains("IOException, "),
            "ALWAYS_WRAP should not have exceptions on same line");
    }

    /**
     * Verifies that {@link Wrapping#ENUM_CONSTANT_LIST} defaults to
     * {@link WrappingMode#ALWAYS_WRAP} (each constant on its own line), and that
     * setting it to {@link WrappingMode#NEVER} with
     * {@link Space#COMMA_ENUM_CONSTANT} set to {@link SpaceType#SPACE} places
     * all constants on one line.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void enumConstantListWrapping() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Dir", sf -> {
            sf.enum_("Dir", ec -> {
                ec.constant("N", c -> {});
                ec.constant("S", c -> {});
                ec.constant("E", c -> {});
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Dir");
        assertFalse(defaultOutput.contains("N, S"),
            "default (ALWAYS_WRAP) should not have constants on same line");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .wrapMode(Wrapping.ENUM_CONSTANT_LIST, WrappingMode.NEVER)
            .space(Space.COMMA_ENUM_CONSTANT, SpaceType.SPACE)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Dir2", sf -> {
            sf.enum_("Dir2", ec -> {
                ec.constant("N", c -> {});
                ec.constant("S", c -> {});
                ec.constant("E", c -> {});
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Dir2");
        assertTrue(modifiedOutput.contains("N, S, E"),
            "NEVER wrap should have all constants on same line");
    }

    /**
     * Verifies that setting {@link Wrapping#RECORD_COMPONENT_LIST} to
     * {@link WrappingMode#ALWAYS_WRAP} places each record component on its own line.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void alwaysWrapRecordComponentList() throws IOException {
        final JSources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Point", sf -> {
            sf.record_("Point", rc -> {
                rc.component("x", JType.INT);
                rc.component("y", JType.INT);
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Point");
        assertTrue(defaultOutput.contains("int x, int y"),
            "default should have components on same line");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
            .wrapMode(Wrapping.RECORD_COMPONENT_LIST, WrappingMode.ALWAYS_WRAP)
            .build();
        final JSources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Point2", sf -> {
            sf.record_("Point2", rc -> {
                rc.component("x", JType.INT);
                rc.component("y", JType.INT);
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Point2");
        assertFalse(modifiedOutput.contains("int x, int y"),
            "ALWAYS_WRAP should not have components on same line");
    }
}
