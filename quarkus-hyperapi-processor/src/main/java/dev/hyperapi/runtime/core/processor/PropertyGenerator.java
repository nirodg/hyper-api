package dev.hyperapi.runtime.core.processor;

import com.squareup.javapoet.*;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class PropertyGenerator {

    // List of Java built-in package prefixes
    private static final List<String> JAVA_PACKAGES = Arrays.asList(
            "java.", "javax.", "jakarta.", "android.", "com.sun.", "org.w3c.", "org.xml."
    );

    public static void addBuilderSupport(TypeSpec.Builder classBuilder, List<Element> fields, String className) {
        // Create builder class
        TypeSpec.Builder builder = TypeSpec.classBuilder(className + "Builder")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL);

        // Add builder fields
        for (Element field : fields) {
            String fieldName = field.getSimpleName().toString();
            TypeName fieldType = TypeName.get(field.asType());

            builder.addField(FieldSpec.builder(fieldType, fieldName, Modifier.PRIVATE).build());
        }

        // Add builder constructor
        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .build();
        builder.addMethod(constructor);

        // Add static builder() method
        MethodSpec builderMethod = MethodSpec.methodBuilder("builder")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(ClassName.bestGuess(className + "Builder"))
                .addStatement("return new $T()", ClassName.bestGuess(className + "Builder"))
                .build();
        classBuilder.addMethod(builderMethod);

        // Add withX() methods for each field
        for (Element field : fields) {
            String fieldName = field.getSimpleName().toString();
            TypeName fieldType = TypeName.get(field.asType());

            MethodSpec withMethod = MethodSpec.methodBuilder("with" + capitalize(fieldName))
                    .addModifiers(Modifier.PUBLIC)
                    .returns(ClassName.bestGuess(className + "Builder"))
                    .addParameter(fieldType, fieldName)
                    .addStatement("this.$L = $L", fieldName, fieldName)
                    .addStatement("return this")
                    .build();
            builder.addMethod(withMethod);
        }

        // Add build() method - CORRECTED VERSION
        CodeBlock.Builder codeBuilder = CodeBlock.builder()
                .addStatement("$T dto = new $T()",
                        ClassName.bestGuess(className),
                        ClassName.bestGuess(className));

        fields.forEach(f ->
                codeBuilder.addStatement("dto.$L = this.$L",
                        f.getSimpleName(),
                        f.getSimpleName()));

        MethodSpec buildMethod = MethodSpec.methodBuilder("build")
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.bestGuess(className))
                .addCode(codeBuilder.build())
                .addStatement("return dto")
                .build();

        builder.addMethod(buildMethod);

        // Add the builder to the main class
        classBuilder.addType(builder.build());
    }

    public static void addPropertyMethods(TypeSpec.Builder classBuilder, Element field) {
        String fieldName = field.getSimpleName().toString();
        TypeMirror fieldType = field.asType();
        TypeName fieldTypeName = TypeName.get(fieldType);

        // Generate getter
        MethodSpec getter = generateGetter(fieldName, fieldTypeName, field.getModifiers());
        classBuilder.addMethod(getter);

        // Generate setter if not final
        if (!field.getModifiers().contains(Modifier.FINAL)) {
            MethodSpec setter = generateSetter(fieldName, fieldTypeName, field.getModifiers());
            classBuilder.addMethod(setter);
        }

        // Handle special cases
        if (isCollectionType(fieldType)) {
            addCollectionMethods(classBuilder, field);
        } else if (isCustomObject(fieldType)) {
            addDeepCopyMethods(classBuilder, field);
        }
    }

    private static MethodSpec generateGetter(String fieldName, TypeName fieldType, Set<Modifier> modifiers) {
        String getterName = "get" + capitalize(fieldName);
        if (fieldType.equals(TypeName.BOOLEAN)) {
            getterName = "is" + capitalize(fieldName);
        }

        return MethodSpec.methodBuilder(getterName)
                .addModifiers(Modifier.PUBLIC)
                .returns(fieldType)
                .addStatement("return this.$L", fieldName)
                .build();
    }

    private static MethodSpec generateSetter(String fieldName, TypeName fieldType, Set<Modifier> modifiers) {
        return MethodSpec.methodBuilder("set" + capitalize(fieldName))
                .addModifiers(Modifier.PUBLIC)
                .addParameter(fieldType, fieldName)
                .addStatement("this.$L = $L", fieldName, fieldName)
                .build();
    }

    // Helper to determine if a type is a custom object (not Java built-in)
    public static boolean isCustomObject(TypeMirror type) {
        if (type.getKind() != TypeKind.DECLARED) {
            return false;
        }

        String typeName = type.toString();

        // Skip primitives and arrays
        if (type.getKind().isPrimitive() || typeName.endsWith("[]")) {
            return false;
        }

        // Skip Java platform types
        for (String pkg : JAVA_PACKAGES) {
            if (typeName.startsWith(pkg)) {
                return false;
            }
        }

        // Skip enums
        if (((DeclaredType) type).asElement().getKind() == ElementKind.ENUM) {
            return false;
        }

        return true;
    }

    // Collection type detection
    static boolean isCollectionType(TypeMirror type) {
        if (type.getKind() != TypeKind.DECLARED) return false;

        String typeName = type.toString();
        return typeName.startsWith("java.util.List") ||
                typeName.startsWith("java.util.Set") ||
                typeName.startsWith("java.util.Collection") ||
                typeName.startsWith("java.util.Map");
    }

    // Collection methods generator
    private static void addCollectionMethods(TypeSpec.Builder classBuilder, Element field) {
        String fieldName = field.getSimpleName().toString();
        TypeMirror fieldType = field.asType();

        if (fieldType.toString().startsWith("java.util.Map")) {
            // Map-specific methods
            MethodSpec putter = MethodSpec.methodBuilder("put" + capitalize(fieldName) + "Entry")
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(getMapKeyType(fieldType), "key")
                    .addParameter(getMapValueType(fieldType), "value")
                    .addStatement("this.$L.put(key, value)", fieldName)
                    .build();
            classBuilder.addMethod(putter);
        } else {
            // Collection adder method
            MethodSpec adder = MethodSpec.methodBuilder("add" + capitalize(fieldName) + "Item")
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(getCollectionElementType(fieldType), "item")
                    .addStatement("this.$L.add(item)", fieldName)
                    .build();
            classBuilder.addMethod(adder);
        }

        // Common collection methods
        MethodSpec clearer = MethodSpec.methodBuilder("clear" + capitalize(fieldName))
                .addModifiers(Modifier.PUBLIC)
                .addStatement("this.$L.clear()", fieldName)
                .build();
        classBuilder.addMethod(clearer);
    }

    // Deep copy methods for custom objects
    private static void addDeepCopyMethods(TypeSpec.Builder classBuilder, Element field) {
        String fieldName = field.getSimpleName().toString();
        TypeMirror fieldType = field.asType();

        MethodSpec deepCopyGetter = MethodSpec.methodBuilder("get" + capitalize(fieldName) + "Copy")
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.get(fieldType))
                .addStatement("return this.$L != null ? new $T(this.$L) : null",
                        fieldName,
                        TypeName.get(fieldType),
                        fieldName)
                .build();

        classBuilder.addMethod(deepCopyGetter);
    }

    // Helper to get collection element type
    private static TypeName getCollectionElementType(TypeMirror collectionType) {
        if (collectionType instanceof DeclaredType) {
            List<? extends TypeMirror> typeArgs = ((DeclaredType) collectionType).getTypeArguments();
            if (!typeArgs.isEmpty()) {
                return TypeName.get(typeArgs.get(0));
            }
        }
        return TypeName.get(Object.class);
    }

    // Helper to get Map key type
    private static TypeName getMapKeyType(TypeMirror mapType) {
        if (mapType instanceof DeclaredType) {
            List<? extends TypeMirror> typeArgs = ((DeclaredType) mapType).getTypeArguments();
            if (typeArgs.size() > 0) {
                return TypeName.get(typeArgs.get(0));
            }
        }
        return TypeName.get(Object.class);
    }

    // Helper to get Map value type
    private static TypeName getMapValueType(TypeMirror mapType) {
        if (mapType instanceof DeclaredType) {
            List<? extends TypeMirror> typeArgs = ((DeclaredType) mapType).getTypeArguments();
            if (typeArgs.size() > 1) {
                return TypeName.get(typeArgs.get(1));
            }
        }
        return TypeName.get(Object.class);
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}