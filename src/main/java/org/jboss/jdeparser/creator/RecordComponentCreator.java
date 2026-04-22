package org.jboss.jdeparser.creator;

import org.jboss.jdeparser.impl.RecordComponentCreatorImpl;

/**
 * A creator for configuring a record component.
 * <p>
 * Extends {@link AnnotatableCreator} for adding annotations and
 * {@link DocCommentableCreator} so that documentation set here is
 * replicated to both the record type {@code @param} tag and the
 * canonical constructor {@code @param} tag.
 */
public sealed interface RecordComponentCreator extends AnnotatableCreator, DocCommentableCreator permits RecordComponentCreatorImpl {
}
