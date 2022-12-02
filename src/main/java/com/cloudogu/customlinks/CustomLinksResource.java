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

import com.cloudogu.resources.CollectionResource;
import com.google.common.annotations.VisibleForTesting;
import de.otto.edison.hal.HalRepresentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import sonia.scm.api.v2.resources.Enrich;
import sonia.scm.api.v2.resources.ErrorDto;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricher;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.api.v2.resources.Index;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.plugin.Extension;
import sonia.scm.security.AllowAnonymousAccess;
import sonia.scm.store.ConfigurationEntryStoreFactory;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.Optional;

@Extension
@Enrich(Index.class)
@OpenAPIDefinition(tags = {
  @Tag(name = "Custom Links", description = "Custom links plugin related endpoints")
})
@Path(CustomLinksResource.PATH)
public class CustomLinksResource extends CollectionResource<CustomLink, CustomLinkDto> {

  @VisibleForTesting
  public static final String PATH = "v2/custom-links";

  @VisibleForTesting
  public static final String STORE_NAME = "custom-links";
  @VisibleForTesting
  public static final String MANAGE_PERMISSION = "manageCustomLinks";

  @VisibleForTesting
  public static final String COLLECTION_NAME = "customLinks";

  @Inject
  protected CustomLinksResource(ConfigurationEntryStoreFactory configurationEntryStoreFactory, Provider<ScmPathInfoStore> scmPathInfoStore) {
    super(
      Optional.empty(),
      Optional.of(ConfigurationPermissions.custom(MANAGE_PERMISSION)),
      (CustomLinkDto payload) -> new CustomLink(payload.getName(), payload.getUrl()),
      CustomLinkDto::from,
      COLLECTION_NAME,
      configurationEntryStoreFactory
        .withType(CustomLink.class)
        .withName(STORE_NAME)::build,
      scmPathInfoStore,
      CustomLink::getName
    );
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
      mediaType = MediaType.APPLICATION_JSON,
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
    super.create(customLink);
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
}
