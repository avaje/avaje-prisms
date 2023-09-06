package io.avaje.prisms.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;

import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import io.avaje.prism.internal.PrismGenerator;

class APContextTest {

  @Test
  void runAnnotationProcessor() throws Exception {
    final var source = Paths.get("src").toAbsolutePath().toString();

    final var files = getSourceFiles(source);

    final var compiler = ToolProvider.getSystemJavaCompiler();

    final var task =
        compiler.getTask(new PrintWriter(System.out), null, null, Arrays.asList(), null, files);
    task.setProcessors(Arrays.asList(new TestProcessor()));

    assertThat(task.call()).isTrue();
  }

  private Iterable<JavaFileObject> getSourceFiles(String source) throws Exception {
    final var compiler = ToolProvider.getSystemJavaCompiler();
    final var files = compiler.getStandardFileManager(null, null, null);

    files.setLocation(StandardLocation.SOURCE_PATH, Arrays.asList(new File(source)));

    final Set<Kind> fileKinds = Collections.singleton(Kind.SOURCE);
    return files.list(StandardLocation.SOURCE_PATH, "", fileKinds, true);
  }
}
