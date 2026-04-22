package org.jboss.jdeparser;

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import org.jboss.jdeparser.impl.ReferenceJType;

/**
 * Static factory methods for creating {@link JType} instances.
 */
public final class JTypes {

    private JTypes() {
    }

    /**
     * Creates a type from a {@link Class} object.
     *
     * @param clazz the class (must not be {@code null})
     * @return the corresponding type
     */
    public static JType typeOf(final Class<?> clazz) {
        if (clazz.isPrimitive()) {
            if (clazz == void.class) return JType.VOID;
            if (clazz == boolean.class) return JType.BOOLEAN;
            if (clazz == byte.class) return JType.BYTE;
            if (clazz == short.class) return JType.SHORT;
            if (clazz == int.class) return JType.INT;
            if (clazz == long.class) return JType.LONG;
            if (clazz == float.class) return JType.FLOAT;
            if (clazz == double.class) return JType.DOUBLE;
            if (clazz == char.class) return JType.CHAR;
            throw new IllegalArgumentException("Unknown primitive type: " + clazz);
        }
        if (clazz.isArray()) {
            return typeOf(clazz.getComponentType()).array();
        }
        return new ReferenceJType(clazz.getCanonicalName());
    }

    /**
     * Creates a type from a fully qualified class name string.
     *
     * @param qualifiedName the fully qualified class name (e.g., {@code "com.example.MyClass"})
     * @return the corresponding type
     */
    public static JType typeNamed(final String qualifiedName) {
        return new ReferenceJType(qualifiedName);
    }

    /**
     * Creates a type from a {@link TypeMirror}, typically used in annotation processors.
     *
     * @param mirror the type mirror (must not be {@code null})
     * @return the corresponding type
     * @throws IllegalArgumentException if the mirror kind is not supported
     */
    public static JType typeOf(final TypeMirror mirror) {
        return switch (mirror.getKind()) {
            case VOID -> JType.VOID;
            case BOOLEAN -> JType.BOOLEAN;
            case BYTE -> JType.BYTE;
            case SHORT -> JType.SHORT;
            case INT -> JType.INT;
            case LONG -> JType.LONG;
            case FLOAT -> JType.FLOAT;
            case DOUBLE -> JType.DOUBLE;
            case CHAR -> JType.CHAR;
            case ARRAY -> typeOf(((ArrayType) mirror).getComponentType()).array();
            case DECLARED -> {
                var declared = (DeclaredType) mirror;
                var raw = new ReferenceJType(
                    declared.asElement().toString()
                );
                var typeArgs = declared.getTypeArguments();
                if (typeArgs.isEmpty()) {
                    yield raw;
                }
                yield raw.typeArg(typeArgs.stream().map(JTypes::typeOf).toArray(JType[]::new));
            }
            case NONE -> JType.VOID;
            default -> throw new IllegalArgumentException("Unsupported type mirror kind: " + mirror.getKind());
        };
    }
}
