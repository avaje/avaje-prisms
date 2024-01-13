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
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.ElementFilter;
import javax.management.relation.InvalidRelationIdException;

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

    utypeTests();

    return false;
  }

  private void utypeTests() {
    var testElement = APContext.typeElement("io.avaje.prisms.test.context.UTypeTester");

    intersectionTypes(testElement);

    var methods = ElementFilter.methodsIn(testElement.getEnclosedElements());
    var fields =
        ElementFilter.fieldsIn(testElement.getEnclosedElements()).stream()
            .map(Element::asType)
            .map(UType::parse)
            .collect(toList());
    typeUseTests(fields);

    var testVoid = methods.get(0);
    var arrayParam = UType.parse(testVoid.getParameters().get(0).asType());
    assertThat(arrayParam.kind()).isEqualTo(TypeKind.ARRAY);
    assertThat(arrayParam.full())
        .isEqualTo("@jakarta.validation.constraints.NotEmpty(groups={int[].class}) int[]");
    assertThat(arrayParam.mainType()).isEqualTo("int[]");
    var arrayType = arrayParam.param0();
    assertThat(arrayType.shortWithoutAnnotations()).isEqualTo("int");
    assertThat(arrayType.kind()).isEqualTo(TypeKind.INT);

    var voidType = UType.parse(testVoid.getReturnType());
    assertThat(voidType.kind()).isEqualTo(TypeKind.VOID);
    assertThat(voidType.full()).isEqualTo("void");
    assertThat(voidType.mainType()).isEqualTo("void");
  }

  private void typeUseTests(List<UType> typeUseFields) {

    var map = typeUseFields.get(0);

    assertThat(map.importTypes())
        .contains(
            "jakarta.validation.constraints.NotBlank",
            "java.util.Map",
            "jakarta.validation.constraints.NotEmpty",
            "io.avaje.prisms.test.context.V4Rusty");

    assertThat(map.mainType()).isEqualTo("java.util.Map");

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
    assertThat(list.componentTypes().get(0).componentTypes().get(0).componentTypes()).hasSize(2);
  }

  private void intersectionTypes(TypeElement typeUse) {
    var typeUseUType = UType.parse(typeUse.asType());

    assertThat(typeUseUType).isEqualTo(UType.parse(typeUse.asType()));
    assertThat(typeUseUType.kind()).isEqualTo(TypeKind.DECLARED);
    assertThat(typeUseUType.full()).isEqualTo("io.avaje.prisms.test.context.UTypeTester<T>");
    assertThat(typeUseUType.shortType()).isEqualTo("UTypeTester<T>");
    assertThat(typeUseUType.mainType()).isEqualTo("io.avaje.prisms.test.context.UTypeTester");

    final var typeVar = typeUseUType.param0();
    assertThat(typeVar.mainType()).isEqualTo("T");

    assertThat(typeVar.kind()).isEqualTo(TypeKind.TYPEVAR);
    assertThat(typeVar.componentTypes()).hasSize(1);
    var intersection = typeVar.param0();
    assertThat(intersection.mainType()).isNull();

    assertThat(intersection.kind()).isEqualTo(TypeKind.INTERSECTION);

    assertThat(intersection.param0().toString()).isEqualTo("io.avaje.prisms.test.CommonInterface");
    assertThat(intersection.param1().full()).isEqualTo("io.avaje.prisms.test.CommonInterface2");
  }
}
