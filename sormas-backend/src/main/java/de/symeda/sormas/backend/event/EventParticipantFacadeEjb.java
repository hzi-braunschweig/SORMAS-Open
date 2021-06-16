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

package de.symeda.sormas.backend.event;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.symeda.sormas.api.Disease;
import de.symeda.sormas.api.Language;
import de.symeda.sormas.api.caze.BirthDateDto;
import de.symeda.sormas.api.caze.BurialInfoDto;
import de.symeda.sormas.api.caze.EmbeddedSampleExportDto;
import de.symeda.sormas.api.event.EventParticipantCriteria;
import de.symeda.sormas.api.event.EventParticipantDto;
import de.symeda.sormas.api.event.EventParticipantExportDto;
import de.symeda.sormas.api.event.EventParticipantFacade;
import de.symeda.sormas.api.event.EventParticipantIndexDto;
import de.symeda.sormas.api.event.EventParticipantListEntryDto;
import de.symeda.sormas.api.event.EventParticipantReferenceDto;
import de.symeda.sormas.api.event.EventReferenceDto;
import de.symeda.sormas.api.event.SimilarEventParticipantDto;
import de.symeda.sormas.api.facility.FacilityHelper;
import de.symeda.sormas.api.feature.FeatureType;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.i18n.Validations;
import de.symeda.sormas.api.location.LocationDto;
import de.symeda.sormas.api.messaging.MessageType;
import de.symeda.sormas.api.person.PersonReferenceDto;
import de.symeda.sormas.api.region.DistrictReferenceDto;
import de.symeda.sormas.api.region.RegionReferenceDto;
import de.symeda.sormas.api.user.UserRight;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.api.utils.SortProperty;
import de.symeda.sormas.api.utils.ValidationRuntimeException;
import de.symeda.sormas.api.vaccinationinfo.VaccinationInfoDto;
import de.symeda.sormas.backend.caze.Case;
import de.symeda.sormas.backend.caze.CaseFacadeEjb;
import de.symeda.sormas.backend.caze.CaseService;
import de.symeda.sormas.backend.common.CriteriaBuilderHelper;
import de.symeda.sormas.backend.common.messaging.MessageSubject;
import de.symeda.sormas.backend.common.messaging.MessagingService;
import de.symeda.sormas.backend.common.messaging.NotificationDeliveryFailedException;
import de.symeda.sormas.backend.contact.Contact;
import de.symeda.sormas.backend.contact.ContactService;
import de.symeda.sormas.backend.feature.FeatureConfigurationFacadeEjb;
import de.symeda.sormas.backend.location.Location;
import de.symeda.sormas.backend.person.Person;
import de.symeda.sormas.backend.person.PersonFacadeEjb;
import de.symeda.sormas.backend.person.PersonQueryContext;
import de.symeda.sormas.backend.person.PersonService;
import de.symeda.sormas.backend.region.Community;
import de.symeda.sormas.backend.region.Country;
import de.symeda.sormas.backend.region.District;
import de.symeda.sormas.backend.region.DistrictFacadeEjb;
import de.symeda.sormas.backend.region.DistrictService;
import de.symeda.sormas.backend.region.Region;
import de.symeda.sormas.backend.region.RegionFacadeEjb;
import de.symeda.sormas.backend.region.RegionService;
import de.symeda.sormas.backend.sample.Sample;
import de.symeda.sormas.backend.sormastosormas.origin.SormasToSormasOriginInfoFacadeEjb;
import de.symeda.sormas.backend.sormastosormas.share.shareinfo.ShareInfoHelper;
import de.symeda.sormas.backend.user.User;
import de.symeda.sormas.backend.user.UserService;
import de.symeda.sormas.backend.util.DtoHelper;
import de.symeda.sormas.backend.util.IterableHelper;
import de.symeda.sormas.backend.util.ModelConstants;
import de.symeda.sormas.backend.util.Pseudonymizer;
import de.symeda.sormas.backend.vaccinationinfo.VaccinationInfo;
import de.symeda.sormas.backend.vaccinationinfo.VaccinationInfoFacadeEjb.VaccinationInfoFacadeEjbLocal;

@Stateless(name = "EventParticipantFacade")
public class EventParticipantFacadeEjb implements EventParticipantFacade {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@PersistenceContext(unitName = ModelConstants.PERSISTENCE_UNIT_NAME)
	private EntityManager em;

	@EJB
	private EventService eventService;
	@EJB
	private EventParticipantService eventParticipantService;
	@EJB
	private PersonService personService;
	@EJB
	private CaseService caseService;
	@EJB
	private UserService userService;
	@EJB
	private EventParticipantJurisdictionChecker eventParticipantJurisdictionChecker;
	@EJB
	private ContactService contactService;
	@EJB
	private RegionService regionService;
	@EJB
	private DistrictService districtService;
	@EJB
	private MessagingService messagingService;
	@EJB
	private EventJurisdictionChecker eventJurisdictionChecker;
	@EJB
	private VaccinationInfoFacadeEjbLocal vaccinationInfoFacade;
	@EJB
	private SormasToSormasOriginInfoFacadeEjb.SormasToSormasOriginInfoFacadeEjbLocal sormasToSormasOriginInfoFacade;
	@EJB
	private FeatureConfigurationFacadeEjb.FeatureConfigurationFacadeEjbLocal featureConfigurationFacade;

	@Override
	public List<EventParticipantDto> getAllEventParticipantsByEventAfter(Date date, String eventUuid) {

		User user = userService.getCurrentUser();
		Event event = eventService.getByUuid(eventUuid);

		if (user == null) {
			return Collections.emptyList();
		}

		if (event == null) {
			return Collections.emptyList();
		}

		Pseudonymizer pseudonymizer = Pseudonymizer.getDefault(userService::hasRight);
		return eventParticipantService.getAllByEventAfter(date, event).stream().map(e -> convertToDto(e, pseudonymizer)).collect(Collectors.toList());
	}

	@Override
	public List<String> getAllActiveUuids() {
		User user = userService.getCurrentUser();

		if (user == null) {
			return Collections.emptyList();
		}

		return eventParticipantService.getAllActiveUuids(user);
	}

	@Override
	public List<EventParticipantDto> getAllActiveEventParticipantsAfter(Date date) {

		User user = userService.getCurrentUser();
		if (user == null) {
			return Collections.emptyList();
		}

		Pseudonymizer pseudonymizer = Pseudonymizer.getDefault(userService::hasRight);
		return eventParticipantService.getAllActiveEventParticipantsAfter(date, user)
			.stream()
			.map(c -> convertToDto(c, pseudonymizer))
			.collect(Collectors.toList());
	}

	@Override
	public List<String> getDeletedUuidsSince(Date since) {

		User user = userService.getCurrentUser();
		if (user == null) {
			return Collections.emptyList();
		}

		List<String> deletedEventParticipants = eventParticipantService.getDeletedUuidsSince(since, user);
		return deletedEventParticipants;
	}

	@Override
	public List<EventParticipantDto> getByUuids(List<String> uuids) {
		Pseudonymizer pseudonymizer = Pseudonymizer.getDefault(userService::hasRight);
		return eventParticipantService.getByUuids(uuids).stream().map(c -> convertToDto(c, pseudonymizer)).collect(Collectors.toList());
	}

	@Override
	public EventParticipantDto getEventParticipantByUuid(String uuid) {
		return convertToDto(eventParticipantService.getByUuid(uuid), Pseudonymizer.getDefault(userService::hasRight));
	}

	@Override
	public EventParticipantDto saveEventParticipant(@Valid EventParticipantDto dto) {
		return saveEventParticipant(dto, true);
	}

	public EventParticipantDto saveEventParticipant(EventParticipantDto dto, boolean checkChangeDate) {
		EventParticipant existingParticipant = dto.getUuid() != null ? eventParticipantService.getByUuid(dto.getUuid()) : null;
		EventParticipantDto existingDto = toDto(existingParticipant);

		User user = userService.getCurrentUser();

		EventReferenceDto eventReferenceDto = dto.getEvent();
		Event event = eventService.getByUuid(eventReferenceDto.getUuid());

		if (!eventJurisdictionChecker.isInJurisdiction(event) && (dto.getRegion() == null || dto.getDistrict() == null)) {
			Region region = user.getRegion();
			dto.setRegion(region != null ? new RegionReferenceDto(region.getUuid(), region.getName(), region.getExternalID()) : null);
			District district = user.getDistrict();
			dto.setDistrict(district != null ? new DistrictReferenceDto(district.getUuid(), district.getName(), district.getExternalID()) : null);
		}

		Pseudonymizer pseudonymizer = Pseudonymizer.getDefault(userService::hasRight);
		restorePseudonymizedDto(dto, existingDto, existingParticipant, pseudonymizer);

		validate(dto);

		EventParticipant entity = fromDto(dto, checkChangeDate);
		eventParticipantService.ensurePersisted(entity);

		if (existingParticipant == null) {
			// The Event Participant is newly created, let's check if the related person is related to other events
			// In that case, let's notify corresponding responsible Users of this relation
			notifyEventResponsibleUsersOfCommonEventParticipant(entity, event);
		}

		return convertToDto(entity, pseudonymizer);
	}

	private void notifyEventResponsibleUsersOfCommonEventParticipant(EventParticipant eventParticipant, Event event) {
		if (!featureConfigurationFacade.isFeatureEnabled(FeatureType.EVENT_PARTICIPANT_RELATED_TO_OTHER_EVENTS_NOTIFICATIONS)) {
			return;
		}

		Date fromDate = Date.from(Instant.now().minus(Duration.ofDays(30)));
		Map<String, Optional<User>> responsibleUserByEventUuid =
			eventService.getAllEventUuidsWithResponsibleUserByPersonAndDiseaseAfterDateForNotification(
				eventParticipant.getPerson().getUuid(),
				event.getDisease(),
				fromDate);
		if (responsibleUserByEventUuid.size() == 1 && responsibleUserByEventUuid.containsKey(event.getUuid())) {
			// it means the event participant is only appearing into the current event
			return;
		}

		for (Map.Entry<String, Optional<User>> entry : responsibleUserByEventUuid.entrySet()) {
			entry.getValue().filter(user -> StringUtils.isNotEmpty(user.getUserEmail())).ifPresent(user -> {
				try {
					messagingService.sendMessage(
						user,
						MessageSubject.EVENT_PARTICIPANT_RELATED_TO_OTHER_EVENTS,
						String.format(
							I18nProperties.getString(MessagingService.CONTENT_EVENT_PARTICIPANT_RELATED_TO_OTHER_EVENTS),
							DataHelper.getShortUuid(eventParticipant.getPerson().getUuid()),
							DataHelper.getShortUuid(eventParticipant.getUuid()),
							DataHelper.getShortUuid(event.getUuid()),
							User.buildCaptionForNotification(event.getResponsibleUser()),
							User.buildCaptionForNotification(userService.getCurrentUser()),
							buildEventListContentForNotification(responsibleUserByEventUuid)),
						MessageType.EMAIL,
						MessageType.SMS);
				} catch (NotificationDeliveryFailedException e) {
					logger.error(
						String.format(
							"NotificationDeliveryFailedException when trying to notify event responsible user about a newly created EventPartipant related to other events. "
								+ "Failed to send " + e.getMessageType() + " to user with UUID %s.",
							user.getUuid()));
				}
			});
		}
	}

	private String buildEventListContentForNotification(Map<String, Optional<User>> responsibleUserByEventUuid) {
		return responsibleUserByEventUuid.entrySet()
			.stream()
			.map(entry -> buildEventListContentForNotification(entry.getKey(), entry.getValue()))
			.collect(Collectors.joining("\n* ", "* ", ""));
	}

	private String buildEventListContentForNotification(String eventUuid, Optional<User> responsibleUser) {
		return String.format(
			I18nProperties.getString(Strings.notificationEventWithResponsibleUserLine),
			DataHelper.getShortUuid(eventUuid),
			User.buildCaptionForNotification(responsibleUser.orElse(null)));
	}

	@Override
	public void validate(EventParticipantDto eventParticipant) throws ValidationRuntimeException {

		// Check whether any required field that does not have a not null constraint in the database is empty
		if (eventParticipant.getPerson() == null) {
			throw new ValidationRuntimeException(I18nProperties.getValidationError(Validations.validPerson));
		}
	}

	@Override
	public void deleteEventParticipant(EventParticipantReferenceDto eventParticipantRef) {

		if (!userService.hasRight(UserRight.EVENTPARTICIPANT_DELETE)) {
			throw new UnsupportedOperationException("Your user is not allowed to delete event participants");
		}

		EventParticipant eventParticipant = eventParticipantService.getByReferenceDto(eventParticipantRef);
		eventParticipantService.delete(eventParticipant);
	}

	@Override
	public EventParticipantDto getByUuid(String uuid) {
		return convertToDto(
			eventParticipantService.getByUuid(uuid),
			Pseudonymizer.getDefault(userService::hasRight, I18nProperties.getCaption(Captions.inaccessibleValue)));
	}

	@Override
	public List<EventParticipantIndexDto> getIndexList(
		EventParticipantCriteria eventParticipantCriteria,
		Integer first,
		Integer max,
		List<SortProperty> sortProperties) {

		if ((eventParticipantCriteria == null) || (eventParticipantCriteria.getEvent() == null && eventParticipantCriteria.getPerson() == null)) {
			return new ArrayList<>(); // Retrieving an index list independent of an event is not possible
		}

		final CriteriaBuilder cb = em.getCriteriaBuilder();
		final CriteriaQuery<EventParticipantIndexDto> cq = cb.createQuery(EventParticipantIndexDto.class);
		final Root<EventParticipant> eventParticipant = cq.from(EventParticipant.class);
		final EventParticipantQueryContext queryContext = new EventParticipantQueryContext(cb, cq, eventParticipant);

		Join<EventParticipant, Person> person = eventParticipant.join(EventParticipant.PERSON, JoinType.LEFT);
		Join<EventParticipant, Case> resultingCase = eventParticipant.join(EventParticipant.RESULTING_CASE, JoinType.LEFT);
		Join<EventParticipant, Event> event = eventParticipant.join(EventParticipant.EVENT, JoinType.LEFT);
		Join<EventParticipant, VaccinationInfo> vaccinationInfoJoin = eventParticipant.join(EventParticipant.VACCINATION_INFO, JoinType.LEFT);
		Join<Object, Object> reportingUser = eventParticipant.join(EventParticipant.REPORTING_USER, JoinType.LEFT);
		final Join<EventParticipant, Sample> samples = eventParticipant.join(EventParticipant.SAMPLES, JoinType.LEFT);

		cq.multiselect(
			eventParticipant.get(EventParticipant.UUID),
			person.get(Person.UUID),
			resultingCase.get(Case.UUID),
			event.get(Event.UUID),
			person.get(Person.FIRST_NAME),
			person.get(Person.LAST_NAME),
			person.get(Person.SEX),
			person.get(Person.APPROXIMATE_AGE),
			person.get(Person.APPROXIMATE_AGE_TYPE),
			eventParticipant.get(EventParticipant.INVOLVEMENT_DESCRIPTION),
			// POSITIVE is the max value of available results
			cb.max(samples.get(Sample.PATHOGEN_TEST_RESULT)),
			// all samples have the same date, but have to be aggregated
			cb.max(samples.get(Sample.SAMPLE_DATE_TIME)),
			vaccinationInfoJoin.get(VaccinationInfo.VACCINATION),
			reportingUser.get(User.UUID));
		cq.groupBy(
			eventParticipant.get(EventParticipant.UUID),
			person.get(Person.UUID),
			resultingCase.get(Case.UUID),
			event.get(Event.UUID),
			person.get(Person.FIRST_NAME),
			person.get(Person.LAST_NAME),
			person.get(Person.SEX),
			person.get(Person.APPROXIMATE_AGE),
			person.get(Person.APPROXIMATE_AGE_TYPE),
			eventParticipant.get(EventParticipant.INVOLVEMENT_DESCRIPTION),
			vaccinationInfoJoin.get(VaccinationInfo.VACCINATION),
			reportingUser.get(User.UUID));

		Subquery<Date> dateSubquery = cq.subquery(Date.class);
		Root<Sample> subRoot = dateSubquery.from(Sample.class);
		final Expression<Date> maxSampleDateTime = cb.<Date> greatest(subRoot.get(Sample.SAMPLE_DATE_TIME));

		Predicate filter = eventParticipantService.buildCriteriaFilter(eventParticipantCriteria, queryContext);
		Predicate pathogenTestResultWhereCondition = CriteriaBuilderHelper.and(
			cb,
			cb.isFalse(subRoot.get(Sample.DELETED)),
			cb.equal(subRoot.get(Sample.ASSOCIATED_EVENT_PARTICIPANT), eventParticipant.get(EventParticipant.ID)));
		if (eventParticipantCriteria.getPathogenTestResult() != null) {
			pathogenTestResultWhereCondition = CriteriaBuilderHelper.and(
				cb,
				filter,
				pathogenTestResultWhereCondition,
				cb.equal(samples.get(Sample.PATHOGEN_TEST_RESULT), eventParticipantCriteria.getPathogenTestResult()));
		}
		final Predicate nullOrMaxSampleDateTime = CriteriaBuilderHelper.or(
			cb,
			cb.isNull(samples.get(Sample.SAMPLE_DATE_TIME)),
			CriteriaBuilderHelper.and(
				cb,
				cb.isFalse(samples.get(Sample.DELETED)),
				cb.equal(samples.get(Sample.SAMPLE_DATE_TIME), dateSubquery.select(maxSampleDateTime).where(pathogenTestResultWhereCondition))));
		cq.where(CriteriaBuilderHelper.and(cb, nullOrMaxSampleDateTime, filter));

		if (sortProperties != null && sortProperties.size() > 0) {
			List<Order> order = new ArrayList<>(sortProperties.size());
			for (SortProperty sortProperty : sortProperties) {
				Expression<?> expression;
				switch (sortProperty.propertyName) {
				case EventParticipantIndexDto.UUID:
				case EventParticipantIndexDto.INVOLVEMENT_DESCRIPTION:
					expression = eventParticipant.get(sortProperty.propertyName);
					break;
				case EventParticipantIndexDto.PERSON_UUID:
					expression = person.get(Person.UUID);
					break;
				case EventParticipantIndexDto.APPROXIMATE_AGE:
				case EventParticipantIndexDto.SEX:
				case EventParticipantIndexDto.LAST_NAME:
				case EventParticipantIndexDto.FIRST_NAME:
					expression = person.get(sortProperty.propertyName);
					break;
				case EventParticipantIndexDto.CASE_UUID:
					expression = resultingCase.get(Case.UUID);
					break;
				case EventParticipantIndexDto.VACCINATION:
					expression = vaccinationInfoJoin.get(VaccinationInfo.VACCINATION);
					break;
				default:
					throw new IllegalArgumentException(sortProperty.propertyName);
				}
				order.add(sortProperty.ascending ? cb.asc(expression) : cb.desc(expression));
			}
			cq.orderBy(order);
		} else {
			cq.orderBy(cb.desc(person.get(Person.LAST_NAME)));
		}

		List<EventParticipantIndexDto> indexList;
		if (first != null && max != null) {
			indexList = em.createQuery(cq).setFirstResult(first).setMaxResults(max).getResultList();
		} else {
			indexList = em.createQuery(cq).getResultList();
		}

		if (!indexList.isEmpty()) {
			Map<String, Long> eventParticipantContactCount = getContactCountPerEventParticipant(
				indexList.stream().map(EventParticipantIndexDto::getUuid).collect(Collectors.toList()),
				eventParticipantCriteria);

			for (EventParticipantIndexDto eventParticipantIndexDto : indexList) {
				Optional.ofNullable(eventParticipantContactCount.get(eventParticipantIndexDto.getUuid()))
					.ifPresent(eventParticipantIndexDto::setContactCount);
			}
		}

		Pseudonymizer pseudonymizer = Pseudonymizer.getDefault(userService::hasRight, I18nProperties.getCaption(Captions.inaccessibleValue));
		pseudonymizer.pseudonymizeDtoCollection(
			EventParticipantIndexDto.class,
			indexList,
			p -> eventParticipantJurisdictionChecker.isPseudonymized(p.getUuid()),
			null);

		return indexList;
	}

	@Override
	public List<EventParticipantListEntryDto> getListEntries(EventParticipantCriteria eventParticipantCriteria, Integer first, Integer max) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<EventParticipantListEntryDto> cq = cb.createQuery(EventParticipantListEntryDto.class);
		Root<EventParticipant> eventParticipant = cq.from(EventParticipant.class);

		Join<EventParticipant, Event> event = eventParticipant.join(EventParticipant.EVENT, JoinType.LEFT);

		cq.multiselect(
			eventParticipant.get(EventParticipant.UUID),
			event.get(Event.UUID),
			event.get(Event.EVENT_STATUS),
			event.get(Event.DISEASE),
			event.get(Event.EVENT_TITLE));

		Predicate filter = CriteriaBuilderHelper.and(
			cb,
			eventParticipantService.buildCriteriaFilter(eventParticipantCriteria, new EventParticipantQueryContext(cb, cq, eventParticipant)),
			cb.isFalse(event.get(Event.DELETED)));

		cq.where(filter);
		cq.orderBy(cb.desc(eventParticipant.get(EventParticipant.CREATION_DATE)));

		List<EventParticipantListEntryDto> result;
		if (first != null && max != null) {
			result = em.createQuery(cq).setFirstResult(first).setMaxResults(max).getResultList();
		} else {
			result = em.createQuery(cq).getResultList();
		}

		Pseudonymizer pseudonymizer = Pseudonymizer.getDefault(userService::hasRight, I18nProperties.getCaption(Captions.inaccessibleValue));
		pseudonymizer.pseudonymizeDtoCollection(
			EventParticipantListEntryDto.class,
			result,
			p -> eventParticipantJurisdictionChecker.isPseudonymized(p.getUuid()),
			null);

		return result;
	}

	@Override
	public List<EventParticipantExportDto> getExportList(
		EventParticipantCriteria eventParticipantCriteria,
		Collection<String> selectedRows,
		int first,
		int max,
		Language userLanguage) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<EventParticipantExportDto> cq = cb.createQuery(EventParticipantExportDto.class);
		Root<EventParticipant> eventParticipant = cq.from(EventParticipant.class);
		EventParticipantQueryContext eventParticipantQueryContext = new EventParticipantQueryContext(cb, cq, eventParticipant);

		Join<EventParticipant, Person> person = eventParticipant.join(EventParticipant.PERSON, JoinType.LEFT);

		PersonQueryContext personQueryContext = new PersonQueryContext(cb, cq, person);

		Join<Person, Location> address = person.join(Person.ADDRESS);
		Join<Person, Country> birthCountry = person.join(Person.BIRTH_COUNTRY, JoinType.LEFT);
		Join<Person, Country> citizenship = person.join(Person.CITIZENSHIP, JoinType.LEFT);

		Join<EventParticipant, Event> event = eventParticipant.join(EventParticipant.EVENT, JoinType.LEFT);
		Join<Event, Location> eventLocation = event.join(Event.EVENT_LOCATION, JoinType.LEFT);

		Join<EventParticipant, Case> resultingCase = eventParticipant.join(EventParticipant.RESULTING_CASE, JoinType.LEFT);
		Join<EventParticipant, VaccinationInfo> vaccinationInfo = eventParticipant.join(EventParticipant.VACCINATION_INFO, JoinType.LEFT);

		cq.multiselect(
			eventParticipant.get(EventParticipant.ID),
			person.get(Person.ID),
			person.get(Person.UUID),
			eventParticipant.get(EventParticipant.UUID),
			person.get(Person.NATIONAL_HEALTH_ID),
			person.get(Location.ID),
			eventParticipant.join(EventParticipant.REPORTING_USER, JoinType.LEFT).get(User.UUID),

			event.get(Event.UUID),

			event.get(Event.EVENT_STATUS),
			event.get(Event.EVENT_INVESTIGATION_STATUS),
			event.get(Event.DISEASE),
			event.get(Event.TYPE_OF_PLACE),
			event.get(Event.START_DATE),
			event.get(Event.END_DATE),
			event.get(Event.EVENT_TITLE),
			event.get(Event.EVENT_DESC),
			eventLocation.join(Location.REGION, JoinType.LEFT).get(Region.NAME),
			eventLocation.join(Location.DISTRICT, JoinType.LEFT).get(District.NAME),
			eventLocation.join(Location.COMMUNITY, JoinType.LEFT).get(Community.NAME),
			eventLocation.get(Location.CITY),
			eventLocation.get(Location.STREET),
			eventLocation.get(Location.HOUSE_NUMBER),

			person.get(Person.FIRST_NAME),
			person.get(Person.LAST_NAME),
			person.get(Person.SALUTATION),
			person.get(Person.OTHER_SALUTATION),
			person.get(Person.SEX),
			eventParticipant.get(EventParticipant.INVOLVEMENT_DESCRIPTION),
			person.get(Person.APPROXIMATE_AGE),
			person.get(Person.APPROXIMATE_AGE_TYPE),
			person.get(Person.BIRTHDATE_DD),
			person.get(Person.BIRTHDATE_MM),
			person.get(Person.BIRTHDATE_YYYY),
			person.get(Person.PRESENT_CONDITION),
			person.get(Person.DEATH_DATE),
			person.get(Person.BURIAL_DATE),
			person.get(Person.BURIAL_CONDUCTOR),
			person.get(Person.BURIAL_PLACE_DESCRIPTION),

			address.join(Location.REGION, JoinType.LEFT).get(Region.NAME),
			address.join(Location.DISTRICT, JoinType.LEFT).get(District.NAME),
			address.join(Location.COMMUNITY, JoinType.LEFT).get(Community.NAME),
			address.get(Location.CITY),
			address.get(Location.STREET),
			address.get(Location.HOUSE_NUMBER),
			address.get(Location.ADDITIONAL_INFORMATION),
			address.get(Location.POSTAL_CODE),
			personQueryContext.getSubqueryExpression(PersonQueryContext.PERSON_PHONE_SUBQUERY),
			personQueryContext.getSubqueryExpression(PersonQueryContext.PERSON_EMAIL_SUBQUERY),

			resultingCase.get(Case.UUID),

			person.get(Person.BIRTH_NAME),
			birthCountry.get(Country.ISO_CODE),
			birthCountry.get(Country.DEFAULT_NAME),
			citizenship.get(Country.ISO_CODE),
			citizenship.get(Country.DEFAULT_NAME),

			vaccinationInfo.get(VaccinationInfo.VACCINATION),
			vaccinationInfo.get(VaccinationInfo.VACCINATION_DOSES),
			vaccinationInfo.get(VaccinationInfo.VACCINATION_INFO_SOURCE),
			vaccinationInfo.get(VaccinationInfo.FIRST_VACCINATION_DATE),
			vaccinationInfo.get(VaccinationInfo.LAST_VACCINATION_DATE),
			vaccinationInfo.get(VaccinationInfo.VACCINE_NAME),
			vaccinationInfo.get(VaccinationInfo.OTHER_VACCINE_NAME),
			vaccinationInfo.get(VaccinationInfo.VACCINE_MANUFACTURER),
			vaccinationInfo.get(VaccinationInfo.OTHER_VACCINE_MANUFACTURER),
			vaccinationInfo.get(VaccinationInfo.VACCINE_INN),
			vaccinationInfo.get(VaccinationInfo.VACCINE_BATCH_NUMBER),
			vaccinationInfo.get(VaccinationInfo.VACCINE_UNII_CODE),
			vaccinationInfo.get(VaccinationInfo.VACCINE_ATC_CODE));

		Predicate filter = eventParticipantService.buildCriteriaFilter(eventParticipantCriteria, eventParticipantQueryContext);
		filter = CriteriaBuilderHelper.andInValues(selectedRows, filter, cb, eventParticipant.get(EventParticipant.UUID));
		cq.where(filter);

		List<EventParticipantExportDto> eventParticipantResultList = em.createQuery(cq).setFirstResult(first).setMaxResults(max).getResultList();

		if (!eventParticipantResultList.isEmpty()) {
			Map<String, Long> eventParticipantContactCount = getContactCountPerEventParticipant(
				eventParticipantResultList.stream().map(EventParticipantExportDto::getEventParticipantUuid).collect(Collectors.toList()),
				eventParticipantCriteria);

			Map<Long, Location> personAddresses = null;
			List<Location> personAddressesList = null;
			CriteriaQuery<Location> personAddressesCq = cb.createQuery(Location.class);
			Root<Location> personAddressesRoot = personAddressesCq.from(Location.class);
			Expression<String> personAddressesIdsExpr = personAddressesRoot.get(Location.ID);
			personAddressesCq.where(
				personAddressesIdsExpr
					.in(eventParticipantResultList.stream().map(EventParticipantExportDto::getPersonAddressId).collect(Collectors.toList())));
			personAddressesList = em.createQuery(personAddressesCq).setHint(ModelConstants.HINT_HIBERNATE_READ_ONLY, true).getResultList();
			personAddresses = personAddressesList.stream().collect(Collectors.toMap(Location::getId, Function.identity()));

			Map<Long, List<Sample>> samples = null;
			List<Sample> samplesList = null;
			CriteriaQuery<Sample> samplesCq = cb.createQuery(Sample.class);
			Root<Sample> samplesRoot = samplesCq.from(Sample.class);
			Join<Sample, EventParticipant> samplesEventParticipantJoin = samplesRoot.join(Sample.ASSOCIATED_EVENT_PARTICIPANT, JoinType.LEFT);
			Expression<String> eventParticipantIdsExpr = samplesEventParticipantJoin.get(EventParticipant.ID);
			samplesCq.where(
				eventParticipantIdsExpr.in(eventParticipantResultList.stream().map(EventParticipantExportDto::getId).collect(Collectors.toList())));
			samplesList = em.createQuery(samplesCq).setHint(ModelConstants.HINT_HIBERNATE_READ_ONLY, true).getResultList();
			samples = samplesList.stream().collect(Collectors.groupingBy(s -> s.getAssociatedEventParticipant().getId()));

			Pseudonymizer pseudonymizer = Pseudonymizer.getDefault(userService::hasRight, I18nProperties.getCaption(Captions.inaccessibleValue));
			for (EventParticipantExportDto exportDto : eventParticipantResultList) {
//				final boolean inJurisdiction = eventParticipantJurisdictionChecker.isInJurisdictionOrOwned(exportDto.getJurisdiction());
				final boolean inJurisdiction = eventParticipantJurisdictionChecker.isPseudonymized(exportDto.getEventParticipantUuid());

				if (personAddresses != null) {
					Optional.ofNullable(personAddresses.get(exportDto.getPersonAddressId()))
						.ifPresent(personAddress -> exportDto.setAddressGpsCoordinates(personAddress.buildGpsCoordinatesCaption()));
				}

				if (samples != null) {
					Optional.ofNullable(samples.get(exportDto.getId())).ifPresent(eventParticipantSamples -> {
						int count = 0;
						for (Sample sample : eventParticipantSamples) {
							EmbeddedSampleExportDto sampleDto = new EmbeddedSampleExportDto(
								sample.getUuid(),
								sample.getSampleDateTime(),
								sample.getLab() != null
									? FacilityHelper.buildFacilityString(sample.getLab().getUuid(), sample.getLab().getName(), sample.getLabDetails())
									: null,
								sample.getPathogenTestResult());

							exportDto.addEventParticipantSample(sampleDto);
						}
					});
				}

				Optional.ofNullable(eventParticipantContactCount.get(exportDto.getEventParticipantUuid())).ifPresent(exportDto::setContactCount);

				pseudonymizer.pseudonymizeDto(EventParticipantExportDto.class, exportDto, inJurisdiction, (c) -> {
					pseudonymizer.pseudonymizeDto(BirthDateDto.class, c.getBirthdate(), inJurisdiction, null);
					pseudonymizer.pseudonymizeDtoCollection(EmbeddedSampleExportDto.class, c.getEventParticipantSamples(), s -> inJurisdiction, null);
					pseudonymizer.pseudonymizeDto(BurialInfoDto.class, c.getBurialInfo(), inJurisdiction, null);
				});
			}
		}

		return eventParticipantResultList;
	}

	@Override
	public long count(EventParticipantCriteria eventParticipantCriteria) {
		if (eventParticipantCriteria == null || eventParticipantCriteria.getEvent() == null) {
			return 0L; // Retrieving a count independent of an event is not possible
		}

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<EventParticipant> root = cq.from(EventParticipant.class);
		Predicate filter = eventParticipantService.buildCriteriaFilter(eventParticipantCriteria, new EventParticipantQueryContext(cb, cq, root));
		cq.where(filter);
		cq.select(cb.count(root));
		return em.createQuery(cq).getSingleResult();
	}

	@Override
	public Map<String, Long> getContactCountPerEventParticipant(
		List<String> eventParticipantUuids,
		EventParticipantCriteria eventParticipantCriteria) {

		Map<String, Long> contactCountMap = new HashMap<>();

		IterableHelper.executeBatched(eventParticipantUuids, ModelConstants.PARAMETER_LIMIT, batchedEventParticipantUuids -> {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<Object[]> contactCount = cb.createQuery(Object[].class);

			Root<EventParticipant> epRoot = contactCount.from(EventParticipant.class);
			Root<Contact> contactRoot = contactCount.from(Contact.class);

			Predicate participantPersonEqualsContactPerson = cb.equal(epRoot.get(EventParticipant.PERSON), contactRoot.get(Contact.PERSON));
			Predicate notDeleted = cb.isFalse(epRoot.get(EventParticipant.DELETED));
			Predicate contactNotDeleted = cb.isFalse(contactRoot.get(Contact.DELETED));
			Predicate isInEvent = epRoot.get(EventParticipant.UUID).in(batchedEventParticipantUuids);

			if (Boolean.TRUE.equals(eventParticipantCriteria.getOnlyCountContactsWithSourceCaseInEvent())) {
				Subquery<EventParticipant> sourceCaseSubquery = contactCount.subquery(EventParticipant.class);
				Root<EventParticipant> epr2 = sourceCaseSubquery.from(EventParticipant.class);
				sourceCaseSubquery.select(epr2);
				sourceCaseSubquery.where(
					cb.equal(epr2.get(EventParticipant.RESULTING_CASE), contactRoot.get(Contact.CAZE)),
					cb.equal(epr2.get(EventParticipant.EVENT), epRoot.get(EventParticipant.EVENT)));

				contactCount.multiselect(
					epRoot.get(EventParticipant.UUID),
					cb.sum(cb.selectCase().when(cb.exists(sourceCaseSubquery), 1).otherwise(0).as(Long.class)));
			} else {
				contactCount.multiselect(epRoot.get(EventParticipant.UUID), cb.count(epRoot));
			}
			contactCount.where(participantPersonEqualsContactPerson, notDeleted, contactNotDeleted, isInEvent);
			contactCount.groupBy(epRoot.get(EventParticipant.UUID));

			List<Object[]> resultList = em.createQuery(contactCount).getResultList();
			resultList.forEach(r -> contactCountMap.put((String) r[0], (Long) r[1]));
		});

		return contactCountMap;
	}

	@Override
	public boolean exists(String uuid) {
		return eventParticipantService.exists(uuid);
	}

	@Override
	public EventParticipantReferenceDto getReferenceByUuid(String uuid) {
		EventParticipant eventParticipant = eventParticipantService.getByUuid(uuid);
		return new EventParticipantReferenceDto(eventParticipant.getUuid());
	}

	@Override
	public EventParticipantReferenceDto getReferenceByEventAndPerson(String eventUuid, String personUuid) {
		return Optional.ofNullable(eventParticipantService.getByEventAndPerson(eventUuid, personUuid))
			.map(eventParticipant -> new EventParticipantReferenceDto(eventParticipant.getUuid()))
			.orElse(null);
	}

	@Override
	public boolean isEventParticipantEditAllowed(String uuid) {
		EventParticipant eventParticipant = eventParticipantService.getByUuid(uuid);

		return eventParticipantService.isEventParticiapntEditAllowed(eventParticipant);
	}

	@Override
	public EventParticipantDto getFirst(EventParticipantCriteria criteria) {

		if (criteria.getEvent() == null) {
			return null;
		}

		return eventParticipantService.getFirst(criteria)
			.map(e -> convertToDto(e, Pseudonymizer.getDefault(userService::hasRight, I18nProperties.getCaption(Captions.inaccessibleValue))))
			.orElse(null);
	}

	public EventParticipant fromDto(@NotNull EventParticipantDto source, boolean checkChangeDate) {

		EventParticipant target =
			DtoHelper.fillOrBuildEntity(source, eventParticipantService.getByUuid(source.getUuid()), EventParticipant::new, checkChangeDate);

		if (source.getReportingUser() != null) {
			target.setReportingUser(userService.getByReferenceDto(source.getReportingUser()));
		}

		target.setEvent(eventService.getByReferenceDto(source.getEvent()));
		target.setPerson(personService.getByUuid(source.getPerson().getUuid()));
		target.setInvolvementDescription(source.getInvolvementDescription());
		target.setResultingCase(caseService.getByReferenceDto(source.getResultingCase()));
		target.setRegion(regionService.getByReferenceDto(source.getRegion()));
		target.setDistrict(districtService.getByReferenceDto(source.getDistrict()));

		// create new vaccination info in case it is created in the mobile app
		// TODO [vaccination info] no VaccinationInfoDto.build() will be needed after integrating vaccination info into the app
		VaccinationInfoDto vaccinationInfo = source.getVaccinationInfo();
		if (vaccinationInfo == null && target.getVaccinationInfo() == null) {
			vaccinationInfo = VaccinationInfoDto.build();
		}
		if (vaccinationInfo != null) {
			target.setVaccinationInfo(vaccinationInfoFacade.fromDto(vaccinationInfo, checkChangeDate));
		}

		if (source.getSormasToSormasOriginInfo() != null) {
			target.setSormasToSormasOriginInfo(sormasToSormasOriginInfoFacade.fromDto(source.getSormasToSormasOriginInfo(), checkChangeDate));
		}

		return target;
	}

	public EventParticipantDto convertToDto(EventParticipant source, Pseudonymizer pseudonymizer) {
		EventParticipantDto dto = toDto(source);
		pseudonymizeDto(source, dto, pseudonymizer);

		return dto;
	}

	private void pseudonymizeDto(EventParticipant source, EventParticipantDto dto, Pseudonymizer pseudonymizer) {

		if (source != null) {
			validate(dto);

			boolean inJurisdiction = eventParticipantJurisdictionChecker.isPseudonymized(source);

			pseudonymizer.pseudonymizeDto(EventParticipantDto.class, dto, inJurisdiction, null);
			dto.getPerson().getAddresses().forEach(l -> pseudonymizer.pseudonymizeDto(LocationDto.class, l, inJurisdiction, null));
		}
	}

	private void restorePseudonymizedDto(
		EventParticipantDto dto,
		EventParticipantDto originalDto,
		EventParticipant originalEventParticipant,
		Pseudonymizer pseudonymizer) {

		if (originalDto != null) {
			pseudonymizer.restorePseudonymizedValues(
				EventParticipantDto.class,
				dto,
				originalDto,
				eventParticipantJurisdictionChecker.isPseudonymized(originalEventParticipant));
		}
	}

	public static EventParticipantReferenceDto toReferenceDto(EventParticipant entity) {

		if (entity == null) {
			return null;
		}

		Person person = entity.getPerson();

		return new EventParticipantReferenceDto(entity.getUuid(), person.getFirstName(), person.getFirstName());
	}

	public static EventParticipantDto toDto(EventParticipant source) {

		if (source == null) {
			return null;
		}
		EventParticipantDto target = new EventParticipantDto();
		DtoHelper.fillDto(target, source);

		if (source.getReportingUser() != null) {
			target.setReportingUser(source.getReportingUser().toReference());
		}

		target.setEvent(EventFacadeEjb.toReferenceDto(source.getEvent()));
		target.setPerson(PersonFacadeEjb.toDto(source.getPerson()));
		target.setInvolvementDescription(source.getInvolvementDescription());
		target.setResultingCase(CaseFacadeEjb.toReferenceDto(source.getResultingCase()));
		target.setRegion(RegionFacadeEjb.toReferenceDto(source.getRegion()));
		target.setDistrict(DistrictFacadeEjb.toReferenceDto(source.getDistrict()));
		target.setVaccinationInfo(VaccinationInfoFacadeEjbLocal.toDto(source.getVaccinationInfo()));

		target.setSormasToSormasOriginInfo(SormasToSormasOriginInfoFacadeEjb.toDto(source.getSormasToSormasOriginInfo()));
		target.setOwnershipHandedOver(source.getShareInfoEventParticipants().stream().anyMatch(ShareInfoHelper::isOwnerShipHandedOver));

		return target;
	}

	@LocalBean
	@Stateless
	public static class EventParticipantFacadeEjbLocal extends EventParticipantFacadeEjb {

	}

	@Override
	public List<EventParticipantDto> getAllActiveEventParticipantsByEvent(String eventUuid) {

		User user = userService.getCurrentUser();
		Event event = eventService.getByUuid(eventUuid);

		if (user == null) {
			return Collections.emptyList();
		}

		if (event == null) {
			return Collections.emptyList();
		}

		return eventParticipantService.getAllActiveByEvent(event).stream().map(e -> toDto(e)).collect(Collectors.toList());
	}

	@Override
	public List<EventParticipantDto> getByEventUuids(List<String> eventUuids) {
		Pseudonymizer pseudonymizer = Pseudonymizer.getDefault(userService::hasRight);
		return eventParticipantService.getByEventUuids(eventUuids).stream().map(e -> convertToDto(e, pseudonymizer)).collect(Collectors.toList());
	}

	@Override
	public List<SimilarEventParticipantDto> getMatchingEventParticipants(EventParticipantCriteria criteria) {

		final CriteriaBuilder cb = em.getCriteriaBuilder();
		final CriteriaQuery<SimilarEventParticipantDto> cq = cb.createQuery(SimilarEventParticipantDto.class);
		final Root<EventParticipant> eventParticipantRoot = cq.from(EventParticipant.class);
		Join<Object, Object> personJoin = eventParticipantRoot.join(EventParticipant.PERSON, JoinType.LEFT);
		Join<Object, Object> eventJoin = eventParticipantRoot.join(EventParticipant.EVENT, JoinType.LEFT);

		cq.multiselect(
			eventParticipantRoot.get(EventParticipant.UUID),
			personJoin.get(Person.FIRST_NAME),
			personJoin.get(Person.LAST_NAME),
			eventParticipantRoot.get(EventParticipant.INVOLVEMENT_DESCRIPTION),
			eventJoin.get(Event.UUID),
			eventJoin.get(Event.EVENT_STATUS),
			eventJoin.get(Event.EVENT_TITLE),
			eventJoin.get(Event.START_DATE));
		cq.groupBy(
			eventParticipantRoot.get(EventParticipant.UUID),
			personJoin.get(Person.FIRST_NAME),
			personJoin.get(Person.LAST_NAME),
			eventParticipantRoot.get(EventParticipant.INVOLVEMENT_DESCRIPTION),
			eventJoin.get(Event.UUID),
			eventJoin.get(Event.EVENT_STATUS),
			eventJoin.get(Event.EVENT_TITLE),
			eventJoin.get(Event.START_DATE));

		final Predicate defaultFilter = eventParticipantService.createDefaultFilter(cb, eventParticipantRoot);
		final Predicate userFilter = eventParticipantService.createUserFilter(cb, cq, eventParticipantRoot);

		final PersonReferenceDto person = criteria.getPerson();
		final Predicate samePersonFilter = person != null ? cb.equal(personJoin.get(Person.UUID), person.getUuid()) : null;

		final Disease disease = criteria.getDisease();
		final Predicate diseaseFilter = disease != null ? cb.equal(eventJoin.get(Event.DISEASE), disease) : null;

		final Date relevantDate = criteria.getRelevantDate();
		final Predicate relevantDateFilter = CriteriaBuilderHelper.or(
			cb,
			contactService.recentDateFilter(cb, relevantDate, eventJoin.get(Event.START_DATE), 30),
			contactService.recentDateFilter(cb, relevantDate, eventJoin.get(Event.END_DATE), 30),
			contactService.recentDateFilter(cb, relevantDate, eventJoin.get(Event.REPORT_DATE_TIME), 30));

		final Predicate noResulingCaseFilter =
			Boolean.TRUE.equals(criteria.getNoResultingCase()) ? cb.isNull(eventParticipantRoot.get(EventParticipant.RESULTING_CASE)) : null;

		cq.where(CriteriaBuilderHelper.and(cb, defaultFilter, userFilter, samePersonFilter, diseaseFilter, relevantDateFilter, noResulingCaseFilter));

		List<SimilarEventParticipantDto> participants = em.createQuery(cq).getResultList();

		Pseudonymizer pseudonymizer = Pseudonymizer.getDefault(userService::hasRight);
		pseudonymizer.pseudonymizeDtoCollection(
			SimilarEventParticipantDto.class,
			participants,
			p -> eventParticipantJurisdictionChecker.isPseudonymized(p.getUuid()),
			null);

		if (Boolean.TRUE.equals(criteria.getExcludePseudonymized())) {
			participants = participants.stream().filter(e -> !e.isPseudonymized()).collect(Collectors.toList());
		}

		return participants;
	}

	@Override
	public List<EventParticipantDto> getByPersonUuids(List<String> personUuids) {
		return eventParticipantService.getByPersonUuids(personUuids).stream().map(EventParticipantFacadeEjb::toDto).collect(Collectors.toList());
	}
}
