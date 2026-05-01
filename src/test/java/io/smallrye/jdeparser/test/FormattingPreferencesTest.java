package io.smallrye.jdeparser.test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.smallrye.jdeparser.Expr;
import io.smallrye.jdeparser.SourceVersion;
import io.smallrye.jdeparser.Sources;
import io.smallrye.jdeparser.Type;
import io.smallrye.jdeparser.creator.BlockCreator;
import io.smallrye.jdeparser.format.FormatPreferences;
import io.smallrye.jdeparser.format.FormatPreferences.Indentation;
import io.smallrye.jdeparser.format.FormatPreferences.Opt;
import io.smallrye.jdeparser.format.FormatPreferences.Space;
import io.smallrye.jdeparser.format.FormatPreferences.SpaceType;
import io.smallrye.jdeparser.format.FormatPreferences.Wrapping;
import io.smallrye.jdeparser.format.FormatPreferences.WrappingMode;

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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("test", mc -> {
                    mc.param("x", Type.INT);
                    mc.body(b -> {
                        b.if_(Expr.$v("x").gt(Expr.ZERO), BlockCreator::return_);
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("test", mc -> {
                    mc.param("x", Type.INT);
                    mc.body(b -> {
                        b.if_(Expr.$v("x").gt(Expr.ZERO), BlockCreator::return_);
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.emit(Expr.$v("x").assign(Expr.ZERO));
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.emit(Expr.$v("x").assign(Expr.ZERO));
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.field("nums", fc -> {
                    fc.type(Type.INT.array());
                    fc.init(Type.INT.array().newArrayInit(Expr.decimal(1), Expr.decimal(2)));
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.field("nums", fc -> {
                    fc.type(Type.INT.array());
                    fc.init(Type.INT.array().newArrayInit(Expr.decimal(1), Expr.decimal(2)));
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.emit(Expr.callPlain("method", Expr.$v("a"), Expr.$v("b")));
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.emit(Expr.callPlain("method", Expr.$v("a"), Expr.$v("b")));
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.field("fn", fc -> {
                    fc.type(Type.OBJECT);
                    fc.init(Expr.lambda("x", Expr.$v("x")));
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.field("fn", fc -> {
                    fc.type(Type.OBJECT);
                    fc.init(Expr.lambda("x", Expr.$v("x")));
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.param("n", Type.INT);
                    mc.body(b -> {
                        b.for_(fb -> {
                            fb.init(Type.INT, "i", Expr.ZERO);
                            fb.condition(Expr.$v("i").lt(Expr.$v("n")));
                            fb.update(Expr.$v("i").inc());
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.param("n", Type.INT);
                    mc.body(b -> {
                        b.for_(fb -> {
                            fb.init(Type.INT, "i", Expr.ZERO);
                            fb.condition(Expr.$v("i").lt(Expr.$v("n")));
                            fb.update(Expr.$v("i").inc());
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.while_(Expr.$v("x").gt(Expr.ZERO), BlockCreator::empty);
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.while_(Expr.$v("x").gt(Expr.ZERO), BlockCreator::empty);
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.param("n", Type.INT);
                    mc.body(b -> {
                        b.for_(fb -> {
                            fb.init(Type.INT, "i", Expr.ZERO);
                            fb.condition(Expr.$v("i").lt(Expr.$v("n")));
                            fb.update(Expr.$v("i").inc());
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.param("n", Type.INT);
                    mc.body(b -> {
                        b.for_(fb -> {
                            fb.init(Type.INT, "i", Expr.ZERO);
                            fb.condition(Expr.$v("i").lt(Expr.$v("n")));
                            fb.update(Expr.$v("i").inc());
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.field("flag", fc -> {
                    fc.type(Type.BOOLEAN);
                    fc.init(Expr.$v("x").gt(Expr.ZERO));
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.field("flag", fc -> {
                    fc.type(Type.BOOLEAN);
                    fc.init(Expr.$v("x").gt(Expr.ZERO));
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.field("flag", fc -> {
                    fc.type(Type.BOOLEAN);
                    fc.init(Expr.$v("a").and(Expr.$v("b")));
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.field("flag", fc -> {
                    fc.type(Type.BOOLEAN);
                    fc.init(Expr.$v("a").and(Expr.$v("b")));
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.field("flag", fc -> {
                    fc.type(Type.BOOLEAN);
                    fc.init(Expr.$v("x").eq(Expr.$v("y")));
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.field("flag", fc -> {
                    fc.type(Type.BOOLEAN);
                    fc.init(Expr.$v("x").eq(Expr.$v("y")));
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.field("val", fc -> {
                    fc.type(Type.INT);
                    fc.init(Expr.$v("a").bitOr(Expr.$v("b")));
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.field("val", fc -> {
                    fc.type(Type.INT);
                    fc.init(Expr.$v("a").bitOr(Expr.$v("b")));
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.field("val", fc -> {
                    fc.type(Type.INT);
                    fc.init(Expr.$v("a").add(Expr.$v("b")));
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.field("val", fc -> {
                    fc.type(Type.INT);
                    fc.init(Expr.$v("a").add(Expr.$v("b")));
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.field("val", fc -> {
                    fc.type(Type.INT);
                    fc.init(Expr.$v("a").mul(Expr.$v("b")));
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.field("val", fc -> {
                    fc.type(Type.INT);
                    fc.init(Expr.$v("a").mul(Expr.$v("b")));
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.field("val", fc -> {
                    fc.type(Type.INT);
                    fc.init(Expr.$v("a").shl(Expr.$v("b")));
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.field("val", fc -> {
                    fc.type(Type.INT);
                    fc.init(Expr.$v("a").shl(Expr.$v("b")));
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.field("fn", fc -> {
                    fc.type(Type.OBJECT);
                    fc.init(Type.STRING.methodRef("valueOf"));
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.field("fn", fc -> {
                    fc.type(Type.OBJECT);
                    fc.init(Type.STRING.methodRef("valueOf"));
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.field("val", fc -> {
                    fc.type(Type.OBJECT);
                    fc.init(Expr.$v("x").cond(Expr.$v("a"), Expr.$v("b")));
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.field("val", fc -> {
                    fc.type(Type.OBJECT);
                    fc.init(Expr.$v("x").cond(Expr.$v("a"), Expr.$v("b")));
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.field("val", fc -> {
                    fc.type(Type.OBJECT);
                    fc.init(Expr.$v("x").cond(Expr.$v("a"), Expr.$v("b")));
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.field("val", fc -> {
                    fc.type(Type.OBJECT);
                    fc.init(Expr.$v("x").cond(Expr.$v("a"), Expr.$v("b")));
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.field("val", fc -> {
                    fc.type(Type.OBJECT);
                    fc.init(Expr.$v("x").cond(Expr.$v("a"), Expr.$v("b")));
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.field("val", fc -> {
                    fc.type(Type.OBJECT);
                    fc.init(Expr.$v("x").cond(Expr.$v("a"), Expr.$v("b")));
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.field("val", fc -> {
                    fc.type(Type.OBJECT);
                    fc.init(Expr.$v("x").cond(Expr.$v("a"), Expr.$v("b")));
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.field("val", fc -> {
                    fc.type(Type.OBJECT);
                    fc.init(Expr.$v("x").cond(Expr.$v("a"), Expr.$v("b")));
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.field("val", fc -> {
                    fc.type(Type.STRING);
                    fc.init(Expr.$v("obj").cast(Type.STRING));
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.field("val", fc -> {
                    fc.type(Type.STRING);
                    fc.init(Expr.$v("obj").cast(Type.STRING));
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.typeParam("T", tp -> {
                    tp.extends_(Type.named("java.io.Serializable"), Type.named("java.lang.Comparable"));
                });
                cc.method("run", mc -> {
                    mc.body(BlockCreator::return_);
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains("Serializable & Comparable"),
                "default should contain 'Serializable & Comparable' (space on both sides)");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
                .space(Space.AROUND_TYPE_BOUND_AND, SpaceType.NONE)
                .build();
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.typeParam("T", tp -> {
                    tp.extends_(Type.named("java.io.Serializable"), Type.named("java.lang.Comparable"));
                });
                cc.method("run", mc -> {
                    mc.body(BlockCreator::return_);
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertTrue(modifiedOutput.contains("Serializable&Comparable"), "modified should contain 'Serializable&Comparable'");
        assertFalse(modifiedOutput.contains("Serializable & Comparable"),
                "modified should not contain 'Serializable & Comparable'");
    }

    /**
     * Verifies that setting {@link Space#AROUND_MULTI_CATCH_OR} to {@link SpaceType#NONE}
     * removes spaces around the {@code |} in multi-catch clauses.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void noSpaceAroundMultiCatchOr() throws IOException {
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.try_(tc -> {
                            tc.body(BlockCreator::empty);
                            tc.catch_(List.of(
                                    Type.named("java.io.IOException"),
                                    Type.named("java.sql.SQLException")),
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.try_(tc -> {
                            tc.body(BlockCreator::empty);
                            tc.catch_(List.of(
                                    Type.named("java.io.IOException"),
                                    Type.named("java.sql.SQLException")),
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Color", sf -> {
            sf.enum_("Color", ec -> {
                ec.constant("RED", c -> {
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Color");
        assertTrue(defaultOutput.contains("Color {"), "default should contain 'Color {'");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
                .space(Space.BEFORE_BRACE_ENUM, SpaceType.NONE)
                .build();
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Color2", sf -> {
            sf.enum_("Color2", ec -> {
                ec.constant("RED", c -> {
                });
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Point", sf -> {
            sf.record_("Point", rc -> {
                rc.component("x", Type.INT);
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Point");
        assertTrue(defaultOutput.contains(") {"), "default should contain ') {'");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
                .space(Space.BEFORE_BRACE_RECORD, SpaceType.NONE)
                .build();
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Point2", sf -> {
            sf.record_("Point2", rc -> {
                rc.component("x", Type.INT);
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "MyAnno", sf -> {
            sf.annotationInterface_("MyAnno", ac -> {
                ac.element("value", Type.STRING);
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "MyAnno");
        assertTrue(defaultOutput.contains("MyAnno {"), "default should contain 'MyAnno {'");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
                .space(Space.BEFORE_BRACE_ANNOTATION_TYPE, SpaceType.NONE)
                .build();
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "MyAnno2", sf -> {
            sf.annotationInterface_("MyAnno2", ac -> {
                ac.element("value", Type.STRING);
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.if_(Expr.$v("x").gt(Expr.ZERO), BlockCreator::return_);
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.if_(Expr.$v("x").gt(Expr.ZERO), BlockCreator::return_);
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.ifElse(Expr.$v("x").gt(Expr.ZERO), BlockCreator::return_, BlockCreator::return_);
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.ifElse(Expr.$v("x").gt(Expr.ZERO), BlockCreator::return_, BlockCreator::return_);
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.for_(fb -> {
                            fb.init(Type.INT, "i", Expr.ZERO);
                            fb.condition(Expr.$v("i").lt(Expr.$v("n")));
                            fb.update(Expr.$v("i").inc());
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.for_(fb -> {
                            fb.init(Type.INT, "i", Expr.ZERO);
                            fb.condition(Expr.$v("i").lt(Expr.$v("n")));
                            fb.update(Expr.$v("i").inc());
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.while_(Expr.$v("x").gt(Expr.ZERO), BlockCreator::empty);
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.while_(Expr.$v("x").gt(Expr.ZERO), BlockCreator::empty);
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.doWhile(BlockCreator::empty, Expr.$v("x").gt(Expr.ZERO));
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.doWhile(BlockCreator::empty, Expr.$v("x").gt(Expr.ZERO));
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.switch_(Expr.$v("x"), sc -> {
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.switch_(Expr.$v("x"), sc -> {
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.try_(tc -> {
                            tc.body(BlockCreator::empty);
                            tc.catch_(Type.named("java.lang.Exception"), "e", BlockCreator::empty);
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.try_(tc -> {
                            tc.body(BlockCreator::empty);
                            tc.catch_(Type.named("java.lang.Exception"), "e", BlockCreator::empty);
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.synchronized_(Expr.$v("lock"), BlockCreator::empty);
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.synchronized_(Expr.$v("lock"), BlockCreator::empty);
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
        final FormatPreferences blockPrefs = FormatPreferences.builder()
                .addOption(FormatPreferences.Opt.LAMBDA_ALWAYS_BLOCK_BODY)
                .build();
        final Sources defaultSources = createSources(blockPrefs, SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.field("fn", fc -> {
                    fc.type(Type.OBJECT);
                    fc.init(Expr.lambda(lc -> {
                        lc.param("x");
                        lc.body(body -> body.return_(Expr.$v("x")));
                    }));
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains("-> {"), "default should contain '-> {'");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
                .addOption(FormatPreferences.Opt.LAMBDA_ALWAYS_BLOCK_BODY)
                .space(Space.AROUND_ARROW, SpaceType.NONE)
                .space(Space.BEFORE_BRACE_LAMBDA, SpaceType.NONE)
                .build();
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.field("fn", fc -> {
                    fc.type(Type.OBJECT);
                    fc.init(Expr.lambda(lc -> {
                        lc.param("x");
                        lc.body(body -> body.return_(Expr.$v("x")));
                    }));
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.field("nums", fc -> {
                    fc.type(Type.INT.array());
                    fc.init(Type.INT.array().newArrayInit(Expr.decimal(1)));
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.field("nums", fc -> {
                    fc.type(Type.INT.array());
                    fc.init(Type.INT.array().newArrayInit(Expr.decimal(1)));
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.switch_(Expr.$v("x"), sc -> sc.default_(BlockCreator::empty));
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.switch_(Expr.$v("x"), sc -> sc.default_(BlockCreator::empty));
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.try_(tc -> {
                            tc.with(Type.named("java.io.InputStream"), "is", Expr.$v("getStream").call("open"));
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.try_(tc -> {
                            tc.with(Type.named("java.io.InputStream"), "is", Expr.$v("getStream").call("open"));
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.try_(tc -> {
                            tc.body(BlockCreator::empty);
                            tc.catch_(Type.named("java.lang.Exception"), "e", BlockCreator::empty);
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.try_(tc -> {
                            tc.body(BlockCreator::empty);
                            tc.catch_(Type.named("java.lang.Exception"), "e", BlockCreator::empty);
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.synchronized_(Expr.$v("lock"), BlockCreator::empty);
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.synchronized_(Expr.$v("lock"), BlockCreator::empty);
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Point", sf -> {
            sf.record_("Point", rc -> {
                rc.component("x", Type.INT);
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Point");
        assertTrue(defaultOutput.contains("Point("), "default should contain 'Point('");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
                .space(Space.BEFORE_PAREN_RECORD, SpaceType.SPACE)
                .build();
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Point2", sf -> {
            sf.record_("Point2", rc -> {
                rc.component("x", Type.INT);
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.ifElse(Expr.$v("x").gt(Expr.ZERO), BlockCreator::return_, BlockCreator::return_);
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.ifElse(Expr.$v("x").gt(Expr.ZERO), BlockCreator::return_, BlockCreator::return_);
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.doWhile(BlockCreator::empty, Expr.$v("x").gt(Expr.ZERO));
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.doWhile(BlockCreator::empty, Expr.$v("x").gt(Expr.ZERO));
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.try_(tc -> {
                            tc.body(BlockCreator::empty);
                            tc.catch_(Type.named("java.lang.Exception"), "e", BlockCreator::empty);
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.try_(tc -> {
                            tc.body(BlockCreator::empty);
                            tc.catch_(Type.named("java.lang.Exception"), "e", BlockCreator::empty);
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.forEach(Type.STRING, "item", Expr.$v("list"), BlockCreator::empty);
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.forEach(Type.STRING, "item", Expr.$v("list"), BlockCreator::empty);
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.forEach(Type.STRING, "item", Expr.$v("list"), BlockCreator::empty);
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.forEach(Type.STRING, "item", Expr.$v("list"), BlockCreator::empty);
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.labeled("outer", (label, body) -> {
                            body.while_(Expr.$v("x").gt(Expr.ZERO), loop -> loop.break_(label));
                        });
                    });
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertFalse(defaultOutput.contains("outer: {"),
                "default should not contain 'outer: {' on one line (NEWLINE is default)");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
                .space(Space.AFTER_LABEL, SpaceType.SPACE)
                .build();
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.labeled("outer", (label, body) -> {
                            body.while_(Expr.$v("x").gt(Expr.ZERO), loop -> loop.break_(label));
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.typeParam("K", tp -> {
                });
                cc.typeParam("V", tp -> {
                });
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.typeParam("K", tp -> {
                });
                cc.typeParam("V", tp -> {
                });
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Dir", sf -> {
            sf.enum_("Dir", ec -> {
                ec.constant("N", c -> {
                });
                ec.constant("S", c -> {
                });
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Dir2", sf -> {
            sf.enum_("Dir2", ec -> {
                ec.constant("N", c -> {
                });
                ec.constant("S", c -> {
                });
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Point", sf -> {
            sf.record_("Point", rc -> {
                rc.component("x", Type.INT);
                rc.component("y", Type.INT);
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Point");
        assertTrue(defaultOutput.contains("int x, int y"), "default should contain 'int x, int y'");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
                .space(Space.AFTER_COMMA_RECORD_COMPONENT, SpaceType.NONE)
                .build();
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Point2", sf -> {
            sf.record_("Point2", rc -> {
                rc.component("x", Type.INT);
                rc.component("y", Type.INT);
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.param("x", Type.INT, pc -> {
                        pc.annotate(Type.named("javax.annotation.Nonnull"));
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.param("x", Type.INT, pc -> {
                        pc.annotate(Type.named("javax.annotation.Nonnull"));
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.field("x", fc -> {
                    fc.type(Type.INT);
                    fc.init(Expr.ZERO);
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.field("x", fc -> {
                    fc.type(Type.INT);
                    fc.init(Expr.ZERO);
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.switch_(Expr.$v("x"), sc -> {
                            sc.case_(Expr.ONE, BlockCreator::empty);
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.switch_(Expr.$v("x"), sc -> {
                            sc.case_(Expr.ONE, BlockCreator::empty);
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.if_(Expr.$v("x").gt(Expr.ZERO), BlockCreator::return_);
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.if_(Expr.$v("x").gt(Expr.ZERO), BlockCreator::return_);
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.ifElse(Expr.$v("x").gt(Expr.ZERO), BlockCreator::return_, BlockCreator::return_);
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.ifElse(Expr.$v("x").gt(Expr.ZERO), BlockCreator::return_, BlockCreator::return_);
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.emit(Expr.callPlain("method", Expr.$v("a"), Expr.$v("b")));
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.emit(Expr.callPlain("method", Expr.$v("a"), Expr.$v("b")));
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.emit(Expr.$v("x").assign(Expr.ZERO));
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.emit(Expr.$v("x").assign(Expr.ZERO));
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.field("nums", fc -> {
                    fc.type(Type.INT.array());
                    fc.init(Type.INT.array().newArrayInit());
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.field("nums", fc -> {
                    fc.type(Type.INT.array());
                    fc.init(Type.INT.array().newArrayInit());
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.if_(Expr.$v("x").gt(Expr.ZERO), ifBody -> {
                        });
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.if_(Expr.$v("x").gt(Expr.ZERO), ifBody -> {
                        });
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.field("obj", fc -> {
                    fc.type(Type.OBJECT);
                    fc.init(Type.OBJECT.new_(SourceVersion.JAVA_17, List.of(), body -> {
                    }));
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains(") {}"),
                "default (NONE) should produce empty anonymous class braces with no content");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
                .space(Space.WITHIN_BRACES_EMPTY, SpaceType.SPACE)
                .build();
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.field("obj", fc -> {
                    fc.type(Type.OBJECT);
                    fc.init(Type.OBJECT.new_(SourceVersion.JAVA_17, List.of(), body -> {
                    }));
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.field("fn", fc -> {
                    fc.type(Type.OBJECT);
                    fc.init(Expr.lambda(lc -> {
                        lc.param("x");
                        lc.body(body -> {
                        });
                    }));
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.field("fn", fc -> {
                    fc.type(Type.OBJECT);
                    fc.init(Expr.lambda(lc -> {
                        lc.param("x");
                        lc.body(body -> {
                        });
                    }));
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Empty", sf -> {
            sf.class_("Empty", cc -> {
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Empty");
        assertTrue(defaultOutput.contains("Empty {}"), "default (NONE) should produce empty class braces with no content");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
                .space(Space.WITHIN_BRACES_EMPTY, SpaceType.SPACE)
                .build();
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Empty2", sf -> {
            sf.class_("Empty2", cc -> {
            });
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Color", sf -> {
            sf.enum_("Color", ec -> {
                ec.constant("RED", c -> {
                });
                ec.constant("GREEN", c -> {
                });
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Color2", sf -> {
            sf.enum_("Color2", ec -> {
                ec.constant("RED", c -> {
                });
                ec.constant("GREEN", c -> {
                });
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Color", sf -> {
            sf.enum_("Color", ec -> {
                ec.constant("RED", c -> {
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Color");
        assertFalse(defaultOutput.contains("RED()"), "default should not have empty parens");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
                .addOption(Opt.ENUM_EMPTY_PARENS)
                .build();
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Color2", sf -> {
            sf.enum_("Color2", ec -> {
                ec.constant("RED", c -> {
                });
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.field("obj", fc -> {
                    fc.type(Type.OBJECT);
                    fc.init(Type.OBJECT.new_(SourceVersion.JAVA_17, List.of(), body -> {
                        body.instanceInit(init -> {
                            init.emit(Expr.callPlain("init"));
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.field("obj", fc -> {
                    fc.type(Type.OBJECT);
                    fc.init(Type.OBJECT.new_(SourceVersion.JAVA_17, List.of(), body -> {
                        body.instanceInit(init -> {
                            init.emit(Expr.callPlain("init"));
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Color", sf -> {
            sf.enum_("Color", ec -> {
                ec.constant("RED", c -> {
                    c.arg(Expr.decimal(1));
                    c.arg(Expr.decimal(2));
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Color2", sf -> {
            sf.enum_("Color2", ec -> {
                ec.constant("RED", c -> {
                    c.arg(Expr.decimal(1));
                    c.arg(Expr.decimal(2));
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
        final Sources defaultSources = createSources(defaultPrefs, SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Color", sf -> {
            sf.enum_("Color", ec -> {
                ec.constant("RED", c -> {
                });
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Color2", sf -> {
            sf.enum_("Color2", ec -> {
                ec.constant("RED", c -> {
                });
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
     * part is absent. With the default, {@code for(;;)} has no spaces between
     * semicolons; setting it to {@link SpaceType#SPACE} produces {@code for(; ; )}.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void afterSemicolonEmptyForLoop() throws IOException {
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.for_(fc -> {
                            fc.condition(Expr.$v("x").gt(Expr.ZERO));
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.emit(Expr.callPlain("method", Expr.$v("a"), Expr.$v("b")));
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.emit(Expr.callPlain("method", Expr.$v("a"), Expr.$v("b")));
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.param("a", Type.INT);
                    mc.param("b", Type.INT);
                    mc.body(b -> {
                    });
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.param("a", Type.INT);
                    mc.param("b", Type.INT);
                    mc.body(b -> {
                    });
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.implements_(Type.named("java.io.Serializable"));
                cc.implements_(Type.named("java.lang.Comparable"));
                cc.method("compareTo", mc -> {
                    mc.param("o", Type.OBJECT);
                    mc.returning(Type.INT);
                    mc.body(b -> {
                        b.return_(Expr.ZERO);
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.implements_(Type.named("java.io.Serializable"));
                cc.implements_(Type.named("java.lang.Comparable"));
                cc.method("compareTo", mc -> {
                    mc.param("o", Type.OBJECT);
                    mc.returning(Type.INT);
                    mc.body(b -> {
                        b.return_(Expr.ZERO);
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.throws_(Type.named("java.io.IOException"));
                    mc.throws_(Type.named("java.sql.SQLException"));
                    mc.body(b -> {
                    });
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.throws_(Type.named("java.io.IOException"));
                    mc.throws_(Type.named("java.sql.SQLException"));
                    mc.body(b -> {
                    });
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
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Dir", sf -> {
            sf.enum_("Dir", ec -> {
                ec.constant("N", c -> {
                });
                ec.constant("S", c -> {
                });
                ec.constant("E", c -> {
                });
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Dir2", sf -> {
            sf.enum_("Dir2", ec -> {
                ec.constant("N", c -> {
                });
                ec.constant("S", c -> {
                });
                ec.constant("E", c -> {
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Dir2");
        assertTrue(modifiedOutput.contains("N, S, E"),
                "NEVER wrap should have all constants on same line");
    }

    // ── Batch 20: SINGLE_STATEMENT_BRACES ────────────────────────────────

    /**
     * Verifies that disabling {@link Opt#SINGLE_STATEMENT_BRACES} omits braces
     * around a single-statement {@code if} body.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void singleStatementBracesIf() throws IOException {
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.if_(Expr.$v("x").gt(Expr.ZERO), ifBody -> {
                            ifBody.return_();
                        });
                    });
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains(") {"), "default (ON) should have braces");
        assertTrue(defaultOutput.contains("return;"), "default should contain return");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
                .removeOption(Opt.SINGLE_STATEMENT_BRACES)
                .build();
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.if_(Expr.$v("x").gt(Expr.ZERO), ifBody -> {
                            ifBody.return_();
                        });
                    });
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertFalse(modifiedOutput.contains(") {" + System.lineSeparator() + "            return;"),
                "modified (OFF) should not have braces around single statement if body");
        assertTrue(modifiedOutput.contains("return;"), "modified should still contain return");
        assertFalse(modifiedOutput.contains("} "), "modified should not have closing brace for if body");
    }

    /**
     * Verifies that disabling {@link Opt#SINGLE_STATEMENT_BRACES} omits braces
     * around single-statement {@code if-else} bodies.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void singleStatementBracesIfElse() throws IOException {
        final FormatPreferences prefs = FormatPreferences.builder()
                .removeOption(Opt.SINGLE_STATEMENT_BRACES)
                .build();
        final Sources sources = createSources(prefs, SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.ifElse(Expr.$v("x").gt(Expr.ZERO),
                                ifBody -> ifBody.return_(Expr.$v("x")),
                                elseBody -> elseBody.return_(Expr.ZERO));
                    });
                });
            });
        });
        sources.writeSources();
        final String output = getSource("com.example", "Cls1");
        assertTrue(output.contains("return x;"), "should contain if-body return");
        assertTrue(output.contains("return 0;"), "should contain else-body return");
        assertTrue(output.contains("else"), "should contain else keyword");
        assertFalse(output.contains("} else"), "should not have } else (no braces on if body)");
    }

    /**
     * Verifies that disabling {@link Opt#SINGLE_STATEMENT_BRACES} omits braces
     * around a single-statement {@code while} body.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void singleStatementBracesWhile() throws IOException {
        final FormatPreferences prefs = FormatPreferences.builder()
                .removeOption(Opt.SINGLE_STATEMENT_BRACES)
                .build();
        final Sources sources = createSources(prefs, SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.while_(Expr.$v("x").gt(Expr.ZERO), body -> {
                            body.emit(Expr.$v("x").dec());
                        });
                    });
                });
            });
        });
        sources.writeSources();
        final String output = getSource("com.example", "Cls1");
        assertTrue(output.contains("x--"), "should contain decrement");
        assertTrue(output.lines().noneMatch(l -> l.contains("while") && l.contains("{")),
                "should not have braces on while line");
    }

    /**
     * Verifies that disabling {@link Opt#SINGLE_STATEMENT_BRACES} omits braces
     * around a single-statement {@code for} loop body.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void singleStatementBracesFor() throws IOException {
        final FormatPreferences prefs = FormatPreferences.builder()
                .removeOption(Opt.SINGLE_STATEMENT_BRACES)
                .build();
        final Sources sources = createSources(prefs, SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.for_(fc -> {
                            fc.init(Type.INT, "i", Expr.ZERO);
                            fc.condition(Expr.$v("i").lt(Expr.$v("n")));
                            fc.update(Expr.$v("i").inc());
                            fc.body(body -> {
                                body.emit(Expr.callPlain("process", Expr.$v("i")));
                            });
                        });
                    });
                });
            });
        });
        sources.writeSources();
        final String output = getSource("com.example", "Cls1");
        assertTrue(output.contains("process(i)"), "should contain process call");
        assertTrue(output.lines().noneMatch(l -> l.contains("for") && l.contains("{")),
                "should not have braces on for line");
    }

    /**
     * Verifies that disabling {@link Opt#SINGLE_STATEMENT_BRACES} still renders
     * braces when the block contains more than one statement.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void singleStatementBracesMultiStatement() throws IOException {
        final FormatPreferences prefs = FormatPreferences.builder()
                .removeOption(Opt.SINGLE_STATEMENT_BRACES)
                .build();
        final Sources sources = createSources(prefs, SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.if_(Expr.$v("x").gt(Expr.ZERO), ifBody -> {
                            ifBody.emit(Expr.callPlain("a"));
                            ifBody.emit(Expr.callPlain("b"));
                        });
                    });
                });
            });
        });
        sources.writeSources();
        final String output = getSource("com.example", "Cls1");
        assertTrue(output.contains(") {"), "multi-statement block should still have braces");
        assertTrue(output.contains("a()"), "should contain first call");
        assertTrue(output.contains("b()"), "should contain second call");
    }

    /**
     * Verifies that setting {@link Wrapping#RECORD_COMPONENT_LIST} to
     * {@link WrappingMode#ALWAYS_WRAP} places each record component on its own line.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void alwaysWrapRecordComponentList() throws IOException {
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Point", sf -> {
            sf.record_("Point", rc -> {
                rc.component("x", Type.INT);
                rc.component("y", Type.INT);
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
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Point2", sf -> {
            sf.record_("Point2", rc -> {
                rc.component("x", Type.INT);
                rc.component("y", Type.INT);
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Point2");
        assertFalse(modifiedOutput.contains("int x, int y"),
                "ALWAYS_WRAP should not have components on same line");
    }

    // ── Batch 21: BEFORE_PAREN_METHOD_CALL ──────────────────────────────

    /**
     * Verifies that setting {@link Space#BEFORE_PAREN_METHOD_CALL} to SPACE
     * inserts a space before the opening paren of a method call.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void spaceBeforeParenMethodCall() throws IOException {
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.emit(Expr.callPlain("foo", Expr.ONE));
                    });
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains("foo(1)"), "default: no space before paren");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
                .space(Space.BEFORE_PAREN_METHOD_CALL, SpaceType.SPACE)
                .build();
        final Sources modified = createSources(prefs, SourceVersion.JAVA_17);
        modified.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.emit(Expr.callPlain("foo", Expr.ONE));
                    });
                });
            });
        });
        modified.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertTrue(modifiedOutput.contains("foo (1)"), "modified: space before paren");
    }

    // ── Batch 22: BEFORE_PAREN_METHOD_DECLARATION ───────────────────────

    /**
     * Verifies that setting {@link Space#BEFORE_PAREN_METHOD_DECLARATION} to SPACE
     * inserts a space before the opening paren of a method declaration.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void spaceBeforeParenMethodDeclaration() throws IOException {
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> b.empty());
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains("run()"), "default: no space before paren");

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
                .space(Space.BEFORE_PAREN_METHOD_DECLARATION, SpaceType.SPACE)
                .build();
        final Sources modified = createSources(prefs, SourceVersion.JAVA_17);
        modified.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> b.empty());
                });
            });
        });
        modified.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertTrue(modifiedOutput.contains("run ()"), "modified: space before paren");
    }

    // ── Batch 23: BEFORE_PAREN_CAST ─────────────────────────────────────

    /**
     * Verifies that setting {@link Space#BEFORE_PAREN_CAST} to SPACE
     * inserts a space before the opening paren of a cast expression.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void spaceBeforeParenCast() throws IOException {
        final FormatPreferences prefs = FormatPreferences.builder()
                .space(Space.BEFORE_PAREN_CAST, SpaceType.SPACE)
                .build();
        final Sources sources = createSources(prefs, SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.emit(Expr.$v("obj").cast(Type.STRING));
                    });
                });
            });
        });
        sources.writeSources();
        final String output = getSource("com.example", "Cls1");
        assertTrue(output.contains(" (String)"), "modified: space before cast paren");
    }

    // ── Batch 24: BEFORE_PAREN_ANNOTATION_PARAM ─────────────────────────

    /**
     * Verifies that setting {@link Space#BEFORE_PAREN_ANNOTATION_PARAM} to SPACE
     * inserts a space before the opening paren of an annotation.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void spaceBeforeParenAnnotationParam() throws IOException {
        final FormatPreferences prefs = FormatPreferences.builder()
                .space(Space.BEFORE_PAREN_ANNOTATION_PARAM, SpaceType.SPACE)
                .build();
        final Sources sources = createSources(prefs, SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.annotate(Type.named("java.lang.SuppressWarnings"), ac -> {
                    ac.value(Expr.str("unchecked"));
                });
            });
        });
        sources.writeSources();
        final String output = getSource("com.example", "Cls1");
        assertTrue(output.contains("SuppressWarnings ("), "modified: space before annotation paren");
    }

    // ── Batch 25: WITHIN_PAREN_EXPR ─────────────────────────────────────

    /**
     * Verifies that setting {@link Space#WITHIN_PAREN_EXPR} to SPACE
     * inserts spaces inside parenthesized expressions.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void spaceWithinParenExpr() throws IOException {
        final FormatPreferences prefs = FormatPreferences.builder()
                .space(Space.WITHIN_PAREN_EXPR, SpaceType.SPACE)
                .build();
        final Sources sources = createSources(prefs, SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.emit(Expr.$v("a").add(Expr.$v("b")).paren());
                    });
                });
            });
        });
        sources.writeSources();
        final String output = getSource("com.example", "Cls1");
        assertTrue(output.contains("( a + b )"), "modified: space within paren expr");
    }

    // ── Batch 26: WITHIN_PAREN_METHOD_CALL ──────────────────────────────

    /**
     * Verifies that setting {@link Space#WITHIN_PAREN_METHOD_CALL} to SPACE
     * inserts spaces inside a non-empty method call's parentheses.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void spaceWithinParenMethodCall() throws IOException {
        final FormatPreferences prefs = FormatPreferences.builder()
                .space(Space.WITHIN_PAREN_METHOD_CALL, SpaceType.SPACE)
                .build();
        final Sources sources = createSources(prefs, SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.emit(Expr.callPlain("foo", Expr.ONE));
                    });
                });
            });
        });
        sources.writeSources();
        final String output = getSource("com.example", "Cls1");
        assertTrue(output.contains("foo( 1 )"), "modified: space within method call parens");
    }

    // ── Batch 27: WITHIN_PAREN_METHOD_CALL_EMPTY ────────────────────────

    /**
     * Verifies that setting {@link Space#WITHIN_PAREN_METHOD_CALL_EMPTY} to SPACE
     * inserts a space inside an empty method call.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void spaceWithinParenMethodCallEmpty() throws IOException {
        final FormatPreferences prefs = FormatPreferences.builder()
                .space(Space.WITHIN_PAREN_METHOD_CALL_EMPTY, SpaceType.SPACE)
                .build();
        final Sources sources = createSources(prefs, SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.emit(Expr.callPlain("foo"));
                    });
                });
            });
        });
        sources.writeSources();
        final String output = getSource("com.example", "Cls1");
        assertTrue(output.contains("foo( )"), "modified: space within empty method call parens");
    }

    // ── Batch 28: WITHIN_PAREN_METHOD_DECLARATION ───────────────────────

    /**
     * Verifies that setting {@link Space#WITHIN_PAREN_METHOD_DECLARATION} to SPACE
     * inserts spaces inside a non-empty method declaration's parentheses.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void spaceWithinParenMethodDeclaration() throws IOException {
        final FormatPreferences prefs = FormatPreferences.builder()
                .space(Space.WITHIN_PAREN_METHOD_DECLARATION, SpaceType.SPACE)
                .build();
        final Sources sources = createSources(prefs, SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.param("x", Type.INT);
                    mc.body(b -> b.empty());
                });
            });
        });
        sources.writeSources();
        final String output = getSource("com.example", "Cls1");
        assertTrue(output.contains("( int x )"), "modified: space within method declaration parens");
    }

    // ── Batch 29: WITHIN_PAREN_METHOD_DECLARATION_EMPTY ─────────────────

    /**
     * Verifies that setting {@link Space#WITHIN_PAREN_METHOD_DECLARATION_EMPTY} to SPACE
     * inserts a space inside an empty method declaration's parentheses.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void spaceWithinParenMethodDeclarationEmpty() throws IOException {
        final FormatPreferences prefs = FormatPreferences.builder()
                .space(Space.WITHIN_PAREN_METHOD_DECLARATION_EMPTY, SpaceType.SPACE)
                .build();
        final Sources sources = createSources(prefs, SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> b.empty());
                });
            });
        });
        sources.writeSources();
        final String output = getSource("com.example", "Cls1");
        assertTrue(output.contains("run( )"), "modified: space within empty method declaration parens");
    }

    // ── Batch 30: WITHIN_PAREN_IF ───────────────────────────────────────

    /**
     * Verifies that setting {@link Space#WITHIN_PAREN_IF} to SPACE
     * inserts spaces inside the parentheses of an if condition.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void spaceWithinParenIf() throws IOException {
        final FormatPreferences prefs = FormatPreferences.builder()
                .space(Space.WITHIN_PAREN_IF, SpaceType.SPACE)
                .build();
        final Sources sources = createSources(prefs, SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.if_(Expr.TRUE, body -> {
                            body.empty();
                        });
                    });
                });
            });
        });
        sources.writeSources();
        final String output = getSource("com.example", "Cls1");
        assertTrue(output.contains("( true )"), "modified: space within if parens");
    }

    // ── Batch 31: WITHIN_PAREN_FOR ──────────────────────────────────────

    /**
     * Verifies that setting {@link Space#WITHIN_PAREN_FOR} to SPACE
     * inserts spaces inside the parentheses of a for loop.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void spaceWithinParenFor() throws IOException {
        final FormatPreferences prefs = FormatPreferences.builder()
                .space(Space.WITHIN_PAREN_FOR, SpaceType.SPACE)
                .build();
        final Sources sources = createSources(prefs, SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.for_(fc -> {
                            fc.init(Type.INT, "i", Expr.ZERO);
                            fc.condition(Expr.$v("i").lt(Expr.$v("n")));
                            fc.update(Expr.$v("i").inc());
                            fc.body(body -> body.empty());
                        });
                    });
                });
            });
        });
        sources.writeSources();
        final String output = getSource("com.example", "Cls1");
        assertTrue(output.contains("( int"), "modified: space after opening paren of for");
        assertTrue(output.contains("i++ )"), "modified: space before closing paren of for");
    }

    // ── Batch 32: WITHIN_PAREN_WHILE ────────────────────────────────────

    /**
     * Verifies that setting {@link Space#WITHIN_PAREN_WHILE} to SPACE
     * inserts spaces inside the parentheses of a while condition.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void spaceWithinParenWhile() throws IOException {
        final FormatPreferences prefs = FormatPreferences.builder()
                .space(Space.WITHIN_PAREN_WHILE, SpaceType.SPACE)
                .build();
        final Sources sources = createSources(prefs, SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.while_(Expr.TRUE, body -> body.empty());
                    });
                });
            });
        });
        sources.writeSources();
        final String output = getSource("com.example", "Cls1");
        assertTrue(output.contains("( true )"), "modified: space within while parens");
    }

    // ── Batch 33: WITHIN_PAREN_SWITCH ───────────────────────────────────

    /**
     * Verifies that setting {@link Space#WITHIN_PAREN_SWITCH} to SPACE
     * inserts spaces inside the parentheses of a switch selector.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void spaceWithinParenSwitch() throws IOException {
        final FormatPreferences prefs = FormatPreferences.builder()
                .space(Space.WITHIN_PAREN_SWITCH, SpaceType.SPACE)
                .build();
        final Sources sources = createSources(prefs, SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.switch_(Expr.$v("x"), sc -> {
                            sc.case_(List.of(Expr.ONE), cb -> cb.break_());
                        });
                    });
                });
            });
        });
        sources.writeSources();
        final String output = getSource("com.example", "Cls1");
        assertTrue(output.contains("( x )"), "modified: space within switch parens");
    }

    // ── Batch 34: WITHIN_PAREN_TRY ──────────────────────────────────────

    /**
     * Verifies that setting {@link Space#WITHIN_PAREN_TRY} to SPACE
     * inserts spaces inside the parentheses of a try-with-resources.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void spaceWithinParenTry() throws IOException {
        final FormatPreferences prefs = FormatPreferences.builder()
                .space(Space.WITHIN_PAREN_TRY, SpaceType.SPACE)
                .build();
        final Sources sources = createSources(prefs, SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.try_(tc -> {
                            tc.with(Type.named("java.io.InputStream"), "in",
                                    Expr.callPlain("open"));
                            tc.body(body -> body.empty());
                        });
                    });
                });
            });
        });
        sources.writeSources();
        final String output = getSource("com.example", "Cls1");
        assertTrue(output.contains("( java.io.InputStream") || output.contains("( InputStream"),
                "modified: space after opening paren of try");
    }

    // ── Batch 35: WITHIN_PAREN_CATCH ────────────────────────────────────

    /**
     * Verifies that setting {@link Space#WITHIN_PAREN_CATCH} to SPACE
     * inserts spaces inside the parentheses of a catch clause.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void spaceWithinParenCatch() throws IOException {
        final FormatPreferences prefs = FormatPreferences.builder()
                .space(Space.WITHIN_PAREN_CATCH, SpaceType.SPACE)
                .build();
        final Sources sources = createSources(prefs, SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.try_(tc -> {
                            tc.body(body -> body.empty());
                            tc.catch_(Type.named("java.lang.Exception"), "e",
                                    body -> body.empty());
                        });
                    });
                });
            });
        });
        sources.writeSources();
        final String output = getSource("com.example", "Cls1");
        assertTrue(output.contains("( Exception"), "modified: space after opening paren of catch");
        assertTrue(output.contains("e )"), "modified: space before closing paren of catch");
    }

    // ── Batch 36: WITHIN_PAREN_SYNCHRONIZED ─────────────────────────────

    /**
     * Verifies that setting {@link Space#WITHIN_PAREN_SYNCHRONIZED} to SPACE
     * inserts spaces inside the parentheses of a synchronized statement.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void spaceWithinParenSynchronized() throws IOException {
        final FormatPreferences prefs = FormatPreferences.builder()
                .space(Space.WITHIN_PAREN_SYNCHRONIZED, SpaceType.SPACE)
                .build();
        final Sources sources = createSources(prefs, SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.synchronized_(Expr.$v("lock"), body -> body.empty());
                    });
                });
            });
        });
        sources.writeSources();
        final String output = getSource("com.example", "Cls1");
        assertTrue(output.contains("( lock )"), "modified: space within synchronized parens");
    }

    // ── Batch 37: WITHIN_PAREN_CAST ─────────────────────────────────────

    /**
     * Verifies that setting {@link Space#WITHIN_PAREN_CAST} to SPACE
     * inserts spaces inside the parentheses of a cast expression.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void spaceWithinParenCast() throws IOException {
        final FormatPreferences prefs = FormatPreferences.builder()
                .space(Space.WITHIN_PAREN_CAST, SpaceType.SPACE)
                .build();
        final Sources sources = createSources(prefs, SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.emit(Expr.$v("obj").cast(Type.STRING));
                    });
                });
            });
        });
        sources.writeSources();
        final String output = getSource("com.example", "Cls1");
        assertTrue(output.contains("( String )"), "modified: space within cast parens");
    }

    // ── Batch 38: WITHIN_PAREN_ANNOTATION ───────────────────────────────

    /**
     * Verifies that setting {@link Space#WITHIN_PAREN_ANNOTATION} to SPACE
     * inserts spaces inside the parentheses of an annotation.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void spaceWithinParenAnnotation() throws IOException {
        final FormatPreferences prefs = FormatPreferences.builder()
                .space(Space.WITHIN_PAREN_ANNOTATION, SpaceType.SPACE)
                .build();
        final Sources sources = createSources(prefs, SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.annotate(Type.named("java.lang.SuppressWarnings"), ac -> {
                    ac.value(Expr.str("unchecked"));
                });
            });
        });
        sources.writeSources();
        final String output = getSource("com.example", "Cls1");
        assertTrue(output.contains("( \"unchecked\" )"), "modified: space within annotation parens");
    }

    // ── Batch 39: WITHIN_PAREN_RECORD ───────────────────────────────────

    /**
     * Verifies that setting {@link Space#WITHIN_PAREN_RECORD} to SPACE
     * inserts spaces inside the parentheses of a record component list.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void spaceWithinParenRecord() throws IOException {
        final FormatPreferences prefs = FormatPreferences.builder()
                .space(Space.WITHIN_PAREN_RECORD, SpaceType.SPACE)
                .build();
        final Sources sources = createSources(prefs, SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Point", sf -> {
            sf.record_("Point", rc -> {
                rc.component("x", Type.INT);
                rc.component("y", Type.INT);
            });
        });
        sources.writeSources();
        final String output = getSource("com.example", "Point");
        assertTrue(output.contains("( int x"), "modified: space after opening paren of record");
        assertTrue(output.contains("int y )"), "modified: space before closing paren of record");
    }

    // ── Batch 40: BEFORE_BRACE_INTERFACE ────────────────────────────────

    /**
     * Verifies that setting {@link Space#BEFORE_BRACE_INTERFACE} to NONE
     * removes the space before the opening brace of an interface declaration.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void noSpaceBeforeBraceInterface() throws IOException {
        final FormatPreferences prefs = FormatPreferences.builder()
                .space(Space.BEFORE_BRACE_INTERFACE, SpaceType.NONE)
                .build();
        final Sources sources = createSources(prefs, SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "MyIface", sf -> {
            sf.interface_("MyIface", ic -> {
                ic.method("run", mc -> {
                });
            });
        });
        sources.writeSources();
        final String output = getSource("com.example", "MyIface");
        assertTrue(output.contains("MyIface{"), "modified: no space before interface brace");
    }

    // ── Batch 41: BEFORE_BRACE_ANNOTATION_ARRAY_INIT ────────────────────

    /**
     * Verifies that setting {@link Space#BEFORE_BRACE_ANNOTATION_ARRAY_INIT} to SPACE
     * inserts a space before the opening brace of an annotation array initializer.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void spaceBeforeBraceAnnotationArrayInit() throws IOException {
        final FormatPreferences prefs = FormatPreferences.builder()
                .space(Space.BEFORE_BRACE_ANNOTATION_ARRAY_INIT, SpaceType.SPACE)
                .build();
        final Sources sources = createSources(prefs, SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.annotate(Type.named("java.lang.SuppressWarnings"), ac -> {
                    ac.memberArray("value", Expr.str("unchecked"), Expr.str("rawtypes"));
                });
            });
        });
        sources.writeSources();
        final String output = getSource("com.example", "Cls1");
        assertTrue(output.contains("= {"), "modified: space before annotation array brace");
    }

    // ── Batch 42: WITHIN_BRACES_CODE ────────────────────────────────────

    /**
     * Verifies that setting {@link Space#WITHIN_BRACES_CODE} to SPACE
     * places the block content on the same line as the brace.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void spaceWithinBracesCode() throws IOException {
        final FormatPreferences prefs = FormatPreferences.builder()
                .space(Space.WITHIN_BRACES_CODE, SpaceType.SPACE)
                .build();
        final Sources sources = createSources(prefs, SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.return_();
                    });
                });
            });
        });
        sources.writeSources();
        final String output = getSource("com.example", "Cls1");
        assertTrue(output.contains("{ return;"), "modified: content on same line as opening brace");
    }

    // ── Batch 43: AFTER_ANNOTATION ──────────────────────────────────────

    /**
     * Verifies that setting {@link Space#AFTER_ANNOTATION} to SPACE
     * places the annotation on the same line as the declaration.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void spaceAfterAnnotation() throws IOException {
        final FormatPreferences prefs = FormatPreferences.builder()
                .space(Space.AFTER_ANNOTATION, SpaceType.SPACE)
                .build();
        final Sources sources = createSources(prefs, SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.annotate(Type.named("java.lang.Override"));
                    mc.body(b -> b.empty());
                });
            });
        });
        sources.writeSources();
        final String output = getSource("com.example", "Cls1");
        assertTrue(output.contains("@Override void"), "modified: annotation and declaration on same line");
    }

    // ── Batch 44: BEFORE_METHOD ─────────────────────────────────────────

    /**
     * Verifies that setting {@link Space#BEFORE_METHOD} to NONE
     * removes the blank line before a method declaration.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void noSpaceBeforeMethod() throws IOException {
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.field("x", fc -> {
                    fc.type(Type.INT);
                    fc.init(Expr.ZERO);
                });
                cc.method("run", mc -> {
                    mc.body(b -> b.empty());
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        // With default NEWLINE, there should be a blank line before the method
        long defaultBlankLines = defaultOutput.lines()
                .filter(String::isBlank)
                .count();

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
                .space(Space.BEFORE_METHOD, SpaceType.NONE)
                .build();
        final Sources modified = createSources(prefs, SourceVersion.JAVA_17);
        modified.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.field("x", fc -> {
                    fc.type(Type.INT);
                    fc.init(Expr.ZERO);
                });
                cc.method("run", mc -> {
                    mc.body(b -> b.empty());
                });
            });
        });
        modified.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        long modifiedBlankLines = modifiedOutput.lines()
                .filter(String::isBlank)
                .count();
        assertTrue(modifiedBlankLines < defaultBlankLines,
                "modified: fewer blank lines with BEFORE_METHOD=NONE");
    }

    // ── Batch 45: BEFORE_CLASS ──────────────────────────────────────────

    /**
     * Verifies that setting {@link Space#BEFORE_CLASS} to NONE
     * removes the blank line before a nested class declaration.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void noSpaceBeforeClass() throws IOException {
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Outer", sf -> {
            sf.class_("Outer", cc -> {
                cc.field("x", fc -> {
                    fc.type(Type.INT);
                    fc.init(Expr.ZERO);
                });
                cc.class_("Inner", nc -> {
                    nc.field("y", fc -> {
                        fc.type(Type.INT);
                        fc.init(Expr.ONE);
                    });
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Outer");
        long defaultBlankLines = defaultOutput.lines()
                .filter(String::isBlank)
                .count();

        clearSources();
        final FormatPreferences prefs = FormatPreferences.builder()
                .space(Space.BEFORE_CLASS, SpaceType.NONE)
                .build();
        final Sources modified = createSources(prefs, SourceVersion.JAVA_17);
        modified.createSourceFile("com.example", "Outer2", sf -> {
            sf.class_("Outer2", cc -> {
                cc.field("x", fc -> {
                    fc.type(Type.INT);
                    fc.init(Expr.ZERO);
                });
                cc.class_("Inner", nc -> {
                    nc.field("y", fc -> {
                        fc.type(Type.INT);
                        fc.init(Expr.ONE);
                    });
                });
            });
        });
        modified.writeSources();
        final String modifiedOutput = getSource("com.example", "Outer2");
        long modifiedBlankLines = modifiedOutput.lines()
                .filter(String::isBlank)
                .count();
        assertTrue(modifiedBlankLines < defaultBlankLines,
                "modified: fewer blank lines with BEFORE_CLASS=NONE");
    }

    // ── Batch 46: LABELS indentation ────────────────────────────────────

    /**
     * Verifies that setting {@link Indentation#LABELS} to a non-zero value
     * indents the label relative to the surrounding code.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void customLabelsIndent() throws IOException {
        final FormatPreferences prefs = FormatPreferences.builder()
                .indent(Indentation.LABELS, 4)
                .build();
        final Sources sources = createSources(prefs, SourceVersion.JAVA_17);
        sources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.labeled("outer", (label, lb) -> {
                            lb.while_(Expr.TRUE, body -> {
                                body.break_(label);
                            });
                        });
                    });
                });
            });
        });
        sources.writeSources();
        final String output = getSource("com.example", "Cls1");
        assertTrue(output.contains("outer:"), "should contain label");
        // With LABELS indent of 4, the label should be indented further
        // than the default (0)
        String labelLine = output.lines()
                .filter(l -> l.contains("outer:"))
                .findFirst().orElseThrow();
        int labelIndent = labelLine.indexOf("outer:");
        assertTrue(labelIndent > 8, "label should be further indented with LABELS=4");
    }

    // ── SWITCH_ARROW_ALWAYS_BLOCK_BODY ──────────────────────────────────

    /**
     * Verifies that enabling {@link FormatPreferences.Opt#SWITCH_ARROW_ALWAYS_BLOCK_BODY}
     * forces arrow case bodies to always render in block form, even for single statements.
     *
     * @throws IOException if source generation fails
     */
    @Test
    void switchArrowAlwaysBlockBody() throws IOException {
        // default: single-statement renders without braces
        final Sources defaultSources = createSources(SourceVersion.JAVA_17);
        defaultSources.createSourceFile("com.example", "Cls1", sf -> {
            sf.class_("Cls1", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.switch_(Expr.$v("x"), sc -> {
                            sc.case_(Expr.ONE, body -> body.return_(Expr.str("one")));
                            sc.default_(body -> body.return_(Expr.str("other")));
                        });
                    });
                });
            });
        });
        defaultSources.writeSources();
        final String defaultOutput = getSource("com.example", "Cls1");
        assertTrue(defaultOutput.contains("case 1 -> return"), "default: single-statement without braces");
        assertFalse(defaultOutput.contains("case 1 -> {"), "default: no block braces");

        clearSources();
        // with option: always block
        final FormatPreferences prefs = FormatPreferences.builder()
                .addOption(FormatPreferences.Opt.SWITCH_ARROW_ALWAYS_BLOCK_BODY)
                .build();
        final Sources modifiedSources = createSources(prefs, SourceVersion.JAVA_17);
        modifiedSources.createSourceFile("com.example", "Cls2", sf -> {
            sf.class_("Cls2", cc -> {
                cc.method("run", mc -> {
                    mc.body(b -> {
                        b.switch_(Expr.$v("x"), sc -> {
                            sc.case_(Expr.ONE, body -> body.return_(Expr.str("one")));
                            sc.default_(body -> body.return_(Expr.str("other")));
                        });
                    });
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertTrue(modifiedOutput.contains("case 1 -> {"), "modified: block braces forced");
        assertTrue(modifiedOutput.contains("default -> {"), "modified: default block braces forced");
    }
}
