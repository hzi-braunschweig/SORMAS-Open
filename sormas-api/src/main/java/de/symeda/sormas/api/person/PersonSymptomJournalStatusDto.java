package de.symeda.sormas.api.person;

import javax.validation.constraints.NotNull;

import java.io.Serializable;

public class PersonSymptomJournalStatusDto implements Serializable {

	private static final long serialVersionUID = 6985497943624025440L;

	@NotNull
	private SymptomJournalStatus status;
	private String statusDateTime; // is not used, but given according to API specification.

	public static PersonSymptomJournalStatusDto build(SymptomJournalStatus status, String statusDateTime) {
		final PersonSymptomJournalStatusDto personSymptomJournalStatusDto = new PersonSymptomJournalStatusDto();
		personSymptomJournalStatusDto.setStatus(status);
		personSymptomJournalStatusDto.setStatusDateTime(statusDateTime);
		return personSymptomJournalStatusDto;
	};

	public SymptomJournalStatus getStatus() {
		return status;
	}

	public void setStatus(SymptomJournalStatus status) {
		this.status = status;
	}

	public String getStatusDateTime() {
		return statusDateTime;
	}

	public void setStatusDateTime(String statusDateTime) {
		this.statusDateTime = statusDateTime;
	}
}
