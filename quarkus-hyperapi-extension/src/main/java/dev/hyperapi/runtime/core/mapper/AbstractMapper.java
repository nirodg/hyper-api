package dev.hyperapi.runtime.core.mapper;

import dev.hyperapi.runtime.core.dto.BaseDTO;
import dev.hyperapi.runtime.core.model.BaseEntity;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Map;

public abstract class AbstractMapper<DTO, ENTITY extends BaseEntity> {

    /**
     * Converts an entity to a DTO.
     *
     * @param entity the entity to convert
     * @return the converted DTO
     */
    public abstract DTO toDto(ENTITY entity);


    /**
     * Converts a DTO to an entity.
     *
     * @param dto the DTO to convert
     * @return the converted entity
     */
    public abstract ENTITY toEntity(DTO dto);


    public abstract List<DTO> toList(List<ENTITY> entities);
}
