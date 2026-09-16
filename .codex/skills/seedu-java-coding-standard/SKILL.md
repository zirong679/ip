---
name: seedu-java-coding-standard
description: Apply the SE-EDUCATION basic and intermediate Java coding standard to Java code in this project.
---

# Seedu Java Coding Standard

Apply the SE-EDUCATION [Java coding standard (basic + intermediate)](https://se-education.org/guides/conventions/java/intermediate.html) to every Java file in this repository, including tests. For topics not covered by that guide, follow the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html).

- Use lowercase package names; English PascalCase nouns for classes/enums; camelCase variables and verb-based methods; and SCREAMING_SNAKE_CASE constants. Boolean names should read as predicates and collection names should be plural.
- Use four spaces, K&R braces, spaces around operators/keywords/commas, one logical unit per blank line, and a hard line limit of 120 characters. Wrap long lines with eight extra spaces of indentation.
- Put every class in a package. Keep imports explicit and consistently ordered. Attach array brackets to the type. Initialize variables at declaration and use the smallest practical scope.
- Keep mutable class fields non-public; expose behavior through methods. Always brace loop and conditional bodies, put conditional bodies on separate lines, and mark intentional switch fallthrough with `// Fallthrough`.
- Add descriptive English-American Javadocs to public classes and public methods, except getters/setters, exact overrides, and test code. Start method summaries with an action such as `Returns`, `Adds`, or `Sets`.

Before finishing a Java change, inspect touched files for these violations and run relevant tests after any behavior-preserving refactor.
