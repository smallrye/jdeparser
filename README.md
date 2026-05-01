[![Maven Central](https://img.shields.io/maven-central/v/io.smallrye.jdeparser/jdeparser?color=green)](https://search.maven.org/search?q=a:jdeparser)
[![License](https://img.shields.io/github/license/smallrye/jdeparser)](http://www.apache.org/licenses/LICENSE-2.0)

# SmallRye JDeparser

A Java source code generation library.
Rather than concatenating strings or manipulating template files, JDeparser provides a type-safe API for constructing syntactically correct Java source code.

JDeparser is designed for two primary use cases:

* **Annotation processors** that generate source files during compilation
* **Build-time code generators** that produce Java source as part of a build pipeline

## Getting Started

Add the following dependency to your project:

```xml
<dependency>
    <groupId>io.smallrye.jdeparser</groupId>
    <artifactId>jdeparser</artifactId>
    <version>VERSION</version>
</dependency>
```

Replace `VERSION` with the latest release version.

For detailed usage instructions and API documentation, see the [User Manual](MANUAL.adoc).

## Build

Building this project requires JDK 25 or later:

```bash
mvn clean verify
```

## License

This project is licensed under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0).
