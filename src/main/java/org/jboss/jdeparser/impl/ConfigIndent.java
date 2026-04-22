package org.jboss.jdeparser.impl;

import org.jboss.jdeparser.format.FormatPreferences;
import org.jboss.jdeparser.format.FormatPreferences.Indentation;

/**
 * An indentation element driven by a {@link FormatPreferences.Indentation}
 * context and the corresponding configuration in {@link FormatPreferences}.
 * <p>
 * When adding indentation, this element appends the configured number of
 * spaces (or tabs) for its indentation context.  If the indentation is
 * <em>relative</em> (the default), the parent indentation is applied first
 * by delegating to the next element in the chain; if <em>absolute</em>,
 * the parent indentation is skipped and only this element's indent is used.
 * <p>
 * Escaping is not modified by this element; it delegates directly to the
 * next element in the chain.
 *
 * @see Indent
 * @see FormatPreferences.Indentation
 */
public final class ConfigIndent implements Indent {

    /** The indentation context this element represents. */
    private final Indentation indentation;

    /**
     * Constructs a configuration-driven indent element.
     *
     * @param indentation the indentation context
     */
    public ConfigIndent(final Indentation indentation) {
        this.indentation = indentation;
    }

    /**
     * Returns the indentation context.
     *
     * @return the indentation context
     */
    public Indentation indentation() {
        return indentation;
    }

    /**
     * {@inheritDoc}
     * <p>
     * If this indentation is relative, delegates to the next element first
     * to accumulate parent indentation.  Then appends this context's
     * configured indent (as spaces or tabs depending on preferences).
     */
    @Override
    public void addIndent(final Indent next, final FormatPreferences preferences, final StringBuilder lineBuffer) {
        // relative: accumulate parent indentation first; absolute: skip parent
        if (!preferences.isIndentAbsolute(indentation)) {
            next.addIndent(next, preferences, lineBuffer);
        }
        final int indent = preferences.getIndent(indentation);
        if (preferences.useTabs()) {
            final int tabWidth = preferences.tabWidth();
            final int tabs = indent / tabWidth;
            final int spaces = indent % tabWidth;
            for (int i = 0; i < tabs; i++) {
                lineBuffer.append('\t');
            }
            for (int i = 0; i < spaces; i++) {
                lineBuffer.append(' ');
            }
        } else {
            for (int i = 0; i < indent; i++) {
                lineBuffer.append(' ');
            }
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Delegates directly to the next element without modification.
     */
    @Override
    public void escape(final Indent next, final StringBuilder b, final int idx) {
        next.escape(next, b, idx);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Delegates directly to the next element without modification.
     */
    @Override
    public void unescaped(final Indent next, final StringBuilder b, final int idx) {
        next.unescaped(next, b, idx);
    }
}
