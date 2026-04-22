package org.jboss.jdeparser.impl;

/**
 * A lexical token that can write itself and serve as a state marker
 * in the {@link SourceFileWriter}'s token state machine.
 * <p>
 * The token state machine tracks which kind of token was last written
 * (keyword, identifier, number, punctuation, etc.) so the formatter can
 * insert appropriate spacing between adjacent tokens.  Every token
 * written via {@link SourceFileWriter#write(Token)} becomes the new
 * state, and subsequent tokens query the state to decide whether a
 * separating space is required.
 *
 * @see Tokens
 * @see SourceFileWriter#write(Token)
 * @see SourceFileWriter#getState()
 */
public interface Token extends Writable {
}
