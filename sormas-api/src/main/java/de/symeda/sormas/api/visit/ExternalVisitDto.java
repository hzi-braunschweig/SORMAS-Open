package de.symeda.sormas.api.visit;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;

import de.symeda.sormas.api.Disease;
import de.symeda.sormas.api.symptoms.SymptomsDto;
import javax.validation.constraints.NotNull;
import de.symeda.sormas.api.utils.SensitiveData;

/**
 * The class ExternalVisitDto.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExternalVisitDto implements Serializable, Cloneable {

	private static final long serialVersionUID = 7909093498222091926L;

	@NotNull
	private String personUuid;
	@NotNull
	private Disease disease;
	@NotNull
	private Date visitDateTime;
	@NotNull
	private VisitStatus visitStatus;
	@SensitiveData
	private String visitRemarks;

	private SymptomsDto symptoms;

	private Double reportLat;
	private Double reportLon;
	private Float reportLatLonAccuracy;

	public static ExternalVisitDto build(
		String personUuid,
		Disease disease,
		Date visitDateTime,
		VisitStatus visitStatus,
		String visitRemarks,
		SymptomsDto symptoms,
		Double reportLat,
		Double reportLon,
		Float reportLatLonAccuracy) {
		final ExternalVisitDto externalVisitDto = new ExternalVisitDto();
		externalVisitDto.setPersonUuid(personUuid);
		externalVisitDto.setDisease(disease);
		externalVisitDto.setVisitDateTime(visitDateTime);
		externalVisitDto.setVisitStatus(visitStatus);
		externalVisitDto.setVisitRemarks(visitRemarks);
		externalVisitDto.setSymptoms(symptoms);
		externalVisitDto.setReportLat(reportLat);
		externalVisitDto.setReportLon(reportLon);
		externalVisitDto.setReportLatLonAccuracy(reportLatLonAccuracy);
		return externalVisitDto;
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

	public String getPersonUuid() {
		return personUuid;
	}

	public void setPersonUuid(String personUuid) {
		this.personUuid = personUuid;
	}

	public Disease getDisease() {
		return disease;
	}

	public void setDisease(Disease disease) {
		this.disease = disease;
	}
}
