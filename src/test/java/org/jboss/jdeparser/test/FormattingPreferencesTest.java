package org.jboss.jdeparser.test;

import java.io.IOException;

import org.jboss.jdeparser.JExpr;
import org.jboss.jdeparser.JExprs;
import org.jboss.jdeparser.JSources;
import org.jboss.jdeparser.JType;
import org.jboss.jdeparser.SourceVersion;
import org.jboss.jdeparser.format.FormatPreferences;
import org.jboss.jdeparser.format.FormatPreferences.Space;
import org.jboss.jdeparser.format.FormatPreferences.SpaceType;
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
                        b.if_(JExprs.$v("x").gt(JExpr.ZERO), then -> {
                            then.return_();
                        });
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
                        b.if_(JExprs.$v("x").gt(JExpr.ZERO), then -> {
                            then.return_();
                        });
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
                    mc.body(b -> {
                        b.return_();
                    });
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
                    mc.body(b -> {
                        b.return_();
                    });
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
                        b.emit(JExprs.$v("x").assign(JExpr.ZERO));
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
                        b.emit(JExprs.$v("x").assign(JExpr.ZERO));
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
                    fc.init(JExprs.newArrayInit(JType.INT, JExprs.decimal(1), JExprs.decimal(2)));
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
                    fc.init(JExprs.newArrayInit(JType.INT, JExprs.decimal(1), JExprs.decimal(2)));
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
                        b.emit(JExprs.call("method", JExprs.$v("a"), JExprs.$v("b")));
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
                        b.emit(JExprs.call("method", JExprs.$v("a"), JExprs.$v("b")));
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
                    fc.init(JExprs.lambda("x", JExprs.$v("x")));
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
                    fc.init(JExprs.lambda("x", JExprs.$v("x")));
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
                            fb.condition(JExprs.$v("i").lt(JExprs.$v("n")));
                            fb.update(JExprs.$v("i").postInc());
                            fb.body(loop -> {
                                loop.empty();
                            });
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
                            fb.condition(JExprs.$v("i").lt(JExprs.$v("n")));
                            fb.update(JExprs.$v("i").postInc());
                            fb.body(loop -> {
                                loop.empty();
                            });
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
                        b.while_(JExprs.$v("x").gt(JExpr.ZERO), loop -> {
                            loop.empty();
                        });
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
                        b.while_(JExprs.$v("x").gt(JExpr.ZERO), loop -> {
                            loop.empty();
                        });
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
                            fb.condition(JExprs.$v("i").lt(JExprs.$v("n")));
                            fb.update(JExprs.$v("i").postInc());
                            fb.body(loop -> {
                                loop.empty();
                            });
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
                            fb.condition(JExprs.$v("i").lt(JExprs.$v("n")));
                            fb.update(JExprs.$v("i").postInc());
                            fb.body(loop -> {
                                loop.empty();
                            });
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
                    fc.init(JExprs.$v("x").gt(JExpr.ZERO));
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
                    fc.init(JExprs.$v("x").gt(JExpr.ZERO));
                });
            });
        });
        modifiedSources.writeSources();
        final String modifiedOutput = getSource("com.example", "Cls2");
        assertTrue(modifiedOutput.contains("x>0"), "modified should contain 'x>0'");
        assertFalse(modifiedOutput.contains("x > 0"), "modified should not contain 'x > 0'");
    }
}
