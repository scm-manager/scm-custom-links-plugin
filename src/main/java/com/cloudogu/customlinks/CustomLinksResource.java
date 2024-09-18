/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package com.cloudogu.customlinks;

import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Link;
import de.otto.edison.hal.Links;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import sonia.scm.api.v2.resources.ErrorDto;
import sonia.scm.security.AllowAnonymousAccess;
import sonia.scm.web.VndMediaType;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.cloudogu.customlinks.CustomLinksResource.CUSTOM_LINKS_CONFIG_PATH;

@OpenAPIDefinition(tags = {
  @Tag(name = "Custom Links", description = "Custom links plugin related endpoints")
})
@Path(CUSTOM_LINKS_CONFIG_PATH)
public class CustomLinksResource {

  public static final String CUSTOM_LINKS_MEDIA_TYPE = VndMediaType.PREFIX + "custom-links" + VndMediaType.SUFFIX;
  public static final String CUSTOM_LINKS_CONFIG_PATH = "v2/custom-links";

  private final CustomLinkConfigStore configStore;

  @Inject
  CustomLinksResource(CustomLinkConfigStore configStore) {
    this.configStore = configStore;
  }

  @GET
  @Path("")
  @Produces(CUSTOM_LINKS_MEDIA_TYPE)
  @Operation(
    summary = "Get all custom links",
    description = "Returns all custom links.",
    tags = "Custom Links",
    operationId = "custom_links_get_all_links"
  )
  @ApiResponse(
    responseCode = "200",
    description = "success",
    content = @Content(
      mediaType = CUSTOM_LINKS_MEDIA_TYPE,
      schema = @Schema(implementation = HalRepresentation.class)
    )
  )
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  @AllowAnonymousAccess
  public HalRepresentation getAllCustomLinks(@Context UriInfo uriInfo) {
    RestAPI restAPI = new RestAPI(uriInfo);
    Collection<CustomLink> customLinks = configStore.getAllLinks();
    List<CustomLinkDto> linkDtos = mapCustomLinksToDtos(restAPI, customLinks);
    return new HalRepresentation(createCollectionLinks(restAPI), Embedded.embedded("customLinks", linkDtos));
  }

  private Links createCollectionLinks(RestAPI restAPI) {
    Links.Builder builder = Links.linkingTo();
      builder.single(Link.link("self", restAPI.customLinks().getAllCustomLinks().asString()));
      if (PermissionCheck.mayManageCustomLinks()) {
        builder.single(Link.link("addLink", restAPI.customLinks().addCustomLink().asString()));
      }

      return builder.build();
  }

  private List<CustomLinkDto> mapCustomLinksToDtos(RestAPI restAPI, Collection<CustomLink> customLinks) {
    return customLinks.stream().map(customLink -> {
      Links.Builder builder = Links.linkingTo();
      if (PermissionCheck.mayManageCustomLinks()) {
        builder.single(Link.link("delete", restAPI.customLinks().deleteCustomLink(customLink.getName()).asString()));
      }
      return CustomLinkDto.from(customLink, builder.build());
    }).collect(Collectors.toList());
  }

  @POST
  @Path("")
  @Consumes(CUSTOM_LINKS_MEDIA_TYPE)
  @Operation(
    summary = "Add single custom link",
    description = "Adds a single custom link.",
    tags = "Custom Links",
    operationId = "custom_links_add_link"
  )
  @ApiResponse(responseCode = "204", description = "no content")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"manageCustomLinks\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public void addCustomLink(@Valid CustomLinkDto customLink) {
    configStore.addLink(customLink.getName(), customLink.getUrl());
  }

  @DELETE
  @Path("{linkName}")
  @Operation(
    summary = "Delete single custom link",
    description = "Deletes a single custom link.",
    tags = "Custom Links",
    operationId = "custom_links_delete_link"
  )
  @ApiResponse(responseCode = "204", description = "delete success or nothing to delete")
  @ApiResponse(responseCode = "401", description = "not authenticated / invalid credentials")
  @ApiResponse(responseCode = "403", description = "not authorized, the current user does not have the \"manageCustomLinks\" privilege")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public void deleteCustomLink(@PathParam("linkName") String linkName) {
    configStore.removeLink(linkName);
  }
}
