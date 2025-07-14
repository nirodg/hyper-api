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
package com.eorghe.hyperapi.deployment;

import com.eorghe.hyperapi.controller.RestController;
import com.eorghe.hyperapi.registry.EntityRegistry;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.logging.Log;

/**
 * HyperApiProcessor is a build step processor for the HyperAPI extension.
 *
 * <p>This class defines build steps to register the HyperAPI feature and runtime beans
 * during the Quarkus application build process.
 *
 * @author Dorin Brage
 * @version 0.1.0
 * @since 0.1.0
 */
public final class HyperApiProcessor {

  /**
   * The name of the HyperAPI feature used for Quarkus feature registration.
   */
  private static final String FEATURE_NAME = "hyperapi";

  /**
   * Registers the HyperAPI feature with Quarkus.
   *
   * <p>This build step adds the feature name to the Quarkus feature list, enabling
   * the framework to recognize the HyperAPI extension.
   *
   * @return a FeatureBuildItem representing the HyperAPI feature
   */
  @BuildStep
  FeatureBuildItem feature() {
    return new FeatureBuildItem(FEATURE_NAME);
  }

  /**
   * Registers runtime beans required by the HyperAPI extension.
   *
   * <p>This build step ensures that the `EntityRegistry` and `RestController` beans
   * are added to the application context and marked as unremovable.
   *
   * @return an AdditionalBeanBuildItem containing the registered beans
   */
  @BuildStep
  AdditionalBeanBuildItem beans() {
    // Make runtime beans unremovable
    return AdditionalBeanBuildItem.builder()
        .addBeanClasses(
            EntityRegistry.class, RestController.class)
        .setUnremovable()
        .build();
  }
}