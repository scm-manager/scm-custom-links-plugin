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
