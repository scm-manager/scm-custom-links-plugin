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

import org.github.sdorra.jse.ShiroExtension;
import org.github.sdorra.jse.SubjectAware;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.web.MockScmPathInfoStore;

import jakarta.inject.Provider;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith({MockitoExtension.class, ShiroExtension.class})
@SubjectAware(value = "trillian")
class IndexLinkEnricherTest {

  private final Provider<ScmPathInfoStore> scmPathInfoStore = MockScmPathInfoStore.forUri("/");
  @Mock
  private HalAppender appender;

  private IndexLinkEnricher enricher;

  @BeforeEach
  void createEnricher() {
    enricher = new IndexLinkEnricher(scmPathInfoStore);
  }

  @Test
  void shouldAppendCustomLinksLink() {
    HalEnricherContext context = HalEnricherContext.of();

    enricher.enrich(context, appender);

    verify(appender).appendLink("customLinks", "/v2/custom-links");
    verify(appender, never()).appendLink(eq("customLinksConfig"), any());
  }

  @Test
  @SubjectAware(permissions = "configuration:manageCustomLinks")
  void shouldAppendCustomLinksConfigLink() {
    HalEnricherContext context = HalEnricherContext.of();

    enricher.enrich(context, appender);

    verify(appender).appendLink("customLinks", "/v2/custom-links");
    verify(appender).appendLink("customLinksConfig", "/v2/custom-links");
  }
}
