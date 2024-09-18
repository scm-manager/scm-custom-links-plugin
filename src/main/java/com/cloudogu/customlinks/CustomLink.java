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

import com.cloudogu.conveyor.GenerateDto;
import com.cloudogu.conveyor.Include;
import com.cloudogu.jaxrstie.GenerateLinkBuilder;

import jakarta.validation.constraints.NotEmpty;

@GenerateDto
@GenerateLinkBuilder(className = "RestAPI")
public class CustomLink {
  @Include
  @NotEmpty
  private String name;
  @Include
  @NotEmpty
  private String url;

  CustomLink() {
  }

  public CustomLink(String name, String url) {
    this.name = name;
    this.url = url;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }
}
