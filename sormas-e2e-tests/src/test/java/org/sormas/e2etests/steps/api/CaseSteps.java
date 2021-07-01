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
package org.sormas.e2etests.steps.api;

import static com.google.common.truth.Truth.assertWithMessage;

import cucumber.api.java8.En;
import javax.inject.Inject;
import org.sormas.e2etests.helpers.api.CaseHelper;
import org.sormas.e2etests.pojo.api.Case;
import org.sormas.e2etests.services.api.CaseApiService;
import org.sormas.e2etests.state.ApiState;

public class CaseSteps implements En {

  @Inject
  public CaseSteps(CaseHelper caseHelper, CaseApiService caseApiService, ApiState apiState) {

    When(
        "^API: I create a new case$",
        () -> {
          Case caze = caseApiService.buildGeneratedCase(apiState.getEditPerson());
          caseHelper.createCase(caze);
          apiState.setCreatedCase(caze);
        });

    When(
        "^API: I create a new case with already created Case uuid$",
        () -> {
          Case caze =
              caseApiService.buildGeneratedCase(
                  apiState.getEditPerson().toBuilder()
                      .uuid(apiState.getCreatedCase().getUuid())
                      .build());
          caseHelper.createCase(caze);
        });

    When(
        "API: I create a new case with an already created case",
        () -> caseHelper.createCase(apiState.getCreatedCase()));

    When(
        "API: I query the last created case",
        () -> caseHelper.getCaseByUuid(apiState.getCreatedCase().getUuid()));

    When(
        "API: I check if the response can be converted to a case object",
        () -> {
          Case caze = caseHelper.getCaseFromResponseIgnoringUnrecognizedFields();
          assertWithMessage("The queried case was not created").that(caze).isNotNull();
          assertWithMessage("The queried case uuid is not valid").that(caze.getUuid()).isNotEmpty();
        });

    When(
        "^API: I create a new case with disease (.*)$",
        (String disease) -> {
          Case caze = caseApiService.buildGeneratedCase(apiState.getEditPerson());
          caze = caze.toBuilder().disease(disease).build();
          caseHelper.createCase(caze);
        });
  }
}
