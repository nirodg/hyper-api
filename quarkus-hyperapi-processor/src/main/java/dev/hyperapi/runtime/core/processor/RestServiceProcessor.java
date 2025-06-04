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
                .addAnnotation(ClassName.get("lombok", "AllArgsConstructor"));

        for (Element field : entity.getEnclosedElements()) {
            if (field.getKind() == ElementKind.FIELD && !ignore.contains(field.getSimpleName().toString())) {
                TypeMirror type = field.asType();
                dtoBuilder.addField(FieldSpec.builder(TypeName.get(type), field.getSimpleName().toString(), Modifier.PRIVATE).build());
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

        TypeSpec mapperInterface = TypeSpec.classBuilder(mapperName)
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addAnnotation(AnnotationSpec.builder(ClassName.get("org.mapstruct", "Mapper"))
                        .addMember("componentModel", "$S", "cdi")
                        .build())
                .superclass(superType)
                .build();

        JavaFile.builder(basePackage + ".mapper", mapperInterface)
                .indent("    ")
                .build()
                .writeTo(filer);
    }

    private void generateService(TypeElement entity, String dtoName) throws IOException {
        String entityName = entity.getSimpleName().toString();
        String basePackage = elementUtils.getPackageOf(entity).getQualifiedName().toString();
        String serviceName = entityName + "AutoService";

        ClassName dtoClass = ClassName.get(basePackage + ".dto", dtoName);
        ClassName entityClass = ClassName.get(basePackage, entityName);
        ClassName mapperClass = ClassName.get(basePackage + ".mapper", entityName + "Mapper");

        TypeName superType = ParameterizedTypeName.get(
                ClassName.get("dev.hyperapi.runtime.core.service", "BaseEntityService"),
                entityClass, dtoClass, mapperClass
        );

        TypeSpec serviceClass = TypeSpec.classBuilder(serviceName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(ClassName.get("jakarta.enterprise.context", "ApplicationScoped"))
                .superclass(superType)
                .addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addStatement("super($T.class)", entityClass)
                        .build())
                .build();

        JavaFile.builder(basePackage + ".service", serviceClass)
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
}
