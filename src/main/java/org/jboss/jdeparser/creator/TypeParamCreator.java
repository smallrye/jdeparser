package org.jboss.jdeparser.creator;

import java.util.List;

import org.jboss.jdeparser.JType;
import org.jboss.jdeparser.impl.TypeParamCreatorImpl;

/**
 * A creator for configuring a type parameter on a generic type or method.
 * <p>
 * Extends {@link DocCommentableCreator} so that documentation set here is
 * automatically generated as a {@code @param <T>} tag in the enclosing
 * type or method Javadoc.
 */
public sealed interface TypeParamCreator extends DocCommentableCreator permits TypeParamCreatorImpl {

    /**
     * Adds an upper bound to this type parameter (e.g., {@code T extends Comparable & Serializable}).
     *
     * @param bound the bound type
     */
    default void extends_(JType bound) {
        extends_(List.of(bound));
    }

    /**
     * Adds upper bounds to this type parameter (e.g., {@code T extends Comparable & Serializable}).
     *
     * @param bounds the bound types
     */
    default void extends_(JType... bounds) {
        extends_(List.of(bounds));
    }

    /**
     * Adds upper bounds to this type parameter (e.g., {@code T extends Comparable & Serializable}).
     *
     * @param bounds the bound types
     */
    void extends_(List<JType> bounds);
}
