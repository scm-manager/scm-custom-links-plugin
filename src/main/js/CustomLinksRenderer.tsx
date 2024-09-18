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

import { Link, Links } from "@scm-manager/ui-types";
import React, { FC } from "react";
import { useCustomLinks } from "./useCustomLinks";
import { CustomLink } from "./GlobalConfig";

type Props = {
  links: Links;
};

const CustomLinksRenderer: FC<Props> = ({ links }) => {
  const { data, isLoading } = useCustomLinks((links.customLinks as Link).href);

  if (isLoading) {
    return null;
  }

  return (
    <>
      {(data?._embedded?.customLinks as CustomLink[]).map(cl => (
        <li>
          <a href={cl.url} target="_blank">
            {cl.name}
          </a>
        </li>
      ))}
    </>
  );
};

export default CustomLinksRenderer;
