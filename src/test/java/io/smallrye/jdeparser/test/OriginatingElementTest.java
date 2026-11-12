package io.smallrye.jdeparser.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.Element;
import javax.tools.JavaFileObject;

import org.junit.jupiter.api.Test;

import io.smallrye.jdeparser.JDeparser;
import io.smallrye.jdeparser.SourceVersion;
import io.smallrye.jdeparser.Sources;
import io.smallrye.jdeparser.Type;
import io.smallrye.jdeparser.creator.SourceFileCreator;
import io.smallrye.jdeparser.format.Filer;
import io.smallrye.jdeparser.format.FormatPreferences;

/**
 * Tests for originating element support.
 * <p>
 * Verifies that:
 * <ul>
 * <li>elements added via {@code sf.originatingElement(...)} reach
 * {@link Filer#openWriter(String, String, List)} in order</li>
 * <li>a file with no originating elements gets an empty list</li>
 * <li>a plain two-argument lambda {@link Filer} still works via the default method</li>
 * <li>{@link Filer#newInstance(javax.annotation.processing.Filer)} forwards the elements to
 * {@code createSourceFile(name, originatingElements)}</li>
 * <li>{@code null} elements are rejected</li>
 * <li>calling the method after the source file callback returned throws {@link IllegalStateException}</li>
 * </ul>
 * <p>
 * {@link Element}, {@link JavaFileObject}, and the annotation processing
 * {@link javax.annotation.processing.Filer} are Mockito mocks, since they are
 * only passed through or act as simple collaborators.
 */
class OriginatingElementTest {

    /** A single recorded call to the three-argument `openWriter` method. */
    record Request(String packageName, String fileName,
            List<Element> originatingElements,
            StringWriter output) {
    }

    /**
     * Stub of the JDeparser {@link Filer} (a two-method interface) that
     * records calls to the three-argument {@code openWriter} method and
     * captures output into {@code StringWriter}s.
     */
    static final class RecordingFiler implements Filer {
        final List<Request> requests = new ArrayList<>();

        @Override
        public Writer openWriter(final String packageName, final String fileName) {
            return openWriter(packageName, fileName, List.of());
        }

        @Override
        public Writer openWriter(final String packageName, final String fileName,
                final List<Element> originatingElements) {
            final StringWriter output = new StringWriter();
            requests.add(new Request(packageName, fileName,
                    List.copyOf(originatingElements), output));
            return output;
        }
    }

    /**
     * Creates an {@link Element} mock. Elements are only passed through and
     * never inspected.
     *
     * @param name a name used in failure messages
     * @return a mock element
     */
    static Element element(final String name) {
        return mock(Element.class, name);
    }

    /**
     * Creates a minimal {@link Sources} instance writing through the given filer.
     *
     * @param filer the filer to write through
     * @return a new sources instance
     */
    static Sources sourcesFor(final Filer filer) {
        return JDeparser.createSources(filer, FormatPreferences.defaults(),
                SourceVersion.JAVA_17);
    }

    /** Adds a simple public class to the given source file creator. */
    static void addSimpleClass(final SourceFileCreator sf) {
        sf.class_("Foo", cc -> {
            cc.public_();
            cc.method("bar", mc -> {
                mc.public_();
                mc.returning(Type.VOID);
            });
        });
    }

    @Test
    void originatingElementsReachFilerInOrder() throws IOException {
        final RecordingFiler filer = new RecordingFiler();
        final Sources sources = sourcesFor(filer);
        final Element first = element("first");
        final Element second = element("second");
        sources.createSourceFile("com.example", "Foo", sf -> {
            sf.originatingElement(first);
            addSimpleClass(sf);
            // may be called any time before the file is written, like the import methods
            sf.originatingElement(second);
        });
        sources.writeSources();

        assertEquals(1, filer.requests.size());
        final Request request = filer.requests.get(0);
        assertEquals("com.example", request.packageName());
        assertEquals("Foo", request.fileName());
        assertEquals(2, request.originatingElements().size());
        assertSame(first, request.originatingElements().get(0));
        assertSame(second, request.originatingElements().get(1));
    }

    @Test
    void fileWithoutOriginatingElementsGetsEmptyList() throws IOException {
        final RecordingFiler filer = new RecordingFiler();
        final Sources sources = sourcesFor(filer);
        sources.createSourceFile("com.example", "Foo",
                OriginatingElementTest::addSimpleClass);
        sources.writeSources();

        assertEquals(1, filer.requests.size());
        assertTrue(filer.requests.get(0).originatingElements().isEmpty());
    }

    @Test
    void twoArgumentLambdaFilerStillWorks() throws IOException {
        // a lambda Filer only implements the two-argument method; the default
        // three-argument method must delegate to it, ignoring the elements
        final Map<String, StringWriter> outputs = new HashMap<>();
        final boolean[] called = { false };
        final Filer lambdaFiler = (packageName, fileName) -> {
            called[0] = true;
            final StringWriter output = new StringWriter();
            outputs.put(packageName + "." + fileName, output);
            return output;
        };
        final Sources sources = sourcesFor(lambdaFiler);
        sources.createSourceFile("com.example", "Foo", sf -> {
            sf.originatingElement(element("ignored"));
            addSimpleClass(sf);
        });
        sources.writeSources();

        final String lambdaOutput = outputs.get("com.example.Foo").toString();
        assertTrue(called[0], "the two-argument method should have been called");
        assertTrue(lambdaOutput.contains("public class Foo"));

        // the generated output is identical to what the origin-element-aware filer produces
        final RecordingFiler recordingFiler = new RecordingFiler();
        final Sources recordingSources = sourcesFor(recordingFiler);
        recordingSources.createSourceFile("com.example", "Foo",
                OriginatingElementTest::addSimpleClass);
        recordingSources.writeSources();
        assertEquals(recordingFiler.requests.get(0).output().toString(), lambdaOutput);
    }

    @Test
    void annotationProcessingFilerReceivesElements() throws IOException {
        final StringWriter output = new StringWriter();
        final JavaFileObject fileObject = mock(JavaFileObject.class);
        when(fileObject.openWriter()).thenReturn(output);
        final javax.annotation.processing.Filer apFiler = mock(javax.annotation.processing.Filer.class);
        when(apFiler.createSourceFile(anyString(), any(Element[].class))).thenReturn(fileObject);

        final Sources sources = sourcesFor(Filer.newInstance(apFiler));
        final Element first = element("first");
        final Element second = element("second");
        sources.createSourceFile("com.example", "Foo", sf -> {
            sf.originatingElement(first);
            sf.originatingElement(second);
            addSimpleClass(sf);
        });
        sources.writeSources();

        // the qualified name and the varargs originating elements are forwarded
        verify(apFiler).createSourceFile("com.example.Foo", first, second);

        // the generated content actually reaches the file object's writer
        assertTrue(output.toString().contains("public class Foo"));
    }

    @Test
    void nullElementIsRejected() {
        final Sources sources = sourcesFor(new RecordingFiler());
        assertThrows(IllegalArgumentException.class,
                () -> sources.createSourceFile("com.example", "Foo",
                        sf -> sf.originatingElement(null)));
    }

    @Test
    void originatingElementAfterCallbackThrows() {
        final Sources sources = sourcesFor(new RecordingFiler());
        final SourceFileCreator[] captured = new SourceFileCreator[1];
        sources.createSourceFile("com.example", "Foo", sf -> captured[0] = sf);
        assertThrows(IllegalStateException.class, () -> captured[0].originatingElement(element("late")));
    }
}
