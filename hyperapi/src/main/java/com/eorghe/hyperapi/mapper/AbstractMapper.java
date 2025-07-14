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
package com.eorghe.hyperapi.mapper;

import com.eorghe.hyperapi.model.HyperEntity;
import java.util.List;

/**
 * AbstractMapper is an abstract class that provides methods for mapping between DTOs and entities.
 *
 * <p>This class defines the contract for converting entities to DTOs and vice versa, as well as
 * converting lists of entities to lists of DTOs. It is designed to be extended by specific
 * implementations for different types of DTOs and entities.
 *
 * @param <DTO> the type of the Data Transfer Object
 * @param <ENTITY> the type of the entity, which must extend `HyperEntity`
 *
 * @author Dorin Brage
 * @version 0.1.0
 * @since 0.1.0
 */
public abstract class AbstractMapper<DTO, ENTITY extends HyperEntity> {

  /**
   * Converts an entity to a DTO.
   *
   * <p>This method should be implemented by subclasses to define the logic for converting
   * an entity to its corresponding DTO representation.
   *
   * @param entity the entity to convert
   * @return the converted DTO
   */
  public abstract DTO toDto(ENTITY entity);

  /**
   * Converts a DTO to an entity.
   *
   * <p>This method should be implemented by subclasses to define the logic for converting
   * a DTO to its corresponding entity representation.
   *
   * @param dto the DTO to convert
   * @return the converted entity
   */
  public abstract ENTITY toEntity(DTO dto);

  /**
   * Converts a list of entities to a list of DTOs.
   *
   * <p>This method should be implemented by subclasses to define the logic for converting
   * a list of entities to their corresponding DTO representations.
   *
   * @param entities the list of entities to convert
   * @return the list of converted DTOs
   */
  public abstract List<DTO> toList(List<ENTITY> entities);
}