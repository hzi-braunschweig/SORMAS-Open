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

package de.symeda.sormas.backend.sormastosormas;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;

import javax.ws.rs.core.Response;

import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import com.fasterxml.jackson.core.JsonProcessingException;

import de.symeda.sormas.api.labmessage.LabMessageDto;
import de.symeda.sormas.api.labmessage.LabMessageStatus;
import de.symeda.sormas.api.sample.PathogenTestResultType;
import de.symeda.sormas.api.sample.PathogenTestType;
import de.symeda.sormas.api.sample.SampleMaterial;
import de.symeda.sormas.api.sormastosormas.ServerAccessDataReferenceDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasEncryptedDataDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasException;
import de.symeda.sormas.api.sormastosormas.SormasToSormasOptionsDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasValidationException;
import de.symeda.sormas.backend.MockProducer;

public class SormasToSormasLabMessageFacadeEjbTest extends SormasToSormasFacadeTest {

	@Test
	public void testSendLabMessage() throws SormasToSormasException {
		Date dateNow = new Date();

		LabMessageDto labMessage = creator.createLabMessage((lm) -> setLabMessageFields(lm, dateNow));

		Mockito
			.when(
				MockProducer.getSormasToSormasClient()
					.post(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.any()))
			.thenAnswer(invocation -> {
				assertThat(invocation.getArgument(0, String.class), is(SECOND_SERVER_REST_URL));
				assertThat(invocation.getArgument(1, String.class), is("/sormasToSormas/labmessages"));

				String authToken = invocation.getArgument(2, String.class);
				assertThat(authToken, startsWith("Basic "));
				String credentials = new String(Base64.getDecoder().decode(authToken.replace("Basic ", "")), StandardCharsets.UTF_8);
				// uses password from server-list.csv from `serveraccessdefault` package
				//assertThat(credentials, is(StartupShutdownService.SORMAS_TO_SORMAS_USER_NAME + ":" + SECOND_SERVER_REST_PASSWORD));

				SormasToSormasEncryptedDataDto encryptedData = invocation.getArgument(3, SormasToSormasEncryptedDataDto.class);
				assertThat(encryptedData.getOrganizationId(), is(DEFAULT_SERVER_ACCESS_CN));

				LabMessageDto[] sharedMessages = decryptSharesData(encryptedData.getData(), LabMessageDto[].class);
				LabMessageDto sharedLabMessage = sharedMessages[0];

				assertThat(labMessage.getUuid(), is(labMessage.getUuid()));
				assertLabMessageFields(sharedLabMessage, dateNow);

				return Response.noContent().build();
			});

		SormasToSormasOptionsDto options = new SormasToSormasOptionsDto();
		options.setOrganization(new ServerAccessDataReferenceDto(SECOND_SERVER_ACCESS_CN));

		getSormasToSormasLabMessageFacade().sendLabMessages(Collections.singletonList(labMessage.getUuid()), options);

		Mockito.verify(MockProducer.getSormasToSormasClient(), Mockito.times(1))
			.post(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.any(), ArgumentMatchers.any());
		assertThat(getLabMessageFacade().getByUuid(labMessage.getUuid()).getStatus(), is(LabMessageStatus.FORWARDED));
	}

	@Test
	public void testSaveLabMessages() throws JsonProcessingException, SormasToSormasException, SormasToSormasValidationException {
		LabMessageDto labMessage = LabMessageDto.build();
		Date dateNow = new Date();
		setLabMessageFields(labMessage, dateNow);

		SormasToSormasEncryptedDataDto encryptedData = encryptShareDataAsArray(labMessage);
		getSormasToSormasLabMessageFacade().saveLabMessages(encryptedData);

		LabMessageDto savedLabMessage = getLabMessageFacade().getByUuid(labMessage.getUuid());
		assertThat(savedLabMessage, is(notNullValue()));
		assertLabMessageFields(savedLabMessage, dateNow);
	}

	private void setLabMessageFields(LabMessageDto labMessage, Date dateValue) {
		labMessage.setMessageDateTime(dateValue);
		labMessage.setSampleDateTime(dateValue);
		labMessage.setSampleMaterial(SampleMaterial.RECTAL_SWAB);
		labMessage.setLabSampleId("Test lab sample ID");
		labMessage.setTestType(PathogenTestType.CULTURE);
		labMessage.setTestDateTime(dateValue);
		labMessage.setTestResult(PathogenTestResultType.PENDING);
		labMessage.setTestResultVerified(true);
		labMessage.setPersonFirstName("James");
		labMessage.setPersonLastName("Smith");
		labMessage.setPersonPostalCode("test postal code");
	}

	private void assertLabMessageFields(LabMessageDto labMessage, Date dateValue) {
		long dateTime = dateValue.getTime();

		assertThat(labMessage.getMessageDateTime().getTime(), is(dateTime));
		assertThat(labMessage.getSampleDateTime().getTime(), is(dateTime));
		assertThat(labMessage.getSampleMaterial(), is(SampleMaterial.RECTAL_SWAB));
		assertThat(labMessage.getLabSampleId(), is("Test lab sample ID"));
		assertThat(labMessage.getTestType(), is(PathogenTestType.CULTURE));
		assertThat(labMessage.getTestDateTime().getTime(), is(dateTime));
		assertThat(labMessage.getTestResult(), is(PathogenTestResultType.PENDING));
		assertThat(labMessage.isTestResultVerified(), is(true));
		assertThat(labMessage.getPersonFirstName(), is("James"));
		assertThat(labMessage.getPersonLastName(), is("Smith"));
		assertThat(labMessage.getPersonPostalCode(), is("test postal code"));
	}
}
