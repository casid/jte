---
title: jte-extension-api
description: A jte module that defines the interface to add extensions to jte generation. 
---

# jte-extension-api

This module defines the interface to add extensions to jte generation. Extensions can generate additional files based on metadata about jte templates.

## Writing an Extension

Extension authors must implement the `gg.jte.extension.api.JteExtension` interface.
The implementing class must have a no-argument constructor so that it can be instantiated by name.

## Testing

The companion module `jte-extension-api-mocks` provides mock implementations of the API interfaces to help with writing unit tests.

## Usage

The jte Maven and Gradle plugins allow configuring extensions.

## Examples

* [jte-models](jte-models.md) module is an extension that generates typesafe facades for templates.
* [test/jte-runtime-cp-test-models](https://github.com/casid/jte/blob/{{ latest-git-tag }}/test/jte-runtime-cp-test-models) uses the Maven plugin to apply the jte-models extension.
* [test/jte-runtime-cp-test-models-gradle](https://github.com/casid/jte/blob/{{ latest-git-tag }}/test/jte-runtime-cp-test-models-gradle) uses the Gradle plugin to apply the jte-models extension.