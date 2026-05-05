package io.smallrye.jdeparser.test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

import org.junit.jupiter.api.Test;

/**
 * Extracts, compiles, and runs Java examples from {@code MANUAL.adoc}.
 * <p>
 * To be tested, examples in {@code MANUAL.adoc} must be enclosed by
 * {@code //TEST:BEGIN} and {@code //TEST:END} lines. The Java source code
 * must appear between AsciiDoc {@code ----} delimiters within that enclosure.
 * All example classes must have unique names.
 * <p>
 * By default each example is compiled and executed. Add a
 * {@code //TEST:COMPILE-ONLY} line within the enclosure to skip execution.
 */
class DocumentationExamplesTest {

    private static final Pattern TEST_PATTERN = Pattern.compile(
            "//TEST:BEGIN(.*?)----(.*?)----(.*?)//TEST:END", Pattern.DOTALL);
    private static final Pattern CLASS_NAME_PATTERN = Pattern.compile(
            "class ([a-zA-Z0-9_]+) ");

    /**
     * Parses, compiles, and runs all testable examples from MANUAL.adoc.
     *
     * @throws Throwable if any example fails to compile or run
     */
    @Test
    void compileAndRunDocumentationExamples() throws Throwable {
        Path path = Paths.get("MANUAL.adoc");
        assumeTrue(Files.exists(path), "MANUAL.adoc not found at project root");
        JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
        assumeTrue(javac != null, "No system Java compiler available");

        // ensure output directory exists
        Files.createDirectories(Path.of("target/doc-example"));

        String manual = Files.readString(path);
        Matcher matcher = TEST_PATTERN.matcher(manual);
        int count = 0;
        List<String> failures = new ArrayList<>();
        while (matcher.find()) {
            count++;
            String match = matcher.group();
            boolean compileOnly = match.contains("//TEST:COMPILE-ONLY");

            String source = matcher.group(2);
            Matcher classNameMatcher = CLASS_NAME_PATTERN.matcher(source);
            assertTrue(classNameMatcher.find(), "No class name found in example #" + count);
            String className = classNameMatcher.group(1);

            System.out.println("Testing example: " + className
                    + (compileOnly ? " (compile-only)" : ""));

            try {
                compileAndRun(javac, source, className, compileOnly);
            } catch (Throwable t) {
                failures.add(className + ": " + t.getMessage());
                t.printStackTrace(System.err);
            }
        }

        assertTrue(count > 0, "No //TEST:BEGIN markers found in MANUAL.adoc");

        if (!failures.isEmpty()) {
            fail("Failed examples:\n  " + String.join("\n  ", failures));
        }

        System.out.println("All " + count + " documentation examples passed.");
    }

    /**
     * Compiles and optionally runs a single example.
     *
     * @param javac the system Java compiler
     * @param source the Java source code
     * @param className the class name to compile
     * @param compileOnly if {@code true}, skip execution after compilation
     * @throws Throwable if compilation or execution fails
     */
    private static void compileAndRun(JavaCompiler javac, String source,
            String className, boolean compileOnly) throws Throwable {
        // rewrite output path so generated files go to target/doc-example
        String rewritten = source.replace(
                "\"generated-sources\"", "\"target/doc-example\"");

        String classpath = buildClasspath();

        List<String> options = List.of(
                "-cp", classpath,
                "-d", "target/doc-example");
        JavaFileObject compilationUnit = new StringJavaSource(className, rewritten);

        ByteArrayOutputStream errStream = new ByteArrayOutputStream();
        boolean compiled = javac.getTask(
                new java.io.OutputStreamWriter(errStream),
                null, null, options, null,
                List.of(compilationUnit)).call();

        if (!compiled) {
            fail("Compilation failed for " + className + ":\n" + errStream);
        }

        if (compileOnly) {
            return;
        }

        // run main()
        try (URLClassLoader cl = new URLClassLoader(
                new URL[] { Path.of("target/doc-example").toUri().toURL() },
                DocumentationExamplesTest.class.getClassLoader())) {
            Class<?> example = cl.loadClass(className);
            example.getMethod("main", String[].class)
                    .invoke(null, (Object) new String[0]);
        }
    }

    /**
     * Builds the classpath string including project classes and their dependencies.
     *
     * @return a classpath string suitable for javac
     */
    private static String buildClasspath() {
        // start with project compiled classes
        StringBuilder cp = new StringBuilder();
        cp.append("target/classes");

        // add the doc-example output directory itself (for cross-example references)
        cp.append(java.io.File.pathSeparatorChar).append("target/doc-example");

        // add dependency jars from the local Maven repository by scanning the classpath
        // of the current classloader to find the jars
        String javaClassPath = System.getProperty("java.class.path", "");
        for (String entry : javaClassPath.split(java.io.File.pathSeparator)) {
            if (entry.endsWith(".jar")) {
                cp.append(java.io.File.pathSeparatorChar).append(entry);
            }
        }

        return cp.toString();
    }

    /**
     * In-memory Java source file for compilation.
     */
    private static final class StringJavaSource extends SimpleJavaFileObject {
        private final String source;

        StringJavaSource(String name, String source) {
            super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension),
                    Kind.SOURCE);
            this.source = source;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return source;
        }
    }
}
