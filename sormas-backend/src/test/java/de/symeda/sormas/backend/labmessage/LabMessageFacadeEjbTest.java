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

package de.symeda.sormas.backend.labmessage;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import de.symeda.sormas.api.labmessage.LabMessageDto;
import de.symeda.sormas.backend.AbstractBeanTest;

public class LabMessageFacadeEjbTest extends AbstractBeanTest {

	@Test
	public void deleteLabMessage() {
		LabMessageDto labMessage = creator.createLabMessage(null);

		getLabMessageFacade().deleteLabMessage(labMessage.getUuid());

		assertThat(getLabMessageFacade().getByUuid(labMessage.getUuid()), is(nullValue()));
	}

	@Test
	public void testGetByReportId() {
		String reportId = "123456789";
		creator.createLabMessage((lm) -> lm.setReportId(reportId));

		List<LabMessageDto> list = getLabMessageFacade().getByReportId(null);

		assertNotNull(list);
		assertTrue(list.isEmpty());

		list = getLabMessageFacade().getByReportId("");

		assertNotNull(list);
		assertTrue(list.isEmpty());

		list = getLabMessageFacade().getByReportId(reportId);

		assertNotNull(list);
		assertFalse(list.isEmpty());
		assertEquals(1, list.size());
		assertEquals(reportId, list.get(0).getReportId());
	}
}
