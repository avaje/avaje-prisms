package io.avaje.prisms.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import io.avaje.prism.internal.PrismGenerator;

public class PrismProcessorTest {

  @AfterEach
  void deleteGeneratedFiles() throws IOException {

    Files.walk(Paths.get("io").toAbsolutePath())
        .sorted(Comparator.reverseOrder())
        .map(Path::toFile)
        .forEach(File::delete);
  }

  @Test
  public void runAnnotationProcessorJsonB() throws Exception {
    final String source = Paths.get("src").toAbsolutePath().toString();

    final Iterable files = getSourceFiles(source);

    final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

    final CompilationTask task =
        compiler.getTask(
            new PrintWriter(System.out), null, null, List.of("--release=11"), null, files);
    task.setProcessors(List.of(new PrismGenerator()));

    assertThat(task.call()).isTrue();
    assert Files.readString(
            Paths.get("org/example/myapp/web/BarController$Route.java").toAbsolutePath())
        .contains("io.avaje.jsonb.Jsonb");
  }

  private Iterable<JavaFileObject> getSourceFiles(String source) throws Exception {
    final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    final StandardJavaFileManager files = compiler.getStandardFileManager(null, null, null);

    files.setLocation(StandardLocation.SOURCE_PATH, List.of(new File(source)));

    final Set<Kind> fileKinds = Collections.singleton(Kind.SOURCE);
    return files.list(StandardLocation.SOURCE_PATH, "", fileKinds, true);
  }
}
