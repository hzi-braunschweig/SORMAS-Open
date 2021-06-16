/*
 * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * Copyright © 2016-2020 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package de.symeda.sormas.backend.sormastosormas.access;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opencsv.CSVReader;

import de.symeda.sormas.api.SormasToSormasConfig;
import de.symeda.sormas.api.utils.CSVUtils;

@Stateless
@LocalBean
public class ServerAccessDataService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServerAccessDataService.class);

	private static final String ORGANIZATION_LIST_FILE_NAME = "organization-list.csv";

	@Inject
	private SormasToSormasConfig sormasToSormasConfig;

	public Optional<OrganizationServerAccessData> getServerAccessData() {

		String configPath = sormasToSormasConfig.getPath();

		if (StringUtils.isEmpty(configPath)) {
			return Optional.empty();
		}

		Path inputFile = Paths.get(configPath, sormasToSormasConfig.getServerAccessDataFileName());

		try (Reader reader = Files.newBufferedReader(inputFile, StandardCharsets.UTF_8);
			CSVReader csvReader = CSVUtils.createCSVReader(reader, ',')) {
			return Optional.of(buildServerAccessData(csvReader.readNext()));

		} catch (Exception e) {
			LOGGER.warn("Unexpected error while reading sormas to sormas server access data", e);
			return Optional.empty();
		}
	}

	public List<OrganizationServerAccessData> getOrganizationList() {
		String configPath = sormasToSormasConfig.getPath();

		if (StringUtils.isEmpty(configPath)) {
			return Collections.emptyList();
		}

		String ownOrganizationId = getServerAccessData().map(OrganizationServerAccessData::getId).orElse(null);
		if (StringUtils.isEmpty(ownOrganizationId)) {
			return Collections.emptyList();
		}

		Path inputFile = Paths.get(configPath, ORGANIZATION_LIST_FILE_NAME);

		try (Reader reader = Files.newBufferedReader(inputFile, StandardCharsets.UTF_8);
			CSVReader csvReader = CSVUtils.createCSVReader(reader, ',')) {
			return csvReader.readAll()
				.stream()
				// skip the empty line and comment lines(starting with #)
				.filter(r -> r.length > 1 || r[0].startsWith("#"))
				// parse non-empty lines
				.map(this::buildServerAccessData)
				.filter(serverAccessData -> !ownOrganizationId.equals(serverAccessData.getId()))
				.collect(Collectors.toList());
		} catch (Exception e) {
			LOGGER.warn("Unexpected error while reading sormas to sormas server list", e);
			return Collections.emptyList();
		}
	}

	public Optional<OrganizationServerAccessData> getServerListItemById(String id) {
		return getOrganizationList().stream().filter(i -> i.getId().equals(id)).findFirst();
	}

	private OrganizationServerAccessData buildServerAccessData(String[] row) {
		OrganizationServerAccessData dto = new OrganizationServerAccessData();
		if (row[0] != null) {
			dto.setId(row[0]);
		}
		if (row[1] != null) {
			dto.setName(row[1]);
		}
		if (row[2] != null) {
			dto.setHostName(row[2]);
		}
		if (row[3] != null) {
			dto.setRestUserPassword(row[3]);
		}

		return dto;
	}
}
