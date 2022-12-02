package com.cloudogu.resources;

import com.github.sdorra.ssp.PermissionCheck;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Link;
import de.otto.edison.hal.Links;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricher;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.store.ConfigurationStore;

import javax.inject.Provider;
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
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class ConfigurationResource<DAO, DTO extends HalRepresentation> extends Resource<DAO, DTO, ConfigurationStore<DAO>> implements HalEnricher {

  public ConfigurationResource(
    Optional<PermissionCheck> readPermission,
    Optional<PermissionCheck> writePermission,
    Function<DTO, DAO> dtoToDaoMapper,
    DaoToDtoMapper<DAO, DTO> daoToDtoMapper,
    String name,
    Supplier<ConfigurationStore<DAO>> storeSupplier,
    Supplier<UriBuilder> baseUriBuilderSupplier
  ) {
    super(readPermission, writePermission, dtoToDaoMapper, daoToDtoMapper, name, storeSupplier, baseUriBuilderSupplier);
  }

  public ConfigurationResource(
    Optional<PermissionCheck> readPermission,
    Optional<PermissionCheck> writePermission,
    Function<DTO, DAO> dtoToDaoMapper,
    DaoToDtoMapper<DAO, DTO> daoToDtoMapper,
    String name,
    Supplier<ConfigurationStore<DAO>> storeSupplier,
    Provider<ScmPathInfoStore> scmPathInfoStore
  ) {
    super(readPermission, writePermission, dtoToDaoMapper, daoToDtoMapper, name, storeSupplier, scmPathInfoStore);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("")
  public Response get(@Context UriInfo uriInfo) {
    getReadPermission().ifPresent(PermissionCheck::check);
    return Response.ok(daoToDtoMapper.map(storeSupplier.get().get(), createDtoLinks(uriInfo))).build();
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("")
  public void update(@Valid DTO payload) {
    getWritePermission().ifPresent(PermissionCheck::check);
    storeSupplier.get().set(dtoToDaoMapper.apply(payload));
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

  private String getReadLink(Supplier<UriBuilder> baseBuilderFactory) {
    return getResourceLinkBuilder(baseBuilderFactory.get()).path(ConfigurationResource.class, "get").build().toASCIIString();
  }

  private String getUpdateLink(Supplier<UriBuilder> baseBuilderFactory) {
    return getResourceLinkBuilder(baseBuilderFactory.get()).path(ConfigurationResource.class, "update").build().toASCIIString();
  }

  @Override
  public final void enrich(HalEnricherContext context, HalAppender appender) {
    if (getReadPermission().map(PermissionCheck::isPermitted).orElse(true)) {
      appender.appendLink(name, getReadLink(baseUriBuilderSupplier));
    }

    if (getWritePermission().map(PermissionCheck::isPermitted).orElse(true)) {
      appender.appendLink(
        "update" + name.substring(0, 1).toUpperCase() + name.substring(1),
        getUpdateLink(baseUriBuilderSupplier)
      );
    }
  }
}
