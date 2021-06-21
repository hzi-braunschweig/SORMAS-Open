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
package de.symeda.sormas.backend.common;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import de.symeda.sormas.api.ConfigFacade;
import de.symeda.sormas.api.Language;
import de.symeda.sormas.api.SormasToSormasConfig;
import de.symeda.sormas.api.externaljournal.PatientDiaryConfig;
import de.symeda.sormas.api.externaljournal.SymptomJournalConfig;
import de.symeda.sormas.api.externaljournal.UserConfig;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.person.PersonHelper;
import de.symeda.sormas.api.region.GeoLatLon;
import de.symeda.sormas.api.utils.CompatibilityCheckResponse;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.api.utils.InfoProvider;
import de.symeda.sormas.api.utils.VersionHelper;

/**
 * Provides the application configuration settings
 */
@Stateless(name = "ConfigFacade")
public class ConfigFacadeEjb implements ConfigFacade {

	private static final String AUTHENTICATION_PROVIDER = "authentication.provider";
	private static final String AUTHENTICATION_PROVIDER_USER_SYNC_AT_STARTUP = "authentication.provider.userSyncAtStartup";

	public static final String COUNTRY_NAME = "country.name";
	public static final String COUNTRY_LOCALE = "country.locale";
	public static final String COUNTRY_EPID_PREFIX = "country.epidprefix";
	private static final String COUNTRY_CENTER_LAT = "country.center.latitude";
	private static final String COUNTRY_CENTER_LON = "country.center.longitude";
	private static final String MAP_USE_COUNTRY_CENTER = "map.usecountrycenter";
	private static final String MAP_ZOOM = "map.zoom";

	public static final String VERSION_PLACEHOLER = "%version";

	public static final String DEV_MODE = "devmode";

	public static final String CUSTOM_BRANDING = "custombranding";
	public static final String CUSTOM_BRANDING_NAME = "custombranding.name";
	public static final String CUSTOM_BRANDING_LOGO_PATH = "custombranding.logo.path";
	public static final String CUSTOM_BRANDING_USE_LOGIN_SIDEBAR = "custombranding.useloginsidebar";
	public static final String CUSTOM_BRANDING_LOGIN_BACKGROUND_PATH = "custombranding.loginbackground.path";

	public static final String APP_URL = "app.url";
	public static final String APP_LEGACY_URL = "app.legacy.url";

	public static final String FEATURE_AUTOMATIC_CASE_CLASSIFICATION = "feature.automaticcaseclassification";

	public static final String DOCUMENT_FILES_PATH = "documents.path";
	public static final String TEMP_FILES_PATH = "temp.path";
	public static final String GENERATED_FILES_PATH = "generated.path";
	public static final String CUSTOM_FILES_PATH = "custom.path";
	public static final String CSV_SEPARATOR = "csv.separator";
	public static final String RSCRIPT_EXECUTABLE = "rscript.executable";

	public static final String EMAIL_SENDER_ADDRESS = "email.sender.address";
	public static final String EMAIL_SENDER_NAME = "email.sender.name";
	public static final String SMS_SENDER_NAME = "sms.sender.name";
	public static final String SMS_AUTH_KEY = "sms.auth.key";
	public static final String SMS_AUTH_SECRET = "sms.auth.secret";

	public static final String DUPLICATE_CHECKS_EXCLUDE_PERSONS_OF_ACHIVED_ENTRIES = "duplicatechecks.excludepersonsonlylinkedtoarchivedentries";
	public static final String NAME_SIMILARITY_THRESHOLD = "namesimilaritythreshold";

	public static final String INFRASTRUCTURE_SYNC_THRESHOLD = "infrastructuresyncthreshold";

	public static final String INTERFACE_SYMPTOM_JOURNAL_URL = "interface.symptomjournal.url";
	public static final String INTERFACE_SYMPTOM_JOURNAL_AUTH_URL = "interface.symptomjournal.authurl";
	public static final String INTERFACE_SYMPTOM_JOURNAL_CLIENT_ID = "interface.symptomjournal.clientid";
	public static final String INTERFACE_SYMPTOM_JOURNAL_SECRET = "interface.symptomjournal.secret";
	public static final String INTERFACE_SYMPTOM_JOURNAL_DEFAULT_USER_USERNAME = "interface.symptomjournal.defaultuser.username";
	public static final String INTERFACE_SYMPTOM_JOURNAL_DEFAULT_USER_PASSWORD = "interface.symptomjournal.defaultuser.password";

	public static final String INTERFACE_PATIENT_DIARY_URL = "interface.patientdiary.url";
	public static final String INTERFACE_PATIENT_DIARY_PROBANDS_URL = "interface.patientdiary.probandsurl";
	public static final String INTERFACE_PATIENT_DIARY_AUTH_URL = "interface.patientdiary.authurl";
	public static final String INTERFACE_PATIENT_DIARY_EMAIL = "interface.patientdiary.email";
	public static final String INTERFACE_PATIENT_DIARY_PASSWORD = "interface.patientdiary.password";
	public static final String INTERFACE_PATIENT_DIARY_DEFAULT_USER_USERNAME = "interface.patientdiary.defaultuser.username";
	public static final String INTERFACE_PATIENT_DIARY_DEFAULT_USER_PASSWORD = "interface.patientdiary.defaultuser.password";

	public static final String DOCGENERATION_NULL_REPLACEMENT = "docgeneration.nullReplacement";
	public static final String INTERFACE_DEMIS_JNDINAME = "interface.demis.jndiName";

	public static final String DAYS_AFTER_CASE_GETS_ARCHIVED = "daysAfterCaseGetsArchived";
	private static final String DAYS_AFTER_EVENT_GETS_ARCHIVED = "daysAfterEventGetsArchived";

	private static final String DAYS_AFTER_SYSTEM_EVENT_GETS_DELETED = "daysAfterSystemEventGetsDeleted";

	private static final String GEOCODING_SERVICE_URL_TEMPLATE = "geocodingServiceUrlTemplate";
	private static final String GEOCODING_LONGITUDE_JSON_PATH = "geocodingLongitudeJsonPath";
	private static final String GEOCODING_LATITUDE_JSON_PATH = "geocodingLatitudeJsonPath";
	private static final String GEOCODING_EPSG4326_WKT = "geocodingEPSG4326_WKT";

	private static final String SORMAS2SORMAS_FILES_PATH = "sormas2sormas.path";
	private static final String SORMAS2SORMAS_SERVER_ACCESS_DATA_FILE_NAME = "sormas2sormas.serverAccessDataFileName";
	private static final String SORMAS2SORMAS_KEYSTORE_NAME = "sormas2sormas.keystoreName";
	private static final String SORMAS2SORMAS_KEYSTORE_PASSWORD = "sormas2sormas.keystorePass";
	private static final String SORMAS2SORMAS_TRUSTSTORE_NAME = "sormas2sormas.truststoreName";
	private static final String SORMAS2SORMAS_TRUSTSTORE_PASS = "sormas2sormas.truststorePass";
	private static final String SORMAS2SORMAS_RETAIN_CASE_EXTERNAL_TOKEN = "sormas2sormas.retainCaseExternalToken";
	private static final String SORMAS2SORMAS_OIDC_SERVER = "sormas2sormas.oidc.server";
	private static final String SORMAS2SORMAS_OIDC_REALM = "sormas2sormas.oidc.realm";
	private static final String SORMAS2SORMAS_OIDC_CLIENT_ID = "sormas2sormas.oidc.clientId";
	private static final String SORMAS2SORMAS_OIDC_CLIENT_SECRET = "sormas2sormas.oidc.clientSecret";

	private static final String SORMAS_TO_SORMAS_USER_PASSWORD = "sormasToSormasUserPassword";

	private static final String EXTERNAL_SURVEILLANCE_TOOL_GATEWAY_URL = "survnet.url";

	private static final String DASHBOARD_MAP_MARKER_LIMIT = "dashboardMapMarkerLimit";
	private static final String AUDITOR_ATTRIBUTE_LOGGING = "auditor.attribute.logging";

	private static final String CREATE_DEFAULT_ENTITIES = "createDefaultEntities";
	private static final String SKIP_DEFAULT_PASSWORD_CHECK = "skipDefaultPasswordCheck";

	private static final String STEP_SIZE_FOR_CSV_EXPORT = "stepSizeForCsvExport";

	private static final String UI_URL = "ui.url";

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Resource(lookup = "sormas/Properties")
	private Properties props;

	protected String getProperty(String name, String defaultValue) {

		String prop = props.getProperty(name);
		if (prop == null) {
			return defaultValue;
		} else {
			return prop;
		}
	}

	protected boolean getBoolean(String name, boolean defaultValue) {

		try {
			return Boolean.parseBoolean(getProperty(name, Boolean.toString(defaultValue)));
		} catch (Exception e) {
			logger.error("Could not parse boolean value of property '" + name + "': " + e.getMessage());
			return defaultValue;
		}
	}

	protected double getDouble(String name, double defaultValue) {

		try {
			return Double.parseDouble(getProperty(name, Double.toString(defaultValue)));
		} catch (Exception e) {
			logger.error("Could not parse numeric value of property '" + name + "': " + e.getMessage());
			return defaultValue;
		}
	}

	protected int getInt(String name, int defaultValue) {

		try {
			return Integer.parseInt(getProperty(name, Integer.toString(defaultValue)));
		} catch (Exception e) {
			logger.error("Could not parse integer value of property '" + name + "': " + e.getMessage());
			return defaultValue;
		}
	}

	@Override
	public String getCountryName() {

		String countryName = getProperty(COUNTRY_NAME, "");
		if (countryName.isEmpty()) {
			logger.info("No country name is specified in sormas.properties.");
		}
		return new String(countryName.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
	}

	/**
	 * Returns the normalized locale
	 */
	@Override
	public String getCountryLocale() {

		String locale = getProperty(COUNTRY_LOCALE, Language.EN.getLocale().toString());
		return normalizeLocaleString(locale);
	}

	@Override
	public String getCountryCode() {
		String locale = getProperty(COUNTRY_LOCALE, Language.EN.getLocale().toString());
		String normalizedLocale = normalizeLocaleString(locale);

		if (normalizedLocale.contains("-")) {
			return normalizedLocale.substring(normalizedLocale.lastIndexOf("-") + 1);
		} else {
			return normalizedLocale;
		}
	}

	static String normalizeLocaleString(String locale) {

		locale = locale.trim();
		int pos = Math.max(locale.indexOf('-'), locale.indexOf('_'));
		if (pos < 0) {
			locale = locale.toLowerCase();
		} else {
			locale = locale.substring(0, pos).toLowerCase(Locale.ENGLISH) + '-' + locale.substring(pos + 1).toUpperCase(Locale.ENGLISH);
		}
		return locale;
	}

	@Override
	public boolean isConfiguredCountry(String countryCode) {
		if (Pattern.matches(I18nProperties.FULL_COUNTRY_LOCALE_PATTERN, getCountryLocale())) {
			return getCountryLocale().toLowerCase().endsWith(countryCode.toLowerCase());
		} else {
			return getCountryLocale().toLowerCase().startsWith(countryCode.toLowerCase());
		}
	}

	@Override
	public String getEpidPrefix() {
		return getProperty(COUNTRY_EPID_PREFIX, "");
	}

	@Override
	public GeoLatLon getCountryCenter() {
		return new GeoLatLon(getDouble(COUNTRY_CENTER_LAT, 0), getDouble(COUNTRY_CENTER_LON, 0));
	}

	@Override
	public boolean isMapUseCountryCenter() {
		return getBoolean(MAP_USE_COUNTRY_CENTER, false);
	}

	@Override
	public int getMapZoom() {
		return getInt(MAP_ZOOM, 1);
	}

	@Override
	public boolean isDevMode() {
		return getBoolean(DEV_MODE, false);
	}

	@Override
	public boolean isCustomBranding() {
		return getBoolean(CUSTOM_BRANDING, false);
	}

	@Override
	public String getCustomBrandingName() {
		return getProperty(CUSTOM_BRANDING_NAME, "SORMAS");
	}

	@Override
	public String getCustomBrandingLogoPath() {
		return getProperty(CUSTOM_BRANDING_LOGO_PATH, null);
	}

	@Override
	public boolean isUseLoginSidebar() {
		return getBoolean(CUSTOM_BRANDING_USE_LOGIN_SIDEBAR, true);
	}

	@Override
	public String getLoginBackgroundPath() {
		return getProperty(CUSTOM_BRANDING_LOGIN_BACKGROUND_PATH, null);
	}

	@Override
	public String getSormasInstanceName() {
		return isCustomBranding() ? getCustomBrandingName() : "SORMAS";
	}

	@Override
	public String getAppUrl() {

		String appUrl = getProperty(APP_URL, null);
		if (appUrl != null) {
			appUrl = appUrl.replaceAll(VERSION_PLACEHOLER, InfoProvider.get().getVersion());
		}
		return appUrl;
	}

	@Override
	public String getAppLegacyUrl() {
		return getProperty(APP_LEGACY_URL, null);
	}

	@Override
	public String getUiUrl() {
		return getProperty(UI_URL, null);
	}

	@Override
	public String getDocumentFilesPath() {
		return getProperty(DOCUMENT_FILES_PATH, "/opt/sormas/documents/");
	}

	@Override
	public String getTempFilesPath() {
		return getProperty(TEMP_FILES_PATH, "/opt/sormas/temp/");
	}

	@Override
	public String getGeneratedFilesPath() {
		return getProperty(GENERATED_FILES_PATH, "/opt/sormas/generated/");
	}

	@Override
	public String getCustomFilesPath() {
		return getProperty(CUSTOM_FILES_PATH, "/opt/sormas/custom/");
	}

	@Override
	public String getRScriptExecutable() {
		return getProperty(RSCRIPT_EXECUTABLE, null);
	}

	@Override
	public boolean isFeatureAutomaticCaseClassification() {
		return getBoolean(FEATURE_AUTOMATIC_CASE_CLASSIFICATION, true);
	}

	@Override
	public String getEmailSenderAddress() {
		return getProperty(EMAIL_SENDER_ADDRESS, "noreply@sormas.org");
	}

	@Override
	public String getEmailSenderName() {
		return getProperty(EMAIL_SENDER_NAME, "SORMAS Support");
	}

	@Override
	public String getSmsSenderName() {
		return getProperty(SMS_SENDER_NAME, "SORMAS");
	}

	@Override
	public String getSmsAuthKey() {
		return getProperty(SMS_AUTH_KEY, "");
	}

	@Override
	public String getSmsAuthSecret() {
		return getProperty(SMS_AUTH_SECRET, "");
	}

	@Override
	public boolean isDuplicateChecksExcludePersonsOfArchivedEntries() {
		return getBoolean(DUPLICATE_CHECKS_EXCLUDE_PERSONS_OF_ACHIVED_ENTRIES, false);
	}

	@Override
	public double getNameSimilarityThreshold() {
		return getDouble(NAME_SIMILARITY_THRESHOLD, PersonHelper.DEFAULT_NAME_SIMILARITY_THRESHOLD);
	}

	@Override
	public int getInfrastructureSyncThreshold() {
		return getInt(INFRASTRUCTURE_SYNC_THRESHOLD, 1000);
	}

	@Override
	public char getCsvSeparator() {

		String seperatorString = getProperty(CSV_SEPARATOR, ",");
		if (seperatorString.length() != 1) {
			throw new IllegalArgumentException(CSV_SEPARATOR + " must be a single character instead of '" + seperatorString + "'");
		}
		return seperatorString.charAt(0);
	}

	@Override
	public int getDaysAfterCaseGetsArchived() {
		return getInt(DAYS_AFTER_CASE_GETS_ARCHIVED, 90);
	}

	@Override
	public int getDaysAfterEventGetsArchived() {
		return getInt(DAYS_AFTER_EVENT_GETS_ARCHIVED, 90);
	}

	@Override
	public int getDaysAfterSystemEventGetsDeleted() {
		return getInt(DAYS_AFTER_SYSTEM_EVENT_GETS_DELETED, 90);
	}

	@Override
	public String getGeocodingServiceUrlTemplate() {
		return getProperty(GEOCODING_SERVICE_URL_TEMPLATE, null);
	}

	@Override
	public String getGeocodingLongitudeJsonPath() {
		return getProperty(GEOCODING_LONGITUDE_JSON_PATH, null);
	}

	@Override
	public String getGeocodingLatitudeJsonPath() {
		return getProperty(GEOCODING_LATITUDE_JSON_PATH, null);
	}

	@Override
	public String getGeocodingEPSG4326_WKT() {
		return getProperty(
			GEOCODING_EPSG4326_WKT,
			"GEOGCS[\"WGS 84\",DATUM[\"WGS_1984\",SPHEROID[\"WGS 84\",6378137,298.257223563,AUTHORITY[\"EPSG\",\"7030\"]],AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.01745329251994328,AUTHORITY[\"EPSG\",\"9122\"]],AXIS[\"Long\",EAST],AXIS[\"Lat\",NORTH],AUTHORITY[\"EPSG\",\"4326\"]]");
	}

	@Override
	public SymptomJournalConfig getSymptomJournalConfig() {
		SymptomJournalConfig config = new SymptomJournalConfig();
		config.setUrl(getProperty(INTERFACE_SYMPTOM_JOURNAL_URL, null));
		config.setAuthUrl(getProperty(INTERFACE_SYMPTOM_JOURNAL_AUTH_URL, null));
		config.setClientId(getProperty(INTERFACE_SYMPTOM_JOURNAL_CLIENT_ID, null));
		config.setSecret(getProperty(INTERFACE_SYMPTOM_JOURNAL_SECRET, null));

		UserConfig userConfig = new UserConfig();
		userConfig.setUsername(getProperty(INTERFACE_SYMPTOM_JOURNAL_DEFAULT_USER_USERNAME, null));
		userConfig.setPassword(getProperty(INTERFACE_SYMPTOM_JOURNAL_DEFAULT_USER_PASSWORD, null));

		if (StringUtils.isNoneBlank(userConfig.getUsername(), userConfig.getPassword())) {
			config.setDefaultUser(userConfig);
		}

		return config;
	}

	@Override
	public PatientDiaryConfig getPatientDiaryConfig() {
		PatientDiaryConfig config = new PatientDiaryConfig();
		config.setUrl(getProperty(INTERFACE_PATIENT_DIARY_URL, null));
		config.setProbandsUrl(getProperty(INTERFACE_PATIENT_DIARY_PROBANDS_URL, null));
		config.setAuthUrl(getProperty(INTERFACE_PATIENT_DIARY_AUTH_URL, null));
		config.setEmail(getProperty(INTERFACE_PATIENT_DIARY_EMAIL, null));
		config.setPassword(getProperty(INTERFACE_PATIENT_DIARY_PASSWORD, null));

		UserConfig userConfig = new UserConfig();
		userConfig.setUsername(getProperty(INTERFACE_PATIENT_DIARY_DEFAULT_USER_USERNAME, null));
		userConfig.setPassword(getProperty(INTERFACE_PATIENT_DIARY_DEFAULT_USER_PASSWORD, null));

		if (StringUtils.isNoneBlank(userConfig.getUsername(), userConfig.getPassword())) {
			config.setDefaultUser(userConfig);
		}

		return config;
	}

	@Override
	public SormasToSormasConfig getSormasToSormasConfig() {
		SormasToSormasConfig config = new SormasToSormasConfig();
		config.setPath(getProperty(SORMAS2SORMAS_FILES_PATH, null));
		config.setServerAccessDataFileName(getProperty(SORMAS2SORMAS_SERVER_ACCESS_DATA_FILE_NAME, null));
		config.setKeystoreName(getProperty(SORMAS2SORMAS_KEYSTORE_NAME, null));
		config.setKeystorePass(getProperty(SORMAS2SORMAS_KEYSTORE_PASSWORD, null));
		config.setTruststoreName(getProperty(SORMAS2SORMAS_TRUSTSTORE_NAME, null));
		config.setTruststorePass(getProperty(SORMAS2SORMAS_TRUSTSTORE_PASS, null));
		config.setRetainCaseExternalToken(getBoolean(SORMAS2SORMAS_RETAIN_CASE_EXTERNAL_TOKEN, true));
		config.setOidcServer(getProperty(SORMAS2SORMAS_OIDC_SERVER, null));
		config.setOidcRealm(getProperty(SORMAS2SORMAS_OIDC_REALM, null));
		config.setOidcClientId(getProperty(SORMAS2SORMAS_OIDC_CLIENT_ID, null));
		config.setOidcClientSecret(getProperty(SORMAS2SORMAS_OIDC_CLIENT_SECRET, null));
		return config;
	}

	@Override
	public String getSormasToSormasUserPassword() {
		return getProperty(SORMAS_TO_SORMAS_USER_PASSWORD, null);
	}

	@Override
	public String getExternalSurveillanceToolGatewayUrl() {
		return getProperty(EXTERNAL_SURVEILLANCE_TOOL_GATEWAY_URL, null);
	}

	@Override
	public void validateExternalUrls() {

		List<String> urls = Lists.newArrayList(
			getSymptomJournalConfig().getUrl(),
			getSymptomJournalConfig().getAuthUrl(),
			getPatientDiaryConfig().getUrl(),
			getPatientDiaryConfig().getProbandsUrl(),
			getPatientDiaryConfig().getAuthUrl(),

			getSormasToSormasConfig().getOidcRealmCertEndoint(),
			getSormasToSormasConfig().getOidcRealmTokenEndoint(),
			getSormasToSormasConfig().getOidcRealmUrl(),
			getSormasToSormasConfig().getOidcServer());

		// Must be a valid URL
		UrlValidator urlValidator = new UrlValidator(
			new String[] {
				"http",
				"https" },
			UrlValidator.ALLOW_LOCAL_URLS);

		urls.forEach(url -> {
			if (StringUtils.isBlank(url)) {
				return;
			}

			if (!urlValidator.isValid(url)) {
				throw new IllegalArgumentException("'" + url + "' is not a valid URL");
			}
		});
	}

	@Override
	public String getAuthenticationProvider() {
		return getProperty(AUTHENTICATION_PROVIDER, "SORMAS");
	}

	@Override
	public boolean isAuthenticationProviderUserSyncAtStartupEnabled() {
		return getBoolean(AUTHENTICATION_PROVIDER_USER_SYNC_AT_STARTUP, false);
	}

	@Override
	public boolean isExternalJournalActive() {
		return !StringUtils.isAllBlank(getProperty(INTERFACE_SYMPTOM_JOURNAL_URL, null), getProperty(INTERFACE_PATIENT_DIARY_URL, null));
	}

	@Override
	public void validateAppUrls() {

		String appUrl = getAppUrl();
		String appLegacyUrl = getAppLegacyUrl();

		// must contain version information
		int[] appVersion = VersionHelper.extractVersion(appUrl);
		if (!DataHelper.isNullOrEmpty(appUrl) && !VersionHelper.isVersion(appVersion)) {
			throw new IllegalArgumentException("Property '" + ConfigFacadeEjb.APP_URL + "' must contain a valid version: '" + appUrl + "'");
		}
		int[] appLegacyVersion = VersionHelper.extractVersion(appLegacyUrl);
		if (!DataHelper.isNullOrEmpty(appLegacyUrl) && !VersionHelper.isVersion(appLegacyVersion)) {
			throw new IllegalArgumentException(
				"Property '" + ConfigFacadeEjb.APP_LEGACY_URL + "' must contain a valid version: '" + appLegacyUrl + "'");
		}

		// legacy must be empty or before app version
		if (appLegacyVersion != null && appVersion != null) {
			if (!VersionHelper.isBefore(appLegacyVersion, appVersion)) {
				throw new IllegalArgumentException(
					"Property '" + ConfigFacadeEjb.APP_LEGACY_URL + "' must have a version smaller " + "than property '" + ConfigFacadeEjb.APP_URL
						+ "': '" + appLegacyUrl + "' - '" + appUrl + "'");
			}
		}

		// both have to be compatible
		if (appVersion != null && InfoProvider.get().isCompatibleToApi(appVersion) != CompatibilityCheckResponse.COMPATIBLE) {
			throw new IllegalArgumentException(
				"Property '" + ConfigFacadeEjb.APP_URL + "' does not point to a compatible app version: '" + appUrl + "'. Minimum is '"
					+ InfoProvider.get().getMinimumRequiredVersion() + "'");
		}

		if (appLegacyVersion != null && InfoProvider.get().isCompatibleToApi(appLegacyVersion) != CompatibilityCheckResponse.COMPATIBLE) {
			throw new IllegalArgumentException(
				"Property '" + ConfigFacadeEjb.APP_LEGACY_URL + "' does not point to a compatible app version: '" + appLegacyUrl + "'. Minimum is '"
					+ InfoProvider.get().getMinimumRequiredVersion() + "'");
		}

	}

	public int getDashboardMapMarkerLimit() {
		return getInt(DASHBOARD_MAP_MARKER_LIMIT, -1);
	}

	public boolean isCreateDefaultEntities() {
		return getBoolean(CREATE_DEFAULT_ENTITIES, false);
	}

	public boolean isSkipDefaultPasswordCheck() {
		return getBoolean(SKIP_DEFAULT_PASSWORD_CHECK, false);
	}

	public String getDocgenerationNullReplacement() {
		return getProperty(DOCGENERATION_NULL_REPLACEMENT, "./.");
	}

	@Override
	public boolean isAuditorAttributeLoggingEnabled() {
		return getBoolean(AUDITOR_ATTRIBUTE_LOGGING, true);
	}

	@Override
	public int getStepSizeForCsvExport() {
		return getInt(STEP_SIZE_FOR_CSV_EXPORT, 5000);
	}

	@Override
	public boolean isSmsServiceSetUp() {
		return !StringUtils.isAnyBlank(getProperty(SMS_AUTH_KEY, null), getProperty(SMS_AUTH_SECRET, null));
	}

	@Override
	public String getDemisJndiName() {
		return getProperty(INTERFACE_DEMIS_JNDINAME, null);
	}

	@LocalBean
	@Stateless
	public static class ConfigFacadeEjbLocal extends ConfigFacadeEjb {

	}
}
