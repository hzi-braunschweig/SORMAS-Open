/*
 * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * Copyright © 2016-2021 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
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

package de.symeda.sormas.backend.importexport;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import de.symeda.sormas.backend.action.Action;
import de.symeda.sormas.backend.region.Country;
import org.hibernate.Session;
import org.postgresql.PGConnection;
import org.postgresql.copy.CopyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.symeda.sormas.api.importexport.DatabaseTable;
import de.symeda.sormas.backend.caze.Case;
import de.symeda.sormas.backend.clinicalcourse.ClinicalCourse;
import de.symeda.sormas.backend.clinicalcourse.ClinicalVisit;
import de.symeda.sormas.backend.clinicalcourse.HealthConditions;
import de.symeda.sormas.backend.common.ConfigFacadeEjb.ConfigFacadeEjbLocal;
import de.symeda.sormas.backend.contact.Contact;
import de.symeda.sormas.backend.customizableenum.CustomizableEnumValue;
import de.symeda.sormas.backend.epidata.EpiData;
import de.symeda.sormas.backend.event.Event;
import de.symeda.sormas.backend.event.EventGroup;
import de.symeda.sormas.backend.event.EventParticipant;
import de.symeda.sormas.backend.exposure.Exposure;
import de.symeda.sormas.backend.facility.Facility;
import de.symeda.sormas.backend.hospitalization.Hospitalization;
import de.symeda.sormas.backend.hospitalization.PreviousHospitalization;
import de.symeda.sormas.backend.location.Location;
import de.symeda.sormas.backend.outbreak.Outbreak;
import de.symeda.sormas.backend.person.Person;
import de.symeda.sormas.backend.person.PersonContactDetail;
import de.symeda.sormas.backend.region.Community;
import de.symeda.sormas.backend.region.District;
import de.symeda.sormas.backend.region.Region;
import de.symeda.sormas.backend.sample.PathogenTest;
import de.symeda.sormas.backend.sample.Sample;
import de.symeda.sormas.backend.symptoms.Symptoms;
import de.symeda.sormas.backend.task.Task;
import de.symeda.sormas.backend.therapy.Prescription;
import de.symeda.sormas.backend.therapy.Therapy;
import de.symeda.sormas.backend.therapy.Treatment;
import de.symeda.sormas.backend.util.ModelConstants;
import de.symeda.sormas.backend.visit.Visit;

/**
 * Exporting data directly from the PostgreSQL database with COPY commands as .csv files.
 * 
 * @author Stefan Kock
 */
@Stateless
@LocalBean
public class DatabaseExportService {

	private static final String COPY_SINGLE_TABLE = "COPY (SELECT * FROM %s) TO STDOUT WITH (FORMAT CSV, DELIMITER '%s', HEADER)";
	private static final String COPY_WITH_JOIN_TABLE =
		"COPY (SELECT * FROM %s AS root_table INNER JOIN %s AS leaf_table ON (root_table.%s = leaf_table.%s)) TO STDOUT WITH (FORMAT CSV, DELIMITER '%s', HEADER)";

	private static final Map<DatabaseTable, DatabaseExportConfiguration> EXPORT_CONFIGS = new LinkedHashMap<>();
	public static final String COUNT_TABLE_COLUMNS = "SELECT COUNT(column_name) FROM information_schema.columns WHERE table_name=:tableName";

	static {
		EXPORT_CONFIGS.put(DatabaseTable.CASES, new DatabaseExportConfiguration(Case.TABLE_NAME));
		EXPORT_CONFIGS.put(DatabaseTable.HOSPITALIZATIONS, new DatabaseExportConfiguration(Hospitalization.TABLE_NAME));
		EXPORT_CONFIGS.put(DatabaseTable.PREVIOUSHOSPITALIZATIONS, new DatabaseExportConfiguration(PreviousHospitalization.TABLE_NAME));
		EXPORT_CONFIGS.put(DatabaseTable.EPIDATA, new DatabaseExportConfiguration(EpiData.TABLE_NAME));
		EXPORT_CONFIGS.put(DatabaseTable.EXPOSURES, new DatabaseExportConfiguration(Exposure.TABLE_NAME));
		EXPORT_CONFIGS.put(DatabaseTable.THERAPIES, new DatabaseExportConfiguration(Therapy.TABLE_NAME));
		EXPORT_CONFIGS.put(DatabaseTable.PRESCRIPTIONS, new DatabaseExportConfiguration(Prescription.TABLE_NAME));
		EXPORT_CONFIGS.put(DatabaseTable.TREATMENTS, new DatabaseExportConfiguration(Treatment.TABLE_NAME));
		EXPORT_CONFIGS.put(DatabaseTable.CLINICAL_COURSES, new DatabaseExportConfiguration(ClinicalCourse.TABLE_NAME));
		EXPORT_CONFIGS.put(DatabaseTable.HEALTH_CONDITIONS, new DatabaseExportConfiguration(HealthConditions.TABLE_NAME));
		EXPORT_CONFIGS.put(DatabaseTable.CLINICAL_VISITS, new DatabaseExportConfiguration(ClinicalVisit.TABLE_NAME));
		EXPORT_CONFIGS.put(DatabaseTable.CONTACTS, new DatabaseExportConfiguration(Contact.TABLE_NAME));
		EXPORT_CONFIGS.put(DatabaseTable.VISITS, new DatabaseExportConfiguration(Visit.TABLE_NAME));
		EXPORT_CONFIGS.put(DatabaseTable.EVENTS, new DatabaseExportConfiguration(Event.TABLE_NAME));
		EXPORT_CONFIGS.put(DatabaseTable.EVENTGROUPS, new DatabaseExportConfiguration(EventGroup.TABLE_NAME));
		EXPORT_CONFIGS.put(DatabaseTable.EVENTPARTICIPANTS, new DatabaseExportConfiguration(EventParticipant.TABLE_NAME));
		EXPORT_CONFIGS.put(DatabaseTable.ACTIONS, new DatabaseExportConfiguration(Action.TABLE_NAME));
		EXPORT_CONFIGS.put(DatabaseTable.SAMPLES, new DatabaseExportConfiguration(Sample.TABLE_NAME));
		EXPORT_CONFIGS.put(DatabaseTable.SAMPLETESTS, new DatabaseExportConfiguration(PathogenTest.TABLE_NAME));
		EXPORT_CONFIGS.put(DatabaseTable.TASKS, new DatabaseExportConfiguration(Task.TABLE_NAME));
		EXPORT_CONFIGS.put(DatabaseTable.PERSONS, new DatabaseExportConfiguration(Person.TABLE_NAME));
		EXPORT_CONFIGS.put(DatabaseTable.PERSON_CONTACT_DETAILS, new DatabaseExportConfiguration(PersonContactDetail.TABLE_NAME));
		EXPORT_CONFIGS.put(DatabaseTable.LOCATIONS, new DatabaseExportConfiguration(Location.TABLE_NAME));
		EXPORT_CONFIGS.put(DatabaseTable.COUNTRIES, new DatabaseExportConfiguration(Country.TABLE_NAME));
		EXPORT_CONFIGS.put(DatabaseTable.REGIONS, new DatabaseExportConfiguration(Region.TABLE_NAME));
		EXPORT_CONFIGS.put(DatabaseTable.DISTRICTS, new DatabaseExportConfiguration(District.TABLE_NAME));
		EXPORT_CONFIGS.put(DatabaseTable.COMMUNITIES, new DatabaseExportConfiguration(Community.TABLE_NAME));
		EXPORT_CONFIGS.put(DatabaseTable.FACILITIES, new DatabaseExportConfiguration(Facility.TABLE_NAME));
		EXPORT_CONFIGS.put(DatabaseTable.OUTBREAKS, new DatabaseExportConfiguration(Outbreak.TABLE_NAME));
		EXPORT_CONFIGS.put(DatabaseTable.CASE_SYMPTOMS, new DatabaseExportConfiguration(Symptoms.TABLE_NAME));
		EXPORT_CONFIGS.put(DatabaseTable.CUSTOMIZABLE_ENUM_VALUES, new DatabaseExportConfiguration(CustomizableEnumValue.TABLE_NAME));
		EXPORT_CONFIGS.put(DatabaseTable.VISIT_SYMPTOMS, new DatabaseExportConfiguration(Symptoms.TABLE_NAME, Visit.TABLE_NAME, "id", "symptoms_id"));
		EXPORT_CONFIGS.put(
			DatabaseTable.CLINICAL_VISIT_SYMPTOMS,
			new DatabaseExportConfiguration(Symptoms.TABLE_NAME, ClinicalVisit.TABLE_NAME, "id", "symptoms_id"));
	}

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@PersistenceContext(unitName = ModelConstants.PERSISTENCE_UNIT_NAME)
	private EntityManager em;

	@EJB
	private ConfigFacadeEjbLocal configFacade;

	public void exportAsCsvFiles(ZipOutputStream zos, List<DatabaseTable> databaseTables) throws IOException {

		//Writer must not be closed so it does not close the zip too early
		Writer writer = new OutputStreamWriter(zos, StandardCharsets.UTF_8);

		// Export all selected tables to .csv files
		for (DatabaseTable databaseTable : databaseTables) {
			zos.putNextEntry(new ZipEntry(databaseTable.getFileName() + ".csv"));
			DatabaseExportConfiguration exportConfig = getConfig(databaseTable);
			addEntityNamesRow(exportConfig, writer);
			addDataRows(databaseTable, exportConfig, writer);
			writer.flush();
			zos.closeEntry();
		}
	}

	private void addEntityNamesRow(DatabaseExportConfiguration config, Writer writer) throws IOException {
		final int mainTableColumnCount = getColumnCount(config.getTableName());
		char csvSeparator = configFacade.getCsvSeparator();
		if (mainTableColumnCount > 0) {
			writer.write(config.getTableName());
		}
		for (int i = 0; i < mainTableColumnCount - 1; i++) {
			writer.write(csvSeparator + config.getTableName());
		}
		if (config.isUseJoinTable()) {
			final int joinTableColumnCount = getColumnCount(config.getJoinTableName());
			for (int i = 0; i < joinTableColumnCount; i++) {
				writer.write(csvSeparator + config.getJoinTableName());
			}
		}
		writer.write('\n');
	}

	private int getColumnCount(String tableName) {
		BigInteger bigIntegerResult = (BigInteger) em.createNativeQuery(COUNT_TABLE_COLUMNS).setParameter("tableName", tableName).getSingleResult();
		return bigIntegerResult.intValue();
	}

	private void addDataRows(DatabaseTable databaseTable, DatabaseExportConfiguration config, Writer writer) {
		long startTime = System.currentTimeMillis();
		final String sql;
		if (config.isUseJoinTable()) {
			sql = String.format(
				COPY_WITH_JOIN_TABLE,
				config.getTableName(),
				config.getJoinTableName(),
				config.getColumnName(),
				config.getJoinColumnName(),
				configFacade.getCsvSeparator());
		} else {
			sql = String.format(COPY_SINGLE_TABLE, config.getTableName(), configFacade.getCsvSeparator());
		}
		writeCsv(writer, sql, databaseTable.getFileName());

		// Be able to check performance for each export query
		logger
			.trace("exportAsCsvFiles(): Exported '{}' in {} ms. sql='{}'", databaseTable.getFileName(), System.currentTimeMillis() - startTime, sql);
	}

	/**
	 * Run an export command and write the result directly into a Writer
	 * 
	 * @param writer
	 * @param sql
	 *            Actual native sql command to copy data to CSV.
	 * @param fileName
	 *            for debugging purposes: Human readable file name similar to selected entry
	 */
	private void writeCsv(Writer writer, String sql, String fileName) {
		/*
		 * Here happens the PostgreSQL specific magic, which is not covered by JPA and
		 * therefore solved with org.postgresql implementations.
		 */
		Session session = em.unwrap(Session.class);
		session.doWork(conn -> {
			PGConnection pgConn = conn.unwrap(PGConnection.class);
			CopyManager copyManager = pgConn.getCopyAPI();
			try {
				copyManager.copyOut(sql, writer);
			} catch (SQLException e) {
				throw new RuntimeException(String.format("Failed to export '%s' with COPY operation", fileName), e);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
	}

	static DatabaseExportConfiguration getConfig(DatabaseTable databaseTable) {

		// leave EXPORT_CONFIGS strictly private to fulfill the expectation to a constant
		return EXPORT_CONFIGS.get(databaseTable);
	}
}
