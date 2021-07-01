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
import org.sormas.e2etests.state.ApiState;

public class ApiHelperSteps implements En {

  @Inject
  public ApiHelperSteps(ApiState apiState) {

    When(
        "^API: I check if the response contains ([^\"]*)$",
        (String expectedString) ->
            assertWithMessage("The response does not contains  %s", expectedString)
                .that(apiState.getResponse().prettyPrint())
                .contains(expectedString));

    When(
        "^API: I check if the response has status (\\d+)$",
        (Integer statusCode) ->
            assertWithMessage("The response does not have the status  %s", statusCode)
                .that(apiState.getResponse().getStatusCode())
                .isEqualTo(statusCode));
  }
}
