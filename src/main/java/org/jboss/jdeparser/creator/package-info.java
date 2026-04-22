/**
 * Borrow-pattern creator interfaces for building Java source constructs.
 * <p>
 * Each creator interface represents a scope in the source file being generated.
 * Creators are passed as arguments to {@link java.util.function.Consumer} callbacks,
 * and are valid only for the duration of the callback invocation.  Attempting to use
 * a creator after its callback has returned will result in an {@link IllegalStateException}.
 */
package org.jboss.jdeparser.creator;
