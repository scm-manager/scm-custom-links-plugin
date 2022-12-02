package com.cloudogu.resources;

import com.github.sdorra.ssp.PermissionCheck;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Link;
import de.otto.edison.hal.Links;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.store.ConfigurationStore;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.function.Supplier;

public abstract class ConfigurationResource<DAO, DTO extends HalRepresentation> extends Resource {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("")
  public Response get(@Context UriInfo uriInfo) {
    getReadPermission().ifPresent(PermissionCheck::check);
    return Response.ok(map(getStore().get(), createDtoLinks(uriInfo))).build();
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("")
  public void update(@Valid DTO payload) {
    getWritePermission().ifPresent(PermissionCheck::check);
    getStore().set(map(payload));
  }

  private Links createDtoLinks(UriInfo uriInfo) {
    Supplier<UriBuilder> uriBuilder = () -> UriBuilder.fromUri(uriInfo.getBaseUri());
    Links.Builder builder = Links.linkingTo();
    builder.single(Link.link("self", getReadLink(uriBuilder)));
    if (getWritePermission().map(PermissionCheck::isPermitted).orElse(true)) {
      builder.single(Link.link("update", getUpdateLink(uriBuilder)));
    }

    return builder.build();
  }

  /**
   * Helper method for {@link sonia.scm.api.v2.resources.Enrich}ing HalRepresentations.
   * We cannot implement the HalAppender ourselves because the child resource might be nested.
   * This can therefore be used to append links to both the index and repositories.
   */
  protected final void createLinks(HalAppender appender, Supplier<UriBuilder> uriBaseBuilderFactory) {
    if (getReadPermission().map(PermissionCheck::isPermitted).orElse(true)) {
      appender.appendLink(getConfigurationName(), getReadLink(uriBaseBuilderFactory));
    }

    if (getWritePermission().map(PermissionCheck::isPermitted).orElse(true)) {
      appender.appendLink(
        "update" + getConfigurationName().substring(0, 1).toUpperCase() + getConfigurationName().substring(1),
        getUpdateLink(uriBaseBuilderFactory)
      );
    }
  }

  private String getReadLink(Supplier<UriBuilder> baseBuilderFactory) {
    return getResourceLinkBuilder(baseBuilderFactory.get()).path(ConfigurationResource.class, "get").build().toASCIIString();
  }

  private String getUpdateLink(Supplier<UriBuilder> baseBuilderFactory) {
    return getResourceLinkBuilder(baseBuilderFactory.get()).path(ConfigurationResource.class, "update").build().toASCIIString();
  }
  protected abstract ConfigurationStore<DAO> getStore();
  protected abstract DTO map(DAO entity, Links links);
  protected abstract DAO map(DTO payload);
  protected abstract String getConfigurationName();

}
