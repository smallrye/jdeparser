package org.jboss.jdeparser.creator;

import org.jboss.jdeparser.impl.LocalVarCreatorImpl;

/**
 * A creator for configuring a local variable declaration.
 * <p>
 * Extends {@link AnnotatableCreator} for adding annotations to the
 * local variable declaration.  Also supports the {@code final} modifier.
 * <p>
 * Local variables do not have access modifiers, so this interface
 * extends {@link AnnotatableCreator} directly rather than
 * {@link ModifiableCreator}.
 */
public sealed interface LocalVarCreator extends AnnotatableCreator permits LocalVarCreatorImpl {

    /**
     * Marks the local variable as {@code final}.
     */
    void final_();
}
