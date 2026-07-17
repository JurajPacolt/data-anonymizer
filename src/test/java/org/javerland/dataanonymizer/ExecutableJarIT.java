package org.javerland.dataanonymizer;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExecutableJarIT {

    @Test
    void packagedJarIsExecutableAndContainsJdbcDrivers() throws Exception {
        Path jar = Path.of(System.getProperty("project.build.directory"),
                System.getProperty("project.build.finalName") + ".jar");
        String javaExecutable = Path.of(System.getProperty("java.home"), "bin",
                System.getProperty("os.name").toLowerCase().contains("win") ? "java.exe" : "java").toString();

        Process process = new ProcessBuilder(javaExecutable, "-jar", jar.toString(), "--version")
                .redirectErrorStream(true).start();
        assertTrue(process.waitFor(Duration.ofSeconds(20).toMillis(), TimeUnit.MILLISECONDS));
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        assertEquals(0, process.exitValue(), output);
        assertTrue(output.contains(System.getProperty("project.version")), output);

        try (JarFile jarFile = new JarFile(jar.toFile())) {
            assertEquals("org.javerland.dataanonymizer.App",
                    jarFile.getManifest().getMainAttributes().getValue("Main-Class"));
            assertNotNull(jarFile.getEntry("org/postgresql/Driver.class"));
            assertNotNull(jarFile.getEntry("com/mysql/cj/jdbc/Driver.class"));
        }
    }
}
