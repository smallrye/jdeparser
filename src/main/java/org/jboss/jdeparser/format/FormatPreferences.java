package org.jboss.jdeparser.format;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Properties;

import io.smallrye.common.constraint.Assert;

/**
 * Configuration controlling the formatting of generated Java source code.
 * <p>
 * Instances are created via the {@link #builder()} method.  All preferences
 * have sensible defaults; the builder is used only to customize specific settings.
 * Preferences can also be loaded from a {@link Properties} object via
 * {@link Builder#fromProperties(Properties)}.
 * <p>
 * The configuration includes:
 * <ul>
 *   <li>{@linkplain Indentation Indentation sizes} for different contexts
 *       (class members, labels, line continuations, etc.)</li>
 *   <li>{@linkplain Space Spacing rules} controlling whitespace around operators,
 *       keywords, braces, parentheses, and other syntactic elements</li>
 *   <li>{@linkplain Wrapping Line wrapping modes} for lists (parameters, exceptions,
 *       type arguments, etc.)</li>
 *   <li>{@linkplain Opt Formatting options} for miscellaneous preferences</li>
 *   <li>General settings: line length, tab usage, tab width</li>
 * </ul>
 *
 * @see JFiler
 */
public final class FormatPreferences {

    // ── Inner enums ─────────────────────────────────────────────────────

    /**
     * The type of whitespace to insert at a spacing point.
     */
    public enum SpaceType {
        /** No whitespace. */
        NONE,
        /** A single space character. */
        SPACE,
        /** A newline (line break). */
        NEWLINE,
        ;
    }

    /**
     * A spacing context: a named location in the source code where
     * whitespace may be inserted.
     * <p>
     * Each constant represents a specific syntactic position (e.g.,
     * "before the opening parenthesis of an {@code if} statement").
     * The {@link SpaceType} configured for each constant determines
     * whether a space, newline, or nothing is inserted at that position.
     */
    public enum Space {

        // ── Before parentheses ──────────────────────────────────────

        /** Before the opening parenthesis of a method call. */
        BEFORE_PAREN_METHOD_CALL,
        /** Before the opening parenthesis of a method declaration. */
        BEFORE_PAREN_METHOD_DECLARATION,
        /** Before the opening parenthesis of an {@code if} statement. */
        BEFORE_PAREN_IF,
        /** Before the opening parenthesis of a {@code for} statement. */
        BEFORE_PAREN_FOR,
        /** Before the opening parenthesis of a {@code while} statement. */
        BEFORE_PAREN_WHILE,
        /** Before the opening parenthesis of a {@code switch} statement/expression. */
        BEFORE_PAREN_SWITCH,
        /** Before the opening parenthesis of a {@code try} statement. */
        BEFORE_PAREN_TRY,
        /** Before the opening parenthesis of a {@code catch} clause. */
        BEFORE_PAREN_CATCH,
        /** Before the opening parenthesis of a {@code synchronized} statement. */
        BEFORE_PAREN_SYNCHRONIZED,
        /** Before the opening parenthesis of a cast expression. */
        BEFORE_PAREN_CAST,
        /** Before the opening parenthesis of an annotation with parameters. */
        BEFORE_PAREN_ANNOTATION_PARAM,
        /** Before the opening parenthesis of a record component list. */
        BEFORE_PAREN_RECORD,

        // ── Within parentheses ──────────────────────────────────────

        /** Within parentheses of an expression (grouping). */
        WITHIN_PAREN_EXPR,
        /** Within parentheses of a method call (non-empty). */
        WITHIN_PAREN_METHOD_CALL,
        /** Within parentheses of an empty method call. */
        WITHIN_PAREN_METHOD_CALL_EMPTY,
        /** Within parentheses of a method declaration (non-empty). */
        WITHIN_PAREN_METHOD_DECLARATION,
        /** Within parentheses of an empty method declaration. */
        WITHIN_PAREN_METHOD_DECLARATION_EMPTY,
        /** Within parentheses of an {@code if} condition. */
        WITHIN_PAREN_IF,
        /** Within parentheses of a {@code for} statement. */
        WITHIN_PAREN_FOR,
        /** Within parentheses of a {@code while} condition. */
        WITHIN_PAREN_WHILE,
        /** Within parentheses of a {@code switch} expression. */
        WITHIN_PAREN_SWITCH,
        /** Within parentheses of a {@code try} resource list. */
        WITHIN_PAREN_TRY,
        /** Within parentheses of a {@code catch} clause. */
        WITHIN_PAREN_CATCH,
        /** Within parentheses of a {@code synchronized} statement. */
        WITHIN_PAREN_SYNCHRONIZED,
        /** Within parentheses of a cast expression. */
        WITHIN_PAREN_CAST,
        /** Within parentheses of an annotation. */
        WITHIN_PAREN_ANNOTATION,
        /** Within parentheses of a record component list. */
        WITHIN_PAREN_RECORD,

        // ── Before braces ───────────────────────────────────────────

        /** Before an opening brace (default, when no specific context applies). */
        BEFORE_BRACE,
        /** Before the opening brace of a class body. */
        BEFORE_BRACE_CLASS,
        /** Before the opening brace of an interface body. */
        BEFORE_BRACE_INTERFACE,
        /** Before the opening brace of an enum body. */
        BEFORE_BRACE_ENUM,
        /** Before the opening brace of a record body. */
        BEFORE_BRACE_RECORD,
        /** Before the opening brace of an annotation type body. */
        BEFORE_BRACE_ANNOTATION_TYPE,
        /** Before the opening brace of a method body. */
        BEFORE_BRACE_METHOD,
        /** Before the opening brace of an {@code if} body. */
        BEFORE_BRACE_IF,
        /** Before the opening brace of an {@code else} body. */
        BEFORE_BRACE_ELSE,
        /** Before the opening brace of a {@code for} body. */
        BEFORE_BRACE_FOR,
        /** Before the opening brace of a {@code while} body. */
        BEFORE_BRACE_WHILE,
        /** Before the opening brace of a {@code do} body. */
        BEFORE_BRACE_DO,
        /** Before the opening brace of a {@code switch} body. */
        BEFORE_BRACE_SWITCH,
        /** Before the opening brace of a {@code try} body. */
        BEFORE_BRACE_TRY,
        /** Before the opening brace of a {@code catch} body. */
        BEFORE_BRACE_CATCH,
        /** Before the opening brace of a {@code finally} body. */
        BEFORE_BRACE_FINALLY,
        /** Before the opening brace of a {@code synchronized} body. */
        BEFORE_BRACE_SYNCHRONIZED,
        /** Before the opening brace of a lambda body. */
        BEFORE_BRACE_LAMBDA,
        /** Before the opening brace of an array initializer. */
        BEFORE_BRACE_ARRAY_INIT,
        /** Before the opening brace of an annotation array initializer. */
        BEFORE_BRACE_ANNOTATION_ARRAY_INIT,

        // ── Within braces ───────────────────────────────────────────

        /** Within braces of a code block. */
        WITHIN_BRACES_CODE,
        /** Within braces of an empty block. */
        WITHIN_BRACES_EMPTY,
        /** Within braces of an array initializer. */
        WITHIN_BRACES_ARRAY_INIT,

        // ── Around operators ────────────────────────────────────────

        /** Around assignment operators ({@code =}, {@code +=}, etc.). */
        AROUND_ASSIGN,
        /** Around logical operators ({@code &&}, {@code ||}). */
        AROUND_LOGICAL,
        /** Around equality operators ({@code ==}, {@code !=}). */
        AROUND_EQUALITY,
        /** Around relational operators ({@code <}, {@code >}, {@code <=}, {@code >=}). */
        AROUND_RELATIONAL,
        /** Around bitwise operators ({@code &}, {@code |}, {@code ^}). */
        AROUND_BITWISE,
        /** Around additive operators ({@code +}, {@code -}). */
        AROUND_ADDITIVE,
        /** Around multiplicative operators ({@code *}, {@code /}, {@code %}). */
        AROUND_MULTIPLICATIVE,
        /** Around shift operators ({@code <<}, {@code >>}, {@code >>>}). */
        AROUND_SHIFT,
        /** Around the arrow operator ({@code ->}). */
        AROUND_ARROW,
        /** Around the method reference operator ({@code ::}). */
        AROUND_METHOD_REF,
        /** Around the {@code &} separator in type bounds and intersection types. */
        AROUND_TYPE_BOUND_AND,
        /** Around the {@code |} separator in multi-catch clauses. */
        AROUND_MULTI_CATCH_OR,
        /** After the closing parenthesis of a cast expression. */
        AFTER_CAST,

        // ── Before keywords ─────────────────────────────────────────

        /** Before the {@code else} keyword. */
        BEFORE_KEYWORD_ELSE,
        /** Before the {@code while} keyword in a {@code do-while} loop. */
        BEFORE_KEYWORD_WHILE,
        /** Before the {@code catch} keyword. */
        BEFORE_KEYWORD_CATCH,
        /** Before the {@code finally} keyword. */
        BEFORE_KEYWORD_FINALLY,

        // ── Commas ──────────────────────────────────────────────────

        /** After a comma (general context). */
        AFTER_COMMA,
        /** After a comma in a type argument list. */
        AFTER_COMMA_TYPE_ARGUMENT,
        /** After a comma between enum constants. */
        COMMA_ENUM_CONSTANT,
        /** After a comma between record components. */
        AFTER_COMMA_RECORD_COMPONENT,

        // ── Ternary and colons ──────────────────────────────────────

        /** Before the {@code ?} in a ternary expression. */
        BEFORE_TERNARY_Q,
        /** After the {@code ?} in a ternary expression. */
        AFTER_TERNARY_Q,
        /** Before the {@code :} in a ternary expression. */
        BEFORE_TERNARY_COLON,
        /** After the {@code :} in a ternary expression. */
        AFTER_TERNARY_COLON,
        /** Before a colon (general context). */
        BEFORE_COLON,
        /** After a colon (general context). */
        AFTER_COLON,
        /** After a label's colon. */
        AFTER_LABEL,

        // ── Semicolons ──────────────────────────────────────────────

        /** After a semicolon (e.g., in {@code for} loop header). */
        AFTER_SEMICOLON,
        /** After a semicolon that terminates an empty statement (e.g., in {@code for (;;)}). */
        AFTER_SEMICOLON_EMPTY,

        // ── Annotations ─────────────────────────────────────────────

        /** After an annotation (on a declaration). */
        AFTER_ANNOTATION,
        /** After an annotation on a parameter. */
        AFTER_PARAM_ANNOTATION,

        // ── Declarations ────────────────────────────────────────────

        /** Before a method declaration (vertical spacing). */
        BEFORE_METHOD,
        /** Before a type declaration (vertical spacing). */
        BEFORE_CLASS,
        ;
    }

    /**
     * An indentation context: a named location where indentation is applied.
     * <p>
     * Each constant has a default indent size.  Indentation can be
     * <em>relative</em> (accumulated on top of parent indentation) or
     * <em>absolute</em> (replacing parent indentation).
     */
    public enum Indentation {
        /** Indentation of top-level members within a class/interface body. */
        MEMBERS_TOP_LEVEL(4),
        /** Indentation for labels. */
        LABELS(0),
        /** Indentation for case labels within a switch. */
        CASE_LABELS(0),
        /** Indentation for line continuations. */
        LINE_CONTINUATION(4),
        /** Indentation for block-level content. */
        LINE(4),
        /** Indentation for HTML tags in Javadoc. */
        HTML_TAG(2),
        ;

        /** The default indent size for this context. */
        private final int defaultSize;

        /**
         * Constructs an indentation constant with the given default size.
         *
         * @param defaultSize the default number of spaces
         */
        Indentation(final int defaultSize) {
            this.defaultSize = defaultSize;
        }

        /**
         * Returns the default indent size for this context.
         *
         * @return the default indent size in spaces
         */
        public int defaultSize() {
            return defaultSize;
        }
    }

    /**
     * A wrapping context: a named list that may be wrapped across lines.
     */
    public enum Wrapping {
        /** Exception list in a {@code throws} clause. */
        EXCEPTION_LIST,
        /** Superinterface list in an {@code implements} clause. */
        IMPLEMENTS_LIST,
        /** Supertype list in an {@code extends} clause. */
        EXTENDS_LIST,
        /** Permitted subtypes list in a {@code permits} clause. */
        PERMITS_LIST,
        /** Method parameter list. */
        PARAMETER_LIST,
        /** Method argument list. */
        ARGUMENT_LIST,
        /** Record component list. */
        RECORD_COMPONENT_LIST,
        /** Enum constant list. */
        ENUM_CONSTANT_LIST,
        ;
    }

    /**
     * The wrapping mode for a list context.
     */
    public enum WrappingMode {
        /** Always wrap (each element on its own line). */
        ALWAYS_WRAP,
        /** Wrap only if the line exceeds the configured line length. */
        WRAP_ONLY_IF_LONG,
        /** Never wrap (all elements on one line). */
        NEVER,
        ;
    }

    /**
     * Miscellaneous formatting options.
     */
    public enum Opt {
        /** Add a trailing comma after the last enum constant. */
        ENUM_TRAILING_COMMA,
        /** Add empty parentheses to enum constants with no arguments. */
        ENUM_EMPTY_PARENS,
        /** Use compact formatting for classes with only initializer blocks. */
        COMPACT_INIT_ONLY_CLASS,
        ;
    }

    // ── Default instance ────────────────────────────────────────────────

    /** The default format preferences. */
    private static final FormatPreferences DEFAULT = builder().build();

    // ── Instance fields ─────────────────────────────────────────────────

    /** Indent sizes per context. */
    private final EnumMap<Indentation, Integer> indents;

    /** Set of indentation contexts using absolute (non-accumulating) indentation. */
    private final EnumSet<Indentation> absoluteIndents;

    /** Spacing rules per context. */
    private final EnumMap<Space, SpaceType> spaceTypes;

    /** Active formatting options. */
    private final EnumSet<Opt> options;

    /** Wrapping modes per context. */
    private final EnumMap<Wrapping, WrappingMode> wrappingModes;

    /** Maximum line length before wrapping. */
    private final int lineLength;

    /** Whether to use tabs for indentation. */
    private final boolean useTabs;

    /** Display width of a tab character. */
    private final int tabWidth;

    /**
     * Constructs format preferences from a builder.
     *
     * @param b the builder
     */
    private FormatPreferences(final Builder b) {
        this.indents = b.indents.clone();
        this.absoluteIndents = b.absoluteIndents.clone();
        this.spaceTypes = b.spaceTypes.clone();
        this.options = b.options.clone();
        this.wrappingModes = b.wrappingModes.clone();
        this.lineLength = b.lineLength;
        this.useTabs = b.useTabs;
        this.tabWidth = b.tabWidth;
    }

    // ── Factory methods ─────────────────────────────────────────────────

    /**
     * Returns the default format preferences.
     *
     * @return the default preferences instance
     */
    public static FormatPreferences defaults() {
        return DEFAULT;
    }

    /**
     * Creates a new builder for customizing format preferences.
     *
     * @return a new builder initialized with default values
     */
    public static Builder builder() {
        return new Builder();
    }

    // ── Indentation queries ─────────────────────────────────────────────

    /**
     * Returns the configured indent size for the given context.
     *
     * @param indentation the indentation context
     * @return the indent size in spaces
     */
    public int getIndent(final Indentation indentation) {
        return indents.getOrDefault(indentation, indentation.defaultSize());
    }

    /**
     * Returns whether the given indentation context uses absolute positioning.
     * <p>
     * Absolute indentation replaces the parent indentation rather than
     * accumulating on top of it.
     *
     * @param indentation the indentation context
     * @return {@code true} if absolute, {@code false} if relative
     */
    public boolean isIndentAbsolute(final Indentation indentation) {
        return absoluteIndents.contains(indentation);
    }

    // ── Spacing queries ─────────────────────────────────────────────────

    /**
     * Returns the spacing type configured for the given context.
     *
     * @param space the spacing context
     * @return the space type (never {@code null})
     */
    public SpaceType getSpaceType(final Space space) {
        return spaceTypes.getOrDefault(space, SpaceType.NONE);
    }

    // ── Option queries ──────────────────────────────────────────────────

    /**
     * Returns whether the given formatting option is enabled.
     *
     * @param opt the option to test
     * @return {@code true} if the option is enabled
     */
    public boolean hasOption(final Opt opt) {
        return options.contains(opt);
    }

    // ── Wrapping queries ────────────────────────────────────────────────

    /**
     * Returns the wrapping mode configured for the given context.
     *
     * @param wrapping the wrapping context
     * @return the wrapping mode (never {@code null})
     */
    public WrappingMode getWrapMode(final Wrapping wrapping) {
        return wrappingModes.getOrDefault(wrapping, WrappingMode.NEVER);
    }

    // ── General queries ─────────────────────────────────────────────────

    /**
     * Returns the maximum preferred line width before wrapping.
     *
     * @return the maximum line length in columns
     */
    public int lineLength() {
        return lineLength;
    }

    /**
     * Returns whether tabs should be used for indentation instead of spaces.
     *
     * @return {@code true} if tabs are used
     */
    public boolean useTabs() {
        return useTabs;
    }

    /**
     * Returns the display width of a tab character (used for column tracking).
     *
     * @return the tab width in columns
     */
    public int tabWidth() {
        return tabWidth;
    }

    // ── Builder ─────────────────────────────────────────────────────────

    /**
     * Builder for constructing {@link FormatPreferences} instances.
     * <p>
     * All settings are initialized with sensible defaults.  Use the
     * setter methods to customize specific preferences.
     */
    public static final class Builder {

        /** Indent sizes per context. */
        private final EnumMap<Indentation, Integer> indents = new EnumMap<>(Indentation.class);

        /** Absolute indentation contexts. */
        private final EnumSet<Indentation> absoluteIndents = EnumSet.noneOf(Indentation.class);

        /** Spacing rules per context. */
        private final EnumMap<Space, SpaceType> spaceTypes = new EnumMap<>(Space.class);

        /** Active options. */
        private final EnumSet<Opt> options = EnumSet.noneOf(Opt.class);

        /** Wrapping modes per context. */
        private final EnumMap<Wrapping, WrappingMode> wrappingModes = new EnumMap<>(Wrapping.class);

        /** Maximum line length. */
        private int lineLength = 140;

        /** Whether to use tabs. */
        private boolean useTabs = false;

        /** Tab display width. */
        private int tabWidth = 4;

        /**
         * Constructs a builder with default values.
         */
        private Builder() {
            initDefaults();
        }

        /**
         * Initializes all defaults.  Called once from the constructor.
         */
        private void initDefaults() {
            // indentation defaults are provided by Indentation.defaultSize()

            // spacing defaults: operators get SPACE
            spaceTypes.put(Space.AROUND_ASSIGN, SpaceType.SPACE);
            spaceTypes.put(Space.AROUND_LOGICAL, SpaceType.SPACE);
            spaceTypes.put(Space.AROUND_EQUALITY, SpaceType.SPACE);
            spaceTypes.put(Space.AROUND_RELATIONAL, SpaceType.SPACE);
            spaceTypes.put(Space.AROUND_BITWISE, SpaceType.SPACE);
            spaceTypes.put(Space.AROUND_ADDITIVE, SpaceType.SPACE);
            spaceTypes.put(Space.AROUND_MULTIPLICATIVE, SpaceType.SPACE);
            spaceTypes.put(Space.AROUND_SHIFT, SpaceType.SPACE);
            spaceTypes.put(Space.AROUND_ARROW, SpaceType.SPACE);
            spaceTypes.put(Space.AROUND_TYPE_BOUND_AND, SpaceType.SPACE);
            spaceTypes.put(Space.AROUND_MULTI_CATCH_OR, SpaceType.SPACE);
            spaceTypes.put(Space.AFTER_CAST, SpaceType.SPACE);

            // spacing defaults: ternary gets SPACE
            spaceTypes.put(Space.BEFORE_TERNARY_Q, SpaceType.SPACE);
            spaceTypes.put(Space.AFTER_TERNARY_Q, SpaceType.SPACE);
            spaceTypes.put(Space.BEFORE_TERNARY_COLON, SpaceType.SPACE);
            spaceTypes.put(Space.AFTER_TERNARY_COLON, SpaceType.SPACE);

            // spacing defaults: commas get SPACE after
            spaceTypes.put(Space.AFTER_COMMA, SpaceType.SPACE);
            spaceTypes.put(Space.AFTER_COMMA_TYPE_ARGUMENT, SpaceType.SPACE);
            spaceTypes.put(Space.AFTER_COMMA_RECORD_COMPONENT, SpaceType.SPACE);
            spaceTypes.put(Space.AFTER_SEMICOLON, SpaceType.SPACE);

            // spacing defaults: enum constants get NEWLINE after comma
            spaceTypes.put(Space.COMMA_ENUM_CONSTANT, SpaceType.NEWLINE);

            // spacing defaults: braces in code blocks get NEWLINE inside
            spaceTypes.put(Space.WITHIN_BRACES_CODE, SpaceType.NEWLINE);

            // spacing defaults: braces before type/method bodies get SPACE
            spaceTypes.put(Space.BEFORE_BRACE, SpaceType.SPACE);
            spaceTypes.put(Space.BEFORE_BRACE_CLASS, SpaceType.SPACE);
            spaceTypes.put(Space.BEFORE_BRACE_INTERFACE, SpaceType.SPACE);
            spaceTypes.put(Space.BEFORE_BRACE_ENUM, SpaceType.SPACE);
            spaceTypes.put(Space.BEFORE_BRACE_RECORD, SpaceType.SPACE);
            spaceTypes.put(Space.BEFORE_BRACE_ANNOTATION_TYPE, SpaceType.SPACE);
            spaceTypes.put(Space.BEFORE_BRACE_METHOD, SpaceType.SPACE);
            spaceTypes.put(Space.BEFORE_BRACE_IF, SpaceType.SPACE);
            spaceTypes.put(Space.BEFORE_BRACE_ELSE, SpaceType.SPACE);
            spaceTypes.put(Space.BEFORE_BRACE_FOR, SpaceType.SPACE);
            spaceTypes.put(Space.BEFORE_BRACE_WHILE, SpaceType.SPACE);
            spaceTypes.put(Space.BEFORE_BRACE_DO, SpaceType.SPACE);
            spaceTypes.put(Space.BEFORE_BRACE_SWITCH, SpaceType.SPACE);
            spaceTypes.put(Space.BEFORE_BRACE_TRY, SpaceType.SPACE);
            spaceTypes.put(Space.BEFORE_BRACE_CATCH, SpaceType.SPACE);
            spaceTypes.put(Space.BEFORE_BRACE_FINALLY, SpaceType.SPACE);
            spaceTypes.put(Space.BEFORE_BRACE_SYNCHRONIZED, SpaceType.SPACE);
            spaceTypes.put(Space.BEFORE_BRACE_LAMBDA, SpaceType.SPACE);
            spaceTypes.put(Space.BEFORE_BRACE_ARRAY_INIT, SpaceType.SPACE);
            spaceTypes.put(Space.WITHIN_BRACES_ARRAY_INIT, SpaceType.SPACE);

            // spacing defaults: colons
            spaceTypes.put(Space.BEFORE_COLON, SpaceType.SPACE);
            spaceTypes.put(Space.AFTER_COLON, SpaceType.SPACE);

            // spacing defaults: keyword before-spacing
            spaceTypes.put(Space.BEFORE_KEYWORD_ELSE, SpaceType.SPACE);
            spaceTypes.put(Space.BEFORE_KEYWORD_WHILE, SpaceType.SPACE);
            spaceTypes.put(Space.BEFORE_KEYWORD_CATCH, SpaceType.SPACE);
            spaceTypes.put(Space.BEFORE_KEYWORD_FINALLY, SpaceType.SPACE);

            // spacing defaults: control flow parens get SPACE before
            spaceTypes.put(Space.BEFORE_PAREN_IF, SpaceType.SPACE);
            spaceTypes.put(Space.BEFORE_PAREN_FOR, SpaceType.SPACE);
            spaceTypes.put(Space.BEFORE_PAREN_WHILE, SpaceType.SPACE);
            spaceTypes.put(Space.BEFORE_PAREN_SWITCH, SpaceType.SPACE);
            spaceTypes.put(Space.BEFORE_PAREN_TRY, SpaceType.SPACE);
            spaceTypes.put(Space.BEFORE_PAREN_CATCH, SpaceType.SPACE);
            spaceTypes.put(Space.BEFORE_PAREN_SYNCHRONIZED, SpaceType.SPACE);

            // spacing defaults: annotations get NEWLINE after
            spaceTypes.put(Space.AFTER_ANNOTATION, SpaceType.NEWLINE);
            spaceTypes.put(Space.AFTER_PARAM_ANNOTATION, SpaceType.SPACE);

            // spacing defaults: declarations get NEWLINE before
            spaceTypes.put(Space.BEFORE_METHOD, SpaceType.NEWLINE);
            spaceTypes.put(Space.BEFORE_CLASS, SpaceType.NEWLINE);

            // spacing defaults: labels
            spaceTypes.put(Space.AFTER_LABEL, SpaceType.NEWLINE);

            // option defaults
            options.add(Opt.COMPACT_INIT_ONLY_CLASS);

            // wrapping defaults
            wrappingModes.put(Wrapping.EXCEPTION_LIST, WrappingMode.WRAP_ONLY_IF_LONG);
            wrappingModes.put(Wrapping.IMPLEMENTS_LIST, WrappingMode.WRAP_ONLY_IF_LONG);
            wrappingModes.put(Wrapping.EXTENDS_LIST, WrappingMode.WRAP_ONLY_IF_LONG);
            wrappingModes.put(Wrapping.PERMITS_LIST, WrappingMode.WRAP_ONLY_IF_LONG);
            wrappingModes.put(Wrapping.PARAMETER_LIST, WrappingMode.WRAP_ONLY_IF_LONG);
            wrappingModes.put(Wrapping.ARGUMENT_LIST, WrappingMode.WRAP_ONLY_IF_LONG);
            wrappingModes.put(Wrapping.RECORD_COMPONENT_LIST, WrappingMode.WRAP_ONLY_IF_LONG);
            wrappingModes.put(Wrapping.ENUM_CONSTANT_LIST, WrappingMode.ALWAYS_WRAP);
        }

        // ── Indentation setters ─────────────────────────────────────

        /**
         * Sets the indent size for the given context.
         *
         * @param indentation the indentation context
         * @param size        the indent size in spaces (must be non-negative)
         * @return this builder
         */
        public Builder indent(final Indentation indentation, final int size) {
            Assert.checkNotNullParam("indentation", indentation);
            Assert.checkMinimumParameter("size", 0, size);
            indents.put(indentation, size);
            return this;
        }

        /**
         * Marks the given indentation context as absolute.
         * <p>
         * Absolute indentation replaces the parent rather than accumulating.
         *
         * @param indentation the indentation context
         * @return this builder
         */
        public Builder indentAbsolute(final Indentation indentation) {
            Assert.checkNotNullParam("indentation", indentation);
            absoluteIndents.add(indentation);
            return this;
        }

        /**
         * Marks the given indentation context as relative (the default).
         *
         * @param indentation the indentation context
         * @return this builder
         */
        public Builder indentRelative(final Indentation indentation) {
            Assert.checkNotNullParam("indentation", indentation);
            absoluteIndents.remove(indentation);
            return this;
        }

        // ── Spacing setters ─────────────────────────────────────────

        /**
         * Sets the spacing type for the given context.
         *
         * @param space the spacing context
         * @param type  the space type
         * @return this builder
         */
        public Builder space(final Space space, final SpaceType type) {
            Assert.checkNotNullParam("space", space);
            Assert.checkNotNullParam("type", type);
            spaceTypes.put(space, type);
            return this;
        }

        // ── Option setters ──────────────────────────────────────────

        /**
         * Enables the given formatting option.
         *
         * @param opt the option to enable
         * @return this builder
         */
        public Builder addOption(final Opt opt) {
            Assert.checkNotNullParam("opt", opt);
            options.add(opt);
            return this;
        }

        /**
         * Disables the given formatting option.
         *
         * @param opt the option to disable
         * @return this builder
         */
        public Builder removeOption(final Opt opt) {
            Assert.checkNotNullParam("opt", opt);
            options.remove(opt);
            return this;
        }

        // ── Wrapping setters ────────────────────────────────────────

        /**
         * Sets the wrapping mode for the given context.
         *
         * @param wrapping the wrapping context
         * @param mode     the wrapping mode
         * @return this builder
         */
        public Builder wrapMode(final Wrapping wrapping, final WrappingMode mode) {
            Assert.checkNotNullParam("wrapping", wrapping);
            Assert.checkNotNullParam("mode", mode);
            wrappingModes.put(wrapping, mode);
            return this;
        }

        // ── General setters ─────────────────────────────────────────

        /**
         * Sets the maximum preferred line width.
         *
         * @param lineLength the maximum line length in columns (must be positive)
         * @return this builder
         */
        public Builder lineLength(final int lineLength) {
            Assert.checkMinimumParameter("lineLength", 1, lineLength);
            this.lineLength = lineLength;
            return this;
        }

        /**
         * Sets whether to use tabs for indentation.
         *
         * @param useTabs {@code true} to use tabs, {@code false} for spaces
         * @return this builder
         */
        public Builder useTabs(final boolean useTabs) {
            this.useTabs = useTabs;
            return this;
        }

        /**
         * Sets the display width of a tab character.
         *
         * @param tabWidth the tab width in columns (must be positive)
         * @return this builder
         */
        public Builder tabWidth(final int tabWidth) {
            Assert.checkMinimumParameter("tabWidth", 1, tabWidth);
            this.tabWidth = tabWidth;
            return this;
        }

        // ── Properties loading ──────────────────────────────────────

        /**
         * Loads preferences from a {@link Properties} object.
         * <p>
         * Recognized key patterns:
         * <ul>
         *   <li>{@code line-length} — maximum line length (integer)</li>
         *   <li>{@code use-tabs} — use tabs for indentation (boolean)</li>
         *   <li>{@code tab-width} — tab display width (integer)</li>
         *   <li>{@code indent.<name>} — indent size for context (integer),
         *       where {@code <name>} is the lowercase, hyphen-separated form
         *       of an {@link Indentation} constant (e.g., {@code members-top-level})</li>
         *   <li>{@code indent.<name>.absolute} — whether indentation is absolute (boolean)</li>
         *   <li>{@code space.<name>} — space type for context ({@code NONE},
         *       {@code SPACE}, or {@code NEWLINE}), where {@code <name>} is the
         *       lowercase, hyphen-separated form of a {@link Space} constant</li>
         *   <li>{@code wrapping.<name>} — wrapping mode ({@code ALWAYS_WRAP},
         *       {@code WRAP_ONLY_IF_LONG}, or {@code NEVER})</li>
         *   <li>{@code option.<name>} — formatting option (boolean)</li>
         * </ul>
         * <p>
         * Unknown keys are silently ignored.
         *
         * @param properties the properties to load from
         * @return this builder
         */
        public Builder fromProperties(final Properties properties) {
            Assert.checkNotNullParam("properties", properties);
            String val;
            if ((val = properties.getProperty("line-length")) != null) {
                this.lineLength = Integer.parseInt(val);
            }
            if ((val = properties.getProperty("use-tabs")) != null) {
                this.useTabs = Boolean.parseBoolean(val);
            }
            if ((val = properties.getProperty("tab-width")) != null) {
                this.tabWidth = Integer.parseInt(val);
            }
            for (Indentation ind : Indentation.values()) {
                String key = toPropertyName(ind.name());
                if ((val = properties.getProperty("indent." + key)) != null) {
                    this.indents.put(ind, Integer.parseInt(val));
                }
                if ((val = properties.getProperty("indent." + key + ".absolute")) != null) {
                    if (Boolean.parseBoolean(val)) {
                        this.absoluteIndents.add(ind);
                    } else {
                        this.absoluteIndents.remove(ind);
                    }
                }
            }
            for (Space sp : Space.values()) {
                String key = toPropertyName(sp.name());
                if ((val = properties.getProperty("space." + key)) != null) {
                    this.spaceTypes.put(sp, SpaceType.valueOf(val.toUpperCase(Locale.ROOT)));
                }
            }
            for (Wrapping w : Wrapping.values()) {
                String key = toPropertyName(w.name());
                if ((val = properties.getProperty("wrapping." + key)) != null) {
                    this.wrappingModes.put(w, WrappingMode.valueOf(val.toUpperCase(Locale.ROOT)));
                }
            }
            for (Opt opt : Opt.values()) {
                String key = toPropertyName(opt.name());
                if ((val = properties.getProperty("option." + key)) != null) {
                    if (Boolean.parseBoolean(val)) {
                        this.options.add(opt);
                    } else {
                        this.options.remove(opt);
                    }
                }
            }
            return this;
        }

        /**
         * Builds the {@link FormatPreferences} instance.
         *
         * @return the configured preferences
         */
        public FormatPreferences build() {
            return new FormatPreferences(this);
        }

        /**
         * Converts an enum constant name to a property key name.
         * <p>
         * Lowercases the name and replaces underscores with hyphens
         * (e.g., {@code "MEMBERS_TOP_LEVEL"} becomes {@code "members-top-level"}).
         *
         * @param enumName the enum constant name
         * @return the property key form
         */
        private static String toPropertyName(final String enumName) {
            return enumName.toLowerCase(Locale.ROOT).replace('_', '-');
        }
    }
}
