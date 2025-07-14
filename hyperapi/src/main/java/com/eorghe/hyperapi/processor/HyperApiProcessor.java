/*
 * MIT License
 *
 * Copyright (c) 2025 Dorin Brage
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.eorghe.hyperapi.processor;

import com.eorghe.hyperapi.processor.annotations.Events;
import com.eorghe.hyperapi.processor.annotations.HyperResource;
import com.eorghe.hyperapi.processor.enums.HttpMethod;
import com.eorghe.hyperapi.processor.enums.Scope;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import jakarta.annotation.Generated;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.QueryParam;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

/**
 * HyperApiProcessor is a custom annotation processor for generating code based on the
 * `@HyperResource` annotation.
 *
 * <p>This processor handles the generation of DTOs, mappers, services, and controllers for
 * entities annotated with `@HyperResource`. It ensures that the annotated classes extend a base
 * entity and generates the necessary boilerplate code for interacting with HyperAPI.
 *
 * <p>Key features include:
 * <ul>
 *   <li>Validation of annotated classes to ensure they extend the base entity.</li>
 *   <li>Generation of DTOs with Jackson annotations.</li>
 *   <li>Creation of mappers for entity-to-DTO and DTO-to-entity conversions.</li>
 *   <li>Generation of service classes with CRUD operations and event handling.</li>
 *   <li>Creation of controllers with REST endpoints.</li>
 * </ul>
 *
 * @author Dorin Brage
 * @version 0.1.0
 * @since 0.1.0
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes("com.eorghe.hyperapi.processor.annotations.HyperResource")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class HyperApiProcessor extends AbstractProcessor {

  /**
   * Fully qualified name of the base entity class required for `@HyperResource` annotations.
   */
  public static final String DEV_HYPERAPI_RUNTIME_CORE_ENTITY = "com.eorghe.hyperapi.model.HyperEntity";

  /**
   * Fully qualified name of the base DTO class used for generated DTOs.
   */
  public static final String DEV_HYPERAPI_RUNTIME_CORE_ENTITY_DTO = "com.eorghe.hyperapi.dto.HyperDto";

  private Filer filer;
  private Messager messager;
  private Elements elementUtils;

  /**
   * Initializes the annotation processor with the processing environment.
   *
   * @param env the processing environment provided by the compiler
   */
  @Override
  public synchronized void init(ProcessingEnvironment env) {
    super.init(env);
    filer = env.getFiler();
    messager = env.getMessager();
    elementUtils = env.getElementUtils();
  }



  /**
   * Processes the `@HyperResource` annotations and generates the required code.
   *
   * @param annotations the set of annotations to process
   * @param roundEnv    the environment for the current processing round
   * @return true if the annotations were processed successfully, false otherwise
   */
  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(HyperResource.class)) {
      if (!(annotatedElement instanceof TypeElement entityType)) {
        error(annotatedElement, "@HyperResource must annotate a class.");
        continue;
      }

      // 2. Check BaseEntity inheritance
      if (!isExtendingBaseEntity(entityType)) {
        error(
            entityType,
            "Class %s must extend %s to use @HyperResource",
            DEV_HYPERAPI_RUNTIME_CORE_ENTITY,
            entityType.getSimpleName().toString());
        return false;
      }

      HyperResource hyperResource = entityType.getAnnotation(HyperResource.class);
      String dtoName = sanitizeDtoName(entityType.getSimpleName().toString(), hyperResource.dto());
      List<String> ignoredFields = Arrays.asList(hyperResource.mapping().ignore());
      List<String> ignoredNestedFields = Arrays.asList(hyperResource.mapping().ignoreNested());

      boolean shouldGenerate = !dtoName.isBlank() || !ignoredFields.isEmpty();
      if (!shouldGenerate) {
        info(entityType, "Skipping generation for " + entityType.getSimpleName());
        continue;
      }

      try {
        generateDTO(entityType, dtoName, ignoredFields);
        generateMapper(entityType, dtoName, ignoredFields, ignoredNestedFields);
        generateService(entityType, dtoName, hyperResource);
        generateController(entityType, dtoName, hyperResource);
      } catch (IOException | ClassNotFoundException e) {
        error(entityType, "Code generation failed: " + e.getMessage());
      }
    }
    return true;
  }

  /**
   * Checks if the given class extends the base entity class.
   *
   * @param typeElement the class to check
   * @return true if the class extends the base entity, false otherwise
   */
  private boolean isExtendingBaseEntity(TypeElement typeElement) {
    // 1. Get BaseEntity type
    TypeElement baseEntityType =
        elementUtils.getTypeElement(DEV_HYPERAPI_RUNTIME_CORE_ENTITY);

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

  /**
   * Generates the Mapper class for the given entity.
   *
   * @param entity       the entity TypeElement
   * @param dtoName      the name of the DTO to generate
   * @param ignore       the list of fields to ignore in mapping
   * @param ignoreNested the list of nested fields to ignore in mapping
   * @throws IOException if there is an error writing the generated file
   */
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
            ClassName.get("com.eorghe.hyperapi.mapper", "AbstractMapper"),
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

  /**
   * Generates a DTO class for the given entity.
   *
   * @param entity  the entity TypeElement
   * @param dtoName the name of the DTO to generate
   * @param ignore  the list of fields to ignore in the DTO
   * @throws IOException            if there is an error writing the generated file
   * @throws ClassNotFoundException if the base DTO class cannot be found
   */
  private void generateDTO(TypeElement entity, String dtoName, List<String> ignore)
      throws IOException, ClassNotFoundException {
    String basePackage = elementUtils.getPackageOf(entity).getQualifiedName().toString();
    ClassName dtoClass = ClassName.get(basePackage + ".dto", dtoName);
    ClassName baseDtoClass = ClassName.bestGuess(DEV_HYPERAPI_RUNTIME_CORE_ENTITY_DTO);

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
              + "Ensure it has fields annotated with @HyperResource or not ignored by mapping.";
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

  /**
   * Returns the appropriate collection implementation type based on the provided collection type.
   *
   * @param collectionType the TypeMirror representing the collection type
   * @return the TypeName of the collection implementation
   */
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

  /**
   * Adds common methods (toString, equals, hashCode) to the DTO class.
   *
   * @param builder      the TypeSpec.Builder for the DTO class
   * @param fields       the list of fields in the DTO
   * @param className    the name of the DTO class
   * @param originalName the original name of the entity class
   * @throws ClassNotFoundException if the base DTO class cannot be found
   */
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

  /**
   * Retrieves the field names from the BaseDTO class.
   *
   * @return a list of field names in the BaseDTO class
   * @throws ClassNotFoundException if the BaseDTO class cannot be found in the classpath
   */
  private List<String> getBaseDtoFields() throws ClassNotFoundException {
    List<String> fieldNames = new ArrayList<>();

    // Load BaseDTO class using element utils
    TypeElement baseDtoType = elementUtils.getTypeElement(DEV_HYPERAPI_RUNTIME_CORE_ENTITY_DTO);
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

  /**
   * Generates common methods (toString, equals, hashCode) for the DTO class.
   *
   * @param builder      the TypeSpec.Builder for the DTO class
   * @param fields       the list of fields in the DTO
   * @param className    the name of the DTO class
   * @param objectsClass the ClassName for the Objects utility class
   */
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

  /**
   * Generates the create method override for the service class.
   *
   * @param dtoClass         the ClassName of the DTO
   * @param entityEventClass the ClassName of the EntityEvent
   * @param customEmitter    indicates if a custom event emitter is used
   * @return the MethodSpec for the create method override
   */
  private MethodSpec generateCreateOverride(
      ClassName dtoClass, ClassName entityEventClass, boolean customEmitter) {

    String strCustomEmitter = customEmitter ? "emitter.emit" : "fireEvent";
    return MethodSpec.methodBuilder("create")
        .addAnnotation(Override.class)
        .addAnnotation(ClassName.get("jakarta.transaction", "Transactional"))
        .addModifiers(Modifier.PUBLIC)
        .returns(dtoClass)
        .addParameter(dtoClass, "dto")
        .addStatement("$T result = super.create(dto)", dtoClass)
        .addStatement(
            strCustomEmitter + "($T.Type.CREATE, mapper.toEntity(result))", entityEventClass)
        .addStatement("return result")
        .build();
  }

  /**
   * Generates the update method override for the service class.
   *
   * @param dtoClass         the ClassName of the DTO
   * @param entityEventClass the ClassName of the EntityEvent
   * @param customEmitter    indicates if a custom event emitter is used
   * @return the MethodSpec for the update method override
   */
  private MethodSpec generateUpdateOverride(
      ClassName dtoClass, ClassName entityEventClass, boolean customEmitter) {
    String strCustomEmitter = customEmitter ? "emitter.emit" : "fireEvent";

    return MethodSpec.methodBuilder("update")
        .addAnnotation(Override.class)
        .addAnnotation(ClassName.get("jakarta.transaction", "Transactional"))
        .addModifiers(Modifier.PUBLIC)
        .returns(dtoClass)
        .addParameter(dtoClass, "dto")
        .addStatement("$T result = super.update(dto)", dtoClass)
        .addStatement(
            strCustomEmitter + "($T.Type.UPDATE, mapper.toEntity(result))", entityEventClass)
        .addStatement("return result")
        .build();
  }

  /**
   * Generates the delete method override for the service class.
   *
   * @param entityEventClass the ClassName of the EntityEvent
   * @param customEmitter    indicates if a custom event emitter is used
   * @return the MethodSpec for the delete method override
   */
  private MethodSpec generateDeleteOverride(ClassName entityEventClass, boolean customEmitter) {
    String strCustomEmitter = customEmitter ? "emitter.emit" : "fireEvent";

    return MethodSpec.methodBuilder("delete")
        .addAnnotation(Override.class)
        .addAnnotation(ClassName.get("jakarta.transaction", "Transactional"))
        .addModifiers(Modifier.PUBLIC)
        .returns(TypeName.VOID)
        .addParameter(ParameterSpec.builder(ClassName.get(Long.class), "id").build())
        .addStatement("super.delete(id)")
        .addStatement(
            strCustomEmitter + "($T.Type.DELETE, null)", entityEventClass) // if you don’t re-fetch
        .build();
  }

  /**
   * Generates the patch method override for the service class.
   *
   * @param dtoClass         the ClassName of the DTO
   * @param entityEventClass the ClassName of the EntityEvent
   * @param customEmitter    indicates if a custom event emitter is used
   * @return the MethodSpec for the patch method override
   */
  private MethodSpec generatePatchOverride(
      ClassName dtoClass, ClassName entityEventClass, boolean customEmitter) {
    String strCustomEmitter = customEmitter ? "emitter.emit" : "fireEvent";

    return MethodSpec.methodBuilder("patch")
        .addAnnotation(Override.class)
        .addAnnotation(ClassName.get("jakarta.transaction", "Transactional"))
        .addModifiers(Modifier.PUBLIC)
        .returns(dtoClass)
        .addParameter(ParameterSpec.builder(ClassName.get(Long.class), "id").build())
        .addParameter(
            ParameterSpec.builder(ClassName.get("jakarta.json", "JsonObject"), "patchJson").build())
        .addStatement("$T result = super.patch(id, patchJson)", dtoClass)
        .addStatement(
            strCustomEmitter + "($T.Type.UPDATE, mapper.toEntity(result))", entityEventClass)
        .addStatement("return result")
        .build();
  }

  /**
   * Generates the service class for the given entity.
   *
   * @param entity        the entity TypeElement
   * @param dtoName       the name of the DTO to generate
   * @param hyperResource the HyperResource annotation containing configuration
   * @throws IOException            if there is an error writing the generated file
   * @throws ClassNotFoundException if the base entity class cannot be found
   */
  private void generateService(TypeElement entity, String dtoName, HyperResource hyperResource)
      throws IOException, ClassNotFoundException {
    String entityName = entity.getSimpleName().toString();
    String basePackage = elementUtils.getPackageOf(entity).getQualifiedName().toString();
    String serviceName = entityName + "Service";

    ClassName dtoClass = ClassName.get(basePackage + ".dto", dtoName);
    ClassName entityClass = ClassName.get(basePackage, entityName);
    ClassName mapperClass = ClassName.get(basePackage + ".mapper", entityName + "Mapper");

    Events events = hyperResource.events();
    boolean fireOnCreate = events.onCreate();
    boolean fireOnUpdate = events.onUpdate();
    boolean fireOnDelete = events.onDelete();
    boolean fireOnPatch = events.onDelete();

    TypeName superType =
        ParameterizedTypeName.get(
            ClassName.get("com.eorghe.hyperapi.service", "BaseEntityService"),
            entityClass,
            dtoClass,
            mapperClass);

    MethodSpec.Builder constructor =
        MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC)
            .addStatement("super($T.class)", dtoClass);

    TypeSpec.Builder serviceClass =
        TypeSpec.classBuilder(serviceName)
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(generatedAnnotation())
            .addAnnotation(ClassName.get("jakarta.enterprise.context", "ApplicationScoped"))
            .superclass(superType);

    // Define injection for the repository
    MethodSpec repoGetter = generateInjectRepository(entity, basePackage, entityName, serviceClass,
        entityClass, hyperResource);
    serviceClass.addMethod(repoGetter);

    Optional<TypeMirror> emitterMirror = getEmitterTypeMirror(entity);

    if (fireOnCreate) {
      MethodSpec method =
          generateCreateOverride(
              dtoClass,
              ClassName.get("com.eorghe.hyperapi.events", "EntityEvent"),
              emitterMirror.isPresent());
      serviceClass.addMethod(method);
    }

    if (fireOnUpdate) {
      MethodSpec method =
          generateUpdateOverride(
              dtoClass,
              ClassName.get("com.eorghe.hyperapi.events", "EntityEvent"),
              emitterMirror.isPresent());
      serviceClass.addMethod(method);
    }

    if (fireOnDelete) {
      MethodSpec method =
          generateDeleteOverride(
              ClassName.get("com.eorghe.hyperapi.events", "EntityEvent"),
              emitterMirror.isPresent());
      serviceClass.addMethod(method);
    }

    if (fireOnPatch) {
      MethodSpec method =
          generatePatchOverride(
              dtoClass,
              ClassName.get("com.eorghe.hyperapi.events", "EntityEvent"),
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

  /**
   * Generates the repository injection method for the service class.
   *
   * @param entity        the entity TypeElement
   * @param basePackage   the base package of the entity
   * @param entityName    the name of the entity
   * @param serviceClass  the TypeSpec.Builder for the service class
   * @param entityClass   the ClassName of the entity
   * @param hyperResource the HyperResource annotation containing configuration
   * @return the MethodSpec for the repository getter
   */
  private MethodSpec generateInjectRepository(TypeElement entity, String basePackage,
      String entityName, TypeSpec.Builder serviceClass, ClassName entityClass,
      HyperResource hyperResource) {

    // Add injected repository field
    ClassName repositoryClass = ClassName.get(basePackage + "." + hyperResource.repositoryPackage(),
        entityName + "Repository");
    serviceClass.addField(FieldSpec.builder(repositoryClass, "repository", Modifier.PRIVATE)
        .addAnnotation(ClassName.get("jakarta.inject", "Inject"))
        .build());

    // Add repository override method
    ParameterizedTypeName repoType = ParameterizedTypeName.get(
        ClassName.get("io.quarkus.hibernate.orm.panache", "PanacheRepositoryBase"),
        entityClass,
        ClassName.get("java.lang", "Long")
    );

    MethodSpec repoGetter = MethodSpec.methodBuilder("getRepository")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PROTECTED)
        .returns(repoType)
        .addStatement("return repository")
        .build();
    return repoGetter;
  }

  /**
   * Retrieves the emitter type mirror from the HyperResource annotation.
   *
   * @param element the TypeElement to inspect for the HyperResource annotation
   * @return an Optional containing the TypeMirror of the emitter type if found, otherwise empty
   */
  private Optional<TypeMirror> getEmitterTypeMirror(TypeElement element) {
    for (AnnotationMirror annotation : element.getAnnotationMirrors()) {
      if (!annotation
          .getAnnotationType()
          .toString()
          .equals("annotations.processor.com.eorghe.runtime.core.HyperResource")) {
        continue;
      }

      for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry :
          annotation.getElementValues().entrySet()) {
        if (!entry.getKey().getSimpleName().contentEquals("events")) {
          continue;
        }

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

  /**
   * Generates the controller class for the given entity.
   *
   * @param entity        the entity TypeElement
   * @param dtoName       the name of the DTO to generate
   * @param hyperResource the HyperResource annotation containing configuration
   * @throws IOException if there is an error writing the generated file
   */
  private void generateController(TypeElement entity, String dtoName, HyperResource hyperResource)
      throws IOException {
    String entityName = entity.getSimpleName().toString();
    String basePackage = elementUtils.getPackageOf(entity).getQualifiedName().toString();
    String controllerName = entityName + "HyperResource";

    ClassName dtoClass = ClassName.get(basePackage + ".dto", dtoName);
    ClassName mapperClass = ClassName.get(basePackage + ".mapper", entityName + "Mapper");
    ClassName serviceClass = ClassName.get(basePackage + ".service", entityName + "Service");
    ClassName entityClass = ClassName.get(basePackage, entityName);

    TypeName superType =
        ParameterizedTypeName.get(
            ClassName.get("com.eorghe.hyperapi.controller", "RestController"),
            dtoClass,
            mapperClass,
            entityClass);

    String path =
        hyperResource.path().isBlank() ? "/api/" + entityName.toLowerCase() : hyperResource.path();
    Scope scope = hyperResource.scope();

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
            .addMethod( // Overriding getService method
                MethodSpec.methodBuilder("getService")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(
                        ParameterizedTypeName.get(
                            ClassName.get("com.eorghe.hyperapi.service", "BaseEntityService"),
                            entityClass,
                            dtoClass,
                            mapperClass))
                    .addStatement("return service")
                    .build());

    // Define paging for GetAll method
    Optional<HttpMethod> isGetMethodDisabled =
        Arrays.stream(hyperResource.disabledFor())
            .filter(
                r -> {
                  return r == HttpMethod.GET;
                })
            .findFirst();

    if (hyperResource.pageable() != null && isGetMethodDisabled.isEmpty()) {
      int defaultLimit = hyperResource.pageable().limit();
      int maxLimit = hyperResource.pageable().maxLimit();

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
    if (hyperResource.disabledFor().length > 0) {

      Set<String> disabledMethods =
          Arrays.stream(hyperResource.disabledFor())
              .map(HttpMethod::name)
              .collect(Collectors.toSet());

      disabledMethods.forEach(
          method -> {
            if (method.equals("DELETE")) {
              ctrl.addMethod(generateDisabledDeleteMethod(dtoClass));
            }
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

  /**
   * Generates a disabled delete method for the controller.
   *
   * @param dtoClass the ClassName of the DTO
   * @return the MethodSpec for the disabled delete method
   */
  private MethodSpec generateDisabledDeleteMethod(ClassName dtoClass) {
    return MethodSpec.methodBuilder("delete")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .addParameter(ParameterSpec.builder(Object.class, "id").build())
        .returns(ClassName.get("jakarta.ws.rs.core", "Response"))
        .addStatement(
            "throw new $T($S)",
            ClassName.get("jakarta.ws.rs", "NotFoundException"),
            "DELETE method is disabled for this resource")
        .build();
  }

  /**
   * Generates a disabled getAll method for the controller.
   *
   * @param dtoClass the ClassName of the DTO
   * @return the MethodSpec for the disabled getAll method
   */
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

  /**
   * Generates a disabled getById method for the controller.
   *
   * @param dtoClass the ClassName of the DTO
   * @return the MethodSpec for the disabled getById method
   */
  private MethodSpec generateDisabledGetByIdMethod(ClassName dtoClass) {
    return MethodSpec.methodBuilder("getById")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .returns(ClassName.get("jakarta.ws.rs.core", "Response"))
        .addParameter(ParameterSpec.builder(Long.class, "id").build())
        .addStatement(
            "throw new $T($S)",
            ClassName.get("jakarta.ws.rs", "NotFoundException"),
            "Get By Id method is disabled for this resource")
        .build();
  }

  /**
   * Generates a disabled post method for the controller.
   *
   * @param dtoClass the ClassName of the DTO
   * @return the MethodSpec for the disabled post method
   */
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

  /**
   * Generates a disabled put method for the controller.
   *
   * @param dtoClass the ClassName of the DTO
   * @return the MethodSpec for the disabled put method
   */
  private MethodSpec generateDisabledPutMethod(ClassName dtoClass) {
    return MethodSpec.methodBuilder("update")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .returns(ClassName.get("jakarta.ws.rs.core", "Response"))
        .addParameter(ParameterSpec.builder(Object.class, "id").build())
        .addParameter(dtoClass, "dto")
        .addStatement(
            "throw new $T($S)",
            ClassName.get("jakarta.ws.rs", "NotFoundException"),
            "PUT method is disabled for this resource")
        .build();
  }

  /**
   * Generates a disabled patch method for the controller.
   *
   * @return the MethodSpec for the disabled patch method
   */
  private MethodSpec generateDisabledPatchMethod() {
    return MethodSpec.methodBuilder("patch")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .returns(ClassName.get("jakarta.ws.rs.core", "Response"))
        .addParameter(ParameterSpec.builder(Object.class, "id").build())
        .addParameter(ClassName.get("jakarta.json", "JsonObject"), "patchJson")
        .addStatement(
            "throw new $T($S)",
            ClassName.get("jakarta.ws.rs", "NotFoundException"),
            "PATH method is disabled for this resource")
        .build();
  }

  /**
   * Logs an error message with the specified element and message.
   *
   * @param e   the Element to log the error for
   * @param msg the error message to log
   */
  private void error(Element e, String msg) {
    messager.printMessage(Diagnostic.Kind.ERROR, msg, e);
  }

  /**
   * Logs an error message with the specified element, message, and arguments.
   *
   * @param e    the Element to log the error for
   * @param msg  the error message to log, formatted with String.format
   * @param args the arguments to format the message with
   */
  private void error(Element e, String msg, String... args) {
    messager.printMessage(Diagnostic.Kind.ERROR, String.format(msg, args), e);
  }

  /**
   * Logs an informational message with the specified element and message.
   *
   * @param e   the Element to log the info for
   * @param msg the informational message to log
   */
  private void info(Element e, String msg) {
    messager.printMessage(Diagnostic.Kind.NOTE, msg, e);
  }

  /**
   * Logs a warning message with the specified element and message.
   *
   * @param e   the Element to log the warning for
   * @param msg the warning message to log
   */
  private void warn(Element e, String msg) {
    messager.printMessage(Diagnostic.Kind.NOTE, msg, e);
  }

  /**
   * Logs a warning message with the specified element, message, and arguments.
   *
   * @param e    the Element to log the warning for
   * @param msg  the warning message to log, formatted with String.format
   * @param args the arguments to format the message with
   */
  private void warn(Element e, String msg, String... args) {
    messager.printMessage(Diagnostic.Kind.WARNING, String.format(msg, args), e);
  }

  /**
   * Sanitizes the DTO name based on the entity name and raw DTO name.
   *
   * @param entityName the name of the entity
   * @param rawDto     the raw DTO name provided by the user
   * @return
   */
  private String sanitizeDtoName(String entityName, String rawDto) {
    if (rawDto == null || rawDto.isBlank()) {
      return entityName + "DTO";
    }
    String cleaned = rawDto.replaceAll("(?i)_?dto$", "").trim();
    return cleaned + "DTO";
  }

  /**
   * Generates @Generated annotation with detailed build metadata
   */
  private AnnotationSpec generatedAnnotation() {
    return AnnotationSpec.builder(Generated.class)
        .addMember("value", "$S", "com.eorghe.hyperapi.processor.HyperResourceProcessor")
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
