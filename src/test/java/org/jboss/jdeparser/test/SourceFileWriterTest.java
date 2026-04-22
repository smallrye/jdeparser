package org.jboss.jdeparser.test;

import java.io.IOException;
import java.io.StringWriter;

import org.jboss.jdeparser.SourceVersion;
import org.jboss.jdeparser.format.FormatPreferences;
import org.jboss.jdeparser.impl.SourceFileWriter;
import org.jboss.jdeparser.impl.Tokens;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link SourceFileWriter} indentation and spacing.
 */
class SourceFileWriterTest {

    /**
     * Verifies that pushIndent/popIndent works correctly for a class body.
     *
     * @throws IOException if an I/O error occurs
     */
    @Test
    void pushPopIndent() throws IOException {
        final StringWriter out = new StringWriter();
        try (SourceFileWriter w = new SourceFileWriter(out, FormatPreferences.defaults(), SourceVersion.JAVA_17)) {
            w.write(Tokens.$BRACE.OPEN);
            w.nl();
            w.pushIndent(FormatPreferences.Indentation.MEMBERS_TOP_LEVEL);
            w.writeName("hello");
            w.write(Tokens.$PUNCT.SEMI);
            w.nl();
            w.popIndent(FormatPreferences.Indentation.MEMBERS_TOP_LEVEL);
            w.write(Tokens.$BRACE.CLOSE);
            w.nl();
        }
        final String result = out.toString();
        assertTrue(result.contains("hello;"), "should contain content");
        assertTrue(result.contains("}"), "should contain closing brace");
    }
}
