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
package com.eorghe.hyperapi.processor.annotations;

import com.eorghe.hyperapi.events.CdiEntityEmitter;
import com.eorghe.hyperapi.events.EntityEmitter;

/**
 * Events is an annotation for configuring entity lifecycle event handling.
 *
 * <p>This annotation allows enabling or disabling event handling for specific
 * entity lifecycle operations such as create, update, delete, and patch.
 *
 * <p>It also allows specifying a custom emitter class for handling these events.
 *
 * @author Dorin Brage
 * @version 0.1.0
 * @since 0.1.0
 */
public @interface Events {

  /**
   * Indicates whether the `onCreate` event is enabled.
   *
   * @return true if the `onCreate` event is enabled, false otherwise
   */
  boolean onCreate() default false;

  /**
   * Indicates whether the `onUpdate` event is enabled.
   *
   * @return true if the `onUpdate` event is enabled, false otherwise
   */
  boolean onUpdate() default false;

  /**
   * Indicates whether the `onDelete` event is enabled.
   *
   * @return true if the `onDelete` event is enabled, false otherwise
   */
  boolean onDelete() default false;

  /**
   * Indicates whether the `onPatch` event is enabled.
   *
   * @return true if the `onPatch` event is enabled, false otherwise
   */
  boolean onPatch() default false;

  /**
   * Indicates whether the `onGet` event is enabled.
   *
   * @return true if the `onGet` event is enabled, false otherwise
   */
  boolean onGet() default false;

  /**
   * Specifies the class of the emitter used for handling events.
   *
   * <p>The default emitter is `CdiEntityEmitter`.
   *
   * @return the class of the emitter
   */
  Class<? extends EntityEmitter> emitter() default CdiEntityEmitter.class;
}