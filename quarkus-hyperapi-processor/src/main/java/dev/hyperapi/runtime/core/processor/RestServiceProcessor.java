package dev.hyperapi.runtime.core.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;
import dev.hyperapi.runtime.core.processor.annotations.RestService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import jakarta.annotation.Generated;


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

            RestService restService = entityType.getAnnotation(RestService.class);
            String dtoName = sanitizeDtoName(entityType.getSimpleName().toString(), restService.dto());
            List<String> ignoredFields = Arrays.asList(restService.mapping().ignore());

            boolean shouldGenerate = !dtoName.isBlank() || !ignoredFields.isEmpty();
            if (!shouldGenerate) {
                info(entityType, "Skipping generation for " + entityType.getSimpleName());
                continue;
            }

            try {
                generateDTO(entityType, dtoName, ignoredFields);
                generateMapper(entityType, dtoName);
                generateService(entityType, dtoName);
                generateController(entityType, dtoName, restService);
            } catch (IOException e) {
                error(entityType, "Code generation failed: " + e.getMessage());
            }
        }
        return true;
    }


    private void generateMapper(TypeElement entity, String dtoName) throws IOException {
        String entityName = entity.getSimpleName().toString();
        String basePackage = elementUtils.getPackageOf(entity).getQualifiedName().toString();
        String mapperName = entityName + "Mapper";

        ClassName dtoClass = ClassName.get(basePackage + ".dto", dtoName);
        ClassName entityClass = ClassName.get(basePackage, entityName);

        TypeName superType = ParameterizedTypeName.get(
                ClassName.get("dev.hyperapi.runtime.core.mapper", "AbstractMapper"),
                dtoClass, entityClass
        );

        TypeSpec mapperClass = TypeSpec.classBuilder(mapperName)
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addAnnotation(generatedAnnotation())
                .addAnnotation(AnnotationSpec.builder(ClassName.get("org.mapstruct", "Mapper"))
                        .addMember("componentModel", "$S", "cdi")
                        .build())
                .superclass(superType)
                .build();

        JavaFile.builder(basePackage + ".mapper", mapperClass)
                .indent("    ")
                .build()
                .writeTo(filer);
    }

    private void generateDTO(TypeElement entity, String dtoName, List<String> ignore) throws IOException {
        String basePackage = elementUtils.getPackageOf(entity).getQualifiedName().toString();
        ClassName dtoClass = ClassName.get(basePackage + ".dto", dtoName);
        ClassName baseDtoClass = ClassName.get("dev.hyperapi.runtime.core.dto", "BaseDTO");

        // Initialize builder with common configurations
        TypeSpec.Builder dtoBuilder = TypeSpec.classBuilder(dtoClass)
                .addModifiers(Modifier.PUBLIC)
                .superclass(baseDtoClass)
                .addAnnotation(generatedAnnotation())
                .addAnnotation(AnnotationSpec.builder(ClassName.get("com.fasterxml.jackson.annotation", "JsonInclude"))
                        .addMember("value", "$T.Include.NON_NULL",
                                ClassName.get("com.fasterxml.jackson.annotation", "JsonInclude"))
                        .build());

        // Track fields for potential builder pattern
        List<Element> allFields = new ArrayList<>();
//        boolean generateBuilder = entityHasManyFields || hasOptionalFields;
        // Process all fields
        for (Element field : entity.getEnclosedElements()) {
            if (field.getKind() == ElementKind.FIELD && !ignore.contains(field.getSimpleName().toString())) {
                String fieldName = field.getSimpleName().toString();
                TypeMirror fieldType = field.asType();
                TypeName fieldTypeName = TypeName.get(fieldType);

                // Add the field with Jackson annotations
                FieldSpec.Builder fieldBuilder = FieldSpec.builder(fieldTypeName, fieldName, Modifier.PRIVATE)
                        .addAnnotation(AnnotationSpec.builder(ClassName.get("com.fasterxml.jackson.annotation", "JsonProperty"))
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

        // Add toString(), equals() and hashCode()
        addCommonMethods(dtoBuilder, allFields, dtoName);


        // Add builder pattern if needed
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
    private void addCommonMethods(TypeSpec.Builder builder, List<Element> fields, String className) {
        ClassName objectsClass = ClassName.get("java.util", "Objects");

        // Generate toString()
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
        String formatArgs = toStringArgs.stream()
                .map(Object::toString)
                .collect(Collectors.joining(", "));

        MethodSpec toString = MethodSpec.methodBuilder("toString")
                .addModifiers(Modifier.PUBLIC)
                .returns(String.class)
                .addAnnotation(Override.class)
                .addStatement("return String.format($S, $L)",
                        toStringFormat.toString(),
                        formatArgs)
                .build();

        // Generate equals()
        ClassName dtoClassName = ClassName.bestGuess(className);

        MethodSpec.Builder equalsBuilder = MethodSpec.methodBuilder("equals")
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
                comparisonBuilder.add("$T.equals(this.$L, that.$L)",
                        objectsClass, fieldName, fieldName);
            } else {
                comparisonBuilder.add(" &&\n    $T.equals(this.$L, that.$L)",
                        objectsClass, fieldName, fieldName);
            }
        }

        equalsBuilder.addStatement("return $L", comparisonBuilder.build());
        MethodSpec equals = equalsBuilder.build();


        // Generate hashCode()
        String hashFields = fields.stream()
                .map(f -> f.getSimpleName().toString())
                .collect(Collectors.joining(", "));

        MethodSpec hashCode = MethodSpec.methodBuilder("hashCode")
                .addModifiers(Modifier.PUBLIC)
                .returns(int.class)
                .addAnnotation(Override.class)
                .addStatement("return $T.hash($L)", objectsClass, hashFields)
                .build();

        builder.addMethod(toString)
                .addMethod(equals)
                .addMethod(hashCode);
    }

    private void generateService(TypeElement entity, String dtoName) throws IOException {
        String entityName = entity.getSimpleName().toString();
        String basePackage = elementUtils.getPackageOf(entity).getQualifiedName().toString();
        String serviceName = entityName + "Service";

        ClassName dtoClass = ClassName.get(basePackage + ".dto", dtoName);
        ClassName entityClass = ClassName.get(basePackage, entityName);
        ClassName mapperClass = ClassName.get(basePackage + ".mapper", entityName + "Mapper");

        TypeName superType = ParameterizedTypeName.get(
                ClassName.get("dev.hyperapi.runtime.core.service", "BaseEntityService"),
                entityClass, dtoClass, mapperClass
        );

        TypeSpec serviceClass = TypeSpec.classBuilder(serviceName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(generatedAnnotation())
                .addAnnotation(ClassName.get("jakarta.enterprise.context", "ApplicationScoped"))
                .superclass(superType)
                .addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addStatement("super($T.class, $T.class)", entityClass, dtoClass)
                        .build())
                .build();

        JavaFile.builder(basePackage + ".service", serviceClass)
                .indent("    ")
                .build()
                .writeTo(filer);
    }

    private void generateController(TypeElement entity, String dtoName, RestService restService) throws IOException {
        String entityName = entity.getSimpleName().toString();
        String basePackage = elementUtils.getPackageOf(entity).getQualifiedName().toString();
        String controllerName = entityName + "RestService";

        ClassName dtoClass = ClassName.get(basePackage + ".dto", dtoName);
        ClassName mapperClass = ClassName.get(basePackage + ".mapper", entityName + "Mapper");
        ClassName serviceClass = ClassName.get(basePackage + ".service", entityName + "Service");
        ClassName entityClass = ClassName.get(basePackage, entityName);

        TypeName superType = ParameterizedTypeName.get(ClassName.get("dev.hyperapi.runtime.core.controller", "RestController"), dtoClass, mapperClass, entityClass);

        String path = restService.path().isBlank() ? "/api/" + entityName.toLowerCase() : restService.path();
        RestService.Scope scope = restService.scope();

        TypeSpec controllerClass = TypeSpec.classBuilder(controllerName).addModifiers(Modifier.PUBLIC).addAnnotation(generatedAnnotation()).addAnnotation(AnnotationSpec.builder(ClassName.get("jakarta.ws.rs", "Path")).addMember("value", "$S", path).build()).addAnnotation(AnnotationSpec.builder(ClassName.bestGuess(scope.getScopeClass())).build()).superclass(superType).addField(FieldSpec.builder(serviceClass, "service", Modifier.PRIVATE).addAnnotation(ClassName.get("jakarta.inject", "Inject")).build()).addMethod(MethodSpec.methodBuilder("getService").addAnnotation(Override.class).addModifiers(Modifier.PUBLIC).returns(ParameterizedTypeName.get(ClassName.get("dev.hyperapi.runtime.core.service", "BaseEntityService"), entityClass, dtoClass, mapperClass)).addStatement("return service").build()).build();

        JavaFile.builder(basePackage + ".controller", controllerClass).indent("    ").build().writeTo(filer);
    }

    private void error(Element e, String msg) {
        messager.printMessage(Diagnostic.Kind.ERROR, msg, e);
    }

    private void info(Element e, String msg) {
        messager.printMessage(Diagnostic.Kind.NOTE, msg, e);
    }

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
        return AnnotationSpec.builder(Generated.class).addMember("value", "$S", "dev.hyperapi.runtime.core.processor.RestServiceProcessor").addMember("date", "$S", OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)).addMember("comments", "$S", String.format("Source version: %s\n" + "Compiler: %s %s\n" + "Build environment: %s %s (%s)\n" + "Project: %s\n" + "License: Apache 2.0", Runtime.version(), System.getProperty("java.vm.name"), System.getProperty("java.vm.version"), System.getProperty("os.name"), System.getProperty("os.version"), System.getProperty("os.arch"), "HyperAPI Quarkus Extension")).build();
    }
}
