module io.smallrye.jdeparser.test {
    requires io.smallrye.jdeparser;
    requires org.junit.jupiter.api;
    requires org.mockito;
    requires java.compiler;

    opens io.smallrye.jdeparser.test to org.junit.platform.commons;
}
