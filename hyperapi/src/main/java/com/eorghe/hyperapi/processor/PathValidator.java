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

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * PathValidator is a utility class for validating paths in annotations.
 *
 * <p>This class is primarily used to validate paths specified in annotations, ensuring
 * that they correspond to valid fields or methods in the annotated class.
 *
 * <p>It provides methods to validate paths, resolve collection element types, and
 * find fields or methods based on their names.
 *
 * @author Dorin Brage
 * @version 0.1.0
 * @since 0.1.0
 */
public class PathValidator {

  private final Elements elementUtils;
  private final Types typeUtils;
  private final Messager messager;

  /**
   * Constructs a PathValidator instance with the given processing environment.
   *
   * @param env the processing environment provided by the annotation processor
   */
  public PathValidator(ProcessingEnvironment env) {
    this.elementUtils = env.getElementUtils();
    this.typeUtils = env.getTypeUtils();
    this.messager = env.getMessager();
  }

  /**
   * Validates an array of paths against the specified type element.
   *
   * @param typeElement the type element to validate paths against
   * @param paths       an array of paths to validate
   * @return true if all paths are valid, false otherwise
   */
  public boolean validatePaths(TypeElement typeElement, String[] paths) {
    if (paths == null || paths.length == 0) {
      return true;
    }

    boolean allValid = true;
    for (String path : paths) {
      if (!validatePath(typeElement, path.trim())) {
        allValid = false;
      }
    }
    return allValid;
  }

  /**
   * Validates a single path against the specified root type.
   *
   * @param rootType the root type element to validate the path against
   * @param path     the path to validate
   * @return true if the path is valid, false otherwise
   */
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

  /**
   * Resolves the element type of a collection, if applicable.
   *
   * @param type the type to resolve
   * @return the element type of the collection, or the original type if not a collection
   */
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

  /**
   * Finds a field or method in the specified type by name.
   *
   * @param type      the type to search
   * @param fieldName the name of the field or method to find
   * @return the matching field or method element, or null if not found
   */
  private Element findField(TypeMirror type, String fieldName) {
    TypeElement typeElement = (TypeElement) typeUtils.asElement(type);
    if (typeElement == null) {
      return null;
    }

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

  /**
   * Prints an error message for an invalid path.
   *
   * @param root       the root element where the error occurred
   * @param fullPath   the full path that was invalid
   * @param failedPart the part of the path that caused the error
   * @param context    the type context in which the error occurred
   * @param reason     the reason for the error
   */
  private void printPathError(Element root, String fullPath, String failedPart,
      TypeMirror context, String reason) {
    messager.printMessage(Diagnostic.Kind.ERROR,
        String.format("Invalid path '%s' in @Mapping: " +
                "Segment '%s' (%s) %s in type %s",
            fullPath, failedPart, context, reason, root.getSimpleName()),
        root);
  }
}