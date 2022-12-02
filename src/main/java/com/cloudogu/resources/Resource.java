/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.cloudogu.resources;

import com.github.sdorra.ssp.PermissionCheck;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import sonia.scm.api.v2.resources.ScmPathInfoStore;

import javax.inject.Provider;
import javax.ws.rs.core.UriBuilder;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class Resource<DAO, DTO extends HalRepresentation, Store> {

  protected final Optional<PermissionCheck> readPermission;
  protected final Optional<PermissionCheck> writePermission;

  protected final Function<DTO, DAO> dtoToDaoMapper;
  protected final DaoToDtoMapper<DAO, DTO> daoToDtoMapper;
  
  protected final String name;

  protected final Supplier<Store> storeSupplier;

  protected final Supplier<UriBuilder> baseUriBuilderSupplier;

  protected Resource(Optional<PermissionCheck> readPermission, Optional<PermissionCheck> writePermission, Function<DTO, DAO> dtoToDaoMapper, DaoToDtoMapper<DAO, DTO> daoToDtoMapper, String name, Supplier<Store> storeSupplier, Supplier<UriBuilder> baseUriBuilderSupplier) {
    this.readPermission = readPermission;
    this.writePermission = writePermission;
    this.dtoToDaoMapper = dtoToDaoMapper;
    this.daoToDtoMapper = daoToDtoMapper;
    this.name = name;
    this.storeSupplier = storeSupplier;
    this.baseUriBuilderSupplier = baseUriBuilderSupplier;
  }

  protected Resource(Optional<PermissionCheck> readPermission, Optional<PermissionCheck> writePermission, Function<DTO, DAO> dtoToDaoMapper, DaoToDtoMapper<DAO, DTO> daoToDtoMapper, String name, Supplier<Store> storeSupplier, Provider<ScmPathInfoStore> scmPathInfoStore) {
    this.readPermission = readPermission;
    this.writePermission = writePermission;
    this.dtoToDaoMapper = dtoToDaoMapper;
    this.daoToDtoMapper = daoToDtoMapper;
    this.name = name;
    this.storeSupplier = storeSupplier;
    this.baseUriBuilderSupplier = () -> UriBuilder.fromUri(scmPathInfoStore.get().get().getApiRestUri());
  }

  protected final UriBuilder getResourceLinkBuilder(UriBuilder base) {
    return base.path(this.getClass());
  }

  public Optional<PermissionCheck> getReadPermission() {
    return readPermission;
  }

  public Optional<PermissionCheck> getWritePermission() {
    return writePermission;
  }

  public interface DaoToDtoMapper<DAO, DTO> {
    DTO map(DAO entity, Links links);
  }
}
