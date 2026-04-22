module org.jboss.jdeparser.test {
    requires org.jboss.jdeparser;
    requires org.junit.jupiter.api;

    opens org.jboss.jdeparser.test to org.junit.platform.commons;
}
