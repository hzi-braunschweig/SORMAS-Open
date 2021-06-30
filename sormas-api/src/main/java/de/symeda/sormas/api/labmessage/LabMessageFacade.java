package de.symeda.sormas.api.labmessage;

import java.util.Date;
import java.util.List;

import javax.ejb.Remote;

import de.symeda.sormas.api.utils.SortProperty;

@Remote
public interface LabMessageFacade {

	LabMessageDto save(LabMessageDto dto);

	LabMessageDto getByUuid(String uuid);

	void deleteLabMessage(String uuid);

	void deleteLabMessages(List<String> uuids);

	List<LabMessageDto> getForSample(String sampleUuid);

	List<LabMessageDto> getByPathogenTestUuid(String pathogenTestUuid);

	/**
	 * This method is used to check whether a labMessage is marked processed in the database.
	 * It can be used to check for recent changes.
	 *
	 * @param uuid
	 *            of the labMessage
	 * @return true if the labMessage is marked processed in the database, false otherwise.
	 */
	Boolean isProcessed(String uuid);

	long count(LabMessageCriteria criteria);

	List<LabMessageIndexDto> getIndexList(LabMessageCriteria criteria, Integer first, Integer max, List<SortProperty> sortProperties);

	boolean atLeastOneFetchExecuted();

	/**
	 * Fetches external lab messages from the connected external system and saves them in the database.
	 *
	 * @return a {@link LabMessageFetchResult LabMessageFetchResult}. If any error occurred, an appropriate message is provided in the
	 *         object.
	 */
	LabMessageFetchResult fetchAndSaveExternalLabMessages(Date since);

	boolean exists(String uuid);

	List<LabMessageDto> getByReportId(String reportId);

}
