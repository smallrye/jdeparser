package org.jboss.jdeparser.impl;

import java.io.IOException;
import java.util.Locale;

import org.jboss.jdeparser.format.FormatPreferences.Space;

/**
 * All lexical tokens used by the {@link SourceFileWriter}'s token state
 * machine, organized as an outer enum of state markers with nested enums
 * for punctuation, keywords, operators, and bracket pairs.
 * <p>
 * The outer constants ({@link #$START}, {@link #$WORD}, etc.) are state
 * markers that do not write any content; they are set as the writer's
 * state when identifiers, numbers, or other non-token content is written.
 * <p>
 * The nested enums ({@link $PUNCT}, {@link $KW}, {@link $BINOP}, etc.)
 * are actual tokens that write their content and manage spacing according
 * to their associated {@link Space} rules.
 */
public enum Tokens implements Token {

    // ── State markers (no content written) ──────────────────────────────

    /** Initial state before any content is written. */
    $START,
    /** State after writing a comment. */
    $COMMENT,
    /** State after writing a string literal. */
    $STRING_LIT,
    /** State after writing an identifier (class name, variable name, etc.). */
    $WORD,
    /** State after writing a numeric literal. */
    $NUMBER,
    ;

    /**
     * {@inheritDoc}
     * <p>
     * State markers do not write any content.
     */
    @Override
    public void write(final SourceFileWriter writer) throws IOException {
        // state markers are not written
    }

    // ── Punctuation ─────────────────────────────────────────────────────

    /**
     * Punctuation tokens: semicolons, dots, commas, colons, and other
     * single-character or short punctuation marks.
     */
    public enum $PUNCT implements Token {
        /** Semicolon ({@code ;}). */
        SEMI(";"),
        /** Dot ({@code .}). */
        DOT("."),
        /** Question mark ({@code ?}). */
        Q("?"),
        /** Colon ({@code :}). */
        COLON(":"),
        /** At sign ({@code @}). */
        AT("@"),
        /** Comma ({@code ,}). */
        COMMA(","),
        /** Ellipsis ({@code ...}). */
        ELLIPSIS("..."),
        ;

        /** The punctuation text. */
        private final String text;

        /**
         * Constructs a punctuation token.
         *
         * @param text the punctuation text
         */
        $PUNCT(final String text) {
            this.text = text;
        }

        /**
         * Returns the punctuation text.
         *
         * @return the text
         */
        public String text() {
            return text;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void write(final SourceFileWriter writer) throws IOException {
            writer.writeEscaped(text);
        }
    }

    // ── Comment syntax tokens ───────────────────────────────────────────

    /**
     * Comment delimiter tokens.
     */
    public enum $COMMENT_TOK implements Token {
        /** Line comment start ({@code //}). */
        LINE("//"),
        /** Block comment open ({@code /*}). */
        OPEN("/*"),
        /** Javadoc comment open ({@code /**}). */
        OPEN_DOC("/**"),
        /** Block/Javadoc comment close ({@code * /}). */
        CLOSE("*/"),
        ;

        /** The comment delimiter text. */
        private final String text;

        /**
         * Constructs a comment delimiter token.
         *
         * @param text the delimiter text
         */
        $COMMENT_TOK(final String text) {
            this.text = text;
        }

        /**
         * Returns the comment delimiter text.
         *
         * @return the text
         */
        public String text() {
            return text;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void write(final SourceFileWriter writer) throws IOException {
            writer.writeEscaped(text);
        }
    }

    // ── Unary operators ─────────────────────────────────────────────────

    /**
     * Unary operator tokens.
     */
    public enum $UNOP implements Token {
        /** Bitwise complement ({@code ~}). */
        COMP("~"),
        /** Logical not ({@code !}). */
        NOT("!"),
        /** Unary minus ({@code -}). */
        MINUS("-"),
        /** Unary plus ({@code +}). */
        PLUS("+"),
        /** Increment ({@code ++}). */
        PP("++"),
        /** Decrement ({@code --}). */
        MM("--"),
        ;

        /** The operator text. */
        private final String text;

        /**
         * Constructs a unary operator token.
         *
         * @param text the operator text
         */
        $UNOP(final String text) {
            this.text = text;
        }

        /**
         * Returns the operator text.
         *
         * @return the text
         */
        public String text() {
            return text;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void write(final SourceFileWriter writer) throws IOException {
            writer.writeEscaped(text);
        }
    }

    // ── Binary operators ────────────────────────────────────────────────

    /**
     * Binary operator tokens, each associated with a {@link Space} rule
     * that controls spacing around the operator.
     */
    public enum $BINOP implements Token {
        // arithmetic
        /** Addition ({@code +}). */
        PLUS("+", Space.AROUND_ADDITIVE),
        /** Subtraction ({@code -}). */
        MINUS("-", Space.AROUND_ADDITIVE),
        /** Multiplication ({@code *}). */
        TIMES("*", Space.AROUND_MULTIPLICATIVE),
        /** Division ({@code /}). */
        DIV("/", Space.AROUND_MULTIPLICATIVE),
        /** Modulo ({@code %}). */
        MOD("%", Space.AROUND_MULTIPLICATIVE),

        // bitwise
        /** Bitwise AND ({@code &}). */
        BIT_AND("&", Space.AROUND_BITWISE),
        /** Bitwise OR ({@code |}). */
        BIT_OR("|", Space.AROUND_BITWISE),
        /** Bitwise XOR ({@code ^}). */
        BIT_XOR("^", Space.AROUND_BITWISE),

        // logical
        /** Logical AND ({@code &&}). */
        LOGICAL_AND("&&", Space.AROUND_LOGICAL),
        /** Logical OR ({@code ||}). */
        LOGICAL_OR("||", Space.AROUND_LOGICAL),

        // shift
        /** Left shift ({@code <<}). */
        SHL("<<", Space.AROUND_SHIFT),
        /** Right shift ({@code >>}). */
        SHR(">>", Space.AROUND_SHIFT),
        /** Unsigned right shift ({@code >>>}). */
        USHR(">>>", Space.AROUND_SHIFT),

        // equality
        /** Equals ({@code ==}). */
        EQ("==", Space.AROUND_EQUALITY),
        /** Not equals ({@code !=}). */
        NE("!=", Space.AROUND_EQUALITY),

        // relational
        /** Less than ({@code <}). */
        LT("<", Space.AROUND_RELATIONAL),
        /** Greater than ({@code >}). */
        GT(">", Space.AROUND_RELATIONAL),
        /** Less or equal ({@code <=}). */
        LE("<=", Space.AROUND_RELATIONAL),
        /** Greater or equal ({@code >=}). */
        GE(">=", Space.AROUND_RELATIONAL),

        // assignment
        /** Simple assignment ({@code =}). */
        ASSIGN("=", Space.AROUND_ASSIGN),
        /** Addition assignment ({@code +=}). */
        PLUS_ASSIGN("+=", Space.AROUND_ASSIGN),
        /** Subtraction assignment ({@code -=}). */
        MINUS_ASSIGN("-=", Space.AROUND_ASSIGN),
        /** Multiplication assignment ({@code *=}). */
        TIMES_ASSIGN("*=", Space.AROUND_ASSIGN),
        /** Division assignment ({@code /=}). */
        DIV_ASSIGN("/=", Space.AROUND_ASSIGN),
        /** Modulo assignment ({@code %=}). */
        MOD_ASSIGN("%=", Space.AROUND_ASSIGN),
        /** Bitwise AND assignment ({@code &=}). */
        AND_ASSIGN("&=", Space.AROUND_ASSIGN),
        /** Bitwise OR assignment ({@code |=}). */
        OR_ASSIGN("|=", Space.AROUND_ASSIGN),
        /** Bitwise XOR assignment ({@code ^=}). */
        XOR_ASSIGN("^=", Space.AROUND_ASSIGN),
        /** Left shift assignment ({@code <<=}). */
        SHL_ASSIGN("<<=", Space.AROUND_ASSIGN),
        /** Right shift assignment ({@code >>=}). */
        SHR_ASSIGN(">>=", Space.AROUND_ASSIGN),
        /** Unsigned right shift assignment ({@code >>>=}). */
        USHR_ASSIGN(">>>=", Space.AROUND_ASSIGN),

        // special
        /** Arrow ({@code ->}). */
        ARROW("->", Space.AROUND_ARROW),
        /** Method reference ({@code ::}). */
        DBL_COLON("::", Space.AROUND_METHOD_REF),
        ;

        /** The operator text. */
        private final String text;

        /** The spacing rule for this operator. */
        private final Space spacingRule;

        /**
         * Constructs a binary operator token.
         *
         * @param text        the operator text
         * @param spacingRule the spacing rule for around-operator spacing
         */
        $BINOP(final String text, final Space spacingRule) {
            this.text = text;
            this.spacingRule = spacingRule;
        }

        /**
         * Returns the operator text.
         *
         * @return the text
         */
        public String text() {
            return text;
        }

        /**
         * Returns the spacing rule for this operator.
         *
         * @return the spacing rule
         */
        public Space spacingRule() {
            return spacingRule;
        }

        /**
         * {@inheritDoc}
         * <p>
         * Writes the operator with its configured spacing on both sides.
         */
        @Override
        public void write(final SourceFileWriter writer) throws IOException {
            writer.write(spacingRule);
            writer.writeEscaped(text);
            writer.write(spacingRule);
        }
    }

    // ── Bracket pairs ───────────────────────────────────────────────────

    /**
     * Parenthesis tokens.
     */
    public enum $PAREN implements Token {
        /** Opening parenthesis ({@code (}). */
        OPEN("("),
        /** Closing parenthesis ({@code )}). */
        CLOSE(")"),
        ;

        /** The bracket text. */
        private final String text;

        /**
         * Constructs a parenthesis token.
         *
         * @param text the bracket character
         */
        $PAREN(final String text) {
            this.text = text;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void write(final SourceFileWriter writer) throws IOException {
            writer.writeEscaped(text);
        }
    }

    /**
     * Angle bracket tokens (for generics).
     */
    public enum $ANGLE implements Token {
        /** Opening angle bracket ({@code <}). */
        OPEN("<"),
        /** Closing angle bracket ({@code >}). */
        CLOSE(">"),
        ;

        /** The bracket text. */
        private final String text;

        /**
         * Constructs an angle bracket token.
         *
         * @param text the bracket character
         */
        $ANGLE(final String text) {
            this.text = text;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void write(final SourceFileWriter writer) throws IOException {
            writer.writeEscaped(text);
        }
    }

    /**
     * Curly brace tokens.
     */
    public enum $BRACE implements Token {
        /** Opening brace ({@code {}). */
        OPEN("{"),
        /** Closing brace ({@code }}). */
        CLOSE("}"),
        ;

        /** The bracket text. */
        private final String text;

        /**
         * Constructs a brace token.
         *
         * @param text the bracket character
         */
        $BRACE(final String text) {
            this.text = text;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void write(final SourceFileWriter writer) throws IOException {
            writer.writeEscaped(text);
        }
    }

    /**
     * Square bracket tokens (for arrays).
     */
    public enum $BRACKET implements Token {
        /** Opening bracket ({@code [}). */
        OPEN("["),
        /** Closing bracket ({@code ]}). */
        CLOSE("]"),
        ;

        /** The bracket text. */
        private final String text;

        /**
         * Constructs a bracket token.
         *
         * @param text the bracket character
         */
        $BRACKET(final String text) {
            this.text = text;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void write(final SourceFileWriter writer) throws IOException {
            writer.writeEscaped(text);
        }
    }

    // ── Keywords ────────────────────────────────────────────────────────

    /**
     * Java keyword and reserved-word tokens.
     * <p>
     * Each keyword has optional {@link Space} rules for spacing before
     * and/or after the keyword.  The {@link #write(SourceFileWriter)}
     * method ensures a space is inserted between adjacent keywords,
     * identifiers, and numeric literals.
     */
    public enum $KW implements Token {
        // standard keywords
        /** {@code abstract}. */
        ABSTRACT,
        /** {@code assert}. */
        ASSERT,
        /** {@code boolean}. */
        BOOLEAN,
        /** {@code break}. */
        BREAK,
        /** {@code byte}. */
        BYTE,
        /** {@code case}. */
        CASE,
        /** {@code catch}. */
        CATCH(Space.BEFORE_KEYWORD_CATCH, null),
        /** {@code char}. */
        CHAR,
        /** {@code class}. */
        CLASS,
        /** {@code continue}. */
        CONTINUE,
        /** {@code default}. */
        DEFAULT,
        /** {@code do}. */
        DO,
        /** {@code double}. */
        DOUBLE,
        /** {@code else}. */
        ELSE(Space.BEFORE_KEYWORD_ELSE, null),
        /** {@code enum}. */
        ENUM,
        /** {@code extends}. */
        EXTENDS,
        /** {@code final}. */
        FINAL,
        /** {@code finally}. */
        FINALLY(Space.BEFORE_KEYWORD_FINALLY, null),
        /** {@code float}. */
        FLOAT,
        /** {@code for}. */
        FOR,
        /** {@code if}. */
        IF,
        /** {@code implements}. */
        IMPLEMENTS,
        /** {@code import}. */
        IMPORT,
        /** {@code instanceof}. */
        INSTANCEOF,
        /** {@code int}. */
        INT,
        /** {@code interface}. */
        INTERFACE,
        /** {@code long}. */
        LONG,
        /** {@code native}. */
        NATIVE,
        /** {@code new}. */
        NEW,
        /** {@code package}. */
        PACKAGE,
        /** {@code private}. */
        PRIVATE,
        /** {@code protected}. */
        PROTECTED,
        /** {@code public}. */
        PUBLIC,
        /** {@code return}. */
        RETURN,
        /** {@code short}. */
        SHORT,
        /** {@code static}. */
        STATIC,
        /** {@code strictfp}. */
        STRICTFP,
        /** {@code super}. */
        SUPER,
        /** {@code switch}. */
        SWITCH,
        /** {@code synchronized}. */
        SYNCHRONIZED,
        /** {@code this}. */
        THIS,
        /** {@code throw}. */
        THROW,
        /** {@code throws}. */
        THROWS,
        /** {@code transient}. */
        TRANSIENT,
        /** {@code try}. */
        TRY,
        /** {@code void}. */
        VOID,
        /** {@code volatile}. */
        VOLATILE,
        /** {@code while}. */
        WHILE(Space.BEFORE_KEYWORD_WHILE, null),

        // literals
        /** {@code false}. */
        FALSE,
        /** {@code true}. */
        TRUE,
        /** {@code null}. */
        NULL,

        // special compound keyword
        /** {@code @interface}. */
        AT_INTERFACE("@interface", null, null),

        // contextual keywords (Java 9+)
        /** {@code module} (Java 9+). */
        MODULE,
        /** {@code requires} (Java 9+). */
        REQUIRES,
        /** {@code exports} (Java 9+). */
        EXPORTS,
        /** {@code opens} (Java 9+). */
        OPENS,
        /** {@code uses} (Java 9+). */
        USES,
        /** {@code provides} (Java 9+). */
        PROVIDES,
        /** {@code with} (Java 9+). */
        WITH,
        /** {@code to} (Java 9+). */
        TO,
        /** {@code transitive} (Java 9+). */
        TRANSITIVE_KW("transitive", null, null),

        // contextual keywords (Java 10+)
        /** {@code var} (Java 10+). */
        VAR,

        // contextual keywords (Java 14+)
        /** {@code yield} (Java 14+). */
        YIELD,

        // contextual keywords (Java 16+)
        /** {@code record} (Java 16+). */
        RECORD,

        // contextual keywords (Java 17+)
        /** {@code sealed} (Java 17+). */
        SEALED,
        /** {@code non-sealed} (Java 17+). */
        NON_SEALED("non-sealed", null, null),
        /** {@code permits} (Java 17+). */
        PERMITS,

        // contextual keywords (Java 21+)
        /** {@code when} (Java 21+). */
        WHEN,
        ;

        /** The keyword text as it appears in source code. */
        private final String keyword;

        /** Optional spacing rule applied before the keyword. */
        private final Space before;

        /** Optional spacing rule applied after the keyword. */
        private final Space after;

        /**
         * Constructs a keyword token using the enum constant name
         * (lowercased) as the keyword text, with no spacing rules.
         */
        $KW() {
            this.keyword = name().toLowerCase(Locale.ROOT);
            this.before = null;
            this.after = null;
        }

        /**
         * Constructs a keyword token using the enum constant name
         * (lowercased) as the keyword text, with optional spacing rules.
         *
         * @param before optional spacing rule before the keyword (may be {@code null})
         * @param after  optional spacing rule after the keyword (may be {@code null})
         */
        $KW(final Space before, final Space after) {
            this.keyword = name().toLowerCase(Locale.ROOT);
            this.before = before;
            this.after = after;
        }

        /**
         * Constructs a keyword token with an explicit keyword text
         * and optional spacing rules.
         *
         * @param keyword the keyword text as it appears in source code
         * @param before  optional spacing rule before the keyword (may be {@code null})
         * @param after   optional spacing rule after the keyword (may be {@code null})
         */
        $KW(final String keyword, final Space before, final Space after) {
            this.keyword = keyword;
            this.before = before;
            this.after = after;
        }

        /**
         * Returns the keyword text.
         *
         * @return the keyword as it appears in source code
         */
        public String keyword() {
            return keyword;
        }

        /**
         * Returns the spacing rule applied before this keyword, if any.
         *
         * @return the before-spacing rule, or {@code null}
         */
        public Space before() {
            return before;
        }

        /**
         * Returns the spacing rule applied after this keyword, if any.
         *
         * @return the after-spacing rule, or {@code null}
         */
        public Space after() {
            return after;
        }

        /**
         * {@inheritDoc}
         * <p>
         * Ensures proper spacing between adjacent keywords, identifiers,
         * and number literals, then writes the keyword text with any
         * configured before/after spacing.
         */
        @Override
        public void write(final SourceFileWriter writer) throws IOException {
            // ensure space between a keyword and adjacent tokens that would
            // otherwise run together (words, numbers, other keywords, closing
            // delimiters, string/char literals)
            final Token prev = writer.getState();
            if (prev == $WORD || prev == $NUMBER || prev == $STRING_LIT
                    || prev instanceof $KW
                    || prev == $PAREN.CLOSE || prev == $ANGLE.CLOSE
                    || prev == $BRACE.CLOSE || prev == $BRACKET.CLOSE) {
                writer.sp();
            }
            if (before != null) {
                writer.write(before);
            }
            writer.writeEscapedWord(keyword);
            if (after != null) {
                writer.write(after);
            }
        }
    }
}
