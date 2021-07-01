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
package org.sormas.e2etests.helpers.api;

import static org.sormas.e2etests.constants.api.Endpoints.CASES;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.Method;
import java.io.ByteArrayOutputStream;
import java.util.List;
import javax.inject.Inject;
import lombok.SneakyThrows;
import org.sormas.e2etests.helpers.RestAssuredClient;
import org.sormas.e2etests.pojo.api.Case;
import org.sormas.e2etests.pojo.api.Request;
import org.sormas.e2etests.state.ApiState;

public class CaseHelper {

  private final RestAssuredClient restAssuredClient;
  private final ObjectMapper objectMapper;
  private final ApiState apiState;

  @Inject
  public CaseHelper(
      RestAssuredClient restAssuredClient, ObjectMapper objectMapper, ApiState apiState) {
    this.restAssuredClient = restAssuredClient;
    this.objectMapper = objectMapper;
    this.apiState = apiState;
  }

  @SneakyThrows
  public void createCase(Case caze) {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    List<Case> listOfContacts = List.of(caze);
    objectMapper.writeValue(out, listOfContacts);
    restAssuredClient.sendRequest(
        Request.builder().method(Method.POST).path(CASES + "push").body(out.toString()).build());
  }

  public void getCaseByUuid(String uuid) {
    restAssuredClient.sendRequest(Request.builder().method(Method.GET).path(CASES + uuid).build());
  }

  @SneakyThrows
  public Case getCaseFromResponseIgnoringUnrecognizedFields() {
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    return objectMapper.readValue(apiState.getResponse().prettyPrint(), Case.class);
  }
}
