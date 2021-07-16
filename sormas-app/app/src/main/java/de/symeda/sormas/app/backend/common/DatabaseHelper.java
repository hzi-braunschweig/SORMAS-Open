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

package de.symeda.sormas.app.backend.common;

import java.lang.reflect.Array;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import de.symeda.sormas.api.caze.Vaccination;
import de.symeda.sormas.api.epidata.AnimalCondition;
import de.symeda.sormas.api.exposure.AnimalContactType;
import de.symeda.sormas.api.exposure.ExposureType;
import de.symeda.sormas.api.exposure.HabitationType;
import de.symeda.sormas.api.exposure.TypeOfAnimal;
import de.symeda.sormas.api.person.PersonContactDetailType;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.api.utils.YesNoUnknown;
import de.symeda.sormas.app.backend.activityascase.ActivityAsCase;
import de.symeda.sormas.app.backend.activityascase.ActivityAsCaseDao;
import de.symeda.sormas.app.backend.campaign.Campaign;
import de.symeda.sormas.app.backend.campaign.CampaignDao;
import de.symeda.sormas.app.backend.campaign.data.CampaignFormData;
import de.symeda.sormas.app.backend.campaign.data.CampaignFormDataDao;
import de.symeda.sormas.app.backend.campaign.form.CampaignFormMeta;
import de.symeda.sormas.app.backend.campaign.form.CampaignFormMetaDao;
import de.symeda.sormas.app.backend.caze.Case;
import de.symeda.sormas.app.backend.caze.CaseDao;
import de.symeda.sormas.app.backend.caze.maternalhistory.MaternalHistory;
import de.symeda.sormas.app.backend.caze.maternalhistory.MaternalHistoryDao;
import de.symeda.sormas.app.backend.caze.porthealthinfo.PortHealthInfo;
import de.symeda.sormas.app.backend.caze.porthealthinfo.PortHealthInfoDao;
import de.symeda.sormas.app.backend.classification.DiseaseClassificationCriteria;
import de.symeda.sormas.app.backend.classification.DiseaseClassificationCriteriaDao;
import de.symeda.sormas.app.backend.clinicalcourse.ClinicalCourse;
import de.symeda.sormas.app.backend.clinicalcourse.ClinicalCourseDao;
import de.symeda.sormas.app.backend.clinicalcourse.ClinicalVisit;
import de.symeda.sormas.app.backend.clinicalcourse.ClinicalVisitDao;
import de.symeda.sormas.app.backend.clinicalcourse.HealthConditions;
import de.symeda.sormas.app.backend.clinicalcourse.HealthConditionsDao;
import de.symeda.sormas.app.backend.config.Config;
import de.symeda.sormas.app.backend.config.ConfigDao;
import de.symeda.sormas.app.backend.config.ConfigProvider;
import de.symeda.sormas.app.backend.contact.Contact;
import de.symeda.sormas.app.backend.contact.ContactDao;
import de.symeda.sormas.app.backend.customizableenum.CustomizableEnumValue;
import de.symeda.sormas.app.backend.customizableenum.CustomizableEnumValueDao;
import de.symeda.sormas.app.backend.disease.DiseaseConfiguration;
import de.symeda.sormas.app.backend.disease.DiseaseConfigurationDao;
import de.symeda.sormas.app.backend.epidata.EpiData;
import de.symeda.sormas.app.backend.epidata.EpiDataDao;
import de.symeda.sormas.app.backend.event.Event;
import de.symeda.sormas.app.backend.event.EventDao;
import de.symeda.sormas.app.backend.event.EventParticipant;
import de.symeda.sormas.app.backend.event.EventParticipantDao;
import de.symeda.sormas.app.backend.exposure.Exposure;
import de.symeda.sormas.app.backend.exposure.ExposureDao;
import de.symeda.sormas.app.backend.facility.Facility;
import de.symeda.sormas.app.backend.facility.FacilityDao;
import de.symeda.sormas.app.backend.feature.FeatureConfiguration;
import de.symeda.sormas.app.backend.feature.FeatureConfigurationDao;
import de.symeda.sormas.app.backend.hospitalization.Hospitalization;
import de.symeda.sormas.app.backend.hospitalization.HospitalizationDao;
import de.symeda.sormas.app.backend.hospitalization.PreviousHospitalization;
import de.symeda.sormas.app.backend.hospitalization.PreviousHospitalizationDao;
import de.symeda.sormas.app.backend.immunization.Immunization;
import de.symeda.sormas.app.backend.immunization.ImmunizationDao;
import de.symeda.sormas.app.backend.infrastructure.PointOfEntry;
import de.symeda.sormas.app.backend.infrastructure.PointOfEntryDao;
import de.symeda.sormas.app.backend.lbds.LbdsSync;
import de.symeda.sormas.app.backend.lbds.LbdsSyncDao;
import de.symeda.sormas.app.backend.location.Location;
import de.symeda.sormas.app.backend.location.LocationDao;
import de.symeda.sormas.app.backend.outbreak.Outbreak;
import de.symeda.sormas.app.backend.outbreak.OutbreakDao;
import de.symeda.sormas.app.backend.person.Person;
import de.symeda.sormas.app.backend.person.PersonContactDetail;
import de.symeda.sormas.app.backend.person.PersonContactDetailDao;
import de.symeda.sormas.app.backend.person.PersonDao;
import de.symeda.sormas.app.backend.region.Area;
import de.symeda.sormas.app.backend.region.AreaDao;
import de.symeda.sormas.app.backend.region.Community;
import de.symeda.sormas.app.backend.region.CommunityDao;
import de.symeda.sormas.app.backend.region.Continent;
import de.symeda.sormas.app.backend.region.ContinentDao;
import de.symeda.sormas.app.backend.region.Country;
import de.symeda.sormas.app.backend.region.CountryDao;
import de.symeda.sormas.app.backend.region.District;
import de.symeda.sormas.app.backend.region.DistrictDao;
import de.symeda.sormas.app.backend.region.Region;
import de.symeda.sormas.app.backend.region.RegionDao;
import de.symeda.sormas.app.backend.region.Subcontinent;
import de.symeda.sormas.app.backend.region.SubcontinentDao;
import de.symeda.sormas.app.backend.report.AggregateReport;
import de.symeda.sormas.app.backend.report.AggregateReportDao;
import de.symeda.sormas.app.backend.report.WeeklyReport;
import de.symeda.sormas.app.backend.report.WeeklyReportDao;
import de.symeda.sormas.app.backend.report.WeeklyReportEntry;
import de.symeda.sormas.app.backend.report.WeeklyReportEntryDao;
import de.symeda.sormas.app.backend.sample.AdditionalTest;
import de.symeda.sormas.app.backend.sample.AdditionalTestDao;
import de.symeda.sormas.app.backend.sample.PathogenTest;
import de.symeda.sormas.app.backend.sample.PathogenTestDao;
import de.symeda.sormas.app.backend.sample.Sample;
import de.symeda.sormas.app.backend.sample.SampleDao;
import de.symeda.sormas.app.backend.sormastosormas.SormasToSormasOriginInfo;
import de.symeda.sormas.app.backend.sormastosormas.SormasToSormasOriginInfoDao;
import de.symeda.sormas.app.backend.symptoms.Symptoms;
import de.symeda.sormas.app.backend.symptoms.SymptomsDao;
import de.symeda.sormas.app.backend.synclog.SyncLog;
import de.symeda.sormas.app.backend.synclog.SyncLogDao;
import de.symeda.sormas.app.backend.task.Task;
import de.symeda.sormas.app.backend.task.TaskDao;
import de.symeda.sormas.app.backend.therapy.Prescription;
import de.symeda.sormas.app.backend.therapy.PrescriptionDao;
import de.symeda.sormas.app.backend.therapy.Therapy;
import de.symeda.sormas.app.backend.therapy.TherapyDao;
import de.symeda.sormas.app.backend.therapy.Treatment;
import de.symeda.sormas.app.backend.therapy.TreatmentDao;
import de.symeda.sormas.app.backend.user.User;
import de.symeda.sormas.app.backend.user.UserDao;
import de.symeda.sormas.app.backend.user.UserRoleConfig;
import de.symeda.sormas.app.backend.user.UserRoleConfigDao;
import de.symeda.sormas.app.backend.vaccination.VaccinationEntity;
import de.symeda.sormas.app.backend.vaccination.VaccinationEntityDao;
import de.symeda.sormas.app.backend.visit.Visit;
import de.symeda.sormas.app.backend.visit.VisitDao;

/**
 * Database helper class used to manage the creation and upgrading of your database. This class also usually provides
 * the DAOs used by the other classes.
 *
 * @see <a href="http://stackoverflow.com/questions/17529766/view-contents-of-database-file-in-android-studio">Viewing databases from
 *      Android Studio</a>
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

	// name of the database file for your application. Stored in data/data/de.symeda.sormas.app/databases
	public static final String DATABASE_NAME = "sormas.db";
	// any time you make changes to your database objects, you may have to increase the database version

	public static final int DATABASE_VERSION = 310;

	private static DatabaseHelper instance = null;

	public static void init(Context context) {
		if (instance != null) {
			Log.e(DatabaseHelper.class.getName(), "DatabaseHelper has already been initalized");
		}
		instance = new DatabaseHelper(context);
	}

	private boolean clearingTables = false;

	private ConfigDao configDao = null;
	private final Context context;

	private final HashMap<Class<? extends AbstractDomainObject>, AbstractAdoDao<? extends AbstractDomainObject>> adoDaos = new HashMap<>();

	private SyncLogDao syncLogDao = null;

	private LbdsSyncDao lbdsSyncDao = null;

	private DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);//, R.raw.ormlite_config);
		this.context = context;
		// HACK to make sure database is initialized - otherwise we could run into problems caused by threads
		this.getReadableDatabase();
	}

	public static void clearTables(boolean clearInfrastructure) {
		if (instance.clearingTables) {
			return;
		}
		instance.clearingTables = true;

		try {
			ConnectionSource connectionSource = getCaseDao().getConnectionSource();
			TableUtils.clearTable(connectionSource, Case.class);
			TableUtils.clearTable(connectionSource, Immunization.class);
			TableUtils.clearTable(connectionSource, VaccinationEntity.class);
			TableUtils.clearTable(connectionSource, Treatment.class);
			TableUtils.clearTable(connectionSource, Prescription.class);
			TableUtils.clearTable(connectionSource, Therapy.class);
			TableUtils.clearTable(connectionSource, ClinicalVisit.class);
			TableUtils.clearTable(connectionSource, ClinicalCourse.class);
			TableUtils.clearTable(connectionSource, HealthConditions.class);
			TableUtils.clearTable(connectionSource, MaternalHistory.class);
			TableUtils.clearTable(connectionSource, PortHealthInfo.class);
			TableUtils.clearTable(connectionSource, Person.class);
			TableUtils.clearTable(connectionSource, PersonContactDetail.class);
			TableUtils.clearTable(connectionSource, Symptoms.class);
			TableUtils.clearTable(connectionSource, Task.class);
			TableUtils.clearTable(connectionSource, Contact.class);
			TableUtils.clearTable(connectionSource, Visit.class);
			TableUtils.clearTable(connectionSource, Event.class);
			TableUtils.clearTable(connectionSource, Sample.class);
			TableUtils.clearTable(connectionSource, PathogenTest.class);
			TableUtils.clearTable(connectionSource, AdditionalTest.class);
			TableUtils.clearTable(connectionSource, EventParticipant.class);
			TableUtils.clearTable(connectionSource, Hospitalization.class);
			TableUtils.clearTable(connectionSource, PreviousHospitalization.class);
			TableUtils.clearTable(connectionSource, EpiData.class);
			TableUtils.clearTable(connectionSource, Exposure.class);
			TableUtils.clearTable(connectionSource, ActivityAsCase.class);
			TableUtils.clearTable(connectionSource, WeeklyReport.class);
			TableUtils.clearTable(connectionSource, WeeklyReportEntry.class);
			TableUtils.clearTable(connectionSource, AggregateReport.class);
			TableUtils.clearTable(connectionSource, Location.class);
			TableUtils.clearTable(connectionSource, Outbreak.class);
			TableUtils.clearTable(connectionSource, SyncLog.class);
			TableUtils.clearTable(connectionSource, DiseaseClassificationCriteria.class);
			TableUtils.clearTable(connectionSource, CampaignFormData.class);
			TableUtils.clearTable(connectionSource, LbdsSync.class);
			// TODO [vaccination info] integrate vaccination info
//			TableUtils.clearTable(connectionSource, VaccinationInfo.class);

			if (clearInfrastructure) {
				TableUtils.clearTable(connectionSource, User.class);
				TableUtils.clearTable(connectionSource, UserRoleConfig.class);
				TableUtils.clearTable(connectionSource, DiseaseConfiguration.class);
				TableUtils.clearTable(connectionSource, CustomizableEnumValue.class);
				TableUtils.clearTable(connectionSource, FeatureConfiguration.class);
				TableUtils.clearTable(connectionSource, PointOfEntry.class);
				TableUtils.clearTable(connectionSource, Facility.class);
				TableUtils.clearTable(connectionSource, Community.class);
				TableUtils.clearTable(connectionSource, District.class);
				TableUtils.clearTable(connectionSource, Continent.class);
				TableUtils.clearTable(connectionSource, Subcontinent.class);
				TableUtils.clearTable(connectionSource, Country.class);
				TableUtils.clearTable(connectionSource, Region.class);
				TableUtils.clearTable(connectionSource, Area.class);
				TableUtils.clearTable(connectionSource, Campaign.class);
				TableUtils.clearTable(connectionSource, CampaignFormMeta.class);

				ConfigProvider.init(instance.context);
			}

			// keep config!
			//TableUtils.clearTable(connectionSource, Config.class);
		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Can't clear database", e);
			throw new RuntimeException(e);
		} finally {
			instance.clearingTables = false;
		}
	}

	/**
	 * This is called when the database is first created. Usually you should call createTable statements here to build
	 * the tables that will store your data.
	 */
	@Override
	public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
		try {
			Log.i(DatabaseHelper.class.getName(), "onCreate");
			TableUtils.createTableIfNotExists(connectionSource, Config.class);
			TableUtils.createTable(connectionSource, Location.class);
			TableUtils.createTable(connectionSource, Continent.class);
			TableUtils.createTable(connectionSource, Subcontinent.class);
			TableUtils.createTable(connectionSource, Country.class);
			TableUtils.createTable(connectionSource, Area.class);
			TableUtils.createTable(connectionSource, Region.class);
			TableUtils.createTable(connectionSource, District.class);
			TableUtils.createTable(connectionSource, Community.class);
			TableUtils.createTable(connectionSource, Facility.class);
			TableUtils.createTable(connectionSource, PointOfEntry.class);
			TableUtils.createTable(connectionSource, UserRoleConfig.class);
			TableUtils.createTable(connectionSource, DiseaseConfiguration.class);
			TableUtils.createTable(connectionSource, CustomizableEnumValue.class);
			TableUtils.createTable(connectionSource, FeatureConfiguration.class);
			TableUtils.createTable(connectionSource, User.class);
			TableUtils.createTable(connectionSource, Person.class);
			TableUtils.createTable(connectionSource, PersonContactDetail.class);
			TableUtils.createTable(connectionSource, Case.class);
			TableUtils.createTable(connectionSource, Immunization.class);
			TableUtils.createTable(connectionSource, VaccinationEntity.class);
			TableUtils.createTable(connectionSource, Symptoms.class);
			TableUtils.createTable(connectionSource, Therapy.class);
			TableUtils.createTable(connectionSource, Prescription.class);
			TableUtils.createTable(connectionSource, Treatment.class);
			TableUtils.createTable(connectionSource, HealthConditions.class);
			TableUtils.createTable(connectionSource, ClinicalCourse.class);
			TableUtils.createTable(connectionSource, ClinicalVisit.class);
			TableUtils.createTable(connectionSource, MaternalHistory.class);
			TableUtils.createTable(connectionSource, PortHealthInfo.class);
			TableUtils.createTable(connectionSource, Contact.class);
			TableUtils.createTable(connectionSource, Visit.class);
			TableUtils.createTable(connectionSource, Task.class);
			TableUtils.createTable(connectionSource, Event.class);
			TableUtils.createTable(connectionSource, Sample.class);
			TableUtils.createTable(connectionSource, PathogenTest.class);
			TableUtils.createTable(connectionSource, AdditionalTest.class);
			TableUtils.createTable(connectionSource, EventParticipant.class);
			TableUtils.createTable(connectionSource, Hospitalization.class);
			TableUtils.createTable(connectionSource, PreviousHospitalization.class);
			TableUtils.createTable(connectionSource, EpiData.class);
			TableUtils.createTable(connectionSource, Exposure.class);
			TableUtils.createTable(connectionSource, ActivityAsCase.class);
			TableUtils.createTable(connectionSource, SyncLog.class);
			TableUtils.createTable(connectionSource, WeeklyReport.class);
			TableUtils.createTable(connectionSource, WeeklyReportEntry.class);
			TableUtils.createTable(connectionSource, AggregateReport.class);
			TableUtils.createTable(connectionSource, Outbreak.class);
			TableUtils.createTable(connectionSource, DiseaseClassificationCriteria.class);
			TableUtils.createTable(connectionSource, SormasToSormasOriginInfo.class);
			TableUtils.createTable(connectionSource, Campaign.class);
			TableUtils.createTable(connectionSource, CampaignFormData.class);
			TableUtils.createTable(connectionSource, CampaignFormMeta.class);
			TableUtils.createTable(connectionSource, LbdsSync.class);
			// TODO [vaccination info] integrate vaccination info
//			TableUtils.createTable(connectionSource, VaccinationInfo.class);
		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Can't build database", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * This is called when your application is upgraded and it has a higher version number. This allows you to adjust
	 * the various data to match the new version number.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {

		if (oldVersion < 91) {
			upgradeFromUnupgradableVersion(db, connectionSource, oldVersion);
			return;
		}

		// see http://ormlite.com/javadoc/ormlite-core/doc-files/ormlite_4.html#Upgrading-Schema
		// IMPORTANT: table and column names are CASE SENSITIVE!
		int currentVersion = oldVersion;
		try {
			switch (oldVersion) {
			case 91:
				currentVersion = 91;
				getDao(District.class).executeRaw("ALTER TABLE district ADD COLUMN epidCode varchar(255);");
			case 92:
				currentVersion = 92;
				getDao(District.class).executeRaw("ALTER TABLE region ADD COLUMN epidCode varchar(255);");
			case 93:
				currentVersion = 93;
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN symptomatic boolean;");
			case 94:
				currentVersion = 94;
				// nothing
			case 95:
				currentVersion = 95;
				getDao(Sample.class).executeRaw("ALTER TABLE samples ADD COLUMN referredTo_id bigint REFERENCES samples(id);");
			case 96:
				currentVersion = 96;
				getDao(Sample.class).executeRaw("ALTER TABLE samples ADD COLUMN shipped boolean;");
				getDao(Sample.class).executeRaw("ALTER TABLE samples ADD COLUMN received boolean;");
				getDao(Sample.class).executeRaw(
					"UPDATE samples SET shipped='true' WHERE shipmentStatus = 'SHIPPED' OR shipmentStatus = 'RECEIVED' OR shipmentStatus = 'REFERRED_OTHER_LAB';");
				getDao(Sample.class)
					.executeRaw("UPDATE samples SET received='true' WHERE shipmentStatus = 'RECEIVED' OR shipmentStatus = 'REFERRED_OTHER_LAB';");
				getDao(Sample.class).executeRaw("UPDATE samples SET shipped='false' WHERE shipmentStatus = 'NOT_SHIPPED';");
				getDao(Sample.class)
					.executeRaw("UPDATE samples SET received='false' WHERE shipmentStatus = 'NOT_SHIPPED' OR shipmentStatus = 'SHIPPED';");
			case 97:
				currentVersion = 97;
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN reportLat float8;");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN reportLon float8;");
				getDao(Contact.class).executeRaw("ALTER TABLE contacts ADD COLUMN reportLat float8;");
				getDao(Contact.class).executeRaw("ALTER TABLE contacts ADD COLUMN reportLon float8;");
				getDao(Event.class).executeRaw("ALTER TABLE events ADD COLUMN reportLat float8;");
				getDao(Event.class).executeRaw("ALTER TABLE events ADD COLUMN reportLon float8;");
				getDao(Visit.class).executeRaw("ALTER TABLE visits ADD COLUMN reportLat float8;");
				getDao(Visit.class).executeRaw("ALTER TABLE visits ADD COLUMN reportLon float8;");
				getDao(Task.class).executeRaw("ALTER TABLE tasks ADD COLUMN closedLat float8;");
				getDao(Task.class).executeRaw("ALTER TABLE tasks ADD COLUMN closedLon float8;");
			case 98:
				currentVersion = 98;
				// nothing
			case 99:
				currentVersion = 99;
				getDao(Contact.class).executeRaw("ALTER TABLE contacts ADD COLUMN followUpComment varchar(512);");
			case 100:
				currentVersion = 100;
				// nothing
			case 101:
				currentVersion = 101;
				getDao(Facility.class).executeRaw("UPDATE facility SET name = 'Other health facility' WHERE uuid = 'SORMAS-CONSTID-OTHERS-FACILITY'");
				getDao(Facility.class).executeRaw("UPDATE facility SET name = 'Not a health facility' WHERE uuid = 'SORMAS-CONSTID-ISNONE-FACILITY'");
			case 102:
				currentVersion = 102;
				getDao(PreviousHospitalization.class)
					.executeRaw("ALTER TABLE previoushospitalizations ADD COLUMN region_id bigint REFERENCES region (id);");
				getDao(PreviousHospitalization.class)
					.executeRaw("ALTER TABLE previoushospitalizations ADD COLUMN district_id bigint REFERENCES district (id);");
				getDao(PreviousHospitalization.class)
					.executeRaw("ALTER TABLE previoushospitalizations ADD COLUMN community_id bigint REFERENCES community (id);");
				getDao(PreviousHospitalization.class).executeRaw(
					"UPDATE previoushospitalizations SET region_id = (SELECT region_id FROM facility WHERE facility.id = previoushospitalizations.healthfacility_id);");
				getDao(PreviousHospitalization.class).executeRaw(
					"UPDATE previoushospitalizations SET district_id = (SELECT district_id FROM facility WHERE facility.id = previoushospitalizations.healthfacility_id);");
				getDao(PreviousHospitalization.class).executeRaw(
					"UPDATE previoushospitalizations SET community_id = (SELECT community_id FROM facility WHERE facility.id = previoushospitalizations.healthfacility_id);");
				// Set region, district and community to the values of the case for 'Other' and 'None' health facilities
				getDao(PreviousHospitalization.class).executeRaw(
					"UPDATE previoushospitalizations SET region_id = (SELECT region_id FROM cases WHERE cases.hospitalization_id = previoushospitalizations.hospitalization_id) WHERE region_id IS NULL;");
				getDao(PreviousHospitalization.class).executeRaw(
					"UPDATE previoushospitalizations SET district_id = (SELECT district_id FROM cases WHERE cases.hospitalization_id = previoushospitalizations.hospitalization_id) WHERE district_id IS NULL;");
				getDao(PreviousHospitalization.class).executeRaw(
					"UPDATE previoushospitalizations SET community_id = (SELECT community_id FROM cases WHERE cases.hospitalization_id = previoushospitalizations.hospitalization_id) WHERE community_id IS NULL;");
			case 103:
				currentVersion = 103;
				getDao(Facility.class).executeRaw("UPDATE facility SET name = 'OTHER_FACILITY' WHERE uuid = 'SORMAS-CONSTID-OTHERS-FACILITY'");
				getDao(Facility.class).executeRaw("UPDATE facility SET name = 'NO_FACILITY' WHERE uuid = 'SORMAS-CONSTID-ISNONE-FACILITY'");
			case 104:
				currentVersion = 104;
				getDao(WeeklyReport.class).executeRaw(
					"CREATE TABLE weeklyreport(" + "id integer primary key autoincrement," + "uuid varchar(36) not null unique,"
						+ "changeDate timestamp not null," + "creationDate timestamp not null," + "lastOpenedDate timestamp,"
						+ "localChangeDate timestamp not null," + "modified integer," + "snapshot integer,"
						+ "healthFacility_id bigint not null REFERENCES facility(id)," + "informant_id bigint not null REFERENCES users(id),"
						+ "reportDateTime timestamp not null," + "totalNumberOfCases integer not null" + ");");
				getDao(WeeklyReportEntry.class).executeRaw(
					"CREATE TABLE weeklyreportentry(" + "id integer primary key autoincrement," + "uuid varchar(36) not null unique,"
						+ "changeDate timestamp not null," + "creationDate timestamp not null," + "lastOpenedDate timestamp,"
						+ "localChangeDate timestamp not null," + "modified integer," + "snapshot integer,"
						+ "weeklyReport_id bigint not null REFERENCES weeklyreport(id)," + "disease character varying(255) not null,"
						+ "numberOfCases integer not null" + ");");
			case 105:
				currentVersion = 105;
				getDao(WeeklyReport.class).executeRaw("ALTER TABLE weeklyreport ADD COLUMN year integer NOT NULL DEFAULT 1900;");
				getDao(WeeklyReport.class).executeRaw("ALTER TABLE weeklyreport ADD COLUMN epiWeek integer NOT NULL DEFAULT l;");
			case 106:
				currentVersion = 106;
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN yellowFeverVaccination varchar(255);");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN yellowFeverVaccinationInfoSource varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN backache varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN eyesBleeding varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN jaundice varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN darkUrine varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN stomachBleeding varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN rapidBreathing varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN swollenGlands varchar(255);");
			case 107:
				currentVersion = 107;
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN diseaseDetails varchar(512);");
				getDao(Event.class).executeRaw("ALTER TABLE events ADD COLUMN diseaseDetails varchar(512);");
			case 108:
				currentVersion = 108;
				getDao(Location.class).executeRaw("ALTER TABLE location ADD COLUMN latLonAccuracy real;");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN reportLatLonAccuracy real;");
				getDao(Contact.class).executeRaw("ALTER TABLE contacts ADD COLUMN reportLatLonAccuracy real;");
				getDao(Event.class).executeRaw("ALTER TABLE events ADD COLUMN reportLatLonAccuracy real;");
				getDao(Visit.class).executeRaw("ALTER TABLE visits ADD COLUMN reportLatLonAccuracy real;");
				getDao(Task.class).executeRaw("ALTER TABLE tasks ADD COLUMN closedLatLonAccuracy real;");
				getDao(Sample.class).executeRaw("ALTER TABLE samples ADD COLUMN reportLat double precision;");
				getDao(Sample.class).executeRaw("ALTER TABLE samples ADD COLUMN reportLon double precision;");
				getDao(Sample.class).executeRaw("ALTER TABLE samples ADD COLUMN reportLatLonAccuracy real;");
			case 109:
				currentVersion = 109;
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN smallpoxVaccinationScar varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN cutaneousEruption varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN lesions varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN lesionsSameState varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN lesionsSameSize varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN lesionsDeepProfound varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN lesionsFace boolean;");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN lesionsLegs boolean;");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN lesionsSolesFeet boolean;");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN lesionsPalmsHands boolean;");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN lesionsThorax boolean;");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN lesionsArms boolean;");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN lesionsGenitals boolean;");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN lesionsAllOverBody boolean;");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN lesionsResembleImg1 varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN lesionsResembleImg2 varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN lesionsResembleImg3 varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN lesionsResembleImg4 varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN lymphadenopathyInguinal varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN lymphadenopathyAxillary varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN lymphadenopathyCervical varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN chillsSweats varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN lesionsThatItch varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN bedridden varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN oralUlcers varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN patientIllLocation varchar(512);");
				getDao(EpiData.class).executeRaw("ALTER TABLE epidata ADD COLUMN dateOfLastExposure timestamp;");
				getDao(EpiData.class).executeRaw("ALTER TABLE epidata ADD COLUMN placeOfLastExposure varchar(512);");
				getDao(EpiData.class).executeRaw("ALTER TABLE epidata ADD COLUMN animalCondition varchar(255);");
			case 110:
				currentVersion = 110;
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN painfulLymphadenitis varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN buboesGroinArmpitNeck varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN blackeningDeathOfTissue varchar(255);");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN plagueType varchar(255);");
				getDao(EpiData.class).executeRaw("ALTER TABLE epidata ADD COLUMN fleaBite varchar(255);");
			case 111:
				currentVersion = 111;
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN smallpoxVaccinationReceived varchar(255);");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN smallpoxVaccinationDate timestamp;");
			case 112:
				currentVersion = 112;
				getDao(Event.class).executeRaw("ALTER TABLE events RENAME TO tmp_events;");
				getDao(Event.class).executeRaw(
					"CREATE TABLE events (disease VARCHAR, diseaseDetails VARCHAR, eventDate BIGINT, eventDesc VARCHAR, eventLocation_id BIGINT, eventStatus VARCHAR, eventType VARCHAR, reportDateTime BIGINT, reportLat DOUBLE PRECISION, reportLatLonAccuracy FLOAT, reportLon DOUBLE PRECISION, reportingUser_id BIGINT, srcEmail VARCHAR, srcFirstName VARCHAR, srcLastName VARCHAR, srcTelNo VARCHAR, surveillanceOfficer_id BIGINT, typeOfPlace VARCHAR, typeOfPlaceText VARCHAR, changeDate BIGINT NOT NULL, creationDate BIGINT NOT NULL, id INTEGER PRIMARY KEY AUTOINCREMENT, lastOpenedDate BIGINT, localChangeDate BIGINT NOT NULL, modified SMALLINT, snapshot SMALLINT, uuid VARCHAR NOT NULL, UNIQUE(snapshot, uuid));");
				getDao(Event.class).executeRaw(
					"INSERT INTO events(disease, diseaseDetails, eventDate, eventDesc, eventLocation_id, eventStatus, eventType, reportDateTime, reportLat, reportLatLonAccuracy, reportLon, reportingUser_id, srcEmail, srcFirstName, srcLastName, srcTelNo, surveillanceOfficer_id, typeOfPlace, typeOfPlaceText, changeDate, creationDate, id, lastOpenedDate, localChangeDate, modified, snapshot, uuid) "
						+ "SELECT disease, diseaseDetails, eventDate, eventDesc, eventLocation_id, eventStatus, eventType, reportDateTime, reportLat, reportLatLonAccuracy, reportLon, reportingUser_id, srcEmail, srcFirstName, srcLastName, srcTelNo, surveillanceOfficer_id, typeOfPlace, typeOfPlaceText, changeDate, creationDate, id, lastOpenedDate, localChangeDate, modified, snapshot, uuid "
						+ "FROM tmp_events;");
				getDao(Event.class).executeRaw("DROP TABLE tmp_events;");
			case 113:
				currentVersion = 113;
				getDao(User.class).executeRaw("UPDATE users SET userRole = '[\"' || userRole || '\"]';");
			case 114:
				// Re-creation of the events table has to be done again because of fractions of the not-null constraints not being removed properly
				currentVersion = 114;
				getDao(Event.class).executeRaw("ALTER TABLE events RENAME TO tmp_events;");
				getDao(Event.class).executeRaw(
					"CREATE TABLE events (disease VARCHAR, diseaseDetails VARCHAR, eventDate BIGINT, eventDesc VARCHAR, eventLocation_id BIGINT, eventStatus VARCHAR, eventType VARCHAR, reportDateTime BIGINT, reportLat DOUBLE PRECISION, reportLatLonAccuracy FLOAT, reportLon DOUBLE PRECISION, reportingUser_id BIGINT, srcEmail VARCHAR, srcFirstName VARCHAR, srcLastName VARCHAR, srcTelNo VARCHAR, surveillanceOfficer_id BIGINT, typeOfPlace VARCHAR, typeOfPlaceText VARCHAR, changeDate BIGINT NOT NULL, creationDate BIGINT NOT NULL, id INTEGER PRIMARY KEY AUTOINCREMENT, lastOpenedDate BIGINT, localChangeDate BIGINT NOT NULL, modified SMALLINT, snapshot SMALLINT, uuid VARCHAR NOT NULL, UNIQUE(snapshot, uuid));");
				getDao(Event.class).executeRaw(
					"INSERT INTO events(disease, diseaseDetails, eventDate, eventDesc, eventLocation_id, eventStatus, eventType, reportDateTime, reportLat, reportLatLonAccuracy, reportLon, reportingUser_id, srcEmail, srcFirstName, srcLastName, srcTelNo, surveillanceOfficer_id, typeOfPlace, typeOfPlaceText, changeDate, creationDate, id, lastOpenedDate, localChangeDate, modified, snapshot, uuid) "
						+ "SELECT disease, diseaseDetails, eventDate, eventDesc, eventLocation_id, eventStatus, eventType, reportDateTime, reportLat, reportLatLonAccuracy, reportLon, reportingUser_id, srcEmail, srcFirstName, srcLastName, srcTelNo, surveillanceOfficer_id, typeOfPlace, typeOfPlaceText, changeDate, creationDate, id, lastOpenedDate, localChangeDate, modified, snapshot, uuid "
						+ "FROM tmp_events;");
				getDao(Event.class).executeRaw("DROP TABLE tmp_events;");
			case 115:
				currentVersion = 115;
				getDao(Person.class).executeRaw("ALTER TABLE person ADD COLUMN causeOfDeath varchar(255);");
				getDao(Person.class).executeRaw("ALTER TABLE person ADD COLUMN causeOfDeathDetails varchar(512);");
				getDao(Person.class).executeRaw("ALTER TABLE person ADD COLUMN causeOfDeathDisease varchar(255);");
				getDao(Person.class).executeRaw("ALTER TABLE person ADD COLUMN causeOfDeathDiseaseDetails varchar(512);");
			case 116:
				currentVersion = 116;
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN outcome varchar(255);");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN outcomeDate timestamp without time zone;");
			case 117:
				currentVersion = 117;
				getDao(Hospitalization.class).executeRaw("ALTER TABLE hospitalizations ADD COLUMN admittedToHealthFacility varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN bulgingFontanelle varchar(255);");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN vaccination varchar(255);");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN vaccinationDoses varchar(512);");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN vaccinationInfoSource varchar(255);");

				getDao(Case.class).executeRaw("UPDATE cases SET vaccination = measlesVaccination WHERE measlesVaccination IS NOT NULL;");
				getDao(Case.class).executeRaw("UPDATE cases SET vaccination = yellowFeverVaccination WHERE yellowFeverVaccination IS NOT NULL;");
				getDao(Case.class).executeRaw("UPDATE cases SET vaccination = measlesDoses WHERE measlesDoses IS NOT NULL;");
				getDao(Case.class)
					.executeRaw("UPDATE cases SET vaccination = measlesVaccinationInfoSource WHERE measlesVaccinationInfoSource IS NOT NULL;");
				getDao(Case.class).executeRaw(
					"UPDATE cases SET vaccination = yellowFeverVaccinationInfoSource WHERE yellowFeverVaccinationInfoSource IS NOT NULL;");
			case 118:
				currentVersion = 118;
				getDao(Outbreak.class).executeRaw(
					"CREATE TABLE outbreak(" + "id integer primary key autoincrement," + "uuid varchar(36) not null unique,"
						+ "changeDate timestamp not null," + "creationDate timestamp not null," + "district_id bigint REFERENCES district(id),"
						+ "disease varchar(255)," + "reportDate timestamp," + "reportingUser_id bigint REFERENCES users(id),"
						+ "lastOpenedDate timestamp," + "localChangeDate timestamp not null," + "modified integer," + "snapshot integer);");
			case 119:
				currentVersion = 119;
				getDao(Contact.class).executeRaw("ALTER TABLE contacts ADD COLUMN contactStatus varchar(255);");
				getDao(Contact.class)
					.executeRaw("UPDATE contacts SET contactClassification = 'UNCONFIRMED' where contactClassification = 'POSSIBLE';");
				getDao(Contact.class).executeRaw("UPDATE contacts SET contactStatus = 'DROPPED' where contactClassification = 'DROPPED';");
				getDao(Contact.class).executeRaw("UPDATE contacts SET contactStatus = 'DROPPED' where contactClassification = 'NO_CONTACT';");
				getDao(Contact.class).executeRaw("UPDATE contacts SET contactStatus = 'CONVERTED' where contactClassification = 'CONVERTED';");
				getDao(Contact.class).executeRaw(
					"UPDATE contacts SET contactStatus = 'ACTIVE' where contactClassification = 'UNCONFIRMED' or contactClassification = 'CONFIRMED';");
				getDao(Contact.class).executeRaw(
					"UPDATE contacts SET contactClassification = 'CONFIRMED' where contactClassification = 'CONVERTED' or contactClassification = 'DROPPED';");
			case 120:
				currentVersion = 120;
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN vaccinationDate timestamp;");
				getDao(Case.class).executeRaw("UPDATE cases SET vaccinationDate = smallpoxVaccinationDate;");
			case 121:
				currentVersion = 121;
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN lesionsOnsetDate timestamp;");
			case 122:
				currentVersion = 122;
				getDao(Contact.class).executeRaw("ALTER TABLE contacts ADD COLUMN resultingCaseUuid varchar(36);");
				getDao(EventParticipant.class).executeRaw("ALTER TABLE eventParticipants ADD COLUMN resultingCaseUuid varchar(36);");
			case 123:
				currentVersion = 123;
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN dengueFeverType varchar(255);");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN classificationDate timestamp;");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN classificationComment varchar(512);");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN classificationUser_id bigint REFERENCES users(id);");
				getDao(Case.class).executeRaw("UPDATE cases SET disease = 'NEW_INFLUENCA' where disease = 'AVIAN_INFLUENCA';");
				getDao(Event.class).executeRaw("UPDATE events SET disease = 'NEW_INFLUENCA' where disease = 'AVIAN_INFLUENCA';");
				getDao(Outbreak.class).executeRaw("UPDATE outbreak SET disease = 'NEW_INFLUENCA' where disease = 'AVIAN_INFLUENCA';");
				getDao(Person.class)
					.executeRaw("UPDATE person SET causeofdeathdisease = 'NEW_INFLUENCA' where causeofdeathdisease = 'AVIAN_INFLUENCA';");
				getDao(Visit.class).executeRaw("UPDATE visits SET disease = 'NEW_INFLUENCA' where disease = 'AVIAN_INFLUENCA';");
				getDao(WeeklyReportEntry.class)
					.executeRaw("UPDATE weeklyreportentry SET disease = 'NEW_INFLUENCA' where disease = 'AVIAN_INFLUENCA';");
			case 124:
				currentVersion = 124;
				getDao(Person.class).executeRaw("ALTER TABLE person ADD COLUMN occupationFacilityDetails varchar(512);");
				getDao(Person.class).executeRaw("ALTER TABLE person ADD COLUMN occupationRegion_id bigint REFERENCES region(id);");
				getDao(Person.class).executeRaw("ALTER TABLE person ADD COLUMN occupationDistrict_id bigint REFERENCES district(id);");
				getDao(Person.class).executeRaw("ALTER TABLE person ADD COLUMN occupationCommunity_id bigint REFERENCES community(id);");
				getDao(Person.class).executeRaw(
					"UPDATE person SET occupationRegion_id = (SELECT region_id FROM facility WHERE facility.id = person.occupationFacility_id) WHERE occupationFacility_id IS NOT NULL;");
				getDao(Person.class).executeRaw(
					"UPDATE person SET occupationDistrict_id = (SELECT district_id FROM facility WHERE facility.id = person.occupationFacility_id) WHERE occupationFacility_id IS NOT NULL;");
				getDao(Person.class).executeRaw(
					"UPDATE person SET occupationCommunity_id = (SELECT community_id FROM facility WHERE facility.id = person.occupationFacility_id) WHERE occupationFacility_id IS NOT NULL;");
				getDao(PreviousHospitalization.class)
					.executeRaw("ALTER TABLE previoushospitalizations ADD COLUMN healthFacilityDetails varchar(512);");
			case 125:
				currentVersion = 125;
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN meningealSigns varchar(255);");
			case 126:
				currentVersion = 126;
				getDao(PathogenTest.class)
					.executeRaw("UPDATE sampleTests SET testType = 'IGM_SERUM_ANTIBODY' WHERE testType = 'SERUM_ANTIBODY_TITER';");
				getDao(PathogenTest.class).executeRaw("UPDATE sampleTests SET testType = 'IGM_SERUM_ANTIBODY' WHERE testType = 'ELISA';");
				getDao(PathogenTest.class)
					.executeRaw("UPDATE sampleTests SET testType = 'PCR_RT_PCR' WHERE testType = 'PCR' OR testType = 'RT_PCR';");
				getDao(Sample.class)
					.executeRaw("UPDATE samples SET suggestedTypeOfTest = 'IGM_SERUM_ANTIBODY' WHERE suggestedTypeOfTest = 'SERUM_ANTIBODY_TITER';");
				getDao(Sample.class).executeRaw("UPDATE samples SET suggestedTypeOfTest = 'IGM_SERUM_ANTIBODY' WHERE suggestedTypeOfTest = 'ELISA';");
				getDao(Sample.class).executeRaw(
					"UPDATE samples SET suggestedTypeOfTest = 'PCR_RT_PCR' WHERE suggestedTypeOfTest = 'PCR' OR suggestedTypeOfTest = 'RT_PCR';");
				getDao(Sample.class).executeRaw("ALTER TABLE samples ADD COLUMN labDetails varchar(512);");
			case 127:
				currentVersion = 127;
				getDao(EpiData.class).executeRaw("ALTER TABLE epidata ADD COLUMN directContactConfirmedCase varchar(255);");
				getDao(EpiData.class).executeRaw("ALTER TABLE epidata ADD COLUMN directContactProbableCase varchar(255);");
				getDao(EpiData.class).executeRaw("ALTER TABLE epidata ADD COLUMN closeContactProbableCase varchar(255);");
				getDao(EpiData.class).executeRaw("ALTER TABLE epidata ADD COLUMN areaConfirmedCases varchar(255);");
				getDao(EpiData.class).executeRaw("ALTER TABLE epidata ADD COLUMN processingConfirmedCaseFluidUnsafe varchar(255);");
				getDao(EpiData.class).executeRaw("ALTER TABLE epidata ADD COLUMN percutaneousCaseBlood varchar(255);");
				getDao(EpiData.class).executeRaw(
					"UPDATE epidata SET poultryDetails=null, poultry=null,"
						+ " wildbirds=null, wildbirdsDetails=null, wildbirdsDate=null, wildbirdsLocation=null;");
			case 128:
				currentVersion = 128;
				getDao(Contact.class).executeRaw("ALTER TABLE contacts ADD COLUMN resultingCaseUser_id bigint REFERENCES users(id);");
				getDao(Contact.class).executeRaw("ALTER TABLE contacts ADD COLUMN caseUuid VARCHAR;");
				getDao(Contact.class).executeRaw("ALTER TABLE contacts ADD COLUMN caseDisease VARCHAR;");
				getDao(Contact.class).executeRaw(
					"UPDATE contacts SET caseUuid = (SELECT uuid FROM cases WHERE cases.id = contacts.caze_id) WHERE caze_id IS NOT NULL;");
				getDao(Contact.class).executeRaw(
					"UPDATE contacts SET caseDisease = (SELECT disease FROM cases WHERE cases.id = contacts.caze_id) WHERE caze_id IS NOT NULL;");
				// Re-creation of the contacts table has to be done to remove not null constraints and the case id column
				getDao(Contact.class).executeRaw("ALTER TABLE contacts RENAME TO tmp_contacts;");
				getDao(Contact.class).executeRaw(
					"CREATE TABLE contacts (caseUuid VARCHAR, caseDisease VARCHAR, contactClassification VARCHAR, "
						+ "contactOfficer_id BIGINT REFERENCES users(id), contactProximity VARCHAR, contactStatus VARCHAR, "
						+ "description VARCHAR, followUpComment VARCHAR, followUpStatus VARCHAR, followUpUntil BIGINT, "
						+ "lastContactDate BIGINT, person_id BIGINT REFERENCES person(id), relationToCase VARCHAR, "
						+ "reportDateTime BIGINT, reportLat DOUBLEPRECISION, reportLatLonAccuracy FLOAT, reportLon DOUBLEPRECISION, reportingUser_id BIGINT REFERENCES users(id), "
						+ "resultingCaseUuid VARCHAR, resultingCaseUser_id BIGINT REFERENCES users(id),"
						+ "changeDate BIGINT NOT NULL, creationDate BIGINT NOT NULL, id INTEGER PRIMARY KEY AUTOINCREMENT, "
						+ "lastOpenedDate BIGINT, localChangeDate BIGINT NOT NULL, modified SMALLINT, snapshot SMALLINT, uuid VARCHAR NOT NULL, UNIQUE(snapshot, uuid));");
				getDao(Contact.class).executeRaw(
					"INSERT INTO contacts(caseUuid, caseDisease, contactClassification, "
						+ "contactOfficer_id, contactProximity, contactStatus, description, followUpComment, followUpStatus, followUpUntil,"
						+ "lastContactDate, person_id, relationToCase, reportDateTime, reportLat, reportLatLonAccuracy, reportLon, reportingUser_id,"
						+ "resultingCaseUuid, resultingCaseUser_id,"
						+ "changeDate, creationDate, id, lastOpenedDate, localChangeDate, modified, snapshot, uuid) "
						+ "SELECT caseUuid, caseDisease, contactClassification, "
						+ "contactOfficer_id, contactProximity, contactStatus, description, followUpComment, followUpStatus, followUpUntil, "
						+ "lastContactDate, person_id, relationToCase, reportDateTime, reportLat, reportLatLonAccuracy, reportLon, reportingUser_id, "
						+ "resultingCaseUuid, resultingCaseUser_id, "
						+ "changeDate, creationDate, id, lastOpenedDate, localChangeDate, modified, snapshot, uuid " + "FROM tmp_contacts;");
				getDao(Contact.class).executeRaw("DROP TABLE tmp_contacts;");
			case 129:
				currentVersion = 129;
				getDao(Sample.class).executeRaw("ALTER TABLE samples ADD COLUMN referredToUuid varchar(36);");
				getDao(Sample.class)
					.executeRaw("UPDATE samples SET referredToUuid = (SELECT uuid FROM samples s2 WHERE s2.id = samples.referredTo_id);");
			case 130:
				currentVersion = 130;
				getDao(DiseaseClassificationCriteria.class).executeRaw(
					"CREATE TABLE diseaseClassificationCriteria (disease VARCHAR, suspectCriteria TEXT, "
						+ "probableCriteria TEXT, confirmedCriteria TEXT, changeDate BIGINT NOT NULL, creationDate BIGINT NOT NULL, "
						+ "id INTEGER PRIMARY KEY AUTOINCREMENT, localChangeDate BIGINT NOT NULL, modified SMALLINT, snapshot SMALLINT, uuid VARCHAR NOT NULL, "
						+ "UNIQUE(snapshot, uuid));");
			case 131:
				currentVersion = 131;
				getDao(User.class).executeRaw("ALTER TABLE users ADD COLUMN community_id bigint REFERENCES community(id);");
				getDao(User.class).executeRaw("UPDATE users SET userRole = replace(userRole, 'INFORMANT', 'HOSPITAL_INFORMANT');");
			case 132:
				currentVersion = 132;
				getDao(UserRoleConfig.class).executeRaw(
					"CREATE TABLE userrolesconfig(" + "id integer primary key autoincrement," + "uuid varchar(36)," + "changeDate timestamp,"
						+ "creationDate timestamp," + "userRole varchar(255)," + "userRights varchar(1023)," + "lastOpenedDate timestamp,"
						+ "localChangeDate timestamp," + "modified integer," + "snapshot integer);");
			case 133:
				currentVersion = 133;
				getDao(WeeklyReport.class).executeRaw("ALTER TABLE weeklyreport RENAME TO tmp_weeklyreport;");
				getDao(WeeklyReport.class).executeRaw(
					"CREATE TABLE weeklyreport(id integer primary key autoincrement, uuid varchar(36) not null unique, changeDate timestamp not null, "
						+ "creationDate timestamp not null, lastOpenedDate timestamp, localChangeDate timestamp not null, modified integer, snapshot integer, healthFacility_id bigint REFERENCES facility(id), "
						+ "reportingUser_id bigint REFERENCES users(id), reportDateTime timestamp, totalNumberOfCases integer, epiWeek integer, year integer, district_id bigint REFERENCES district(id), "
						+ "community_id bigint REFERENCES community(id), assignedOfficer_id bigint REFERENCES users(id), UNIQUE(snapshot, uuid));");
				getDao(WeeklyReport.class).executeRaw(
					"INSERT INTO weeklyreport(id, uuid, changeDate, creationDate, lastOpenedDate, localChangeDate, modified, snapshot, healthFacility_id, "
						+ "reportingUser_id, reportDateTime, totalNumberOfCases, epiWeek, year) "
						+ "SELECT id, uuid, changeDate, creationDate, lastOpenedDate, localChangeDate, modified, snapshot, healthFacility_id, informant_id, reportDateTime, "
						+ "totalNumberOfCases, epiWeek, year " + "FROM tmp_weeklyreport;");
				getDao(WeeklyReport.class).executeRaw("DROP TABLE tmp_weeklyreport;");
			case 134:
				currentVersion = 134;
				getDao(Outbreak.class).executeRaw("ALTER TABLE outbreak ADD COLUMN startDate timestamp;");
				getDao(Outbreak.class).executeRaw("ALTER TABLE outbreak ADD COLUMN endDate timestamp;");
				getDao(Outbreak.class).executeRaw("UPDATE outbreak SET startDate=creationDate");
			case 135:
				currentVersion = 135;
				getDao(Sample.class).executeRaw("UPDATE samples SET suggestedTypeOfTest='ISOLATION' WHERE suggestedTypeOfTest='VIRUS_ISOLATION'");
				getDao(PathogenTest.class).executeRaw("UPDATE sampleTests SET testType='ISOLATION' WHERE testType='VIRUS_ISOLATION'");
			case 136:
				currentVersion = 136;
				try {
					getDao(Outbreak.class).executeRaw("ALTER TABLE diseaseClassificationCriteria ADD COLUMN lastOpenedDate timestamp;");
				} catch (SQLException e) {
				} // may already exist
				getDao(WeeklyReportEntry.class).executeRaw("ALTER TABLE weeklyreportentry RENAME TO tmp_weeklyreportentry;");
				getDao(WeeklyReportEntry.class).executeRaw(
					"CREATE TABLE weeklyreportentry(" + "id integer primary key autoincrement," + "uuid varchar(36) not null unique,"
						+ "changeDate timestamp not null," + "creationDate timestamp not null," + "lastOpenedDate timestamp,"
						+ "localChangeDate timestamp not null," + "modified integer," + "snapshot integer,"
						+ "weeklyReport_id bigint REFERENCES weeklyreport(id)," + "disease character varying(255)," + "numberOfCases integer" + ");");
				getDao(WeeklyReportEntry.class).executeRaw(
					"INSERT INTO weeklyreportentry(id, uuid, changeDate, creationDate, lastOpenedDate, localChangeDate, modified, snapshot, weeklyReport_id, "
						+ "disease, numberOfCases) "
						+ "SELECT id, uuid, changeDate, creationDate, lastOpenedDate, localChangeDate, modified, snapshot, weeklyReport_id, disease, numberOfCases "
						+ "FROM tmp_weeklyreportentry;");
				getDao(WeeklyReportEntry.class).executeRaw("DROP TABLE tmp_weeklyreportentry;");
			case 137:
				currentVersion = 137;
				try {
					getDao(DiseaseClassificationCriteria.class).executeRaw("DROP TABLE diseaseClassificationCriteria");
					getDao(DiseaseClassificationCriteria.class).executeRaw("DROP TABLE diseaseClassification");
				} catch (SQLException e) {
				} // one of the tables won't exist
				getDao(DiseaseClassificationCriteria.class).executeRaw(
					"CREATE TABLE diseaseClassificationCriteria (disease VARCHAR, suspectCriteria TEXT, "
						+ "probableCriteria TEXT, confirmedCriteria TEXT, changeDate BIGINT NOT NULL, creationDate BIGINT NOT NULL, "
						+ "id INTEGER PRIMARY KEY AUTOINCREMENT, localChangeDate BIGINT NOT NULL, modified SMALLINT, lastOpenedDate timestamp, snapshot SMALLINT, uuid VARCHAR NOT NULL, "
						+ "UNIQUE(snapshot, uuid));");
			case 138:
				currentVersion = 138;
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN sequelae varchar(255);");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN sequelaeDetails varchar(255);");
				getDao(Person.class).executeRaw("ALTER TABLE person ADD COLUMN educationType varchar(255);");
				getDao(Person.class).executeRaw("ALTER TABLE person ADD COLUMN educationDetails varchar(255);");
				getDao(Person.class).executeRaw("ALTER TABLE person ADD COLUMN approximateAgeReferenceDate varchar(255);");
				getDao(Person.class).executeRaw("UPDATE person SET approximateAgeReferenceDate=changeDate WHERE person.approximateAge IS NOT NULL;");
				getDao(Hospitalization.class).executeRaw("ALTER TABLE hospitalizations ADD COLUMN accommodation varchar(255);");
				getDao(Hospitalization.class).executeRaw("ALTER TABLE hospitalizations ADD COLUMN leftAgainstAdvice varchar(255);");
			case 139:
				currentVersion = 139;
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN bloodPressureSystolic integer;");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN bloodPressureDiastolic integer;");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN heartRate integer;");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN pharyngealErythema varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN pharyngealExudate varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN oedemaFaceNeck varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN oedemaLowerExtremity varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN lossSkinTurgor varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN palpableLiver varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN palpableSpleen varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN malaise varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN sunkenEyesFontanelle varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN sidePain varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN fluidInLungCavity varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN tremor varchar(255);");
			case 140:
				currentVersion = 140;
				getDao(PathogenTest.class).executeRaw("ALTER TABLE sampleTests RENAME TO pathogenTests;");
				getDao(Sample.class).executeRaw("ALTER TABLE samples RENAME TO tmp_samples;");
				getDao(Sample.class).executeRaw(
					"CREATE TABLE samples (associatedCase_id BIGINT, comment VARCHAR, lab_id BIGINT, "
						+ "labDetails VARCHAR, labSampleID VARCHAR, noTestPossibleReason VARCHAR, received SMALLINT, receivedDate BIGINT, referredToUuid VARCHAR, "
						+ "reportDateTime BIGINT, reportLat DOUBLE PRECISION, reportLatLonAccuracy FLOAT, reportLon DOUBLE PRECISION, "
						+ "reportingUser_id BIGINT, sampleCode VARCHAR, sampleDateTime BIGINT, sampleMaterial VARCHAR, sampleMaterialText VARCHAR, "
						+ "sampleSource VARCHAR, shipmentDate BIGINT, shipmentDetails VARCHAR, shipped SMALLINT, specimenCondition VARCHAR, "
						+ "pathogenTestingRequested SMALLINT, additionalTestingRequested SMALLINT, requestedPathogenTestsString VARCHAR, requestedAdditionalTestsString VARCHAR, "
						+ "changeDate BIGINT NOT NULL, creationDate BIGINT NOT NULL, id INTEGER PRIMARY KEY AUTOINCREMENT, lastOpenedDate BIGINT, "
						+ "localChangeDate BIGINT NOT NULL, modified SMALLINT, snapshot SMALLINT, uuid VARCHAR NOT NULL, UNIQUE (snapshot, uuid));");
				getDao(Sample.class).executeRaw(
					"INSERT INTO samples(associatedCase_id, comment, lab_id, labDetails, labSampleID, noTestPossibleReason, "
						+ "received, receivedDate, referredToUuid, reportDateTime, reportLat, reportLatLonAccuracy, reportLon, reportingUser_id, sampleCode, "
						+ "sampleDateTime, sampleMaterial, sampleMaterialText, sampleSource, shipmentDate, shipmentDetails, shipped, specimenCondition, "
						+ "requestedPathogenTestsString, changeDate, creationDate, id, lastOpenedDate, localChangeDate, modified, snapshot, uuid) "
						+ "SELECT associatedCase_id, comment, lab_id, labDetails, labSampleID, noTestPossibleReason, received, receivedDate, referredToUuid, "
						+ "reportDateTime, reportLat, reportLatLonAccuracy, reportLon, reportingUser_id, sampleCode, sampleDateTime, sampleMaterial, "
						+ "sampleMaterialText, sampleSource, shipmentDate, shipmentDetails, shipped, specimenCondition, suggestedTypeOfTest, changeDate, creationDate, "
						+ "id, lastOpenedDate, localChangeDate, modified, snapshot, uuid FROM tmp_samples;");
				getDao(Sample.class).executeRaw("UPDATE samples SET pathogenTestingRequested = 1;");
				getDao(Sample.class).executeRaw("UPDATE samples SET additionalTestingRequested = 0;");
				getDao(Sample.class).executeRaw("DROP TABLE tmp_samples;");
			case 141:
				currentVersion = 141;
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN respiratoryRate integer;");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN weight integer;");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN height integer;");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN midUpperArmCircumference integer;");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN glasgowComaScale integer;");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN hemorrhagicSyndrome varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN hyperglycemia varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN hypoglycemia varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN sepsis varchar(255);");
			case 142:
				currentVersion = 142;
				getDao(Sample.class).executeRaw("ALTER TABLE samples ADD COLUMN pathogenTestResult varchar(255);");
			case 143:
				currentVersion = 143;
				getDao(Sample.class).executeRaw("ALTER TABLE samples ADD COLUMN requestedOtherPathogenTests varchar(512);");
				getDao(Sample.class).executeRaw("ALTER TABLE samples ADD COLUMN requestedOtherAdditionalTests varchar(512);");
			case 144:
				currentVersion = 144;
				getDao(WeeklyReportEntry.class).executeRaw("ALTER TABLE weeklyreportentry RENAME TO tmp_weeklyreportentry;");
				getDao(WeeklyReportEntry.class).executeRaw(
					"CREATE TABLE weeklyreportentry(" + "id integer primary key autoincrement," + "uuid varchar(36) not null unique,"
						+ "changeDate timestamp not null," + "creationDate timestamp not null," + "lastOpenedDate timestamp,"
						+ "localChangeDate timestamp not null," + "modified integer," + "snapshot integer," + "weeklyReport_id bigint,"
						+ "disease character varying(255)," + "numberOfCases integer,"
						+ "CONSTRAINT fk_weeklyreportentry_weeklyreport_id FOREIGN KEY (weeklyReport_id) REFERENCES weeklyreport (id) ON DELETE CASCADE"
						+ ");");
				getDao(WeeklyReportEntry.class).executeRaw(
					"INSERT INTO weeklyreportentry(id, uuid, changeDate, creationDate, lastOpenedDate, localChangeDate, modified, snapshot, weeklyReport_id, "
						+ "disease, numberOfCases) "
						+ "SELECT id, uuid, changeDate, creationDate, lastOpenedDate, localChangeDate, modified, snapshot, weeklyReport_id, disease, numberOfCases "
						+ "FROM tmp_weeklyreportentry;");
				getDao(WeeklyReportEntry.class).executeRaw("DROP TABLE tmp_weeklyreportentry;");
				getDao(WeeklyReport.class).executeRaw(
					"DELETE FROM weeklyreport WHERE rowid NOT IN (SELECT min(rowid) FROM weeklyreport GROUP BY epiWeek, year, reportingUser_id);");
			case 145:
				currentVersion = 145;
				getDao(Therapy.class).executeRaw(
					"CREATE TABLE therapy(" + "id integer primary key autoincrement," + "uuid varchar(36) not null,"
						+ "changeDate timestamp not null," + "creationDate timestamp not null," + "lastOpenedDate timestamp,"
						+ "localChangeDate timestamp not null," + "modified integer," + "snapshot integer," + "UNIQUE (snapshot, uuid));");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN therapy_id bigint REFERENCES therapy(id)");
				getDao(Prescription.class).executeRaw(
					"CREATE TABLE prescription(" + "id integer primary key autoincrement," + "uuid varchar(36) not null,"
						+ "changeDate timestamp not null," + "creationDate timestamp not null," + "lastOpenedDate timestamp,"
						+ "localChangeDate timestamp not null," + "modified integer," + "snapshot integer,"
						+ "therapy_id bigint REFERENCES therapy(id)," + "prescriptionDate timestamp," + "prescriptionStart timestamp,"
						+ "prescriptionEnd timestamp," + "prescribingClinician varchar(512)," + "prescriptionType varchar(255),"
						+ "prescriptionDetails varchar(512)," + "frequency varchar(512)," + "dose varchar(512)," + "route varchar(255),"
						+ "routeDetails varchar(512)," + "additionalNotes varchar(512)," + "typeOfDrug varchar(255)," + "UNIQUE(snapshot, uuid));");
				getDao(Treatment.class).executeRaw(
					"CREATE TABLE treatment(" + "id integer primary key autoincrement," + "uuid varchar(36) not null,"
						+ "changeDate timestamp not null," + "creationDate timestamp not null," + "lastOpenedDate timestamp,"
						+ "localChangeDate timestamp not null," + "modified integer," + "snapshot integer,"
						+ "therapy_id bigint REFERENCES therapy(id)," + "treatmentDateTime timestamp," + "executingClinician varchar(512),"
						+ "treatmentType varchar(255)," + "treatmentDetails varchar(512)," + "dose varchar(512)," + "route varchar(255),"
						+ "routeDetails varchar(512)," + "additionalNotes varchar(512)," + "typeOfDrug varchar(255),"
						+ "prescription_id bigint REFERENCES prescription(id)," + "UNIQUE(snapshot, uuid));");
			case 146:
				currentVersion = 146;
				getDao(Case.class).executeRaw("UPDATE cases SET changeDate = 0 WHERE changeDate IS NOT NULL;");
				getDao(Symptoms.class).executeRaw("UPDATE symptoms SET changeDate = 0 WHERE changeDate IS NOT NULL;");
				getDao(Hospitalization.class).executeRaw("UPDATE hospitalizations SET changeDate = 0 WHERE changeDate IS NOT NULL;");
				getDao(PreviousHospitalization.class).executeRaw("UPDATE previoushospitalizations SET changeDate = 0 WHERE changeDate IS NOT NULL;");
				getDao(EpiData.class).executeRaw("UPDATE epidata SET changeDate = 0 WHERE changeDate IS NOT NULL;");
				getDao(Location.class).executeRaw("UPDATE location SET changeDate = 0 WHERE changeDate IS NOT NULL;");
			case 147:
				currentVersion = 147;
				getDao(AdditionalTest.class).executeRaw(
					"CREATE TABLE additionaltest(" + "id integer primary key autoincrement," + "uuid varchar(36) not null,"
						+ "changeDate timestamp not null," + "creationDate timestamp not null," + "lastOpenedDate timestamp,"
						+ "localChangeDate timestamp not null," + "modified integer," + "snapshot integer,"
						+ "sample_id bigint REFERENCES samples(id)," + "testDateTime timestamp," + "haemoglobinuria varchar(255),"
						+ "proteinuria varchar(255)," + "hematuria varchar(255)," + "arterialVenousGasPh integer," + "arterialVenousGasPco2 integer,"
						+ "arterialVenousGasPao2 integer," + "arterialVenousGasHco3 integer," + "gasOxygenTherapy integer," + "altSgpt integer,"
						+ "astSgot integer," + "creatinine integer," + "potassium integer," + "urea integer," + "haemoglobin integer,"
						+ "totalBilirubin integer," + "conjBilirubin integer," + "wbcCount integer," + "platelets integer,"
						+ "prothrombinTime integer," + "otherTestResults varchar(512)," + "UNIQUE(snapshot, uuid));");
			case 148:
				currentVersion = 148;
				getDao(HealthConditions.class).executeRaw(
					"CREATE TABLE healthConditions(" + "id integer primary key autoincrement," + "uuid varchar(36) not null,"
						+ "changeDate timestamp not null," + "creationDate timestamp not null," + "lastOpenedDate timestamp,"
						+ "localChangeDate timestamp not null," + "modified integer," + "snapshot integer," + "tuberculosis varchar(255),"
						+ "asplenia varchar(255)," + "hepatitis varchar(255)," + "diabetes varchar(255)," + "hiv varchar(255),"
						+ "hivArt varchar(255)," + "chronicLiverDisease varchar(255)," + "malignancyChemotherapy varchar(255),"
						+ "chronicHeartFailure varchar(255)," + "chronicPulmonaryDisease varchar(255)," + "chronicKidneyDisease varchar(255),"
						+ "chronicNeurologicCondition varchar(255)," + "otherConditions varchar(512)," + "UNIQUE(snapshot, uuid));");
				getDao(ClinicalCourse.class).executeRaw(
					"CREATE TABLE clinicalCourse(" + "id integer primary key autoincrement," + "uuid varchar(36) not null,"
						+ "changeDate timestamp not null," + "creationDate timestamp not null," + "lastOpenedDate timestamp,"
						+ "localChangeDate timestamp not null," + "modified integer," + "snapshot integer,"
						+ "healthConditions_id bigint REFERENCES healthConditions(id)," + "UNIQUE(snapshot, uuid));");
				getDao(ClinicalVisit.class).executeRaw(
					"CREATE TABLE clinicalVisit(" + "id integer primary key autoincrement," + "uuid varchar(36) not null,"
						+ "changeDate timestamp not null," + "creationDate timestamp not null," + "lastOpenedDate timestamp,"
						+ "localChangeDate timestamp not null," + "modified integer," + "snapshot integer,"
						+ "clinicalCourse_id bigint REFERENCES clinicalCourse(id)," + "symptoms_id bigint REFERENCES symptoms(id),"
						+ "disease varchar(255)," + "visitDateTime timestamp," + "visitRemarks varchar(512)," + "visitingPerson varchar(512),"
						+ "UNIQUE(snapshot, uuid));");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN clinicalCourse_id bigint REFERENCES therapy(id)");
				getDao(Case.class).executeRaw("UPDATE cases SET changeDate = 0 WHERE changeDate IS NOT NULL;");
				getDao(Therapy.class).executeRaw("UPDATE therapy SET changeDate = 0 WHERE changeDate IS NOT NULL;");
				getDao(Symptoms.class).executeRaw("UPDATE symptoms SET changeDate = 0 WHERE changeDate IS NOT NULL;");
				getDao(Hospitalization.class).executeRaw("UPDATE hospitalizations SET changeDate = 0 WHERE changeDate IS NOT NULL;");
				getDao(PreviousHospitalization.class).executeRaw("UPDATE previoushospitalizations SET changeDate = 0 WHERE changeDate IS NOT NULL;");
				getDao(EpiData.class).executeRaw("UPDATE epidata SET changeDate = 0 WHERE changeDate IS NOT NULL;");
				getDao(Location.class).executeRaw("UPDATE location SET changeDate = 0 WHERE changeDate IS NOT NULL;");
			case 149:
				currentVersion = 149;
				getDao(DiseaseConfiguration.class).executeRaw(
					"CREATE TABLE diseaseConfiguration(" + "id integer primary key autoincrement," + "uuid varchar(36) not null,"
						+ "changeDate timestamp not null," + "creationDate timestamp not null," + "lastOpenedDate timestamp,"
						+ "localChangeDate timestamp not null," + "modified integer," + "snapshot integer," + "disease varchar(255),"
						+ "active boolean," + "primaryDisease boolean," + "followUpEnabled boolean," + "followUpDuration integer,"
						+ "UNIQUE(snapshot, uuid));");
			case 150:
				currentVersion = 150;
				getDao(Person.class).executeRaw("UPDATE person SET educationtype='NONE' WHERE educationtype='NURSERY';");
			case 151:
				currentVersion = 151;
				getDao(PathogenTest.class).executeRaw("ALTER TABLE pathogenTests ADD COLUMN testedDisease varchar(255);");
				getDao(PathogenTest.class).executeRaw(
					"UPDATE pathogenTests SET testedDisease = 'WEST_NILE_FEVER' WHERE testType = 'WEST_NILE_FEVER_IGM' OR testType = 'WEST_NILE_FEVER_ANTIBODIES';");
				getDao(PathogenTest.class).executeRaw(
					"UPDATE pathogenTests SET testedDisease = 'DENGUE' WHERE testType = 'DENGUE_FEVER_IGM' OR testType = 'DENGUE_FEVER_ANTIBODIES';");
				getDao(PathogenTest.class).executeRaw(
					"UPDATE pathogenTests SET testedDisease = 'YELLOW_FEVER' WHERE testType = 'YELLOW_FEVER_IGM' OR testType = 'YELLOW_FEVER_ANTIBODIES';");
				getDao(PathogenTest.class)
					.executeRaw("UPDATE pathogenTests SET testedDisease = 'PLAGUE' WHERE testType = 'YERSINIA_PESTIS_ANTIGEN';");
				getDao(PathogenTest.class).executeRaw(
					"UPDATE pathogenTests SET testedDisease = (SELECT disease FROM cases WHERE cases.id = (SELECT associatedCase_id FROM samples WHERE samples.id = pathogenTests.sample_id)) WHERE testedDisease IS NULL;");
				getDao(PathogenTest.class).executeRaw(
					"UPDATE pathogenTests SET testType = 'IGM_SERUM_ANTIBODY' WHERE testType = 'DENGUE_FEVER_IGM' OR testType = 'WEST_NILE_FEVER_IGM' OR testType = 'YELLOW_FEVER_IGM';");
				getDao(PathogenTest.class).executeRaw(
					"UPDATE pathogenTests SET testType = 'NEUTRALIZING_ANTIBODIES' WHERE testType = 'DENGUE_FEVER_ANTIBODIES' OR testType = 'WEST_NILE_FEVER_ANTIBODIES' OR testType = 'YELLOW_FEVER_ANTIBODIES';");
				getDao(PathogenTest.class)
					.executeRaw("UPDATE pathogenTests SET testType = 'ANTIGEN_DETECTION' WHERE testType = 'YERSINIA_PESTIS_ANTIGEN';");
				getDao(Sample.class).executeRaw(
					"UPDATE samples SET requestedPathogenTestsString = REPLACE(requestedPathogenTestsString, 'DENGUE_FEVER_IGM', 'IGM_SERUM_ANTIBODY');");
				getDao(Sample.class).executeRaw(
					"UPDATE samples SET requestedPathogenTestsString = REPLACE(requestedPathogenTestsString, 'WEST_NILE_FEVER_IGM', 'IGM_SERUM_ANTIBODY');");
				getDao(Sample.class).executeRaw(
					"UPDATE samples SET requestedPathogenTestsString = REPLACE(requestedPathogenTestsString, 'YELLOW_FEVER_IGM', 'IGM_SERUM_ANTIBODY');");
				getDao(Sample.class).executeRaw(
					"UPDATE samples SET requestedPathogenTestsString = REPLACE(requestedPathogenTestsString, 'DENGUE_FEVER_ANTIBODIES', 'NEUTRALIZING_ANTIBODIES');");
				getDao(Sample.class).executeRaw(
					"UPDATE samples SET requestedPathogenTestsString = REPLACE(requestedPathogenTestsString, 'WEST_NILE_FEVER_ANTIBODIES', 'NEUTRALIZING_ANTIBODIES');");
				getDao(Sample.class).executeRaw(
					"UPDATE samples SET requestedPathogenTestsString = REPLACE(requestedPathogenTestsString, 'YELLOW_FEVER_ANTIBODIES', 'NEUTRALIZING_ANTIBODIES');");
				getDao(Sample.class).executeRaw(
					"UPDATE samples SET requestedPathogenTestsString = REPLACE(requestedPathogenTestsString, 'YERSINIA_PESTIS_ANTIGEN', 'ANTIGEN_DETECTION');");
				getDao(PathogenTest.class).executeRaw("ALTER TABLE pathogenTests ADD COLUMN testedDiseaseDetails varchar(512);");
			case 152:
				currentVersion = 152;
				getDao(Person.class).executeRaw("UPDATE person SET educationType='NURSERY' WHERE educationType='NONE';");
			case 153:
				currentVersion = 153;
				getDao(Location.class).executeRaw("ALTER TABLE location ADD COLUMN areaType varchar(255);");
			case 154:
				currentVersion = 154;
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN clinicianDetails varchar(512);");
			case 155:
				currentVersion = 155;
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN districtLevelDate timestamp;");
			case 156:
				currentVersion = 156;
				getDao(User.class).executeRaw("ALTER TABLE users ADD COLUMN limitedDisease varchar(255);");
			case 157:
				currentVersion = 157;
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN notifyingClinic varchar(255);");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN notifyingClinicDetails varchar(512);");
				getDao(HealthConditions.class).executeRaw("ALTER TABLE healthConditions ADD COLUMN downSyndrome varchar(255);");
				getDao(HealthConditions.class).executeRaw("ALTER TABLE healthConditions ADD COLUMN congenitalSyphilis varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN bilateralCataracts varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN unilateralCataracts varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN congenitalGlaucoma varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN pigmentaryRetinopathy varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN purpuricRash varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN microcephaly varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN developmentalDelay varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN splenomegaly varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN meningoencephalitis varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN radiolucentBoneDisease varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN congenitalHeartDisease varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN congenitalHeartDiseaseType varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN congenitalHeartDiseaseDetails varchar(512);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN jaundiceWithin24HoursOfBirth varchar(255);");
			case 158:
				currentVersion = 158;
				getDao(MaternalHistory.class).executeRaw(
					"CREATE TABLE maternalHistory(" + "id integer primary key autoincrement," + "uuid varchar(36) not null,"
						+ "changeDate timestamp not null," + "creationDate timestamp not null," + "lastOpenedDate timestamp,"
						+ "localChangeDate timestamp not null," + "modified integer," + "snapshot integer," + "childrenNumber integer,"
						+ "ageAtBirth integer," + "conjunctivitis varchar(255)," + "conjunctivitisOnset timestamp," + "conjunctivitisMonth integer,"
						+ "maculopapularRash varchar(255)," + "maculopapularRashOnset timestamp," + "maculopapularRashMonth integer,"
						+ "swollenLymphs varchar(255)," + "swollenLymphsOnset timestamp," + "swollenLymphsMonth integer,"
						+ "arthralgiaArthritis varchar(255)," + "arthralgiaArthritisOnset timestamp," + "arthralgiaArthritisMonth integer,"
						+ "otherComplications varchar(255)," + "otherComplicationsOnset timestamp," + "otherComplicationsMonth integer,"
						+ "otherComplicationsDetails varchar(512)," + "rubella varchar(255)," + "rubellaOnset timestamp,"
						+ "rashExposure varchar(255)," + "rashExposureDate timestamp," + "rashExposureMonth integer,"
						+ "rashExposureRegion_id bigint REFERENCES region(id)," + "rashExposureDistrict_id bigint REFERENCES district(id),"
						+ "rashExposureCommunity_id bigint REFERENCES community(id)," + "UNIQUE(snapshot, uuid));");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN maternalHistory_id bigint REFERENCES maternalHistory(id);");
				getDao(Case.class).executeRaw("UPDATE cases SET changeDate = 0 WHERE changeDate IS NOT NULL;");
				getDao(Therapy.class).executeRaw("UPDATE therapy SET changeDate = 0 WHERE changeDate IS NOT NULL;");
				getDao(ClinicalCourse.class).executeRaw("UPDATE clinicalCourse SET changeDate = 0 WHERE changeDate IS NOT NULL;");
				getDao(HealthConditions.class).executeRaw("UPDATE healthConditions SET changeDate = 0 WHERE changeDate IS NOT NULL;");
				getDao(Symptoms.class).executeRaw("UPDATE symptoms SET changeDate = 0 WHERE changeDate IS NOT NULL;");
				getDao(Hospitalization.class).executeRaw("UPDATE hospitalizations SET changeDate = 0 WHERE changeDate IS NOT NULL;");
				getDao(PreviousHospitalization.class).executeRaw("UPDATE previoushospitalizations SET changeDate = 0 WHERE changeDate IS NOT NULL;");
				getDao(EpiData.class).executeRaw("UPDATE epidata SET changeDate = 0 WHERE changeDate IS NOT NULL;");
				getDao(Location.class).executeRaw("UPDATE location SET changeDate = 0 WHERE changeDate IS NOT NULL;");
			case 159:
				currentVersion = 159;
				getDao(Person.class).executeRaw("ALTER TABLE person ADD COLUMN mothersName varchar(512);");
				getDao(Person.class).executeRaw("ALTER TABLE person ADD COLUMN fathersName varchar(512);");
				getDao(Person.class).executeRaw("ALTER TABLE person ADD COLUMN placeOfBirthRegion_id bigint REFERENCES region(id);");
				getDao(Person.class).executeRaw("ALTER TABLE person ADD COLUMN placeOfBirthDistrict_id bigint REFERENCES district(id);");
				getDao(Person.class).executeRaw("ALTER TABLE person ADD COLUMN placeOfBirthCommunity_id bigint REFERENCES community(id);");
				getDao(Person.class).executeRaw("ALTER TABLE person ADD COLUMN placeOfBirthFacility_id bigint REFERENCES facility(id);");
				getDao(Person.class).executeRaw("ALTER TABLE person ADD COLUMN placeOfBirthFacilityDetails varchar(512);");
				getDao(Person.class).executeRaw("ALTER TABLE person ADD COLUMN gestationAgeAtBirth integer;");
				getDao(Person.class).executeRaw("ALTER TABLE person ADD COLUMN birthWeight integer;");
			case 160:
				currentVersion = 160;
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN versionCreated varchar(32);");
			case 161:
				currentVersion = 161;
				getDao(PointOfEntry.class).executeRaw(
					"CREATE TABLE pointOfEntry(" + "id integer primary key autoincrement," + "uuid varchar(36) not null,"
						+ "changeDate timestamp not null," + "creationDate timestamp not null," + "lastOpenedDate timestamp,"
						+ "localChangeDate timestamp not null," + "modified integer," + "snapshot integer," + "pointOfEntryType varchar(255),"
						+ "name varchar(512)," + "region_id bigint REFERENCES region(id)," + "district_id bigint REFERENCES district(id),"
						+ "latitude double precision," + "longitude double precision," + "active boolean," + "UNIQUE(snapshot, uuid));");
				getDao(PointOfEntry.class).executeRaw(
					"CREATE TABLE portHealthInfo(" + "id integer primary key autoincrement," + "uuid varchar(36) not null,"
						+ "changeDate timestamp not null," + "creationDate timestamp not null," + "lastOpenedDate timestamp,"
						+ "localChangeDate timestamp not null," + "modified integer," + "snapshot integer," + "airlineName varchar(512),"
						+ "flightNumber varchar(512)," + "departureDateTime timestamp," + "arrivalDateTime timestamp," + "freeSeating varchar(255),"
						+ "seatNumber varchar(512)," + "departureAirport varchar(512)," + "numberOfTransitStops integer,"
						+ "transitStopDetails1 varchar(512)," + "transitStopDetails2 varchar(512)," + "transitStopDetails3 varchar(512),"
						+ "transitStopDetails4 varchar(512)," + "transitStopDetails5 varchar(512)," + "vesselName varchar(512),"
						+ "vesselDetails varchar(512)," + "portOfDeparture varchar(512)," + "lastPortOfCall varchar(512),"
						+ "conveyanceType varchar(255)," + "conveyanceTypeDetails varchar(512)," + "departureLocation varchar(512),"
						+ "finalDestination varchar(512)," + "details varchar(512)," + "UNIQUE(snapshot, uuid));");
				getDao(User.class).executeRaw("ALTER TABLE users ADD COLUMN pointOfEntry_id bigint REFERENCES pointOfEntry(id);");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN portHealthInfo_id bigint REFERENCES portHealthInfo(id);");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN caseOrigin varchar(255);");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN pointOfEntry_id bigint REFERENCES pointOfEntry(id);");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN pointOfEntryDetails varchar(512);");
				getDao(Case.class).executeRaw("UPDATE cases SET changeDate = 0 WHERE changeDate IS NOT NULL;");
				getDao(Therapy.class).executeRaw("UPDATE therapy SET changeDate = 0 WHERE changeDate IS NOT NULL;");
				getDao(ClinicalCourse.class).executeRaw("UPDATE clinicalCourse SET changeDate = 0 WHERE changeDate IS NOT NULL;");
				getDao(HealthConditions.class).executeRaw("UPDATE healthConditions SET changeDate = 0 WHERE changeDate IS NOT NULL;");
				getDao(Symptoms.class).executeRaw("UPDATE symptoms SET changeDate = 0 WHERE changeDate IS NOT NULL;");
				getDao(Hospitalization.class).executeRaw("UPDATE hospitalizations SET changeDate = 0 WHERE changeDate IS NOT NULL;");
				getDao(PreviousHospitalization.class).executeRaw("UPDATE previoushospitalizations SET changeDate = 0 WHERE changeDate IS NOT NULL;");
				getDao(EpiData.class).executeRaw("UPDATE epidata SET changeDate = 0 WHERE changeDate IS NOT NULL;");
				getDao(MaternalHistory.class).executeRaw("UPDATE maternalHistory SET changeDate = 0 WHERE changeDate IS NOT NULL;");
				getDao(Location.class).executeRaw("UPDATE location SET changeDate = 0 WHERE changeDate IS NOT NULL;");
			case 162:
				currentVersion = 162;
				getDao(AdditionalTest.class).executeRaw("ALTER TABLE additionalTest RENAME TO tmp_additionalTest");
				getDao(AdditionalTest.class).executeRaw(
					"CREATE TABLE additionalTest(" + "id integer primary key autoincrement," + "uuid varchar(36) not null,"
						+ "changeDate timestamp not null," + "creationDate timestamp not null," + "lastOpenedDate timestamp,"
						+ "localChangeDate timestamp not null," + "modified integer," + "snapshot integer,"
						+ "sample_id bigint REFERENCES samples(id)," + "testDateTime timestamp," + "haemoglobinuria varchar(255),"
						+ "proteinuria varchar(255)," + "hematuria varchar(255)," + "arterialVenousGasPh real," + "arterialVenousGasPco2 real,"
						+ "arterialVenousGasPao2 real," + "arterialVenousGasHco3 real," + "gasOxygenTherapy real," + "altSgpt real," + "astSgot real,"
						+ "creatinine real," + "potassium real," + "urea real," + "haemoglobin real," + "totalBilirubin real," + "conjBilirubin real,"
						+ "wbcCount real," + "platelets real," + "prothrombinTime real," + "otherTestResults varchar(512),"
						+ "UNIQUE(snapshot, uuid));");
				getDao(AdditionalTest.class).executeRaw(
					"INSERT INTO additionalTest(id, uuid, changeDate, creationDate, lastOpenedDate, "
						+ "localChangeDate, modified, snapshot, sample_id, testDateTime, haemoglobinuria, proteinuria, hematuria, arterialVenousGasPh, "
						+ "arterialVenousGasPco2, arterialVenousGasPao2, arterialVenousGasHco3, gasOxygenTherapy, altSgpt, astSgot, creatinine, "
						+ "potassium, urea, haemoglobin, totalBilirubin, conjBilirubin, wbcCount, platelets, prothrombinTime, otherTestResults) "
						+ "SELECT id, uuid, changeDate, creationDate, lastOpenedDate, localChangeDate, modified, snapshot, sample_id, testDateTime, "
						+ "haemoglobinuria, proteinuria, hematuria, arterialVenousGasPh, arterialVenousGasPco2, arterialVenousGasPao2, arterialVenousGasHco3, "
						+ "gasOxygenTherapy, altSgpt, astSgot, creatinine, potassium, urea, haemoglobin, totalBilirubin, conjBilirubin, wbcCount, "
						+ "platelets, prothrombinTime, otherTestResults FROM tmp_additionalTest");
				getDao(Sample.class).executeRaw("DROP TABLE tmp_additionalTest;");
			case 163:
				currentVersion = 163;
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN clinicianPhone varchar(512);");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN clinicianEmail varchar(512);");
			case 164:
				currentVersion = 164;
				getDao(Case.class).executeRaw("DELETE FROM cases WHERE person_id IS NULL");
			case 165:
				currentVersion = 165;
				// normally we would have a temp table and copy the data. In this context we have to pull all samples anyway
				getDao(WeeklyReportEntry.class).executeRaw("DROP TABLE pathogenTests;");
				getDao(WeeklyReportEntry.class).executeRaw(
					"CREATE TABLE pathogenTest(" + "id integer primary key autoincrement," + "uuid varchar(36) not null,"
						+ "changeDate timestamp not null," + "creationDate timestamp not null," + "lastOpenedDate timestamp,"
						+ "localChangeDate timestamp not null," + "modified integer," + "snapshot integer,"
						+ "sample_id bigint REFERENCES samples(id)," + "testDateTime timestamp," + "testType character varying(255),"
						+ "testTypeText character varying(512)," + "lab_id bigint REFERENCES facility(id),"
						+ "labUser_id bigint REFERENCES users(id)," + "testResult character varying(255)," + "testResultText character varying(512) ,"
						+ "testResultVerified boolean," + "fourFoldIncreaseAntibodyTiter boolean," + "labDetails character varying(512) ,"
						+ "testedDisease character varying(255)," + "testedDiseaseDetails character varying(512)," + "UNIQUE(snapshot, uuid));");

				// we had an invalid break here - re-execute db update for all versions in between

			case 166:
			case 167:
			case 168:
			case 169:
			case 170:
			case 171:

				// this is a special solution to repair broken devices and at the same time not corrupt healthy ones

				try {
					currentVersion = 166;
					getDao(PathogenTest.class).executeRaw("ALTER TABLE pathogenTest ADD COLUMN serotype varchar(255);");
					getDao(PathogenTest.class).executeRaw("ALTER TABLE pathogenTest ADD COLUMN cqValue real;");
				} catch (SQLException e) {
				}

				try {
					currentVersion = 167;
					getDao(Sample.class).executeRaw("ALTER TABLE samples RENAME TO tmp_samples;");
					getDao(Sample.class).executeRaw(
						"CREATE TABLE samples (associatedCase_id BIGINT, comment VARCHAR, lab_id BIGINT, "
							+ "labDetails VARCHAR, labSampleID VARCHAR, noTestPossibleReason VARCHAR, received SMALLINT, receivedDate BIGINT, referredToUuid VARCHAR, "
							+ "reportDateTime BIGINT, reportLat DOUBLE PRECISION, reportLatLonAccuracy FLOAT, reportLon DOUBLE PRECISION, "
							+ "reportingUser_id BIGINT, sampleDateTime BIGINT, sampleMaterial VARCHAR, sampleMaterialText VARCHAR, "
							+ "sampleSource VARCHAR, shipmentDate BIGINT, shipmentDetails VARCHAR, shipped SMALLINT, specimenCondition VARCHAR, "
							+ "pathogenTestingRequested SMALLINT, additionalTestingRequested SMALLINT, requestedPathogenTestsString VARCHAR, requestedAdditionalTestsString VARCHAR, "
							+ "requestedOtherPathogenTests varchar(512), requestedOtherAdditionalTests varchar(512), samplePurpose varchar(255), pathogenTestResult varchar(255), "
							+ "changeDate BIGINT NOT NULL, creationDate BIGINT NOT NULL, id INTEGER PRIMARY KEY AUTOINCREMENT, lastOpenedDate BIGINT, "
							+ "localChangeDate BIGINT NOT NULL, modified SMALLINT, snapshot SMALLINT, uuid VARCHAR NOT NULL, UNIQUE (snapshot, uuid));");
					getDao(Sample.class).executeRaw(
						"INSERT INTO samples(associatedCase_id, comment, lab_id, labDetails, labSampleID, noTestPossibleReason, "
							+ "received, receivedDate, referredToUuid, reportDateTime, reportLat, reportLatLonAccuracy, reportLon, reportingUser_id, "
							+ "sampleDateTime, sampleMaterial, sampleMaterialText, sampleSource, shipmentDate, shipmentDetails, shipped, specimenCondition, "
							+ "pathogenTestingRequested, additionalTestingRequested, requestedPathogenTestsString, requestedAdditionalTestsString, "
							+ "requestedOtherPathogenTests, requestedOtherAdditionalTests, samplePurpose, pathogenTestResult, "
							+ "changeDate, creationDate, id, lastOpenedDate, localChangeDate, modified, snapshot, uuid) "
							+ "SELECT associatedCase_id, comment, lab_id, labDetails, labSampleID, noTestPossibleReason, "
							+ "received, receivedDate, referredToUuid, reportDateTime, reportLat, reportLatLonAccuracy, reportLon, reportingUser_id, "
							+ "sampleDateTime, sampleMaterial, sampleMaterialText, sampleSource, shipmentDate, shipmentDetails, shipped, specimenCondition, "
							+ "pathogenTestingRequested, additionalTestingRequested, requestedPathogenTestsString, requestedAdditionalTestsString, "
							+ "requestedOtherPathogenTests, requestedOtherAdditionalTests, 'EXTERNAL', pathogenTestResult, "
							+ "changeDate, creationDate, id, lastOpenedDate, localChangeDate, modified, snapshot, uuid FROM tmp_samples;");
					getDao(Sample.class).executeRaw("DROP TABLE tmp_samples;");
				} catch (SQLException e) {
				}

				try {
					currentVersion = 168;

					getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN vaccine varchar(512);");
					getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN rabiesType varchar(255);");

					getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN hydrophobia varchar(255);");
					getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN opisthotonus varchar(255);");
					getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN anxietyStates varchar(255);");
					getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN delirium varchar(255);");
					getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN uproariousness varchar(255);");
					getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN paresthesiaAroundWound varchar(255);");
					getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN excessSalivation varchar(255);");
					getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN insomnia varchar(255);");
					getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN paralysis varchar(255);");
					getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN excitation varchar(255);");
					getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN dysphagia varchar(255);");
					getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN aerophobia varchar(255);");
					getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN hyperactivity varchar(255);");
					getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN paresis varchar(255);");
					getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN agitation varchar(255);");
					getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN ascendingFlaccidParalysis varchar(255);");
					getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN erraticBehaviour varchar(255);");
					getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN coma varchar(255);");

					getDao(EpiData.class).executeRaw("ALTER TABLE epidata ADD COLUMN dogs varchar(255);");
					getDao(EpiData.class).executeRaw("ALTER TABLE epidata ADD COLUMN cats varchar(255);");
					getDao(EpiData.class).executeRaw("ALTER TABLE epidata ADD COLUMN canidae varchar(255);");
					getDao(EpiData.class).executeRaw("ALTER TABLE epidata ADD COLUMN rabbits varchar(255);");

					getDao(EpiData.class).executeRaw("ALTER TABLE epidata ADD COLUMN prophylaxisStatus varchar(255);");
					getDao(EpiData.class).executeRaw("ALTER TABLE epidata ADD COLUMN dateOfProphylaxis timestamp;");

					getDao(EpiData.class).executeRaw("ALTER TABLE epidata ADD COLUMN kindOfExposureBite varchar(255);");
					getDao(EpiData.class).executeRaw("ALTER TABLE epidata ADD COLUMN kindOfExposureTouch varchar(255);");
					getDao(EpiData.class).executeRaw("ALTER TABLE epidata ADD COLUMN kindOfExposureScratch varchar(255);");
					getDao(EpiData.class).executeRaw("ALTER TABLE epidata ADD COLUMN kindOfExposureLick varchar(255);");
					getDao(EpiData.class).executeRaw("ALTER TABLE epidata ADD COLUMN kindOfExposureOther varchar(255);");
					getDao(EpiData.class).executeRaw("ALTER TABLE epidata ADD COLUMN kindOfExposureDetails varchar(512);");

					getDao(EpiData.class).executeRaw("ALTER TABLE epidata ADD COLUMN animalVaccinationStatus varchar(255);");
				} catch (SQLException e) {
				}

				try {
					currentVersion = 169;
					getDao(Contact.class).executeRaw("ALTER TABLE contacts ADD COLUMN relationDescription varchar(512);");
				} catch (SQLException e) {
				}

				try {
					currentVersion = 170;
					getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN convulsion varchar(255);");
				} catch (SQLException e) {
				}
			case 172:
				currentVersion = 172;
				getDao(FeatureConfiguration.class).executeRaw(
					"CREATE TABLE featureConfiguration(" + "id integer primary key autoincrement," + "uuid varchar(36) not null,"
						+ "changeDate timestamp not null," + "creationDate timestamp not null," + "lastOpenedDate timestamp,"
						+ "localChangeDate timestamp not null," + "modified integer," + "snapshot integer," + "disease varchar(255),"
						+ "featureType varchar(255)," + "endDate timestamp," + "UNIQUE(snapshot, uuid));");
			case 173:
				currentVersion = 173;
				getDao(AggregateReport.class).executeRaw(
					"CREATE TABLE aggregateReport(" + "id integer primary key autoincrement," + "uuid varchar(36) not null,"
						+ "changeDate timestamp not null," + "creationDate timestamp not null," + "lastOpenedDate timestamp,"
						+ "localChangeDate timestamp not null," + "modified integer," + "snapshot integer," + "disease varchar(255),"
						+ "reportingUser_id bigint REFERENCES users(id)," + "region_id bigint REFERENCES region(id),"
						+ "district_id bigint REFERENCES district(id)," + "healthFacility_id bigint REFERENCES facility(id),"
						+ "pointOfEntry_id bigint REFERENCES pointOfEntry(id)," + "year integer," + "epiWeek integer," + "newCases integer,"
						+ "labConfirmations integer," + "deaths integer," + "UNIQUE(snapshot, uuid));");
			case 174:
				currentVersion = 174;
				getDao(DiseaseConfiguration.class).executeRaw("ALTER TABLE diseaseConfiguration ADD COLUMN caseBased boolean;");
			case 175:
				currentVersion = 175;
				getDao(Region.class).executeRaw("ALTER TABLE region ADD COLUMN archived SMALLINT DEFAULT 0;");
				getDao(District.class).executeRaw("ALTER TABLE district ADD COLUMN archived SMALLINT DEFAULT 0;");
				getDao(Community.class).executeRaw("ALTER TABLE community ADD COLUMN archived SMALLINT DEFAULT 0;");
				getDao(Facility.class).executeRaw("ALTER TABLE facility ADD COLUMN archived SMALLINT DEFAULT 0;");
				getDao(PointOfEntry.class).executeRaw("ALTER TABLE pointOfEntry ADD COLUMN archived SMALLINT DEFAULT 0;");
			case 176:
				currentVersion = 176;
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN fluidInLungCavityAuscultation varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN fluidInLungCavityXray varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN abnormalLungXrayFindings varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN conjunctivalInjection varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN acuteRespiratoryDistressSyndrome varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN pneumoniaClinicalOrRadiologic varchar(255);");
				getDao(EpiData.class).executeRaw("ALTER TABLE epidata ADD COLUMN visitedHealthFacility varchar(255);");
				getDao(EpiData.class).executeRaw("ALTER TABLE epidata ADD COLUMN contactWithSourceRespiratoryCase varchar(255);");
				getDao(EpiData.class).executeRaw("ALTER TABLE epidata ADD COLUMN visitedAnimalMarket varchar(255);");
				getDao(EpiData.class).executeRaw("ALTER TABLE epidata ADD COLUMN camels varchar(255);");
				getDao(EpiData.class).executeRaw("ALTER TABLE epidata ADD COLUMN snakes varchar(255);");
				getDao(HealthConditions.class).executeRaw("ALTER TABLE healthConditions ADD COLUMN immunodeficiencyOtherThanHiv varchar(255);");
				getDao(HealthConditions.class)
					.executeRaw("ALTER TABLE healthConditions ADD COLUMN cardiovascularDiseaseIncludingHypertension varchar(255);");
			case 177:
				currentVersion = 177;
				getDao(Case.class).executeRaw("UPDATE cases SET disease = 'NEW_INFLUENZA' WHERE disease = 'NEW_INFLUENCA';");
				getDao(Case.class).executeRaw("UPDATE events SET disease = 'NEW_INFLUENZA' WHERE disease = 'NEW_INFLUENCA';");
				getDao(Case.class).executeRaw("UPDATE outbreak SET disease = 'NEW_INFLUENZA' WHERE disease = 'NEW_INFLUENCA';");
				getDao(Case.class).executeRaw("UPDATE person SET causeOfDeathDisease = 'NEW_INFLUENZA' WHERE causeOfDeathDisease = 'NEW_INFLUENCA';");
				getDao(Case.class).executeRaw("UPDATE visits SET disease = 'NEW_INFLUENZA' WHERE disease = 'NEW_INFLUENCA';");
				getDao(Case.class).executeRaw("UPDATE weeklyreportentry SET disease = 'NEW_INFLUENZA' WHERE disease = 'NEW_INFLUENCA';");
				getDao(Case.class).executeRaw("UPDATE clinicalVisit SET disease = 'NEW_INFLUENZA' WHERE disease = 'NEW_INFLUENCA';");
				getDao(Case.class).executeRaw("UPDATE diseaseConfiguration SET disease = 'NEW_INFLUENZA' WHERE disease = 'NEW_INFLUENCA';");
				getDao(Case.class).executeRaw("UPDATE featureConfiguration SET disease = 'NEW_INFLUENZA' WHERE disease = 'NEW_INFLUENCA';");
				getDao(Case.class).executeRaw("UPDATE pathogenTest SET testedDisease = 'NEW_INFLUENZA' WHERE testedDisease = 'NEW_INFLUENCA';");
				getDao(Case.class).executeRaw("UPDATE users SET limitedDisease = 'NEW_INFLUENZA' WHERE limitedDisease = 'NEW_INFLUENCA';");
			case 178:
				currentVersion = 178;
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN completeness float;");
			case 179:
				currentVersion = 179;
				getDao(User.class).executeRaw("ALTER TABLE users ADD COLUMN language varchar(255);");
			case 180:
				currentVersion = 180;
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN additionalDetails varchar(512);");
			case 181:
				currentVersion = 181;
				getDao(Location.class).executeRaw("ALTER TABLE location ADD COLUMN postalCode varchar(255);");
			case 182:
				currentVersion = 182;
				getDao(FeatureConfiguration.class).executeRaw("ALTER TABLE featureConfiguration ADD COLUMN enabled SMALLINT DEFAULT 0;");
			case 183:
				currentVersion = 183;
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN externalID varchar(255);");
				getDao(Contact.class).executeRaw("ALTER TABLE contacts ADD COLUMN externalID varchar(255);");
			case 184:
				currentVersion = 184;
				getDao(Contact.class).executeRaw("ALTER TABLE contacts ADD COLUMN region_id bigint REFERENCES region(id);");
				getDao(Contact.class).executeRaw("ALTER TABLE contacts ADD COLUMN district_id bigint REFERENCES district(id);");
			case 185:
				currentVersion = 185;
				getDao(Contact.class).executeRaw("ALTER TABLE contacts ADD COLUMN highPriority SMALLINT DEFAULT 0;");
				getDao(Contact.class).executeRaw("ALTER TABLE contacts ADD COLUMN immunosuppressiveTherapyBasicDisease varchar(255);");
				getDao(Contact.class).executeRaw("ALTER TABLE contacts ADD COLUMN immunosuppressiveTherapyBasicDiseaseDetails varchar(512);");
				getDao(Contact.class).executeRaw("ALTER TABLE contacts ADD COLUMN careForPeopleOver60 varchar(255);");
			case 186:
				currentVersion = 186;
				getDao(Person.class).executeRaw("ALTER TABLE person ADD COLUMN generalPractitionerDetails varchar(512);");
			case 187:
				currentVersion = 187;
				getDao(Contact.class).executeRaw("ALTER TABLE contacts ADD COLUMN quarantine varchar(255);");
				getDao(Contact.class).executeRaw("ALTER TABLE contacts ADD COLUMN quarantineFrom timestamp;");
				getDao(Contact.class).executeRaw("ALTER TABLE contacts ADD COLUMN quarantineTo timestamp;");
			case 188:
				currentVersion = 188;
				getDao(Contact.class).executeRaw("ALTER TABLE contacts ADD COLUMN diseaseDetails varchar(512);");
				getDao(Contact.class).executeRaw("UPDATE contacts SET caseDisease = (SELECT disease FROM cases WHERE uuid = contacts.caseUuid);");
				getDao(Contact.class)
					.executeRaw("UPDATE contacts SET diseaseDetails = (SELECT diseaseDetails FROM cases WHERE uuid = contacts.caseUuid);");
			case 189:
				currentVersion = 189;
				getDao(Contact.class).executeRaw("ALTER TABLE contacts ADD COLUMN caseIdExternalSystem varchar(255);");
				getDao(Contact.class).executeRaw("ALTER TABLE contacts ADD COLUMN caseOrEventInformation varchar(255);");
			case 190:
				currentVersion = 190;
				getDao(Person.class).executeRaw("ALTER TABLE person ADD COLUMN emailAddress varchar(255);");
				getDao(Person.class).executeRaw("ALTER TABLE person ADD COLUMN passportNumber varchar(255);");
				getDao(Person.class).executeRaw("ALTER TABLE person ADD COLUMN nationalHealthId varchar(255);");
			case 191:
				currentVersion = 191;
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN quarantine varchar(255);");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN quarantineFrom timestamp;");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN quarantineTo timestamp;");
			case 192:
				currentVersion = 192;
				getDao(Contact.class).executeRaw("ALTER TABLE contacts ADD COLUMN contactCategory varchar(255);");
				getDao(Contact.class).executeRaw("ALTER TABLE contacts ADD COLUMN contactProximityDetails varchar(512);");
			case 193:
				currentVersion = 193;
				// Fill report date if necessary; required because the report date was not a mandatory field before
				getDao(Contact.class).executeRaw("UPDATE contacts SET reportDateTime = creationDate WHERE reportDateTime IS NULL;");
			case 194:
				currentVersion = 194;
				getDao(Contact.class).executeRaw("UPDATE contacts SET followUpUntil = quarantineTo WHERE quarantineTo > followUpUntil;");
				getDao(Contact.class).executeRaw("ALTER TABLE contacts ADD COLUMN quarantineOrderMeans varchar(255);");
				getDao(Contact.class).executeRaw("ALTER TABLE contacts ADD COLUMN quarantineHelpNeeded varchar(512);");
			case 195:
				currentVersion = 195;
				getDao(Hospitalization.class).executeRaw("ALTER TABLE hospitalizations ADD COLUMN intensiveCareUnit varchar(255);");
				getDao(Hospitalization.class).executeRaw("ALTER TABLE hospitalizations ADD COLUMN intensiveCareUnitStart timestamp;");
				getDao(Hospitalization.class).executeRaw("ALTER TABLE hospitalizations ADD COLUMN intensiveCareUnitEnd timestamp");
				getDao(Hospitalization.class).executeRaw("UPDATE hospitalizations SET intensiveCareUnit = 'YES' WHERE accommodation = 'ICU';");
			case 196:
				currentVersion = 196;
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN quarantineHelpNeeded varchar(512);");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN quarantineOrderedVerbally SMALLINT DEFAULT 0;");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN quarantineOrderedOfficialDocument SMALLINT DEFAULT 0;");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN quarantineOrderedVerballyDate timestamp;");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN quarantineOrderedOfficialDocumentDate timestamp;");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN quarantineHomePossible varchar(255);");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN quarantineHomePossibleComment varchar(512);");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN quarantineHomeSupplyEnsured varchar(255);");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN quarantineHomeSupplyEnsuredComment varchar(512);");
				getDao(Contact.class).executeRaw("ALTER TABLE contacts ADD COLUMN quarantineOrderedVerbally SMALLINT DEFAULT 0;");
				getDao(Contact.class).executeRaw("ALTER TABLE contacts ADD COLUMN quarantineOrderedOfficialDocument SMALLINT DEFAULT 0;");
				getDao(Contact.class).executeRaw("ALTER TABLE contacts ADD COLUMN quarantineOrderedVerballyDate timestamp;");
				getDao(Contact.class).executeRaw("ALTER TABLE contacts ADD COLUMN quarantineOrderedOfficialDocumentDate timestamp;");
				getDao(Contact.class).executeRaw("ALTER TABLE contacts ADD COLUMN quarantineHomePossible varchar(255);");
				getDao(Contact.class).executeRaw("ALTER TABLE contacts ADD COLUMN quarantineHomePossibleComment varchar(512);");
				getDao(Contact.class).executeRaw("ALTER TABLE contacts ADD COLUMN quarantineHomeSupplyEnsured varchar(255);");
				getDao(Contact.class).executeRaw("ALTER TABLE contacts ADD COLUMN quarantineHomeSupplyEnsuredComment varchar(512);");
				getDao(Contact.class)
					.executeRaw("UPDATE contacts SET quarantineOrderedVerbally = CASE WHEN quarantineOrderMeans = 'VERBALLY' THEN 1 ELSE 0 END;");
				getDao(Contact.class).executeRaw(
					"UPDATE contacts SET quarantineOrderedOfficialDocument = CASE WHEN quarantineOrderMeans = 'OFFICIAL_DOCUMENT' THEN 1 ELSE 0 END;");
			case 197:
				currentVersion = 197;
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN reportingType varchar(255);");
			case 198:
				currentVersion = 198;
				getDao(Sample.class).executeRaw("ALTER TABLE samples ADD COLUMN fieldSampleID varchar(512);");
			case 199:
				currentVersion = 199;
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN lossOfTaste varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN lossOfSmell varchar(255);");
			case 200:
				currentVersion = 200;
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN coughWithSputum varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN coughWithHeamoptysis varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN lymphadenopathy varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN wheezing varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN skinUlcers varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN inabilityToWalk varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN inDrawingOfChestWall varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN otherComplications varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN otherComplicationsText varchar(255);");

				getDao(Symptoms.class).executeRaw("ALTER TABLE healthconditions ADD COLUMN obesity varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE healthconditions ADD COLUMN currentSmoker varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE healthconditions ADD COLUMN formerSmoker varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE healthconditions ADD COLUMN asthma varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE healthconditions ADD COLUMN sickleCellDisease varchar(255);");
			case 201:
				currentVersion = 201;
				getDao(Contact.class).executeRaw("ALTER TABLE contacts ADD COLUMN additionalDetails varchar(512);");
			case 202:
				currentVersion = 202;
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN postpartum varchar(255);");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN trimester varchar(255);");
			case 203:
				currentVersion = 203;
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN respiratoryDiseaseVentilation varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN generalSignsOfDisease varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN fastHeartRate varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN oxygenSaturationLower94 varchar(255);");
				getDao(HealthConditions.class).executeRaw("ALTER TABLE healthConditions ADD COLUMN immunodeficiencyIncludingHiv varchar(255);");
			case 204:
				currentVersion = 204;
				getDao(Sample.class).executeRaw("ALTER TABLE samples ADD COLUMN associatedContact_id bigint REFERENCES contact (id);");
			case 205:
				currentVersion = 205;
				getDao(Contact.class).executeRaw("UPDATE contacts SET contactProximity = 'MEDICAL_SAFE' WHERE contactProximity = 'MEDICAL_SAVE';");
				getDao(Contact.class)
					.executeRaw("UPDATE contacts SET contactProximity = 'MEDICAL_UNSAFE' WHERE contactProximity = 'MEDICAL_UNSAVE';");
			case 206:
				currentVersion = 206;
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN pseudonymized boolean;");
				getDao(Person.class).executeRaw("ALTER TABLE person ADD COLUMN pseudonymized boolean;");
				getDao(Location.class).executeRaw("ALTER TABLE location ADD COLUMN pseudonymized boolean;");
			case 207:
				currentVersion = 207;
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN quarantineTypeDetails varchar(512);");
				getDao(Contact.class).executeRaw("ALTER TABLE contacts ADD COLUMN quarantineTypeDetails varchar(512);");
			case 208:
				currentVersion = 208;
				getDao(Sample.class)
					.executeRaw("ALTER TABLE samples ADD COLUMN associatedEventParticipant_id bigint REFERENCES eventParticipants (id);");
			case 209:
				currentVersion = 209;

				getDao(Event.class).executeRaw("UPDATE events set eventStatus='SIGNAL' where eventStatus='POSSIBLE'");
				getDao(Event.class).executeRaw("UPDATE events set eventStatus='EVENT' where eventStatus='CONFIRMED'");
				getDao(Event.class).executeRaw("UPDATE events set eventStatus='DROPPED' where eventStatus='NO_EVENT'");

				Cursor dbCursor = db.query(Event.TABLE_NAME, null, null, null, null, null, null);
				String[] columnNames = dbCursor.getColumnNames();
				dbCursor.close();

				String queryColumns = TextUtils.join(",", columnNames);

				getDao(Event.class).executeRaw("ALTER TABLE events RENAME TO tmp_events;");

				getDao(Event.class).executeRaw(
					"CREATE TABLE events (" + "		disease VARCHAR," + "		diseaseDetails VARCHAR," + "		endDate BIGINT,"
						+ "		eventDesc VARCHAR," + "		eventLocation_id BIGINT," + "		eventStatus VARCHAR," + "		eventType VARCHAR,"
						+ "		externalId VARCHAR," + "		nosocomial VARCHAR," + "		reportDateTime BIGINT,"
						+ "		reportLat DOUBLE PRECISION," + "		reportLatLonAccuracy FLOAT," + "		reportLon DOUBLE PRECISION,"
						+ "		reportingUser_id BIGINT," + "		srcEmail VARCHAR," + "		srcFirstName VARCHAR," + "		srcLastName VARCHAR,"
						+ "		srcMediaDetails VARCHAR," + "		srcMediaName VARCHAR," + "		srcMediaWebsite VARCHAR,"
						+ "		srcTelNo VARCHAR," + "		srcType VARCHAR," + "		startDate BIGINT," + "		surveillanceOfficer_id BIGINT,"
						+ "		typeOfPlace VARCHAR," + "		typeOfPlaceText VARCHAR," + "		changeDate BIGINT NOT NULL,"
						+ "		creationDate BIGINT NOT NULL," + "		id INTEGER PRIMARY KEY AUTOINCREMENT," + "		lastOpenedDate BIGINT,"
						+ "		localChangeDate BIGINT NOT NULL," + "		modified SMALLINT," + "		snapshot SMALLINT,"
						+ "		uuid VARCHAR NOT NULL," + "		UNIQUE (snapshot ASC, uuid ASC)" + ");");

				db.execSQL("INSERT INTO events (" + queryColumns.replace("eventDate", "startDate") + ") SELECT " + queryColumns + " FROM tmp_events");
				db.execSQL("DROP TABLE tmp_events;");

				getDao(Event.class).executeRaw(
					"UPDATE events set srcType='HOTLINE_PERSON' where length(ifnull(srcFirstName,'')||ifnull(srcLastName,'')||ifnull(srcTelNo,'')||ifnull(srcEmail,'')) > 0;");
			case 210:
				currentVersion = 210;
				getDao(Sample.class).executeRaw("ALTER TABLE contacts ADD COLUMN epiData_id bigint REFERENCES epidata (id);");
				getDao(Contact.class).executeRaw("UPDATE contacts SET changeDate = 0 WHERE changeDate IS NOT NULL;");
				getDao(EpiData.class).executeRaw("UPDATE epidata SET changeDate = 0 WHERE changeDate IS NOT NULL;");
			case 211:
				// Re-synchronize all contacts and epi data to prevent missing embedded entities
				currentVersion = 211;
				getDao(Contact.class).executeRaw("UPDATE contacts SET changeDate = 0 WHERE changeDate IS NOT NULL;");
				getDao(EpiData.class).executeRaw("UPDATE epidata SET changeDate = 0 WHERE changeDate IS NOT NULL;");
				getDao(Location.class).executeRaw("UPDATE location SET changeDate = 0 WHERE changeDate IS NOT NULL;");
			case 212:
				// Re-synchronize all contacts and epi data to prevent missing embedded entities
				getDao(Case.class).executeRaw("UPDATE cases SET changeDate = 0 WHERE changeDate IS NOT NULL;");
				getDao(Contact.class).executeRaw("UPDATE contacts SET changeDate = 0 WHERE changeDate IS NOT NULL;");
				getDao(Therapy.class).executeRaw("UPDATE therapy SET changeDate = 0 WHERE changeDate IS NOT NULL;");
				getDao(ClinicalCourse.class).executeRaw("UPDATE clinicalCourse SET changeDate = 0 WHERE changeDate IS NOT NULL;");
				getDao(HealthConditions.class).executeRaw("UPDATE healthConditions SET changeDate = 0 WHERE changeDate IS NOT NULL;");
				getDao(Symptoms.class).executeRaw("UPDATE symptoms SET changeDate = 0 WHERE changeDate IS NOT NULL;");
				getDao(Hospitalization.class).executeRaw("UPDATE hospitalizations SET changeDate = 0 WHERE changeDate IS NOT NULL;");
				getDao(PreviousHospitalization.class).executeRaw("UPDATE previoushospitalizations SET changeDate = 0 WHERE changeDate IS NOT NULL;");
				getDao(EpiData.class).executeRaw("UPDATE epidata SET changeDate = 0 WHERE changeDate IS NOT NULL;");
				getDao(MaternalHistory.class).executeRaw("UPDATE maternalHistory SET changeDate = 0 WHERE changeDate IS NOT NULL;");
				getDao(PortHealthInfo.class).executeRaw("UPDATE portHealthInfo SET changeDate = 0 WHERE changeDate IS NOT NULL;");
				getDao(Location.class).executeRaw("UPDATE location SET changeDate = 0 WHERE changeDate IS NOT NULL;");
				getDao(Event.class).executeRaw(
					"UPDATE events set srcType='HOTLINE_PERSON' where length(ifnull(srcFirstName,'')||ifnull(srcLastName,'')||ifnull(srcTelNo,'')||ifnull(srcEmail,'')) > 0;");
			case 213:
				currentVersion = 213;
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN clinicalConfirmation varchar(255);");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN epidemiologicalConfirmation varchar(255);");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN laboratoryDiagnosticConfirmation varchar(255);");

			case 214:
				currentVersion = 214;
				getDao(Contact.class).executeRaw("ALTER TABLE contacts ADD COLUMN contactIdentificationSource varchar(255);");
				getDao(Contact.class).executeRaw("ALTER TABLE contacts ADD COLUMN contactIdentificationSourceDetails varchar(512);");
				getDao(Contact.class).executeRaw("ALTER TABLE contacts ADD COLUMN tracingApp varchar(255);");
				getDao(Contact.class).executeRaw("ALTER TABLE contacts ADD COLUMN tracingAppDetails varchar(512);");
			case 215:
				currentVersion = 215;
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN quarantineExtended boolean;");
				getDao(Contact.class).executeRaw("ALTER TABLE contacts ADD COLUMN quarantineExtended boolean;");
			case 216:
				currentVersion = 216;
				getDao(Contact.class).executeRaw("ALTER TABLE contacts ADD COLUMN community_id bigint;");
			case 217:
				currentVersion = 217;
				getDao(PathogenTest.class).executeRaw("ALTER TABLE pathogenTest ADD COLUMN pseudonymized boolean;");
				getDao(PreviousHospitalization.class).executeRaw("ALTER TABLE previoushospitalizations ADD COLUMN pseudonymized boolean;");
				getDao(EpiData.class).executeRaw("ALTER TABLE epidata ADD COLUMN pseudonymized boolean;");
				getDao(MaternalHistory.class).executeRaw("ALTER TABLE maternalHistory ADD COLUMN pseudonymized boolean;");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN pseudonymized boolean;");
				getDao(HealthConditions.class).executeRaw("ALTER TABLE healthConditions ADD COLUMN pseudonymized boolean;");
				getDao(Contact.class).executeRaw("ALTER TABLE contacts ADD COLUMN pseudonymized boolean;");
				getDao(Visit.class).executeRaw("ALTER TABLE visits ADD COLUMN pseudonymized boolean;");
				getDao(ClinicalVisit.class).executeRaw("ALTER TABLE clinicalVisit ADD COLUMN pseudonymized boolean;");
				getDao(Treatment.class).executeRaw("ALTER TABLE treatment ADD COLUMN pseudonymized boolean;");
				getDao(Prescription.class).executeRaw("ALTER TABLE prescription ADD COLUMN pseudonymized boolean;");
				getDao(Sample.class).executeRaw("ALTER TABLE samples ADD COLUMN pseudonymized boolean;");
				getDao(Event.class).executeRaw("ALTER TABLE events ADD COLUMN pseudonymized boolean;");
				getDao(EventParticipant.class).executeRaw("ALTER TABLE eventParticipants ADD COLUMN pseudonymized boolean;");

			case 218:
				currentVersion = 218;
				Cursor visitDbCursor = db.query(Visit.TABLE_NAME, null, null, null, null, null, null);
				String[] visitColumnNames = visitDbCursor.getColumnNames();
				visitDbCursor.close();
				String visitQueryColumns = TextUtils.join(",", visitColumnNames);

				db.execSQL("ALTER TABLE visits RENAME TO visits_old;");

				getDao(Visit.class).executeRaw(
					"CREATE TABLE visits (" + "		disease VARCHAR," + "		person_id BIGINT NOT NULL," + "		reportLat DOUBLE PRECISION,"
						+ "		reportLatLonAccuracy FLOAT," + "		reportLon DOUBLE PRECISION," + "		symptoms_id BIGINT,"
						+ "		visitDateTime BIGINT NOT NULL," + "		visitRemarks VARCHAR," + "		visitStatus VARCHAR,"
						+ "		visitUser_id BIGINT," + "		pseudonymized SMALLINT," + "		changeDate BIGINT NOT NULL,"
						+ "		creationDate BIGINT NOT NULL," + "		id INTEGER PRIMARY KEY AUTOINCREMENT," + "		lastOpenedDate BIGINT,"
						+ "		localChangeDate BIGINT NOT NULL," + "		modified SMALLINT," + "		snapshot SMALLINT,"
						+ "		uuid VARCHAR NOT NULL," + "		UNIQUE (snapshot ASC, uuid ASC)" + ");");

				db.execSQL("INSERT INTO visits (" + visitQueryColumns + ") SELECT " + visitQueryColumns + " FROM visits_old;");
				db.execSQL("DROP TABLE visits_old;");

			case 219:
				currentVersion = 219;
				getDao(Sample.class).executeRaw(
					"UPDATE samples SET lab_id = (SELECT id FROM facility WHERE uuid = 'SORMAS-CONSTID-OTHERS-FACILITY') WHERE lab_id = (SELECT id FROM facility WHERE uuid = 'SORMAS-CONSTID-OTHERS-LABORATO');");
				getDao(PathogenTest.class).executeRaw(
					"UPDATE pathogenTest SET lab_id = (SELECT id FROM facility WHERE uuid = 'SORMAS-CONSTID-OTHERS-FACILITY') WHERE lab_id = (SELECT id FROM facility WHERE uuid = 'SORMAS-CONSTID-OTHERS-LABORATO');");
				getDao(Facility.class).executeRaw("DELETE FROM facility WHERE uuid = 'SORMAS-CONSTID-OTHERS-LABORATO';");
				getDao(Facility.class).executeRaw(
					"UPDATE facility SET type = 'HOSPITAL' WHERE type = null AND uuid NOT IN ('SORMAS-CONSTID-OTHERS-FACILITY','SORMAS-CONSTID-ISNONE-FACILITY');");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN facilityType varchar(255);");
				getDao(Case.class).executeRaw(
					"UPDATE cases SET facilityType = 'HOSPITAL' WHERE healthFacility_id != (SELECT id FROM facility WHERE uuid = 'SORMAS-CONSTID-ISNONE-FACILITY');");
				getDao(Person.class).executeRaw("ALTER TABLE person ADD COLUMN occupationFacilityType varchar(255);");
				getDao(Person.class).executeRaw("UPDATE person SET occupationFacilityType = 'HOSPITAL' WHERE occupationFacility_id IS NOT NULL;");
				getDao(Person.class).executeRaw("ALTER TABLE person ADD COLUMN placeOfBirthFacilityType varchar(255);");
				getDao(Person.class).executeRaw("UPDATE person SET placeOfBirthFacilityType = 'HOSPITAL' WHERE placeOfBirthFacility_id IS NOT NULL;");

			case 220:
				currentVersion = 220;
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN feverishFeeling varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN weakness varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN fatigue varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN coughWithoutSputum varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN breathlessness varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN chestPressure varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN blueLips varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN bloodCirculationProblems varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN palpitations varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN dizzinessStandingUp varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN highOrLowBloodPressure varchar(255);");
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN urinaryRetention varchar(255);");

			case 221:
				currentVersion = 221;
				getDao(Contact.class).executeRaw("ALTER TABLE contacts ADD COLUMN healthConditions_id bigint REFERENCES healthConditions(id);");

			case 222:
				currentVersion = 222;
				// Re-synchronize all contacts
				getDao(Case.class).executeRaw("UPDATE cases SET changeDate = 0 WHERE changeDate IS NOT NULL;");
				getDao(Contact.class).executeRaw("UPDATE contacts SET changeDate = 0 WHERE changeDate IS NOT NULL;");
				getDao(Therapy.class).executeRaw("UPDATE therapy SET changeDate = 0 WHERE changeDate IS NOT NULL;");
				getDao(ClinicalCourse.class).executeRaw("UPDATE clinicalCourse SET changeDate = 0 WHERE changeDate IS NOT NULL;");
				getDao(HealthConditions.class).executeRaw("UPDATE healthConditions SET changeDate = 0 WHERE changeDate IS NOT NULL;");
				getDao(Symptoms.class).executeRaw("UPDATE symptoms SET changeDate = 0 WHERE changeDate IS NOT NULL;");
				getDao(Hospitalization.class).executeRaw("UPDATE hospitalizations SET changeDate = 0 WHERE changeDate IS NOT NULL;");
				getDao(PreviousHospitalization.class).executeRaw("UPDATE previoushospitalizations SET changeDate = 0 WHERE changeDate IS NOT NULL;");
				getDao(EpiData.class).executeRaw("UPDATE epidata SET changeDate = 0 WHERE changeDate IS NOT NULL;");
				getDao(MaternalHistory.class).executeRaw("UPDATE maternalHistory SET changeDate = 0 WHERE changeDate IS NOT NULL;");
				getDao(PortHealthInfo.class).executeRaw("UPDATE portHealthInfo SET changeDate = 0 WHERE changeDate IS NOT NULL;");
				getDao(Location.class).executeRaw("UPDATE location SET changeDate = 0 WHERE changeDate IS NOT NULL;");

			case 223:
				currentVersion = 223;
				getDao(Location.class).executeRaw("ALTER TABLE location RENAME TO tmp_location;");
				getDao(Location.class).executeRaw(
					"CREATE TABLE location(street	VARCHAR, areaType VARCHAR, city VARCHAR, community_id BIGINT, details VARCHAR, district_id BIGINT, latLonAccuracy FLOAT, "
						+ "latitude DOUBLE PRECISION, longitude DOUBLE PRECISION, postalCode VARCHAR, region_id BIGINT, pseudonymized SMALLINT, changeDate BIGINT NOT NULL, creationDate BIGINT NOT NULL, id	INTEGER, "
						+ "lastOpenedDate BIGINT, localChangeDate BIGINT NOT NULL, modified SMALLINT, snapshot SMALLINT, uuid VARCHAR NOT NULL, PRIMARY KEY(id AUTOINCREMENT), UNIQUE(snapshot, uuid));");
				getDao(Location.class).executeRaw(
					"INSERT INTO location(street, areaType, city, community_id, details, district_id, latLonAccuracy, latitude, longitude, postalCode, region_id, pseudonymized, changeDate, creationDate, id, "
						+ "lastOpenedDate, localChangeDate, modified, snapshot, uuid) SELECT address, areaType, city, community_id, details, district_id, latLonAccuracy, latitude, longitude, postalCode, region_id, "
						+ "pseudonymized, changeDate, creationDate, id, lastOpenedDate, localChangeDate, modified, snapshot, uuid FROM tmp_location;");
				getDao(Location.class).executeRaw("DROP TABLE tmp_location;");

				getDao(Location.class).executeRaw("ALTER TABLE location ADD COLUMN houseNumber varchar(255);");
				getDao(Location.class).executeRaw("ALTER TABLE location ADD COLUMN additionalInformation varchar(255);");
				getDao(Location.class).executeRaw("ALTER TABLE location ADD COLUMN addressType varchar(255);");
				getDao(Location.class).executeRaw("ALTER TABLE location ADD COLUMN addressTypeDetails varchar(255);");

			case 224:
				currentVersion = 224;
				getDao(EventParticipant.class).executeRaw("ALTER TABLE eventParticipants ADD COLUMN reportingUser_id bigint REFERENCES users(id);");

			case 225:
				currentVersion = 225;
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN quarantineOfficialOrderSent SMALLINT DEFAULT 0;");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN quarantineOfficialOrderSentDate timestamp;");
				getDao(Case.class).executeRaw("ALTER TABLE contacts ADD COLUMN quarantineOfficialOrderSent SMALLINT DEFAULT 0;");
				getDao(Case.class).executeRaw("ALTER TABLE contacts ADD COLUMN quarantineOfficialOrderSentDate timestamp;");

			case 226:
				currentVersion = 226;
				// Re-synchronize persons to retrieve new addresses
				getDao(Location.class).executeRaw("ALTER TABLE location ADD COLUMN person_id bigint REFERENCES person(id);");
				getDao(Person.class).executeRaw("UPDATE person SET changeDate = 0 WHERE changeDate IS NOT NULL;");
				getDao(Location.class).executeRaw("UPDATE location SET changeDate = 0 WHERE changeDate IS NOT NULL;");

			case 227:
				currentVersion = 227;
				getDao(Contact.class).executeRaw("ALTER TABLE contacts ADD COLUMN quarantineReduced boolean DEFAULT false;");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN quarantineReduced boolean DEFAULT false;");

			case 228:
				currentVersion = 228;
				getDao(Person.class).executeRaw("ALTER TABLE person ADD COLUMN externalId varchar(255);");

			case 229:
				currentVersion = 229;
				getDao(Event.class).executeRaw("ALTER TABLE events ADD COLUMN eventTitle varchar(512);");

			case 230:
				currentVersion = 230;

				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN caseIdIsm integer;");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN covidTestReason varchar(255);");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN covidTestReasonDetails varchar(512);");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN contactTracingFirstContactType varchar(255);");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN contactTracingFirstContactDate timestamp;");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN quarantineReasonBeforeIsolation varchar(255);");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN quarantineReasonBeforeIsolationDetails varchar(512);");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN endOfIsolationReason varchar(255);");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN endOfIsolationReasonDetails varchar(512);");

			case 231:
				currentVersion = 231;

				getDao(Location.class).executeRaw("ALTER TABLE location ADD COLUMN facilityType varchar(255);");
				getDao(Location.class).executeRaw("ALTER TABLE location ADD COLUMN facility_id bigint REFERENCES facility(id);");
				getDao(Location.class).executeRaw("ALTER TABLE location ADD COLUMN facilityDetails varchar(512);");

				GenericRawResults<Object[]> rawResult = getDao(Person.class).queryRaw(
					"SELECT occupationRegion_id, occupationDistrict_id, occupationCommunity_id, occupationFacility_id, occupationFacilityDetails, occupationFacilityType, id FROM person WHERE changeDate IS 0 AND (occupationRegion_id IS NOT NULL OR occupationFacilityType IS NOT NULL);",
					new DataType[] {
						DataType.BIG_INTEGER,
						DataType.BIG_INTEGER,
						DataType.BIG_INTEGER,
						DataType.BIG_INTEGER,
						DataType.STRING,
						DataType.ENUM_STRING,
						DataType.INTEGER });

				for (Object[] result : rawResult) {
					if (DataHelper.isNullOrEmpty((String) result[4])) {
						Array.set(result, 4, null);
					} else {
						Array.set(result, 4, "'" + result[4] + "'");
					}
					if (!DataHelper.isNullOrEmpty((String) result[5])) {
						Array.set(result, 5, "'" + result[5] + "'");
					}
					String query =
						"INSERT INTO location (uuid, changeDate, localChangeDate, creationDate, region_id, district_id, community_id, facility_id, facilityDetails, facilityType, addressType, person_id, pseudonymized, modified, snapshot) VALUES ('"
							+ DataHelper.createUuid()
							+ "', 0, CAST(ROUND((julianday('now') - 2440587.5)*86400000) As INTEGER), CAST(ROUND((julianday('now') - 2440587.5)*86400000) As INTEGER), "
							+ result[0] + ", " + result[1] + ", " + result[2] + ", " + result[3] + ", " + result[4] + ", " + result[5]
							+ ", 'PLACE_OF_WORK', " + result[6] + ", 0, 0, 0);";
					getDao(Location.class).executeRaw(query);
				}

				Cursor personDbCursor = db.query(Person.TABLE_NAME, null, null, null, null, null, null);
				String[] personColumnNames = personDbCursor.getColumnNames();
				personDbCursor.close();
				List personColumnList = new ArrayList(Arrays.asList(personColumnNames));
				personColumnList.removeAll(
					Arrays.asList(
						"occupationRegion_id",
						"occupationDistrict_id",
						"occupationCommunity_id",
						"occupationFacility_id",
						"occupationFacilityDetails",
						"occupationFacilityType"));
				String personQueryColumns = TextUtils.join(",", personColumnList);

				db.execSQL("ALTER TABLE person RENAME TO person_old;");

				getDao(Person.class).executeRaw(
					"CREATE TABLE person (" + "		address_id BIGINT," + "		approximateAge INTEGER,"
						+ "		approximateAgeReferenceDate BIGINT," + "		approximateAgeType VARCHAR," + "		birthWeight INTEGER,"
						+ "		birthdateDD INTEGER," + "		birthdateMM INTEGER," + "		birthdateYYYY INTEGER,"
						+ "		burialConductor VARCHAR," + "		burialDate BIGINT," + "		burialPlaceDescription VARCHAR,"
						+ "		causeOfDeath VARCHAR," + "		causeOfDeathDetails VARCHAR," + "		causeOfDeathDisease VARCHAR,"
						+ "		deathDate BIGINT," + "		deathPlaceDescription VARCHAR," + "		deathPlaceType VARCHAR,"
						+ "		educationDetails VARCHAR," + "		educationType VARCHAR," + "		emailAddress VARCHAR,"
						+ "		externalId VARCHAR," + "		fathersName VARCHAR," + "		firstName VARCHAR NOT NULL,"
						+ "		generalPractitionerDetails VARCHAR," + "		gestationAgeAtBirth INTEGER," + "		lastName VARCHAR NOT NULL,"
						+ "		mothersMaidenName VARCHAR," + "		mothersName VARCHAR," + "		nationalHealthId VARCHAR,"
						+ "		nickname VARCHAR," + "		occupationDetails VARCHAR," + "		occupationType VARCHAR,"
						+ "		passportNumber VARCHAR," + "		phone VARCHAR," + "		phoneOwner VARCHAR,"
						+ "		placeOfBirthCommunity_id BIGINT," + "		placeOfBirthDistrict_id BIGINT,"
						+ "		placeOfBirthFacility_id BIGINT," + "		placeOfBirthFacilityDetails VARCHAR,"
						+ "		placeOfBirthFacilityType VARCHAR," + "		placeOfBirthRegion_id BIGINT," + "		presentCondition VARCHAR,"
						+ "		sex VARCHAR," + "		pseudonymized SMALLINT," + "		changeDate BIGINT NOT NULL,"
						+ "		creationDate BIGINT NOT NULL," + "		id INTEGER PRIMARY KEY AUTOINCREMENT," + "		lastOpenedDate BIGINT,"
						+ "		localChangeDate BIGINT NOT NULL," + "		modified SMALLINT," + "		snapshot SMALLINT,"
						+ "		uuid VARCHAR NOT NULL," + "		UNIQUE (snapshot ASC, uuid ASC)" + ");");

				db.execSQL("INSERT INTO person (" + personQueryColumns + ") SELECT " + personQueryColumns + " FROM person_old;");
				db.execSQL("DROP TABLE person_old;");

			case 232:
				currentVersion = 232;
				getDao(Symptoms.class).executeRaw("ALTER TABLE symptoms ADD COLUMN shivering varchar(255);");

			case 233:
				currentVersion = 233;
				getDao(Contact.class).executeRaw("ALTER TABLE contacts ADD COLUMN endOfQuarantineReason varchar(255);");
				getDao(Contact.class).executeRaw("ALTER TABLE contacts ADD COLUMN endOfQuarantineReasonDetails varchar(512);");

			case 234:
				currentVersion = 234;

				getDao(SormasToSormasOriginInfo.class).executeRaw(
					"CREATE TABLE sormasToSormasOriginInfo (" + "		comment VARCHAR," + "		organizationId VARCHAR,"
						+ "		ownershipHandedOver SMALLINT," + "		senderEmail VARCHAR," + "		senderName VARCHAR,"
						+ "		senderPhoneNumber VARCHAR," + "		changeDate BIGINT NOT NULL," + "		creationDate BIGINT NOT NULL,"
						+ "		id INTEGER PRIMARY KEY AUTOINCREMENT," + "		lastOpenedDate BIGINT," + "		localChangeDate BIGINT NOT NULL,"
						+ "		modified SMALLINT," + "		snapshot SMALLINT," + "		uuid VARCHAR NOT NULL,"
						+ "		UNIQUE (snapshot ASC, uuid ASC)" + ");");

				getDao(Case.class)
					.executeRaw("ALTER TABLE cases ADD COLUMN sormasToSormasOriginInfo_id bigint REFERENCES sormasToSormasOriginInfo(id);");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN ownershipHandedOver boolean;");

				getDao(Contact.class)
					.executeRaw("ALTER TABLE contacts ADD COLUMN sormasToSormasOriginInfo_id bigint REFERENCES sormasToSormasOriginInfo(id);");
				getDao(Contact.class).executeRaw("ALTER TABLE contacts ADD COLUMN ownershipHandedOver boolean;");

			case 235:
				currentVersion = 235;
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN wasInQuarantineBeforeIsolation varchar(255);");

			case 236:
				currentVersion = 236;
				getDao(Contact.class).executeRaw("ALTER TABLE contacts ADD COLUMN returningTraveler varchar(255);");

			case 237:
				currentVersion = 237;

				getDao(Campaign.class).executeRaw(
					"CREATE TABLE campaigns (" + "		archived SMALLINT," + "		creatingUser_id BIGINT," + "		description VARCHAR,"
						+ "		endDate BIGINT," + "		name VARCHAR," + "		startDate BIGINT," + "		pseudonymized SMALLINT,"
						+ "		changeDate BIGINT NOT NULL," + "		creationDate BIGINT NOT NULL," + "		id INTEGER PRIMARY KEY AUTOINCREMENT,"
						+ "		lastOpenedDate BIGINT," + "		localChangeDate BIGINT NOT NULL," + "		modified SMALLINT,"
						+ "		snapshot SMALLINT," + "		uuid VARCHAR NOT NULL," + "		UNIQUE (snapshot ASC, uuid ASC)" + ");");

				getDao(CampaignFormMeta.class).executeRaw(
					"CREATE TABLE campaignformmeta (" + "		campaignFormElements VARCHAR," + "		campaignFormTranslations VARCHAR,"
						+ "		formId VARCHAR," + "		formName VARCHAR," + "		languageCode VARCHAR," + "		pseudonymized SMALLINT,"
						+ "		changeDate BIGINT NOT NULL," + "		creationDate BIGINT NOT NULL," + "		id INTEGER PRIMARY KEY AUTOINCREMENT,"
						+ "		lastOpenedDate BIGINT," + "		localChangeDate BIGINT NOT NULL," + "		modified SMALLINT,"
						+ "		snapshot SMALLINT," + "		uuid VARCHAR NOT NULL," + "		UNIQUE (snapshot ASC, uuid ASC)" + ");");

				getDao(CampaignFormData.class).executeRaw(
					"CREATE TABLE campaignFormData (" + "		archived SMALLINT," + "		campaign_id BIGINT,"
						+ "		campaignFormMeta_id BIGINT," + "		community_id BIGINT," + "		creatingUser_id BIGINT,"
						+ "		district_id BIGINT," + "		formDate BIGINT," + "		formValues VARCHAR," + "		region_id BIGINT,"
						+ "		pseudonymized SMALLINT," + "		changeDate BIGINT NOT NULL," + "		creationDate BIGINT NOT NULL,"
						+ "		id INTEGER PRIMARY KEY AUTOINCREMENT," + "		lastOpenedDate BIGINT," + "		localChangeDate BIGINT NOT NULL,"
						+ "		modified SMALLINT," + "		snapshot SMALLINT," + "		uuid VARCHAR NOT NULL,"
						+ "		UNIQUE (snapshot ASC, uuid ASC)" + ");");

			case 238:
				currentVersion = 238;
				getDao(Visit.class).executeRaw("ALTER TABLE visits ADD COLUMN origin varchar(255);");
				getDao(Visit.class).executeRaw("UPDATE visits SET origin='USER'");

			case 239:
				currentVersion = 239;
				getDao(Sample.class)
					.executeRaw("ALTER TABLE samples ADD COLUMN sormasToSormasOriginInfo_id bigint REFERENCES sormasToSormasOriginInfo(id);");
				getDao(Sample.class).executeRaw("ALTER TABLE samples ADD COLUMN ownershipHandedOver boolean;");

			case 240:
				currentVersion = 240;
				getDao(Event.class).executeRaw("ALTER TABLE events ADD COLUMN eventInvestigationStatus varchar(255);");
				getDao(Event.class).executeRaw("ALTER TABLE events ADD COLUMN eventInvestigationStartDate timestamp;");
				getDao(Event.class).executeRaw("ALTER TABLE events ADD COLUMN eventInvestigationEndDate timestamp;");

			case 241:
				currentVersion = 241;
				getDao(DiseaseConfiguration.class).executeRaw("ALTER TABLE diseaseConfiguration ADD COLUMN caseFollowUpDuration integer;");
				getDao(DiseaseConfiguration.class)
					.executeRaw("ALTER TABLE diseaseConfiguration ADD COLUMN eventParticipantFollowUpDuration integer;");
				getDao(DiseaseConfiguration.class).executeRaw("UPDATE diseaseConfiguration SET caseFollowUpDuration = followUpDuration;");
				getDao(DiseaseConfiguration.class).executeRaw("UPDATE diseaseConfiguration SET eventParticipantFollowUpDuration = followUpDuration;");

			case 242:
				currentVersion = 242;

				getDao(Country.class).executeRaw(
					"CREATE TABLE IF NOT EXISTS country (" + "		isoCode VARCHAR," + "		name VARCHAR," + "		archived SMALLINT,"
						+ "		changeDate BIGINT NOT NULL," + "		creationDate BIGINT NOT NULL," + "		id INTEGER PRIMARY KEY AUTOINCREMENT,"
						+ "		lastOpenedDate BIGINT," + "		localChangeDate BIGINT NOT NULL," + "		modified SMALLINT,"
						+ "		snapshot SMALLINT," + "		uuid VARCHAR NOT NULL," + "		UNIQUE (snapshot ASC, uuid ASC)" + ");");

			case 243:
				currentVersion = 243;

				getDao(Exposure.class).executeRaw(
					"CREATE TABLE exposures (" + "		animalCondition VARCHAR," + "		animalContactType VARCHAR,"
						+ "		animalContactTypeDetails text," + "		animalMarket VARCHAR," + "		animalVaccinated VARCHAR,"
						+ "		bodyOfWater VARCHAR," + "		connectionNumber VARCHAR," + "		contactToBodyFluids VARCHAR,"
						+ "		contactToCase_id BIGINT," + "		deceasedPersonIll VARCHAR," + "		deceasedPersonName VARCHAR,"
						+ "		deceasedPersonRelation VARCHAR," + "		description text," + "		eatingRawAnimalProducts VARCHAR,"
						+ "		endDate BIGINT," + "		epiData_id BIGINT," + "		exposureType VARCHAR," + "		exposureTypeDetails text,"
						+ "		gatheringDetails text," + "		gatheringType VARCHAR," + "		habitationDetails text,"
						+ "		habitationType VARCHAR," + "		handlingAnimals VARCHAR," + "		handlingSamples VARCHAR,"
						+ "		indoors VARCHAR," + "		location_id BIGINT," + "		longFaceToFaceContact VARCHAR,"
						+ "		meansOfTransport VARCHAR," + "		meansOfTransportDetails text," + "		otherProtectiveMeasures VARCHAR,"
						+ "		outdoors VARCHAR," + "		percutaneous VARCHAR," + "		physicalContactDuringPreparation VARCHAR,"
						+ "		physicalContactWithBody VARCHAR," + "		prophylaxis VARCHAR," + "		prophylaxisDate BIGINT,"
						+ "		protectiveMeasuresDetails text," + "		reportingUser_id BIGINT," + "		riskArea VARCHAR,"
						+ "		seatNumber VARCHAR," + "		shortDistance VARCHAR," + "		startDate BIGINT," + "		typeOfAnimal VARCHAR,"
						+ "		typeOfAnimalDetails text," + "		typeOfPlace VARCHAR," + "		typeOfPlaceDetails text,"
						+ "		waterSource VARCHAR," + "		waterSourceDetails text," + "		wearingMask VARCHAR,"
						+ "		wearingPpe VARCHAR," + "		pseudonymized SMALLINT," + "		changeDate BIGINT NOT NULL,"
						+ "		creationDate BIGINT NOT NULL," + "		id INTEGER PRIMARY KEY AUTOINCREMENT," + "		lastOpenedDate BIGINT,"
						+ "		localChangeDate BIGINT NOT NULL," + "		modified SMALLINT," + "		snapshot SMALLINT,"
						+ "		uuid VARCHAR NOT NULL," + "		UNIQUE (snapshot ASC, uuid ASC)" + ");");

				getDao(EpiData.class).executeRaw("ALTER TABLE epidata ADD COLUMN exposureDetailsKnown varchar(255);");
				getDao(EpiData.class).executeRaw(
					"UPDATE epidata SET exposureDetailsKnown = CASE "
						+ "WHEN traveled = 'YES' OR gatheringAttended = 'YES' OR burialAttended = 'YES' THEN 'YES' "
						+ "WHEN traveled = 'NO' OR gatheringAttended = 'NO' OR burialAttended = 'NO' THEN 'NO' "
						+ "WHEN traveled = 'UNKNOWN' OR gatheringAttended = 'UNKNOWN' OR burialAttended = 'UNKNOWN' THEN 'UNKNOWN' " + "END;");

				migrateEmbeddedEpiDataToExposures();

				getDao(EpiData.class).executeRaw("DROP TABLE epidatagathering;");
				getDao(EpiData.class).executeRaw("DROP TABLE epidatatravel;");
				getDao(EpiData.class).executeRaw("DROP TABLE epidataburial;");

			case 244:
				currentVersion = 244;
				getDao(EpiData.class).executeRaw("ALTER TABLE epidata ADD COLUMN contactWithSourceCaseKnown varchar(255);");
				getDao(EpiData.class).executeRaw("ALTER TABLE epidata ADD COLUMN highTransmissionRiskArea varchar(255);");
				getDao(EpiData.class).executeRaw("ALTER TABLE epidata ADD COLUMN largeOutbreaksArea varchar(255);");

			case 245:
				currentVersion = 245;
				// Mistakenly added
				//getDao(EpiData.class).executeRaw("ALTER TABLE epidata ADD COLUMN exposureDetailsKnown varchar(255);");

			case 246:
				currentVersion = 246;

				migrateEpiData();

				getDao(EpiData.class).executeRaw("ALTER TABLE epidata RENAME TO tmp_epidata;");

				getDao(EpiData.class).executeRaw(
					"CREATE TABLE epidata (" + "		areaInfectedAnimals VARCHAR," + "		contactWithSourceCaseKnown VARCHAR,"
						+ "		exposureDetailsKnown VARCHAR," + "		highTransmissionRiskArea VARCHAR," + "		largeOutbreaksArea VARCHAR,"
						+ "		pseudonymized SMALLINT," + "		changeDate BIGINT NOT NULL," + "		creationDate BIGINT NOT NULL,"
						+ "		id INTEGER PRIMARY KEY AUTOINCREMENT," + "		lastOpenedDate BIGINT," + "		localChangeDate BIGINT NOT NULL,"
						+ "		modified SMALLINT," + "		snapshot SMALLINT," + "		uuid VARCHAR NOT NULL,"
						+ "		UNIQUE (snapshot ASC, uuid ASC)" + ");");

				getDao(EpiData.class).executeRaw(
					"INSERT INTO epidata(exposureDetailsKnown, contactWithSourceCaseKnown, areaInfectedAnimals, changeDate, creationDate, "
						+ "id, lastOpenedDate, localChangeDate, modified, snapshot, uuid, pseudonymized) "
						+ "SELECT exposureDetailsKnown, contactWithSourceCaseKnown, wildbirds, changeDate, creationDate, id, lastOpenedDate, localChangeDate, modified, snapshot, uuid, pseudonymized "
						+ "FROM tmp_epidata;");
				getDao(EpiData.class).executeRaw("DROP TABLE tmp_epidata;");
			case 247:
				currentVersion = 247;
				getDao(Contact.class).executeRaw("ALTER TABLE contacts ADD column multiDayContact boolean default false;");
				getDao(Contact.class).executeRaw("ALTER TABLE contacts ADD column firstContactDate timestamp;");
			case 248:
				currentVersion = 248;

				getDao(Person.class).executeRaw("ALTER TABLE person ADD COLUMN armedForcesRelationType varchar(255);");

			case 249:
				currentVersion = 249;
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN nosocomialOutbreak boolean DEFAULT false");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN infectionSetting varchar(255)");

			case 250:
				currentVersion = 250;

				getDao(Person.class).executeRaw("ALTER TABLE person ADD column namesOfGuardians varchar(512);");

			case 251:
				currentVersion = 251;

				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN  prohibitionToWork varchar(255);");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN  prohibitionToWorkFrom timestamp;");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN  prohibitionToWorkUntil timestamp;");

				getDao(Contact.class).executeRaw("ALTER TABLE contacts ADD COLUMN  prohibitionToWork varchar(255);");
				getDao(Contact.class).executeRaw("ALTER TABLE contacts ADD COLUMN  prohibitionToWorkFrom timestamp;");
				getDao(Contact.class).executeRaw("ALTER TABLE contacts ADD COLUMN  prohibitionToWorkUntil timestamp;");

			case 252:
				currentVersion = 252;
				getDao(Event.class).executeRaw("ALTER TABLE events ADD COLUMN srcInstitutionalPartnerType varchar(255)");
				getDao(Event.class).executeRaw("ALTER TABLE events ADD COLUMN srcInstitutionalPartnerTypeDetails varchar(512)");

			case 253:
				currentVersion = 253;
				getDao(Contact.class).executeRaw("ALTER TABLE events ADD column riskLevel varchar(255);");

			case 254:
				currentVersion = 254;

				getDao(Person.class).executeRaw("ALTER TABLE person ADD COLUMN salutation varchar(255)");
				getDao(Person.class).executeRaw("ALTER TABLE person ADD COLUMN otherSalutation text");

			case 255:
				currentVersion = 255;
				getDao(Person.class).executeRaw("ALTER TABLE person ADD column birthName varchar(255);");
				getDao(Person.class).executeRaw("ALTER TABLE person ADD column birthCountry_id bigint REFERENCES country (id);");
				getDao(Person.class).executeRaw("ALTER TABLE person ADD column citizenship_id bigint REFERENCES country (id);");

			case 256:
				currentVersion = 256;
				getDao(Event.class).executeRaw("ALTER TABLE events ADD COLUMN meansOfTransport varchar(255);");
				getDao(Event.class).executeRaw("ALTER TABLE events ADD COLUMN meansOfTransportDetails text;");

			case 257:
				currentVersion = 257;
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN reportingDistrict_id REFERENCES district (id);");
				getDao(Contact.class).executeRaw("ALTER TABLE contacts ADD COLUMN reportingDistrict_id REFERENCES district (id);");

			case 258:
				currentVersion = 258;
				getDao(Location.class).executeRaw(
					"UPDATE location SET facilitytype = 'HOSPITAL' " + "WHERE facilitytype IS NULL "
						+ "AND (SELECT typeofplace from events WHERE eventlocation_id = location.id) = 'HOSPITAL';");
				getDao(Event.class).executeRaw(
					"UPDATE events SET typeofplace = 'FACILITY' "
						+ "WHERE (SELECT facilitytype FROM location WHERE id = events.eventlocation_id) IS NOT NULL;");

			case 259:
				currentVersion = 259;
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN externalToken text;");
				getDao(Contact.class).executeRaw("ALTER TABLE contacts ADD COLUMN externalToken text;");
				getDao(Person.class).executeRaw("ALTER TABLE person ADD COLUMN externalToken text;");
				getDao(Event.class).executeRaw("ALTER TABLE events ADD COLUMN externalToken text;");

			case 260:
				currentVersion = 260;
				getDao(Event.class).executeRaw("ALTER TABLE events ADD COLUMN transregionalOutbreak varchar(255);");
				getDao(Event.class).executeRaw("ALTER TABLE events ADD COLUMN diseaseTransmissionMode varchar(255);");

			case 261:
				currentVersion = 261;
				getDao(Event.class).executeRaw("ALTER TABLE events ADD COLUMN superordinateEventUuid varchar(36);");

			case 262:
				currentVersion = 262;
				getDao(Location.class).executeRaw(
					"UPDATE location SET facilityType = 'HOSPITAL' " + "WHERE facilityType IS NULL "
						+ "AND (SELECT typeOfPlace from exposures WHERE location_id = location.id) = 'HOSPITAL';");
				getDao(Exposure.class).executeRaw(
					"UPDATE exposures SET typeOfPlace = 'FACILITY' "
						+ "WHERE (SELECT facilityType FROM location WHERE id = exposures.location_id) IS NOT NULL;");

			case 263:
				currentVersion = 263;
				getDao(Event.class).executeRaw("ALTER TABLE events ADD COLUMN connectionNumber varchar(512);");
				getDao(Event.class).executeRaw("ALTER TABLE events ADD COLUMN travelDate timestamp;");

			case 264:
				currentVersion = 264;
				getDao(Event.class).executeRaw("ALTER TABLE events ADD COLUMN evolutionDate timestamp;");
				getDao(Event.class).executeRaw("ALTER TABLE events ADD COLUMN evolutionComment text;");

			case 265:
				currentVersion = 265;
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN firstVaccinationDate timestamp;");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN vaccineName varchar(255);");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN otherVaccineName text;");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN vaccineManufacturer varchar(255);");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN otherVaccineManufacturer text;");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN vaccineInn text;");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN vaccineBatchNumber text;");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN vaccineUniiCode text;");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN vaccineAtcCode text;");

			case 266:
				currentVersion = 266;
				getDao(Case.class).executeRaw(
					"CREATE TABLE diseaseVariant(" + "  id integer not null primary key autoincrement," + "  uuid varchar(36) not null unique,"
						+ "  changeDate TIMESTAMP not null," + "  creationDate TIMESTAMP not null," + "  disease varchar(255) not null,"
						+ "  name VARCHAR(512) not null," + "  lastOpenedDate BIGINT," + "  localChangeDate BIGINT NOT NULL," + "  modified SMALLINT,"
						+ "  snapshot SMALLINT);");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN diseaseVariant_id bigint REFERENCES diseaseVariant(id);");

			case 267:
				currentVersion = 267;
				getDao(PathogenTest.class).executeRaw("ALTER TABLE pathogentest ADD COLUMN typingId text;");

			case 268:
				currentVersion = 268;
				getDao(Exposure.class).executeRaw("ALTER TABLE exposures ADD COLUMN exposureRole varchar(255);");

				// TODO [vaccination info] integrate vaccination info
//			case 269:
//
//				currentVersion = 269;
//
//				getDao(VaccinationInfo.class).executeRaw(
//					"CREATE TABLE vaccinationInfo(vaccination	VARCHAR(255), vaccinationDoses TEXT, vaccinationInfoSource VARCHAR(255), firstVaccinationDate TIMESTAMP, lastVaccinationDate TIMESTAMP, vaccineName VARCHAR(255), otherVaccineName TEXT, "
//						+ "vaccineManufacturer VARCHAR(255), otherVaccineManufacturer TEXT, vaccineInn TEXT, vaccineBatchNumber TEXT, vaccineUniiCode TEXT, vaccineAtcCode TEXT, "
//						+ "changeDate BIGINT NOT NULL, creationDate BIGINT NOT NULL, id	INTEGER, lastOpenedDate BIGINT, localChangeDate BIGINT NOT NULL, modified SMALLINT, snapshot SMALLINT, uuid VARCHAR NOT NULL, PRIMARY KEY(id AUTOINCREMENT), UNIQUE(snapshot, uuid));");
//
//				getDao(Contact.class).executeRaw("ALTER TABLE contacts ADD COLUMN vaccinationInfo_id references vaccinationInfo(id)");
//				getDao(EventParticipant.class)
//					.executeRaw("ALTER TABLE eventParticipants ADD COLUMN vaccinationInfo_id references vaccinationInfo(id)");

			case 269:
				currentVersion = 269;
				getDao(Exposure.class).executeRaw("ALTER TABLE exposures ADD COLUMN workEnvironment varchar(255);");
				getDao(Event.class).executeRaw("ALTER TABLE events ADD COLUMN workEnvironment varchar(255);");

			case 270:
				currentVersion = 270;
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN bloodOrganOrTissueDonated varchar(255);");

			case 271:
				currentVersion = 271;
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN notACaseReasonNegativeTest boolean DEFAULT false;");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN notACaseReasonPhysicianInformation boolean DEFAULT false;");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN notACaseReasonDifferentPathogen boolean DEFAULT false;");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN notACaseReasonOther boolean DEFAULT false;");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN notACaseReasonDetails text;");

			case 272:
				currentVersion = 272;
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN caseIdentificationSource varchar(255);");

			case 273:
				currentVersion = 273;
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN reInfection varchar(255);");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN previousInfectionDate timestamp;");

			case 274:
				currentVersion = 274;
				getDao(Hospitalization.class).executeRaw("ALTER TABLE hospitalizations ADD COLUMN hospitalizationReason varchar(255);");
				getDao(Hospitalization.class).executeRaw("ALTER TABLE hospitalizations ADD COLUMN otherHospitalizationReason text;");
				getDao(PreviousHospitalization.class)
					.executeRaw("ALTER TABLE previoushospitalizations ADD COLUMN hospitalizationReason varchar(255);");
				getDao(PreviousHospitalization.class).executeRaw("ALTER TABLE previoushospitalizations ADD COLUMN otherHospitalizationReason text;");

			case 275:
				currentVersion = 275;
				getDao(PathogenTest.class).executeRaw("ALTER TABLE pathogenTest ADD COLUMN reportDate timestamp;");

			case 276:
				currentVersion = 276;
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN screeningType varchar(255);");
				getDao(Case.class).executeRaw(
					"UPDATE cases SET screeningType = 'ON_HOSPITAL_ADMISSION', caseIdentificationSource = 'SCREENING' where caseIdentificationSource = 'ON_HOSPITAL_ADMISSION';");
				getDao(Case.class).executeRaw(
					"UPDATE cases SET screeningType = 'ON_CARE_HOME_ADMISSION', caseIdentificationSource = 'SCREENING' where caseIdentificationSource = 'ON_CARE_HOME_ADMISSION';");
				getDao(Case.class).executeRaw(
					"UPDATE cases SET screeningType = 'ON_ASYLUM_ADMISSION', caseIdentificationSource = 'SCREENING' where caseIdentificationSource = 'ON_ASYLUM_ADMISSION';");
				getDao(Case.class).executeRaw(
					"UPDATE cases SET screeningType = 'ON_ENTRY_FROM_RISK_AREA', caseIdentificationSource = 'SCREENING' where caseIdentificationSource = 'ON_ENTRY_FROM_RISK_AREA';");
				getDao(Case.class).executeRaw(
					"UPDATE cases SET screeningType = 'HEALTH_SECTOR_EMPLOYEE', caseIdentificationSource = 'SCREENING' where caseIdentificationSource = 'HEALTH_SECTOR_EMPLOYEE';");
				getDao(Case.class).executeRaw(
					"UPDATE cases SET screeningType = 'EDUCATIONAL_INSTITUTIONS', caseIdentificationSource = 'SCREENING' where caseIdentificationSource = 'EDUCATIONAL_INSTITUTIONS';");

			case 277:
				currentVersion = 277;
				getDao(Location.class).executeRaw("ALTER TABLE location ADD COLUMN country_id BIGINT REFERENCES country(id);");

			case 278:
				currentVersion = 278;

				getDao(ActivityAsCase.class).executeRaw(
					"CREATE TABLE activityascase (" + "		activityAsCaseType VARCHAR," + "		activityAsCaseTypeDetails text,"
						+ "		connectionNumber VARCHAR," + "		description text," + "		endDate BIGINT," + "		epiData_id BIGINT,"
						+ "		gatheringDetails text," + "		gatheringType VARCHAR," + "		habitationDetails text,"
						+ "		habitationType VARCHAR," + "		location_id BIGINT," + "		meansOfTransport VARCHAR,"
						+ "		meansOfTransportDetails text," + "		reportingUser_id BIGINT," + "		role VARCHAR,"
						+ "		seatNumber VARCHAR," + "		startDate BIGINT," + "		typeOfPlace VARCHAR," + "		typeOfPlaceDetails text,"
						+ "		workEnvironment VARCHAR," + "		pseudonymized SMALLINT," + "		changeDate BIGINT NOT NULL,"
						+ "		creationDate BIGINT NOT NULL," + "		id INTEGER PRIMARY KEY AUTOINCREMENT," + "		lastOpenedDate BIGINT,"
						+ "		localChangeDate BIGINT NOT NULL," + "		modified SMALLINT," + "		snapshot SMALLINT,"
						+ "		uuid VARCHAR NOT NULL," + "		UNIQUE (snapshot ASC, uuid ASC)" + ");");

				getDao(EpiData.class).executeRaw("ALTER TABLE epidata ADD COLUMN activityAsCaseDetailsKnown varchar(255);");

				getDao(Case.class).executeRaw("UPDATE cases SET changeDate = 0 WHERE changeDate IS NOT NULL;");
				getDao(Therapy.class).executeRaw("UPDATE therapy SET changeDate = 0 WHERE changeDate IS NOT NULL;");
				getDao(ClinicalCourse.class).executeRaw("UPDATE clinicalCourse SET changeDate = 0 WHERE changeDate IS NOT NULL;");
				getDao(HealthConditions.class).executeRaw("UPDATE healthConditions SET changeDate = 0 WHERE changeDate IS NOT NULL;");
				getDao(Symptoms.class).executeRaw("UPDATE symptoms SET changeDate = 0 WHERE changeDate IS NOT NULL;");
				getDao(Hospitalization.class).executeRaw("UPDATE hospitalizations SET changeDate = 0 WHERE changeDate IS NOT NULL;");
				getDao(PreviousHospitalization.class).executeRaw("UPDATE previoushospitalizations SET changeDate = 0 WHERE changeDate IS NOT NULL;");
				getDao(EpiData.class).executeRaw("UPDATE epidata SET changeDate = 0 WHERE changeDate IS NOT NULL;");
				getDao(Exposure.class).executeRaw("UPDATE exposures SET changeDate = 0 WHERE changeDate IS NOT NULL;");
				getDao(MaternalHistory.class).executeRaw("UPDATE maternalHistory SET changeDate = 0 WHERE changeDate IS NOT NULL;");
				getDao(PortHealthInfo.class).executeRaw("UPDATE portHealthInfo SET changeDate = 0 WHERE changeDate IS NOT NULL;");
				getDao(Location.class).executeRaw("UPDATE location SET changeDate = 0 WHERE changeDate IS NOT NULL;");
			case 279:
				currentVersion = 279;

				getDao(Event.class)
					.executeRaw("ALTER TABLE events ADD COLUMN sormasToSormasOriginInfo_id bigint REFERENCES sormasToSormasOriginInfo(id);");
				getDao(Event.class).executeRaw("ALTER TABLE events ADD COLUMN ownershipHandedOver boolean;");

				getDao(EventParticipant.class).executeRaw(
					"ALTER TABLE eventParticipants ADD COLUMN sormasToSormasOriginInfo_id bigint REFERENCES sormasToSormasOriginInfo(id);");
				getDao(EventParticipant.class).executeRaw("ALTER TABLE eventParticipants ADD COLUMN ownershipHandedOver boolean;");
			case 280:
				currentVersion = 280;

				getDao(Facility.class).executeRaw("ALTER TABLE facility ADD COLUMN street varchar(4096);");
				getDao(Facility.class).executeRaw("ALTER TABLE facility ADD COLUMN houseNumber varchar(512);");
				getDao(Facility.class).executeRaw("ALTER TABLE facility ADD COLUMN additionalInformation varchar(255);");
				getDao(Facility.class).executeRaw("ALTER TABLE facility ADD COLUMN postalCode varchar(512);");
				getDao(Facility.class).executeRaw("ALTER TABLE facility ADD COLUMN areaType varchar(255);");
			case 281:
				currentVersion = 281;
				getDao(Event.class).executeRaw("ALTER TABLE events ADD COLUMN eventManagementStatus varchar(255);");

			case 282:
				currentVersion = 282;

				getDao(Exposure.class).executeRaw("ALTER TABLE exposures ADD COLUMN largeAttendanceNumber varchar(255);");

			case 283:
				currentVersion = 283;
				getDao(Sample.class).executeRaw("ALTER TABLE samples ADD COLUMN samplingReason varchar(255);");
				getDao(Sample.class).executeRaw("ALTER TABLE samples ADD COLUMN samplingReasonDetails text;");

			case 284:
				currentVersion = 284;
				getDao(DiseaseConfiguration.class).executeRaw("ALTER TABLE diseaseconfiguration ADD COLUMN extendedClassification boolean;");
				getDao(DiseaseConfiguration.class).executeRaw("ALTER TABLE diseaseconfiguration ADD COLUMN extendedClassificationMulti boolean;");
				getDao(DiseaseConfiguration.class)
					.executeRaw("UPDATE diseaseconfiguration SET extendedClassification = 0 WHERE disease not in ('CORONAVIRUS', 'MEASLES');");
				getDao(DiseaseConfiguration.class)
					.executeRaw("UPDATE diseaseconfiguration SET extendedClassificationMulti = 0 WHERE disease not in ('CORONAVIRUS');");
				getDao(DiseaseConfiguration.class)
					.executeRaw("UPDATE diseaseconfiguration SET extendedClassification = 1 WHERE disease in ('CORONAVIRUS', 'MEASLES');");
				getDao(DiseaseConfiguration.class)
					.executeRaw("UPDATE diseaseconfiguration SET extendedClassificationMulti = 1 WHERE disease in ('CORONAVIRUS');");
				getDao(DiseaseConfiguration.class).executeRaw("UPDATE diseaseconfiguration SET changeDate = 0;");

			case 285:
				currentVersion = 285;
				getDao(PathogenTest.class).executeRaw("ALTER TABLE pathogenTest ADD COLUMN viaLims boolean DEFAULT false;");

			case 286:
				currentVersion = 286;

				getDao(Event.class).executeRaw("ALTER TABLE events ADD COLUMN infectionPathCertainty varchar(255);");
				getDao(Event.class).executeRaw("ALTER TABLE events ADD COLUMN humanTransmissionMode varchar(255);");
				getDao(Event.class).executeRaw("ALTER TABLE events ADD COLUMN parenteralTransmissionMode varchar(255);");
				getDao(Event.class).executeRaw("ALTER TABLE events ADD COLUMN medicallyAssociatedTransmissionMode varchar(255);");

			case 287:
				currentVersion = 287;
				getDao(Event.class).executeRaw("ALTER TABLE events ADD COLUMN internalId text;");

			case 288:
				currentVersion = 288;

				getDao(PersonContactDetail.class).executeRaw(
					"CREATE TABLE personContactDetail (" + "		additionalInformation text," + "		contactInformation text,"
						+ "		details text," + "		person_id BIGINT," + "		personContactDetailType VARCHAR,"
						+ "		phoneNumberType VARCHAR," + "		primaryContact SMALLINT," + "		thirdParty SMALLINT,"
						+ "		thirdPartyName text," + "		thirdPartyRole text," + "		pseudonymized SMALLINT,"
						+ "		changeDate BIGINT NOT NULL," + "		creationDate BIGINT NOT NULL," + "		id INTEGER PRIMARY KEY AUTOINCREMENT,"
						+ "		lastOpenedDate BIGINT," + "		localChangeDate BIGINT NOT NULL," + "		modified SMALLINT,"
						+ "		snapshot SMALLINT," + "		uuid VARCHAR NOT NULL," + "		UNIQUE (snapshot ASC, uuid ASC)" + ");");

				migratePersonContactDetails();

			case 289:
				currentVersion = 289;
				getDao(PreviousHospitalization.class).executeRaw("ALTER TABLE previoushospitalizations ADD COLUMN intensiveCareUnit varchar(255);");
				getDao(PreviousHospitalization.class).executeRaw("ALTER TABLE previoushospitalizations ADD COLUMN intensiveCareUnitStart timestamp;");
				getDao(PreviousHospitalization.class).executeRaw("ALTER TABLE previoushospitalizations ADD COLUMN intensiveCareUnitEnd timestamp");

			case 290:
				currentVersion = 290;

				getDao(Continent.class).executeRaw(
					"CREATE TABLE IF NOT EXISTS continent (" + "		defaultName text," + "		archived SMALLINT,"
						+ "		changeDate BIGINT NOT NULL," + "		creationDate BIGINT NOT NULL," + "		id INTEGER PRIMARY KEY AUTOINCREMENT,"
						+ "		lastOpenedDate BIGINT," + "		localChangeDate BIGINT NOT NULL," + "		modified SMALLINT,"
						+ "		snapshot SMALLINT," + "		uuid VARCHAR NOT NULL," + "		UNIQUE (snapshot ASC, uuid ASC)" + ");");

				getDao(Subcontinent.class).executeRaw(
					"CREATE TABLE IF NOT EXISTS subcontinent (" + "		continent_id BIGINT NOT NULL," + "		defaultName text,"
						+ "		archived SMALLINT," + "		changeDate BIGINT NOT NULL," + "		creationDate BIGINT NOT NULL,"
						+ "		id INTEGER PRIMARY KEY AUTOINCREMENT," + "		lastOpenedDate BIGINT," + "		localChangeDate BIGINT NOT NULL,"
						+ "		modified SMALLINT," + "		snapshot SMALLINT," + "		uuid VARCHAR NOT NULL,"
						+ "		UNIQUE (snapshot ASC, uuid ASC)" + ");");

				getDao(Country.class).executeRaw("ALTER TABLE country ADD COLUMN subcontinent_id BIGINT REFERENCES subcontinent(id);");

			case 291:
				currentVersion = 291;
				getDao(Region.class).executeRaw("ALTER TABLE region ADD COLUMN country_id bigint REFERENCES country(id);");

			case 292:
				currentVersion = 292;
				getDao(EpiData.class).executeRaw("ALTER TABLE exposures ADD COLUMN probableInfectionEnvironment boolean DEFAULT false;");

			case 293:
				currentVersion = 293;
				getDao(Location.class).executeRaw("ALTER TABLE location ADD COLUMN continent_id BIGINT REFERENCES continent(id);");
				getDao(Location.class).executeRaw("ALTER TABLE location ADD COLUMN subcontinent_id BIGINT REFERENCES subcontinent(id);");

			case 294:
				currentVersion = 294;
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN followUpStatusChangeDate timestamp without time zone;");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN followUpStatusChangeUser_id BIGINT REFERENCES users(id);");
				getDao(Contact.class).executeRaw("ALTER TABLE contacts ADD COLUMN followUpStatusChangeDate timestamp without time zone;");
				getDao(Contact.class).executeRaw("ALTER TABLE contacts ADD COLUMN followUpStatusChangeUser_id BIGINT REFERENCES users(id);");

			case 295:
				currentVersion = 295;
				getDao(Facility.class).executeRaw("ALTER TABLE facility ADD COLUMN contactPersonFirstName text");
				getDao(Facility.class).executeRaw("ALTER TABLE facility ADD COLUMN contactPersonLastName text");
				getDao(Facility.class).executeRaw("ALTER TABLE facility ADD COLUMN contactPersonPhone text");
				getDao(Facility.class).executeRaw("ALTER TABLE facility ADD COLUMN contactPersonEmail text");

				getDao(Location.class).executeRaw("ALTER TABLE location ADD COLUMN contactPersonFirstName text;");
				getDao(Location.class).executeRaw("ALTER TABLE location ADD COLUMN contactPersonLastName text;");
				getDao(Location.class).executeRaw("ALTER TABLE location ADD COLUMN contactPersonPhone text;");
				getDao(Location.class).executeRaw("ALTER TABLE location ADD COLUMN contactPersonEmail text;");

			case 296:
				currentVersion = 296;
				getDao(Person.class).executeRaw("ALTER TABLE person ADD COLUMN additionalDetails text;");

			case 297:
				currentVersion = 297;
				getDao(PathogenTest.class).executeRaw("ALTER TABLE pathogenTest ADD COLUMN pcrTestSpecification varchar(255);");
				getDao(PathogenTest.class)
					.executeRaw("ALTER TABLE pathogenTest ADD COLUMN testedDiseaseVariant_id bigint REFERENCES diseaseVariant(id);");

			case 298:
				currentVersion = 298;
				getDao(Area.class).executeRaw(
					"CREATE TABLE area(" + "id integer primary key autoincrement," + "uuid varchar(36) not null unique,"
						+ "changeDate timestamp not null," + "creationDate timestamp not null," + "lastOpenedDate timestamp,"
						+ "localChangeDate timestamp not null," + "modified SMALLINT DEFAULT 0," + "snapshot SMALLINT DEFAULT 0,"
						+ "archived SMALLINT DEFAULT 0," + "name varchar(255) not null," + "externalId varchar(255) not null" + ");");
				getDao(Region.class).executeRaw("ALTER TABLE region ADD COLUMN area_id BIGINT REFERENCES area(id);");
				getDao(Region.class).executeRaw("UPDATE region set changeDate=0;");

			case 299:
				currentVersion = 299;
				getDao(Campaign.class).executeRaw("ALTER TABLE campaigns ADD COLUMN campaignFormMetas text;");
				getDao(Campaign.class).executeRaw("UPDATE campaigns set changeDate=0;");

			case 300:
				currentVersion = 300;
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN responsibleRegion_id BIGINT REFERENCES region(id);");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN responsibleDistrict_id BIGINT REFERENCES district(id);");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN responsibleCommunity_id BIGINT REFERENCES community(id);");

			case 301:
				currentVersion = 301;
				getDao(CustomizableEnumValue.class).executeRaw(
					"CREATE TABLE customizableEnumValue(" + "id integer primary key autoincrement," + "uuid varchar(36) not null unique,"
						+ "changeDate timestamp not null," + "creationDate timestamp not null," + "lastOpenedDate timestamp,"
						+ "localChangeDate timestamp not null," + "modified SMALLINT DEFAULT 0," + "snapshot SMALLINT DEFAULT 0,"
						+ "dataType varchar(255)," + "value text," + "caption text," + "translations text," + "diseases text," + "description text,"
						+ "descriptionTranslations text," + "properties text);");
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN diseaseVariant text;");
				getDao(Case.class).executeRaw(
					"UPDATE cases SET diseaseVariant = UPPER(REPLACE((SELECT name FROM diseaseVariant WHERE diseaseVariant.id = cases.diseaseVariant_id), ' ', '_')) WHERE cases.diseaseVariant_id IS NOT NULL;");
				getDao(Case.class).executeRaw("UPDATE cases SET diseaseVariant_id = NULL;");
				getDao(PathogenTest.class).executeRaw("ALTER TABLE pathogenTest ADD COLUMN testedDiseaseVariant text;");
				getDao(Case.class).executeRaw(
					"UPDATE pathogenTest SET testedDiseaseVariant = UPPER(REPLACE((SELECT name FROM diseaseVariant WHERE diseaseVariant.id = pathogenTest.testedDiseaseVariant_id), ' ', '_')) WHERE pathogenTest.testedDiseaseVariant_id IS NOT NULL;");
				getDao(PathogenTest.class).executeRaw("UPDATE pathogenTest SET testedDiseaseVariant_id = NULL;");
				getDao(Case.class).executeRaw("DROP TABLE diseaseVariant;");

			case 302:
				currentVersion = 302;
				getDao(Case.class).executeRaw("ALTER TABLE cases ADD COLUMN internalToken text;");
				getDao(Contact.class).executeRaw("ALTER TABLE contacts ADD COLUMN internalToken text;");
				getDao(Person.class).executeRaw("ALTER TABLE person ADD COLUMN internalToken text;");

			case 303:
				currentVersion = 303;
				getDao(Case.class).executeRaw(
					"UPDATE cases" + " SET responsibleRegion_id = region_id," + " responsibleDistrict_id = district_id,"
						+ " responsibleCommunity_id = community_id," + " region_id = null," + " district_id = null," + " community_id = null,"
						+ " reportingDistrict_id = null"
						+ " WHERE responsibleRegion_id IS NULL AND (reportingDistrict_id IS NULL OR reportingDistrict_id = district_id)");

				getDao(Case.class).executeRaw(
					"UPDATE cases" + " SET responsibleRegion_id = (SELECT region_id from district where id = cases.reportingDistrict_id),"
						+ " responsibleDistrict_id = reportingDistrict_id," + " reportingDistrict_id = null"
						+ " WHERE responsibleRegion_id IS NULL AND reportingDistrict_id IS NOT NULL");

			case 304:
				currentVersion = 304;
				getDao(SormasToSormasOriginInfo.class).executeRaw("ALTER TABLE sormasToSormasOriginInfo ADD COLUMN withAssociatedContacts boolean;");
				getDao(SormasToSormasOriginInfo.class).executeRaw("ALTER TABLE sormasToSormasOriginInfo ADD COLUMN withSamples boolean;");
				getDao(SormasToSormasOriginInfo.class).executeRaw("ALTER TABLE sormasToSormasOriginInfo ADD COLUMN withEventParticipants boolean;");

			case 305:
				currentVersion = 305;
				getDao(Immunization.class).executeRaw(
					"CREATE TABLE immunization (" + " id integer primary key autoincrement, uuid varchar(36) not null unique,"
						+ " changeDate timestamp not null, creationDate timestamp not null, lastOpenedDate timestamp, localChangeDate timestamp not null,"
						+ " modified SMALLINT DEFAULT 0, snapshot SMALLINT DEFAULT 0," + " disease varchar(255) not null,"
						+ " person_id bigint not null," + " reportdate timestamp not null," + " reportinguser_id bigint not null,"
						+ " archived boolean DEFAULT false," + " immunizationstatus varchar(255) not null,"
						+ " meansofimmunization varchar(255) not null," + " meansOfImmunizationDetails varchar(512),"
						+ " immunizationmanagementstatus varchar(255) not null," + " externalid varchar(255) not null,"
						+ " responsibleregion_id bigint," + " responsibledistrict_id bigint," + " responsiblecommunity_id bigint,"
						+ " country_id bigint," + " startdate timestamp," + " enddate timestamp," + " numberofdoses int,"
						+ " previousinfection varchar(255)," + " lastinfectiondate timestamp," + " additionaldetails varchar(512),"
						+ " positivetestresultdate timestamp not null," + " recoverydate timestamp not null," + " relatedcase_id bigint)");

			case 306:
				currentVersion = 306;
				getDao(Immunization.class).executeRaw("ALTER TABLE immunization ADD COLUMN pseudonymized boolean;");

			case 307:
				currentVersion = 307;
				getDao(LbdsSync.class).executeRaw(
					"CREATE TABLE lbdsSync(" + "sentUuid varchar(36) primary key," + "lastSendDate timestamp," + "lastReceivedDate timestamp);");

			case 308:
				currentVersion = 308;
				getDao(Event.class).executeRaw("ALTER TABLE events ADD COLUMN diseaseVariant text;");

			case 309:
				currentVersion = 309;
				getDao(VaccinationEntity.class).executeRaw(
					"CREATE TABLE vaccination (\n" + "id integer primary key autoincrement,\n" + " uuid varchar(36) not null unique,\n"
						+ " changedate timestamp not null,\n" + " creationdate timestamp not null,\n" + " immunization_id bigint not null,\n"
						+ " healthconditions_id bigint not null,\n" + " reportdate timestamp not null,\n" + " reportinguser_id bigint,\n"
						+ " vaccinationDate timestamp,\n" + " vaccinename varchar(255),\n" + " othervaccinename text,\n"
						+ " vaccinenamedetails text,\n" + " vaccinemanufacturer varchar(255),\n" + " othervaccinemanufacturer text,\n"
						+ " vaccinemanufacturerdetails text,\n" + " vaccineinn text,\n" + " vaccinebatchnumber text,\n" + " vaccineuniicode text,\n"
						+ " vaccineatccode text,\n" + " vaccinationinfosource varchar(255),\n" + " pregnant varchar(255),\n"
						+ " trimester varchar(255), pseudonymized boolean);");

				// ATTENTION: break should only be done after last version
				break;

			default:
				throw new IllegalStateException("onUpgrade() with unknown oldVersion " + oldVersion);
			}
		} catch (

		Exception ex) {
			throw new RuntimeException("Database upgrade failed for version " + currentVersion + ": " + ex.getMessage(), ex);
		}
	}

	private void formatRawResultString(Object[] result, int index, boolean doNullCheck) {
		if (doNullCheck && DataHelper.isNullOrEmpty((String) result[index])) {
			Array.set(result, index, null);
		}
		if (!DataHelper.isNullOrEmpty((String) result[index])) {
			Array.set(result, index, "'" + result[index] + "'");
		}
	}

	private void formatRawResultDate(Object[] result, int index) {
		if (result[index] != null && result[index] instanceof Date) {
			long time = ((Date) result[index]).getTime();
			if (time == 0L) {
				Array.set(result, index, null);
			} else {
				Array.set(result, index, time);
			}
		}
	}

	private void migrateEpiData() throws SQLException {
		getDao(EpiData.class)
			.executeRaw("UPDATE epidata SET wildbirds = 'YES', poultryEat = 'YES' WHERE poultry = 'YES' AND changeDate = 0 AND snapshot = 0;");

		// Epi data field names sometimes don't match the actual field names because the columns were renamed in the past
		migrateEpiDataField("processingConfirmedCaseFluidUnsafe", Exposure.HANDLING_SAMPLES, YesNoUnknown.YES, ExposureType.WORK);
		migrateEpiDataField("percutaneousCaseBlood", Exposure.PERCUTANEOUS, YesNoUnknown.YES, ExposureType.WORK);
		migrateEpiDataField("wildbirdsLocation", Exposure.PHYSICAL_CONTACT_WITH_BODY, YesNoUnknown.YES, ExposureType.BURIAL);
		migrateEpiDataField("wildbirdsDetails", Exposure.HANDLING_SAMPLES, YesNoUnknown.YES, ExposureType.WORK);
		migrateEpiDataField(
			"poultrySick",
			Exposure.ANIMAL_CONDITION,
			AnimalCondition.DEAD,
			ExposureType.ANIMAL_CONTACT,
			"poultryDate",
			"poultryDate",
			"poultrySickDetails",
			"poultryLocation");
		migrateEpiDataField(
			"poultryEat",
			Exposure.EATING_RAW_ANIMAL_PRODUCTS,
			YesNoUnknown.YES,
			ExposureType.ANIMAL_CONTACT,
			null,
			null,
			"poultryDetails",
			null);
		migrateEpiDataField("rodents", Exposure.TYPE_OF_ANIMAL, TypeOfAnimal.RODENT, ExposureType.ANIMAL_CONTACT);
		migrateEpiDataField("bats", Exposure.TYPE_OF_ANIMAL, TypeOfAnimal.BAT, ExposureType.ANIMAL_CONTACT);
		migrateEpiDataField("primates", Exposure.TYPE_OF_ANIMAL, TypeOfAnimal.PRIMATE, ExposureType.ANIMAL_CONTACT);
		migrateEpiDataField("swine", Exposure.TYPE_OF_ANIMAL, TypeOfAnimal.SWINE, ExposureType.ANIMAL_CONTACT);
		migrateEpiDataField("birds", Exposure.TYPE_OF_ANIMAL, TypeOfAnimal.POULTRY, ExposureType.ANIMAL_CONTACT);
		migrateEpiDataField("rabbits", Exposure.TYPE_OF_ANIMAL, TypeOfAnimal.RABBIT, ExposureType.ANIMAL_CONTACT);
		migrateEpiDataField("cattle", Exposure.TYPE_OF_ANIMAL, TypeOfAnimal.CATTLE, ExposureType.ANIMAL_CONTACT);
		migrateEpiDataField("dogs", Exposure.TYPE_OF_ANIMAL, TypeOfAnimal.DOG, ExposureType.ANIMAL_CONTACT);
		migrateEpiDataField("cats", Exposure.TYPE_OF_ANIMAL, TypeOfAnimal.CAT, ExposureType.ANIMAL_CONTACT);
		migrateEpiDataField("canidae", Exposure.TYPE_OF_ANIMAL, TypeOfAnimal.CANIDAE, ExposureType.ANIMAL_CONTACT);
		migrateEpiDataField("camels", Exposure.TYPE_OF_ANIMAL, TypeOfAnimal.CAMEL, ExposureType.ANIMAL_CONTACT);
		migrateEpiDataField("snakes", Exposure.TYPE_OF_ANIMAL, TypeOfAnimal.SNAKE, ExposureType.ANIMAL_CONTACT);
		migrateEpiDataField("tickBite", Exposure.TYPE_OF_ANIMAL, TypeOfAnimal.TICK, ExposureType.ANIMAL_CONTACT);
		migrateEpiDataField("fleaBite", Exposure.TYPE_OF_ANIMAL, TypeOfAnimal.FLEA, ExposureType.ANIMAL_CONTACT);
		migrateEpiDataField("otherAnimals", Exposure.TYPE_OF_ANIMAL, TypeOfAnimal.OTHER, ExposureType.ANIMAL_CONTACT);
		migrateEpiDataField("waterBody", Exposure.BODY_OF_WATER, YesNoUnknown.YES, ExposureType.OTHER, null, null, "waterBodyDetails", null);
		migrateEpiDataField("visitedHealthFacility", Exposure.HABITATION_TYPE, HabitationType.MEDICAL, ExposureType.HABITATION);
		migrateEpiDataField("visitedAnimalMarket", Exposure.ANIMAL_MARKET, YesNoUnknown.YES, ExposureType.OTHER);
		migrateEpiDataField("areaConfirmedCases", Exposure.RISK_AREA, YesNoUnknown.YES, ExposureType.TRAVEL);
		migrateEpiDataField("kindOfExposureBite", Exposure.ANIMAL_CONTACT_TYPE, AnimalContactType.BITE, ExposureType.ANIMAL_CONTACT);
		migrateEpiDataField("kindOfExposureTouch", Exposure.ANIMAL_CONTACT_TYPE, AnimalContactType.TOUCH, ExposureType.ANIMAL_CONTACT);
		migrateEpiDataField("kindOfExposureScratch", Exposure.ANIMAL_CONTACT_TYPE, AnimalContactType.SCRATCH, ExposureType.ANIMAL_CONTACT);
		migrateEpiDataField("kindOfExposureLick", Exposure.ANIMAL_CONTACT_TYPE, AnimalContactType.LICK, ExposureType.ANIMAL_CONTACT);
		migrateEpiDataField("kindOfExposureOther", Exposure.ANIMAL_CONTACT_TYPE, AnimalContactType.OTHER, ExposureType.ANIMAL_CONTACT);

		GenericRawResults<Object[]> lastExposureInfo = getDao(EpiData.class).queryRaw(
			"SELECT id, dateOfLastExposure, placeOfLastExposure, animalCondition, animalVaccinationStatus, prophylaxisStatus, dateOfProphylaxis"
				+ " FROM epidata WHERE changeDate = 0 AND snapshot = 0 AND (dateOfLastExposure IS NOT NULL OR placeOfLastExposure IS NOT NULL"
				+ " OR animalCondition IS NOT NULL OR animalVaccinationStatus IS NOT NULL OR prophylaxisStatus IS NOT NULL OR dateOfProphylaxis IS NOT NULL);",
			new DataType[] {
				DataType.BIG_INTEGER,
				DataType.DATE_LONG,
				DataType.STRING,
				DataType.ENUM_STRING,
				DataType.ENUM_STRING,
				DataType.ENUM_STRING,
				DataType.DATE_LONG });

		for (Object[] result : lastExposureInfo) {
			formatRawResultString(result, 2, true);
			formatRawResultString(result, 3, false);
			formatRawResultString(result, 5, false);
			formatRawResultDate(result, 1);
			formatRawResultDate(result, 6);

			long locationId = insertLocation((String) result[2]);
			Vaccination vaccinationStatus = result[4] != null ? Vaccination.valueOf((String) result[4]) : null;

			String exposureQuery = "INSERT INTO exposures(uuid, changeDate, localChangeDate, creationDate, epiData_id, location_id, exposureType, "
				+ "startDate, endDate, animalCondition, animalVaccinated, prophylaxis, prophylaxisDate, description, pseudonymized, modified, snapshot) VALUES ('"
				+ DataHelper.createUuid()
				+ "', 0, CAST(ROUND((julianday('now') - 2440587.5)*86400000) As INTEGER), CAST(ROUND((julianday('now') - 2440587.5)*86400000) As INTEGER), "
				+ result[0] + ", " + locationId + ", '" + ExposureType.ANIMAL_CONTACT.name() + "', " + result[1] + ", " + result[1] + ", " + result[3]
				+ ", "
				+ (vaccinationStatus == Vaccination.VACCINATED
					? "'" + YesNoUnknown.YES.name() + "'"
					: vaccinationStatus == Vaccination.UNVACCINATED
						? "'" + YesNoUnknown.NO.name() + "'"
						: vaccinationStatus == Vaccination.UNKNOWN ? "'" + YesNoUnknown.UNKNOWN.name() + "'" : null)
				+ ", " + result[5] + ", " + result[6] + ", "
				+ "'Automatic epi data migration based on last exposure details; this exposure may be merged with another exposure with animal contact', 0, 0, 0);";
			getDao(Exposure.class).executeRaw(exposureQuery);
		}

		getDao(Exposure.class).executeRaw(
			"UPDATE exposures SET typeOfAnimalDetails = (SELECT otherAnimalsDetails FROM epidata WHERE id = exposures.epidata_id AND exposures.typeOfAnimal = 'OTHER');");
		getDao(Exposure.class).executeRaw(
			"UPDATE exposures SET animalContactTypeDetails = (SELECT kindOfExposureDetails FROM epidata WHERE id = exposures.epidata_id AND exposures.animalContactType = 'OTHER');");
		getDao(Exposure.class).executeRaw(
			"UPDATE exposures SET waterSource = (SELECT waterSource FROM epidata WHERE id = exposures.epidata_id AND exposures.bodyOfWater = 'YES');");
		getDao(Exposure.class).executeRaw(
			"UPDATE exposures SET waterSourceDetails = (SELECT waterSourceOther FROM epidata WHERE id = exposures.epidata_id AND exposures.bodyOfWater = 'YES');");
		getDao(Exposure.class).executeRaw(
			"UPDATE exposures SET description = 'Automatic epi data migration based on selected kinds of exposure; this exposure may be merged with another exposure with animal contact' WHERE exposureType = 'ANIMAL_CONTACT' AND typeOfAnimal IS NULL;");
		getDao(EpiData.class).executeRaw(
			"UPDATE epidata SET contactWithSourceCaseKnown = 'YES' WHERE snapshot = 0 AND changeDate = 0 AND (directContactConfirmedCase = 'YES' OR directContactProbableCase = 'YES' OR closeContactProbableCase = 'YES' OR contactWithSourceRespiratoryCase = 'YES');");

		getDao(EpiData.class).executeRaw(
			"UPDATE epidata SET exposureDetailsKnown = 'YES' WHERE snapshot = 0 AND changeDate = 0 AND (exposureDetailsKnown IS NULL OR exposureDetailsKnown != 'YES') "
				+ "AND (SELECT COUNT(id) FROM exposures WHERE exposures.epidata_id = epidata.id LIMIT 1) > 0;");
	}

	private void migrateEpiDataField(String epiDataFieldName, String exposuresFieldName, Enum<?> exposuresFieldValue, ExposureType exposureType)
		throws SQLException {
		migrateEpiDataField(epiDataFieldName, exposuresFieldName, exposuresFieldValue, exposureType, null, null, null, null);
	}

	private void migrateEpiDataField(
		String epiDataFieldName,
		String exposuresFieldName,
		Enum<?> exposuresFieldValue,
		ExposureType exposureType,
		String startDateFieldName,
		String endDateFieldName,
		String descriptionFieldName,
		String locationDetailsFieldName)
		throws SQLException {

		GenericRawResults<Object[]> epiDataInfo = getDao(EpiData.class).queryRaw(
			"SELECT id, " + startDateFieldName + ", " + endDateFieldName + ", " + descriptionFieldName + ", " + locationDetailsFieldName
				+ " FROM epidata WHERE changeDate = 0 AND snapshot = 0 AND " + epiDataFieldName + " = 'YES';",
			new DataType[] {
				DataType.BIG_INTEGER,
				DataType.DATE_LONG,
				DataType.DATE_LONG,
				DataType.STRING,
				DataType.STRING });

		for (Object[] result : epiDataInfo) {
			formatRawResultString(result, 3, true);
			formatRawResultString(result, 4, true);
			formatRawResultDate(result, 1);
			formatRawResultDate(result, 2);

			long locationId = insertLocation((String) result[4]);

			String exposureQuery = "INSERT INTO exposures(uuid, changeDate, localChangeDate, creationDate, epiData_id, location_id, exposureType, "
				+ exposuresFieldName + ", " + "startDate, endDate, description, pseudonymized, modified, snapshot) VALUES ('"
				+ DataHelper.createUuid()
				+ "', 0, CAST(ROUND((julianday('now') - 2440587.5)*86400000) As INTEGER), CAST(ROUND((julianday('now') - 2440587.5)*86400000) As INTEGER), "
				+ result[0] + ", " + locationId + ", '" + exposureType.name() + "', '" + exposuresFieldValue.name() + "', " + result[1] + ", "
				+ result[2] + ", " + result[3] + ", 0, 0, 0);";
			getDao(Exposure.class).executeRaw(exposureQuery);
		}
	}

	private long insertLocation(String locationDetails) throws SQLException {
		String locationQuery =
			"INSERT INTO location (uuid, changeDate, localChangeDate, creationDate, details, pseudonymized, modified, snapshot) VALUES ('"
				+ DataHelper.createUuid()
				+ "', 0, CAST(ROUND((julianday('now') - 2440587.5)*86400000) As INTEGER), CAST(ROUND((julianday('now') - 2440587.5)*86400000) As INTEGER), "
				+ locationDetails + ", 0, 0, 0);";
		getDao(Location.class).executeRaw(locationQuery);

		return getDao(Location.class).queryRawValue("SELECT MAX(id) FROM location;");
	}

	private void migratePersonContactDetails() throws SQLException {
		GenericRawResults<Object[]> newPersons = getDao(Person.class).queryRaw(
			"SELECT id, phone, phoneOwner, emailAddress, generalPractitionerDetails FROM person WHERE changeDate = 0 AND snapshot = 0;",
			new DataType[] {
				DataType.BIG_INTEGER,
				DataType.STRING,
				DataType.STRING,
				DataType.STRING,
				DataType.STRING });

		for (Object[] pcd : newPersons) {
			formatRawResultString(pcd, 1, false);
			formatRawResultString(pcd, 2, false);
			formatRawResultString(pcd, 3, false);
			formatRawResultString(pcd, 4, false);

			final String dateNowString = "CAST(ROUND((julianday('now') - 2440587.5)*86400000) As INTEGER)";
			final String insertPart =
				"INSERT INTO personContactDetail(uuid, changeDate, localChangeDate, creationDate, person_id, primaryContact, personContactDetailType, phoneNumberType, "
					+ "contactInformation, additionalInformation, thirdParty, thirdPartyRole, thirdPartyName, snapshot) ";

			BigInteger personId = (BigInteger) pcd[0];
			String phone = (String) pcd[1];
			String phoneOwner = (String) pcd[2];
			String emailAddress = (String) pcd[3];
			String generalPractitionerDetails = (String) pcd[4];

			if (StringUtils.isNotEmpty(phone)) {
				boolean phoneOwnerEmpty = StringUtils.isEmpty(phoneOwner);
				getDao(PersonContactDetail.class).executeRaw(
					insertPart + "VALUES ('" + DataHelper.createUuid() + "', 0, " + dateNowString + ", " + dateNowString + ", " + personId + ", "
						+ (phoneOwnerEmpty ? "1" : "0") + ", \'" + PersonContactDetailType.PHONE.name() + "\', " + "null" + ", " + phone + ", "
						+ "null" + ", " + (phoneOwnerEmpty ? "0" : "1") + ", " + "null" + ", " + (phoneOwnerEmpty ? "null" : phoneOwner) + ", "
						+ "0);");
			}

			if (StringUtils.isNotEmpty(emailAddress)) {
				getDao(PersonContactDetail.class).executeRaw(
					insertPart + "VALUES ('" + DataHelper.createUuid() + "', 0, " + dateNowString + ", " + dateNowString + ", " + personId + ", "
						+ "1" + ", \'" + PersonContactDetailType.EMAIL.name() + "\', " + "null" + ", " + emailAddress + ", " + "null" + ", " + "0"
						+ ", " + "null" + ", " + "null" + ", " + "0);");
			}

			if (StringUtils.isNotEmpty(generalPractitionerDetails)) {
				getDao(PersonContactDetail.class).executeRaw(
					insertPart + "VALUES ('" + DataHelper.createUuid() + "', 0, " + dateNowString + ", " + dateNowString + ", " + personId + ", "
						+ "0" + ", \'" + PersonContactDetailType.OTHER.name() + "\', " + "null" + ", " + "null" + ", " + generalPractitionerDetails
						+ ", " + "1" + ", " + "\'General practitioner\'" + ", " + generalPractitionerDetails + ", " + "0);");
			}
		}
	}

	private void migrateEmbeddedEpiDataToExposures() throws SQLException {
		GenericRawResults<Object[]> newBurials = getDao(EpiData.class).queryRaw(
			"SELECT epiData_id, burialAddress_id, burialPersonName, burialRelation,"
				+ "burialTouching, burialIll, burialDateFrom, burialDateTo FROM epidataburial WHERE changeDate = 0 AND snapshot = 0;",
			new DataType[] {
				DataType.BIG_INTEGER,
				DataType.BIG_INTEGER,
				DataType.STRING,
				DataType.STRING,
				DataType.ENUM_STRING,
				DataType.ENUM_STRING,
				DataType.DATE_LONG,
				DataType.DATE_LONG });

		for (Object[] burial : newBurials) {
			formatRawResultString(burial, 2, true);
			formatRawResultString(burial, 3, true);
			formatRawResultString(burial, 4, false);
			formatRawResultString(burial, 5, false);
			formatRawResultDate(burial, 6);
			formatRawResultDate(burial, 7);

			String burialQuery =
				"INSERT INTO exposures(uuid, changeDate, localChangeDate, creationDate, epiData_id, location_id, deceasedPersonName, deceasedPersonRelation, "
					+ "physicalContactWithBody, deceasedPersonIll, startDate, endDate, exposureType, pseudonymized, modified, snapshot) VALUES ('"
					+ DataHelper.createUuid()
					+ "', 0, CAST(ROUND((julianday('now') - 2440587.5)*86400000) As INTEGER), CAST(ROUND((julianday('now') - 2440587.5)*86400000) As INTEGER), "
					+ burial[0] + ", " + burial[1] + ", " + burial[2] + ", " + burial[3] + ", " + burial[4] + ", " + burial[5] + ", " + burial[6]
					+ ", " + burial[7] + ", 'BURIAL', 0, 0, 0);";
			getDao(Exposure.class).executeRaw(burialQuery);
		}

		GenericRawResults<Object[]> newGatherings = getDao(EpiData.class).queryRaw(
			"SELECT epiData_id, gatheringAddress_id, gatheringDate, description FROM epidatagathering WHERE changeDate = 0 AND snapshot = 0;",
			new DataType[] {
				DataType.BIG_INTEGER,
				DataType.BIG_INTEGER,
				DataType.DATE_LONG,
				DataType.STRING });

		for (Object[] gathering : newGatherings) {
			formatRawResultString(gathering, 3, true);
			formatRawResultDate(gathering, 2);

			String gatheringQuery =
				"INSERT INTO exposures(uuid, changeDate, localChangeDate, creationDate, epiData_id, location_id, startDate, endDate, "
					+ "description, exposureType, pseudonymized, modified, snapshot) VALUES ('" + DataHelper.createUuid()
					+ "', 0, CAST(ROUND((julianday('now') - 2440587.5)*86400000) As INTEGER), CAST(ROUND((julianday('now') - 2440587.5)*86400000) As INTEGER), "
					+ gathering[0] + ", " + gathering[1] + ", " + gathering[2] + ", " + gathering[2] + ", " + gathering[3]
					+ ", 'GATHERING', 0, 0, 0);";
			getDao(Exposure.class).executeRaw(gatheringQuery);
		}

		GenericRawResults<Object[]> newTravels = getDao(EpiData.class).queryRaw(
			"SELECT epiData_id, travelDateFrom, travelDateTo, travelType, travelDestination FROM epidatatravel WHERE changeDate = 0 AND snapshot = 0;",
			new DataType[] {
				DataType.BIG_INTEGER,
				DataType.DATE_LONG,
				DataType.DATE_LONG,
				DataType.ENUM_STRING,
				DataType.STRING });

		for (Object[] travel : newTravels) {
			formatRawResultDate(travel, 1);
			formatRawResultDate(travel, 2);

			String detailsString = Stream.of((String) travel[3] != null ? ((String) travel[3]).replace("_", " ") : null, (String) travel[4])
				.filter(Objects::nonNull)
				.collect(Collectors.joining(", "));

			if (detailsString != null) {
				detailsString = "'" + detailsString + "'";
			}

			String locationQuery =
				"INSERT INTO location (uuid, changeDate, localChangeDate, creationDate, details, pseudonymized, modified, snapshot) VALUES ('"
					+ DataHelper.createUuid()
					+ "', 0, CAST(ROUND((julianday('now') - 2440587.5)*86400000) As INTEGER), CAST(ROUND((julianday('now') - 2440587.5)*86400000) As INTEGER), "
					+ detailsString + ", 0, 0, 0);";
			getDao(Location.class).executeRaw(locationQuery);

			long locationId = getDao(Location.class).queryRawValue("SELECT MAX(id) FROM location;");

			String travelQuery =
				"INSERT INTO exposures(uuid, changeDate, localChangeDate, creationDate, epiData_id, location_id, startDate, endDate, exposureType, "
					+ "pseudonymized, modified, snapshot) VALUES ('" + DataHelper.createUuid()
					+ "', 0, CAST(ROUND((julianday('now') - 2440587.5)*86400000) As INTEGER), CAST(ROUND((julianday('now') - 2440587.5)*86400000) As INTEGER), "
					+ travel[0] + ", " + locationId + ", " + travel[1] + ", " + travel[2] + ", 'TRAVEL', 0, 0, 0);";
			getDao(Exposure.class).executeRaw(travelQuery);
		}
	}

	private void upgradeFromUnupgradableVersion(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion) {
		try {
			Log.i(DatabaseHelper.class.getName(), "onUpgrade");
			TableUtils.dropTable(connectionSource, Case.class, true);
			TableUtils.dropTable(connectionSource, Immunization.class, true);
			TableUtils.dropTable(connectionSource, VaccinationEntity.class, true);
			TableUtils.dropTable(connectionSource, Prescription.class, true);
			TableUtils.dropTable(connectionSource, Treatment.class, true);
			TableUtils.dropTable(connectionSource, Therapy.class, true);
			TableUtils.dropTable(connectionSource, ClinicalVisit.class, true);
			TableUtils.dropTable(connectionSource, ClinicalCourse.class, true);
			TableUtils.dropTable(connectionSource, HealthConditions.class, true);
			TableUtils.dropTable(connectionSource, MaternalHistory.class, true);
			TableUtils.dropTable(connectionSource, PortHealthInfo.class, true);
			TableUtils.dropTable(connectionSource, Person.class, true);
			TableUtils.dropTable(connectionSource, PersonContactDetail.class, true);
			TableUtils.dropTable(connectionSource, Location.class, true);
			TableUtils.dropTable(connectionSource, Continent.class, true);
			TableUtils.dropTable(connectionSource, Subcontinent.class, true);
			TableUtils.dropTable(connectionSource, Country.class, true);
			TableUtils.dropTable(connectionSource, Area.class, true);
			TableUtils.dropTable(connectionSource, Region.class, true);
			TableUtils.dropTable(connectionSource, District.class, true);
			TableUtils.dropTable(connectionSource, Community.class, true);
			TableUtils.dropTable(connectionSource, Facility.class, true);
			TableUtils.dropTable(connectionSource, PointOfEntry.class, true);
			TableUtils.dropTable(connectionSource, User.class, true);
			TableUtils.dropTable(connectionSource, Symptoms.class, true);
			TableUtils.dropTable(connectionSource, Task.class, true);
			TableUtils.dropTable(connectionSource, Contact.class, true);
			TableUtils.dropTable(connectionSource, Visit.class, true);
			TableUtils.dropTable(connectionSource, Event.class, true);
			TableUtils.dropTable(connectionSource, Sample.class, true);
			TableUtils.dropTable(connectionSource, PathogenTest.class, true);
			TableUtils.dropTable(connectionSource, AdditionalTest.class, true);
			TableUtils.dropTable(connectionSource, EventParticipant.class, true);
			TableUtils.dropTable(connectionSource, Hospitalization.class, true);
			TableUtils.dropTable(connectionSource, PreviousHospitalization.class, true);
			TableUtils.dropTable(connectionSource, EpiData.class, true);
			TableUtils.dropTable(connectionSource, Exposure.class, true);
			TableUtils.dropTable(connectionSource, ActivityAsCase.class, true);
			TableUtils.dropTable(connectionSource, SyncLog.class, true);
			TableUtils.dropTable(connectionSource, WeeklyReport.class, true);
			TableUtils.dropTable(connectionSource, WeeklyReportEntry.class, true);
			TableUtils.dropTable(connectionSource, AggregateReport.class, true);
			TableUtils.dropTable(connectionSource, Outbreak.class, true);
			TableUtils.dropTable(connectionSource, DiseaseClassificationCriteria.class, true);
			TableUtils.dropTable(connectionSource, DiseaseConfiguration.class, true);
			TableUtils.dropTable(connectionSource, CustomizableEnumValue.class, true);
			TableUtils.dropTable(connectionSource, FeatureConfiguration.class, true);
			TableUtils.dropTable(connectionSource, Campaign.class, true);
			TableUtils.dropTable(connectionSource, CampaignFormMeta.class, true);
			TableUtils.dropTable(connectionSource, CampaignFormData.class, true);
			TableUtils.dropTable(connectionSource, LbdsSync.class, true);
			// TODO [vaccination info] integrate vaccination info
//			TableUtils.dropTable(connectionSource, VaccinationInfo.class, true);

			if (oldVersion < 30) {
				TableUtils.dropTable(connectionSource, Config.class, true);
			}
			// after we drop the old databases, we build the new ones
			onCreate(db, connectionSource);
		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Can't drop databases", e);
			throw new RuntimeException(e);
		}
	}

	public <ADO extends AbstractDomainObject> AbstractAdoDao<ADO> getAdoDaoInner(Class<ADO> type) {

		if (!adoDaos.containsKey(type)) {

			// build dao
			AbstractAdoDao<ADO> dao;
			Dao<ADO, Long> innerDao;

			try {
				innerDao = super.getDao(type);

				if (type.equals(Case.class)) {
					dao = (AbstractAdoDao<ADO>) new CaseDao((Dao<Case, Long>) innerDao);
				} else if (type.equals(Immunization.class)) {
					dao = (AbstractAdoDao<ADO>) new ImmunizationDao((Dao<Immunization, Long>) innerDao);
				} else if (type.equals(VaccinationEntity.class)) {
					dao = (AbstractAdoDao<ADO>) new VaccinationEntityDao((Dao<VaccinationEntity, Long>) innerDao);
				} else if (type.equals(Therapy.class)) {
					dao = (AbstractAdoDao<ADO>) new TherapyDao((Dao<Therapy, Long>) innerDao);
				} else if (type.equals(Prescription.class)) {
					dao = (AbstractAdoDao<ADO>) new PrescriptionDao((Dao<Prescription, Long>) innerDao);
				} else if (type.equals(Treatment.class)) {
					dao = (AbstractAdoDao<ADO>) new TreatmentDao((Dao<Treatment, Long>) innerDao);
				} else if (type.equals(Person.class)) {
					dao = (AbstractAdoDao<ADO>) new PersonDao((Dao<Person, Long>) innerDao);
				} else if (type.equals(PersonContactDetail.class)) {
					dao = (AbstractAdoDao<ADO>) new PersonContactDetailDao((Dao<PersonContactDetail, Long>) innerDao);
				} else if (type.equals(Location.class)) {
					dao = (AbstractAdoDao<ADO>) new LocationDao((Dao<Location, Long>) innerDao);
				} else if (type.equals(PointOfEntry.class)) {
					dao = (AbstractAdoDao<ADO>) new PointOfEntryDao((Dao<PointOfEntry, Long>) innerDao);
				} else if (type.equals(Facility.class)) {
					dao = (AbstractAdoDao<ADO>) new FacilityDao((Dao<Facility, Long>) innerDao);
				} else if (type.equals(Continent.class)) {
					dao = (AbstractAdoDao<ADO>) new ContinentDao((Dao<Continent, Long>) innerDao);
				} else if (type.equals(Subcontinent.class)) {
					dao = (AbstractAdoDao<ADO>) new SubcontinentDao((Dao<Subcontinent, Long>) innerDao);
				} else if (type.equals(Country.class)) {
					dao = (AbstractAdoDao<ADO>) new CountryDao((Dao<Country, Long>) innerDao);
				} else if (type.equals(Area.class)) {
					dao = (AbstractAdoDao<ADO>) new AreaDao((Dao<Area, Long>) innerDao);
				} else if (type.equals(Region.class)) {
					dao = (AbstractAdoDao<ADO>) new RegionDao((Dao<Region, Long>) innerDao);
				} else if (type.equals(District.class)) {
					dao = (AbstractAdoDao<ADO>) new DistrictDao((Dao<District, Long>) innerDao);
				} else if (type.equals(Community.class)) {
					dao = (AbstractAdoDao<ADO>) new CommunityDao((Dao<Community, Long>) innerDao);
				} else if (type.equals(User.class)) {
					dao = (AbstractAdoDao<ADO>) new UserDao((Dao<User, Long>) innerDao);
				} else if (type.equals(UserRoleConfig.class)) {
					dao = (AbstractAdoDao<ADO>) new UserRoleConfigDao((Dao<UserRoleConfig, Long>) innerDao);
				} else if (type.equals(DiseaseConfiguration.class)) {
					dao = (AbstractAdoDao<ADO>) new DiseaseConfigurationDao((Dao<DiseaseConfiguration, Long>) innerDao);
				} else if (type.equals(CustomizableEnumValue.class)) {
					dao = (AbstractAdoDao<ADO>) new CustomizableEnumValueDao((Dao<CustomizableEnumValue, Long>) innerDao);
				} else if (type.equals(FeatureConfiguration.class)) {
					dao = (AbstractAdoDao<ADO>) new FeatureConfigurationDao((Dao<FeatureConfiguration, Long>) innerDao);
				} else if (type.equals(Symptoms.class)) {
					dao = (AbstractAdoDao<ADO>) new SymptomsDao((Dao<Symptoms, Long>) innerDao);
				} else if (type.equals(HealthConditions.class)) {
					dao = (AbstractAdoDao<ADO>) new HealthConditionsDao((Dao<HealthConditions, Long>) innerDao);
				} else if (type.equals(ClinicalCourse.class)) {
					dao = (AbstractAdoDao<ADO>) new ClinicalCourseDao((Dao<ClinicalCourse, Long>) innerDao);
				} else if (type.equals(ClinicalVisit.class)) {
					dao = (AbstractAdoDao<ADO>) new ClinicalVisitDao((Dao<ClinicalVisit, Long>) innerDao);
				} else if (type.equals(MaternalHistory.class)) {
					dao = (AbstractAdoDao<ADO>) new MaternalHistoryDao((Dao<MaternalHistory, Long>) innerDao);
				} else if (type.equals(PortHealthInfo.class)) {
					dao = (AbstractAdoDao<ADO>) new PortHealthInfoDao((Dao<PortHealthInfo, Long>) innerDao);
				} else if (type.equals(Task.class)) {
					dao = (AbstractAdoDao<ADO>) new TaskDao((Dao<Task, Long>) innerDao);
				} else if (type.equals(Contact.class)) {
					dao = (AbstractAdoDao<ADO>) new ContactDao((Dao<Contact, Long>) innerDao);
				} else if (type.equals(Visit.class)) {
					dao = (AbstractAdoDao<ADO>) new VisitDao((Dao<Visit, Long>) innerDao);
				} else if (type.equals(Event.class)) {
					dao = (AbstractAdoDao<ADO>) new EventDao((Dao<Event, Long>) innerDao);
				} else if (type.equals(EventParticipant.class)) {
					dao = (AbstractAdoDao<ADO>) new EventParticipantDao((Dao<EventParticipant, Long>) innerDao);
				} else if (type.equals(Sample.class)) {
					dao = (AbstractAdoDao<ADO>) new SampleDao((Dao<Sample, Long>) innerDao);
				} else if (type.equals(PathogenTest.class)) {
					dao = (AbstractAdoDao<ADO>) new PathogenTestDao((Dao<PathogenTest, Long>) innerDao);
				} else if (type.equals(AdditionalTest.class)) {
					dao = (AbstractAdoDao<ADO>) new AdditionalTestDao((Dao<AdditionalTest, Long>) innerDao);
				} else if (type.equals(Hospitalization.class)) {
					dao = (AbstractAdoDao<ADO>) new HospitalizationDao((Dao<Hospitalization, Long>) innerDao);
				} else if (type.equals(PreviousHospitalization.class)) {
					dao = (AbstractAdoDao<ADO>) new PreviousHospitalizationDao((Dao<PreviousHospitalization, Long>) innerDao);
				} else if (type.equals(EpiData.class)) {
					dao = (AbstractAdoDao<ADO>) new EpiDataDao((Dao<EpiData, Long>) innerDao);
				} else if (type.equals(Exposure.class)) {
					dao = (AbstractAdoDao<ADO>) new ExposureDao((Dao<Exposure, Long>) innerDao);
				} else if (type.equals(ActivityAsCase.class)) {
					dao = (AbstractAdoDao<ADO>) new ActivityAsCaseDao((Dao<ActivityAsCase, Long>) innerDao);
				} else if (type.equals(WeeklyReport.class)) {
					dao = (AbstractAdoDao<ADO>) new WeeklyReportDao((Dao<WeeklyReport, Long>) innerDao);
				} else if (type.equals(WeeklyReportEntry.class)) {
					dao = (AbstractAdoDao<ADO>) new WeeklyReportEntryDao((Dao<WeeklyReportEntry, Long>) innerDao);
				} else if (type.equals(AggregateReport.class)) {
					dao = (AbstractAdoDao<ADO>) new AggregateReportDao((Dao<AggregateReport, Long>) innerDao);
				} else if (type.equals(Outbreak.class)) {
					dao = (AbstractAdoDao<ADO>) new OutbreakDao((Dao<Outbreak, Long>) innerDao);
				} else if (type.equals(DiseaseClassificationCriteria.class)) {
					dao = (AbstractAdoDao<ADO>) new DiseaseClassificationCriteriaDao((Dao<DiseaseClassificationCriteria, Long>) innerDao);
				} else if (type.equals(SormasToSormasOriginInfo.class)) {
					dao = (AbstractAdoDao<ADO>) new SormasToSormasOriginInfoDao((Dao<SormasToSormasOriginInfo, Long>) innerDao);
				} else if (type.equals(Campaign.class)) {
					dao = (AbstractAdoDao<ADO>) new CampaignDao((Dao<Campaign, Long>) innerDao);
				} else if (type.equals(CampaignFormMeta.class)) {
					dao = (AbstractAdoDao<ADO>) new CampaignFormMetaDao((Dao<CampaignFormMeta, Long>) innerDao);
				} else if (type.equals(CampaignFormData.class)) {
					dao = (AbstractAdoDao<ADO>) new CampaignFormDataDao((Dao<CampaignFormData, Long>) innerDao);
				}
				// TODO [vaccination info] integrate vaccination info
//				else if (type.equals(VaccinationInfo.class)) {
//					dao = (AbstractAdoDao<ADO>) new VaccinationInfoDao((Dao<VaccinationInfo, Long>) innerDao);
//				}
				else {
					throw new UnsupportedOperationException(type.toString());
				}

				adoDaos.put(type, dao);

			} catch (SQLException e) {
				Log.e(DatabaseHelper.class.getName(), "Can't build dao", e);
				throw new RuntimeException(e);
			}
		}

		return (AbstractAdoDao<ADO>) adoDaos.get(type);
	}

	public static <ADO extends AbstractDomainObject> AbstractAdoDao<ADO> getAdoDao(Class<ADO> type) {

		if (!instance.adoDaos.containsKey(type)) {
			synchronized (DatabaseHelper.class) {
				return instance.getAdoDaoInner(type);
			}
		}
		return (AbstractAdoDao<ADO>) instance.adoDaos.get(type);
	}

	public static ConfigDao getConfigDao() {
		if (instance.configDao == null) {
			synchronized (DatabaseHelper.class) {
				if (instance.configDao == null) {
					try {
						instance.configDao = new ConfigDao((Dao<Config, String>) instance.getDao(Config.class));
					} catch (SQLException e) {
						Log.e(DatabaseHelper.class.getName(), "Can't build ConfigDao", e);
						throw new RuntimeException(e);
					}
				}
			}
		}
		return instance.configDao;
	}

	public static SyncLogDao getSyncLogDao() {
		if (instance.syncLogDao == null) {
			synchronized (DatabaseHelper.class) {
				if (instance.syncLogDao == null) {
					try {
						instance.syncLogDao = new SyncLogDao((Dao<SyncLog, Long>) instance.getDao(SyncLog.class));
					} catch (SQLException e) {
						Log.e(DatabaseHelper.class.getName(), "Can't build SyncLogDao", e);
						throw new RuntimeException(e);
					}
				}
			}
		}
		return instance.syncLogDao;
	}

	public static LbdsSyncDao getLbdsSyncDao() {
		if (instance.lbdsSyncDao == null) {
			synchronized (DatabaseHelper.class) {
				if (instance.lbdsSyncDao == null) {
					try {
						instance.lbdsSyncDao = new LbdsSyncDao((Dao<LbdsSync, String>) instance.getDao(LbdsSync.class));
					} catch (SQLException e) {
						Log.e(DatabaseHelper.class.getName(), "Can't build SyncLogDao", e);
						throw new RuntimeException(e);
					}
				}
			}
		}
		return instance.lbdsSyncDao;
	}

	public static CaseDao getCaseDao() {
		return (CaseDao) getAdoDao(Case.class);
	}

	public static ImmunizationDao getImmunizationDao() {
		return (ImmunizationDao) getAdoDao(Immunization.class);
	}

	public static VaccinationEntityDao getVaccinationEntityDao() {
		return (VaccinationEntityDao) getAdoDao(VaccinationEntity.class);
	}

	public static TherapyDao getTherapyDao() {
		return (TherapyDao) getAdoDao(Therapy.class);
	}

	public static PrescriptionDao getPrescriptionDao() {
		return (PrescriptionDao) getAdoDao(Prescription.class);
	}

	public static TreatmentDao getTreatmentDao() {
		return (TreatmentDao) getAdoDao(Treatment.class);
	}

	public static ClinicalCourseDao getClinicalCourseDao() {
		return (ClinicalCourseDao) getAdoDao(ClinicalCourse.class);
	}

	public static HealthConditionsDao getHealthConditionsDao() {
		return (HealthConditionsDao) getAdoDao(HealthConditions.class);
	}

	public static ClinicalVisitDao getClinicalVisitDao() {
		return (ClinicalVisitDao) getAdoDao(ClinicalVisit.class);
	}

	public static MaternalHistoryDao getMaternalHistoryDao() {
		return (MaternalHistoryDao) getAdoDao(MaternalHistory.class);
	}

	public static PortHealthInfoDao getPortHealthInfoDao() {
		return (PortHealthInfoDao) getAdoDao(PortHealthInfo.class);
	}

	public static PersonDao getPersonDao() {
		return (PersonDao) getAdoDao(Person.class);
	}

	public static LocationDao getLocationDao() {
		return (LocationDao) getAdoDao(Location.class);
	}

	public static PersonContactDetailDao getPersonContactDetailDao() {
		return (PersonContactDetailDao) getAdoDao(PersonContactDetail.class);
	}

	public static PointOfEntryDao getPointOfEntryDao() {
		return (PointOfEntryDao) getAdoDao(PointOfEntry.class);
	}

	public static FacilityDao getFacilityDao() {
		return (FacilityDao) getAdoDao(Facility.class);
	}

	public static ContinentDao getContinentDao() {
		return (ContinentDao) getAdoDao(Continent.class);
	}

	public static SubcontinentDao getSubcontinentDao() {
		return (SubcontinentDao) getAdoDao(Subcontinent.class);
	}

	public static CountryDao getCountryDao() {
		return (CountryDao) getAdoDao(Country.class);
	}

	public static RegionDao getRegionDao() {
		return (RegionDao) getAdoDao(Region.class);
	}

	public static AreaDao getAreaDao() {
		return (AreaDao) getAdoDao(Area.class);
	}

	public static DistrictDao getDistrictDao() {
		return (DistrictDao) getAdoDao(District.class);
	}

	public static CommunityDao getCommunityDao() {
		return (CommunityDao) getAdoDao(Community.class);
	}

	public static UserDao getUserDao() {
		return (UserDao) getAdoDao(User.class);
	}

	public static UserRoleConfigDao getUserRoleConfigDao() {
		return (UserRoleConfigDao) getAdoDao(UserRoleConfig.class);
	}

	public static DiseaseConfigurationDao getDiseaseConfigurationDao() {
		return (DiseaseConfigurationDao) getAdoDao(DiseaseConfiguration.class);
	}

	public static CustomizableEnumValueDao getCustomizableEnumValueDao() {
		return (CustomizableEnumValueDao) getAdoDao(CustomizableEnumValue.class);
	}

	public static FeatureConfigurationDao getFeatureConfigurationDao() {
		return (FeatureConfigurationDao) getAdoDao(FeatureConfiguration.class);
	}

	public static SymptomsDao getSymptomsDao() {
		return (SymptomsDao) getAdoDao(Symptoms.class);
	}

	public static TaskDao getTaskDao() {
		return (TaskDao) getAdoDao(Task.class);
	}

	public static ContactDao getContactDao() {
		return (ContactDao) getAdoDao(Contact.class);
	}

	public static VisitDao getVisitDao() {
		return (VisitDao) getAdoDao(Visit.class);
	}

	public static EventDao getEventDao() {
		return (EventDao) getAdoDao(Event.class);
	}

	public static EventParticipantDao getEventParticipantDao() {
		return (EventParticipantDao) getAdoDao(EventParticipant.class);
	}

	public static SampleDao getSampleDao() {
		return (SampleDao) getAdoDao(Sample.class);
	}

	public static PathogenTestDao getSampleTestDao() {
		return (PathogenTestDao) getAdoDao(PathogenTest.class);
	}

	public static AdditionalTestDao getAdditionalTestDao() {
		return (AdditionalTestDao) getAdoDao(AdditionalTest.class);
	}

	public static HospitalizationDao getHospitalizationDao() {
		return (HospitalizationDao) getAdoDao(Hospitalization.class);
	}

	public static PreviousHospitalizationDao getPreviousHospitalizationDao() {
		return (PreviousHospitalizationDao) getAdoDao(PreviousHospitalization.class);
	}

	public static EpiDataDao getEpiDataDao() {
		return (EpiDataDao) getAdoDao(EpiData.class);
	}

	public static ExposureDao getExposureDao() {
		return (ExposureDao) getAdoDao(Exposure.class);
	}

	public static ActivityAsCaseDao getActivityAsCaseDao() {
		return (ActivityAsCaseDao) getAdoDao(ActivityAsCase.class);
	}

	public static WeeklyReportDao getWeeklyReportDao() {
		return (WeeklyReportDao) getAdoDao(WeeklyReport.class);
	}

	public static WeeklyReportEntryDao getWeeklyReportEntryDao() {
		return (WeeklyReportEntryDao) getAdoDao(WeeklyReportEntry.class);
	}

	public static OutbreakDao getOutbreakDao() {
		return (OutbreakDao) getAdoDao(Outbreak.class);
	}

	public static DiseaseClassificationCriteriaDao getDiseaseClassificationCriteriaDao() {
		return (DiseaseClassificationCriteriaDao) getAdoDao(DiseaseClassificationCriteria.class);
	}

	public static AggregateReportDao getAggregateReportDao() {
		return (AggregateReportDao) getAdoDao(AggregateReport.class);
	}

	public static CampaignDao getCampaignDao() {
		return (CampaignDao) getAdoDao(Campaign.class);
	}

	public static CampaignFormMetaDao getCampaignFormMetaDao() {
		return (CampaignFormMetaDao) getAdoDao(CampaignFormMeta.class);
	}

	public static CampaignFormDataDao getCampaignFormDataDao() {
		return (CampaignFormDataDao) getAdoDao(CampaignFormData.class);
	}

	// TODO [vaccination info] integrate vaccination info
//	public static VaccinationInfoDao getVaccinationInfoDao() {
//		return (VaccinationInfoDao) getAdoDao(VaccinationInfo.class);
//	}

	/**
	 * Close the database connections and clear any cached DAOs.
	 */
	@Override
	public void close() {
		super.close();
		configDao = null;
		adoDaos.clear();
		syncLogDao = null;
	}

	public static Context getContext() {
		return instance.context;
	}

	public static String getString(int stringResourceId) {
		if (instance.context == null) {
			return null;
		}

		return instance.context.getResources().getString(stringResourceId);
	}
}
