package dev.hyperapi.runtime.core.processor;

import java.util.*;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.*;
import javax.tools.Diagnostic;

public class PathValidator {
    private final Elements elementUtils;
    private final Types typeUtils;
    private final Messager messager;

    public PathValidator(ProcessingEnvironment env) {
        this.elementUtils = env.getElementUtils();
        this.typeUtils = env.getTypeUtils();
        this.messager = env.getMessager();
    }

    public boolean validatePaths(TypeElement typeElement, String[] paths) {
        if (paths == null || paths.length == 0) return true;

        boolean allValid = true;
        for (String path : paths) {
            if (!validatePath(typeElement, path.trim())) {
                allValid = false;
            }
        }
        return allValid;
    }

    private boolean validatePath(TypeElement rootType, String path) {
        String[] parts = path.split("\\.");
        TypeMirror currentType = rootType.asType();
        Element currentElement = rootType;

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            Element field = findField(currentType, part);

            if (field == null) {
                printPathError(rootType, path, part, currentType, "field not found");
                return false;
            }

            if (i < parts.length - 1) {
                currentType = field.asType();
                currentType = resolveCollectionElementType(currentType);
                currentElement = typeUtils.asElement(currentType);

                if (currentElement == null || currentType.getKind() != TypeKind.DECLARED) {
                    printPathError(rootType, path, part, currentType, "not a navigable type");
                    return false;
                }
            }
        }
        return true;
    }

    private TypeMirror resolveCollectionElementType(TypeMirror type) {
        if (type.getKind() == TypeKind.DECLARED) {
            for (TypeMirror superType : typeUtils.directSupertypes(type)) {
                if (superType.toString().startsWith("java.util.Collection<")) {
                    return ((DeclaredType) superType).getTypeArguments().get(0);
                }
            }
        }
        return type;
    }

    private Element findField(TypeMirror type, String fieldName) {
        TypeElement typeElement = (TypeElement) typeUtils.asElement(type);
        if (typeElement == null) return null;

        return elementUtils.getAllMembers(typeElement).stream()
                .filter(e -> e.getKind() == ElementKind.FIELD || e.getKind() == ElementKind.METHOD)
                .filter(e -> {
                    if (e.getKind() == ElementKind.METHOD) {
                        String methodName = e.getSimpleName().toString();
                        return (methodName.startsWith("get") || methodName.startsWith("is"))
                                && methodName.substring(methodName.startsWith("is") ? 2 : 3)
                                .equalsIgnoreCase(fieldName);
                    }
                    return e.getSimpleName().toString().equals(fieldName);
                })
                .findFirst()
                .orElse(null);
    }

    private void printPathError(Element root, String fullPath, String failedPart,
                                TypeMirror context, String reason) {
        messager.printMessage(Diagnostic.Kind.ERROR,
                String.format("Invalid path '%s' in @RestService.Mapping: " +
                                "Segment '%s' (%s) %s in type %s",
                        fullPath, failedPart, context, reason, root.getSimpleName()),
                root);
    }
}