package org.jboss.jdeparser.impl;

import java.io.IOException;
import java.util.List;

import org.jboss.jdeparser.JType;
import org.jboss.jdeparser.format.FormatPreferences;

/**
 * Represents a parameterized (generic) type with type arguments applied,
 * such as {@code List<String>} or {@code Map<String, Integer>}.
 * <p>
 * Once type arguments have been applied, further calls to {@link #typeArg}
 * are rejected with an {@link IllegalStateException}.  The {@link #erasure()}
 * operation delegates to the raw type's erasure.
 */
public final class NarrowedJType extends AbstractJType {

    /** The raw (unparameterized) type. */
    private final JType rawType;

    /** The type arguments, stored as an unmodifiable list. */
    private final List<JType> typeArgs;

    /**
     * Constructs a new parameterized type.
     *
     * @param rawType  the raw type to which type arguments are applied
     * @param typeArgs the type arguments (defensively copied to an unmodifiable list)
     */
    public NarrowedJType(final JType rawType, final List<JType> typeArgs) {
        this.rawType = rawType;
        this.typeArgs = List.copyOf(typeArgs);
    }

    /**
     * Returns the raw (unparameterized) type.
     *
     * @return the raw type
     */
    public JType rawType() {
        return rawType;
    }

    /**
     * Returns the type arguments as an unmodifiable list.
     *
     * @return the type arguments (never {@code null}, never empty)
     */
    public List<JType> typeArgs() {
        return typeArgs;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Delegates to the raw type's erasure.
     */
    @Override
    public JType erasure() {
        return rawType.erasure();
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException always, since type arguments have already been applied
     */
    @Override
    public JType typeArg(final JType... args) {
        throw new IllegalStateException("Type arguments already applied");
    }

    /** {@inheritDoc} */
    @Override
    public void write(SourceFileWriter writer) throws IOException {
        AbstractJExpr.writeType(writer, rawType);
        writer.write(Tokens.$ANGLE.OPEN);
        boolean first = true;
        for (JType arg : typeArgs) {
            if (!first) {
                writer.write(Tokens.$PUNCT.COMMA);
                writer.write(FormatPreferences.Space.COMMA_TYPE_ARGUMENT);
            }
            first = false;
            AbstractJExpr.writeType(writer, arg);
        }
        writer.write(Tokens.$ANGLE.CLOSE);
    }
}
