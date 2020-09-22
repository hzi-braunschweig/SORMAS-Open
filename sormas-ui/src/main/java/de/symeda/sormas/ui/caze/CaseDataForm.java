/*******************************************************************************
 * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * Copyright © 2016-2018 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
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
 *******************************************************************************/
package de.symeda.sormas.ui.caze;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.UserError;
import com.vaadin.ui.Button;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseListener;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.data.Property;
import com.vaadin.v7.data.util.converter.Converter.ConversionException;
import com.vaadin.v7.data.validator.DateRangeValidator;
import com.vaadin.v7.shared.ui.datefield.Resolution;
import com.vaadin.v7.ui.AbstractSelect;
import com.vaadin.v7.ui.CheckBox;
import com.vaadin.v7.ui.ComboBox;
import com.vaadin.v7.ui.DateField;
import com.vaadin.v7.ui.Field;
import com.vaadin.v7.ui.OptionGroup;
import com.vaadin.v7.ui.TextArea;
import com.vaadin.v7.ui.TextField;
import de.symeda.sormas.api.CountryHelper;
import de.symeda.sormas.api.Disease;
import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.caze.CaseClassification;
import de.symeda.sormas.api.caze.CaseDataDto;
import de.symeda.sormas.api.caze.CaseLogic;
import de.symeda.sormas.api.caze.CaseOrigin;
import de.symeda.sormas.api.caze.CaseOutcome;
import de.symeda.sormas.api.caze.HospitalWardType;
import de.symeda.sormas.api.caze.InvestigationStatus;
import de.symeda.sormas.api.caze.Vaccination;
import de.symeda.sormas.api.caze.classification.DiseaseClassificationCriteriaDto;
import de.symeda.sormas.api.contact.FollowUpStatus;
import de.symeda.sormas.api.contact.QuarantineType;
import de.symeda.sormas.api.event.TypeOfPlace;
import de.symeda.sormas.api.facility.FacilityDto;
import de.symeda.sormas.api.facility.FacilityReferenceDto;
import de.symeda.sormas.api.facility.FacilityType;
import de.symeda.sormas.api.facility.FacilityTypeGroup;
import de.symeda.sormas.api.feature.FeatureType;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.i18n.Validations;
import de.symeda.sormas.api.person.PersonDto;
import de.symeda.sormas.api.person.Sex;
import de.symeda.sormas.api.region.CommunityReferenceDto;
import de.symeda.sormas.api.region.DistrictReferenceDto;
import de.symeda.sormas.api.region.RegionReferenceDto;
import de.symeda.sormas.api.symptoms.SymptomsDto;
import de.symeda.sormas.api.user.UserRight;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.api.utils.DateHelper;
import de.symeda.sormas.api.utils.YesNoUnknown;
import de.symeda.sormas.api.utils.fieldvisibility.FieldVisibilityCheckers;
import de.symeda.sormas.api.utils.fieldvisibility.checkers.CountryFieldVisibilityChecker;
import de.symeda.sormas.ui.ControllerProvider;
import de.symeda.sormas.ui.UserProvider;
import de.symeda.sormas.ui.utils.AbstractEditForm;
import de.symeda.sormas.ui.utils.ButtonHelper;
import de.symeda.sormas.ui.utils.CommitDiscardWrapperComponent.DoneListener;
import de.symeda.sormas.ui.utils.ConfirmationComponent;
import de.symeda.sormas.ui.utils.CssStyles;
import de.symeda.sormas.ui.utils.FieldHelper;
import de.symeda.sormas.ui.utils.OutbreakFieldVisibilityChecker;
import de.symeda.sormas.ui.utils.StringToAngularLocationConverter;
import de.symeda.sormas.ui.utils.UiFieldAccessCheckers;
import de.symeda.sormas.ui.utils.VaadinUiUtil;
import de.symeda.sormas.ui.utils.ViewMode;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static de.symeda.sormas.ui.utils.CssStyles.ERROR_COLOR_PRIMARY;
import static de.symeda.sormas.ui.utils.CssStyles.FORCE_CAPTION;
import static de.symeda.sormas.ui.utils.CssStyles.H3;
import static de.symeda.sormas.ui.utils.CssStyles.LAYOUT_COL_HIDE_INVSIBLE;
import static de.symeda.sormas.ui.utils.CssStyles.SOFT_REQUIRED;
import static de.symeda.sormas.ui.utils.CssStyles.VSPACE_3;
import static de.symeda.sormas.ui.utils.CssStyles.style;
import static de.symeda.sormas.ui.utils.LayoutUtil.fluidColumn;
import static de.symeda.sormas.ui.utils.LayoutUtil.fluidColumnLoc;
import static de.symeda.sormas.ui.utils.LayoutUtil.fluidColumnLocCss;
import static de.symeda.sormas.ui.utils.LayoutUtil.fluidRow;
import static de.symeda.sormas.ui.utils.LayoutUtil.fluidRowLocs;
import static de.symeda.sormas.ui.utils.LayoutUtil.inlineLocs;
import static de.symeda.sormas.ui.utils.LayoutUtil.loc;
import static de.symeda.sormas.ui.utils.LayoutUtil.locCss;
import static de.symeda.sormas.ui.utils.LayoutUtil.locs;

public class CaseDataForm extends AbstractEditForm<CaseDataDto> {

	private static final long serialVersionUID = 1L;

	private static final String CASE_DATA_HEADING_LOC = "caseDataHeadingLoc";
	private static final String MEDICAL_INFORMATION_LOC = "medicalInformationLoc";
	private static final String PAPER_FORM_DATES_LOC = "paperFormDatesLoc";
	private static final String SMALLPOX_VACCINATION_SCAR_IMG = "smallpoxVaccinationScarImg";
	private static final String CLASSIFICATION_RULES_LOC = "classificationRulesLoc";
	private static final String CLASSIFIED_BY_SYSTEM_LOC = "classifiedBySystemLoc";
	private static final String ASSIGN_NEW_EPID_NUMBER_LOC = "assignNewEpidNumberLoc";
	private static final String EPID_NUMBER_WARNING_LOC = "epidNumberWarningLoc";
	private static final String GENERAL_COMMENT_LOC = "generalCommentLoc";
	private static final String FOLLOW_UP_STATUS_HEADING_LOC = "followUpStatusHeadingLoc";
	private static final String CANCEL_OR_RESUME_FOLLOW_UP_BTN_LOC = "cancelOrResumeFollowUpBtnLoc";
	private static final String LOST_FOLLOW_UP_BTN_LOC = "lostFollowUpBtnLoc";
	private static final String FACILITY_OR_HOME_LOC = "facilityOrHomeLoc";
	private static final String TYPE_GROUP_LOC = "typeGroupLoc";

	//@formatter:off
	private static final String MAIN_HTML_LAYOUT =
			loc(CASE_DATA_HEADING_LOC) +
					fluidRowLocs(4, CaseDataDto.UUID, 3, CaseDataDto.REPORT_DATE, 5, CaseDataDto.REPORTING_USER) +
					fluidRowLocs(4, CaseDataDto.CLINICAL_CONFIRMATION, 4, CaseDataDto.EPIDEMIOLOGICAL_CONFIRMATION, 4, CaseDataDto.LABORATORY_DIAGNOSTIC_CONFIRMATION) +
					inlineLocs(CaseDataDto.CASE_CLASSIFICATION, CLASSIFICATION_RULES_LOC) +
					fluidRow(
							fluidColumnLoc(3, 0, CaseDataDto.CLASSIFICATION_DATE),
							fluidColumnLocCss(LAYOUT_COL_HIDE_INVSIBLE, 5, 0, CaseDataDto.CLASSIFICATION_USER),
							fluidColumnLocCss(LAYOUT_COL_HIDE_INVSIBLE, 4, 0, CLASSIFIED_BY_SYSTEM_LOC)) +
					fluidRowLocs(9, CaseDataDto.INVESTIGATION_STATUS, 3, CaseDataDto.INVESTIGATED_DATE) +
					fluidRowLocs(6, CaseDataDto.EPID_NUMBER, 3, ASSIGN_NEW_EPID_NUMBER_LOC) +
					loc(EPID_NUMBER_WARNING_LOC) +
					fluidRowLocs(6, CaseDataDto.EXTERNAL_ID, 6, null) +
					fluidRow(
							fluidColumnLoc(6, 0, CaseDataDto.DISEASE),
							fluidColumn(6, 0, locs(
									CaseDataDto.DISEASE_DETAILS,
									CaseDataDto.PLAGUE_TYPE,
									CaseDataDto.DENGUE_FEVER_TYPE,
									CaseDataDto.RABIES_TYPE))) +
					fluidRowLocs(9, CaseDataDto.OUTCOME, 3, CaseDataDto.OUTCOME_DATE) +
					fluidRowLocs(3, CaseDataDto.SEQUELAE, 9, CaseDataDto.SEQUELAE_DETAILS) +
					fluidRowLocs(CaseDataDto.REPORTING_TYPE,
							"")
					+
					fluidRowLocs(CaseDataDto.CASE_ORIGIN, "") +
					fluidRowLocs(CaseDataDto.REGION, CaseDataDto.DISTRICT, CaseDataDto.COMMUNITY) +
					fluidRowLocs(FACILITY_OR_HOME_LOC, TYPE_GROUP_LOC, CaseDataDto.FACILITY_TYPE) +
					fluidRowLocs(CaseDataDto.HEALTH_FACILITY, CaseDataDto.HEALTH_FACILITY_DETAILS) +
					fluidRowLocs(CaseDataDto.POINT_OF_ENTRY, CaseDataDto.POINT_OF_ENTRY_DETAILS) +
					locCss(VSPACE_3, CaseDataDto.SHARED_TO_COUNTRY) +
					fluidRowLocs(4, CaseDataDto.QUARANTINE_HOME_POSSIBLE, 8, CaseDataDto.QUARANTINE_HOME_POSSIBLE_COMMENT) +
					fluidRowLocs(4, CaseDataDto.QUARANTINE_HOME_SUPPLY_ENSURED, 8, CaseDataDto.QUARANTINE_HOME_SUPPLY_ENSURED_COMMENT) +
					fluidRowLocs(6, CaseDataDto.QUARANTINE, 3, CaseDataDto.QUARANTINE_FROM, 3, CaseDataDto.QUARANTINE_TO) +
					fluidRowLocs(CaseDataDto.QUARANTINE_EXTENDED) +
					fluidRowLocs(CaseDataDto.QUARANTINE_REDUCED) +
					fluidRowLocs(CaseDataDto.QUARANTINE_TYPE_DETAILS) +
					fluidRowLocs(CaseDataDto.QUARANTINE_ORDERED_VERBALLY, CaseDataDto.QUARANTINE_ORDERED_VERBALLY_DATE) +
					fluidRowLocs(CaseDataDto.QUARANTINE_ORDERED_OFFICIAL_DOCUMENT, CaseDataDto.QUARANTINE_ORDERED_OFFICIAL_DOCUMENT_DATE) +
					fluidRowLocs(CaseDataDto.QUARANTINE_OFFICIAL_ORDER_SENT, CaseDataDto.QUARANTINE_OFFICIAL_ORDER_SENT_DATE) +
					fluidRowLocs(CaseDataDto.QUARANTINE_HELP_NEEDED) +
					fluidRowLocs(CaseDataDto.REPORT_LAT, CaseDataDto.REPORT_LON, CaseDataDto.REPORT_LAT_LON_ACCURACY) +
					loc(MEDICAL_INFORMATION_LOC) +
					fluidRowLocs(CaseDataDto.PREGNANT, CaseDataDto.POSTPARTUM) + fluidRowLocs(CaseDataDto.TRIMESTER, "")
					+
					fluidRowLocs(CaseDataDto.VACCINATION, CaseDataDto.VACCINATION_DOSES) +
					fluidRowLocs(CaseDataDto.VACCINE, "") +
					fluidRowLocs(CaseDataDto.SMALLPOX_VACCINATION_RECEIVED, CaseDataDto.SMALLPOX_VACCINATION_SCAR) +
					fluidRowLocs(SMALLPOX_VACCINATION_SCAR_IMG) +
					fluidRowLocs(CaseDataDto.VACCINATION_DATE, CaseDataDto.VACCINATION_INFO_SOURCE) +
					fluidRowLocs(CaseDataDto.SURVEILLANCE_OFFICER, CaseDataDto.CLINICIAN_NAME) +
					fluidRowLocs(CaseDataDto.NOTIFYING_CLINIC, CaseDataDto.NOTIFYING_CLINIC_DETAILS) +
					fluidRowLocs(CaseDataDto.CLINICIAN_PHONE, CaseDataDto.CLINICIAN_EMAIL);

	private static final String FOLLOWUP_LAYOUT =
			loc(FOLLOW_UP_STATUS_HEADING_LOC) +
					fluidRowLocs(CaseDataDto.FOLLOW_UP_STATUS, CANCEL_OR_RESUME_FOLLOW_UP_BTN_LOC, LOST_FOLLOW_UP_BTN_LOC) +
					fluidRowLocs(4, CaseDataDto.FOLLOW_UP_UNTIL, 8, CaseDataDto.OVERWRITE_FOLLOW_UP_UNTIL) +
					fluidRowLocs(CaseDataDto.FOLLOW_UP_COMMENT);

	private static final String PAPER_FORM_DATES_AND_COMMENTS_HTML_LAYOUT =
			loc(PAPER_FORM_DATES_LOC) +
					fluidRowLocs(CaseDataDto.DISTRICT_LEVEL_DATE, CaseDataDto.REGION_LEVEL_DATE, CaseDataDto.NATIONAL_LEVEL_DATE) +
					loc(GENERAL_COMMENT_LOC) + fluidRowLocs(CaseDataDto.ADDITIONAL_DETAILS);
	//@formatter:on

	private final String caseUuid;
	private final PersonDto person;
	private final Disease disease;
	private final SymptomsDto symptoms;
	private final boolean caseFollowUpEnabled;
	private Field<?> quarantine;
	private DateField quarantineFrom;
	private DateField quarantineTo;
	private CheckBox quarantineExtended;
	private CheckBox quarantineReduced;
	private CheckBox quarantineOrderedVerbally;
	private CheckBox quarantineOrderedOfficialDocument;
	private OptionGroup facilityOrHome;
	private ComboBox facilityTypeGroup;
	private ComboBox facilityType;
	private boolean quarantineChangedByFollowUpUntilChange = false;

	public CaseDataForm(String caseUuid, PersonDto person, Disease disease, SymptomsDto symptoms, ViewMode viewMode, boolean isInJurisdiction) {

		super(
			CaseDataDto.class,
			CaseDataDto.I18N_PREFIX,
			false,
			FieldVisibilityCheckers.withDisease(disease)
				.add(new OutbreakFieldVisibilityChecker(viewMode))
				.add(new CountryFieldVisibilityChecker(FacadeProvider.getConfigFacade().getCountryLocale())),
			UiFieldAccessCheckers.withCheckers(
				isInJurisdiction,
				FieldHelper.createPersonalDataFieldAccessChecker(),
				FieldHelper.createSensitiveDataFieldAccessChecker()));

		this.caseUuid = caseUuid;
		this.person = person;
		this.disease = disease;
		this.symptoms = symptoms;
		this.caseFollowUpEnabled = FacadeProvider.getFeatureConfigurationFacade().isFeatureEnabled(FeatureType.CASE_FOLLOWUP);

		addFields();
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void addFields() {

		if (person == null || disease == null) {
			return;
		}

		Label caseDataHeadingLabel = new Label(I18nProperties.getString(Strings.headingCaseData));
		caseDataHeadingLabel.addStyleName(H3);
		getContent().addComponent(caseDataHeadingLabel, CASE_DATA_HEADING_LOC);

		if (caseFollowUpEnabled) {
			Label followUpStatusHeadingLabel = new Label(I18nProperties.getString(Strings.headingFollowUpStatus));
			followUpStatusHeadingLabel.addStyleName(H3);
			getContent().addComponent(followUpStatusHeadingLabel, FOLLOW_UP_STATUS_HEADING_LOC);
		}

		// Add fields
		DateField reportDate = addField(CaseDataDto.REPORT_DATE, DateField.class);
		addFields(
			CaseDataDto.UUID,
			CaseDataDto.REPORTING_USER,
			CaseDataDto.DISTRICT_LEVEL_DATE,
			CaseDataDto.REGION_LEVEL_DATE,
			CaseDataDto.NATIONAL_LEVEL_DATE,
			CaseDataDto.CLASSIFICATION_DATE,
			CaseDataDto.CLASSIFICATION_USER,
			CaseDataDto.CLASSIFICATION_COMMENT,
			CaseDataDto.NOTIFYING_CLINIC,
			CaseDataDto.NOTIFYING_CLINIC_DETAILS,
			CaseDataDto.CLINICIAN_NAME,
			CaseDataDto.CLINICIAN_PHONE,
			CaseDataDto.CLINICIAN_EMAIL);

		TextField epidField = addField(CaseDataDto.EPID_NUMBER, TextField.class);
		epidField.setInvalidCommitted(true);
		epidField.setMaxLength(24);
		style(epidField, ERROR_COLOR_PRIMARY);

		// Button to automatically assign a new epid number
		Button assignNewEpidNumberButton = ButtonHelper.createButton(Captions.actionAssignNewEpidNumber, e -> {
			epidField.setValue(FacadeProvider.getCaseFacade().generateEpidNumber(getValue().toReference()));
		}, ValoTheme.BUTTON_DANGER, FORCE_CAPTION);

		getContent().addComponent(assignNewEpidNumberButton, ASSIGN_NEW_EPID_NUMBER_LOC);
		assignNewEpidNumberButton.setVisible(false);

		Label epidNumberWarningLabel = new Label(I18nProperties.getString(Strings.messageEpidNumberWarning));
		epidNumberWarningLabel.addStyleName(VSPACE_3);
		addField(CaseDataDto.EXTERNAL_ID, TextField.class);

		addField(CaseDataDto.INVESTIGATION_STATUS, OptionGroup.class);
		addField(CaseDataDto.OUTCOME, OptionGroup.class);
		addField(CaseDataDto.SEQUELAE, OptionGroup.class);
		if (isConfiguredServer("de")) {
			addField(CaseDataDto.REPORTING_TYPE);
		}
		addFields(CaseDataDto.INVESTIGATED_DATE, CaseDataDto.OUTCOME_DATE, CaseDataDto.SEQUELAE_DETAILS);

		ComboBox diseaseField = addDiseaseField(CaseDataDto.DISEASE, false);
		addField(CaseDataDto.DISEASE_DETAILS, TextField.class);
		addField(CaseDataDto.PLAGUE_TYPE, OptionGroup.class);
		addField(CaseDataDto.DENGUE_FEVER_TYPE, OptionGroup.class);
		addField(CaseDataDto.RABIES_TYPE, OptionGroup.class);

		addField(CaseDataDto.CASE_ORIGIN, TextField.class);

		quarantine = addField(CaseDataDto.QUARANTINE);
		quarantine.addValueChangeListener(e -> onValueChange());
		quarantineFrom = addField(CaseDataDto.QUARANTINE_FROM, DateField.class);
		quarantineTo = addDateField(CaseDataDto.QUARANTINE_TO, DateField.class, -1);

		if (isConfiguredServer("de")) {
			final ComboBox cbCaseClassification = addField(CaseDataDto.CASE_CLASSIFICATION, ComboBox.class);
			cbCaseClassification.addValidator(
				new GermanCaseClassificationValidator(caseUuid, I18nProperties.getValidationError(Validations.caseClassificationInvalid)));

			addField(CaseDataDto.CLINICAL_CONFIRMATION, ComboBox.class);
			addField(CaseDataDto.EPIDEMIOLOGICAL_CONFIRMATION, ComboBox.class);
			addField(CaseDataDto.LABORATORY_DIAGNOSTIC_CONFIRMATION, ComboBox.class);
		} else {
			final OptionGroup caseClassificationGroup = addField(CaseDataDto.CASE_CLASSIFICATION, OptionGroup.class);
			caseClassificationGroup.removeItem(CaseClassification.CONFIRMED_NO_SYMPTOMS);
			caseClassificationGroup.removeItem(CaseClassification.CONFIRMED_UNKNOWN_SYMPTOMS);
		}

		quarantineOrderedVerbally = addField(CaseDataDto.QUARANTINE_ORDERED_VERBALLY, CheckBox.class);
		CssStyles.style(quarantineOrderedVerbally, CssStyles.FORCE_CAPTION);
		addField(CaseDataDto.QUARANTINE_ORDERED_VERBALLY_DATE, DateField.class);
		quarantineOrderedOfficialDocument = addField(CaseDataDto.QUARANTINE_ORDERED_OFFICIAL_DOCUMENT, CheckBox.class);
		CssStyles.style(quarantineOrderedOfficialDocument, CssStyles.FORCE_CAPTION);
		addField(CaseDataDto.QUARANTINE_ORDERED_OFFICIAL_DOCUMENT_DATE, DateField.class);

		CheckBox quarantineOfficialOrderSent = addField(CaseDataDto.QUARANTINE_OFFICIAL_ORDER_SENT, CheckBox.class);
		CssStyles.style(quarantineOfficialOrderSent, FORCE_CAPTION);
		addField(CaseDataDto.QUARANTINE_OFFICIAL_ORDER_SENT_DATE, DateField.class);
		FieldHelper.setVisibleWhen(
			getFieldGroup(),
			CaseDataDto.QUARANTINE_OFFICIAL_ORDER_SENT,
			CaseDataDto.QUARANTINE_ORDERED_OFFICIAL_DOCUMENT,
			Collections.singletonList(Boolean.TRUE),
			true);

		quarantineExtended = addField(CaseDataDto.QUARANTINE_EXTENDED, CheckBox.class);
		quarantineExtended.setEnabled(false);
		quarantineExtended.setVisible(false);
		CssStyles.style(quarantineExtended, CssStyles.FORCE_CAPTION);

		quarantineReduced = addField(CaseDataDto.QUARANTINE_REDUCED, CheckBox.class);
		quarantineReduced.setEnabled(false);
		quarantineReduced.setVisible(false);
		CssStyles.style(quarantineReduced, CssStyles.FORCE_CAPTION);

		TextField quarantineHelpNeeded = addField(CaseDataDto.QUARANTINE_HELP_NEEDED, TextField.class);
		quarantineHelpNeeded.setInputPrompt(I18nProperties.getString(Strings.pleaseSpecify));
		TextField quarantineTypeDetails = addField(CaseDataDto.QUARANTINE_TYPE_DETAILS, TextField.class);
		quarantineTypeDetails.setInputPrompt(I18nProperties.getString(Strings.pleaseSpecify));

		addField(CaseDataDto.QUARANTINE_HOME_POSSIBLE, OptionGroup.class);
		addField(CaseDataDto.QUARANTINE_HOME_POSSIBLE_COMMENT, TextField.class);
		addField(CaseDataDto.QUARANTINE_HOME_SUPPLY_ENSURED, OptionGroup.class);
		addField(CaseDataDto.QUARANTINE_HOME_SUPPLY_ENSURED_COMMENT, TextField.class);

		FieldHelper.setVisibleWhen(
			getFieldGroup(),
			Arrays
				.asList(CaseDataDto.QUARANTINE_FROM, CaseDataDto.QUARANTINE_TO, CaseDataDto.QUARANTINE_HELP_NEEDED),
			CaseDataDto.QUARANTINE,
			Arrays.asList(QuarantineType.HOME, QuarantineType.INSTITUTIONELL),
			true);
		if (isConfiguredServer(CountryHelper.COUNTRY_CODE_GERMANY) || isConfiguredServer(CountryHelper.COUNTRY_CODE_SWITZERLAND)) {
			FieldHelper.setVisibleWhen(
				getFieldGroup(),
				Arrays.asList(CaseDataDto.QUARANTINE_ORDERED_VERBALLY, CaseDataDto.QUARANTINE_ORDERED_OFFICIAL_DOCUMENT),
				CaseDataDto.QUARANTINE,
				Arrays.asList(QuarantineType.HOME, QuarantineType.INSTITUTIONELL),
				true);
		}
		FieldHelper.setVisibleWhen(
			getFieldGroup(),
			CaseDataDto.QUARANTINE_HOME_POSSIBLE_COMMENT,
			CaseDataDto.QUARANTINE_HOME_POSSIBLE,
			Arrays.asList(YesNoUnknown.NO),
			true);
		FieldHelper.setVisibleWhen(
			getFieldGroup(),
			CaseDataDto.QUARANTINE_HOME_SUPPLY_ENSURED,
			CaseDataDto.QUARANTINE_HOME_POSSIBLE,
			Arrays.asList(YesNoUnknown.YES),
			true);
		FieldHelper.setVisibleWhen(
			getFieldGroup(),
			CaseDataDto.QUARANTINE_HOME_SUPPLY_ENSURED_COMMENT,
			CaseDataDto.QUARANTINE_HOME_SUPPLY_ENSURED,
			Arrays.asList(YesNoUnknown.NO),
			true);
		FieldHelper
			.setVisibleWhen(getFieldGroup(), CaseDataDto.QUARANTINE_TYPE_DETAILS, CaseDataDto.QUARANTINE, Arrays.asList(QuarantineType.OTHER), true);
		FieldHelper.setVisibleWhen(
			getFieldGroup(),
			CaseDataDto.QUARANTINE_ORDERED_VERBALLY_DATE,
			CaseDataDto.QUARANTINE_ORDERED_VERBALLY,
			Arrays.asList(Boolean.TRUE),
			true);
		FieldHelper.setVisibleWhen(
			getFieldGroup(),
			CaseDataDto.QUARANTINE_ORDERED_OFFICIAL_DOCUMENT_DATE,
			CaseDataDto.QUARANTINE_ORDERED_OFFICIAL_DOCUMENT,
			Arrays.asList(Boolean.TRUE),
			true);
		FieldHelper.setVisibleWhen(
			getFieldGroup(),
			CaseDataDto.QUARANTINE_OFFICIAL_ORDER_SENT_DATE,
			CaseDataDto.QUARANTINE_OFFICIAL_ORDER_SENT,
			Collections.singletonList(Boolean.TRUE),
			true);

		ComboBox surveillanceOfficerField = addField(CaseDataDto.SURVEILLANCE_OFFICER, ComboBox.class);
		surveillanceOfficerField.setNullSelectionAllowed(true);
		ComboBox region = addInfrastructureField(CaseDataDto.REGION);
		ComboBox district = addInfrastructureField(CaseDataDto.DISTRICT);
		ComboBox community = addInfrastructureField(CaseDataDto.COMMUNITY);
		community.setNullSelectionAllowed(true);
		community.addStyleName(SOFT_REQUIRED);
		facilityOrHome = new OptionGroup(I18nProperties.getCaption(Captions.casePlaceOfStay), TypeOfPlace.getTypesOfPlaceForCases());
		facilityOrHome.setId("facilityOrHome");
		facilityOrHome.setWidth(100, Unit.PERCENTAGE);
		CssStyles.style(facilityOrHome, ValoTheme.OPTIONGROUP_HORIZONTAL);
		getContent().addComponent(facilityOrHome, FACILITY_OR_HOME_LOC);
		facilityTypeGroup = new ComboBox();
		facilityTypeGroup.setId("typeGroup");
		facilityTypeGroup.setCaption(I18nProperties.getCaption(Captions.Facility_typeGroup));
		facilityTypeGroup.setWidth(100, Unit.PERCENTAGE);
		facilityTypeGroup.addItems(FacilityTypeGroup.getAccomodationGroups());
		facilityTypeGroup.setVisible(false);
		getContent().addComponent(facilityTypeGroup, TYPE_GROUP_LOC);
		facilityType = addField(CaseDataDto.FACILITY_TYPE);
		ComboBox facility = addInfrastructureField(CaseDataDto.HEALTH_FACILITY);
		facility.setImmediate(true);
		TextField facilityDetails = addField(CaseDataDto.HEALTH_FACILITY_DETAILS, TextField.class);
		facilityDetails.setVisible(false);

		region.addValueChangeListener(e -> {
			RegionReferenceDto regionDto = (RegionReferenceDto) e.getProperty().getValue();
			FieldHelper
				.updateItems(district, regionDto != null ? FacadeProvider.getDistrictFacade().getAllActiveByRegion(regionDto.getUuid()) : null);
		});
		district.addValueChangeListener(e -> {
			DistrictReferenceDto districtDto = (DistrictReferenceDto) e.getProperty().getValue();
			FieldHelper.updateItems(
				community,
				districtDto != null ? FacadeProvider.getCommunityFacade().getAllActiveByDistrict(districtDto.getUuid()) : null);
			updateFacility(
				(DistrictReferenceDto) district.getValue(),
				(CommunityReferenceDto) community.getValue(),
				(FacilityType) facilityType.getValue(),
				facility);
		});
		community.addValueChangeListener(e -> {
			updateFacility(
				(DistrictReferenceDto) district.getValue(),
				(CommunityReferenceDto) community.getValue(),
				(FacilityType) facilityType.getValue(),
				facility);
		});
		facilityOrHome.addValueChangeListener(e -> {
			FieldHelper.removeItems(facility);
			if (TypeOfPlace.FACILITY.equals(facilityOrHome.getValue())) {

				// default values
				if (facilityTypeGroup.getValue() == null && !facilityTypeGroup.isReadOnly()) {
					facilityTypeGroup.setValue(FacilityTypeGroup.MEDICAL_FACILITY);
				}
				if (facilityType.getValue() == null
					&& FacilityTypeGroup.MEDICAL_FACILITY.equals(facilityTypeGroup.getValue())
					&& !facilityType.isReadOnly()) {
					facilityType.setValue(FacilityType.HOSPITAL);
				}
				if (facilityType.getValue() != null) {
					updateFacility(
						(DistrictReferenceDto) district.getValue(),
						(CommunityReferenceDto) community.getValue(),
						(FacilityType) facilityType.getValue(),
						facility);
				}

				if (CaseOrigin.IN_COUNTRY.equals(getField(CaseDataDto.CASE_ORIGIN).getValue())) {
					facility.setRequired(true);
				}
				updateFacilityDetails(facility, facilityDetails);
			} else {

				FacilityReferenceDto noFacilityRef = FacadeProvider.getFacilityFacade().getByUuid(FacilityDto.NONE_FACILITY_UUID).toReference();
				facility.addItem(noFacilityRef);
				facility.setValue(noFacilityRef);
			}
		});
		facilityTypeGroup.addValueChangeListener(e -> {
			FieldHelper.updateEnumData(facilityType, FacilityType.getAccommodationTypes((FacilityTypeGroup) facilityTypeGroup.getValue()));
		});
		facilityType.addValueChangeListener(e -> {
			updateFacility(
				(DistrictReferenceDto) district.getValue(),
				(CommunityReferenceDto) community.getValue(),
				(FacilityType) facilityType.getValue(),
				facility);
		});
		facility.addValueChangeListener(e -> {
			updateFacilityDetails(facility, facilityDetails);
		});
		region.addItems(FacadeProvider.getRegionFacade().getAllActiveAsReference());

		if (!FacadeProvider.getFeatureConfigurationFacade().isFeatureDisabled(FeatureType.NATIONAL_CASE_SHARING)) {
			addField(CaseDataDto.SHARED_TO_COUNTRY, CheckBox.class);
			setReadOnly(!UserProvider.getCurrent().hasUserRight(UserRight.CASE_SHARE), CaseDataDto.SHARED_TO_COUNTRY);
		}

		addInfrastructureField(CaseDataDto.POINT_OF_ENTRY);
		addField(CaseDataDto.POINT_OF_ENTRY_DETAILS, TextField.class);

		TextField tfReportLat = addField(CaseDataDto.REPORT_LAT, TextField.class);
		tfReportLat.setConverter(new StringToAngularLocationConverter());
		TextField tfReportLon = addField(CaseDataDto.REPORT_LON, TextField.class);
		tfReportLon.setConverter(new StringToAngularLocationConverter());
		addField(CaseDataDto.REPORT_LAT_LON_ACCURACY, TextField.class);

		DateField dfFollowUpUntil = null;
		if (caseFollowUpEnabled) {
			addField(CaseDataDto.FOLLOW_UP_STATUS, ComboBox.class);
			addField(CaseDataDto.FOLLOW_UP_COMMENT, TextArea.class).setRows(1);
			dfFollowUpUntil = addDateField(CaseDataDto.FOLLOW_UP_UNTIL, DateField.class, -1);
			dfFollowUpUntil.addValueChangeListener(v -> onFollowUpUntilChanged(v, quarantineTo, quarantineExtended));
			addField(CaseDataDto.OVERWRITE_FOLLOW_UP_UNTIL, CheckBox.class);

			setReadOnly(true, CaseDataDto.FOLLOW_UP_STATUS);

			FieldHelper.setRequiredWhen(
				getFieldGroup(),
				CaseDataDto.FOLLOW_UP_STATUS,
				Arrays.asList(CaseDataDto.FOLLOW_UP_COMMENT),
				Arrays.asList(FollowUpStatus.CANCELED, FollowUpStatus.LOST));
			FieldHelper.setReadOnlyWhen(
				getFieldGroup(),
				Arrays.asList(CaseDataDto.FOLLOW_UP_UNTIL),
				CaseDataDto.OVERWRITE_FOLLOW_UP_UNTIL,
				Arrays.asList(Boolean.FALSE),
				false,
				true);
			FieldHelper.setRequiredWhen(
				getFieldGroup(),
				CaseDataDto.OVERWRITE_FOLLOW_UP_UNTIL,
				Arrays.asList(CaseDataDto.FOLLOW_UP_UNTIL),
				Arrays.asList(Boolean.TRUE));
		}
		final DateField finalFollowUpUntil = dfFollowUpUntil;
		quarantineTo.addValueChangeListener(e -> onQuarantineEndChange(e, quarantineExtended, quarantineReduced, finalFollowUpUntil));
		this.addValueChangeListener(e -> onValueChange());
		Label generalCommentLabel = new Label(I18nProperties.getPrefixCaption(CaseDataDto.I18N_PREFIX, CaseDataDto.ADDITIONAL_DETAILS));
		generalCommentLabel.addStyleName(H3);
		getContent().addComponent(generalCommentLabel, GENERAL_COMMENT_LOC);

		TextArea additionalDetails = addField(CaseDataDto.ADDITIONAL_DETAILS, TextArea.class);
		additionalDetails.setRows(3);
		CssStyles.style(additionalDetails, CssStyles.CAPTION_HIDDEN);

		addField(CaseDataDto.PREGNANT, OptionGroup.class);
		addField(CaseDataDto.POSTPARTUM, OptionGroup.class);
		addField(CaseDataDto.TRIMESTER, OptionGroup.class);
		FieldHelper.setVisibleWhen(getFieldGroup(), CaseDataDto.TRIMESTER, CaseDataDto.PREGNANT, Arrays.asList(YesNoUnknown.YES), true);

		addFields(
			CaseDataDto.VACCINATION,
			CaseDataDto.VACCINATION_DOSES,
			CaseDataDto.VACCINATION_INFO_SOURCE,
			CaseDataDto.VACCINE,
			CaseDataDto.SMALLPOX_VACCINATION_SCAR,
			CaseDataDto.SMALLPOX_VACCINATION_RECEIVED,
			CaseDataDto.VACCINATION_DATE);

		// Set initial visibilities & accesses

		initializeVisibilitiesAndAllowedVisibilities();
		initializeAccessAndAllowedAccesses();

		// Set requirements that don't need visibility changes and read only status

		setRequired(
			true,
			CaseDataDto.REPORT_DATE,
			CaseDataDto.CASE_CLASSIFICATION,
			CaseDataDto.INVESTIGATION_STATUS,
			CaseDataDto.OUTCOME,
			CaseDataDto.DISEASE,
			CaseDataDto.REGION,
			CaseDataDto.DISTRICT);
		setSoftRequired(true, CaseDataDto.INVESTIGATED_DATE, CaseDataDto.OUTCOME_DATE, CaseDataDto.PLAGUE_TYPE, CaseDataDto.SURVEILLANCE_OFFICER);
		if (isEditableAllowed(CaseDataDto.INVESTIGATED_DATE)) {
			FieldHelper.setReadOnlyWhen(
				getFieldGroup(),
				CaseDataDto.INVESTIGATED_DATE,
				CaseDataDto.INVESTIGATION_STATUS,
				Arrays.asList(InvestigationStatus.PENDING),
				false,
				true);
		}
		setReadOnly(
			true,
			CaseDataDto.UUID,
			CaseDataDto.REPORTING_USER,
			CaseDataDto.CLASSIFICATION_USER,
			CaseDataDto.CLASSIFICATION_DATE,
			CaseDataDto.POINT_OF_ENTRY,
			CaseDataDto.POINT_OF_ENTRY_DETAILS,
			CaseDataDto.CASE_ORIGIN);

		setReadOnly(!UserProvider.getCurrent().hasUserRight(UserRight.CASE_CHANGE_DISEASE), CaseDataDto.DISEASE);
		setReadOnly(
			!UserProvider.getCurrent().hasUserRight(UserRight.CASE_INVESTIGATE),
			CaseDataDto.INVESTIGATION_STATUS,
			CaseDataDto.INVESTIGATED_DATE);
		setReadOnly(
			!UserProvider.getCurrent().hasUserRight(UserRight.CASE_CLASSIFY),
			CaseDataDto.CASE_CLASSIFICATION,
			CaseDataDto.OUTCOME,
			CaseDataDto.OUTCOME_DATE);
		setReadOnly(
			!UserProvider.getCurrent().hasUserRight(UserRight.CASE_TRANSFER),
			CaseDataDto.REGION,
			CaseDataDto.DISTRICT,
			CaseDataDto.COMMUNITY,
			FACILITY_OR_HOME_LOC,
			TYPE_GROUP_LOC,
			CaseDataDto.FACILITY_TYPE,
			CaseDataDto.HEALTH_FACILITY,
			CaseDataDto.HEALTH_FACILITY_DETAILS);

		if (!isEditableAllowed(CaseDataDto.COMMUNITY)) {
			setEnabled(false, CaseDataDto.REGION, CaseDataDto.DISTRICT);
		}

		// Set conditional visibilities - ALWAYS call isVisibleAllowed before
		// dynamically setting the visibility

		if (isVisibleAllowed(CaseDataDto.PREGNANT)) {
			setVisible(person.getSex() == Sex.FEMALE, CaseDataDto.PREGNANT, CaseDataDto.POSTPARTUM);
		}
		if (isVisibleAllowed(CaseDataDto.VACCINATION_DOSES)) {
			FieldHelper
				.setVisibleWhen(getFieldGroup(), CaseDataDto.VACCINATION_DOSES, CaseDataDto.VACCINATION, Arrays.asList(Vaccination.VACCINATED), true);
		}
		if (isVisibleAllowed(CaseDataDto.VACCINATION_INFO_SOURCE)) {
			FieldHelper.setVisibleWhen(
				getFieldGroup(),
				CaseDataDto.VACCINATION_INFO_SOURCE,
				CaseDataDto.VACCINATION,
				Arrays.asList(Vaccination.VACCINATED),
				true);
		}
		if (isVisibleAllowed(CaseDataDto.DISEASE_DETAILS)) {
			FieldHelper
				.setVisibleWhen(getFieldGroup(), Arrays.asList(CaseDataDto.DISEASE_DETAILS), CaseDataDto.DISEASE, Arrays.asList(Disease.OTHER), true);
			FieldHelper
				.setRequiredWhen(getFieldGroup(), CaseDataDto.DISEASE, Arrays.asList(CaseDataDto.DISEASE_DETAILS), Arrays.asList(Disease.OTHER));
		}
		if (isVisibleAllowed(CaseDataDto.PLAGUE_TYPE)) {
			FieldHelper
				.setVisibleWhen(getFieldGroup(), Arrays.asList(CaseDataDto.PLAGUE_TYPE), CaseDataDto.DISEASE, Arrays.asList(Disease.PLAGUE), true);
		}
		if (isVisibleAllowed(CaseDataDto.DENGUE_FEVER_TYPE)) {
			FieldHelper.setVisibleWhen(
				getFieldGroup(),
				Arrays.asList(CaseDataDto.DENGUE_FEVER_TYPE),
				CaseDataDto.DISEASE,
				Arrays.asList(Disease.DENGUE),
				true);
		}
		if (isVisibleAllowed(CaseDataDto.RABIES_TYPE)) {
			FieldHelper
				.setVisibleWhen(getFieldGroup(), Arrays.asList(CaseDataDto.RABIES_TYPE), CaseDataDto.DISEASE, Arrays.asList(Disease.RABIES), true);
		}
		if (isVisibleAllowed(CaseDataDto.SMALLPOX_VACCINATION_SCAR)) {
			FieldHelper.setVisibleWhen(
				getFieldGroup(),
				CaseDataDto.SMALLPOX_VACCINATION_SCAR,
				CaseDataDto.SMALLPOX_VACCINATION_RECEIVED,
				Arrays.asList(YesNoUnknown.YES),
				true);
		}
		if (isVisibleAllowed(CaseDataDto.VACCINATION_DATE)) {
			FieldHelper.setVisibleWhen(
				getFieldGroup(),
				CaseDataDto.VACCINATION_DATE,
				CaseDataDto.SMALLPOX_VACCINATION_RECEIVED,
				Arrays.asList(YesNoUnknown.YES),
				true);
			FieldHelper
				.setVisibleWhen(getFieldGroup(), CaseDataDto.VACCINATION_DATE, CaseDataDto.VACCINATION, Arrays.asList(Vaccination.VACCINATED), true);
		}
		if (isVisibleAllowed(CaseDataDto.VACCINE)) {
			FieldHelper.setVisibleWhen(getFieldGroup(), CaseDataDto.VACCINE, CaseDataDto.VACCINATION, Arrays.asList(Vaccination.VACCINATED), true);
		}
		if (isVisibleAllowed(CaseDataDto.OUTCOME_DATE)) {
			FieldHelper.setVisibleWhen(
				getFieldGroup(),
				CaseDataDto.OUTCOME_DATE,
				CaseDataDto.OUTCOME,
				Arrays.asList(CaseOutcome.DECEASED, CaseOutcome.RECOVERED),
				true);
		}
		if (isVisibleAllowed(CaseDataDto.SEQUELAE)) {
			FieldHelper.setVisibleWhen(
				getFieldGroup(),
				CaseDataDto.SEQUELAE,
				CaseDataDto.OUTCOME,
				Arrays.asList(CaseOutcome.RECOVERED, CaseOutcome.UNKNOWN),
				true);
		}
		if (isVisibleAllowed(CaseDataDto.SEQUELAE_DETAILS)) {
			FieldHelper.setVisibleWhen(getFieldGroup(), CaseDataDto.SEQUELAE_DETAILS, CaseDataDto.SEQUELAE, Arrays.asList(YesNoUnknown.YES), true);
		}
		if (isVisibleAllowed(CaseDataDto.NOTIFYING_CLINIC_DETAILS)) {
			FieldHelper.setVisibleWhen(
				getFieldGroup(),
				CaseDataDto.NOTIFYING_CLINIC_DETAILS,
				CaseDataDto.NOTIFYING_CLINIC,
				Arrays.asList(HospitalWardType.OTHER),
				true);
		}
		FieldHelper.setVisibleWhen(
			facilityOrHome,
			Arrays.asList(facilityTypeGroup, facilityType, facility),
			Collections.singletonList(TypeOfPlace.FACILITY),
			false);
		FieldHelper.setRequiredWhen(
			facilityOrHome,
			Arrays.asList(facilityTypeGroup, facilityType, facility),
			Collections.singletonList(TypeOfPlace.FACILITY),
			false,
			null);

		/// CLINICIAN FIELDS
		if (UserProvider.getCurrent().hasUserRight(UserRight.CASE_MANAGEMENT_ACCESS)) {
			if (isVisibleAllowed(CaseDataDto.CLINICIAN_NAME)) {
				FieldHelper.setVisibleWhen(
					getFieldGroup(),
					CaseDataDto.CLINICIAN_NAME,
					CaseDataDto.FACILITY_TYPE,
					Arrays.asList(FacilityType.HOSPITAL, FacilityType.OTHER_MEDICAL_FACILITY),
					true);
			}
			if (isVisibleAllowed(CaseDataDto.CLINICIAN_PHONE)) {
				FieldHelper.setVisibleWhen(
					getFieldGroup(),
					CaseDataDto.CLINICIAN_PHONE,
					CaseDataDto.FACILITY_TYPE,
					Arrays.asList(FacilityType.HOSPITAL, FacilityType.OTHER_MEDICAL_FACILITY),
					true);
			}
			if (isVisibleAllowed(CaseDataDto.CLINICIAN_EMAIL)) {
				FieldHelper.setVisibleWhen(
					getFieldGroup(),
					CaseDataDto.CLINICIAN_EMAIL,
					CaseDataDto.FACILITY_TYPE,
					Arrays.asList(FacilityType.HOSPITAL, FacilityType.OTHER_MEDICAL_FACILITY),
					true);
			}
		} else {
			setVisible(false, CaseDataDto.CLINICIAN_NAME, CaseDataDto.CLINICIAN_PHONE, CaseDataDto.CLINICIAN_EMAIL);
		}

		// Other initializations

		if (disease == Disease.MONKEYPOX) {
			Image smallpoxVaccinationScarImg = new Image(null, new ThemeResource("img/smallpox-vaccination-scar.jpg"));
			style(smallpoxVaccinationScarImg, VSPACE_3);
			getContent().addComponent(smallpoxVaccinationScarImg, SMALLPOX_VACCINATION_SCAR_IMG);

			// Set up initial image visibility
			getContent().getComponent(SMALLPOX_VACCINATION_SCAR_IMG)
				.setVisible(getFieldGroup().getField(CaseDataDto.SMALLPOX_VACCINATION_RECEIVED).getValue() == YesNoUnknown.YES);

			// Set up image visibility listener
			getFieldGroup().getField(CaseDataDto.SMALLPOX_VACCINATION_RECEIVED).addValueChangeListener(e -> {
				getContent().getComponent(SMALLPOX_VACCINATION_SCAR_IMG).setVisible(e.getProperty().getValue() == YesNoUnknown.YES);
			});
		}

		List<String> medicalInformationFields =
			Arrays.asList(CaseDataDto.PREGNANT, CaseDataDto.VACCINATION, CaseDataDto.SMALLPOX_VACCINATION_RECEIVED);

		for (String medicalInformationField : medicalInformationFields) {
			if (getFieldGroup().getField(medicalInformationField).isVisible()) {
				Label medicalInformationCaptionLabel = new Label(I18nProperties.getString(Strings.headingMedicalInformation));
				medicalInformationCaptionLabel.addStyleName(H3);
				getContent().addComponent(medicalInformationCaptionLabel, MEDICAL_INFORMATION_LOC);
				break;
			}
		}

		Label paperFormDatesLabel = new Label(I18nProperties.getString(Strings.headingPaperFormDates));
		paperFormDatesLabel.addStyleName(H3);
		getContent().addComponent(paperFormDatesLabel, PAPER_FORM_DATES_LOC);

		// Automatic case classification rules button - invisible for other diseases
		DiseaseClassificationCriteriaDto diseaseClassificationCriteria = FacadeProvider.getCaseClassificationFacade().getByDisease(disease);
		if (disease != Disease.OTHER && diseaseClassificationCriteria != null) {
			Button classificationRulesButton = ButtonHelper.createIconButton(Captions.info, VaadinIcons.INFO_CIRCLE, e -> {
				ControllerProvider.getCaseController().openClassificationRulesPopup(diseaseClassificationCriteria);
			}, ValoTheme.BUTTON_PRIMARY, FORCE_CAPTION);

			getContent().addComponent(classificationRulesButton, CLASSIFICATION_RULES_LOC);
		}

		addValueChangeListener(e -> {
			diseaseField.addValueChangeListener(new DiseaseChangeListener(diseaseField, getValue().getDisease()));
			surveillanceOfficerField
				.addItems(FacadeProvider.getUserFacade().getUserRefsByDistrict(getValue().getDistrict(), false, UserRole.SURVEILLANCE_OFFICER));

			// Replace classification user if case has been automatically classified
			if (getValue().getClassificationDate() != null && getValue().getClassificationUser() == null) {
				getField(CaseDataDto.CLASSIFICATION_USER).setVisible(false);
				Label classifiedBySystemLabel = new Label(I18nProperties.getCaption(Captions.system));
				classifiedBySystemLabel.setCaption(I18nProperties.getPrefixCaption(CaseDataDto.I18N_PREFIX, CaseDataDto.CLASSIFIED_BY));
				getContent().addComponent(classifiedBySystemLabel, CLASSIFIED_BY_SYSTEM_LOC);
			}

			updateFollowUpStatusComponents();

			setEpidNumberError(epidField, assignNewEpidNumberButton, epidNumberWarningLabel, getValue().getEpidNumber());

			epidField.addValueChangeListener(f -> {
				setEpidNumberError(epidField, assignNewEpidNumberButton, epidNumberWarningLabel, (String) f.getProperty().getValue());
			});

			if (getValue().getHealthFacility() != null) {
				boolean facilityOrHomeReadOnly = facilityOrHome.isReadOnly();
				boolean facilityTypeGroupReadOnly = facilityTypeGroup.isReadOnly();
				facilityOrHome.setReadOnly(false);
				facilityTypeGroup.setReadOnly(false);
				boolean noneHealthFacility = getValue().getHealthFacility().getUuid().equals(FacilityDto.NONE_FACILITY_UUID);

				FacilityType caseFacilityType = getValue().getFacilityType();
				if (noneHealthFacility || caseFacilityType == null) {
					facilityOrHome.setValue(TypeOfPlace.HOME);
				} else {
					facilityOrHome.setValue(TypeOfPlace.FACILITY);
					facilityTypeGroup.setValue(caseFacilityType.getFacilityTypeGroup());
					if (!facilityType.isReadOnly()) {
						facilityType.setValue(caseFacilityType);
					}
				}

				facilityOrHome.setReadOnly(facilityOrHomeReadOnly);
				facilityTypeGroup.setReadOnly(facilityTypeGroupReadOnly);
			} else {
				facilityOrHome.setVisible(false);
			}

			// Set health facility/point of entry visibility based on case origin
			if (getValue().getCaseOrigin() == CaseOrigin.POINT_OF_ENTRY) {
				setVisible(true, CaseDataDto.POINT_OF_ENTRY);
				if (getValue().getPointOfEntry() != null) {
					setVisible(getValue().getPointOfEntry().isOtherPointOfEntry(), CaseDataDto.POINT_OF_ENTRY_DETAILS);
				}

				if (getValue().getHealthFacility() == null) {
					setVisible(
						false,
						CaseDataDto.COMMUNITY,
						FACILITY_OR_HOME_LOC,
						TYPE_GROUP_LOC,
						CaseDataDto.FACILITY_TYPE,
						CaseDataDto.HEALTH_FACILITY,
						CaseDataDto.HEALTH_FACILITY_DETAILS);
					setReadOnly(true, CaseDataDto.REGION, CaseDataDto.DISTRICT, CaseDataDto.COMMUNITY);
				}
			} else {
				facilityOrHome.setRequired(true);
				setVisible(false, CaseDataDto.POINT_OF_ENTRY, CaseDataDto.POINT_OF_ENTRY_DETAILS);
			}

			// take over the value that has been set based on access rights
			facilityTypeGroup.setReadOnly(facilityType.isReadOnly());
			facilityOrHome.setReadOnly(facilityType.isReadOnly());

			// Hide case origin from port health users
			if (UserRole.isPortHealthUser(UserProvider.getCurrent().getUserRoles())) {
				setVisible(false, CaseDataDto.CASE_ORIGIN);
			}

			if (isConfiguredServer("de")) {
				setVisible(false, CaseDataDto.EPID_NUMBER);
			} else {
				setVisible(false, CaseDataDto.EXTERNAL_ID);
			}

			if (caseFollowUpEnabled) {
				// Add follow-up until validator
				Date minimumFollowUpUntilDate = DateHelper.addDays(
					CaseLogic.getStartDate(symptoms.getOnsetDate(), reportDate.getValue()),
					FacadeProvider.getDiseaseConfigurationFacade().getFollowUpDuration((Disease) diseaseField.getValue()));
				finalFollowUpUntil.addValidator(
					new DateRangeValidator(
						I18nProperties.getValidationError(Validations.contactFollowUpUntilDate),
						minimumFollowUpUntilDate,
						null,
						Resolution.DAY));
			}

			// Overwrite visibility for quarantine fields
			if (!isConfiguredServer(CountryHelper.COUNTRY_CODE_GERMANY) && !isConfiguredServer(CountryHelper.COUNTRY_CODE_SWITZERLAND)) {
				setVisible(
					false,
					CaseDataDto.QUARANTINE_ORDERED_OFFICIAL_DOCUMENT,
					CaseDataDto.QUARANTINE_ORDERED_OFFICIAL_DOCUMENT_DATE,
					CaseDataDto.QUARANTINE_ORDERED_VERBALLY,
					CaseDataDto.QUARANTINE_ORDERED_VERBALLY_DATE,
					CaseDataDto.QUARANTINE_OFFICIAL_ORDER_SENT,
					CaseDataDto.QUARANTINE_OFFICIAL_ORDER_SENT_DATE);
			}
		});
	}

	private void onFollowUpUntilChanged(Property.ValueChangeEvent valueChangeEvent, DateField quarantineTo, CheckBox quarantineExtendedCheckBox) {
		Property<Date> followUpUntilField = valueChangeEvent.getProperty();
		Date followUpUntil = followUpUntilField.getValue();
		if (quarantineTo.getValue() != null && (followUpUntil == null || followUpUntil.compareTo(quarantineTo.getValue()) != 0)) {
			VaadinUiUtil.showConfirmationPopup(
				I18nProperties.getString(Strings.headingExtendQuarantine),
				new Label(I18nProperties.getString(Strings.confirmationAlsoExtendQuarantine)),
				I18nProperties.getString(Strings.yes),
				I18nProperties.getString(Strings.no),
				640,
				confirmed -> {
					if (confirmed) {
						quarantineChangedByFollowUpUntilChange = true;
						quarantineTo.setValue(followUpUntil);
						if (followUpUntil.after(getInternalValue().getFollowUpUntil())) {
							quarantineExtendedCheckBox.setValue(true);
						}
					}
				});
		}
	}

	private void onQuarantineEndChange(Property.ValueChangeEvent valueChangeEvent, CheckBox quarantineExtendedCheckBox, CheckBox quarantineReducedCheckBox, DateField followUpUntilField) {
		if (quarantineChangedByFollowUpUntilChange) {
			quarantineChangedByFollowUpUntilChange = false;
		} else {
			Property<Date> quarantineEndField = valueChangeEvent.getProperty();
			Date newQuarantineEnd = quarantineEndField.getValue();
			CaseDataDto originalCase = getInternalValue();
			Date oldQuarantineEnd = originalCase.getQuarantineTo();
			if (newQuarantineEnd != null) {
				if (oldQuarantineEnd != null) {
					if (newQuarantineEnd.after(oldQuarantineEnd)) {
						confirmQuarantineEndExtended(quarantineExtendedCheckBox, quarantineReducedCheckBox, quarantineEndField, originalCase, oldQuarantineEnd, followUpUntilField);
					} else if (newQuarantineEnd.before(oldQuarantineEnd)) {
						confirmQuarantineEndReduced(quarantineExtendedCheckBox, quarantineReducedCheckBox, quarantineEndField, oldQuarantineEnd);
					}
				}
			} else if (!originalCase.isQuarantineExtended() && !originalCase.isQuarantineReduced()) {
				setVisible(false, quarantineExtendedCheckBox.getId(), quarantineReducedCheckBox.getId());
				quarantineExtendedCheckBox.setValue(false);
				quarantineReducedCheckBox.setValue(false);
			}
		}
	}

	private void confirmQuarantineEndExtended(
		CheckBox quarantineExtendedCheckbox,
		CheckBox quarantineReducedCheckbox,
		Property<Date> quarantineEndField,
		CaseDataDto originalCase,
		Date oldQuarantineEnd,
		DateField followUpUntil) {
		VaadinUiUtil.showConfirmationPopup(
			I18nProperties.getString(Strings.headingExtendQuarantine),
			new Label(I18nProperties.getString(Strings.confirmationExtendQuarantine)),
			I18nProperties.getString(Strings.yes),
			I18nProperties.getString(Strings.no),
			640,
			confirmed -> {
				if (confirmed) {
					quarantineExtendedCheckbox.setValue(true);
					quarantineReducedCheckbox.setValue(false);
					setVisible(true, CaseDataDto.QUARANTINE_EXTENDED);
					setVisible(false, CaseDataDto.QUARANTINE_REDUCED);
					if (caseFollowUpEnabled && originalCase.getFollowUpUntil() != null) {
						confirmExtendFollowUpPeriod(originalCase, quarantineEndField.getValue(), followUpUntil);
					}
				} else {
					quarantineEndField.setValue(oldQuarantineEnd);
				}
			});
	}

	private void confirmExtendFollowUpPeriod(CaseDataDto originalCase, Date quarantineEnd, DateField followUpUntil) {
		if (quarantineEnd.after(originalCase.getFollowUpUntil())) {
			VaadinUiUtil.showConfirmationPopup(
				I18nProperties.getString(Strings.headingExtendFollowUp),
				new Label(I18nProperties.getString(Strings.confirmationExtendFollowUp)),
				I18nProperties.getString(Strings.yes),
				I18nProperties.getString(Strings.no),
				640,
				confirmed -> {
					if (confirmed) {
						if (followUpUntil.isReadOnly()) {
							followUpUntil.setReadOnly(false);
							followUpUntil.setValue(quarantineEnd);
							followUpUntil.setReadOnly(true);
						} else {
							followUpUntil.setValue(quarantineEnd);
						}
					}
				});
		}
	}

	private void confirmQuarantineEndReduced(
		CheckBox quarantineExtendedCheckbox,
		CheckBox quarantineReducedCheckbox,
		Property<Date> quarantineEndField,
		Date oldQuarantineEnd) {
		VaadinUiUtil.showConfirmationPopup(
			I18nProperties.getString(Strings.headingReduceQuarantine),
			new Label(I18nProperties.getString(Strings.confirmationReduceQuarantine)),
			I18nProperties.getString(Strings.yes),
			I18nProperties.getString(Strings.no),
			640,
			confirmed -> {
				if (confirmed) {
					quarantineExtendedCheckbox.setValue(false);
					quarantineReducedCheckbox.setValue(true);
					setVisible(false, CaseDataDto.QUARANTINE_EXTENDED);
					setVisible(true, CaseDataDto.QUARANTINE_REDUCED);
				} else {
					quarantineEndField.setValue(oldQuarantineEnd);
				}
			});
	}

	@SuppressWarnings("unchecked")
	private void updateFollowUpStatusComponents() {
		if (!caseFollowUpEnabled) {
			return;
		}

		getContent().removeComponent(CANCEL_OR_RESUME_FOLLOW_UP_BTN_LOC);
		getContent().removeComponent(LOST_FOLLOW_UP_BTN_LOC);

		Field<FollowUpStatus> statusField = (Field<FollowUpStatus>) getField(CaseDataDto.FOLLOW_UP_STATUS);
		boolean followUpVisible = getValue() != null && statusField.isVisible();
		if (followUpVisible && UserProvider.getCurrent().hasUserRight(UserRight.CASE_EDIT)) {
			FollowUpStatus followUpStatus = statusField.getValue();
			if (followUpStatus == FollowUpStatus.FOLLOW_UP) {

				Button cancelButton = ButtonHelper.createButton(Captions.contactCancelFollowUp, event -> {
					Field<FollowUpStatus> statusField1 = (Field<FollowUpStatus>) getField(CaseDataDto.FOLLOW_UP_STATUS);
					statusField1.setReadOnly(false);
					statusField1.setValue(FollowUpStatus.CANCELED);
					statusField1.setReadOnly(true);
					updateFollowUpStatusComponents();
				});
				cancelButton.setWidth(100, Unit.PERCENTAGE);
				getContent().addComponent(cancelButton, CANCEL_OR_RESUME_FOLLOW_UP_BTN_LOC);

				Button lostButton = ButtonHelper.createButton(Captions.contactLostToFollowUp, event -> {
					Field<FollowUpStatus> statusField12 = (Field<FollowUpStatus>) getField(CaseDataDto.FOLLOW_UP_STATUS);
					statusField12.setReadOnly(false);
					statusField12.setValue(FollowUpStatus.LOST);
					statusField12.setReadOnly(true);
					updateFollowUpStatusComponents();
				});
				lostButton.setWidth(100, Unit.PERCENTAGE);
				getContent().addComponent(lostButton, LOST_FOLLOW_UP_BTN_LOC);

			} else if (followUpStatus == FollowUpStatus.CANCELED || followUpStatus == FollowUpStatus.LOST) {

				Button resumeButton = ButtonHelper.createButton(Captions.contactResumeFollowUp, event -> {
					Field<FollowUpStatus> statusField13 = (Field<FollowUpStatus>) getField(CaseDataDto.FOLLOW_UP_STATUS);
					statusField13.setReadOnly(false);
					statusField13.setValue(FollowUpStatus.FOLLOW_UP);
					statusField13.setReadOnly(true);
					updateFollowUpStatusComponents();
				}, CssStyles.FORCE_CAPTION);
				resumeButton.setWidth(100, Unit.PERCENTAGE);

				getContent().addComponent(resumeButton, CANCEL_OR_RESUME_FOLLOW_UP_BTN_LOC);
			}
		}
	}

	@Override
	public void setValue(CaseDataDto newFieldValue) throws ReadOnlyException, ConversionException {
		super.setValue(newFieldValue);

		// HACK: Binding to the fields will call field listeners that may clear/modify the values of other fields.
		// this hopefully resets everything to it's correct value
		discard();
	}

	private void updateFacility(DistrictReferenceDto district, CommunityReferenceDto community, FacilityType facilityType, ComboBox facility) {
		if (facilityType != null) {
			if (community != null) {
				FieldHelper.updateItems(
					facility,
					FacadeProvider.getFacilityFacade().getActiveFacilitiesByCommunityAndType(community, facilityType, true, false));
			} else if (district != null) {
				FieldHelper.updateItems(
					facility,
					FacadeProvider.getFacilityFacade().getActiveFacilitiesByDistrictAndType(district, facilityType, true, false));
			} else {
				FieldHelper.removeItems(facility);

			}
		} else {
			if (TypeOfPlace.HOME.equals(facilityOrHome.getValue())) {
				FacilityReferenceDto noFacilityRef = FacadeProvider.getFacilityFacade().getByUuid(FacilityDto.NONE_FACILITY_UUID).toReference();
				facility.addItem(noFacilityRef);
				facility.setValue(noFacilityRef);
			} else {
				FieldHelper.removeItems(facility);
			}
		}
	}

	private void updateFacilityDetails(ComboBox cbFacility, TextField tfFacilityDetails) {
		if (cbFacility.getValue() != null) {
			boolean otherHealthFacility = ((FacilityReferenceDto) cbFacility.getValue()).getUuid().equals(FacilityDto.OTHER_FACILITY_UUID);
			boolean noneHealthFacility = ((FacilityReferenceDto) cbFacility.getValue()).getUuid().equals(FacilityDto.NONE_FACILITY_UUID);
			boolean visibleAndRequired = otherHealthFacility || noneHealthFacility;

			tfFacilityDetails.setVisible(visibleAndRequired);
			tfFacilityDetails.setRequired(visibleAndRequired);

			if (otherHealthFacility) {
				tfFacilityDetails.setCaption(I18nProperties.getPrefixCaption(CaseDataDto.I18N_PREFIX, CaseDataDto.HEALTH_FACILITY_DETAILS));
			}
			if (noneHealthFacility) {
				tfFacilityDetails.setCaption(I18nProperties.getCaption(Captions.CaseData_noneHealthFacilityDetails));
			}
			if (!visibleAndRequired && !tfFacilityDetails.isReadOnly()) {
				tfFacilityDetails.clear();
			}
		} else {
			tfFacilityDetails.setVisible(false);
			tfFacilityDetails.setRequired(false);
			if (!tfFacilityDetails.isReadOnly()) {
				tfFacilityDetails.clear();
			}
		}
	}

	@Override
	protected String createHtmlLayout() {
		return MAIN_HTML_LAYOUT + (caseFollowUpEnabled ? FOLLOWUP_LAYOUT : "") + PAPER_FORM_DATES_AND_COMMENTS_HTML_LAYOUT;
	}

	private void setEpidNumberError(TextField epidField, Button assignNewEpidNumberButton, Label epidNumberWarningLabel, String fieldValue) {
		if (!isConfiguredServer("de")
			&& FacadeProvider.getCaseFacade().doesEpidNumberExist(fieldValue, getValue().getUuid(), getValue().getDisease())) {
			epidField.setComponentError(new UserError(I18nProperties.getValidationError(Validations.duplicateEpidNumber)));
			assignNewEpidNumberButton.setVisible(true);
			getContent().addComponent(epidNumberWarningLabel, EPID_NUMBER_WARNING_LOC);
		} else {
			epidField.setComponentError(null);
			getContent().removeComponent(epidNumberWarningLabel);
			assignNewEpidNumberButton
				.setVisible(!isConfiguredServer("de") && !CaseLogic.isEpidNumberPrefix(fieldValue) && !CaseLogic.isCompleteEpidNumber(fieldValue));
		}
	}

	private static class DiseaseChangeListener implements ValueChangeListener {

		private static final long serialVersionUID = -5339850320902885768L;

		private final AbstractSelect diseaseField;

		private final Disease currentDisease;
		DiseaseChangeListener(AbstractSelect diseaseField, Disease currentDisease) {
			this.diseaseField = diseaseField;
			this.currentDisease = currentDisease;
		}

		@Override
		public void valueChange(Property.ValueChangeEvent e) {

			if (diseaseField.getValue() != currentDisease) {
				ConfirmationComponent confirmDiseaseChangeComponent = new ConfirmationComponent(false) {

					private static final long serialVersionUID = 1L;

					@Override
					protected void onConfirm() {
						diseaseField.removeValueChangeListener(DiseaseChangeListener.this);
					}

					@Override
					protected void onCancel() {
						diseaseField.setValue(currentDisease);
					}
				};
				confirmDiseaseChangeComponent.getConfirmButton().setCaption(I18nProperties.getString(Strings.confirmationChangeCaseDisease));
				confirmDiseaseChangeComponent.getCancelButton().setCaption(I18nProperties.getCaption(Captions.actionCancel));
				confirmDiseaseChangeComponent.setMargin(true);

				Window popupWindow = VaadinUiUtil.showPopupWindow(confirmDiseaseChangeComponent);
				CloseListener closeListener = ce -> diseaseField.setValue(currentDisease);
				popupWindow.addCloseListener(closeListener);
				confirmDiseaseChangeComponent.addDoneListener(new DoneListener() {

					public void onDone() {
						popupWindow.removeCloseListener(closeListener);
						popupWindow.close();
					}
				});
				popupWindow.setCaption(I18nProperties.getString(Strings.headingChangeCaseDisease));
			}
		}

	}

	private void onValueChange() {
		QuarantineType quarantineType = (QuarantineType) quarantine.getValue();
		if (QuarantineType.HOME.equals(quarantineType) || QuarantineType.INSTITUTIONELL.equals(quarantineType)) {
			CaseDataDto caze = this.getInternalValue();
			if (caze != null) {
				quarantineFrom.setValue(caze.getQuarantineFrom());
				if (caze.getQuarantineTo() == null) {
					if (caseFollowUpEnabled) {
						quarantineTo.setValue(caze.getFollowUpUntil());
					}
				} else {
					quarantineTo.setValue(caze.getQuarantineTo());
				}
				if (caze.isQuarantineExtended()) {
					quarantineExtended.setValue(true);
					setVisible(true, CaseDataDto.QUARANTINE_EXTENDED);
				}
				if (caze.isQuarantineReduced()) {
					quarantineReduced.setValue(true);
					setVisible(true, CaseDataDto.QUARANTINE_REDUCED);
				}
			}
		} else {
			quarantineFrom.clear();
			quarantineTo.clear();
			quarantineExtended.setValue(false);
			quarantineReduced.setValue(false);
			setVisible(false, CaseDataDto.QUARANTINE_REDUCED, CaseDataDto.QUARANTINE_EXTENDED);
		}
	}
}
