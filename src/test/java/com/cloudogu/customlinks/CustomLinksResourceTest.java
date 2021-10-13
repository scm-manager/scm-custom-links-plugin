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

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
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
  void shouldGetAllLinks() throws URISyntaxException {
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

    MockHttpRequest request = MockHttpRequest.put("/" + CustomLinksResource.CUSTOM_LINKS_CONFIG_PATH)
      .contentType(CustomLinksResource.CUSTOM_LINKS_MEDIA_TYPE)
      .content(contentJson);

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(204);
    verify(configStore, times(1)).addLink("SCM-Manager", "https://scm-manager.org/");
  }

  @Test
  void shouldRemoveLink() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.delete("/" + CustomLinksResource.CUSTOM_LINKS_CONFIG_PATH + "/SCM-Manager")
      .contentType(CustomLinksResource.CUSTOM_LINKS_MEDIA_TYPE);

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(204);
    verify(configStore, times(1)).removeLink("SCM-Manager");
  }
}
