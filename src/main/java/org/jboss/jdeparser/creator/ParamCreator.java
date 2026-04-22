package org.jboss.jdeparser.creator;

import org.jboss.jdeparser.impl.ParamCreatorImpl;

/**
 * A creator for configuring a method or constructor parameter.
 * <p>
 * Extends {@link ModifiableCreator} for annotations and the {@code final} modifier,
 * and {@link DocCommentableCreator} so that documentation set here is automatically
 * generated as a {@code @param} tag in the enclosing method or constructor Javadoc.
 */
public sealed interface ParamCreator extends ModifiableCreator permits ParamCreatorImpl {
}
