/**
 * JDeparser 3: a Java source code generation library.
 * <p>
 * This module provides a programmatic API for generating syntactically correct
 * Java source files, supporting all Java language constructs
 * with configurable source version validation.
 */
module org.jboss.jdeparser {
    requires java.compiler;

    exports org.jboss.jdeparser;
    exports org.jboss.jdeparser.creator;
    exports org.jboss.jdeparser.format;
    exports org.jboss.jdeparser.impl to org.jboss.jdeparser.test;
}
