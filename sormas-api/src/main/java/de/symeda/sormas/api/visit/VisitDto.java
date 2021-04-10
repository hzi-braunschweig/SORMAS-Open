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
package de.symeda.sormas.api.visit;

import java.util.Date;

import de.symeda.sormas.api.Disease;
import de.symeda.sormas.api.VisitOrigin;
import de.symeda.sormas.api.person.PersonReferenceDto;
import de.symeda.sormas.api.symptoms.SymptomsDto;
import de.symeda.sormas.api.user.UserReferenceDto;
import de.symeda.sormas.api.utils.DataHelper;
import javax.validation.constraints.NotNull;
import de.symeda.sormas.api.utils.SensitiveData;
import de.symeda.sormas.api.utils.pseudonymization.PseudonymizableDto;

public class VisitDto extends PseudonymizableDto {

	private static final long serialVersionUID = -441664767075414789L;

	public static final String I18N_PREFIX = "Visit";

	public static final String PERSON = "person";
	public static final String DISEASE = "disease";
	public static final String VISIT_DATE_TIME = "visitDateTime";
	public static final String VISIT_USER = "visitUser";
	public static final String VISIT_STATUS = "visitStatus";
	public static final String VISIT_REMARKS = "visitRemarks";
	public static final String SYMPTOMS = "symptoms";
	public static final String REPORT_LAT = "reportLat";
	public static final String REPORT_LON = "reportLon";
	public static final String ORIGIN = "origin";

	@NotNull
	private PersonReferenceDto person;
	private Disease disease;
	@NotNull
	private Date visitDateTime;
	@NotNull
	@SensitiveData
	private UserReferenceDto visitUser;
	@NotNull
	private VisitStatus visitStatus;
	@SensitiveData
	private String visitRemarks;
	private SymptomsDto symptoms;

	@SensitiveData
	private Double reportLat;
	@SensitiveData
	private Double reportLon;

	private Float reportLatLonAccuracy;
	private VisitOrigin origin;

	public static VisitDto build(PersonReferenceDto contactPerson, Disease disease, VisitOrigin origin) {

		VisitDto visit = new VisitDto();
		visit.setUuid(DataHelper.createUuid());

		visit.setPerson(contactPerson);
		visit.setDisease(disease);

		SymptomsDto symptoms = new SymptomsDto();
		visit.setSymptoms(symptoms);

		visit.setVisitDateTime(new Date());
		visit.setOrigin(origin);

		return visit;
	}

	public static VisitDto build(
		PersonReferenceDto person,
		Disease disease,
		Date visitDateTime,
		UserReferenceDto visitUser,
		VisitStatus visitStatus,
		String visitRemarks,
		SymptomsDto symptoms,
		Double reportLat,
		Double reportLon,
		Float reportLatLonAccuracy,
		VisitOrigin origin) {

		final VisitDto visit = build(person, disease, origin);

		if (visitDateTime != null) {
			visit.setVisitDateTime(visitDateTime);
		}
		visit.setVisitUser(visitUser);
		visit.setVisitStatus(visitStatus);
		visit.setVisitRemarks(visitRemarks);
		visit.setVisitRemarks(visitRemarks);
		if (symptoms != null) {
			visit.setSymptoms(symptoms);
		}
		visit.setReportLat(reportLat);
		visit.setReportLon(reportLon);
		visit.setReportLatLonAccuracy(reportLatLonAccuracy);

		return visit;
	}

	public Date getVisitDateTime() {
		return visitDateTime;
	}

	public void setVisitDateTime(Date visitDateTime) {
		this.visitDateTime = visitDateTime;
	}

	public VisitStatus getVisitStatus() {
		return visitStatus;
	}

	public void setVisitStatus(VisitStatus visitStatus) {
		this.visitStatus = visitStatus;
	}

	public String getVisitRemarks() {
		return visitRemarks;
	}

	public void setVisitRemarks(String visitRemarks) {
		this.visitRemarks = visitRemarks;
	}

	public SymptomsDto getSymptoms() {
		return symptoms;
	}

	public void setSymptoms(SymptomsDto symptoms) {
		this.symptoms = symptoms;
	}

	public PersonReferenceDto getPerson() {
		return person;
	}

	public void setPerson(PersonReferenceDto person) {
		this.person = person;
	}

	public Disease getDisease() {
		return disease;
	}

	public void setDisease(Disease disease) {
		this.disease = disease;
	}

	public UserReferenceDto getVisitUser() {
		return visitUser;
	}

	public void setVisitUser(UserReferenceDto visitUser) {
		this.visitUser = visitUser;
	}

	public Double getReportLat() {
		return reportLat;
	}

	public void setReportLat(Double reportLat) {
		this.reportLat = reportLat;
	}

	public Double getReportLon() {
		return reportLon;
	}

	public void setReportLon(Double reportLon) {
		this.reportLon = reportLon;
	}

	public Float getReportLatLonAccuracy() {
		return reportLatLonAccuracy;
	}

	public void setReportLatLonAccuracy(Float reportLatLonAccuracy) {
		this.reportLatLonAccuracy = reportLatLonAccuracy;
	}

	public VisitOrigin getOrigin() {
		return origin;
	}

	public void setOrigin(VisitOrigin origin) {
		this.origin = origin;
	}

	public VisitReferenceDto toReference() {
		return new VisitReferenceDto(getUuid());
	}
}
