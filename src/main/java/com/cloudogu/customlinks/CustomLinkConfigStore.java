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

import com.google.common.annotations.VisibleForTesting;
import sonia.scm.store.ConfigurationEntryStore;
import sonia.scm.store.ConfigurationEntryStoreFactory;

import jakarta.inject.Inject;
import java.util.Collection;

public class CustomLinkConfigStore {

  @VisibleForTesting
  public static final String STORE_NAME = "custom-links";

  private final ConfigurationEntryStoreFactory configurationEntryStoreFactory;

  @Inject
  public CustomLinkConfigStore(ConfigurationEntryStoreFactory configurationEntryStoreFactory) {
    this.configurationEntryStoreFactory = configurationEntryStoreFactory;
  }

  public Collection<CustomLink> getAllLinks() {
    return getStore().getAll().values();
  }

  public void addLink(String name, String url) {
    PermissionCheck.checkManageCustomLinks();
    getStore().put(name, new CustomLink(name, url));
  }

  public void removeLink(String name) {
    PermissionCheck.checkManageCustomLinks();
    getStore().remove(name);
  }

  private ConfigurationEntryStore<CustomLink> getStore() {
    return configurationEntryStoreFactory.withType(CustomLink.class).withName(STORE_NAME).build();
  }
}
