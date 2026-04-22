package org.jboss.jdeparser.creator;

import java.util.function.Consumer;

/**
 * A creator for declarations that can have Javadoc comments.
 * <p>
 * Provides the {@link #docComment(Consumer)} method which accepts a callback
 * to configure the documentation content.
 */
public sealed interface DocCommentableCreator permits EnumConstantCreator, ModifiableCreator, RecordComponentCreator, TypeParamCreator {

    /**
     * Configures a Javadoc comment for this declaration.
     *
     * @param builder the callback to configure the doc comment content
     */
    void docComment(Consumer<DocCommentCreator> builder);
}
