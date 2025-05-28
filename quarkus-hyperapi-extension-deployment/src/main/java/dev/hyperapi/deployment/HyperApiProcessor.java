package dev.hyperapi.deployment;

import dev.hyperapi.runtime.core.controller.GenericCrudController;
import dev.hyperapi.runtime.core.mapper.DtoMapper;
import dev.hyperapi.runtime.core.registry.EntityRegistry;
import dev.hyperapi.runtime.core.service.GenericCrudService;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;

public final class HyperApiProcessor {

    private static final String FEATURE_NAME = "hyperapi";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE_NAME);
    }

    @BuildStep
    AdditionalBeanBuildItem beans() {
        // Make runtime beans unremovable
        return AdditionalBeanBuildItem.builder()
                .addBeanClasses(
                        DtoMapper.class,
                        EntityRegistry.class,
                        GenericCrudController.class,
                        GenericCrudService.class
                )
                .setUnremovable()
                .build();

    }
}
