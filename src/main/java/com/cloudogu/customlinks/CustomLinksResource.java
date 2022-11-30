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

import com.google.common.annotations.VisibleForTesting;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import sonia.scm.api.v2.resources.ErrorDto;
import sonia.scm.security.AllowAnonymousAccess;
import sonia.scm.store.ConfigurationEntryStore;
import sonia.scm.store.ConfigurationEntryStoreFactory;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.ws.rs.Path;
import javax.ws.rs.core.UriInfo;

import static com.cloudogu.customlinks.CustomLinksResource.CUSTOM_LINKS_CONFIG_PATH;

@OpenAPIDefinition(tags = {
  @Tag(name = "Custom Links", description = "Custom links plugin related endpoints")
})
@Path(CUSTOM_LINKS_CONFIG_PATH)
public class CustomLinksResource extends GlobalCollectionConfigurationResource<CustomLink, CustomLinkDto> {

  public static final String MEDIA_TYPE = VndMediaType.PREFIX + "custom-links" + VndMediaType.SUFFIX;
  public static final String CUSTOM_LINKS_CONFIG_PATH = "v2/custom-links";

  private final CustomLinkConfigStore configStore;

  @VisibleForTesting
  public static final String STORE_NAME = "custom-links";
  private static final String MANAGE_CUSTOM_LINKS = "manageCustomLinks";

  private final ConfigurationEntryStoreFactory configurationEntryStoreFactory;

  @Inject
  protected CustomLinksResource(CustomLinkConfigStore configStore, ConfigurationEntryStoreFactory configurationEntryStoreFactory) {
    this.configStore = configStore;
    this.configurationEntryStoreFactory = configurationEntryStoreFactory;
  }

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
      mediaType = MEDIA_TYPE,
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
  public HalRepresentation getAll(UriInfo uriInfo) {
    return super.getAll(uriInfo);
  }

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
  public void create(CustomLinkDto customLink) {
    configStore.addLink(customLink.getName(), customLink.getUrl());
  }

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
  @Override
  public void delete(String id) {
    super.delete(id);
  }

  @Override
  ConfigurationEntryStore<CustomLink> getConfigStore() {
    return configurationEntryStoreFactory.withType(CustomLink.class).withName(STORE_NAME).build();
  }

  @Override
  String getId(CustomLink entity) {
    return entity.getName();
  }

  @Override
  CustomLinkDto map(CustomLink entity, Links links) {
    return CustomLinkDto.from(entity, links);
  }

  @Override
  String getReadPermission() {
    return null;
  }

  @Override
  String getWritePermission() {
    return MANAGE_CUSTOM_LINKS;
  }

  @Override
  String getCollectionName() {
    return "customLinks";
  }
}
