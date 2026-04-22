# JDeparser 3

A Java source code generation library using the borrow pattern.

## Build

```
mvn clean verify
```

Requires JDK 25+, compiles with `--release 17`.

## Architecture

- `org.jboss.jdeparser` — public API: entry point (`JDeparser`), core types (`JType`, `JExpr`, `JVar`, `JExprs`, `JTypes`), source management (`JSources`), version control (`SourceVersion`, `LanguageFeature`)
- `org.jboss.jdeparser.creator` — borrow-pattern API interfaces (`BlockCreator`, `ClassCreator`, `MethodCreator`, etc.)
- `org.jboss.jdeparser.format` — public formatting configuration (`FormatPreferences`, `JFiler`)
- `org.jboss.jdeparser.impl` — internal implementations (not exported from module)

## Design Principles

- Borrow pattern (consumer/callback API) for all structural constructs
- Expressions are composable values; structural constructs use `Consumer<XxxCreator>` callbacks
- Version-gated feature validation via `SourceVersion.supports(LanguageFeature)`
- Formatting engine adapted from jdeparser2
