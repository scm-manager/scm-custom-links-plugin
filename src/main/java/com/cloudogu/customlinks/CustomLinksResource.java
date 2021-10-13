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

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
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
    return Links.linkingTo()
      .single(Link.link("self", restAPI.customLinks().getAllCustomLinks().asString()))
      .single(Link.link("addLink", restAPI.customLinks().addCustomLink().asString()))
      .build();
  }

  private List<CustomLinkDto> mapCustomLinksToDtos(RestAPI restAPI, Collection<CustomLink> customLinks) {
    return customLinks.stream().map(customLink -> {
      Link deleteLink = Link.link("delete", restAPI.customLinks().deleteCustomLink(customLink.getName()).asString());
      return CustomLinkDto.from(customLink, Links.linkingTo().single(deleteLink).build());
    }).collect(Collectors.toList());
  }

  @PUT
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
