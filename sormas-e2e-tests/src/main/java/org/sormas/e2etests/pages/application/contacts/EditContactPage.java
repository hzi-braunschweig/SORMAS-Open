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

package org.sormas.e2etests.pages.application.contacts;

import org.openqa.selenium.By;

public class EditContactPage {
  public static final By UUID_INPUT = By.id("uuid");
  public static final By USER_INFORMATION =
      By.cssSelector(".v-slot.v-slot-h2.v-slot-vspace-top-none.v-slot-primary");
  public static final By REPORT_DATE = By.cssSelector("#reportDateTime input");
  public static final By DISEASE_COMBOBOX =
      By.cssSelector(".v-verticallayout [location='disease'] [role='combobox'] div");
  public static final By CASE_ID_IN_EXTERNAL_SYSTEM_INPUT = By.cssSelector("#caseIdExternalSystem");
  public static final By LAST_CONTACT_DATE = By.cssSelector("#lastContactDate input");
  public static final By CASE_OR_EVENT_INFORMATION_INPUT =
      By.cssSelector("#caseOrEventInformation");
  public static final By RESPONSIBLE_REGION_COMBOBOX =
      By.cssSelector(".v-verticallayout [location='region'] [role='combobox'] div");
  public static final By RESPONSIBLE_DISTRICT_COMBOBOX =
      By.cssSelector(".v-verticallayout [location='district'] [role='combobox'] div");
  public static final By RESPONSIBLE_COMMUNITY_COMBOBOX =
      By.cssSelector(".v-verticallayout [location='community'] [role='combobox'] div");
  public static final By ADDITIONAL_INFORMATION_OF_THE_TYPE_OF_CONTACT_INPUT =
      By.cssSelector("#contactProximityDetails");
  public static final By RELATIONSHIP_WITH_CASE_COMBOBOX =
      By.cssSelector(".v-verticallayout [location='relationToCase'] [role='combobox'] div");
  public static final By DESCRIPTION_OF_HOW_CONTACT_TOOK_PLACE_INPUT =
      By.cssSelector("#description");
  public static final By RETURNING_TRAVELER_OPTIONS = By.cssSelector("#returningTraveler label");
  public static final By TYPE_OF_CONTACT_OPTIONS = By.cssSelector("#contactProximity label");
  public static final By CONTACT_CATEGORY_OPTIONS = By.cssSelector("#contactCategory label");
  public static final By CONTACT_CREATED_POPUP = By.cssSelector(".v-Notification-caption");
  public static final By DELETE_BUTTON = By.id("delete");
  public static final By DELETE_POPUP_YES_BUTTON = By.cssSelector(".popupContent #actionConfirm");
  public static final By CONTACT_CLASSIFICATION_OPTIONS =
      By.cssSelector("#contactClassification label");
  public static final By MULTI_DAY_CONTACT_CHECKBOX = By.cssSelector("#multiDayContact > input");
  public static final By MULTI_DAY_CONTACT_LABEL = By.cssSelector("#multiDayContact > label");
  public static final By FIRST_DAY_CONTACT_DATE = By.cssSelector("#firstContactDate input");
  public static final By EXTERNAL_ID_INPUT = By.id("externalID");
  public static final By EXTERNAL_TOKEN_INPUT = By.id("externalToken");
  public static final By REPORTING_DISTRICT_COMBOBOX =
      By.cssSelector(".v-verticallayout [location='reportingDistrict'] [role='combobox'] div");
  public static final By IDENTIFICATION_SOURCE_INPUT =
      By.cssSelector("#contactIdentificationSourceDetails");
  public static final By CONTACT_IDENTIFICATION_SOURCE_DETAILS_COMBOBOX =
      By.cssSelector("#contactIdentificationSource div");
  public static final By PROHIBITION_TO_WORK_OPTIONS = By.cssSelector("#prohibitionToWork label");
  public static final By HOME_BASED_QUARANTINE_OPTIONS =
      By.cssSelector("#quarantineHomePossible label");
  public static final By QUARANTINE_COMBOBOX = By.cssSelector("#quarantine div");
  public static final By HIGH_PRIORITY_CHECKBOX = By.cssSelector("#highPriority > input");
  public static final By HIGH_PRIORITY_LABEL = By.cssSelector("#highPriority > label");
  public static final By DIABETES_OPTIONS = By.cssSelector("#diabetes label");
  public static final By HIV_OPTIONS = By.cssSelector("#immunodeficiencyIncludingHiv label");
  public static final By LIVER_OPTIONS = By.cssSelector("#chronicLiverDisease label");
  public static final By MALIGNANCY_OPTIONS = By.cssSelector("#malignancyChemotherapy label");
  public static final By PULMONARY_OPTIONS = By.cssSelector("#chronicPulmonaryDisease label");
  public static final By RENAL_OPTIONS = By.cssSelector("#chronicKidneyDisease label");
  public static final By NEUROLOGIC_OPTIONS = By.cssSelector("#chronicNeurologicCondition label");
  public static final By CARDIOVASCULAR_OPTIONS =
      By.cssSelector("#cardiovascularDiseaseIncludingHypertension label");
  public static final By ADDITIONAL_RELEVANT_PRE_CONDITIONS_TEXT = By.id("otherConditions");
  public static final By VACCINATION_STATUS_COMBOBOX =
      By.cssSelector(".v-verticallayout [location='vaccinationInfo'] [role='combobox'] div");
  public static final By IMMUNOSUPPRESSIVE_THERAPY_OPTIONS =
      By.cssSelector("#immunosuppressiveTherapyBasicDisease label");
  public static final By CARE_OVER_60_OPTIONS = By.cssSelector("#careForPeopleOver60 label");
  public static final By CANCEL_FOLLOW_UP_BUTTON = By.id("contactCancelFollowUp");
  public static final By OVERWRITE_FOLLOW_UP_CHECKBOX =
      By.cssSelector("#overwriteFollowUpUntil > input");
  public static final By OVERWRITE_FOLLOW_UP_LABEL =
      By.cssSelector("#overwriteFollowUpUntil > label");
  public static final By FOLLOW_UP_UNTIL_DATE = By.cssSelector("#followUpUntil input");
  public static final By FOLLOW_UP_STATUS_TEXT = By.id("followUpComment");
  public static final By RESPONSIBLE_STATUS_OFFICER_COMBOBOX =
      By.cssSelector(".v-verticallayout [location='contactOfficer'] [role='combobox'] div");
  public static final By GENERAL_COMMENT_TEXT = By.id("additionalDetails");
  public static final By SAVE_EDIT_BUTTON = By.id("commit");
  public static final By CHOOSE_SOURCE_CASE_BUTTON = By.id("contactChooseSourceCase");
  public static final By DISCARD_POPUP_YES_BUTTON = By.id("actionConfirm");
  public static final By SOURCE_CASE_WINDOW_SEARCH_CASE_BUTTON = By.id("caseSearchCase");
  public static final By SOURCE_CASE_WINDOW_CASE_INPUT = By.cssSelector(".v-window-wrap input");
  public static final By SOURCE_CASE_WINDOW_CONFIRM_BUTTON =
      By.cssSelector(".v-window-wrap #commit");
  public static final By SOURCE_CASE_WINDOW_FIRST_RESULT_OPTION =
      By.cssSelector("tr[class*='v-grid-row-has-data']");
  public static final By CHANGE_CASE_BUTTON = By.id("contactChangeCase");
  public static final By CASE_ID_LABEL = By.id("caseIdLabel");
  public static final By CASE_PERSON_LABEL =
      By.xpath(
          "//span[contains(text(), 'Case person')]/ancestor::div[@class='v-caption']/following-sibling::div");
  public static final By CASE_DISEASE_LABEL =
      By.xpath(
          "//span[contains(text(), 'Disease')]/ancestor::div[@class='v-caption']/following-sibling::div");
  public static final By CASE_CLASSIFICATION_LABEL =
      By.xpath(
          "//span[contains(text(), 'Case classification')]/ancestor::div[@class='v-caption']/following-sibling::div");
}
