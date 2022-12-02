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
import com.github.sdorra.ssp.PermissionCheck;
import com.google.common.annotations.VisibleForTesting;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
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
import sonia.scm.store.DataStore;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.Optional;

// TODO: Find a way to add the @Extension and @Enrich annotations to the abstract class
@Extension
@Enrich(Index.class)
@OpenAPIDefinition(tags = {
  @Tag(name = "Custom Links", description = "Custom links plugin related endpoints")
})
@Path(CustomLinksResource.PATH)
public class CustomLinksResource extends CollectionResource<CustomLink, CustomLinkDto> implements HalEnricher {

  @VisibleForTesting
  public static final String PATH = "v2/custom-links";

  @VisibleForTesting
  public static final String STORE_NAME = "custom-links";
  @VisibleForTesting
  public static final String MANAGE_PERMISSION = "manageCustomLinks";

  private final ConfigurationEntryStoreFactory configurationEntryStoreFactory;
  private final Provider<ScmPathInfoStore> scmPathInfoStore;

  @Inject
  protected CustomLinksResource(ConfigurationEntryStoreFactory configurationEntryStoreFactory, Provider<ScmPathInfoStore> scmPathInfoStore) {
    this.configurationEntryStoreFactory = configurationEntryStoreFactory;
    this.scmPathInfoStore = scmPathInfoStore;
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

  @Override
  public DataStore<CustomLink> getStore() {
    return configurationEntryStoreFactory
      .withType(CustomLink.class)
      .withName(STORE_NAME) // TODO: Could this be a generic base alternative: CaseFormat.LOWER_CAMEL.converterTo(CaseFormat.LOWER_HYPHEN).convert(getCollectionName()) ?
      .build();
  }

  @Override
  protected String getId(CustomLink entity) {
    return entity.getName();
  }

  @Override
  protected CustomLinkDto map(CustomLink entity, Links links) {
    return CustomLinkDto.from(entity, links);
  }

  @Override
  protected CustomLink map(CustomLinkDto payload) {
    return new CustomLink(payload.getName(), payload.getUrl());
  }

  @Override
  protected Optional<PermissionCheck> getReadPermission() {
    return Optional.empty();
  }

  @Override
  protected Optional<PermissionCheck> getWritePermission() {
    return Optional.of(ConfigurationPermissions.custom(MANAGE_PERMISSION));
  }

  @Override
  protected String getCollectionName() {
    return "customLinks";
  }

  @Override
  public void enrich(HalEnricherContext context, HalAppender appender) {
    createLinks(appender, () -> UriBuilder.fromUri(scmPathInfoStore.get().get().getApiRestUri()));
  }
}
