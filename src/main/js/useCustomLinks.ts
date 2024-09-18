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

import { apiClient } from "@scm-manager/ui-components";
import { HalRepresentation } from "@scm-manager/ui-types";
import { useMutation, useQueryClient, useQuery } from "react-query";
import { CustomLink } from "./GlobalConfig";

export const useCustomLinks = (link: string) => {
  const { error, isLoading, data } = useQuery<HalRepresentation, Error>("custom-links", () =>
    apiClient.get(link).then(res => res.json())
  );

  return {
    error,
    isLoading,
    data
  };
};

export const useDeleteCustomLink = () => {
  const queryClient = useQueryClient();
  const { mutate, isLoading, error } = useMutation<unknown, Error, string>(
    link => {
      return apiClient.delete(link);
    },
    {
      onSuccess: () => {
        return queryClient.invalidateQueries(["custom-links"]);
      }
    }
  );
  return {
    deleteLink: (link: string) => {
      mutate(link);
    },
    isLoading,
    error
  };
};

export const useAddCustomLink = (link: string) => {
  const queryClient = useQueryClient();
  const { mutate, isLoading, error } = useMutation<unknown, Error, CustomLink>(
    (customLink) => {
      return apiClient.post(link, customLink, "application/vnd.scmm-custom-links+json;v=2");
    },
    {
      onSuccess: () => {
        return queryClient.invalidateQueries(["custom-links"]);
      }
    }
  );
  return {
    addLink: (customLink: CustomLink) => {
      mutate(customLink);
    },
    isLoading,
    error
  };
};
