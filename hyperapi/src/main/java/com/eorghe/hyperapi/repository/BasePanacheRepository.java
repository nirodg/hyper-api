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
package com.eorghe.hyperapi.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;


/**
 * BasePanacheRepository is a generic interface that extends PanacheRepositoryBase.
 *
 * <p>This interface provides basic CRUD operations for entities managed by Hibernate ORM
 * in Quarkus applications. It serves as a base repository for custom repositories.
 *
 * @param <ENTITY> the type of the entity managed by the repository
 * @param <ID>     the type of the entity's identifier
 * @author Dorin Brage
 * @version 0.1.0
 * @since 0.1.0
 */
public interface BasePanacheRepository<ENTITY, ID> extends PanacheRepositoryBase<ENTITY, ID> {

}