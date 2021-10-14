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

import React, { FC, useState } from "react";
import {
  AddButton,
  Column,
  ErrorNotification,
  Icon,
  InputField,
  Table,
  TextColumn,
  Title
} from "@scm-manager/ui-components";
import { useTranslation } from "react-i18next";
import { HalRepresentation, Link } from "@scm-manager/ui-types";
import { useAddCustomLink, useCustomLinks, useDeleteCustomLink } from "./useCustomLinks";

type Props = {
  link: string;
};

export type CustomLink = HalRepresentation & {
  name: string;
  url: string;
};

const CustomLinksTable: FC<{ customLinks: CustomLink[] }> = ({ customLinks }) => {
  const [t] = useTranslation("plugins");
  const { deleteLink, error: deleteError } = useDeleteCustomLink();

  return (
    <>
      <Table data={customLinks} emptyMessage={t("scm-custom-links-plugin.form.table.empty")}>
        <TextColumn header={t("scm-custom-links-plugin.form.table.name")} dataKey="name" />
        <TextColumn header={t("scm-custom-links-plugin.form.table.url")} dataKey="url" />
        <Column header={t("")}>
          {(row: any) => (
            <Icon
              name="trash"
              onClick={() => deleteLink((row._links.delete as Link).href)}
              title={t("scm-custom-links-plugin.form.table.deleteLink")}
            />
          )}
        </Column>
      </Table>
      <ErrorNotification error={deleteError} />
    </>
  );
};

const GlobalConfig: FC<Props> = ({ link }) => {
  const [t] = useTranslation("plugins");
  const { data, error, isLoading } = useCustomLinks(link);
  const { addLink, error: addLinkError } = useAddCustomLink(link);

  const [name, setName] = useState("");
  const [url, setUrl] = useState("");

  const reset = () => {
    setName("");
    setUrl("");
  };

  return (
    <>
      <Title title={t("scm-custom-links-plugin.settings.title")} />
      {!isLoading ? <CustomLinksTable customLinks={data?._embedded?.customLinks as CustomLink[]} /> : null}
      <div className="columns is-flex is-align-items-center is-justify-content-space-between">
        <InputField
          label={t("scm-custom-links-plugin.form.name.label")}
          helpText={t("scm-custom-links-plugin.form.name.helpText")}
          value={name}
          onChange={setName}
          className="column mt-1"
        />
        <InputField
          label={t("scm-custom-links-plugin.form.url.label")}
          helpText={t("scm-custom-links-plugin.form.url.helpText")}
          value={url}
          onChange={setUrl}
          className="column mt-1"
        />
        <AddButton
          label={t("scm-custom-links-plugin.form.add")}
          title={t("scm-custom-links-plugin.form.add")}
          className="mt-5"
          disabled={!name || !url}
          action={() => {
            addLink({ name, url, _links: {} });
            reset();
          }}
        />
      </div>
      <ErrorNotification error={addLinkError || error} />
    </>
  );
};

export default GlobalConfig;
