package io.avaje.prism.internal;

import static io.avaje.prism.internal.ProcessingContext.filer;
import static io.avaje.prism.internal.ProcessingContext.logDebug;
import static io.avaje.prism.internal.ProcessingContext.logError;

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

  static void addProcessor(TypeElement e) {
    services.add(e.getQualifiedName().toString());
  }

  static void write() {

    if (services.isEmpty()) return;

    final String contract = "javax.annotation.processing.Processor";
    // Read the existing service files
    final Filer filer = filer();
    try {
      final FileObject f =
          filer.getResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/services/" + contract);
      final BufferedReader r =
          new BufferedReader(new InputStreamReader(f.openInputStream(), StandardCharsets.UTF_8));
      String line;
      while ((line = r.readLine()) != null) services.add(line);
      r.close();
    } catch (final FileNotFoundException | java.nio.file.NoSuchFileException x) {
      // missing and thus not created yet
    } catch (final IOException x) {
      logError(
          "Failed to load existing service definition file. SPI: " + contract + " exception: " + x);
    }
    try {
      logDebug("Writing META-INF/services/%s", contract);
      final FileObject f =
          filer.createResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/services/" + contract);
      final PrintWriter pw = new PrintWriter(new OutputStreamWriter(f.openOutputStream(), "UTF-8"));

      // Write the service files
      for (final var value : services) {
        pw.println(value);
      }
      pw.close();
    } catch (final IOException x) {
      logError("Failed to write service definition files: %s", x);
    }
  }
}
