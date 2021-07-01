package de.symeda.sormas.backend.labmessage;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.naming.CannotProceedException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.i18n.Validations;
import de.symeda.sormas.api.labmessage.ExternalLabResultsFacade;
import de.symeda.sormas.api.labmessage.ExternalMessageResult;
import de.symeda.sormas.api.labmessage.LabMessageCriteria;
import de.symeda.sormas.api.labmessage.LabMessageDto;
import de.symeda.sormas.api.labmessage.LabMessageFacade;
import de.symeda.sormas.api.labmessage.LabMessageFetchResult;
import de.symeda.sormas.api.labmessage.LabMessageIndexDto;
import de.symeda.sormas.api.labmessage.LabMessageStatus;
import de.symeda.sormas.api.labmessage.NewMessagesState;
import de.symeda.sormas.api.systemevents.SystemEventDto;
import de.symeda.sormas.api.systemevents.SystemEventStatus;
import de.symeda.sormas.api.systemevents.SystemEventType;
import de.symeda.sormas.api.utils.SortProperty;
import de.symeda.sormas.backend.common.ConfigFacadeEjb;
import de.symeda.sormas.backend.common.CriteriaBuilderHelper;
import de.symeda.sormas.backend.sample.PathogenTest;
import de.symeda.sormas.backend.sample.PathogenTestService;
import de.symeda.sormas.backend.sample.Sample;
import de.symeda.sormas.backend.systemevent.SystemEventFacadeEjb;
import de.symeda.sormas.backend.util.DtoHelper;
import de.symeda.sormas.backend.util.ModelConstants;

@Stateless(name = "LabMessageFacade")
public class LabMessageFacadeEjb implements LabMessageFacade {

	public static final List<String> VALID_SORT_PROPERTY_NAMES = Arrays.asList(
		LabMessageIndexDto.UUID,
		LabMessageIndexDto.PERSON_FIRST_NAME,
		LabMessageIndexDto.PERSON_LAST_NAME,
		LabMessageIndexDto.MESSAGE_DATE_TIME,
		LabMessageIndexDto.STATUS,
		LabMessageIndexDto.TEST_RESULT,
		LabMessageIndexDto.TESTED_DISEASE);

	@PersistenceContext(unitName = ModelConstants.PERSISTENCE_UNIT_NAME)
	private EntityManager em;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@EJB
	private LabMessageService labMessageService;
	@EJB
	private PathogenTestService pathogenTestService;
	@EJB
	private ConfigFacadeEjb.ConfigFacadeEjbLocal configFacade;
	@EJB
	private SystemEventFacadeEjb.SystemEventFacadeEjbLocal systemEventFacade;

	LabMessage fromDto(@NotNull LabMessageDto source, LabMessage target, boolean checkChangeDate) {

		target = DtoHelper.fillOrBuildEntity(source, target, LabMessage::new, checkChangeDate);

		target.setLabMessageDetails(source.getLabMessageDetails());
		target.setLabSampleId(source.getLabSampleId());
		target.setMessageDateTime(source.getMessageDateTime());
		target.setPersonBirthDateDD(source.getPersonBirthDateDD());
		target.setPersonBirthDateMM(source.getPersonBirthDateMM());
		target.setPersonBirthDateYYYY(source.getPersonBirthDateYYYY());
		target.setPersonCity(source.getPersonCity());
		target.setPersonFirstName(source.getPersonFirstName());
		target.setPersonHouseNumber(source.getPersonHouseNumber());
		target.setPersonLastName(source.getPersonLastName());
		target.setPersonPostalCode(source.getPersonPostalCode());
		target.setPersonSex(source.getPersonSex());
		target.setPersonStreet(source.getPersonStreet());
		target.setStatus(source.getStatus());
		target.setSampleDateTime(source.getSampleDateTime());
		target.setSampleMaterial(source.getSampleMaterial());
		target.setSampleMaterialText(source.getSampleMaterialText());
		target.setSampleReceivedDate(source.getSampleReceivedDate());
		target.setSpecimenCondition(source.getSpecimenCondition());
		target.setPersonPhone(source.getPersonPhone());
		target.setPersonEmail(source.getPersonEmail());
		target.setTestDateTime(source.getTestDateTime());
		target.setTestedDisease(source.getTestedDisease());
		target.setTestLabCity(source.getTestLabCity());
		target.setTestLabExternalId(source.getTestLabExternalId());
		target.setTestLabName(source.getTestLabName());
		target.setTestLabPostalCode(source.getTestLabPostalCode());
		target.setTestResult(source.getTestResult());
		target.setTestResultVerified(source.isTestResultVerified());
		target.setTestType(source.getTestType());
		target.setTestResultText(source.getTestResultText());
		target.setPathogenTest(pathogenTestService.getByReferenceDto(source.getPathogenTest()));
		target.setReportId(source.getReportId());

		return target;
	}

	@Override
	public LabMessageDto save(LabMessageDto dto) {

		LabMessage labMessage = labMessageService.getByUuid(dto.getUuid());

		labMessage = fromDto(dto, labMessage, true);
		labMessageService.ensurePersisted(labMessage);

		return toDto(labMessage);
	}

	public LabMessageDto toDto(LabMessage source) {

		if (source == null) {
			return null;
		}
		LabMessageDto target = new LabMessageDto();
		DtoHelper.fillDto(target, source);

		target.setLabMessageDetails(source.getLabMessageDetails());
		target.setLabSampleId(source.getLabSampleId());
		target.setMessageDateTime(source.getMessageDateTime());
		target.setPersonBirthDateDD(source.getPersonBirthDateDD());
		target.setPersonBirthDateMM(source.getPersonBirthDateMM());
		target.setPersonBirthDateYYYY(source.getPersonBirthDateYYYY());
		target.setPersonCity(source.getPersonCity());
		target.setPersonFirstName(source.getPersonFirstName());
		target.setPersonHouseNumber(source.getPersonHouseNumber());
		target.setPersonLastName(source.getPersonLastName());
		target.setPersonPostalCode(source.getPersonPostalCode());
		target.setPersonSex(source.getPersonSex());
		target.setPersonStreet(source.getPersonStreet());
		target.setPersonPhone(source.getPersonPhone());
		target.setPersonEmail(source.getPersonEmail());
		target.setStatus(source.getStatus());
		target.setSampleDateTime(source.getSampleDateTime());
		target.setSampleMaterial(source.getSampleMaterial());
		target.setSampleMaterialText(source.getSampleMaterialText());
		target.setSampleReceivedDate(source.getSampleReceivedDate());
		target.setSpecimenCondition(source.getSpecimenCondition());
		target.setTestDateTime(source.getTestDateTime());
		target.setTestedDisease(source.getTestedDisease());
		target.setTestLabCity(source.getTestLabCity());
		target.setTestLabExternalId(source.getTestLabExternalId());
		target.setTestLabName(source.getTestLabName());
		target.setTestLabPostalCode(source.getTestLabPostalCode());
		target.setTestResult(source.getTestResult());
		target.setTestResultVerified(source.isTestResultVerified());
		target.setTestType(source.getTestType());
		target.setTestResultText(source.getTestResultText());
		if (source.getStatus() == LabMessageStatus.PROCESSED && source.getPathogenTest() != null) {
			target.setPathogenTest(source.getPathogenTest().toReference());
		}
		target.setReportId(source.getReportId());

		return target;
	}

	@Override
	public LabMessageDto getByUuid(String uuid) {
		return toDto(labMessageService.getByUuid(uuid));
	}

	@Override
	public void deleteLabMessage(String uuid) {
		labMessageService.delete(labMessageService.getByUuid(uuid));
	}

	@Override
	public void deleteLabMessages(List<String> uuids) {
		List<LabMessage> labMessages = labMessageService.getByUuids(uuids);
		for (LabMessage labMessage : labMessages) {
			if (labMessage.getStatus() != LabMessageStatus.PROCESSED) {
				labMessageService.delete(labMessage);
			}
		}
	}

	@Override
	public List<LabMessageDto> getForSample(String sampleUuid) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<LabMessage> cq = cb.createQuery(LabMessage.class);
		Root<LabMessage> from = cq.from(LabMessage.class);
		Join<LabMessage, PathogenTest> pathogenTestJoin = from.join(LabMessage.PATHOGEN_TEST, JoinType.INNER);
		Join<PathogenTest, Sample> sampleJoin = pathogenTestJoin.join(PathogenTest.SAMPLE, JoinType.INNER);

		cq.where(cb.equal(sampleJoin.get(Sample.UUID), sampleUuid));
		cq.orderBy(cb.desc(from.get(LabMessage.MESSAGE_DATE_TIME)), cb.desc(from.get(LabMessage.CREATION_DATE)));

		return em.createQuery(cq).getResultList().stream().map(this::toDto).collect(toList());
	}

	@Override
	public List<LabMessageDto> getByPathogenTestUuid(String pathogenTestUuid) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<LabMessage> cq = cb.createQuery(LabMessage.class);
		Root<LabMessage> from = cq.from(LabMessage.class);

		cq.where(cb.equal(from.join(LabMessage.PATHOGEN_TEST, JoinType.INNER).get(PathogenTest.UUID), pathogenTestUuid));
		cq.orderBy(cb.desc(from.get(LabMessage.MESSAGE_DATE_TIME)), cb.desc(from.get(LabMessage.CREATION_DATE)));

		return em.createQuery(cq).getResultList().stream().map(this::toDto).collect(toList());
	}

	@Override
	public Boolean isProcessed(String uuid) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Boolean> cq = cb.createQuery(Boolean.class);
		Root<LabMessage> from = cq.from(LabMessage.class);

		Predicate filter = cb.and(cb.equal(from.get(LabMessage.UUID), uuid));

		cq.where(filter);
		cq.select(cb.equal(from.get(LabMessage.STATUS), LabMessageStatus.PROCESSED));

		try {
			return em.createQuery(cq).getSingleResult();
		} catch (NoResultException nre) {
			return null;
		}
	}

	@Override
	public long count(LabMessageCriteria criteria) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<LabMessage> labMessage = cq.from(LabMessage.class);

		criteriaHandler(criteria, cb, cq, labMessage);

		cq.select(cb.countDistinct(labMessage));
		return em.createQuery(cq).getSingleResult();
	}

	private <T> void criteriaHandler(LabMessageCriteria criteria, CriteriaBuilder cb, CriteriaQuery<T> cq, Root<LabMessage> labMessage) {
		Predicate filter = null;
		if (criteria != null) {
			Predicate statusFilter = labMessageService.buildCriteriaFilter(cb, labMessage, criteria);
			filter = CriteriaBuilderHelper.and(cb, null, statusFilter);
		}
		if (filter != null) {
			cq.where(filter);
		}
	}

	@Override
	public List<LabMessageIndexDto> getIndexList(LabMessageCriteria criteria, Integer first, Integer max, List<SortProperty> sortProperties) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<LabMessageIndexDto> cq = cb.createQuery(LabMessageIndexDto.class);
		Root<LabMessage> labMessage = cq.from(LabMessage.class);

		cq.multiselect(
			labMessage.get(LabMessage.UUID),
			labMessage.get(LabMessage.MESSAGE_DATE_TIME),
			labMessage.get(LabMessage.TEST_LAB_NAME),
			labMessage.get(LabMessage.TEST_LAB_POSTAL_CODE),
			labMessage.get(LabMessage.TESTED_DISEASE),
			labMessage.get(LabMessage.TEST_RESULT),
			labMessage.get(LabMessage.PERSON_FIRST_NAME),
			labMessage.get(LabMessage.PERSON_LAST_NAME),
			labMessage.get(LabMessage.PERSON_POSTAL_CODE),
			labMessage.get(LabMessage.STATUS));

		criteriaHandler(criteria, cb, cq, labMessage);

		// Distinct is necessary here to avoid duplicate results due to the user role join in taskService.createAssigneeFilter
		cq.distinct(true);

		List<Order> order = Optional.ofNullable(sortProperties)
			.orElseGet(ArrayList::new)
			.stream()
			.filter(sortProperty -> VALID_SORT_PROPERTY_NAMES.contains(sortProperty.propertyName))
			.map(sortProperty -> {
				Expression<?> expression = labMessage.get(sortProperty.propertyName);
				return sortProperty.ascending ? cb.asc(expression) : cb.desc(expression);
			})
			.collect(toList());

		order.add(cb.desc(labMessage.get(LabMessage.MESSAGE_DATE_TIME)));
		cq.orderBy(order);
		List<LabMessageIndexDto> labMessages;
		if (first != null && max != null) {
			labMessages = em.createQuery(cq).setFirstResult(first).setMaxResults(max).getResultList();
		} else {
			labMessages = em.createQuery(cq).getResultList();
		}

		return labMessages;
	}

	@Override
	public boolean atLeastOneFetchExecuted() {
		SystemEventDto latestSuccessEvent = systemEventFacade.getLatestSuccessByType(SystemEventType.FETCH_LAB_MESSAGES);
		return latestSuccessEvent != null;
	}

	/**
	 * This method marks the previously unfinished system events as UNCLEAR(if any exists) and creates a new event with status STARTED.
	 * If the fetching succeds, the status of the currentSystemEvent is changed to SUCCESS.
	 * In case of any Exception, the status of the currentSystemEvent is changed to ERROR.
	 *
	 * @return An indication whether the fetching of new labMessage was successful. If it was not, an error message meant for UI users.
	 */
	@Override
	public LabMessageFetchResult fetchAndSaveExternalLabMessages(Date since) {
		systemEventFacade.markPreviouslyStartedAsUnclear(SystemEventType.FETCH_LAB_MESSAGES);
		SystemEventDto currentSystemEvent = initializeFetchEvent();
		try {
			return fetchAndSaveExternalLabMessages(currentSystemEvent, since);
		} catch (CannotProceedException e) {
			systemEventFacade.reportError(currentSystemEvent, e.getMessage(), new Date());
			return new LabMessageFetchResult(false, NewMessagesState.UNCLEAR, e.getMessage());
		} catch (NamingException e) {
			systemEventFacade.reportError(currentSystemEvent, e.getMessage(), new Date());
			return new LabMessageFetchResult(false, NewMessagesState.UNCLEAR, I18nProperties.getString(Strings.errorLabResultsAdapterNotFound));
		} catch (Exception t) {
			systemEventFacade.reportError(currentSystemEvent, t.getMessage(), new Date());
			throw t;
		}
	}

	protected LabMessageFetchResult fetchAndSaveExternalLabMessages(SystemEventDto currentSystemEvent, Date since) throws NamingException {
		if (since == null) {
			since = findLastUpdateDate();
		}
		ExternalMessageResult<List<LabMessageDto>> externalMessageResult = fetchExternalMessages(since);
		if (externalMessageResult.isSuccess()) {
			externalMessageResult.getValue().forEach(this::save);
			String message = "Last synchronization date: " + externalMessageResult.getSynchronizationDate().getTime();
			systemEventFacade.reportSuccess(currentSystemEvent, message, new Date());
			return getSuccessfulFetchResult(externalMessageResult);
		} else {
			throw new CannotProceedException(externalMessageResult.getError());
		}
	}

	protected ExternalMessageResult<List<LabMessageDto>> fetchExternalMessages(Date since) throws NamingException {
		InitialContext ic = new InitialContext();
		String jndiName = configFacade.getDemisJndiName();

		if (jndiName == null) {
			throw new CannotProceedException(I18nProperties.getValidationError(Validations.externalMessageConfigError));
		}

		ExternalLabResultsFacade labResultsFacade = (ExternalLabResultsFacade) ic.lookup(jndiName);
		return labResultsFacade.getExternalLabMessages(since);
	}

	protected SystemEventDto initializeFetchEvent() {
		Date startDate = new Date();
		SystemEventDto systemEvent = SystemEventDto.build();
		systemEvent.setType(SystemEventType.FETCH_LAB_MESSAGES);
		systemEvent.setStatus(SystemEventStatus.STARTED);
		systemEvent.setStartDate(startDate);
		systemEventFacade.saveSystemEvent(systemEvent);
		return systemEvent;
	}

	protected Date findLastUpdateDate() {
		SystemEventDto latestSuccess = systemEventFacade.getLatestSuccessByType(SystemEventType.FETCH_LAB_MESSAGES);
		Long millis;
		if (latestSuccess != null) {
			millis = determineLatestSuccessMillis(latestSuccess);
		} else {
			logger.info(
				"No previous successful attempt to fetch external lab message could be found. The synchronization date is set to 0 (UNIX milliseconds)");
			millis = 0L;
		}
		return new Date(millis);
	}

	private long determineLatestSuccessMillis(SystemEventDto latestSuccess) {
		String info = latestSuccess.getAdditionalInfo();
		if (info != null) {
			try {
				//parse last synchronization date
				return Long.parseLong(info.replace("Last synchronization date: ", ""));
			} catch (NumberFormatException e) {
				logger.error("Synchronization date could not be parsed for the last successful lab message retrieval. Falling back to start date.");
				return latestSuccess.getStartDate().getTime();
			}
		} else {
			logger.warn("Synchronization date could not be found for the last successful lab message retrieval. Falling back to start date.");
			return latestSuccess.getStartDate().getTime();
		}
	}

	private LabMessageFetchResult getSuccessfulFetchResult(ExternalMessageResult<List<LabMessageDto>> externalMessageResult) {
		if (isEmptyResult(externalMessageResult)) {
			return new LabMessageFetchResult(true, NewMessagesState.NO_NEW_MESSAGES, null);
		} else {
			return new LabMessageFetchResult(true, NewMessagesState.NEW_MESSAGES, null);
		}
	}

	private boolean isEmptyResult(ExternalMessageResult<List<LabMessageDto>> externalMessageResult) {
		return externalMessageResult.getValue() == null || externalMessageResult.getValue().isEmpty();
	}

	@Override
	public boolean exists(String uuid) {
		return labMessageService.exists(uuid);
	}

	@Override
	public List<LabMessageDto> getByReportId(String reportId) {

		if (reportId == null) {
			return Collections.emptyList();
		}

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<LabMessage> cq = cb.createQuery(LabMessage.class);
		Root<LabMessage> from = cq.from(LabMessage.class);

		cq.where(cb.equal(from.get(LabMessage.REPORT_ID), reportId));

		return em.createQuery(cq).getResultList().stream().map(this::toDto).collect(toList());
	}

	@LocalBean
	@Stateless
	public static class LabMessageFacadeEjbLocal extends LabMessageFacadeEjb {

	}
}
