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
