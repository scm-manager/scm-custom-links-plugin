package com.cloudogu.customlinks;

import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Link;
import de.otto.edison.hal.Links;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.store.ConfigurationEntryStore;
import sonia.scm.web.VndMediaType;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class GlobalCollectionConfigurationResource<DAO, DTO extends HalRepresentation> {

  public static final String MEDIA_TYPE = "application/json";

  @GET
  @Produces(MEDIA_TYPE)
  @Path("")
  public HalRepresentation getAll(@Context UriInfo uriInfo) {
    if (getListPermission() != null) {
      ConfigurationPermissions.custom(getListPermission()).check();
    }
    return new HalRepresentation(
      createCollectionLinks(uriInfo),
      Embedded.embedded(
        getCollectionName(),
        mapToDtos(getConfigStore().getAll().values(), uriInfo)
      )
    );
  }

  @POST
  @Consumes(MEDIA_TYPE)
  @Path("")
  public void create(@Valid DAO payload) {
    ConfigurationPermissions.custom(getCreatePermission()).check();
    getConfigStore().put(getId(payload), payload);
  }

  @DELETE
  @Path("{id}")
  public void delete(@PathParam("id") String id) {
    ConfigurationPermissions.custom(getDeletePermission()).check();
    getConfigStore().remove(id);
  }

  private List<DTO> mapToDtos(Collection<DAO> customLinks, UriInfo uriInfo) {
    return customLinks.stream().map(mapToDto(uriInfo)).collect(Collectors.toList());
  }

  private Function<DAO, DTO> mapToDto(UriInfo uriInfo) {
    return (DAO entity) -> {
      Links.Builder builder = Links.linkingTo();
      if (ConfigurationPermissions.custom(getDeletePermission()).isPermitted()) {
        builder.single(Link.link("delete", getDeleteLink(uriInfo, entity)));
      }
      return map(entity, builder.build());
    };
  }

  private Links createCollectionLinks(UriInfo uriInfo) {
    Links.Builder builder = Links.linkingTo();
    builder.single(Link.link("self", getAllLink(uriInfo)));
    if (ConfigurationPermissions.custom(getCreatePermission()).isPermitted()) {
      builder.single(Link.link("addLink", getAddLink(uriInfo)));
    }

    return builder.build();
  }

  private UriBuilder getLinkBuilder(UriInfo uriInfo) {
    return UriBuilder.fromUri(uriInfo.getBaseUri()).path(this.getClass());
  }

  public final String getDeleteLink(UriInfo uriInfo, DAO entity) {
    return getLinkBuilder(uriInfo).path(GlobalCollectionConfigurationResource.class, "delete").build(getId(entity)).toASCIIString();
  }

  public final String getAllLink(UriInfo uriInfo) {
    return getLinkBuilder(uriInfo).path(GlobalCollectionConfigurationResource.class, "getAll").build().toASCIIString();
  }

  public final String getAddLink(UriInfo uriInfo) {
    return getLinkBuilder(uriInfo).path(GlobalCollectionConfigurationResource.class, "create").build().toASCIIString();
  }

  protected String getCreatePermission() {
    return getWritePermission();
  }

  protected String getDeletePermission() {
    return getWritePermission();
  }

  protected String getListPermission() {
    return getReadPermission();
  }

  abstract ConfigurationEntryStore<DAO> getConfigStore();

  abstract String getId(DAO entity);

  abstract DTO map(DAO entity, Links links);

  abstract String getReadPermission();

  abstract String getWritePermission();

  abstract String getCollectionName();
}


