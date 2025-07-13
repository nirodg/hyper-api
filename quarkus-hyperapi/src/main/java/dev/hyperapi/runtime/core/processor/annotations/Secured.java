/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dev.hyperapi.runtime.core.processor.annotations;

import jakarta.ws.rs.NameBinding;
import java.lang.annotation.*;

/** Use on resource classes or methods to enable HyperAPI security */
@NameBinding
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Secured {}
