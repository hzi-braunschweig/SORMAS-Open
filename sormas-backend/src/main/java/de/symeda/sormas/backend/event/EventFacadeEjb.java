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
package de.symeda.sormas.backend.event;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import de.symeda.sormas.api.Disease;
import de.symeda.sormas.api.caze.CaseOutcome;
import de.symeda.sormas.api.common.Page;
import de.symeda.sormas.api.event.EventCriteria;
import de.symeda.sormas.api.event.EventDto;
import de.symeda.sormas.api.event.EventExportDto;
import de.symeda.sormas.api.event.EventFacade;
import de.symeda.sormas.api.event.EventGroupReferenceDto;
import de.symeda.sormas.api.event.EventGroupsIndexDto;
import de.symeda.sormas.api.event.EventIndexDto;
import de.symeda.sormas.api.event.EventReferenceDto;
import de.symeda.sormas.api.event.EventStatus;
import de.symeda.sormas.api.externaldata.ExternalDataDto;
import de.symeda.sormas.api.externaldata.ExternalDataUpdateException;
import de.symeda.sormas.api.externalsurveillancetool.ExternalSurveillanceToolException;
import de.symeda.sormas.api.facility.FacilityDto;
import de.symeda.sormas.api.feature.FeatureType;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Validations;
import de.symeda.sormas.api.location.LocationDto;
import de.symeda.sormas.api.region.RegionReferenceDto;
import de.symeda.sormas.api.user.UserRight;
import de.symeda.sormas.api.utils.SortProperty;
import de.symeda.sormas.api.utils.ValidationRuntimeException;
import de.symeda.sormas.backend.caze.Case;
import de.symeda.sormas.backend.common.AbstractDomainObject;
import de.symeda.sormas.backend.common.CriteriaBuilderHelper;
import de.symeda.sormas.backend.contact.Contact;
import de.symeda.sormas.backend.externalsurveillancetool.ExternalSurveillanceToolGatewayFacadeEjb.ExternalSurveillanceToolGatewayFacadeEjbLocal;
import de.symeda.sormas.backend.facility.FacilityFacadeEjb.FacilityFacadeEjbLocal;
import de.symeda.sormas.backend.feature.FeatureConfigurationFacadeEjb.FeatureConfigurationFacadeEjbLocal;
import de.symeda.sormas.backend.location.Location;
import de.symeda.sormas.backend.location.LocationFacadeEjb;
import de.symeda.sormas.backend.location.LocationFacadeEjb.LocationFacadeEjbLocal;
import de.symeda.sormas.backend.region.Community;
import de.symeda.sormas.backend.region.CommunityFacadeEjb.CommunityFacadeEjbLocal;
import de.symeda.sormas.backend.region.District;
import de.symeda.sormas.backend.region.DistrictFacadeEjb.DistrictFacadeEjbLocal;
import de.symeda.sormas.backend.region.Region;
import de.symeda.sormas.backend.share.ExternalShareInfoCountAndLatestDate;
import de.symeda.sormas.backend.share.ExternalShareInfoService;
import de.symeda.sormas.backend.sormastosormas.origin.SormasToSormasOriginInfoFacadeEjb;
import de.symeda.sormas.backend.sormastosormas.origin.SormasToSormasOriginInfoFacadeEjb.SormasToSormasOriginInfoFacadeEjbLocal;
import de.symeda.sormas.backend.sormastosormas.share.shareinfo.ShareInfoEvent;
import de.symeda.sormas.backend.sormastosormas.share.shareinfo.ShareInfoHelper;
import de.symeda.sormas.backend.sormastosormas.share.shareinfo.SormasToSormasShareInfo;
import de.symeda.sormas.backend.user.User;
import de.symeda.sormas.backend.user.UserFacadeEjb;
import de.symeda.sormas.backend.user.UserService;
import de.symeda.sormas.backend.util.DtoHelper;
import de.symeda.sormas.backend.util.IterableHelper;
import de.symeda.sormas.backend.util.ModelConstants;
import de.symeda.sormas.backend.util.Pseudonymizer;

@Stateless(name = "EventFacade")
public class EventFacadeEjb implements EventFacade {

	@PersistenceContext(unitName = ModelConstants.PERSISTENCE_UNIT_NAME)
	private EntityManager em;

	@EJB
	private UserService userService;
	@EJB
	private EventService eventService;
	@EJB
	private EventGroupService eventGroupService;
	@EJB
	private LocationFacadeEjbLocal locationFacade;
	@EJB
	private EventJurisdictionChecker eventJurisdictionChecker;
	@EJB
	private SormasToSormasOriginInfoFacadeEjbLocal sormasToSormasOriginInfoFacade;
	@EJB
	private DistrictFacadeEjbLocal districtFacade;
	@EJB
	private CommunityFacadeEjbLocal communityFacade;
	@EJB
	private FacilityFacadeEjbLocal facilityFacade;
	@EJB
	private FeatureConfigurationFacadeEjbLocal featureConfigurationFacade;
	@EJB
	private ExternalSurveillanceToolGatewayFacadeEjbLocal externalSurveillanceToolFacade;
	@EJB
	private ExternalShareInfoService externalShareInfoService;

	@Override
	public List<String> getAllActiveUuids() {

		User user = userService.getCurrentUser();
		if (user == null) {
			return Collections.emptyList();
		}

		return eventService.getAllActiveUuids();
	}

	@Override
	public List<EventDto> getAllActiveEventsAfter(Date date) {

		User user = userService.getCurrentUser();
		if (user == null) {
			return Collections.emptyList();
		}

		Pseudonymizer pseudonymizer = Pseudonymizer.getDefault(userService::hasRight);
		return eventService.getAllActiveEventsAfter(date).stream().map(e -> convertToDto(e, pseudonymizer)).collect(Collectors.toList());
	}

	@Override
	public List<EventDto> getByUuids(List<String> uuids) {
		Pseudonymizer pseudonymizer = Pseudonymizer.getDefault(userService::hasRight);
		return eventService.getByUuids(uuids).stream().map(e -> convertToDto(e, pseudonymizer)).collect(Collectors.toList());
	}

	@Override
	public List<String> getDeletedUuidsSince(Date since) {

		User user = userService.getCurrentUser();
		if (user == null) {
			return Collections.emptyList();
		}

		return eventService.getDeletedUuidsSince(since);
	}

	public Map<Disease, Long> getEventCountByDisease(EventCriteria eventCriteria) {

		return eventService.getEventCountByDisease(eventCriteria);
	}

	@Override
	public EventDto getEventByUuid(String uuid) {
		return convertToDto(eventService.getByUuid(uuid), Pseudonymizer.getDefault(userService::hasRight));
	}

	@Override
	public EventReferenceDto getReferenceByUuid(String uuid) {
		return toReferenceDto(eventService.getByUuid(uuid));
	}

	@Override
	public EventReferenceDto getReferenceByEventParticipant(String uuid) {
		return toReferenceDto(eventService.getEventReferenceByEventParticipant(uuid));
	}

	@Override
	public EventDto saveEvent(@Valid @NotNull EventDto dto) {
		return saveEvent(dto, true);
	}

	public EventDto saveEvent(@NotNull EventDto dto, boolean checkChangeDate) {

		Pseudonymizer pseudonymizer = Pseudonymizer.getDefault(userService::hasRight);
		Event existingEvent = dto.getUuid() != null ? eventService.getByUuid(dto.getUuid()) : null;
		EventDto existingDto = toDto(existingEvent);

		restorePseudonymizedDto(dto, existingEvent, existingDto, pseudonymizer);

		if (dto.getReportDateTime() == null) {
			throw new ValidationRuntimeException(I18nProperties.getValidationError(Validations.validReportDateTime));
		}

		Event event = fromDto(dto, checkChangeDate);
		eventService.ensurePersisted(event);

		return convertToDto(event, pseudonymizer);
	}

	@Override
	public void deleteEvent(String eventUuid) throws ExternalSurveillanceToolException {

		if (!userService.hasRight(UserRight.EVENT_DELETE)) {
			throw new UnsupportedOperationException("User " + userService.getCurrentUser().getUuid() + " is not allowed to delete events.");
		}

		Event event = eventService.getByUuid(eventUuid);

		if (event.getEventStatus() == EventStatus.CLUSTER
			&& externalSurveillanceToolFacade.isFeatureEnabled()
			&& externalShareInfoService.isEventShared(event.getId())) {
			externalSurveillanceToolFacade.deleteEvents(Collections.singletonList(toDto(event)));
		}

		eventService.delete(event);
	}

	@Override
	public long count(EventCriteria eventCriteria) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<Event> event = cq.from(Event.class);

		Predicate filter = null;

		if (eventCriteria != null) {
			if (eventCriteria.getUserFilterIncluded()) {
				filter = eventService.createUserFilter(cb, cq, event);
			}

			Predicate criteriaFilter = eventService.buildCriteriaFilter(eventCriteria, new EventQueryContext(cb, cq, event));
			filter = CriteriaBuilderHelper.and(cb, filter, criteriaFilter);
		}

		if (filter != null) {
			cq.where(filter);
		}

		cq.select(cb.countDistinct(event));
		return em.createQuery(cq).getSingleResult();
	}

	@Override
	public List<EventIndexDto> getIndexList(EventCriteria eventCriteria, Integer first, Integer max, List<SortProperty> sortProperties) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<EventIndexDto> cq = cb.createQuery(EventIndexDto.class);
		Root<Event> event = cq.from(Event.class);
		Join<Event, Location> location = event.join(Event.EVENT_LOCATION, JoinType.LEFT);
		Join<Location, Region> region = location.join(Location.REGION, JoinType.LEFT);
		Join<Location, District> district = location.join(Location.DISTRICT, JoinType.LEFT);
		Join<Location, Community> community = location.join(Location.COMMUNITY, JoinType.LEFT);
		Join<Event, User> reportingUser = event.join(Event.REPORTING_USER, JoinType.LEFT);
		Join<Event, User> responsibleUser = event.join(Event.RESPONSIBLE_USER, JoinType.LEFT);

		cq.multiselect(
			event.get(Event.ID),
			event.get(Event.UUID),
			event.get(Event.EXTERNAL_ID),
			event.get(Event.EXTERNAL_TOKEN),
			event.get(Event.INTERNAL_TOKEN),
			event.get(Event.EVENT_STATUS),
			event.get(Event.RISK_LEVEL),
			event.get(Event.EVENT_INVESTIGATION_STATUS),
			event.get(Event.EVENT_MANAGEMENT_STATUS),
			event.get(Event.DISEASE),
			event.get(Event.DISEASE_DETAILS),
			event.get(Event.START_DATE),
			event.get(Event.END_DATE),
			event.get(Event.EVOLUTION_DATE),
			event.get(Event.EVENT_TITLE),
			region.get(Region.UUID),
			region.get(Region.NAME),
			district.get(District.UUID),
			district.get(District.NAME),
			community.get(Community.UUID),
			community.get(Community.NAME),
			location.get(Location.CITY),
			location.get(Location.STREET),
			location.get(Location.HOUSE_NUMBER),
			location.get(Location.ADDITIONAL_INFORMATION),
			event.get(Event.SRC_TYPE),
			event.get(Event.SRC_FIRST_NAME),
			event.get(Event.SRC_LAST_NAME),
			event.get(Event.SRC_TEL_NO),
			event.get(Event.SRC_MEDIA_WEBSITE),
			event.get(Event.SRC_MEDIA_NAME),
			event.get(Event.REPORT_DATE_TIME),
			reportingUser.get(User.UUID),
			reportingUser.get(User.FIRST_NAME),
			reportingUser.get(User.LAST_NAME),
			responsibleUser.get(User.UUID),
			responsibleUser.get(User.FIRST_NAME),
			responsibleUser.get(User.LAST_NAME),
			event.get(Event.CHANGE_DATE));

		Predicate filter = null;

		if (eventCriteria != null) {
			if (eventCriteria.getUserFilterIncluded()) {
				filter = eventService.createUserFilter(cb, cq, event);
			}

			Predicate criteriaFilter = eventService.buildCriteriaFilter(eventCriteria, new EventQueryContext(cb, cq, event));
			filter = CriteriaBuilderHelper.and(cb, filter, criteriaFilter);
		}

		if (filter != null) {
			cq.where(filter);
		}

		if (sortProperties != null && sortProperties.size() > 0) {
			List<Order> order = new ArrayList<Order>(sortProperties.size());
			for (SortProperty sortProperty : sortProperties) {
				Expression<?> expression;
				switch (sortProperty.propertyName) {
				case EventIndexDto.UUID:
				case EventIndexDto.EXTERNAL_ID:
				case EventIndexDto.EXTERNAL_TOKEN:
				case EventIndexDto.INTERNAL_TOKEN:
				case EventIndexDto.EVENT_STATUS:
				case EventIndexDto.RISK_LEVEL:
				case EventIndexDto.EVENT_INVESTIGATION_STATUS:
				case EventIndexDto.EVENT_MANAGEMENT_STATUS:
				case EventIndexDto.DISEASE:
				case EventIndexDto.DISEASE_DETAILS:
				case EventIndexDto.START_DATE:
				case EventIndexDto.EVOLUTION_DATE:
				case EventIndexDto.EVENT_TITLE:
				case EventIndexDto.SRC_FIRST_NAME:
				case EventIndexDto.SRC_LAST_NAME:
				case EventIndexDto.SRC_TEL_NO:
				case EventIndexDto.SRC_TYPE:
				case EventIndexDto.REPORT_DATE_TIME:
					expression = event.get(sortProperty.propertyName);
					break;
				case EventIndexDto.EVENT_LOCATION:
					expression = region.get(Region.NAME);
					order.add(sortProperty.ascending ? cb.asc(expression) : cb.desc(expression));
					expression = district.get(District.NAME);
					order.add(sortProperty.ascending ? cb.asc(expression) : cb.desc(expression));
					expression = community.get(Community.NAME);
					break;
				case EventIndexDto.REGION:
					expression = region.get(Region.NAME);
					break;
				case EventIndexDto.DISTRICT:
					expression = district.get(District.NAME);
					break;
				case EventIndexDto.COMMUNITY:
					expression = community.get(Community.NAME);
					break;
				case EventIndexDto.ADDRESS:
					expression = location.get(Location.CITY);
					order.add(sortProperty.ascending ? cb.asc(expression) : cb.desc(expression));
					expression = location.get(Location.STREET);
					order.add(sortProperty.ascending ? cb.asc(expression) : cb.desc(expression));
					expression = location.get(Location.HOUSE_NUMBER);
					order.add(sortProperty.ascending ? cb.asc(expression) : cb.desc(expression));
					expression = location.get(Location.ADDITIONAL_INFORMATION);
					break;
				case EventIndexDto.REPORTING_USER:
					expression = reportingUser.get(User.FIRST_NAME);
					order.add(sortProperty.ascending ? cb.asc(expression) : cb.desc(expression));
					expression = reportingUser.get(User.LAST_NAME);
					break;
				case EventIndexDto.RESPONSIBLE_USER:
					expression = responsibleUser.get(User.FIRST_NAME);
					order.add(sortProperty.ascending ? cb.asc(expression) : cb.desc(expression));
					expression = responsibleUser.get(User.LAST_NAME);
					break;
				default:
					throw new IllegalArgumentException(sortProperty.propertyName);
				}
				order.add(sortProperty.ascending ? cb.asc(expression) : cb.desc(expression));
			}
			cq.orderBy(order);
		} else {
			cq.orderBy(cb.desc(event.get(Event.CHANGE_DATE)));
		}

		cq.distinct(true);

		List<EventIndexDto> indexList;
		if (first != null && max != null) {
			indexList = em.createQuery(cq).setFirstResult(first).setMaxResults(max).getResultList();
		} else {
			indexList = em.createQuery(cq).getResultList();
		}

		Map<String, Long> participantCounts = new HashMap<>();
		Map<String, Long> caseCounts = new HashMap<>();
		Map<String, Long> deathCounts = new HashMap<>();
		Map<String, Long> contactCounts = new HashMap<>();
		Map<String, Long> contactCountsSourceInEvent = new HashMap<>();
		Map<String, EventGroupsIndexDto> eventGroupsByEventId = new HashMap<>();
		Map<String, ExternalShareInfoCountAndLatestDate> survToolShareCountAndDates = new HashMap<>();

		if (CollectionUtils.isNotEmpty(indexList)) {
			List<String> eventUuids = indexList.stream().map(EventIndexDto::getUuid).collect(Collectors.toList());
			List<Long> eventIds = indexList.stream().map(EventIndexDto::getId).collect(Collectors.toList());
			List<Object[]> objectQueryList = null;

			// Participant, Case and Death Count
			CriteriaQuery<Object[]> participantCQ = cb.createQuery(Object[].class);
			Root<EventParticipant> epRoot = participantCQ.from(EventParticipant.class);
			Join<EventParticipant, Case> caseJoin = epRoot.join(EventParticipant.RESULTING_CASE, JoinType.LEFT);
			Predicate notDeleted = cb.isFalse(epRoot.get(EventParticipant.DELETED));
			Predicate isInIndexlist =
				CriteriaBuilderHelper.andInValues(eventUuids, null, cb, epRoot.get(EventParticipant.EVENT).get(AbstractDomainObject.UUID));
			participantCQ.multiselect(
				epRoot.get(EventParticipant.EVENT).get(AbstractDomainObject.UUID),
				cb.count(epRoot),
				cb.sum(cb.selectCase().when(cb.isNotNull(epRoot.get(EventParticipant.RESULTING_CASE)), 1).otherwise(0).as(Long.class)),
				cb.sum(cb.selectCase().when(cb.equal(caseJoin.get(Case.OUTCOME), CaseOutcome.DECEASED), 1).otherwise(0).as(Long.class)));
			participantCQ.where(notDeleted, isInIndexlist);
			participantCQ.groupBy(epRoot.get(EventParticipant.EVENT).get(AbstractDomainObject.UUID));

			objectQueryList = em.createQuery(participantCQ).getResultList();

			if (objectQueryList != null) {
				objectQueryList.forEach(r -> {
					participantCounts.put((String) r[0], (Long) r[1]);
					caseCounts.put((String) r[0], (Long) r[2]);
					deathCounts.put((String) r[0], (Long) r[3]);
				});
			}

			// Contact Count (with and without sourcecase in event) using theta join
			CriteriaQuery<Object[]> contactCQ = cb.createQuery(Object[].class);
			epRoot = contactCQ.from(EventParticipant.class);
			Root<Contact> contactRoot = contactCQ.from(Contact.class);
			Predicate participantPersonEqualsContactPerson = cb.equal(epRoot.get(EventParticipant.PERSON), contactRoot.get(Contact.PERSON));
			notDeleted = cb.isFalse(epRoot.get(EventParticipant.DELETED));
			Predicate contactNotDeleted = cb.isFalse(contactRoot.get(Contact.DELETED));
			isInIndexlist =
				CriteriaBuilderHelper.andInValues(eventUuids, null, cb, epRoot.get(EventParticipant.EVENT).get(AbstractDomainObject.UUID));

			Subquery<EventParticipant> sourceCaseSubquery = contactCQ.subquery(EventParticipant.class);
			Root<EventParticipant> epr2 = sourceCaseSubquery.from(EventParticipant.class);
			sourceCaseSubquery.select(epr2);
			sourceCaseSubquery.where(
				cb.equal(epr2.get(EventParticipant.RESULTING_CASE), contactRoot.get(Contact.CAZE)),
				cb.equal(epr2.get(EventParticipant.EVENT), epRoot.get(EventParticipant.EVENT)));

			contactCQ.multiselect(
				epRoot.get(EventParticipant.EVENT).get(AbstractDomainObject.UUID),
				cb.count(epRoot),
				cb.sum(cb.selectCase().when(cb.exists(sourceCaseSubquery), 1).otherwise(0).as(Long.class)));
			contactCQ.where(participantPersonEqualsContactPerson, notDeleted, contactNotDeleted, isInIndexlist);
			contactCQ.groupBy(epRoot.get(EventParticipant.EVENT).get(AbstractDomainObject.UUID));

			objectQueryList = em.createQuery(contactCQ).getResultList();
			if (objectQueryList != null) {
				objectQueryList.forEach(r -> {
					contactCounts.put((String) r[0], ((Long) r[1]));
					contactCountsSourceInEvent.put((String) r[0], ((Long) r[2]));
				});
			}

			if (featureConfigurationFacade.isFeatureEnabled(FeatureType.EVENT_GROUPS)) {
				// Get latest EventGroup with EventGroup count
				CriteriaQuery<Object[]> latestEventCQ = cb.createQuery(Object[].class);
				Root<Event> eventRoot = latestEventCQ.from(Event.class);
				Join<Event, EventGroup> eventGroupJoin = eventRoot.join(Event.EVENT_GROUPS, JoinType.INNER);
				isInIndexlist = CriteriaBuilderHelper.andInValues(eventUuids, null, cb, eventRoot.get(Event.UUID));
				latestEventCQ.where(isInIndexlist);
				latestEventCQ.multiselect(
					eventRoot.get(Event.UUID),
					CriteriaBuilderHelper.windowFirstValueDesc(
						cb,
						eventGroupJoin.get(EventGroup.UUID),
						eventRoot.get(Event.UUID),
						eventGroupJoin.get(EventGroup.CREATION_DATE)),
					CriteriaBuilderHelper.windowFirstValueDesc(
						cb,
						eventGroupJoin.get(EventGroup.NAME),
						eventRoot.get(Event.UUID),
						eventGroupJoin.get(EventGroup.CREATION_DATE)),
					CriteriaBuilderHelper.windowCount(cb, eventGroupJoin.get(EventGroup.ID), eventRoot.get(Event.UUID)));

				objectQueryList = em.createQuery(latestEventCQ).getResultList();

				if (objectQueryList != null) {
					objectQueryList.forEach(r -> {
						EventGroupReferenceDto eventGroupReference = new EventGroupReferenceDto((String) r[1], (String) r[2]);
						EventGroupsIndexDto eventGroups = new EventGroupsIndexDto(eventGroupReference, ((Number) r[3]).longValue());
						eventGroupsByEventId.put((String) r[0], eventGroups);
					});
				}
			}

			if (externalSurveillanceToolFacade.isFeatureEnabled()) {
				survToolShareCountAndDates = externalShareInfoService.getEventShareCountAndLatestDate(eventIds)
					.stream()
					.collect(Collectors.toMap(ExternalShareInfoCountAndLatestDate::getAssociatedObjectUuid, Function.identity()));
			}
		}

		if (indexList != null) {
			for (EventIndexDto eventDto : indexList) {
				Optional.ofNullable(participantCounts.get(eventDto.getUuid())).ifPresent(eventDto::setParticipantCount);
				Optional.ofNullable(caseCounts.get(eventDto.getUuid())).ifPresent(eventDto::setCaseCount);
				Optional.ofNullable(deathCounts.get(eventDto.getUuid())).ifPresent(eventDto::setDeathCount);
				Optional.ofNullable(contactCounts.get(eventDto.getUuid())).ifPresent(eventDto::setContactCount);
				Optional.ofNullable(contactCountsSourceInEvent.get(eventDto.getUuid())).ifPresent(eventDto::setContactCountSourceInEvent);
				Optional.ofNullable(eventGroupsByEventId.get(eventDto.getUuid())).ifPresent(eventDto::setEventGroups);
				Optional.ofNullable(survToolShareCountAndDates.get(eventDto.getUuid())).ifPresent((c) -> {
					eventDto.setSurveillanceToolStatus(c.getLatestStatus());
					eventDto.setSurveillanceToolLastShareDate(c.getLatestDate());
					eventDto.setSurveillanceToolShareCount(c.getCount());
				});
			}
		}

		return indexList;

	}

	@Override
	public Page<EventIndexDto> getIndexPage(EventCriteria eventCriteria, Integer offset, Integer size, List<SortProperty> sortProperties) {
		List<EventIndexDto> eventIndexList = getIndexList(eventCriteria, offset, size, sortProperties);
		long totalElementCount = count(eventCriteria);
		return new Page<>(eventIndexList, offset, size, totalElementCount);
	}

	@Override
	public List<EventExportDto> getExportList(EventCriteria eventCriteria, Collection<String> selectedRows, Integer first, Integer max) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<EventExportDto> cq = cb.createQuery(EventExportDto.class);
		Root<Event> event = cq.from(Event.class);
		Join<Event, Location> location = event.join(Event.EVENT_LOCATION, JoinType.LEFT);
		Join<Location, Region> region = location.join(Location.REGION, JoinType.LEFT);
		Join<Location, District> district = location.join(Location.DISTRICT, JoinType.LEFT);
		Join<Location, Community> community = location.join(Location.COMMUNITY, JoinType.LEFT);
		Join<Event, User> reportingUser = event.join(Event.REPORTING_USER, JoinType.LEFT);
		Join<Event, User> responsibleUser = event.join(Event.RESPONSIBLE_USER, JoinType.LEFT);

		cq.multiselect(
			event.get(Event.UUID),
			event.get(Event.EXTERNAL_ID),
			event.get(Event.EXTERNAL_TOKEN),
			event.get(Event.INTERNAL_TOKEN),
			event.get(Event.EVENT_STATUS),
			event.get(Event.RISK_LEVEL),
			event.get(Event.EVENT_INVESTIGATION_STATUS),
			event.get(Event.DISEASE),
			event.get(Event.DISEASE_DETAILS),
			event.get(Event.START_DATE),
			event.get(Event.END_DATE),
			event.get(Event.EVOLUTION_DATE),
			event.get(Event.EVOLUTION_COMMENT),
			event.get(Event.EVENT_TITLE),
			event.get(Event.EVENT_DESC),
			event.get(Event.DISEASE_TRANSMISSION_MODE),
			event.get(Event.NOSOCOMIAL),
			event.get(Event.TRANSREGIONAL_OUTBREAK),
			event.get(Event.MEANS_OF_TRANSPORT),
			event.get(Event.MEANS_OF_TRANSPORT_DETAILS),
			region.get(Region.UUID),
			region.get(Region.NAME),
			district.get(District.UUID),
			district.get(District.NAME),
			community.get(Community.UUID),
			community.get(Community.NAME),
			location.get(Location.CITY),
			location.get(Location.STREET),
			location.get(Location.HOUSE_NUMBER),
			location.get(Location.ADDITIONAL_INFORMATION),
			event.get(Event.SRC_TYPE),
			event.get(Event.SRC_INSTITUTIONAL_PARTNER_TYPE),
			event.get(Event.SRC_INSTITUTIONAL_PARTNER_TYPE_DETAILS),
			event.get(Event.SRC_FIRST_NAME),
			event.get(Event.SRC_LAST_NAME),
			event.get(Event.SRC_TEL_NO),
			event.get(Event.SRC_EMAIL),
			event.get(Event.SRC_MEDIA_WEBSITE),
			event.get(Event.SRC_MEDIA_NAME),
			event.get(Event.SRC_MEDIA_DETAILS),
			event.get(Event.REPORT_DATE_TIME),
			reportingUser.get(User.UUID),
			reportingUser.get(User.FIRST_NAME),
			reportingUser.get(User.LAST_NAME),
			responsibleUser.get(User.UUID),
			responsibleUser.get(User.FIRST_NAME),
			responsibleUser.get(User.LAST_NAME),
			event.get(Event.EVENT_MANAGEMENT_STATUS));

		Predicate filter = eventService.createUserFilter(cb, cq, event);

		if (eventCriteria != null) {
			Predicate criteriaFilter = eventService.buildCriteriaFilter(eventCriteria, new EventQueryContext(cb, cq, event));
			filter = CriteriaBuilderHelper.and(cb, filter, criteriaFilter);
		}

		if (CollectionUtils.isNotEmpty(selectedRows)) {
			filter = CriteriaBuilderHelper.andInValues(selectedRows, filter, cb, event.get(Event.UUID));
		}

		cq.where(filter);
		cq.orderBy(cb.desc(event.get(Event.REPORT_DATE_TIME)));

		List<EventExportDto> exportList;
		if (first != null && max != null) {
			exportList = em.createQuery(cq).setFirstResult(first).setMaxResults(max).getResultList();

		} else {
			exportList = em.createQuery(cq).getResultList();
		}

		Map<String, Long> participantCounts = new HashMap<>();
		Map<String, Long> caseCounts = new HashMap<>();
		Map<String, Long> deathCounts = new HashMap<>();
		Map<String, Long> contactCounts = new HashMap<>();
		Map<String, Long> contactCountsSourceInEvent = new HashMap<>();
		Map<String, EventGroupReferenceDto> latestEventGroupByEventId = new HashMap<>();
		Map<String, Long> eventGroupCountByEventId = new HashMap<>();
		if (exportList != null && !exportList.isEmpty()) {
			List<Object[]> objectQueryList = null;
			List<String> eventUuids = exportList.stream().map(EventExportDto::getUuid).collect(Collectors.toList());

			// Participant, Case and Death Count
			CriteriaQuery<Object[]> participantCQ = cb.createQuery(Object[].class);
			Root<EventParticipant> epRoot = participantCQ.from(EventParticipant.class);
			Join<EventParticipant, Case> caseJoin = epRoot.join(EventParticipant.RESULTING_CASE, JoinType.LEFT);
			Predicate notDeleted = cb.isFalse(epRoot.get(EventParticipant.DELETED));
			Predicate isInExportlist =
				CriteriaBuilderHelper.andInValues(eventUuids, null, cb, epRoot.get(EventParticipant.EVENT).get(AbstractDomainObject.UUID));
			participantCQ.multiselect(
				epRoot.get(EventParticipant.EVENT).get(AbstractDomainObject.UUID),
				cb.count(epRoot),
				cb.sum(cb.selectCase().when(cb.isNotNull(epRoot.get(EventParticipant.RESULTING_CASE)), 1).otherwise(0).as(Long.class)),
				cb.sum(cb.selectCase().when(cb.equal(caseJoin.get(Case.OUTCOME), CaseOutcome.DECEASED), 1).otherwise(0).as(Long.class)));
			participantCQ.where(notDeleted, isInExportlist);
			participantCQ.groupBy(epRoot.get(EventParticipant.EVENT).get(AbstractDomainObject.UUID));

			objectQueryList = em.createQuery(participantCQ).getResultList();

			if (objectQueryList != null) {
				objectQueryList.forEach(r -> {
					participantCounts.put((String) r[0], (Long) r[1]);
					caseCounts.put((String) r[0], (Long) r[2]);
					deathCounts.put((String) r[0], (Long) r[3]);
				});
			}

			// Contact Count (with and without sourcecase in event) using theta join
			CriteriaQuery<Object[]> contactCQ = cb.createQuery(Object[].class);
			epRoot = contactCQ.from(EventParticipant.class);
			Root<Contact> contactRoot = contactCQ.from(Contact.class);
			Predicate participantPersonEqualsContactPerson = cb.equal(epRoot.get(EventParticipant.PERSON), contactRoot.get(Contact.PERSON));
			notDeleted = cb.isFalse(epRoot.get(EventParticipant.DELETED));
			Predicate contactNotDeleted = cb.isFalse(contactRoot.get(Contact.DELETED));
			isInExportlist =
				CriteriaBuilderHelper.andInValues(eventUuids, null, cb, epRoot.get(EventParticipant.EVENT).get(AbstractDomainObject.UUID));

			Subquery<EventParticipant> sourceCaseSubquery = contactCQ.subquery(EventParticipant.class);
			Root<EventParticipant> epr2 = sourceCaseSubquery.from(EventParticipant.class);
			sourceCaseSubquery.select(epr2);
			sourceCaseSubquery.where(
				cb.equal(epr2.get(EventParticipant.RESULTING_CASE), contactRoot.get(Contact.CAZE)),
				cb.equal(epr2.get(EventParticipant.EVENT), epRoot.get(EventParticipant.EVENT)));

			contactCQ.multiselect(
				epRoot.get(EventParticipant.EVENT).get(AbstractDomainObject.UUID),
				cb.count(epRoot),
				cb.sum(cb.selectCase().when(cb.exists(sourceCaseSubquery), 1).otherwise(0).as(Long.class)));
			contactCQ.where(participantPersonEqualsContactPerson, notDeleted, contactNotDeleted, isInExportlist);
			contactCQ.groupBy(epRoot.get(EventParticipant.EVENT).get(AbstractDomainObject.UUID));

			objectQueryList = em.createQuery(contactCQ).getResultList();
			if (objectQueryList != null) {
				objectQueryList.forEach(r -> {
					contactCounts.put((String) r[0], ((Long) r[1]));
					contactCountsSourceInEvent.put((String) r[0], ((Long) r[2]));
				});
			}

			if (featureConfigurationFacade.isFeatureEnabled(FeatureType.EVENT_GROUPS)) {
				// Get latest EventGroup with EventGroup count
				CriteriaQuery<Object[]> latestEventCQ = cb.createQuery(Object[].class);
				Root<Event> eventRoot = latestEventCQ.from(Event.class);
				Join<Event, EventGroup> eventGroupJoin = eventRoot.join(Event.EVENT_GROUPS, JoinType.INNER);
				isInExportlist = CriteriaBuilderHelper.andInValues(eventUuids, null, cb, eventRoot.get(Event.UUID));
				latestEventCQ.where(notDeleted, isInExportlist);
				latestEventCQ.multiselect(
					eventRoot.get(Event.UUID),
					CriteriaBuilderHelper.windowFirstValueDesc(
						cb,
						eventGroupJoin.get(EventGroup.UUID),
						eventRoot.get(Event.UUID),
						eventGroupJoin.get(EventGroup.CREATION_DATE)),
					CriteriaBuilderHelper.windowFirstValueDesc(
						cb,
						eventGroupJoin.get(EventGroup.NAME),
						eventRoot.get(Event.UUID),
						eventGroupJoin.get(EventGroup.CREATION_DATE)),
					CriteriaBuilderHelper.windowCount(cb, eventGroupJoin.get(EventGroup.ID), eventRoot.get(Event.UUID)));

				objectQueryList = em.createQuery(latestEventCQ).getResultList();

				if (objectQueryList != null) {
					objectQueryList.forEach(r -> {
						EventGroupReferenceDto eventGroupReference = new EventGroupReferenceDto((String) r[1], (String) r[2]);
						latestEventGroupByEventId.put((String) r[0], eventGroupReference);
						eventGroupCountByEventId.put((String) r[0], ((Number) r[3]).longValue());
					});
				}
			}
		}

		if (exportList != null) {
			for (EventExportDto exportDto : exportList) {
				Optional.ofNullable(participantCounts.get(exportDto.getUuid())).ifPresent(exportDto::setParticipantCount);
				Optional.ofNullable(caseCounts.get(exportDto.getUuid())).ifPresent(exportDto::setCaseCount);
				Optional.ofNullable(deathCounts.get(exportDto.getUuid())).ifPresent(exportDto::setDeathCount);
				Optional.ofNullable(contactCounts.get(exportDto.getUuid())).ifPresent(exportDto::setContactCount);
				Optional.ofNullable(contactCountsSourceInEvent.get(exportDto.getUuid())).ifPresent(exportDto::setContactCountSourceInEvent);
				Optional.ofNullable(latestEventGroupByEventId.get(exportDto.getUuid())).ifPresent(exportDto::setLatestEventGroup);
				Optional.ofNullable(eventGroupCountByEventId.get(exportDto.getUuid())).ifPresent(exportDto::setEventGroupCount);
			}
		}

		return exportList;
	}

	@Override
	public boolean isArchived(String eventUuid) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<Event> from = cq.from(Event.class);

		// Workaround for probable bug in Eclipse Link/Postgre that throws a NoResultException when trying to
		// query for a true Boolean result
		cq.where(cb.and(cb.equal(from.get(Event.ARCHIVED), true), cb.equal(from.get(AbstractDomainObject.UUID), eventUuid)));
		cq.select(cb.count(from));
		long count = em.createQuery(cq).getSingleResult();
		return count > 0;
	}

	@Override
	public boolean isDeleted(String eventUuid) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<Event> from = cq.from(Event.class);

		cq.where(cb.and(cb.isTrue(from.get(Event.DELETED)), cb.equal(from.get(AbstractDomainObject.UUID), eventUuid)));
		cq.select(cb.count(from));
		long count = em.createQuery(cq).getSingleResult();
		return count > 0;
	}

	@Override
	public void archiveOrDearchiveEvent(String eventUuid, boolean archive) {

		Event event = eventService.getByUuid(eventUuid);
		event.setArchived(archive);
		eventService.ensurePersisted(event);
	}

	@Override
	public List<String> getArchivedUuidsSince(Date since) {

		User user = userService.getCurrentUser();

		if (user == null) {
			return Collections.emptyList();
		}

		return eventService.getArchivedUuidsSince(since);
	}

	@Override
	public Set<String> getAllSubordinateEventUuids(String eventUuid) {

		Set<String> uuids = new HashSet<>();
		Event superordinateEvent = eventService.getByUuid(eventUuid);
		addAllSubordinateEventsToSet(superordinateEvent, uuids);

		return uuids;
	}

	private void addAllSubordinateEventsToSet(Event event, Set<String> uuids) {

		uuids.addAll(event.getSubordinateEvents().stream().map(AbstractDomainObject::getUuid).collect(Collectors.toSet()));
		event.getSubordinateEvents().forEach(e -> addAllSubordinateEventsToSet(e, uuids));
	}

	@Override
	public Set<String> getAllSuperordinateEventUuids(String eventUuid) {

		Set<String> uuids = new HashSet<>();
		Event event = eventService.getByUuid(eventUuid);
		addSuperordinateEventToSet(event, uuids);

		return uuids;
	}

	@Override
	public Set<String> getAllEventUuidsByEventGroupUuid(String eventGroupUuid) {
		EventGroup eventGroup = eventGroupService.getByUuid(eventGroupUuid);
		if (eventGroup == null) {
			return Collections.emptySet();
		}

		return eventGroup.getEvents().stream().map(Event::getUuid).collect(Collectors.toSet());
	}

	@Override
	public String getFirstEventUuidWithOwnershipHandedOver(List<String> eventUuids) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<String> cq = cb.createQuery(String.class);
		Root<Event> eventRoot = cq.from(Event.class);
		Join<ShareInfoEvent, SormasToSormasShareInfo> sormasToSormasJoin =
			eventRoot.join(Event.SHARE_INFO_EVENTS, JoinType.LEFT).join(ShareInfoEvent.SHARE_INFO, JoinType.LEFT);

		cq.select(eventRoot.get(Event.UUID));
		cq.where(cb.and(eventRoot.get(Event.UUID).in(eventUuids), cb.isTrue(sormasToSormasJoin.get(SormasToSormasShareInfo.OWNERSHIP_HANDED_OVER))));
		cq.orderBy(cb.asc(eventRoot.get(AbstractDomainObject.CREATION_DATE)));

		try {
			return em.createQuery(cq).setMaxResults(1).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public void validate(EventDto event) throws ValidationRuntimeException {

		// Check whether any required field that does not have a not null constraint in
		// the database is empty
		if (event.getEventStatus() == null) {
			throw new ValidationRuntimeException(I18nProperties.getValidationError(Validations.validEventStatus));
		}
		if (event.getEventInvestigationStatus() == null) {
			throw new ValidationRuntimeException(I18nProperties.getValidationError(Validations.validEventInvestigationStatus));
		}
		if (StringUtils.isEmpty(event.getEventTitle())) {
			throw new ValidationRuntimeException(I18nProperties.getValidationError(Validations.validEventTitle));
		}

		LocationDto location = event.getEventLocation();
		if (location == null) {
			throw new ValidationRuntimeException(I18nProperties.getValidationError(Validations.validLocation));
		}
		if (location.getRegion() == null) {
			throw new ValidationRuntimeException(I18nProperties.getValidationError(Validations.validRegion));
		}
		if (location.getDistrict() == null) {
			throw new ValidationRuntimeException(I18nProperties.getValidationError(Validations.validDistrict));
		}
		// Check whether there are any infrastructure errors
		if (!districtFacade.getDistrictByUuid(location.getDistrict().getUuid()).getRegion().equals(location.getRegion())) {
			throw new ValidationRuntimeException(I18nProperties.getValidationError(Validations.noDistrictInRegion));
		}
		if (location.getCommunity() != null
			&& !communityFacade.getByUuid(location.getCommunity().getUuid()).getDistrict().equals(location.getDistrict())) {
			throw new ValidationRuntimeException(I18nProperties.getValidationError(Validations.noCommunityInDistrict));
		}
		if (location.getFacility() != null) {
			FacilityDto facility = facilityFacade.getByUuid(location.getFacility().getUuid());

			if (location.getFacilityType() == null && !FacilityDto.NONE_FACILITY_UUID.equals(location.getFacility().getUuid())) {
				throw new ValidationRuntimeException(I18nProperties.getValidationError(Validations.noFacilityType));
			}
			if (location.getFacilityType() != null && !location.getFacilityType().equals(facility.getType())) {
				throw new ValidationRuntimeException(I18nProperties.getValidationError(Validations.noFacilityType));
			}
			if (location.getCommunity() == null && facility.getDistrict() != null && !facility.getDistrict().equals(location.getDistrict())) {
				throw new ValidationRuntimeException(I18nProperties.getValidationError(Validations.noFacilityInDistrict));
			}
			if (location.getCommunity() != null && facility.getCommunity() != null && !location.getCommunity().equals(facility.getCommunity())) {
				throw new ValidationRuntimeException(I18nProperties.getValidationError(Validations.noFacilityInCommunity));
			}
			if (facility.getRegion() != null && !location.getRegion().equals(facility.getRegion())) {
				throw new ValidationRuntimeException(I18nProperties.getValidationError(Validations.noFacilityInRegion));
			}
		}
	}

	private void addSuperordinateEventToSet(Event event, Set<String> uuids) {

		if (event.getSuperordinateEvent() != null) {
			uuids.add(event.getSuperordinateEvent().getUuid());
			addSuperordinateEventToSet(event.getSuperordinateEvent(), uuids);
		}
	}

	public static EventDto toDto(Event source) {

		if (source == null) {
			return null;
		}
		EventDto target = new EventDto();
		DtoHelper.fillDto(target, source);

		target.setEventStatus(source.getEventStatus());
		target.setRiskLevel(source.getRiskLevel());
		target.setEventInvestigationStatus(source.getEventInvestigationStatus());
		target.setEventInvestigationStartDate(source.getEventInvestigationStartDate());
		target.setEventInvestigationEndDate(source.getEventInvestigationEndDate());
		target.setExternalId(source.getExternalId());
		target.setExternalToken(source.getExternalToken());
		target.setEventTitle(source.getEventTitle());
		target.setEventDesc(source.getEventDesc());
		target.setNosocomial(source.getNosocomial());
		target.setStartDate(source.getStartDate());
		target.setEndDate(source.getEndDate());
		target.setReportDateTime(source.getReportDateTime());
		target.setReportingUser(UserFacadeEjb.toReferenceDto(source.getReportingUser()));
		target.setEvolutionDate(source.getEvolutionDate());
		target.setEvolutionComment(source.getEvolutionComment());
		target.setEventLocation(LocationFacadeEjb.toDto(source.getEventLocation()));
		target.setTypeOfPlace(source.getTypeOfPlace());
		target.setMeansOfTransport(source.getMeansOfTransport());
		target.setMeansOfTransportDetails(source.getMeansOfTransportDetails());
		target.setConnectionNumber(source.getConnectionNumber());
		target.setTravelDate(source.getTravelDate());
		target.setWorkEnvironment(source.getWorkEnvironment());
		target.setSrcType(source.getSrcType());
		target.setSrcInstitutionalPartnerType(source.getSrcInstitutionalPartnerType());
		target.setSrcInstitutionalPartnerTypeDetails(source.getSrcInstitutionalPartnerTypeDetails());
		target.setSrcFirstName(source.getSrcFirstName());
		target.setSrcLastName(source.getSrcLastName());
		target.setSrcTelNo(source.getSrcTelNo());
		target.setSrcEmail(source.getSrcEmail());
		target.setSrcMediaWebsite(source.getSrcMediaWebsite());
		target.setSrcMediaName(source.getSrcMediaName());
		target.setSrcMediaDetails(source.getSrcMediaDetails());
		target.setDisease(source.getDisease());
		target.setDiseaseDetails(source.getDiseaseDetails());
		target.setResponsibleUser(UserFacadeEjb.toReferenceDto(source.getResponsibleUser()));
		target.setTypeOfPlaceText(source.getTypeOfPlaceText());
		target.setTransregionalOutbreak(source.getTransregionalOutbreak());
		target.setDiseaseTransmissionMode(source.getDiseaseTransmissionMode());
		target.setSuperordinateEvent(EventFacadeEjb.toReferenceDto(source.getSuperordinateEvent()));
		target.setEventManagementStatus(source.getEventManagementStatus());

		target.setReportLat(source.getReportLat());
		target.setReportLon(source.getReportLon());
		target.setReportLatLonAccuracy(source.getReportLatLonAccuracy());

		target.setInfectionPathCertainty(source.getInfectionPathCertainty());
		target.setHumanTransmissionMode(source.getHumanTransmissionMode());
		target.setParenteralTransmissionMode(source.getParenteralTransmissionMode());
		target.setMedicallyAssociatedTransmissionMode(source.getMedicallyAssociatedTransmissionMode());

		target.setEpidemiologicalEvidence(source.getEpidemiologicalEvidence());
		target.setEpidemiologicalEvidenceDetails(source.getEpidemiologicalEvidenceDetails());
		target.setLaboratoryDiagnosticEvidence(source.getLaboratoryDiagnosticEvidence());
		target.setLaboratoryDiagnosticEvidenceDetails(source.getLaboratoryDiagnosticEvidenceDetails());

		target.setInternalToken(source.getInternalToken());

		target.setSormasToSormasOriginInfo(SormasToSormasOriginInfoFacadeEjb.toDto(source.getSormasToSormasOriginInfo()));
		target.setOwnershipHandedOver(source.getShareInfoEvents().stream().anyMatch(ShareInfoHelper::isOwnerShipHandedOver));

		return target;
	}

	public EventDto convertToDto(Event source, Pseudonymizer pseudonymizer) {
		EventDto eventDto = toDto(source);

		pseudonymizeDto(source, eventDto, pseudonymizer);

		return eventDto;
	}

	private void pseudonymizeDto(Event event, EventDto dto, Pseudonymizer pseudonymizer) {
		if (dto != null) {
			boolean inJurisdiction = eventJurisdictionChecker.isInJurisdictionOrOwned(event);

			pseudonymizer.pseudonymizeDto(EventDto.class, dto, inJurisdiction, (e) -> {
				pseudonymizer.pseudonymizeUser(event.getReportingUser(), userService.getCurrentUser(), dto::setReportingUser);
			});
		}
	}

	private void restorePseudonymizedDto(EventDto dto, Event existingEvent, EventDto existingDto, Pseudonymizer pseudonymizer) {
		if (existingDto != null) {
			boolean inJurisdiction = eventJurisdictionChecker.isInJurisdictionOrOwned(existingEvent);
			pseudonymizer.restorePseudonymizedValues(EventDto.class, dto, existingDto, inJurisdiction);
			pseudonymizer.restoreUser(existingEvent.getReportingUser(), userService.getCurrentUser(), dto, dto::setReportingUser);
		}
	}

	public static EventReferenceDto toReferenceDto(Event entity) {

		if (entity == null) {
			return null;
		}

		return new EventReferenceDto(entity.getUuid(), entity.toString());
	}

	public Event fromDto(@NotNull EventDto source, boolean checkChangeDate) {
		Event target = DtoHelper.fillOrBuildEntity(source, eventService.getByUuid(source.getUuid()), Event::new, checkChangeDate);

		target.setEventStatus(source.getEventStatus());
		target.setRiskLevel(source.getRiskLevel());
		target.setEventInvestigationStatus(source.getEventInvestigationStatus());
		target.setEventInvestigationStartDate(source.getEventInvestigationStartDate());
		target.setEventInvestigationEndDate(source.getEventInvestigationEndDate());
		target.setExternalId(source.getExternalId());
		target.setExternalToken(source.getExternalToken());
		target.setEventTitle(source.getEventTitle());
		target.setEventDesc(source.getEventDesc());
		target.setNosocomial(source.getNosocomial());
		target.setStartDate(source.getStartDate());
		target.setEndDate(source.getEndDate());
		target.setReportDateTime(source.getReportDateTime());
		target.setReportingUser(userService.getByReferenceDto(source.getReportingUser()));
		target.setEvolutionDate(source.getEvolutionDate());
		target.setEvolutionComment(source.getEvolutionComment());
		target.setEventLocation(locationFacade.fromDto(source.getEventLocation(), checkChangeDate));
		target.setTypeOfPlace(source.getTypeOfPlace());
		target.setMeansOfTransport(source.getMeansOfTransport());
		target.setMeansOfTransportDetails(source.getMeansOfTransportDetails());
		target.setConnectionNumber(source.getConnectionNumber());
		target.setTravelDate(source.getTravelDate());
		target.setWorkEnvironment(source.getWorkEnvironment());
		target.setSrcType(source.getSrcType());
		target.setSrcInstitutionalPartnerType(source.getSrcInstitutionalPartnerType());
		target.setSrcInstitutionalPartnerTypeDetails(source.getSrcInstitutionalPartnerTypeDetails());
		target.setSrcFirstName(source.getSrcFirstName());
		target.setSrcLastName(source.getSrcLastName());
		target.setSrcTelNo(source.getSrcTelNo());
		target.setSrcEmail(source.getSrcEmail());
		target.setSrcMediaWebsite(source.getSrcMediaWebsite());
		target.setSrcMediaName(source.getSrcMediaName());
		target.setSrcMediaDetails(source.getSrcMediaDetails());
		target.setDisease(source.getDisease());
		target.setDiseaseDetails(source.getDiseaseDetails());
		target.setResponsibleUser(userService.getByReferenceDto(source.getResponsibleUser()));
		target.setTypeOfPlaceText(source.getTypeOfPlaceText());
		target.setTransregionalOutbreak(source.getTransregionalOutbreak());
		target.setDiseaseTransmissionMode(source.getDiseaseTransmissionMode());
		target.setSuperordinateEvent(eventService.getByReferenceDto(source.getSuperordinateEvent()));
		target.setEventManagementStatus(source.getEventManagementStatus());

		target.setReportLat(source.getReportLat());
		target.setReportLon(source.getReportLon());
		target.setReportLatLonAccuracy(source.getReportLatLonAccuracy());

		target.setInfectionPathCertainty(source.getInfectionPathCertainty());
		target.setHumanTransmissionMode(source.getHumanTransmissionMode());
		target.setParenteralTransmissionMode(source.getParenteralTransmissionMode());
		target.setMedicallyAssociatedTransmissionMode(source.getMedicallyAssociatedTransmissionMode());

		target.setEpidemiologicalEvidence(source.getEpidemiologicalEvidence());
		target.setEpidemiologicalEvidenceDetails(source.getEpidemiologicalEvidenceDetails());
		target.setLaboratoryDiagnosticEvidence(source.getLaboratoryDiagnosticEvidence());
		target.setLaboratoryDiagnosticEvidenceDetails(source.getLaboratoryDiagnosticEvidenceDetails());

		target.setInternalToken(source.getInternalToken());

		if (source.getSormasToSormasOriginInfo() != null) {
			target.setSormasToSormasOriginInfo(sormasToSormasOriginInfoFacade.fromDto(source.getSormasToSormasOriginInfo(), checkChangeDate));
		}

		return target;
	}

	/**
	 * Archives all events that have not been changed for a defined amount of days
	 * 
	 * @param daysAfterEventGetsArchived
	 *            defines the amount of days
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void archiveAllArchivableEvents(int daysAfterEventGetsArchived) {

		archiveAllArchivableEvents(daysAfterEventGetsArchived, LocalDate.now());
	}

	void archiveAllArchivableEvents(int daysAfterEventGetsArchived, @NotNull LocalDate referenceDate) {

		LocalDate notChangedSince = referenceDate.minusDays(daysAfterEventGetsArchived);

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<String> cq = cb.createQuery(String.class);
		Root<Event> from = cq.from(Event.class);

		Timestamp notChangedTimestamp = Timestamp.valueOf(notChangedSince.atStartOfDay());
		cq.where(cb.equal(from.get(Event.ARCHIVED), false), cb.not(eventService.createChangeDateFilter(cb, from, notChangedTimestamp)));
		cq.select(from.get(Event.UUID));
		List<String> uuids = em.createQuery(cq).getResultList();

		if (!uuids.isEmpty()) {

			CriteriaUpdate<Event> cu = cb.createCriteriaUpdate(Event.class);
			Root<Event> root = cu.from(Event.class);

			cu.set(root.get(Event.ARCHIVED), true);

			cu.where(root.get(Event.UUID).in(uuids));

			em.createQuery(cu).executeUpdate();
		}
	}

	@Override
	public boolean exists(String uuid) {
		return eventService.exists(uuid);
	}

	@Override
	public boolean doesExternalTokenExist(String externalToken, String eventUuid) {
		return eventService.exists(
			(cb, eventRoot) -> CriteriaBuilderHelper.and(
				cb,
				cb.equal(eventRoot.get(Event.EXTERNAL_TOKEN), externalToken),
				cb.notEqual(eventRoot.get(Event.UUID), eventUuid),
				cb.notEqual(eventRoot.get(Event.DELETED), Boolean.TRUE)));
	}

	@Override
	public String getUuidByCaseUuidOrPersonUuid(String searchTerm) {
		return eventService.getUuidByCaseUuidOrPersonUuid(searchTerm);
	}

	@Override
	public Set<RegionReferenceDto> getAllRegionsRelatedToEventUuids(List<String> uuids) {
		Set<RegionReferenceDto> regionReferenceDtos = new HashSet<>();
		IterableHelper.executeBatched(uuids, ModelConstants.PARAMETER_LIMIT, batchedUuids -> {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<String> cq = cb.createQuery(String.class);
			Root<Event> eventRoot = cq.from(Event.class);
			Join<Event, Location> locationJoin = eventRoot.join(Event.EVENT_LOCATION, JoinType.INNER);
			Join<Location, Region> regionJoin = locationJoin.join(Location.REGION, JoinType.INNER);

			cq.select(regionJoin.get(Region.UUID)).distinct(true);
			cq.where(eventRoot.get(Event.UUID).in(batchedUuids));

			em.createQuery(cq).getResultList().stream().map(RegionReferenceDto::new).forEach(regionReferenceDtos::add);
		});
		return regionReferenceDtos;
	}

	@Override
	public void updateExternalData(List<ExternalDataDto> externalData) throws ExternalDataUpdateException {
		eventService.updateExternalData(externalData);
	}

	@LocalBean
	@Stateless
	public static class EventFacadeEjbLocal extends EventFacadeEjb {

	}

	public Boolean isEventEditAllowed(String eventUuid) {
		Event event = eventService.getByUuid(eventUuid);
		return eventService.isEventEditAllowed(event);
	}
}
