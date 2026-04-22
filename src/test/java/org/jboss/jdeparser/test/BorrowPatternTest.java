package org.jboss.jdeparser.test;

import org.jboss.jdeparser.JExpr;
import org.jboss.jdeparser.JType;
import org.jboss.jdeparser.JSources;
import org.jboss.jdeparser.SourceVersion;
import org.jboss.jdeparser.impl.BlockCreatorImpl;
import org.jboss.jdeparser.impl.ClassCreatorImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for the borrow pattern state machine: verifying that finished creators
 * throw on reuse, and that nested callbacks deactivate the parent.
 */
class BorrowPatternTest {

    /**
     * Verifies that calling methods on a finished block creator throws.
     */
    @Test
    void finishedBlockThrows() {
        final BlockCreatorImpl block = new BlockCreatorImpl(SourceVersion.JAVA_17);
        assertDoesNotThrow(() -> block.emit(JExpr.ZERO));
        block.finish();
        assertThrows(IllegalStateException.class, () -> block.emit(JExpr.ONE),
            "finished block should reject new statements");
    }

    /**
     * Verifies that calling methods on a finished class creator throws.
     */
    @Test
    void finishedClassThrows() {
        final ClassCreatorImpl cc = new ClassCreatorImpl(SourceVersion.JAVA_17, "Test", false);
        assertDoesNotThrow(() -> cc.public_());
        cc.finish();
        assertThrows(IllegalStateException.class, () -> cc.public_(),
            "finished class should reject modifications");
    }

    /**
     * Verifies that the parent creator is deactivated during a nested callback
     * and reactivated afterward.
     */
    @Test
    void nestedCallbackDeactivatesParent() {
        final ClassCreatorImpl cc = new ClassCreatorImpl(SourceVersion.JAVA_17, "Test", false);
        cc.method("m", mc -> {
            // parent (cc) should be inactive during this callback
            assertThrows(IllegalStateException.class, () -> cc.public_(),
                "parent should be inactive during nested callback");

            mc.body(b -> {
                // mc should be inactive during body callback
                assertThrows(IllegalStateException.class, () -> mc.returning(JType.INT),
                    "method should be inactive during nested body callback");
                b.return_(JExpr.ZERO);
            });
        });
        // parent should be active again after nest completes
        assertDoesNotThrow(() -> cc.final_(),
            "parent should be active again after nested callback");
    }
}
