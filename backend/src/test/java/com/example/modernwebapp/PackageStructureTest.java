package com.example.modernwebapp;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class PackageStructureTest {

    @Test
    void requiredBaselinePackagesExist() {
        Path basePath = Path.of("src/main/java/com/example/modernwebapp");
        List<String> requiredPackages = List.of(
                "controller",
                "service",
                "repository",
                "entity",
                "dto",
                "config",
                "exception",
                "security");

        requiredPackages.forEach(packageName ->
                assertThat(Files.isDirectory(basePath.resolve(packageName)))
                        .as("Expected package directory %s to exist", packageName)
                        .isTrue());
    }
}
