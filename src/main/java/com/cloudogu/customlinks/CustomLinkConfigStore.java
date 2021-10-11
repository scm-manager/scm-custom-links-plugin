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
import sonia.scm.store.ConfigurationEntryStore;
import sonia.scm.store.ConfigurationEntryStoreFactory;

import javax.inject.Inject;
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
    //TODO Manage links permission
    getStore().put(name, new CustomLink(name, url));
  }

  public void removeLink(String name) {
    //TODO Manage links permission
    getStore().remove(name);
  }

  private ConfigurationEntryStore<CustomLink> getStore() {
    return configurationEntryStoreFactory.withType(CustomLink.class).withName(STORE_NAME).build();
  }
}
