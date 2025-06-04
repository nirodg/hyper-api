package dev.hyperapi.runtime.core.processor;

import dev.hyperapi.runtime.annotations.RestService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.util.Set;

import static java.rmi.server.LogStream.log;

@SupportedAnnotationTypes("your.package.RestService")
@SupportedSourceVersion(SourceVersion.RELEASE_21) // or 21
public class RestServiceProcessor extends AbstractProcessor {

    private Filer filer;
    private Messager messager;
    private Elements elementUtils;
    private Types typeUtils;
    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        this.filer = env.getFiler();
        this.messager = env.getMessager();
        this.elementUtils = env.getElementUtils();
        this.typeUtils = env.getTypeUtils();
    }
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(RestService.class)) {

            if (!(annotatedElement instanceof TypeElement entityType)) {
                error(annotatedElement, "@RestService must be on a class.");
                continue;
            }

            RestService restService = entityType.getAnnotation(RestService.class);
            String dtoName = restService.dto().getName();
            String[] ignoredFields = restService.mapping().ignore();

            boolean shouldGenerate = !dtoName.isBlank() || ignoredFields.length > 0;

            if (shouldGenerate) {
                info(entityType, "Generating files for " + entityType.getSimpleName());

                // These would call the actual code generators
                generateDTO(entityType, dtoName, ignoredFields);
                generateMapper(entityType, dtoName);
                generateAutoService(entityType, dtoName);
            } else {
                info(entityType, "Skipping generation for " + entityType.getSimpleName() + " (no dto or mapping set)");
            }
        }
        return true;
    }

    private void generateDTO(TypeElement entityType, String dtoName, String[] ignoredFields) {
        // Stub: Replace with real generator
        info(entityType, "[STUB] Would generate DTO: " + dtoName);
    }

    private void generateMapper(TypeElement entityType, String dtoName) {
        // Stub: Replace with real generator
        info(entityType, "[STUB] Would generate Mapper for DTO: " + dtoName);
    }

    private void generateAutoService(TypeElement entityType, String dtoName) {
        // Stub: Replace with real generator
        info(entityType, "[STUB] Would generate AutoService for DTO: " + dtoName);
    }

    private void error(Element e, String msg) {
        messager.printMessage(Diagnostic.Kind.ERROR, msg, e);
    }

    private void info(Element e, String msg) {
        messager.printMessage(Diagnostic.Kind.NOTE, msg, e);
    }
}
