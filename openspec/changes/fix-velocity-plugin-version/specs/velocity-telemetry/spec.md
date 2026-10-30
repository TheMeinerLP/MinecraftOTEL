## ADDED Requirements

### Requirement: Instrumentation version matches the plugin
The shaded jar SHALL declare the project version as `Implementation-Version` in its manifest.

#### Scenario: Release build
- **WHEN** the shaded jar is built
- **THEN** its manifest contains the project version as `Implementation-Version`
