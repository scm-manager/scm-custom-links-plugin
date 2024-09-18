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

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import org.github.sdorra.jse.ShiroExtension;
import org.github.sdorra.jse.SubjectAware;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.web.JsonMockHttpResponse;
import sonia.scm.web.RestDispatcher;

import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, ShiroExtension.class})
@SubjectAware(value = "trillian")
class CustomLinksResourceTest {

  @Mock
  private CustomLinkConfigStore configStore;

  private RestDispatcher dispatcher;
  private final JsonMockHttpResponse response = new JsonMockHttpResponse();

  @BeforeEach
  void initResource() {
    CustomLinksResource resource = new CustomLinksResource(configStore);

    dispatcher = new RestDispatcher();
    dispatcher.addSingletonResource(resource);
  }

  @Test
  void shouldGetAllLinksWithoutManagePermission() throws URISyntaxException {
    when(configStore.getAllLinks()).thenReturn(ImmutableList.of(new CustomLink("SCM-Manager", "https://scm-manager.org")));

    MockHttpRequest request = MockHttpRequest.get("/" + CustomLinksResource.CUSTOM_LINKS_CONFIG_PATH);

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(200);
    JsonNode mainNode = response.getContentAsJson();
    assertThat(mainNode.path("_links").path("self").path("href").textValue()).isEqualTo("/v2/custom-links");
    assertThat(mainNode.path("_links").path("addLink").isEmpty()).isTrue();
    assertThat(mainNode.path("_embedded").path("customLinks").get(0).path("name").textValue()).isEqualTo("SCM-Manager");
    assertThat(mainNode.path("_embedded").path("customLinks").get(0).path("url").textValue()).isEqualTo("https://scm-manager.org");
    assertThat(mainNode.path("_embedded").path("customLinks").get(0).path("_links").path("delete").isEmpty()).isTrue();
  }

  @Test
  @SubjectAware(permissions = "configuration:manageCustomLinks")
  void shouldGetAllLinksWithManageLinks() throws URISyntaxException {
    when(configStore.getAllLinks()).thenReturn(ImmutableList.of(new CustomLink("SCM-Manager", "https://scm-manager.org")));

    MockHttpRequest request = MockHttpRequest.get("/" + CustomLinksResource.CUSTOM_LINKS_CONFIG_PATH);

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(200);
    JsonNode mainNode = response.getContentAsJson();
    assertThat(mainNode.path("_links").path("self").path("href").textValue()).isEqualTo("/v2/custom-links");
    assertThat(mainNode.path("_links").path("addLink").path("href").textValue()).isEqualTo("/v2/custom-links");
    assertThat(mainNode.path("_embedded").path("customLinks").get(0).path("name").textValue()).isEqualTo("SCM-Manager");
    assertThat(mainNode.path("_embedded").path("customLinks").get(0).path("url").textValue()).isEqualTo("https://scm-manager.org");
    assertThat(mainNode.path("_embedded").path("customLinks").get(0).path("_links").path("delete").path("href").textValue()).isEqualTo("/v2/custom-links/SCM-Manager");
  }

  @Test
  void shouldAddLink() throws URISyntaxException {
    byte[] contentJson = ("{\"name\" : \"SCM-Manager\", \"url\" : \"https://scm-manager.org/\"}").getBytes();

    MockHttpRequest request = MockHttpRequest.post("/" + CustomLinksResource.CUSTOM_LINKS_CONFIG_PATH)
      .contentType(CustomLinksResource.CUSTOM_LINKS_MEDIA_TYPE)
      .content(contentJson);

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(204);
    verify(configStore).addLink("SCM-Manager", "https://scm-manager.org/");
  }

  @Test
  void shouldRemoveLink() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.delete("/" + CustomLinksResource.CUSTOM_LINKS_CONFIG_PATH + "/SCM-Manager")
      .contentType(CustomLinksResource.CUSTOM_LINKS_MEDIA_TYPE);

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(204);
    verify(configStore).removeLink("SCM-Manager");
  }
}
