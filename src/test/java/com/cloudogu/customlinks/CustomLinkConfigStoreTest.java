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

import org.apache.shiro.authz.AuthorizationException;
import org.github.sdorra.jse.ShiroExtension;
import org.github.sdorra.jse.SubjectAware;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import sonia.scm.store.InMemoryConfigurationEntryStoreFactory;


import java.util.Collection;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(ShiroExtension.class)
class CustomLinkConfigStoreTest {

  private CustomLinkConfigStore configStore;

  @BeforeEach
  void initStoreFactory() {
    configStore = new CustomLinkConfigStore(new InMemoryConfigurationEntryStoreFactory());
  }

  @Test
  void shouldThrowAuthorizationExceptionIfNotPermittedToManageCustomLinks() {
    assertThrows(AuthorizationException.class, () -> configStore.addLink("SCM-Manager", "https://scm-manager.org/"));
    assertThrows(AuthorizationException.class, () -> configStore.removeLink("SCM-Manager"));
  }

  @SubjectAware(value = "trillian", permissions = "configuration:manageCustomLinks")
  @Nested
  class WithPermission {
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
}
