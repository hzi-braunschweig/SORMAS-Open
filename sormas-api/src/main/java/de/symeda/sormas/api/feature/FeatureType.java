/*
 * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * Copyright © 2016-2021 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package de.symeda.sormas.api.feature;

import java.util.ArrayList;
import java.util.List;

import de.symeda.sormas.api.i18n.I18nProperties;

/**
 * New server features are automatically added to the database in FeatureConfigurationService.createMissingFeatureConfigurations().
 */
public enum FeatureType {

	// FEATURE MODULES
	AGGREGATE_REPORTING(true, true, null),
	CAMPAIGNS(true, false, null),
	CASE_SURVEILANCE(true, true, null),
	CLINICAL_MANAGEMENT(true, true, null),
	CONTACT_TRACING(true,
		true,
		new FeatureType[] {
			CASE_SURVEILANCE }),
	EVENT_SURVEILLANCE(true, true, null),
	SAMPLES_LAB(true,
		true,
		new FeatureType[] {
			CASE_SURVEILANCE,
			EVENT_SURVEILLANCE }),
	TASK_MANAGEMENT(true, true, null),
	WEEKLY_REPORTING(true, true, null),

	// FEATURE EXTENSIONS
	ASSIGN_TASKS_TO_HIGHER_LEVEL(true,
		true,
		new FeatureType[] {
			TASK_MANAGEMENT }),
	CASE_FOLLOWUP(true,
		false,
		new FeatureType[] {
			CASE_SURVEILANCE }),
	DOCUMENTS(true,
		false,
		new FeatureType[] {
			CASE_SURVEILANCE,
			EVENT_SURVEILLANCE }),
	DOCUMENTS_MULTI_UPLOAD(true,
		true,
		new FeatureType[] {
			DOCUMENTS }),
	EVENT_GROUPS(true,
		true,
		new FeatureType[] {
			EVENT_SURVEILLANCE }),
	EVENT_HIERARCHIES(true,
		true,
		new FeatureType[] {
			EVENT_SURVEILLANCE }),
	LAB_MESSAGES(true,
		false,
		new FeatureType[] {
			SAMPLES_LAB }),
	MANUAL_EXTERNAL_MESSAGES(true,
		true,
		new FeatureType[] {
			CASE_SURVEILANCE }),
	NATIONAL_CASE_SHARING(true,
		false,
		new FeatureType[] {
			CASE_SURVEILANCE }),
	SURVEILLANCE_REPORTS(true,
		false,
		new FeatureType[] {
			CASE_SURVEILANCE }),
	SORMAS_TO_SORMAS_ACCEPT_REJECT(true,
		false,
		new FeatureType[] {
			CASE_SURVEILANCE,
			CONTACT_TRACING,
			EVENT_SURVEILLANCE }),
	SORMAS_TO_SORMAS_SHARE_CASES_WITH_CONTACTS_AND_SAMPLES(true,
			true,
			new FeatureType[]{
					CASE_SURVEILANCE,
					CONTACT_TRACING,
					SAMPLES_LAB}),
	SORMAS_TO_SORMAS_SHARE_EVENTS(true,
			false,
			new FeatureType[]{
					EVENT_SURVEILLANCE
			}),
	SORMAS_TO_SORMAS_SHARE_LAB_MESSAGES(true,
			false,
			new FeatureType[]{
					LAB_MESSAGES
			}),

	PERSON_DUPLICATE_CUSTOM_SEARCH(true, false, null),

	// SHOW/HIDE VIEW TAB FEATURES
	VIEW_TAB_CASES_HOSPITALIZATION(true,
		true,
		new FeatureType[] {
			CASE_SURVEILANCE }),
	VIEW_TAB_CASES_SYMPTOMS(true,
		true,
		new FeatureType[] {
			CASE_SURVEILANCE }),
	VIEW_TAB_CASES_EPIDEMIOLOGICAL_DATA(true,
		true,
		new FeatureType[] {
			CASE_SURVEILANCE }),
	VIEW_TAB_CASES_THERAPY(true,
		true,
		new FeatureType[] {
			CASE_SURVEILANCE }),
	VIEW_TAB_CASES_FOLLOW_UP(true,
		true,
		new FeatureType[] {
			CASE_SURVEILANCE }),
	VIEW_TAB_CASES_CLINICAL_COURSE(true,
		true,
		new FeatureType[] {
			CASE_SURVEILANCE }),
	VIEW_TAB_CONTACTS_EPIDEMIOLOGICAL_DATA(true,
		true,
		new FeatureType[] {
			CONTACT_TRACING }),
	VIEW_TAB_CONTACTS_FOLLOW_UP_VISITS(true,
		true,
		new FeatureType[] {
			CONTACT_TRACING }),

	// ADDITIONAL FEATURES
	GDPR_CONSENT_POPUP(true, false, null),
	INFRASTRUCTURE_TYPE_AREA(true, false, null),
	OUTBREAKS(true, true, null),
	PERSON_MANAGEMENT(true,
		true,
		new FeatureType[] {
			CASE_SURVEILANCE,
			EVENT_SURVEILLANCE }),

	// REGION- AND DISEASE-BASED FEATURES
	LINE_LISTING(false, false, null),

	// NOTIFICATION CONFIGURATIONS
	EVENT_GROUPS_MODIFICATION_NOTIFICATIONS(true,
		false,
		new FeatureType[] {
			EVENT_GROUPS }),
	EVENT_PARTICIPANT_CASE_CONFIRMED_NOTIFICATIONS(true,
		true,
		new FeatureType[] {
			EVENT_SURVEILLANCE }),
	EVENT_PARTICIPANT_RELATED_TO_OTHER_EVENTS_NOTIFICATIONS(true,
		true,
		new FeatureType[] {
			EVENT_SURVEILLANCE }),
	TASK_NOTIFICATIONS(true,
		true,
		new FeatureType[] {
			TASK_MANAGEMENT }),
	OTHER_NOTIFICATIONS(true, true, null),

	// TASK GENERATION FEATURES
	TASK_GENERATION_CASE_SURVEILLANCE(true,
		true,
		new FeatureType[] {
			TASK_MANAGEMENT }),
	TASK_GENERATION_CONTACT_TRACING(true,
		true,
		new FeatureType[] {
			TASK_MANAGEMENT }),
	TASK_GENERATION_EVENT_SURVEILLANCE(true,
		true,
		new FeatureType[] {
			TASK_MANAGEMENT }),
	TASK_GENERATION_GENERAL(true,
		true,
		new FeatureType[] {
			TASK_MANAGEMENT }),
	IMMUNIZATION_MANAGEMENT(true, false, null);

	/**
	 * Server feature means that the feature only needs to be configured once per server since they define the way the system
	 * is supposed to operate.
	 */
	private final boolean serverFeature;

	/**
	 * Is the feature enabled by default?
	 */
	private final boolean enabledDefault;

	private final FeatureType[] dependentFeatures;

	FeatureType(boolean serverFeature, boolean enabledDefault, FeatureType[] dependentFeatures) {
		this.serverFeature = serverFeature;
		this.enabledDefault = enabledDefault;
		this.dependentFeatures = dependentFeatures;
	}

	public String toString() {
		return I18nProperties.getEnumCaption(this);
	}

	public boolean isServerFeature() {
		return serverFeature;
	}

	public boolean isEnabledDefault() {
		return enabledDefault;
	}

	public static List<FeatureType> getAllServerFeatures() {
		List<FeatureType> serverFeatures = new ArrayList<>();
		for (FeatureType featureType : values()) {
			if (featureType.isServerFeature()) {
				serverFeatures.add(featureType);
			}
		}

		return serverFeatures;
	}

	public boolean isDependent() {
		return dependentFeatures != null;
	}

	public FeatureType[] getDependentFeatures() {
		return dependentFeatures;
	}

}
