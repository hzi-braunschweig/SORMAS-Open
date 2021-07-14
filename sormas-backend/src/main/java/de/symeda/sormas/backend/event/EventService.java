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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import javax.transaction.Transactional;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import de.symeda.sormas.api.Disease;
import de.symeda.sormas.api.EntityRelevanceStatus;
import de.symeda.sormas.api.event.EventCriteria;
import de.symeda.sormas.api.event.EventCriteriaDateType;
import de.symeda.sormas.api.event.EventReferenceDto;
import de.symeda.sormas.api.externaldata.ExternalDataDto;
import de.symeda.sormas.api.externaldata.ExternalDataUpdateException;
import de.symeda.sormas.api.task.TaskCriteria;
import de.symeda.sormas.api.user.JurisdictionLevel;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.api.utils.DateHelper;
import de.symeda.sormas.api.utils.criteria.CriteriaDateType;
import de.symeda.sormas.api.utils.criteria.ExternalShareDateType;
import de.symeda.sormas.backend.action.Action;
import de.symeda.sormas.backend.action.ActionService;
import de.symeda.sormas.backend.caze.Case;
import de.symeda.sormas.backend.caze.CaseService;
import de.symeda.sormas.backend.common.AbstractCoreAdoService;
import de.symeda.sormas.backend.common.AbstractDomainObject;
import de.symeda.sormas.backend.common.ChangeDateFilterBuilder;
import de.symeda.sormas.backend.common.CoreAdo;
import de.symeda.sormas.backend.common.CriteriaBuilderHelper;
import de.symeda.sormas.backend.contact.Contact;
import de.symeda.sormas.backend.facility.Facility;
import de.symeda.sormas.backend.location.Location;
import de.symeda.sormas.backend.person.Person;
import de.symeda.sormas.backend.person.PersonQueryContext;
import de.symeda.sormas.backend.region.Community;
import de.symeda.sormas.backend.region.District;
import de.symeda.sormas.backend.region.Region;
import de.symeda.sormas.backend.sample.Sample;
import de.symeda.sormas.backend.sample.SampleJoins;
import de.symeda.sormas.backend.sample.SampleJurisdictionPredicateValidator;
import de.symeda.sormas.backend.sample.SampleService;
import de.symeda.sormas.backend.share.ExternalShareInfo;
import de.symeda.sormas.backend.share.ExternalShareInfoService;
import de.symeda.sormas.backend.sormastosormas.shareinfo.SormasToSormasShareInfoService;
import de.symeda.sormas.backend.task.Task;
import de.symeda.sormas.backend.task.TaskService;
import de.symeda.sormas.backend.user.User;
import de.symeda.sormas.backend.user.UserService;
import de.symeda.sormas.backend.util.ExternalDataUtil;
import de.symeda.sormas.backend.util.IterableHelper;
import de.symeda.sormas.backend.util.JurisdictionHelper;
import de.symeda.sormas.backend.util.ModelConstants;
import de.symeda.sormas.backend.util.QueryHelper;
import de.symeda.sormas.utils.EventJoins;

@Stateless
@LocalBean
public class EventService extends AbstractCoreAdoService<Event> {

	@EJB
	private EventParticipantService eventParticipantService;
	@EJB
	private TaskService taskService;
	@EJB
	private ActionService actionService;
	@EJB
	private CaseService caseService;
	@EJB
	private UserService userService;
	@EJB
	private SormasToSormasShareInfoService sormasToSormasShareInfoService;
	@EJB
	private ExternalShareInfoService externalShareInfoService;
	@EJB
	private SampleService sampleService;

	public EventService() {
		super(Event.class);
	}

	public List<Event> getAllActiveEventsAfter(Date date) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Event> cq = cb.createQuery(getElementClass());
		Root<Event> from = cq.from(getElementClass());
		from.fetch(Event.EVENT_LOCATION);

		Predicate filter = createActiveEventsFilter(cb, from);

		User user = getCurrentUser();
		if (user != null) {
			EventUserFilterCriteria eventUserFilterCriteria = new EventUserFilterCriteria();
			eventUserFilterCriteria.includeUserCaseAndEventParticipantFilter(true);
			eventUserFilterCriteria.forceRegionJurisdiction(true);

			Predicate userFilter = createUserFilter(cb, cq, from, eventUserFilterCriteria);
			filter = CriteriaBuilderHelper.and(cb, filter, userFilter);
		}

		if (date != null) {
			Predicate dateFilter = createChangeDateFilter(cb, from, DateHelper.toTimestampUpper(date));
			filter = cb.and(filter, dateFilter);
		}

		cq.where(filter);
		cq.orderBy(cb.desc(from.get(Event.CHANGE_DATE)));
		cq.distinct(true);

		return em.createQuery(cq).getResultList();
	}

	public List<String> getAllActiveUuids() {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<String> cq = cb.createQuery(String.class);
		Root<Event> from = cq.from(getElementClass());

		Predicate filter = createActiveEventsFilter(cb, from);

		User user = getCurrentUser();
		if (user != null) {
			EventUserFilterCriteria eventUserFilterCriteria = new EventUserFilterCriteria();
			eventUserFilterCriteria.includeUserCaseAndEventParticipantFilter(true);
			eventUserFilterCriteria.forceRegionJurisdiction(true);

			Predicate userFilter = createUserFilter(cb, cq, from, eventUserFilterCriteria);
			filter = CriteriaBuilderHelper.and(cb, filter, userFilter);
		}

		cq.where(filter);
		cq.select(from.get(Event.UUID));

		return em.createQuery(cq).getResultList();
	}

	public Map<String, User> getAllEventUuidWithResponsibleUserByCaseAfterDateForNotification(Case caze, Date date) {
		if (caze == null || date == null) {
			return Collections.emptyMap();
		}

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
		Root<EventParticipant> from = cq.from(EventParticipant.class);
		Join<EventParticipant, Event> eventJoin = from.join(EventParticipant.EVENT, JoinType.INNER);

		Predicate diseaseFilter = cb.equal(eventJoin.get(Event.DISEASE), caze.getDisease());
		Predicate personFilter = cb.equal(from.get(EventParticipant.PERSON), caze.getPerson());

		Timestamp timestamp = DateHelper.toTimestampUpper(date);
		Predicate dateFilter = cb.or(
			CriteriaBuilderHelper.greaterThanAndNotNull(cb, eventJoin.get(Event.START_DATE), timestamp),
			CriteriaBuilderHelper.greaterThanAndNotNull(cb, eventJoin.get(Event.END_DATE), timestamp));

		Predicate responsibleUserFilter = cb.and(
			cb.isNotNull(eventJoin.get(Event.RESPONSIBLE_USER)),
			cb.not(cb.equal(eventJoin.get(Event.RESPONSIBLE_USER), caze.getReportingUser())));

		Predicate activeEventsFilter = createActiveEventsFilter(cb, eventJoin);

		cq.where(cb.and(diseaseFilter, personFilter, dateFilter, responsibleUserFilter, activeEventsFilter));
		cq.orderBy(cb.desc(from.get(EventParticipant.CREATION_DATE)));
		cq.multiselect(Arrays.asList(eventJoin.get(Event.UUID), eventJoin.get(Event.RESPONSIBLE_USER)));

		return em.createQuery(cq).getResultList().stream().collect(Collectors.toMap(objects -> (String) objects[0], objects -> (User) objects[1]));
	}

	public Map<String, Optional<User>> getAllEventUuidsWithResponsibleUserByPersonAndDiseaseAfterDateForNotification(
		String personUuid,
		Disease disease,
		Date date) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
		Root<Event> eventRoot = cq.from(Event.class);
		Join<Event, EventParticipant> eventParticipantJoin = eventRoot.join(Event.EVENT_PERSONS, JoinType.INNER);
		Join<EventParticipant, Person> personJoin = eventParticipantJoin.join(EventParticipant.PERSON, JoinType.INNER);
		Join<Event, User> responsibleUserJoin = eventRoot.join(Event.RESPONSIBLE_USER, JoinType.LEFT);

		Timestamp timestamp = DateHelper.toTimestampUpper(date);
		Predicate filter = cb.and(
			cb.equal(personJoin.get(Person.UUID), personUuid),
			cb.equal(eventRoot.get(Event.DISEASE), disease),
			createActiveEventsFilter(cb, eventRoot),
			CriteriaBuilderHelper.greaterThanAndNotNull(cb, eventRoot.get(Event.REPORT_DATE_TIME), timestamp),
			eventParticipantService.createActiveEventParticipantsFilter(cb, eventParticipantJoin));
		cq.where(filter);
		cq.multiselect(eventRoot.get(Event.UUID), responsibleUserJoin);
		return em.createQuery(cq)
			.getResultList()
			.stream()
			.collect(Collectors.toMap(row -> (String) row[0], row -> Optional.ofNullable((User) row[1])));
	}

	public Map<Disease, Long> getEventCountByDisease(EventCriteria eventCriteria) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
		Root<Event> event = cq.from(Event.class);

		cq.multiselect(event.get(Event.DISEASE), cb.count(event));
		cq.groupBy(event.get(Event.DISEASE));

		Predicate filter = createDefaultFilter(cb, event);
		filter = CriteriaBuilderHelper.and(cb, filter, buildCriteriaFilter(eventCriteria, new EventQueryContext(cb, cq, event)));
		filter = CriteriaBuilderHelper.and(cb, filter, createUserFilter(cb, cq, event));

		if (filter != null)
			cq.where(filter);

		List<Object[]> results = em.createQuery(cq).getResultList();

		return results.stream().collect(Collectors.toMap(e -> (Disease) e[0], e -> (Long) e[1]));
	}

	public Event getEventReferenceByEventParticipant(String eventParticipantUuid) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Event> cq = cb.createQuery(Event.class);
		Root<Event> event = cq.from(Event.class);

		Predicate filter = createDefaultFilter(cb, event);
		filter = CriteriaBuilderHelper.and(cb, filter, cb.equal(event.join(Event.EVENT_PERSONS).get(EventParticipant.UUID), eventParticipantUuid));
		filter = CriteriaBuilderHelper.and(cb, filter, createUserFilter(cb, cq, event));
		cq.where(filter);

		return em.createQuery(cq).getResultList().stream().findFirst().orElse(null);
	}

	public List<String> getArchivedUuidsSince(Date since) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<String> cq = cb.createQuery(String.class);
		Root<Event> event = cq.from(Event.class);

		EventUserFilterCriteria eventUserFilterCriteria = new EventUserFilterCriteria();
		eventUserFilterCriteria.includeUserCaseAndEventParticipantFilter(true);
		eventUserFilterCriteria.forceRegionJurisdiction(true);

		Predicate filter = createUserFilter(cb, cq, event, eventUserFilterCriteria);
		if (since != null) {
			Predicate dateFilter = cb.greaterThanOrEqualTo(event.get(Event.CHANGE_DATE), since);
			if (filter != null) {
				filter = cb.and(filter, dateFilter);
			} else {
				filter = dateFilter;
			}
		}

		Predicate archivedFilter = cb.equal(event.get(Event.ARCHIVED), true);
		if (filter != null) {
			filter = cb.and(filter, archivedFilter);
		} else {
			filter = archivedFilter;
		}

		cq.where(filter);
		cq.select(event.get(Event.UUID));

		return em.createQuery(cq).getResultList();
	}

	public List<String> getDeletedUuidsSince(Date since) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<String> cq = cb.createQuery(String.class);
		Root<Event> event = cq.from(Event.class);

		EventUserFilterCriteria eventUserFilterCriteria = new EventUserFilterCriteria();
		eventUserFilterCriteria.includeUserCaseAndEventParticipantFilter(true);
		eventUserFilterCriteria.forceRegionJurisdiction(true);

		Predicate filter = createUserFilter(cb, cq, event, eventUserFilterCriteria);
		if (since != null) {
			Predicate dateFilter = cb.greaterThanOrEqualTo(event.get(Event.CHANGE_DATE), since);
			if (filter != null) {
				filter = cb.and(filter, dateFilter);
			} else {
				filter = dateFilter;
			}
		}

		Predicate deletedFilter = cb.equal(event.get(Event.DELETED), true);
		if (filter != null) {
			filter = cb.and(filter, deletedFilter);
		} else {
			filter = deletedFilter;
		}

		cq.where(filter);
		cq.select(event.get(Event.UUID));

		return em.createQuery(cq).getResultList();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Predicate createUserFilter(CriteriaBuilder cb, CriteriaQuery cq, From<?, Event> eventPath) {
		return createUserFilter(cb, cq, eventPath, null);
	}

	@SuppressWarnings("rawtypes")
	public Predicate createUserFilter(
		CriteriaBuilder cb,
		CriteriaQuery cq,
		From<?, Event> eventPath,
		EventUserFilterCriteria eventUserFilterCriteria) {

		final User currentUser = getCurrentUser();
		final JurisdictionLevel jurisdictionLevel = currentUser.getJurisdictionLevel();
		if (jurisdictionLevel == JurisdictionLevel.NATION || currentUser.hasAnyUserRole(UserRole.REST_USER)) {
			return null;
		}

		Predicate filter = null;

		switch (jurisdictionLevel) {
		case REGION:
			if (currentUser.getRegion() != null) {
				filter = CriteriaBuilderHelper
					.or(cb, filter, cb.equal(eventPath.join(Event.EVENT_LOCATION, JoinType.LEFT).get(Location.REGION), currentUser.getRegion()));
			}
			break;
		case DISTRICT:
			if (currentUser.getDistrict() != null) {
				filter = CriteriaBuilderHelper
					.or(cb, filter, cb.equal(eventPath.join(Event.EVENT_LOCATION, JoinType.LEFT).get(Location.DISTRICT), currentUser.getDistrict()));
			}
			break;
		case COMMUNITY:
			if (currentUser.getCommunity() != null) {
				filter = CriteriaBuilderHelper.or(
					cb,
					filter,
					cb.equal(eventPath.join(Event.EVENT_LOCATION, JoinType.LEFT).get(Location.COMMUNITY), currentUser.getCommunity()));
			}
			break;
		case HEALTH_FACILITY:
			if (currentUser.getHealthFacility() != null && currentUser.getHealthFacility().getDistrict() != null) {
				filter = CriteriaBuilderHelper.or(
					cb,
					filter,
					cb.equal(
						eventPath.join(Event.EVENT_LOCATION, JoinType.LEFT).get(Location.DISTRICT),
						currentUser.getHealthFacility().getDistrict()));
			}
		case LABORATORY:

			final EventJoins eventJoins = new EventJoins(eventPath);

			final Subquery<Long> sampleSubQuery = cq.subquery(Long.class);
			final Root<Sample> sampleRoot = sampleSubQuery.from(Sample.class);
			final SampleJoins sampleJoins = new SampleJoins(sampleRoot);
			final Join eventParticipant = sampleJoins.getEventParticipant();
			SampleJurisdictionPredicateValidator sampleJurisdictionPredicateValidator =
					SampleJurisdictionPredicateValidator.withoutAssociations(cb, sampleJoins, currentUser);
			sampleSubQuery.where(cb.and(cb.equal(eventParticipant, eventJoins.getEventParticipants()), sampleJurisdictionPredicateValidator.inJurisdictionOrOwned()));
			sampleSubQuery.select(sampleRoot.get(Sample.ID));
			filter = CriteriaBuilderHelper.or(cb, cb.exists(sampleSubQuery));
			break;
		default:
		}

		if (filter != null && currentUser.getLimitedDisease() != null) {
			filter = cb
				.and(filter, cb.or(cb.equal(eventPath.get(Event.DISEASE), currentUser.getLimitedDisease()), cb.isNull(eventPath.get(Event.DISEASE))));
		}

		Predicate filterResponsible = cb.equal(eventPath.get(Event.REPORTING_USER), currentUser);
		filterResponsible = cb.or(filterResponsible, cb.equal(eventPath.get(Event.RESPONSIBLE_USER), currentUser));

		if (eventUserFilterCriteria != null && eventUserFilterCriteria.isIncludeUserCaseAndEventParticipantFilter()) {
			filter = CriteriaBuilderHelper.or(cb, filter, createCaseAndEventParticipantFilter(cb, cq, eventPath));
		}

		if (eventUserFilterCriteria != null && eventUserFilterCriteria.isForceRegionJurisdiction()) {
			filter = CriteriaBuilderHelper
				.or(cb, filter, cb.equal(eventPath.join(Event.EVENT_LOCATION, JoinType.LEFT).get(Location.REGION), currentUser.getRegion()));
		}

		if (filter != null) {
			filter = CriteriaBuilderHelper.or(cb, filter, filterResponsible);
		} else {
			filter = filterResponsible;
		}

		return filter;
	}

	public Predicate createCaseAndEventParticipantFilter(CriteriaBuilder cb, CriteriaQuery cq, From<?, Event> eventPath) {

		Join<Event, EventParticipant> eventParticipantJoin = eventPath.join(Event.EVENT_PERSONS, JoinType.LEFT);
		Join<EventParticipant, Case> caseJoin = eventParticipantJoin.join(EventParticipant.RESULTING_CASE, JoinType.LEFT);

		Subquery<Long> caseSubquery = cq.subquery(Long.class);
		Root<Case> caseRoot = caseSubquery.from(Case.class);
		caseSubquery.where(caseService.createUserFilter(cb, cq, caseRoot));
		caseSubquery.select(caseRoot.get(Case.ID));

		Predicate filter = cb.in(caseJoin.get(Case.ID)).value(caseSubquery);

		final User currentUser = getCurrentUser();
		final JurisdictionLevel jurisdictionLevel = currentUser.getJurisdictionLevel();
		if (jurisdictionLevel == JurisdictionLevel.REGION || jurisdictionLevel == JurisdictionLevel.DISTRICT) {
			Subquery<Long> eventParticipantSubquery = cq.subquery(Long.class);
			Root<EventParticipant> epRoot = eventParticipantSubquery.from(EventParticipant.class);

			switch (jurisdictionLevel) {
			case REGION:
				if (currentUser.getRegion() != null) {
					eventParticipantSubquery.where(
						cb.and(
							cb.equal(epRoot.get(EventParticipant.EVENT).get(Event.ID), eventPath.get(Event.ID)),
							cb.equal(epRoot.get(EventParticipant.REGION).get(Region.ID), currentUser.getRegion().getId())));
				}
				break;
			case DISTRICT:
				if (currentUser.getDistrict() != null) {
					eventParticipantSubquery.where(
						cb.and(
							cb.equal(epRoot.get(EventParticipant.EVENT).get(Event.ID), eventPath.get(Event.ID)),
							cb.equal(epRoot.get(EventParticipant.DISTRICT).get(District.ID), currentUser.getDistrict().getId())));
				}
				break;
			default:
			}

			eventParticipantSubquery.select(epRoot.get(EventParticipant.ID));

			filter = CriteriaBuilderHelper.or(cb, filter, cb.in(eventParticipantJoin.get(EventParticipant.ID)).value(eventParticipantSubquery));
		}

		return filter;

	}

	@Override
	public Predicate createChangeDateFilter(CriteriaBuilder cb, From<?, Event> eventPath, Timestamp date) {

		return addChangeDateFilter(new ChangeDateFilterBuilder(cb, date), eventPath).build();
	}

	public Predicate createChangeDateFilter(CriteriaBuilder cb, From<?, Event> eventPath, Expression<? extends Date> dateExpression) {

		return addChangeDateFilter(new ChangeDateFilterBuilder(cb, dateExpression), eventPath).build();
	}

	private ChangeDateFilterBuilder addChangeDateFilter(ChangeDateFilterBuilder filterBuilder, From<?, Event> eventPath) {

		filterBuilder.add(eventPath).add(eventPath, Event.EVENT_LOCATION).add(eventPath, Event.SHARE_INFO_EVENTS);

		return filterBuilder;
	}

	@Override
	public void delete(Event event) {

		// Delete all event participants associated with this event
		List<EventParticipant> eventParticipants = eventParticipantService.getAllByEventAfter(null, event);
		for (EventParticipant eventParticipant : eventParticipants) {
			eventParticipantService.delete(eventParticipant);
		}

		// Delete all tasks associated with this event
		List<Task> tasks = taskService.findBy(new TaskCriteria().event(new EventReferenceDto(event.getUuid())), true);
		for (Task task : tasks) {
			taskService.delete(task);
		}

		// Delete all event actions associated with this event
		List<Action> actions = actionService.getAllByEvent(event);
		for (Action action : actions) {
			actionService.delete(action);
		}

		// Mark the event as deleted
		super.delete(event);
	}

	public Predicate buildCriteriaFilter(EventCriteria eventCriteria, EventQueryContext eventQueryContext) {

		CriteriaBuilder cb = eventQueryContext.getCriteriaBuilder();
		From<?, Event> from = eventQueryContext.getRoot();
		final EventJoins<Event> joins = (EventJoins<Event>) eventQueryContext.getJoins();

		Predicate filter = null;
		if (eventCriteria.getReportingUserRole() != null) {
			filter = CriteriaBuilderHelper
				.and(cb, filter, cb.isMember(eventCriteria.getReportingUserRole(), joins.getReportingUser().get(User.USER_ROLES)));
		}
		if (eventCriteria.getDisease() != null) {
			filter = CriteriaBuilderHelper.and(cb, filter, cb.equal(from.get(Event.DISEASE), eventCriteria.getDisease()));
		}
		if (eventCriteria.getDiseaseVariant() != null) {
			filter = CriteriaBuilderHelper.and(cb, filter, cb.equal(from.get(Event.DISEASE_VARIANT), eventCriteria.getDiseaseVariant()));
		}
		if (eventCriteria.getEventStatus() != null) {
			filter = CriteriaBuilderHelper.and(cb, filter, cb.equal(from.get(Event.EVENT_STATUS), eventCriteria.getEventStatus()));
		}
		if (eventCriteria.getRiskLevel() != null) {
			filter = CriteriaBuilderHelper.and(cb, filter, cb.equal(from.get(Event.RISK_LEVEL), eventCriteria.getRiskLevel()));
		}
		if (eventCriteria.getEventInvestigationStatus() != null) {
			filter = CriteriaBuilderHelper
				.and(cb, filter, cb.equal(from.get(Event.EVENT_INVESTIGATION_STATUS), eventCriteria.getEventInvestigationStatus()));
		}
		if (eventCriteria.getEventManagementStatus() != null) {
			filter =
				CriteriaBuilderHelper.and(cb, filter, cb.equal(from.get(Event.EVENT_MANAGEMENT_STATUS), eventCriteria.getEventManagementStatus()));
		}
		if (eventCriteria.getTypeOfPlace() != null) {
			filter = CriteriaBuilderHelper.and(cb, filter, cb.equal(from.get(Event.TYPE_OF_PLACE), eventCriteria.getTypeOfPlace()));
		}
		if (eventCriteria.getRelevanceStatus() != null) {
			if (eventCriteria.getRelevanceStatus() == EntityRelevanceStatus.ACTIVE) {
				filter = CriteriaBuilderHelper.and(cb, filter, cb.or(cb.equal(from.get(Event.ARCHIVED), false), cb.isNull(from.get(Event.ARCHIVED))));
			} else if (eventCriteria.getRelevanceStatus() == EntityRelevanceStatus.ARCHIVED) {
				filter = CriteriaBuilderHelper.and(cb, filter, cb.equal(from.get(Event.ARCHIVED), true));
			}
		}
		if (eventCriteria.getDeleted() != null) {
			filter = CriteriaBuilderHelper.and(cb, filter, cb.equal(from.get(Event.DELETED), eventCriteria.getDeleted()));
		}
		if (eventCriteria.getRegion() != null) {
			filter = CriteriaBuilderHelper.and(cb, filter, cb.equal(joins.getRegion().get(Region.UUID), eventCriteria.getRegion().getUuid()));
		}
		if (eventCriteria.getDistrict() != null) {
			filter = CriteriaBuilderHelper.and(cb, filter, cb.equal(joins.getDistrict().get(District.UUID), eventCriteria.getDistrict().getUuid()));
		}
		if (eventCriteria.getCommunity() != null) {
			filter =
				CriteriaBuilderHelper.and(cb, filter, cb.equal(joins.getCommunity().get(Community.UUID), eventCriteria.getCommunity().getUuid()));
		}

		if (eventCriteria.getEventEvolutionDateFrom() != null && eventCriteria.getEventEvolutionDateTo() != null) {
			filter = CriteriaBuilderHelper.and(
				cb,
				filter,
				cb.between(from.get(Event.EVOLUTION_DATE), eventCriteria.getEventEvolutionDateFrom(), eventCriteria.getEventEvolutionDateTo()));
		} else if (eventCriteria.getEventEvolutionDateFrom() != null) {
			filter = CriteriaBuilderHelper
				.and(cb, filter, cb.greaterThanOrEqualTo(from.get(Event.EVOLUTION_DATE), eventCriteria.getEventEvolutionDateFrom()));
		} else if (eventCriteria.getEventEvolutionDateTo() != null) {
			filter =
				CriteriaBuilderHelper.and(cb, filter, cb.lessThanOrEqualTo(from.get(Event.EVOLUTION_DATE), eventCriteria.getEventEvolutionDateTo()));
		}
		if (eventCriteria.getResponsibleUser() != null) {
			filter = CriteriaBuilderHelper
				.and(cb, filter, cb.equal(joins.getReportingUser().get(User.UUID), eventCriteria.getResponsibleUser().getUuid()));
		}
		if (StringUtils.isNotEmpty(eventCriteria.getFreeText())) {
			String[] textFilters = eventCriteria.getFreeText().split("\\s+");
			for (String textFilter : textFilters) {
				if (DataHelper.isNullOrEmpty(textFilter)) {
					continue;
				}

				Predicate likeFilters = cb.or(
					CriteriaBuilderHelper.ilike(cb, from.get(Event.UUID), textFilter),
					CriteriaBuilderHelper.unaccentedIlike(cb, from.get(Event.EXTERNAL_ID), textFilter),
					CriteriaBuilderHelper.unaccentedIlike(cb, from.get(Event.EXTERNAL_TOKEN), textFilter),
					CriteriaBuilderHelper.unaccentedIlike(cb, from.get(Event.INTERNAL_TOKEN), textFilter),
					CriteriaBuilderHelper.unaccentedIlike(cb, from.get(Event.EVENT_TITLE), textFilter),
					CriteriaBuilderHelper.unaccentedIlike(cb, from.get(Event.EVENT_DESC), textFilter),
					CriteriaBuilderHelper.unaccentedIlike(cb, from.get(Event.SRC_FIRST_NAME), textFilter),
					CriteriaBuilderHelper.unaccentedIlike(cb, from.get(Event.SRC_LAST_NAME), textFilter),
					CriteriaBuilderHelper.ilike(cb, from.get(Event.SRC_EMAIL), textFilter),
					CriteriaBuilderHelper.ilike(cb, from.get(Event.SRC_TEL_NO), textFilter));
				filter = CriteriaBuilderHelper.and(cb, filter, likeFilters);
			}
		}
		if (StringUtils.isNotEmpty(eventCriteria.getFreeTextEventParticipants())) {
			Join<Event, EventParticipant> eventParticipantJoin = joins.getEventParticipants();
			Join<EventParticipant, Person> personJoin = joins.getEventParticipantPersons();

			final PersonQueryContext personQueryContext = new PersonQueryContext(cb, eventQueryContext.getQuery(), personJoin);

			String[] textFilters = eventCriteria.getFreeTextEventParticipants().split("\\s+");

			for (String textFilter : textFilters) {
				if (DataHelper.isNullOrEmpty(textFilter)) {
					continue;
				}

				Predicate likeFilters = cb.or(
					CriteriaBuilderHelper.ilike(cb, eventParticipantJoin.get(EventParticipant.UUID), textFilter),
					CriteriaBuilderHelper.unaccentedIlike(cb, personJoin.get(Person.FIRST_NAME), textFilter),
					CriteriaBuilderHelper.unaccentedIlike(cb, personJoin.get(Person.LAST_NAME), textFilter),
					CriteriaBuilderHelper.ilike(
						cb,
						(Expression<String>) personQueryContext.getSubqueryExpression(PersonQueryContext.PERSON_PHONE_SUBQUERY),
						textFilter),
					CriteriaBuilderHelper.ilike(
						cb,
						(Expression<String>) personQueryContext.getSubqueryExpression(PersonQueryContext.PERSON_EMAIL_SUBQUERY),
						textFilter));
				filter = CriteriaBuilderHelper.and(cb, filter, likeFilters);
			}
			filter = CriteriaBuilderHelper.and(cb, filter, cb.isFalse(eventParticipantJoin.get(EventParticipant.DELETED)));
		}
		if (StringUtils.isNotEmpty(eventCriteria.getFreeTextEventGroups())) {
			Join<Event, EventGroup> eventGroupJoin = joins.getEventGroup();

			String[] textFilters = eventCriteria.getFreeTextEventGroups().split("\\s+");
			for (String textFilter : textFilters) {
				if (DataHelper.isNullOrEmpty(textFilter)) {
					continue;
				}

				Predicate likeFilters = cb.or(
					CriteriaBuilderHelper.ilike(cb, eventGroupJoin.get(EventGroup.UUID), textFilter),
					CriteriaBuilderHelper.unaccentedIlike(cb, eventGroupJoin.get(EventGroup.NAME), textFilter));
				filter = CriteriaBuilderHelper.and(cb, filter, likeFilters);
			}
		}

		if (eventCriteria.getSrcType() != null) {
			filter = CriteriaBuilderHelper.and(cb, filter, cb.equal(from.get(Event.SRC_TYPE), eventCriteria.getSrcType()));
		}

		if (eventCriteria.getCaze() != null) {
			filter =
				CriteriaBuilderHelper.and(cb, filter, cb.equal(joins.getEventParticipantCases().get(Case.UUID), eventCriteria.getCaze().getUuid()));

			filter = CriteriaBuilderHelper.and(cb, filter, cb.isFalse(joins.getEventParticipants().get(EventParticipant.DELETED)));
		}
		if (eventCriteria.getPerson() != null) {
			filter = CriteriaBuilderHelper.and(
				cb,
				filter,
				cb.in(joins.getEventParticipantPersons().get(Person.UUID)).value(eventCriteria.getPerson().getUuid()),
				cb.isFalse(joins.getEventParticipants().get(EventParticipant.DELETED)));
		}
		if (eventCriteria.getFacilityType() != null) {
			filter = CriteriaBuilderHelper.and(cb, filter, cb.equal(joins.getFacilityType(), eventCriteria.getFacilityType()));
		}
		if (eventCriteria.getFacility() != null) {
			filter = CriteriaBuilderHelper.and(cb, filter, cb.equal(joins.getFacility().get(Facility.UUID), eventCriteria.getFacility().getUuid()));
		}
		if (eventCriteria.getSuperordinateEvent() != null) {
			filter = CriteriaBuilderHelper.and(
				cb,
				filter,
				cb.equal(from.get(Event.SUPERORDINATE_EVENT).get(AbstractDomainObject.UUID), eventCriteria.getSuperordinateEvent().getUuid()));
		}
		if (eventCriteria.getEventGroup() != null) {
			filter =
				CriteriaBuilderHelper.and(cb, filter, cb.equal(joins.getEventGroup().get(EventGroup.UUID), eventCriteria.getEventGroup().getUuid()));
		}
		if (CollectionUtils.isNotEmpty(eventCriteria.getExcludedUuids())) {
			filter = CriteriaBuilderHelper.and(cb, filter, cb.not(from.get(AbstractDomainObject.UUID).in(eventCriteria.getExcludedUuids())));
		}
		if (Boolean.TRUE.equals(eventCriteria.getHasNoSuperordinateEvent())) {
			filter = CriteriaBuilderHelper.and(cb, filter, cb.isNull(from.get(Event.SUPERORDINATE_EVENT)));
		}

		filter = CriteriaBuilderHelper.and(cb, filter, createEventDateFilter(eventQueryContext.getQuery(), cb, from, eventCriteria));
		filter = CriteriaBuilderHelper.and(
			cb,
			filter,
			externalShareInfoService.buildShareCriteriaFilter(
				eventCriteria,
				eventQueryContext.getQuery(),
				cb,
				from,
				ExternalShareInfo.EVENT,
				(latestShareDate) -> createChangeDateFilter(cb, from, latestShareDate)));

		return filter;
	}

	private Predicate createEventDateFilter(CriteriaQuery<?> cq, CriteriaBuilder cb, From<?, Event> from, EventCriteria eventCriteria) {
		Predicate filter = null;

		CriteriaDateType eventDateType = eventCriteria.getEventDateType();
		Date eventDateFrom = eventCriteria.getEventDateFrom();
		Date eventDateTo = eventCriteria.getEventDateTo();

		if (eventDateType == null || eventDateType == EventCriteriaDateType.EVENT_DATE) {
			Predicate eventDateFilter = null;

			if (eventDateFrom != null && eventDateTo != null) {
				eventDateFilter = cb.or(
					cb.and(cb.isNull(from.get(Event.END_DATE)), cb.between(from.get(Event.START_DATE), eventDateFrom, eventDateTo)),
					cb.and(cb.isNull(from.get(Event.START_DATE)), cb.between(from.get(Event.END_DATE), eventDateFrom, eventDateTo)),
					cb.and(
						cb.greaterThanOrEqualTo(from.get(Event.END_DATE), eventDateFrom),
						cb.lessThanOrEqualTo(from.get(Event.START_DATE), eventDateTo)));
			} else if (eventDateFrom != null) {
				eventDateFilter = cb.or(
					cb.and(cb.isNull(from.get(Event.END_DATE)), cb.greaterThanOrEqualTo(from.get(Event.START_DATE), eventDateFrom)),
					cb.and(cb.isNull(from.get(Event.START_DATE)), cb.greaterThanOrEqualTo(from.get(Event.END_DATE), eventDateFrom)));
			} else if (eventDateTo != null) {
				eventDateFilter = cb.or(
					cb.and(cb.isNull(from.get(Event.START_DATE)), cb.lessThanOrEqualTo(from.get(Event.END_DATE), eventDateTo)),
					cb.and(cb.isNull(from.get(Event.END_DATE)), cb.lessThanOrEqualTo(from.get(Event.START_DATE), eventDateTo)));
			}

			if (eventDateFrom != null || eventDateTo != null) {
				filter = CriteriaBuilderHelper.and(cb, filter, eventDateFilter);
			}
		} else if (eventDateType == ExternalShareDateType.LAST_EXTERNAL_SURVEILLANCE_TOOL_SHARE) {
			filter = externalShareInfoService.buildLatestSurvToolShareDateFilter(cq, cb, from, ExternalShareInfo.EVENT, (latestShareDate) -> {
				if (eventDateFrom != null && eventDateTo != null) {
					return cb.between(latestShareDate, eventDateFrom, eventDateTo);
				} else if (eventDateFrom != null) {
					return cb.greaterThanOrEqualTo(latestShareDate, eventDateFrom);
				} else {
					return cb.lessThanOrEqualTo(latestShareDate, eventDateTo);
				}
			});
		}

		return filter;
	}

	/**
	 * Creates a filter that excludes all events that are either {@link Event#isArchived()} or {@link CoreAdo#isDeleted()}.
	 */
	public Predicate createActiveEventsFilter(CriteriaBuilder cb, Root<Event> root) {
		return cb.and(cb.isFalse(root.get(Event.ARCHIVED)), cb.isFalse(root.get(Event.DELETED)));
	}

	/**
	 * Creates a filter that excludes all events that are either {@link Event#isArchived()} or {@link CoreAdo#isDeleted()}.
	 */
	public Predicate createActiveEventsFilter(CriteriaBuilder cb, Path<Event> root) {
		return cb.and(cb.isFalse(root.get(Event.ARCHIVED)), cb.isFalse(root.get(Event.DELETED)));
	}

	/**
	 * Creates a default filter that should be used as the basis of queries that do not use {@link EventCriteria}.
	 * This essentially removes {@link CoreAdo#isDeleted()} events from the queries.
	 */
	public Predicate createDefaultFilter(CriteriaBuilder cb, Root<Event> root) {
		return cb.isFalse(root.get(Event.DELETED));
	}

	public String getUuidByCaseUuidOrPersonUuid(String searchTerm) {

		if (StringUtils.isEmpty(searchTerm)) {
			return null;
		}

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<String> cq = cb.createQuery(String.class);
		Root<Event> root = cq.from(Event.class);
		EventJoins<Event> joins = new EventJoins<>(root);

		Predicate filter = cb.or(
			cb.equal(cb.lower(joins.getEventParticipantCases().get(Case.UUID)), searchTerm.toLowerCase()),
			cb.equal(cb.lower(joins.getEventParticipantPersons().get(Person.UUID)), searchTerm.toLowerCase()));

		cq.where(filter);
		cq.orderBy(cb.desc(root.get(Event.REPORT_DATE_TIME)));
		cq.select(root.get(Event.UUID));

		return QueryHelper.getFirstResult(em, cq);
	}

	public List<EventSummaryDetails> getEventSummaryDetailsByCases(List<Long> casesId) {
		if (casesId.isEmpty()) {
			return Collections.emptyList();
		}

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<EventSummaryDetails> eventsCq = cb.createQuery(EventSummaryDetails.class);
		Root<EventParticipant> eventsCqRoot = eventsCq.from(EventParticipant.class);
		Join<EventParticipant, Event> eventJoin = eventsCqRoot.join(EventParticipant.EVENT, JoinType.INNER);
		Join<EventParticipant, Case> cazeJoin = eventsCqRoot.join(EventParticipant.RESULTING_CASE, JoinType.INNER);

		eventsCq.where(
			cb.and(
				cazeJoin.get(AbstractDomainObject.ID).in(casesId),
				cb.isFalse(eventJoin.get(Event.DELETED)),
				cb.isFalse(eventJoin.get(Event.ARCHIVED)),
				cb.isFalse(eventsCqRoot.get(EventParticipant.DELETED))));
		eventsCq.multiselect(
			cazeJoin.get(Case.ID),
			eventJoin.get(Event.UUID),
			eventJoin.get(Event.EVENT_STATUS),
			eventJoin.get(Event.EVENT_TITLE),
			cb.coalesce(cb.coalesce(eventJoin.get(Event.END_DATE), eventJoin.get(Event.START_DATE)), eventJoin.get(Event.REPORT_DATE_TIME)));

		return em.createQuery(eventsCq).getResultList();
	}

	public List<ContactEventSummaryDetails> getEventSummaryDetailsByContacts(List<String> contactUuids) {
		if (contactUuids.isEmpty()) {
			return Collections.emptyList();
		}

		List<ContactEventSummaryDetails> eventSummaryDetailsList = new ArrayList<>();

		IterableHelper.executeBatched(contactUuids, ModelConstants.PARAMETER_LIMIT, batchedContactUuids -> {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<ContactEventSummaryDetails> eventsCq = cb.createQuery(ContactEventSummaryDetails.class);
			Root<EventParticipant> eventsCqRoot = eventsCq.from(EventParticipant.class);
			Join<EventParticipant, Event> eventJoin = eventsCqRoot.join(EventParticipant.EVENT, JoinType.INNER);
			Join<Person, Contact> contactJoin = eventsCqRoot.join(EventParticipant.PERSON, JoinType.INNER).join(Person.CONTACTS, JoinType.INNER);

			eventsCq.where(
				cb.and(
					contactJoin.get(AbstractDomainObject.UUID).in(batchedContactUuids),
					cb.isFalse(eventJoin.get(Event.DELETED)),
					cb.isFalse(eventJoin.get(Event.ARCHIVED)),
					cb.isFalse(eventsCqRoot.get(EventParticipant.DELETED))));
			eventsCq.multiselect(
				contactJoin.get(Contact.UUID),
				eventJoin.get(Event.UUID),
				eventJoin.get(Event.EVENT_TITLE),
				cb.coalesce(cb.coalesce(eventJoin.get(Event.END_DATE), eventJoin.get(Event.START_DATE)), eventJoin.get(Event.REPORT_DATE_TIME)));

			eventSummaryDetailsList.addAll(em.createQuery(eventsCq).getResultList());
		});

		return eventSummaryDetailsList;
	}

	public boolean isEventEditAllowed(Event event) {
		if (event.getSormasToSormasOriginInfo() != null && !event.getSormasToSormasOriginInfo().isOwnershipHandedOver()) {
			return false;
		}

		return inJurisdictionOrOwned(event) && !sormasToSormasShareInfoService.isEventOwnershipHandedOver(event);
	}

	public boolean inJurisdiction(Event event) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Boolean> cq = cb.createQuery(Boolean.class);
		Root<Event> root = cq.from(Event.class);
		cq.multiselect(JurisdictionHelper.booleanSelector(cb, inJurisdiction(new EventQueryContext(cb, cq, root))));
		cq.where(cb.equal(root.get(Event.UUID), event.getUuid()));
		return em.createQuery(cq).getResultList().stream().anyMatch(aBoolean -> aBoolean);
	}

	public boolean inJurisdictionOrOwned(Event event) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Boolean> cq = cb.createQuery(Boolean.class);
		Root<Event> root = cq.from(Event.class);
		cq.multiselect(JurisdictionHelper.booleanSelector(cb, inJurisdictionOrOwned(new EventQueryContext(cb, cq, root))));
		cq.where(cb.equal(root.get(Event.UUID), event.getUuid()));
		return em.createQuery(cq).getResultList().stream().anyMatch(aBoolean -> aBoolean);
	}

	public Predicate inJurisdiction(EventQueryContext qc) {
		final User currentUser = userService.getCurrentUser();
		return EventJurisdictionPredicateValidator.of(qc, currentUser).inJurisdiction();
	}

	public Predicate inJurisdictionOrOwned(EventQueryContext qc) {
		final User currentUser = userService.getCurrentUser();
		return EventJurisdictionPredicateValidator.of(qc, currentUser).inJurisdictionOrOwned();
	}

	@Transactional(rollbackOn = Exception.class)
	public void updateExternalData(List<ExternalDataDto> externalData) throws ExternalDataUpdateException {
		ExternalDataUtil.updateExternalData(externalData, this::getByUuids, this::ensurePersisted);
	}

	public List<Event> getAllByCase(String caseUuid) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Event> cq = cb.createQuery(getElementClass());
		Root<Event> from = cq.from(getElementClass());
		from.fetch(Event.EVENT_LOCATION);

		Predicate filter = createActiveEventsFilter(cb, from);

		User user = getCurrentUser();
		if (user != null) {
			Predicate userFilter = createUserFilter(cb, cq, from);
			filter = CriteriaBuilderHelper.and(cb, filter, userFilter);
		}

		filter = CriteriaBuilderHelper.and(
			cb,
			filter,
			cb.equal(from.join(Event.EVENT_PERSONS, JoinType.LEFT).join(EventParticipant.RESULTING_CASE, JoinType.LEFT).get(Case.UUID), caseUuid));

		cq.where(filter);
		cq.distinct(true);

		return em.createQuery(cq).getResultList();
	}
}
