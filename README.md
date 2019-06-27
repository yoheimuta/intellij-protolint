# Protobuf Lint Support for JetBrains IDEs
[![Build Status](https://travis-ci.org/yoheimuta/intellij-protolint.svg?branch=master)](https://travis-ci.org/yoheimuta/intellij-protolint)

[Protocol Buffer Linter Plugin](https://plugins.jetbrains.com/plugin/12641-protocol-buffer-linter) for IntelliJ IDEA & other JetBrains products.

The latest plugin release is compatible with IntelliJ IDEA 2018.3.
Other JetBrains IDEs of the same or higher version should be supported as well.

Compatibility Matrix:

| Plugin Version  | IDE Version Range  |
|-----------------|--------------------|
| 0.1.0           | IDEA 2018.3        |


### Installation

You can install the plugin by opening "Plugins" settings, "Marketplace" - search for "Protocol Buffer Linter".

#### Dependencies

- [protolint](https://github.com/yoheimuta/protolint) must be installed.
- [protobuf-jetbrains-plugin](https://github.com/protostuff/protobuf-jetbrains-plugin) must be installed.

### Configuration

The plugin does not require configuration by default, for the majority of projects it should work out of the box.

#### Path

The plugin refers to the `protolint` executable in your PATH by default.
You can configure the path to the `protolint` executable and its config directory through `Preferences -> Tools -> Protocol Buffer Linter`.

### Development

#### Build

```
./gradlew build
```

#### Run IntelliJ IDEA with enabled plugin

```
./gradlew runide
```
