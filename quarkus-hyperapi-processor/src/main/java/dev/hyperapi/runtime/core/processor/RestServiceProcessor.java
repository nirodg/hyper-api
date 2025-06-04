package dev.hyperapi.runtime.core.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;
import dev.hyperapi.runtime.core.processor.annotations.RestService;

import javax.annotation.Generated;
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

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

            boolean shouldGenerate = !restService.dto().isBlank() || !ignoredFields.isEmpty();
            if (!shouldGenerate) {
                info(entityType, "Skipping generation for " + entityType.getSimpleName());
                continue;
            }

            try {
                generateDTO(entityType, dtoName, ignoredFields);
                generateMapper(entityType, dtoName);
                generateService(entityType, dtoName);
                generateController(entityType, dtoName);
            } catch (IOException e) {
                error(entityType, "Code generation failed: " + e.getMessage());
            }
        }
        return true;
    }

    private void generateDTO(TypeElement entity, String dtoName, List<String> ignore) throws IOException {
        String basePackage = elementUtils.getPackageOf(entity).getQualifiedName().toString();
        ClassName dtoClass = ClassName.get(basePackage + ".dto", dtoName);

        TypeSpec.Builder dtoBuilder = TypeSpec.classBuilder(dtoClass)
                .addModifiers(Modifier.PUBLIC)
                .superclass(ClassName.get("dev.hyperapi.runtime.core.dto", "BaseDTO"))
                .addAnnotation(ClassName.get("lombok", "Getter"))
                .addAnnotation(ClassName.get("lombok", "Setter"))
                .addAnnotation(ClassName.get("lombok", "NoArgsConstructor"))
                .addAnnotation(ClassName.get("lombok", "AllArgsConstructor"))
                .addAnnotation(generatedAnnotation());

        for (Element field : entity.getEnclosedElements()) {
            if (field.getKind() == ElementKind.FIELD && !ignore.contains(field.getSimpleName().toString())) {
                dtoBuilder.addField(FieldSpec.builder(TypeName.get(field.asType()), field.getSimpleName().toString(), Modifier.PRIVATE).build());
            }
        }

        JavaFile.builder(dtoClass.packageName(), dtoBuilder.build())
                .indent("    ")
                .build()
                .writeTo(filer);
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

    private void generateController(TypeElement entity, String dtoName) throws IOException {
        String entityName = entity.getSimpleName().toString();
        String basePackage = elementUtils.getPackageOf(entity).getQualifiedName().toString();
        String controllerName = entityName + "RestService";

        ClassName dtoClass = ClassName.get(basePackage + ".dto", dtoName);
        ClassName mapperClass = ClassName.get(basePackage + ".mapper", entityName + "Mapper");
        ClassName serviceClass = ClassName.get(basePackage + ".service", entityName + "Service");
        ClassName entityClass = ClassName.get(basePackage, entityName);

        TypeName superType = ParameterizedTypeName.get(
                ClassName.get("dev.hyperapi.runtime.core.controller", "RestController"),
                dtoClass, mapperClass, entityClass
        );

        TypeSpec controllerClass = TypeSpec.classBuilder(controllerName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(generatedAnnotation())
                .addAnnotation(AnnotationSpec.builder(ClassName.get("jakarta.ws.rs", "Path"))
                        .addMember("value", "$S", "/api/" + entityName.toLowerCase())
                        .build())
                .addAnnotation(ClassName.get("jakarta.enterprise.context", "ApplicationScoped"))
                .superclass(superType)
                .addField(FieldSpec.builder(serviceClass, "service", Modifier.PRIVATE)
                        .addAnnotation(ClassName.get("jakarta.inject", "Inject"))
                        .build())
                .addMethod(MethodSpec.methodBuilder("getService")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(ParameterizedTypeName.get(
                                ClassName.get("dev.hyperapi.runtime.core.service", "BaseEntityService"),
                                entityClass, dtoClass, mapperClass))
                        .addStatement("return service")
                        .build())
                .build();

        JavaFile.builder(basePackage + ".controller", controllerClass)
                .indent("    ")
                .build()
                .writeTo(filer);
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
        return AnnotationSpec.builder(Generated.class)
                .addMember("value", "$S", "dev.hyperapi.runtime.core.processor.RestServiceProcessor")
                .addMember("date", "$S", OffsetDateTime.now()
                        .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .addMember("comments", "$S", String.format(
                        "Source version: %s\n" +
                                "Compiler: %s %s\n" +
                                "Build environment: %s %s (%s)\n" +
                                "Project: %s\n" +
                                "License: Apache 2.0",
                        Runtime.version(),
                        System.getProperty("java.vm.name"),
                        System.getProperty("java.vm.version"),
                        System.getProperty("os.name"),
                        System.getProperty("os.version"),
                        System.getProperty("os.arch"),
                        "HyperAPI Quarkus Extension"
                ))
                .build();
    }
}
