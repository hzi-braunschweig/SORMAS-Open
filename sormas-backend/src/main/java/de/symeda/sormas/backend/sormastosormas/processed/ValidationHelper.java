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

package de.symeda.sormas.backend.sormastosormas.processed;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import de.symeda.sormas.api.HasUuid;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.labmessage.LabMessageDto;
import de.symeda.sormas.api.sample.PathogenTestDto;
import de.symeda.sormas.api.sample.SampleDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasValidationException;
import de.symeda.sormas.api.sormastosormas.ValidationErrors;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.api.utils.ValidationRuntimeException;

public class ValidationHelper {

	public static String buildValidationGroupName(String captionTag, HasUuid hasUuid) {
		return buildValidationGroupName(captionTag, hasUuid.getUuid());
	}

	public static String buildValidationGroupName(String captionTag, String uuid) {
		return String.format("%s %s", I18nProperties.getCaption(captionTag), DataHelper.getShortUuid(uuid));
	}

	public static String buildCaseValidationGroupName(HasUuid caze) {
		return buildValidationGroupName(Captions.CaseData, caze);
	}

	public static String buildContactValidationGroupName(HasUuid contact) {
		return buildValidationGroupName(Captions.Contact, contact);
	}

	public static String buildSampleValidationGroupName(SampleDto sample) {
		return buildValidationGroupName(Captions.Sample, sample);
	}

	public static String buildPathogenTestValidationGroupName(PathogenTestDto pathogenTest) {
		return buildValidationGroupName(Captions.PathogenTest, pathogenTest);
	}

	public static String buildEventValidationGroupName(HasUuid event) {
		return buildValidationGroupName(Captions.Event, event);
	}

	public static String buildEventParticipantValidationGroupName(HasUuid event) {
		return buildValidationGroupName(Captions.Event, event);
	}

	public static String buildLabMessageValidationGroupName(LabMessageDto labMessageDto) {
		return buildValidationGroupName(Captions.LabMessage, labMessageDto);
	}

	public static <T> T handleValidationError(Supplier<T> saveOperation, String validationGroupCaption, String parentValidationGroup)
		throws SormasToSormasValidationException {
		try {
			return saveOperation.get();
		} catch (ValidationRuntimeException exception) {
			Map<String, ValidationErrors> parentError = new HashMap<>(1);
			parentError
				.put(parentValidationGroup, ValidationErrors.create(I18nProperties.getCaption(validationGroupCaption), exception.getMessage()));

			throw new SormasToSormasValidationException(parentError, exception);
		}
	}
}
