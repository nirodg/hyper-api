package dev.hyperapi.runtime.core.processor;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Set;

public class PropertyGenerator {

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

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    // Handle complex objects (Collections, Maps, etc.)
    public static void addCollectionMethods(TypeSpec.Builder classBuilder, Element field) {
        TypeMirror fieldType = field.asType();
        String fieldName = field.getSimpleName().toString();

        if (isCollectionType(fieldType)) {
            // Add adder method for collections
            MethodSpec adder = MethodSpec.methodBuilder("add" + capitalize(fieldName) + "Item")
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(getCollectionElementType(fieldType), "item")
                    .addStatement("this.$L.add(item)", fieldName)
                    .build();
            classBuilder.addMethod(adder);

            // Add clearer method
            MethodSpec clearer = MethodSpec.methodBuilder("clear" + capitalize(fieldName))
                    .addModifiers(Modifier.PUBLIC)
                    .addStatement("this.$L.clear()", fieldName)
                    .build();
            classBuilder.addMethod(clearer);
        }
    }

    private static boolean isCollectionType(TypeMirror type) {
        String typeName = type.toString();
        return typeName.startsWith("java.util.List") ||
                typeName.startsWith("java.util.Set") ||
                typeName.startsWith("java.util.Collection");
    }

    private static TypeName getCollectionElementType(TypeMirror collectionType) {
        if (collectionType instanceof DeclaredType) {
            List<? extends TypeMirror> typeArgs = ((DeclaredType) collectionType).getTypeArguments();
            if (!typeArgs.isEmpty()) {
                return TypeName.get(typeArgs.getFirst());
            }
        }
        return TypeName.get(Object.class);
    }
}