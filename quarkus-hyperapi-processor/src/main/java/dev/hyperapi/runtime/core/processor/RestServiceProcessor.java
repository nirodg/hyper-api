package dev.hyperapi.runtime.core.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;
import dev.hyperapi.runtime.core.processor.annotations.RestService;
import jakarta.annotation.Generated;
import jakarta.ws.rs.*;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
@SupportedAnnotationTypes("dev.hyperapi.runtime.core.processor.annotations.RestService")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class RestServiceProcessor extends AbstractProcessor {

  private Filer filer;
  private Messager messager;
  private Elements elementUtils;

  @Override
  public synchronized void init(ProcessingEnvironment env) {
    super.init(env);
    filer = env.getFiler();
    messager = env.getMessager();
    elementUtils = env.getElementUtils();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(RestService.class)) {
      if (!(annotatedElement instanceof TypeElement entityType)) {
        error(annotatedElement, "@RestService must annotate a class.");
        continue;
      }

      // 2. Check BaseEntity inheritance
      if (!isExtendingBaseEntity(entityType)) {
        error(
            entityType,
            "Class %s must extend dev.hyperapi.runtime.core.entity.BaseEntity to use @RestService",
            entityType.getSimpleName().toString());
        return false;
      }

      RestService restService = entityType.getAnnotation(RestService.class);
      String dtoName = sanitizeDtoName(entityType.getSimpleName().toString(), restService.dto());
      List<String> ignoredFields = Arrays.asList(restService.mapping().ignore());
      List<String> ignoredNestedFields = Arrays.asList(restService.mapping().ignoreNested());

      boolean shouldGenerate = !dtoName.isBlank() || !ignoredFields.isEmpty();
      if (!shouldGenerate) {
        info(entityType, "Skipping generation for " + entityType.getSimpleName());
        continue;
      }

      try {
        generateDTO(entityType, dtoName, ignoredFields);
        generateMapper(entityType, dtoName, ignoredFields, ignoredNestedFields);
        generateService(entityType, dtoName, restService);
        generateController(entityType, dtoName, restService);
      } catch (IOException | ClassNotFoundException e) {
        error(entityType, "Code generation failed: " + e.getMessage());
      }
    }
    return true;
  }

  private boolean isExtendingBaseEntity(TypeElement typeElement) {
    // 1. Get BaseEntity type
    TypeElement baseEntityType =
        elementUtils.getTypeElement("dev.hyperapi.runtime.core.model.BaseEntity");

    if (baseEntityType == null) {
      error(typeElement, "Could not resolve BaseEntity class in classpath");
      return false;
    }

    // 2. Check inheritance
    return typeElement
        .getSuperclass()
        .toString()
        .equals(baseEntityType.getQualifiedName().toString());
  }

  private void generateMapper(
      TypeElement entity, String dtoName, List<String> ignore, List<String> ignoreNested)
      throws IOException {
    String entityName = entity.getSimpleName().toString();
    String basePackage = elementUtils.getPackageOf(entity).getQualifiedName().toString();
    String mapperName = entityName + "Mapper";

    ClassName dtoClass = ClassName.get(basePackage + ".dto", dtoName);
    ClassName entityClass = ClassName.get(basePackage, entityName);

    TypeName superType =
        ParameterizedTypeName.get(
            ClassName.get("dev.hyperapi.runtime.core.mapper", "AbstractMapper"),
            dtoClass,
            entityClass);

    // Build @Mapping annotations for both directions
    List<AnnotationSpec> mappingAnnotations = new ArrayList<>();

    for (String nested : ignoreNested) {
      mappingAnnotations.add(
          AnnotationSpec.builder(ClassName.get("org.mapstruct", "Mapping"))
              .addMember("target", "$S", nested)
              .addMember("ignore", "true")
              .build());
    }

    // toEntity method
    MethodSpec.Builder toEntityBuilder =
        MethodSpec.methodBuilder("toEntity")
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .returns(entityClass)
            .addParameter(dtoClass, "dto");
    mappingAnnotations.forEach(toEntityBuilder::addAnnotation);

    // toDto method
    MethodSpec.Builder toDtoBuilder =
        MethodSpec.methodBuilder("toDto")
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .returns(dtoClass)
            .addParameter(entityClass, "entity");
    mappingAnnotations.forEach(toDtoBuilder::addAnnotation);

    // Abstract class builder
    TypeSpec mapperClass =
        TypeSpec.classBuilder(mapperName)
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addAnnotation(generatedAnnotation())
            .addAnnotation(
                AnnotationSpec.builder(ClassName.get("org.mapstruct", "Mapper"))
                    .addMember("componentModel", "$S", "cdi")
                    .build())
            .superclass(superType)
            .addMethod(toEntityBuilder.build())
            .addMethod(toDtoBuilder.build())
            .build();

    JavaFile.builder(basePackage + ".mapper", mapperClass).indent("    ").build().writeTo(filer);
  }

  private void generateDTO(TypeElement entity, String dtoName, List<String> ignore)
      throws IOException, ClassNotFoundException {
    String basePackage = elementUtils.getPackageOf(entity).getQualifiedName().toString();
    ClassName dtoClass = ClassName.get(basePackage + ".dto", dtoName);
    ClassName baseDtoClass = ClassName.get("dev.hyperapi.runtime.core.dto", "BaseDTO");

    // Initialize builder with common configurations
    TypeSpec.Builder dtoBuilder =
        TypeSpec.classBuilder(dtoClass)
            .addModifiers(Modifier.PUBLIC)
            .superclass(baseDtoClass)
            .addAnnotation(generatedAnnotation())
            .addAnnotation(
                AnnotationSpec.builder(
                        ClassName.get("com.fasterxml.jackson.annotation", "JsonInclude"))
                    .addMember(
                        "value",
                        "$T.Include.NON_NULL",
                        ClassName.get("com.fasterxml.jackson.annotation", "JsonInclude"))
                    .build());

    // Track fields for potential builder pattern
    List<Element> allFields = new ArrayList<>();

    // Process all fields
    for (Element field : entity.getEnclosedElements()) {
      if (field.getKind() == ElementKind.FIELD
          && !ignore.contains(field.getSimpleName().toString())) {
        String fieldName = field.getSimpleName().toString();
        TypeMirror fieldType = field.asType();
        TypeName fieldTypeName = TypeName.get(fieldType);

        // Add the field with Jackson annotations
        FieldSpec.Builder fieldBuilder =
            FieldSpec.builder(fieldTypeName, fieldName, Modifier.PRIVATE)
                .addAnnotation(
                    AnnotationSpec.builder(
                            ClassName.get("com.fasterxml.jackson.annotation", "JsonProperty"))
                        .addMember("value", "$S", fieldName)
                        .build());

        // Handle initialization for collections
        if (PropertyGenerator.isCollectionType(fieldType)) {
          fieldBuilder.initializer("new $T<>()", getCollectionImplType(fieldType));
        }

        dtoBuilder.addField(fieldBuilder.build());
        allFields.add(field);

        // Generate all appropriate methods
        PropertyGenerator.addPropertyMethods(dtoBuilder, field);
      }
    }

    if (allFields.isEmpty()) {
      String wanrMessage =
          "%s : The class doesn't contain any fields to generate DTO for. "
              + "Ensure it has fields annotated with @RestService or not ignored by mapping.";
      warn(entity, wanrMessage, entity.getQualifiedName().toString());
    }

    // Add toString(), equals() and hashCode()
    addCommonMethods(dtoBuilder, allFields, dtoName, entity.getSimpleName().toString());

    // Add builder pattern if needed
    // FIXME the mapper wont create proper toDto method
    //        if (generateBuilder) {
    //        PropertyGenerator.addBuilderSupport(dtoBuilder, allFields, dtoClass.simpleName());
    //        }

    // Write the final DTO class
    JavaFile.builder(dtoClass.packageName(), dtoBuilder.build())
        .indent("    ")
        .build()
        .writeTo(filer);
  }

  // Helper to get proper collection implementation type
  private static TypeName getCollectionImplType(TypeMirror collectionType) {
    if (collectionType.toString().startsWith("java.util.List")) {
      return ClassName.get(ArrayList.class);
    } else if (collectionType.toString().startsWith("java.util.Set")) {
      return ClassName.get(HashSet.class);
    } else if (collectionType.toString().startsWith("java.util.Map")) {
      return ClassName.get(HashMap.class);
    }
    return ClassName.get(Object.class);
  }

  // Add common methods like toString, equals, hashCode
  private void addCommonMethods(
      TypeSpec.Builder builder, List<Element> fields, String className, String originalName)
      throws ClassNotFoundException {
    ClassName objectsClass = ClassName.get("java.util", "Objects");

    List<String> baseDtoFields = getBaseDtoFields();
    List<String> allFieldNames = new ArrayList<>();

    // 2. Add entity fields
    fields.stream().map(f -> f.getSimpleName().toString()).forEach(allFieldNames::add);

    generateCommonMethods(builder, fields, className, objectsClass);
  }

  private List<String> getBaseDtoFields() throws ClassNotFoundException {
    List<String> fieldNames = new ArrayList<>();

    // Load BaseDTO class using element utils
    TypeElement baseDtoType = elementUtils.getTypeElement("dev.hyperapi.runtime.core.dto.BaseDTO");
    if (baseDtoType != null) {
      for (Element enclosed : elementUtils.getAllMembers(baseDtoType)) {
        if (enclosed.getKind() == ElementKind.FIELD) {
          fieldNames.add(enclosed.getSimpleName().toString());
        }
      }
    } else {
      throw new ClassNotFoundException("BaseDTO class not found in classpath");
    }

    return fieldNames;
  }

  private void generateCommonMethods(
      TypeSpec.Builder builder, List<Element> fields, String className, ClassName objectsClass) {
    // Generate toString()
    if (!fields.isEmpty()) {
      StringBuilder toStringFormat = new StringBuilder(className + " [");
      List<Object> toStringArgs = new ArrayList<>();
      for (int i = 0; i < fields.size(); i++) {
        String fieldName = fields.get(i).getSimpleName().toString();
        if (i > 0) {
          toStringFormat.append(", ");
        }
        toStringFormat.append(fieldName).append("=%s");
        toStringArgs.add(fieldName);
      }
      toStringFormat.append("]");

      // Create the format arguments string
      String formatArgs =
          toStringArgs.stream().map(Object::toString).collect(Collectors.joining(", "));

      MethodSpec toString =
          MethodSpec.methodBuilder("toString")
              .addModifiers(Modifier.PUBLIC)
              .returns(String.class)
              .addAnnotation(Override.class)
              .addStatement("return String.format($S, $L)", toStringFormat.toString(), formatArgs)
              .build();
      builder.addMethod(toString);
    }

    // Generate equals()
    if (!fields.isEmpty()) {
      ClassName dtoClassName = ClassName.bestGuess(className);

      MethodSpec.Builder equalsBuilder =
          MethodSpec.methodBuilder("equals")
              .addModifiers(Modifier.PUBLIC)
              .returns(boolean.class)
              .addAnnotation(Override.class)
              .addParameter(Object.class, "o")
              .beginControlFlow("if (this == o)")
              .addStatement("return true")
              .endControlFlow()
              .beginControlFlow("if (o == null || getClass() != o.getClass())")
              .addStatement("return false")
              .endControlFlow()
              .addStatement("$T that = ($T) o", dtoClassName, dtoClassName);

      // Build the equality comparison chain
      CodeBlock.Builder comparisonBuilder = CodeBlock.builder();
      for (Element field : fields) {
        String fieldName = field.getSimpleName().toString();
        if (comparisonBuilder.isEmpty()) {
          comparisonBuilder.add("$T.equals(this.$L, that.$L)", objectsClass, fieldName, fieldName);
        } else {
          comparisonBuilder.add(
              " &&\n    $T.equals(this.$L, that.$L)", objectsClass, fieldName, fieldName);
        }
      }

      equalsBuilder.addStatement("return $L", comparisonBuilder.build());
      builder.addMethod(equalsBuilder.build());
    }

    // Generate hashCode()
    if (!fields.isEmpty()) {
      String hashFields =
          fields.stream().map(f -> f.getSimpleName().toString()).collect(Collectors.joining(", "));

      MethodSpec hashCode =
          MethodSpec.methodBuilder("hashCode")
              .addModifiers(Modifier.PUBLIC)
              .returns(int.class)
              .addAnnotation(Override.class)
              .addStatement("return $T.hash($L)", objectsClass, hashFields)
              .build();

      builder.addMethod(hashCode);
    }
  }

  private MethodSpec generateCreateOverride(
      ClassName dtoClass, ClassName entityEventClass, boolean customEmitter) {

    String strCustomEmitter = customEmitter ? "emitter.emit" : "fireEvent";
    return MethodSpec.methodBuilder("create")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .returns(dtoClass)
        .addParameter(dtoClass, "dto")
        .addStatement("$T result = super.create(dto)", dtoClass)
        .addStatement(
            strCustomEmitter + "($T.Type.CREATE, mapper.toEntity(result))", entityEventClass)
        .addStatement("return result")
        .build();
  }

  private MethodSpec generateUpdateOverride(
      ClassName dtoClass, ClassName entityEventClass, boolean customEmitter) {
    String strCustomEmitter = customEmitter ? "emitter.emit" : "fireEvent";

    return MethodSpec.methodBuilder("update")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .returns(dtoClass)
        .addParameter(dtoClass, "dto")
        .addStatement("$T result = super.update(dto)", dtoClass)
        .addStatement(
            strCustomEmitter + "($T.Type.UPDATE, mapper.toEntity(result))", entityEventClass)
        .addStatement("return result")
        .build();
  }

  private MethodSpec generateDeleteOverride(ClassName entityEventClass, boolean customEmitter) {
    String strCustomEmitter = customEmitter ? "emitter.emit" : "fireEvent";

    return MethodSpec.methodBuilder("delete")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .returns(TypeName.VOID)
        .addParameter(TypeName.OBJECT, "id")
        .addStatement("super.delete(id)")
        .addStatement(
            strCustomEmitter + "($T.Type.DELETE, null)", entityEventClass) // if you don’t re-fetch
        .build();
  }

  private MethodSpec generatePatchOverride(
      ClassName dtoClass, ClassName entityEventClass, boolean customEmitter) {
    String strCustomEmitter = customEmitter ? "emitter.emit" : "fireEvent";

    return MethodSpec.methodBuilder("patch")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .returns(dtoClass)
        .addParameter(ParameterSpec.builder(ClassName.get(String.class), "id").build())
        .addParameter(
            ParameterSpec.builder(ClassName.get("jakarta.json", "JsonObject"), "patchJson").build())
        .addStatement("$T result = super.patch(id, patchJson)", dtoClass)
        .addStatement(strCustomEmitter + "($T.Type.UPDATE, mapper.toEntity(result))", entityEventClass)
        .addStatement("return result")
        .build();
  }

  private void generateService(TypeElement entity, String dtoName, RestService restService)
      throws IOException, ClassNotFoundException {
    String entityName = entity.getSimpleName().toString();
    String basePackage = elementUtils.getPackageOf(entity).getQualifiedName().toString();
    String serviceName = entityName + "Service";

    ClassName dtoClass = ClassName.get(basePackage + ".dto", dtoName);
    ClassName entityClass = ClassName.get(basePackage, entityName);
    ClassName mapperClass = ClassName.get(basePackage + ".mapper", entityName + "Mapper");

    RestService.Events events = restService.events();
    boolean fireOnCreate = events.onCreate();
    boolean fireOnUpdate = events.onUpdate();
    boolean fireOnDelete = events.onDelete();
    boolean fireOnPatch = events.onDelete();

    TypeName superType =
        ParameterizedTypeName.get(
            ClassName.get("dev.hyperapi.runtime.core.service", "BaseEntityService"),
            entityClass,
            dtoClass,
            mapperClass);

    MethodSpec.Builder constructor =
        MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC)
            .addStatement("super($T.class, $T.class)", entityClass, dtoClass);

    TypeSpec.Builder serviceClass =
        TypeSpec.classBuilder(serviceName)
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(generatedAnnotation())
            .addAnnotation(ClassName.get("jakarta.enterprise.context", "ApplicationScoped"))
            .superclass(superType);

    Optional<TypeMirror> emitterMirror = getEmitterTypeMirror(entity);

    if (fireOnCreate) {
      MethodSpec method =
          generateCreateOverride(
              dtoClass,
              ClassName.get("dev.hyperapi.runtime.core.events", "EntityEvent"),
              emitterMirror.isPresent());
      serviceClass.addMethod(method);
    }

    if (fireOnUpdate) {
      MethodSpec method =
          generateUpdateOverride(
              dtoClass,
              ClassName.get("dev.hyperapi.runtime.core.events", "EntityEvent"),
              emitterMirror.isPresent());
      serviceClass.addMethod(method);
    }

    if (fireOnDelete) {
      MethodSpec method =
          generateDeleteOverride(
              ClassName.get("dev.hyperapi.runtime.core.events", "EntityEvent"),
              emitterMirror.isPresent());
      serviceClass.addMethod(method);
    }

    if (fireOnPatch) {
      MethodSpec method =
          generatePatchOverride(
              dtoClass,
              ClassName.get("dev.hyperapi.runtime.core.events", "EntityEvent"),
              emitterMirror.isPresent());
      serviceClass.addMethod(method);
    }

    // Inject custom emitter if specified
    emitterMirror.ifPresent(
        typeMirror ->
            serviceClass.addField(
                FieldSpec.builder(
                        ParameterizedTypeName.get(typeMirror), "emitter", Modifier.PRIVATE)
                    .addAnnotation(ClassName.get("jakarta.inject", "Inject"))
                    .build()));

    serviceClass.addMethod(constructor.build());

    JavaFile.builder(basePackage + ".service", serviceClass.build())
        .indent("    ")
        .build()
        .writeTo(filer);
  }

  private Optional<TypeMirror> getEmitterTypeMirror(TypeElement element) {
    for (AnnotationMirror annotation : element.getAnnotationMirrors()) {
      if (!annotation
          .getAnnotationType()
          .toString()
          .equals("dev.hyperapi.runtime.core.processor.annotations.RestService")) continue;

      for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry :
          annotation.getElementValues().entrySet()) {
        if (!entry.getKey().getSimpleName().contentEquals("events")) continue;

        AnnotationMirror events = (AnnotationMirror) entry.getValue().getValue();

        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> ev :
            events.getElementValues().entrySet()) {
          if (ev.getKey().getSimpleName().contentEquals("emitter")) {
            return Optional.of((TypeMirror) ev.getValue().getValue());
          }
        }
      }
    }
    return Optional.empty();
  }

  private void generateController(TypeElement entity, String dtoName, RestService restService)
      throws IOException {
    String entityName = entity.getSimpleName().toString();
    String basePackage = elementUtils.getPackageOf(entity).getQualifiedName().toString();
    String controllerName = entityName + "RestService";

    ClassName dtoClass = ClassName.get(basePackage + ".dto", dtoName);
    ClassName mapperClass = ClassName.get(basePackage + ".mapper", entityName + "Mapper");
    ClassName serviceClass = ClassName.get(basePackage + ".service", entityName + "Service");
    ClassName entityClass = ClassName.get(basePackage, entityName);

    TypeName superType =
        ParameterizedTypeName.get(
            ClassName.get("dev.hyperapi.runtime.core.controller", "RestController"),
            dtoClass,
            mapperClass,
            entityClass);

    String path =
        restService.path().isBlank() ? "/api/" + entityName.toLowerCase() : restService.path();
    RestService.Scope scope = restService.scope();

    TypeSpec.Builder ctrl =
        TypeSpec.classBuilder(controllerName)
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(generatedAnnotation())
            .addAnnotation(
                AnnotationSpec.builder(ClassName.get("jakarta.ws.rs", "Path"))
                    .addMember("value", "$S", path)
                    .build())
            .addAnnotation(
                AnnotationSpec.builder(ClassName.bestGuess(scope.getScopeClass())).build())
            .superclass(superType)
            .addField(
                FieldSpec.builder(serviceClass, "service", Modifier.PRIVATE)
                    .addAnnotation(ClassName.get("jakarta.inject", "Inject"))
                    .build())
            .addMethod(
                MethodSpec.methodBuilder("getService")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(
                        ParameterizedTypeName.get(
                            ClassName.get("dev.hyperapi.runtime.core.service", "BaseEntityService"),
                            entityClass,
                            dtoClass,
                            mapperClass))
                    .addStatement("return service")
                    .build());

    // Define paging for GetAll method
    Optional<RestService.HttpMethod> isGetMethodDisabled =
        Arrays.stream(restService.disabledFor())
            .filter(
                r -> {
                  return r == RestService.HttpMethod.GET;
                })
            .findFirst();

    if (restService.pageable() != null && isGetMethodDisabled.isEmpty()) {
      int defaultLimit = restService.pageable().limit();
      int maxLimit = restService.pageable().maxLimit();

      MethodSpec getAll =
          MethodSpec.methodBuilder("getAll")
              .addAnnotation(GET.class)
              .addModifiers(Modifier.PUBLIC)
              .returns(ParameterizedTypeName.get(ClassName.get("java.util", "List"), dtoClass))
              .addParameter(
                  ParameterSpec.builder(TypeName.INT, "offset")
                      .addAnnotation(
                          AnnotationSpec.builder(QueryParam.class)
                              .addMember("value", "$S", "offset")
                              .build())
                      .addAnnotation(
                          AnnotationSpec.builder(DefaultValue.class)
                              .addMember("value", "$S", "0")
                              .build())
                      .build())
              .addParameter(
                  ParameterSpec.builder(TypeName.INT, "limit")
                      .addAnnotation(
                          AnnotationSpec.builder(QueryParam.class)
                              .addMember("value", "$S", "limit")
                              .build())
                      .addAnnotation(
                          AnnotationSpec.builder(DefaultValue.class)
                              .addMember("value", "$S", String.valueOf(defaultLimit))
                              .build())
                      .build())
              .addStatement("return getService().findAll(offset, Math.min(limit, $L))", maxLimit)
              .build();

      ctrl.addMethod(getAll);
    }

    // Disabled user-defined endpoints
    if (restService.disabledFor().length > 0) {

      Set<String> disabledMethods =
          Arrays.stream(restService.disabledFor())
              .map(RestService.HttpMethod::name)
              .collect(Collectors.toSet());

      disabledMethods.forEach(
          method -> {
            if (method.equals("DELETE")) ctrl.addMethod(generateDisabledDeleteMethod(dtoClass));
            if (method.equals("GET")) {
              ctrl.addMethod(generateDisabledGetByIdMethod(dtoClass));
              ctrl.addMethod(generateDisabledGetAllMethod(dtoClass));
            }
            if (method.equals("POST")) {
              ctrl.addMethod(generateDisabledPostMethod(dtoClass));
            }
            if (method.equals("PUT")) {
              ctrl.addMethod(generateDisabledPutMethod(dtoClass));
            }
            if (method.equals("PATCH")) {
              ctrl.addMethod(generateDisabledPatchMethod());
            }
          });
    }

    JavaFile.builder(basePackage + ".controller", ctrl.build())
        .indent("    ")
        .build()
        .writeTo(filer);
  }

  private MethodSpec generateDisabledDeleteMethod(ClassName dtoClass) {
    return MethodSpec.methodBuilder("delete")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .addParameter(ParameterSpec.builder(String.class, "id").build())
        .returns(ClassName.get("jakarta.ws.rs.core", "Response"))
        .addStatement(
            "throw new $T($S)",
            ClassName.get("jakarta.ws.rs", "NotFoundException"),
            "DELETE method is disabled for this resource")
        .build();
  }

  private MethodSpec generateDisabledGetAllMethod(ClassName dtoClass) {
    return MethodSpec.methodBuilder("getAll")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .returns(ParameterizedTypeName.get(ClassName.get("java.util", "List"), dtoClass))
        .addParameter(ParameterSpec.builder(TypeName.INT, "offset").build())
        .addParameter(ParameterSpec.builder(TypeName.INT, "limit").build())
        .addStatement(
            "throw new $T($S)",
            ClassName.get("jakarta.ws.rs", "NotFoundException"),
            "Get All method is disabled for this resource")
        .build();
  }

  private MethodSpec generateDisabledGetByIdMethod(ClassName dtoClass) {
    return MethodSpec.methodBuilder("getById")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .returns(ClassName.get("jakarta.ws.rs.core", "Response"))
        .addParameter(ParameterSpec.builder(String.class, "id").build())
        .addStatement(
            "throw new $T($S)",
            ClassName.get("jakarta.ws.rs", "NotFoundException"),
            "Get By Id method is disabled for this resource")
        .build();
  }

  private MethodSpec generateDisabledPostMethod(ClassName dtoClass) {
    return MethodSpec.methodBuilder("create")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .returns(ClassName.get("jakarta.ws.rs.core", "Response"))
        .addParameter(dtoClass, "dto")
        .addStatement(
            "throw new $T($S)",
            ClassName.get("jakarta.ws.rs", "NotFoundException"),
            "POST method is disabled for this resource")
        .build();
  }

  private MethodSpec generateDisabledPutMethod(ClassName dtoClass) {
    return MethodSpec.methodBuilder("update")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .returns(ClassName.get("jakarta.ws.rs.core", "Response"))
        .addParameter(ParameterSpec.builder(String.class, "id").build())
        .addParameter(dtoClass, "dto")
        .addStatement(
            "throw new $T($S)",
            ClassName.get("jakarta.ws.rs", "NotFoundException"),
            "PUT method is disabled for this resource")
        .build();
  }

  private MethodSpec generateDisabledPatchMethod() {
    return MethodSpec.methodBuilder("patch")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .returns(ClassName.get("jakarta.ws.rs.core", "Response"))
        .addParameter(ParameterSpec.builder(String.class, "id").build())
        .addParameter(ClassName.get("jakarta.json", "JsonObject"), "patchJson")
        .addStatement(
            "throw new $T($S)",
            ClassName.get("jakarta.ws.rs", "NotFoundException"),
            "PATH method is disabled for this resource")
        .build();
  }

  private void error(Element e, String msg) {
    messager.printMessage(Diagnostic.Kind.ERROR, msg, e);
  }

  private void error(Element e, String msg, String... args) {
    messager.printMessage(Diagnostic.Kind.ERROR, String.format(msg, args), e);
  }

  private void info(Element e, String msg) {
    messager.printMessage(Diagnostic.Kind.NOTE, msg, e);
  }

  private void warn(Element e, String msg) {
    messager.printMessage(Diagnostic.Kind.NOTE, msg, e);
  }

  private void warn(Element e, String msg, String... args) {
    messager.printMessage(Diagnostic.Kind.WARNING, String.format(msg, args), e);
  }

  private String sanitizeDtoName(String entityName, String rawDto) {
    if (rawDto == null || rawDto.isBlank()) {
      return entityName + "DTO";
    }
    String cleaned = rawDto.replaceAll("(?i)_?dto$", "").trim();
    return cleaned + "DTO";
  }

  /** Generates @Generated annotation with detailed build metadata */
  private AnnotationSpec generatedAnnotation() {
    return AnnotationSpec.builder(Generated.class)
        .addMember("value", "$S", "dev.hyperapi.runtime.core.processor.RestServiceProcessor")
        .addMember(
            "date", "$S", OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
        .addMember(
            "comments",
            "$S",
            String.format(
                "Source version: %s\n"
                    + "Compiler: %s %s\n"
                    + "Build environment: %s %s (%s)\n"
                    + "Project: %s\n"
                    + "License: Apache 2.0",
                Runtime.version(),
                System.getProperty("java.vm.name"),
                System.getProperty("java.vm.version"),
                System.getProperty("os.name"),
                System.getProperty("os.version"),
                System.getProperty("os.arch"),
                "HyperAPI Quarkus Extension"))
        .build();
  }
}
