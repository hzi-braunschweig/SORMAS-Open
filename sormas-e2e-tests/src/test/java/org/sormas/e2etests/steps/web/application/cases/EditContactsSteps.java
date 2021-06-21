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

package org.sormas.e2etests.steps.web.application.cases;

import static org.sormas.e2etests.pages.application.cases.EditContactsPage.*;
import static org.sormas.e2etests.pages.application.cases.EditContactsPage.RELATIONSHIP_WITH_CASE_COMBOBOX;
import static org.sormas.e2etests.pages.application.cases.EditContactsPage.RESPONSIBLE_COMMUNITY_COMBOBOX;
import static org.sormas.e2etests.pages.application.cases.EditContactsPage.RESPONSIBLE_DISTRICT_COMBOBOX;
import static org.sormas.e2etests.pages.application.cases.EditContactsPage.RESPONSIBLE_REGION_COMBOBOX;
import static org.sormas.e2etests.pages.application.contacts.CreateNewContactPage.*;
import static org.sormas.e2etests.pages.application.contacts.EditContactPage.*;
import static org.sormas.e2etests.pages.application.contacts.EditContactPage.ADDITIONAL_INFORMATION_OF_THE_TYPE_OF_CONTACT_INPUT;
import static org.sormas.e2etests.pages.application.contacts.EditContactPage.CONTACT_CATEGORY_OPTIONS;
import static org.sormas.e2etests.pages.application.contacts.EditContactPage.DESCRIPTION_OF_HOW_CONTACT_TOOK_PLACE_INPUT;
import static org.sormas.e2etests.pages.application.contacts.EditContactPage.TYPE_OF_CONTACT_OPTIONS;

import cucumber.api.java8.En;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import javax.inject.Inject;
import javax.inject.Named;
import org.assertj.core.api.SoftAssertions;
import org.openqa.selenium.By;
import org.sormas.e2etests.helpers.WebDriverHelpers;
import org.sormas.e2etests.pojo.web.Contact;
import org.sormas.e2etests.services.ContactService;
import org.sormas.e2etests.state.ApiState;

public class EditContactsSteps implements En {

  private final WebDriverHelpers webDriverHelpers;
  private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy");
  String LAST_CREATED_CASE_CONTACTS_TAB_URL;
  protected Contact contact;
  public static Contact collectedContact;
  protected String contactUUID;

  @Inject
  public EditContactsSteps(
      WebDriverHelpers webDriverHelpers,
      ApiState apiState,
      ContactService contactService,
      SoftAssertions softly,
      @Named("ENVIRONMENT_URL") String environmentUrl) {
    this.webDriverHelpers = webDriverHelpers;

    When(
        "I open the Case Contacts tab of the created case via api",
        () -> {
          LAST_CREATED_CASE_CONTACTS_TAB_URL =
              environmentUrl + "/sormas-ui/#!cases/contacts/" + apiState.getCreatedCase().getUuid();
          webDriverHelpers.accessWebSite(LAST_CREATED_CASE_CONTACTS_TAB_URL);
        });

    Then(
        "I click on new contact button from Case Contacts tab",
        () -> webDriverHelpers.clickOnWebElementBySelector(NEW_CONTACT_BUTTON));

    When(
        "^I create a new contact from Cases Contacts tab$",
        () -> {
          contact = contactService.buildGeneratedContact();
          fillFirstName(contact.getFirstName());
          fillLastName(contact.getLastName());
          fillDateOfBirth(contact.getDateOfBirth());
          selectSex(contact.getSex());
          fillNationalHealthId(contact.getNationalHealthId());
          fillPassportNumber(contact.getPassportNumber());
          fillPrimaryPhoneNumber(contact.getPrimaryPhoneNumber());
          fillPrimaryEmailAddress(contact.getPrimaryEmailAddress());
          selectReturningTraveler(contact.getReturningTraveler());
          fillDateOfReport(contact.getReportDate());
          fillDateOfLastContact(contact.getDateOfLastContact());
          selectResponsibleRegion(contact.getResponsibleRegion());
          selectResponsibleDistrict(contact.getResponsibleDistrict());
          selectResponsibleCommunity(contact.getResponsibleCommunity());
          selectTypeOfContact(contact.getTypeOfContact());
          fillAdditionalInformationOnTheTypeOfContact(
              contact.getAdditionalInformationOnContactType());
          selectContactCategory(contact.getContactCategory().toUpperCase());
          fillRelationshipWithCase(contact.getRelationshipWithCase());
          fillDescriptionOfHowContactTookPlace(contact.getDescriptionOfHowContactTookPlace());
          webDriverHelpers.clickOnWebElementBySelector(SAVE_BUTTON);
          webDriverHelpers.clickOnWebElementBySelector(CONTACT_CREATED_POPUP);
          contactUUID = webDriverHelpers.getValueFromWebElement(UUID_INPUT);
        });

    Then(
        "I verify that created contact from Case Contacts tab is correctly displayed",
        () -> {
          openContactFromResultsByUUID(contactUUID);
          collectedContact = collectContactData();
          softly
              .assertThat(contact.getFirstName())
              .isEqualToIgnoringCase(collectedContact.getFirstName());
          softly
              .assertThat(contact.getLastName())
              .isEqualToIgnoringCase(collectedContact.getLastName());
          softly
              .assertThat(contact.getReturningTraveler())
              .isEqualToIgnoringCase(collectedContact.getReturningTraveler());
          softly.assertThat(contact.getReportDate()).isEqualTo(collectedContact.getReportDate());
          softly
              .assertThat(contact.getDateOfLastContact())
              .isEqualTo(collectedContact.getDateOfLastContact());
          softly
              .assertThat(contact.getResponsibleRegion())
              .isEqualTo(collectedContact.getResponsibleRegion());
          softly
              .assertThat(contact.getResponsibleDistrict())
              .isEqualTo(collectedContact.getResponsibleDistrict());
          softly
              .assertThat(contact.getResponsibleCommunity())
              .isEqualTo(collectedContact.getResponsibleCommunity());
          softly
              .assertThat(
                  contact
                      .getAdditionalInformationOnContactType()
                      .equalsIgnoreCase(collectedContact.getAdditionalInformationOnContactType()))
              .isTrue();
          softly
              .assertThat(contact.getTypeOfContact())
              .isEqualToIgnoringCase(collectedContact.getTypeOfContact());
          softly
              .assertThat(contact.getContactCategory())
              .isEqualToIgnoringCase(collectedContact.getContactCategory());
          softly
              .assertThat(contact.getRelationshipWithCase())
              .isEqualTo(collectedContact.getRelationshipWithCase());
          softly
              .assertThat(contact.getDescriptionOfHowContactTookPlace())
              .isEqualTo(collectedContact.getDescriptionOfHowContactTookPlace());
          softly.assertAll();
        });

    Then(
        "I check the linked contact information is correctly displayed",
        () -> {
          String contactId = webDriverHelpers.getValueFromTableRowUsingTheHeader("Contact ID", 1);
          String contactDisease =
              (webDriverHelpers.getValueFromTableRowUsingTheHeader("Disease", 1).equals("COVID-19"))
                  ? "CORONAVIRUS"
                  : "Not expected string!";
          String contactClassification =
              (webDriverHelpers
                      .getValueFromTableRowUsingTheHeader("Contact classification", 1)
                      .equals("Unconfirmed contact"))
                  ? "UNCONFIRMED"
                  : "Not expected string!";
          String firstName =
              webDriverHelpers.getValueFromTableRowUsingTheHeader(
                  "First name of contact person", 1);
          String lastName =
              webDriverHelpers.getValueFromTableRowUsingTheHeader("Last name of contact person", 1);

          softly
              .assertThat(apiState.getCreatedContact().getUuid().substring(0, 6))
              .isEqualToIgnoringCase(contactId);
          softly
              .assertThat(apiState.getCreatedContact().getDisease())
              .isEqualToIgnoringCase(contactDisease);
          softly
              .assertThat(apiState.getCreatedContact().getContactClassification())
              .isEqualToIgnoringCase(contactClassification);
          softly
              .assertThat(apiState.getCreatedContact().getPerson().getFirstName())
              .isEqualToIgnoringCase(firstName);
          softly
              .assertThat(apiState.getCreatedContact().getPerson().getLastName())
              .isEqualToIgnoringCase(lastName);
          softly.assertAll();
        });
  }

  public void fillFirstName(String firstName) {
    webDriverHelpers.fillInWebElement(FIRST_NAME_OF_CONTACT_PERSON_INPUT, firstName);
  }

  public void fillLastName(String lastName) {
    webDriverHelpers.fillInWebElement(LAST_NAME_OF_CONTACT_PERSON_INPUT, lastName);
  }

  public void fillDateOfBirth(LocalDate localDate) {
    webDriverHelpers.selectFromCombobox(
        DATE_OF_BIRTH_YEAR_COMBOBOX, String.valueOf(localDate.getYear()));
    webDriverHelpers.selectFromCombobox(
        DATE_OF_BIRTH_MONTH_COMBOBOX,
        localDate.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH));
    webDriverHelpers.selectFromCombobox(
        DATE_OF_BIRTH_DAY_COMBOBOX, String.valueOf(localDate.getDayOfMonth()));
  }

  public void selectSex(String sex) {
    webDriverHelpers.selectFromCombobox(SEX_COMBOBOX, sex);
  }

  public void fillNationalHealthId(String nationalHealthId) {
    webDriverHelpers.fillInWebElement(NATIONAL_HEALTH_ID_INPUT, nationalHealthId);
  }

  public void fillPassportNumber(String passportNumber) {
    webDriverHelpers.fillInWebElement(PASSPORT_NUMBER_INPUT, passportNumber);
  }

  public void fillPrimaryPhoneNumber(String primaryPhoneNumber) {
    webDriverHelpers.fillInWebElement(PRIMARY_PHONE_NUMBER_INPUT, primaryPhoneNumber);
  }

  public void fillPrimaryEmailAddress(String primaryPhoneNumber) {
    webDriverHelpers.fillInWebElement(PRIMARY_EMAIL_ADDRESS_INPUT, primaryPhoneNumber);
  }

  public void selectReturningTraveler(String option) {
    webDriverHelpers.clickWebElementByText(TYPE_OF_CONTACT_TRAVELER, option);
  }

  public void fillDateOfReport(LocalDate date) {
    webDriverHelpers.clearAndFillInWebElement(DATE_OF_REPORT_INPUT, formatter.format(date));
  }

  public void fillDateOfLastContact(LocalDate date) {
    webDriverHelpers.fillInWebElement(DATE_OF_LAST_CONTACT_INPUT, formatter.format(date));
  }

  public void selectResponsibleRegion(String selectResponsibleRegion) {
    webDriverHelpers.selectFromCombobox(RESPONSIBLE_REGION_COMBOBOX, selectResponsibleRegion);
  }

  public void selectResponsibleDistrict(String responsibleDistrict) {
    webDriverHelpers.selectFromCombobox(RESPONSIBLE_DISTRICT_COMBOBOX, responsibleDistrict);
  }

  public void selectResponsibleCommunity(String responsibleCommunity) {
    webDriverHelpers.selectFromCombobox(RESPONSIBLE_COMMUNITY_COMBOBOX, responsibleCommunity);
  }

  public void selectTypeOfContact(String typeOfContact) {
    webDriverHelpers.clickWebElementByText(TYPE_OF_CONTACT_OPTIONS, typeOfContact);
  }

  public void selectContactCategory(String category) {
    webDriverHelpers.clickWebElementByText(CONTACT_CATEGORY_OPTIONS, category);
  }

  public void fillAdditionalInformationOnTheTypeOfContact(String description) {
    webDriverHelpers.fillInWebElement(
        ADDITIONAL_INFORMATION_OF_THE_TYPE_OF_CONTACT_INPUT, description);
  }

  public void fillRelationshipWithCase(String relationshipWithCase) {
    webDriverHelpers.selectFromCombobox(RELATIONSHIP_WITH_CASE_COMBOBOX, relationshipWithCase);
  }

  public void fillDescriptionOfHowContactTookPlace(String descriptionOfHowContactTookPlace) {
    webDriverHelpers.fillInWebElement(
        DESCRIPTION_OF_HOW_CONTACT_TOOK_PLACE_INPUT, descriptionOfHowContactTookPlace);
  }

  private void openContactFromResultsByUUID(String uuid) {
    By uuidLocator = By.cssSelector(String.format(CONTACT_RESULTS_UUID_LOCATOR, uuid));
    webDriverHelpers.clickOnWebElementBySelector((uuidLocator));
    webDriverHelpers.waitUntilIdentifiedElementIsPresent(UUID_INPUT);
  }

  public Contact collectContactData() {
    String collectedDateOfReport = webDriverHelpers.getValueFromWebElement(REPORT_DATE);
    LocalDate parsedDateOfReport = LocalDate.parse(collectedDateOfReport, formatter);
    String collectedLastDateOfContact =
        webDriverHelpers.getValueFromWebElement(DATE_OF_LAST_CONTACT_INPUT);
    LocalDate parsedLastDateOfContact = LocalDate.parse(collectedLastDateOfContact, formatter);
    Contact contactInfo = getContactInformation();

    return Contact.builder()
        .firstName(contactInfo.getFirstName())
        .lastName(contactInfo.getLastName())
        .returningTraveler(
            webDriverHelpers.getCheckedOptionFromHorizontalOptionGroup(RETURNING_TRAVELER_OPTIONS))
        .reportDate(parsedDateOfReport)
        .dateOfLastContact(parsedLastDateOfContact)
        .responsibleRegion(webDriverHelpers.getValueFromWebElement(RESPONSIBLE_REGION_INPUT))
        .responsibleDistrict(webDriverHelpers.getValueFromWebElement(RESPONSIBLE_DISTRICT_INPUT))
        .responsibleCommunity(webDriverHelpers.getValueFromWebElement(RESPONSIBLE_COMMUNITY_INPUT))
        .additionalInformationOnContactType(
            webDriverHelpers.getValueFromWebElement(
                ADDITIONAL_INFORMATION_OF_THE_TYPE_OF_CONTACT_INPUT))
        .typeOfContact(
            webDriverHelpers.getCheckedOptionFromHorizontalOptionGroup(TYPE_OF_CONTACT_OPTIONS))
        .contactCategory(
            webDriverHelpers.getCheckedOptionFromHorizontalOptionGroup(CONTACT_CATEGORY_OPTIONS))
        .relationshipWithCase(webDriverHelpers.getValueFromWebElement(RELATIONSHIP_WITH_CASE_INPUT))
        .descriptionOfHowContactTookPlace(
            webDriverHelpers.getValueFromWebElement(DESCRIPTION_OF_HOW_CONTACT_TOOK_PLACE_INPUT))
        .uuid(webDriverHelpers.getValueFromWebElement(UUID_INPUT))
        .build();
  }

  public Contact getContactInformation() {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy");
    String contactInfo = webDriverHelpers.getTextFromWebElement(USER_INFORMATION);
    String[] contactInfos = contactInfo.split(" ");
    LocalDate localDate = LocalDate.parse(contactInfos[3].replace(")", ""), formatter);
    return Contact.builder()
        .firstName(contactInfos[0])
        .lastName(contactInfos[1])
        .dateOfBirth(localDate)
        .build();
  }
}
