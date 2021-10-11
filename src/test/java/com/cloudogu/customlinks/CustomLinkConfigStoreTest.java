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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sonia.scm.store.InMemoryConfigurationEntryStoreFactory;

import java.util.Collection;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class CustomLinkConfigStoreTest {

  private CustomLinkConfigStore configStore;

  @BeforeEach
  void initStoreFactory() {
    configStore = new CustomLinkConfigStore(new InMemoryConfigurationEntryStoreFactory());
  }

  @Test
  void shouldAddLink() {
    configStore.addLink("SCM-Manager", "https://scm-manager.org/");

    assertThat(configStore.getAllLinks()).hasSize(1);
    CustomLink customLink = configStore.getAllLinks().iterator().next();
    assertThat(customLink.getName()).isEqualTo("SCM-Manager");
    assertThat(customLink.getUrl()).isEqualTo("https://scm-manager.org/");
  }

  @Test
  void shouldRemoveLink() {
    configStore.addLink("SCM-Manager", "https://scm-manager.org/");
    assertThat(configStore.getAllLinks()).hasSize(1);

    configStore.removeLink("SCM-Manager");
    assertThat(configStore.getAllLinks()).isEmpty();
  }

  @Test
  void shouldDoNothingIfToBeRemovedLinkDoesNotExist() {
    configStore.addLink("test", "some-link.url");
    configStore.removeLink("SCM-Manager");

    assertThat(configStore.getAllLinks()).hasSize(1);
  }

  @Test
  void shouldGetAllLinks() {
    configStore.addLink("SCM-Manager", "https://scm-manager.org/");
    configStore.addLink("SCM-Manager Community", "https://community.cloudogu.com/");

    Collection<CustomLink> links = configStore.getAllLinks();
    assertThat(links).hasSize(2);
    assertThat(links.stream().map(CustomLink::getUrl).collect(Collectors.toList()))
      .contains("https://community.cloudogu.com/", "https://scm-manager.org/");
  }
}
