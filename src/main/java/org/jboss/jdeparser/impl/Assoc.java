package org.jboss.jdeparser.impl;

/**
 * Operator associativity for expression nodes.
 * <p>
 * Used in conjunction with {@link Prec} to determine the minimal set
 * of parentheses needed during source generation.
 */
public enum Assoc {
    /** Left-to-right associativity (most binary operators). */
    LEFT,
    /** Right-to-left associativity (assignment, ternary). */
    RIGHT,
    /** Non-associative (comparison, instanceof). */
    NONE,
    ;
}
