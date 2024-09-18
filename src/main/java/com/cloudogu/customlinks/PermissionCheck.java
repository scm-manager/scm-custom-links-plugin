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

import sonia.scm.config.ConfigurationPermissions;

public class PermissionCheck {

  private static final String MANAGE_CUSTOM_LINKS = "manageCustomLinks";

  private PermissionCheck() {
  }

  public static boolean mayManageCustomLinks() {
    return ConfigurationPermissions.custom(MANAGE_CUSTOM_LINKS).isPermitted();
  }

  public static void checkManageCustomLinks() {
    ConfigurationPermissions.custom(MANAGE_CUSTOM_LINKS).check();
  }
}
