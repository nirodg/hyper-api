/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dev.hyperapi.runtime.core;

import dev.hyperapi.runtime.core.mapper.DtoMapper;
import dev.hyperapi.runtime.core.service.GenericCrudService;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import java.util.logging.Logger;

@ApplicationScoped
public class HyperApiStartupHandler {

    private static final Logger LOG = Logger.getLogger(HyperApiStartupHandler.class.getName());

    @Inject
    DtoMapper dtoMapper;

    @Inject
    GenericCrudService crudService;

    void onStart(@Observes StartupEvent ev) {
        LOG.info("HyperAPI extension started successfully!");
        LOG.info("DtoMapper instance: " + (dtoMapper != null ? "OK" : "NULL"));
        LOG.info("GenericCrudService instance: " + (crudService != null ? "OK" : "NULL"));
    }
}
