package io.avaje.prism.internal;

import static io.avaje.prism.internal.ProcessingContext.filer;
import static io.avaje.prism.internal.ProcessingContext.getProjectModuleElement;
import static io.avaje.prism.internal.ProcessingContext.logDebug;
import static io.avaje.prism.internal.ProcessingContext.logError;
import static io.avaje.prism.internal.ProcessingContext.logWarn;
import static java.util.stream.Collectors.toSet;

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
  private static final Set<String> foundServiceImpls = new HashSet<>();

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
      logDebug("Writing META-INF/services/%s", PROCESSOR);
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
      final Set<String> missingServiceImpls = services.stream().map(Util::shortName).collect(toSet());

      try (var reader = getModuleInfoReader(); ) {

        boolean inProvides = false;
        String line;
        while ((line = reader.readLine()) != null) {

          if (line.contains("provides") && line.contains("Processor")) {
            inProvides = true;
          }

          if (inProvides) {
            processLine(line, missingServiceImpls);
          }

          if (!inProvides || line.isBlank()) {
            if (line.contains("io.avaje.prism") && !line.contains("static")) {
              logWarn(
                  module, "`requires io.avaje.prism` should be `requires static io.avaje.prism;`");
            }
            continue;
          }

          if (line.contains(";")) {
            break;
          }
        }
        if (!missingServiceImpls.isEmpty()) {
          logError(
              module, "Missing `provides %s with %s;`", PROCESSOR, String.join(", ", services));
        }

      } catch (Exception e) {
        // can't read module
      }
    }

    services.clear();
    foundServiceImpls.clear();
  }

  private static BufferedReader getModuleInfoReader() throws IOException {
    var inputStream =
        filer()
            .getResource(StandardLocation.SOURCE_PATH, "", "module-info.java")
            .toUri()
            .toURL()
            .openStream();
    return new BufferedReader(new InputStreamReader(inputStream));
  }

  private static void processLine(String line, Set<String> missingServices) {

    if (!foundServiceImpls.containsAll(missingServices)) {
      parseServices(line, missingServices);
    }
    missingServices.removeAll(foundServiceImpls);
  }

  private static void parseServices(String input, Set<String> missingServices) {

    for (var str : missingServices) {
      if (input.contains(str)) {
        foundServiceImpls.add(str);
      }
    }
  }
}
