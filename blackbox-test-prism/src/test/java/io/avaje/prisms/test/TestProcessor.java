package io.avaje.prisms.test;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;

@SupportedAnnotationTypes("io.avaje.prisms.test.TestAnnotation")
public class TestProcessor extends AbstractProcessor {

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {

    super.init(processingEnv);
    APContext.init(processingEnv);
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latest();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (roundEnv.processingOver()) {
      APContext.clear();
      return false;
    }
    assertThat(APContext.previewEnabled()).isFalse();
    assertThat(APContext.jdkVersion()).isGreaterThanOrEqualTo(11);
    assertThat(APContext.messager()).isNotNull();

    APContext.setProjectModuleElement(annotations, roundEnv);
    assertThat(APContext.getProjectModuleElement()).isNotNull();
    var elements = APContext.elementsAnnotatedWith(roundEnv, "io.avaje.prisms.test.TestAnnotation");

    assert !elements.isEmpty();

    UtypeTests();

    return false;
  }

  private void UtypeTests() {
    var typeUse = APContext.typeElement("io.avaje.prisms.test.context.TypeUse");

    var typeUseFields =
        ElementFilter.fieldsIn(typeUse.getEnclosedElements()).stream()
            .map(Element::asType)
            .map(UType::parse)
            .collect(toList());

    var map = typeUseFields.get(0);
    assertThat(map.full())
        .isEqualTo(
            "@jakarta.validation.constraints.NotEmpty @jakarta.validation.constraints.NotBlank java.util.Map<@jakarta.validation.constraints.NotBlank(groups={io.avaje.prisms.test.context.V4Rusty.Weapon.class}) java.lang.String, @jakarta.validation.constraints.NotEmpty(groups={io.avaje.prisms.test.context.V4Rusty.Weapon.class}) java.util.Map<io.avaje.prisms.test.context.V4Rusty.@jakarta.validation.constraints.NotBlank(groups={io.avaje.prisms.test.context.V4Rusty.Weapon.class}) Weapon, io.avaje.prisms.test.context.@jakarta.validation.constraints.NotBlank V4Rusty>>");

    assertThat(map.shortType())
        .isEqualTo(
            "@NotEmpty @NotBlank Map<@NotBlank(groups={V4Rusty.Weapon.class}) String, @NotEmpty(groups={V4Rusty.Weapon.class}) Map<V4Rusty.@NotBlank(groups={V4Rusty.Weapon.class}) Weapon, io.avaje.prisms.test.context.@NotBlank V4Rusty>>");

    assertThat(map.fullWithoutAnnotations())
        .isEqualTo(
            "java.util.Map<java.lang.String, java.util.Map<io.avaje.prisms.test.context.V4Rusty.Weapon, io.avaje.prisms.test.context.V4Rusty>>");

    assertThat(map.shortWithoutAnnotations())
        .isEqualTo("Map<String, Map<V4Rusty.Weapon, io.avaje.prisms.test.context.V4Rusty>>");

    var list = typeUseFields.get(1);
    assertThat(list.full()).isEqualTo("java.util.List<T>");
    assertThat(list.componentTypes().get(0).componentTypes().get(0).componentTypes().size())
        .isEqualTo(2);

    var methods =
        ElementFilter.methodsIn(typeUse.getEnclosedElements()).stream()
            .map(ExecutableElement::getTypeParameters)
            .flatMap(List::stream)
            .map(Element::asType)
            .map(
                t -> {
                  System.err.println();
                  return UType.parse(t);
                })
            .collect(toList());
  }
}
