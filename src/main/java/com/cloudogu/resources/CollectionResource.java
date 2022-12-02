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
import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Link;
import de.otto.edison.hal.Links;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.store.DataStore;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class CollectionResource<DAO, DTO extends HalRepresentation> extends Resource {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("")
  public HalRepresentation getAll(@Context UriInfo uriInfo) {
    getListPermission().ifPresent(PermissionCheck::check);
    return new HalRepresentation(
      createCollectionDtoLinks(uriInfo),
      Embedded.embedded(
        getCollectionName(),
        mapToDtos(getStore().getAll().values(), uriInfo)
      )
    );
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("")
  public void create(@Valid DTO payload) {
    getCreatePermission().ifPresent(PermissionCheck::check);
    DAO dao = map(payload);
    getStore().put(getId(dao), dao);
  }

  @DELETE
  @Path("{id}")
  public void delete(@PathParam("id") String id) {
    getDeletePermission().ifPresent(PermissionCheck::check);
    getStore().remove(id);
  }

  private List<DTO> mapToDtos(Collection<DAO> customLinks, UriInfo uriInfo) {
    return customLinks.stream().map(mapToDto(uriInfo)).collect(Collectors.toList());
  }

  private Function<DAO, DTO> mapToDto(UriInfo uriInfo) {
    Supplier<UriBuilder> uriBuilder = () -> UriBuilder.fromUri(uriInfo.getBaseUri());
    return (DAO entity) -> {
      Links.Builder builder = Links.linkingTo();
      if (getDeletePermission().map(PermissionCheck::isPermitted).orElse(true)) {
        builder.single(Link.link("delete", getDeleteLink(uriBuilder, entity)));
      }
      return map(entity, builder.build());
    };
  }

  private Links createCollectionDtoLinks(UriInfo uriInfo) {
    Supplier<UriBuilder> uriBuilder = () -> UriBuilder.fromUri(uriInfo.getBaseUri());
    Links.Builder builder = Links.linkingTo();
    builder.single(Link.link("self", getAllLink(uriBuilder)));
    if (getCreatePermission().map(PermissionCheck::isPermitted).orElse(true)) {
      builder.single(Link.link("add", getAddLink(uriBuilder)));
    }

    return builder.build();
  }

  /**
   * Helper method for {@link sonia.scm.api.v2.resources.Enrich}ing HalRepresentations.
   * We cannot implement the HalAppender ourselves because the child resource might be nested.
   * This can therefore be used to append links to both the index and repositories.
   */
  protected final void createLinks(HalAppender appender, Supplier<UriBuilder> uriBaseBuilderFactory) {
    if (getListPermission().map(PermissionCheck::isPermitted).orElse(true)) {
      appender.appendLink(getCollectionName(), getAllLink(uriBaseBuilderFactory));
    }

    if (getCreatePermission().map(PermissionCheck::isPermitted).orElse(true)) {
      appender.appendLink(
        "add" + getCollectionName().substring(0, 1).toUpperCase() + getCollectionName().substring(1),
        getAddLink(uriBaseBuilderFactory)
      );
    }
  }

  private String getDeleteLink(Supplier<UriBuilder> baseBuilderFactory, DAO entity) {
    return getResourceLinkBuilder(baseBuilderFactory.get()).path(CollectionResource.class, "delete").build(getId(entity)).toASCIIString();
  }

  private String getAllLink(Supplier<UriBuilder> baseBuilderFactory) {
    return getResourceLinkBuilder(baseBuilderFactory.get()).path(CollectionResource.class, "getAll").build().toASCIIString();
  }

  private String getAddLink(Supplier<UriBuilder> baseBuilderFactory) {
    return getResourceLinkBuilder(baseBuilderFactory.get()).path(CollectionResource.class, "create").build().toASCIIString();
  }

  protected Optional<PermissionCheck> getCreatePermission() {
    return getWritePermission();
  }

  protected Optional<PermissionCheck> getDeletePermission() {
    return getWritePermission();
  }

  protected Optional<PermissionCheck> getListPermission() {
    return getReadPermission();
  }

  protected abstract DataStore<DAO> getStore();

  protected abstract String getId(DAO entity);

  protected abstract DTO map(DAO entity, Links links);

  protected abstract DAO map(DTO payload);

  /**
   * TODO: Convention is lowerCamelCase ?
   */
  protected abstract String getCollectionName();

}


