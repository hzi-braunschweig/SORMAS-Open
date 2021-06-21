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

package org.sormas.e2etests.steps.web.application.contacts;

import static org.sormas.e2etests.pages.application.contacts.ContactDirectoryPage.*;
import static org.sormas.e2etests.pages.application.contacts.EditContactPage.*;

import cucumber.api.java8.En;
import javax.inject.Inject;
import org.assertj.core.api.SoftAssertions;
import org.sormas.e2etests.helpers.AssertHelpers;
import org.sormas.e2etests.helpers.WebDriverHelpers;
import org.sormas.e2etests.state.ApiState;

public class ChooseSourceCaseSteps implements En {

  protected WebDriverHelpers webDriverHelpers;

  @Inject
  public ChooseSourceCaseSteps(
      WebDriverHelpers webDriverHelpers,
      ApiState apiState,
      AssertHelpers assertHelpers,
      final SoftAssertions softly) {
    this.webDriverHelpers = webDriverHelpers;

    When(
        "^I search for the last case uuid in the CHOOSE SOURCE window$",
        () -> {
          webDriverHelpers.fillInWebElement(
              SOURCE_CASE_WINDOW_CASE_INPUT, apiState.getCreatedCase().getUuid());
          webDriverHelpers.clickOnWebElementBySelector(SOURCE_CASE_WINDOW_SEARCH_CASE_BUTTON);
        });

    When(
        "^I open the first found result in the CHOOSE SOURCE window$",
        () -> {
          webDriverHelpers.clickOnWebElementBySelector(SOURCE_CASE_WINDOW_FIRST_RESULT_OPTION);
          webDriverHelpers.clickWhileOtherButtonIsDisplayed(
              SOURCE_CASE_WINDOW_CONFIRM_BUTTON, CHANGE_CASE_BUTTON);
        });

    When(
        "I click on the CHOOSE SOURCE CASE button",
        () ->
            webDriverHelpers.clickWhileOtherButtonIsDisplayed(
                CHOOSE_SOURCE_CASE_BUTTON, DISCARD_POPUP_YES_BUTTON));

    When(
        "I click yes on the DISCARD UNSAVED CHANGES popup",
        () ->
            webDriverHelpers.clickWhileOtherButtonIsDisplayed(
                DISCARD_POPUP_YES_BUTTON, SOURCE_CASE_WINDOW_SEARCH_CASE_BUTTON));

    Then(
        "I check the linked case information is correctly displayed",
        () -> {
          String casePerson = webDriverHelpers.getTextFromWebElement(CASE_PERSON_LABEL);
          String caseDisease =
              (webDriverHelpers.getTextFromWebElement(CASE_DISEASE_LABEL).equals("COVID-19"))
                  ? "CORONAVIRUS"
                  : "Not expected string!";
          String caseClassification =
              (webDriverHelpers
                      .getTextFromWebElement(CASE_CLASSIFICATION_LABEL)
                      .equals("Not yet classified"))
                  ? "NOT_CLASSIFIED"
                  : "Not expected string!";
          String caseId = webDriverHelpers.getTextFromWebElement(CASE_ID_LABEL);

          softly
              .assertThat(
                  apiState.getCreatedCase().getPerson().getFirstName()
                      + " "
                      + apiState.getCreatedCase().getPerson().getLastName())
              .isEqualToIgnoringCase(casePerson);
          softly
              .assertThat(apiState.getCreatedCase().getDisease())
              .isEqualToIgnoringCase(caseDisease);
          softly
              .assertThat(apiState.getCreatedCase().getCaseClassification())
              .isEqualToIgnoringCase(caseClassification);
          softly
              .assertThat(apiState.getCreatedCase().getUuid().substring(0, 6))
              .isEqualToIgnoringCase(caseId);
          softly.assertAll();
        });
  }
}
