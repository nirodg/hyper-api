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

import com.squareup.javapoet.*;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

/**
 * PropertyGenerator is a utility class for generating Java class properties and builder methods.
 *
 * <p>This class provides methods to dynamically generate getter, setter, and builder methods
 * for fields in a Java class. It also supports special handling for collection types and custom
 * objects.
 *
 * @author Dorin Brage
 * @version 0.1.0
 * @since 0.1.0
 */
public class PropertyGenerator {

  // List of Java built-in package prefixes
  private static final List<String> JAVA_PACKAGES = Arrays.asList(
      "java.", "javax.", "jakarta.", "android.", "com.sun.", "org.w3c.", "org.xml."
  );

  /**
   * Adds builder support to the specified class.
   *
   * <p>This method generates a nested static builder class with methods to set field values
   * and build an instance of the main class.
   *
   * @param classBuilder the builder for the main class
   * @param fields       the list of fields to include in the builder
   * @param className    the name of the main class
   */
  public static void addBuilderSupport(TypeSpec.Builder classBuilder, List<Element> fields,
      String className) {
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

    // Add build() method
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

  /**
   * Adds getter and setter methods for the specified field with converted DTO types.
   *
   * @param classBuilder the builder for the main class
   * @param fieldName the name of the field
   * @param fieldTypeName the DTO field type (already converted)
   * @param modifiers the modifiers of the original field
   */
  public static void addPropertyMethods(TypeSpec.Builder classBuilder, String fieldName, TypeName fieldTypeName, Set<Modifier> modifiers) {
    // Generate getter
    MethodSpec getter = generateGetter(fieldName, fieldTypeName, modifiers);
    classBuilder.addMethod(getter);

    // Generate setter if not final
    if (!modifiers.contains(Modifier.FINAL)) {
      MethodSpec setter = generateSetter(fieldName, fieldTypeName, modifiers);
      classBuilder.addMethod(setter);
    }

    // Handle special cases for collections
    if (isCollectionTypeName(fieldTypeName)) {
      addCollectionMethodsWithTypeName(classBuilder, fieldName, fieldTypeName);
    }
  }

  /**
   * Adds getter and setter methods for the specified field (legacy method).
   *
   * @param classBuilder the builder for the main class
   * @param field        the field to generate methods for
   * @deprecated Use {@link #addPropertyMethods(TypeSpec.Builder, String, TypeName, Set)} instead
   */
  @Deprecated
  public static void addPropertyMethods(TypeSpec.Builder classBuilder, Element field) {
    String fieldName = field.getSimpleName().toString();
    TypeMirror fieldType = field.asType();
    TypeName fieldTypeName = TypeName.get(fieldType);

    addPropertyMethods(classBuilder, fieldName, fieldTypeName, field.getModifiers());
  }


  /**
   * Checks if a TypeName represents a collection type.
   *
   * @param typeName the TypeName to check
   * @return true if the type is a collection, false otherwise
   */
  private static boolean isCollectionTypeName(TypeName typeName) {
    String typeString = typeName.toString();
    return typeString.startsWith("java.util.List") ||
            typeString.startsWith("java.util.Set") ||
            typeString.startsWith("java.util.Collection") ||
            typeString.startsWith("java.util.Map");
  }

  /**
   * Adds collection manipulation methods using TypeName instead of TypeMirror.
   *
   * @param classBuilder the builder for the main class
   * @param fieldName the name of the collection field
   * @param fieldTypeName the DTO field type
   */
  private static void addCollectionMethodsWithTypeName(TypeSpec.Builder classBuilder, String fieldName, TypeName fieldTypeName) {
    String typeString = fieldTypeName.toString();

    if (typeString.startsWith("java.util.Map")) {
      // Extract key and value types from ParameterizedTypeName
      if (fieldTypeName instanceof ParameterizedTypeName) {
        ParameterizedTypeName paramType = (ParameterizedTypeName) fieldTypeName;
        List<TypeName> typeArgs = paramType.typeArguments;

        if (typeArgs.size() >= 2) {
          TypeName keyType = typeArgs.get(0);
          TypeName valueType = typeArgs.get(1);

          MethodSpec putter = MethodSpec.methodBuilder("put" + capitalize(fieldName) + "Entry")
                  .addModifiers(Modifier.PUBLIC)
                  .addParameter(keyType, "key")
                  .addParameter(valueType, "value")
                  .addStatement("this.$L.put(key, value)", fieldName)
                  .build();
          classBuilder.addMethod(putter);
        }
      }
    } else {
      // Handle List, Set, Collection
      if (fieldTypeName instanceof ParameterizedTypeName) {
        ParameterizedTypeName paramType = (ParameterizedTypeName) fieldTypeName;
        List<TypeName> typeArgs = paramType.typeArguments;

        if (!typeArgs.isEmpty()) {
          TypeName elementType = typeArgs.get(0);

          MethodSpec adder = MethodSpec.methodBuilder("add" + capitalize(fieldName) + "Item")
                  .addModifiers(Modifier.PUBLIC)
                  .addParameter(elementType, "item")
                  .addStatement("this.$L.add(item)", fieldName)
                  .build();
          classBuilder.addMethod(adder);
        }
      }
    }

    // Common collection clear method
    MethodSpec clearer = MethodSpec.methodBuilder("clear" + capitalize(fieldName))
            .addModifiers(Modifier.PUBLIC)
            .addStatement("this.$L.clear()", fieldName)
            .build();
    classBuilder.addMethod(clearer);
  }

  /**
   * Generates a getter method for the specified field.
   *
   * @param fieldName the name of the field
   * @param fieldType the type of the field
   * @param modifiers the modifiers of the field
   * @return the generated getter method
   */
  private static MethodSpec generateGetter(String fieldName, TypeName fieldType,
      Set<Modifier> modifiers) {
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

  /**
   * Generates a setter method for the specified field.
   *
   * @param fieldName the name of the field
   * @param fieldType the type of the field
   * @param modifiers the modifiers of the field
   * @return the generated setter method
   */
  private static MethodSpec generateSetter(String fieldName, TypeName fieldType,
      Set<Modifier> modifiers) {
    return MethodSpec.methodBuilder("set" + capitalize(fieldName))
        .addModifiers(Modifier.PUBLIC)
        .addParameter(fieldType, fieldName)
        .addStatement("this.$L = $L", fieldName, fieldName)
        .build();
  }

  /**
   * Determines if the specified type is a custom object (not a Java built-in type).
   *
   * @param type the type to check
   * @return true if the type is a custom object, false otherwise
   */
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

  /**
   * Determines if the specified type is a collection type.
   *
   * @param type the type to check
   * @return true if the type is a collection, false otherwise
   */
  static boolean isCollectionType(TypeMirror type) {
    if (type.getKind() != TypeKind.DECLARED) {
      return false;
    }

    String typeName = type.toString();
    return typeName.startsWith("java.util.List") ||
        typeName.startsWith("java.util.Set") ||
        typeName.startsWith("java.util.Collection") ||
        typeName.startsWith("java.util.Map");
  }

  /**
   * Adds methods for manipulating collection fields.
   *
   * <p>This includes methods for adding items, clearing the collection, and adding entries
   * for map types.
   *
   * @param classBuilder the builder for the main class
   * @param field        the collection field
   */
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

  /**
   * Helper method to get the element type of a collection.
   *
   * @param collectionType the collection type
   * @return the element type of the collection
   */
  private static TypeName getCollectionElementType(TypeMirror collectionType) {
    if (collectionType instanceof DeclaredType) {
      List<? extends TypeMirror> typeArgs = ((DeclaredType) collectionType).getTypeArguments();
      if (!typeArgs.isEmpty()) {
        return TypeName.get(typeArgs.get(0));
      }
    }
    return TypeName.get(Object.class);
  }

  /**
   * Helper method to get the key type of a map.
   *
   * @param mapType the map type
   * @return the key type of the map
   */
  private static TypeName getMapKeyType(TypeMirror mapType) {
    if (mapType instanceof DeclaredType) {
      List<? extends TypeMirror> typeArgs = ((DeclaredType) mapType).getTypeArguments();
      if (typeArgs.size() > 0) {
        return TypeName.get(typeArgs.get(0));
      }
    }
    return TypeName.get(Object.class);
  }

  /**
   * Helper method to get the value type of a map.
   *
   * @param mapType the map type
   * @return the value type of the map
   */
  private static TypeName getMapValueType(TypeMirror mapType) {
    if (mapType instanceof DeclaredType) {
      List<? extends TypeMirror> typeArgs = ((DeclaredType) mapType).getTypeArguments();
      if (typeArgs.size() > 1) {
        return TypeName.get(typeArgs.get(1));
      }
    }
    return TypeName.get(Object.class);
  }

  /**
   * Capitalizes the first letter of a string.
   *
   * @param str the string to capitalize
   * @return the capitalized string
   */
  private static String capitalize(String str) {
    if (str == null || str.isEmpty()) {
      return str;
    }
    return str.substring(0, 1).toUpperCase() + str.substring(1);
  }
}