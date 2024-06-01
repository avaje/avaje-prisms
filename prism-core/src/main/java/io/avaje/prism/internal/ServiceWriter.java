package io.avaje.prism.internal;

import static io.avaje.prism.internal.APContext.filer;
import static io.avaje.prism.internal.APContext.getModuleInfoReader;
import static io.avaje.prism.internal.APContext.getProjectModuleElement;
import static io.avaje.prism.internal.APContext.logError;
import static io.avaje.prism.internal.APContext.logNote;
import static io.avaje.prism.internal.APContext.logWarn;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

final class ServiceWriter {

  private static final Set<String> services = new HashSet<>();
  private static final String PROCESSOR = "javax.annotation.processing.Processor";

  static void addProcessor(TypeElement e) {
    services.add(e.getQualifiedName().toString());
  }

  static void write() {

    if (services.isEmpty()) return;

    // Read the existing service files
    final Filer filer = filer();
    try (final var f =
            filer
                .getResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/services/" + PROCESSOR)
                .openInputStream();
        final BufferedReader r =
            new BufferedReader(new InputStreamReader(f, StandardCharsets.UTF_8)); ) {

      String line;
      while ((line = r.readLine()) != null) services.add(line);
    } catch (final FileNotFoundException | java.nio.file.NoSuchFileException x) {
      // missing and thus not created yet
    } catch (final IOException x) {
      logError(
          "Failed to load existing service definition file. SPI: "
              + PROCESSOR
              + " exception: "
              + x);
    }
    try {
      logNote("Writing META-INF/services/%s", PROCESSOR);
      final FileObject f =
          filer.createResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/services/" + PROCESSOR);
      final PrintWriter pw = new PrintWriter(new OutputStreamWriter(f.openOutputStream(), "UTF-8"));

      // Write the service files
      for (final var value : services) {
        pw.println(value);
      }
      pw.close();
    } catch (final IOException x) {
      logError("Failed to write service definition files: %s", x);
    }
    validateModules();
  }

  private static void validateModules() {

    var module = getProjectModuleElement();
    if (module != null && !module.isUnnamed()) {
      try (var reader = getModuleInfoReader(); ) {

        String line;
        while ((line = reader.readLine()) != null) {

          if (line.isBlank()) {
            if (line.contains("io.avaje.prism") && !line.contains("static")) {
              logWarn(
                  module, "`requires io.avaje.prism` should be `requires static io.avaje.prism;`");
            }
            if (line.contains("io.avaje.prism.core")) {
              logWarn(module, "io.avaje.prism.core should be not be used directly");
            }
            continue;
          }
        }

      } catch (Exception e) {
        // can't read module
      }
    }
    services.clear();
  }
}
