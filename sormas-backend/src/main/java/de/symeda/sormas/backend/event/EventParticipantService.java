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
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.collections.CollectionUtils;

import de.symeda.sormas.api.event.EventParticipantCriteria;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.backend.caze.Case;
import de.symeda.sormas.backend.common.AbstractCoreAdoService;
import de.symeda.sormas.backend.common.AbstractDomainObject;
import de.symeda.sormas.backend.common.CriteriaBuilderHelper;
import de.symeda.sormas.backend.contact.Contact;
import de.symeda.sormas.backend.contact.ContactQueryContext;
import de.symeda.sormas.backend.person.Person;
import de.symeda.sormas.backend.person.PersonQueryContext;
import de.symeda.sormas.backend.sample.Sample;
import de.symeda.sormas.backend.sample.SampleService;
import de.symeda.sormas.backend.sormastosormas.share.shareinfo.SormasToSormasShareInfoService;
import de.symeda.sormas.backend.user.User;
import de.symeda.sormas.backend.util.IterableHelper;
import de.symeda.sormas.backend.util.ModelConstants;
import de.symeda.sormas.backend.vaccinationinfo.VaccinationInfo;
import de.symeda.sormas.backend.vaccinationinfo.VaccinationInfoService;
import de.symeda.sormas.utils.EventParticipantJoins;

@Stateless
@LocalBean
public class EventParticipantService extends AbstractCoreAdoService<EventParticipant> {

	@EJB
	private EventService eventService;
	@EJB
	private SampleService sampleService;
	@EJB
	private VaccinationInfoService vaccinationInfoService;
	@EJB
	private EventParticipantJurisdictionChecker eventParticipantJurisdictionChecker;
	@EJB
	private SormasToSormasShareInfoService sormasToSormasShareInfoService;

	public EventParticipantService() {
		super(EventParticipant.class);
	}

	public List<EventParticipant> getAllActiveEventParticipantsAfter(Date date, User user) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<EventParticipant> cq = cb.createQuery(getElementClass());
		Root<EventParticipant> from = cq.from(getElementClass());
		Join<EventParticipant, Event> event = from.join(EventParticipant.EVENT, JoinType.LEFT);

		Predicate filter = cb.or(cb.equal(event.get(Event.ARCHIVED), false), cb.isNull(event.get(Event.ARCHIVED)));

		filter = cb.and(filter, createDefaultFilter(cb, from));

		if (user != null) {
			Predicate userFilter = createUserFilter(cb, cq, from);
			filter = CriteriaBuilderHelper.and(cb, filter, userFilter);
		}

		if (date != null) {
			Predicate dateFilter = createChangeDateFilter(cb, from, date);
			filter = cb.and(filter, dateFilter);
		}

		cq.where(filter);
		cq.orderBy(cb.desc(from.get(EventParticipant.CHANGE_DATE)));
		cq.distinct(true);

		List<EventParticipant> resultList = em.createQuery(cq).getResultList();
		return resultList;
	}

	public List<String> getAllActiveUuids(User user) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<String> cq = cb.createQuery(String.class);
		Root<EventParticipant> from = cq.from(getElementClass());
		Join<EventParticipant, Event> event = from.join(EventParticipant.EVENT, JoinType.LEFT);

		Predicate filter = cb.or(cb.equal(event.get(Event.ARCHIVED), false), cb.isNull(event.get(Event.ARCHIVED)));

		filter = cb.and(filter, createDefaultFilter(cb, from));

		if (user != null) {
			Predicate userFilter = createUserFilter(cb, cq, from);
			filter = CriteriaBuilderHelper.and(cb, filter, userFilter);
		}

		cq.where(filter);
		cq.select(from.get(EventParticipant.UUID));

		return em.createQuery(cq).getResultList();
	}

	public List<EventParticipant> getAllByEventAfter(Date date, Event event) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<EventParticipant> cq = cb.createQuery(getElementClass());
		Root<EventParticipant> from = cq.from(getElementClass());

		Predicate filter = cb.equal(from.get(EventParticipant.EVENT), event);
		if (date != null) {
			filter = cb.and(filter, createChangeDateFilter(cb, from, date));
		}
		cq.where(filter);
		cq.orderBy(cb.desc(from.get(EventParticipant.CREATION_DATE)));

		List<EventParticipant> resultList = em.createQuery(cq).getResultList();
		return resultList;
	}

	public List<EventParticipant> getAllActiveByEvent(Event event) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<EventParticipant> cq = cb.createQuery(getElementClass());
		Root<EventParticipant> from = cq.from(getElementClass());

		Predicate filter = cb.equal(from.get(EventParticipant.EVENT), event);
		filter = cb.and(filter, cb.equal(from.get(Event.DELETED), false));

		cq.where(filter);
		cq.orderBy(cb.desc(from.get(EventParticipant.CREATION_DATE)));

		List<EventParticipant> resultList = em.createQuery(cq).getResultList();
		return resultList;
	}

	public Predicate buildCriteriaFilter(EventParticipantCriteria criteria, EventParticipantQueryContext eventParticipantQueryContext) {

		CriteriaBuilder cb = eventParticipantQueryContext.getCriteriaBuilder();
		Root<EventParticipant> from = (Root<EventParticipant>) eventParticipantQueryContext.getRoot();
		CriteriaQuery cq = eventParticipantQueryContext.getQuery();
		Join<EventParticipant, Event> event = from.join(EventParticipant.EVENT, JoinType.LEFT);
		Join<Case, Person> person = from.join(EventParticipant.PERSON, JoinType.LEFT);
		PersonQueryContext personQueryContext = new PersonQueryContext(cb, cq, person);

		Predicate filter = null;
		if (criteria.getEvent() != null) {
			filter = CriteriaBuilderHelper.and(cb, filter, cb.equal(event.get(Event.UUID), criteria.getEvent().getUuid()));
		}
		if (criteria.getPerson() != null) {
			filter = CriteriaBuilderHelper.and(cb, filter, cb.equal(person.get(Person.UUID), criteria.getPerson().getUuid()));
		}

		if (criteria.getFreeText() != null) {
			String[] textFilters = criteria.getFreeText().split("\\s+");
			for (String textFilter : textFilters) {
				if (DataHelper.isNullOrEmpty(textFilter)) {
					continue;
				}

				Predicate likeFilters = cb.or(
					CriteriaBuilderHelper.unaccentedIlike(cb, person.get(Person.FIRST_NAME), textFilter),
					CriteriaBuilderHelper.unaccentedIlike(cb, person.get(Person.LAST_NAME), textFilter),
					phoneNumberPredicate(
						cb,
						(Expression<String>) personQueryContext.getSubqueryExpression(ContactQueryContext.PERSON_PHONE_SUBQUERY),
						textFilter));
				filter = CriteriaBuilderHelper.and(cb, filter, likeFilters);
			}
		}

		if (criteria.getBirthdateYYYY() != null) {
			filter = CriteriaBuilderHelper.and(cb, filter, cb.equal(person.get(Person.BIRTHDATE_YYYY), criteria.getBirthdateYYYY()));
		}
		if (criteria.getBirthdateMM() != null) {
			filter = CriteriaBuilderHelper.and(cb, filter, cb.equal(person.get(Person.BIRTHDATE_MM), criteria.getBirthdateMM()));
		}
		if (criteria.getBirthdateDD() != null) {
			filter = CriteriaBuilderHelper.and(cb, filter, cb.equal(person.get(Person.BIRTHDATE_DD), criteria.getBirthdateDD()));
		}
		if (criteria.getPathogenTestResult() != null) {
			Join<EventParticipant, Sample> samples = from.join(EventParticipant.SAMPLES, JoinType.LEFT);
			filter = CriteriaBuilderHelper.and(cb, filter, cb.equal(samples.get(Sample.PATHOGEN_TEST_RESULT), criteria.getPathogenTestResult()));
		}
		if (criteria.getVaccination() != null) {
			Join<EventParticipant, VaccinationInfo> vaccinationInfoJoin = from.join(EventParticipant.VACCINATION_INFO, JoinType.LEFT);
			filter = CriteriaBuilderHelper.and(cb, filter, cb.equal(vaccinationInfoJoin.get(VaccinationInfo.VACCINATION), criteria.getVaccination()));
		}
		if (Boolean.TRUE.equals(criteria.getNoResultingCase())) {
			filter = CriteriaBuilderHelper.and(cb, filter, cb.isNull(from.get(EventParticipant.RESULTING_CASE)));
		}

		filter = CriteriaBuilderHelper.and(cb, filter, createDefaultFilter(cb, from));

		return filter;
	}

	public Predicate createActiveEventParticipantsFilter(CriteriaBuilder cb, Root<EventParticipant> root) {

		Join<EventParticipant, Event> event = root.join(EventParticipant.EVENT, JoinType.LEFT);
		return cb.and(cb.isFalse(event.get(Event.ARCHIVED)), cb.isFalse(event.get(Event.DELETED)));
	}

	public Predicate createActiveEventParticipantsFilter(CriteriaBuilder cb, Join<?, EventParticipant> eventParticipantJoin) {

		Join<EventParticipant, Event> event = eventParticipantJoin.join(EventParticipant.EVENT, JoinType.LEFT);
		return cb.and(cb.isFalse(event.get(Event.ARCHIVED)), cb.isFalse(event.get(Event.DELETED)));
	}

	/**
	 * @see /sormas-backend/doc/UserDataAccess.md
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Predicate createUserFilter(CriteriaBuilder cb, CriteriaQuery cq, From<?, EventParticipant> eventParticipantPath) {
		return createUserFilterForJoin(cb, cq, eventParticipantPath);
	}

	public Predicate createUserFilterForJoin(CriteriaBuilder cb, CriteriaQuery cq, From<?, EventParticipant> eventParticipantPath) {
		// can see the participants of all accessible events
		EventUserFilterCriteria eventUserFilterCriteria = new EventUserFilterCriteria();
		eventUserFilterCriteria.includeUserCaseAndEventParticipantFilter(true);
		eventUserFilterCriteria.forceRegionJurisdiction(true);

		return eventService.createUserFilter(cb, cq, eventParticipantPath.join(EventParticipant.EVENT, JoinType.LEFT), eventUserFilterCriteria);
	}

	public List<EventParticipant> getAllByPerson(Person person) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<EventParticipant> cq = cb.createQuery(getElementClass());
		Root<EventParticipant> from = cq.from(getElementClass());

		Predicate userFilter = eventService.createUserFilter(cb, cq, from.join(EventParticipant.EVENT, JoinType.INNER));

		Predicate filter = CriteriaBuilderHelper.and(cb, cb.equal(from.get(EventParticipant.PERSON), person), userFilter);

		cq.where(filter);
		cq.orderBy(cb.desc(from.get(EventParticipant.CREATION_DATE)));

		List<EventParticipant> resultList = em.createQuery(cq).getResultList();
		return resultList;
	}

	public EventParticipant getByEventAndPerson(String eventUuid, String personUuid) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<EventParticipant> cq = cb.createQuery(getElementClass());
		Root<EventParticipant> from = cq.from(getElementClass());

		cq.where(
			createDefaultFilter(cb, from),
			cb.equal(from.join(EventParticipant.PERSON).get(Person.UUID), personUuid),
			cb.equal(from.join(EventParticipant.EVENT).get(Event.UUID), eventUuid));

		return em.createQuery(cq).getResultList().stream().findFirst().orElse(null);
	}

	@Override
	public void delete(EventParticipant eventParticipant) {

		eventParticipant.getSamples()
			.stream()
			.filter(sample -> sample.getAssociatedCase() == null && sample.getAssociatedContact() == null)
			.forEach(sample -> sampleService.delete(sample));

		super.delete(eventParticipant);
	}

	public List<String> getDeletedUuidsSince(Date since, User user) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<String> cq = cb.createQuery(String.class);
		Root<EventParticipant> eventParticipantRoot = cq.from(EventParticipant.class);
		Join<EventParticipant, Event> event = eventParticipantRoot.join(EventParticipant.EVENT, JoinType.LEFT);

		EventUserFilterCriteria eventUserFilterCriteria = new EventUserFilterCriteria();
		eventUserFilterCriteria.includeUserCaseAndEventParticipantFilter(true);
		eventUserFilterCriteria.forceRegionJurisdiction(true);

		Predicate filter =
			eventService.createUserFilter(cb, cq, eventParticipantRoot.join(EventParticipant.EVENT, JoinType.LEFT), eventUserFilterCriteria);

		if (since != null) {
			Predicate dateFilter = createChangeDateFilter(cb, eventParticipantRoot, since);
			if (filter != null) {
				filter = cb.and(filter, dateFilter);
			} else {
				filter = dateFilter;
			}
		}

		Predicate deletedFilter = cb.equal(eventParticipantRoot.get(EventParticipant.DELETED), true);
		if (filter != null) {
			filter = cb.and(filter, deletedFilter);
		} else {
			filter = deletedFilter;
		}

		cq.where(filter);
		cq.select(eventParticipantRoot.get(EventParticipant.UUID));

		return em.createQuery(cq).getResultList();
	}

	public Predicate createDefaultFilter(CriteriaBuilder cb, From<?, EventParticipant> root) {
		return cb.isFalse(root.get(EventParticipant.DELETED));
	}

	public Optional<EventParticipant> getFirst(EventParticipantCriteria criteria) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<EventParticipant> cq = cb.createQuery(EventParticipant.class);
		Root<EventParticipant> eventParticipant = cq.from(EventParticipant.class);
		EventParticipantQueryContext eventParticipantQueryContext = new EventParticipantQueryContext(cb, cq, eventParticipant);

		Predicate filter = buildCriteriaFilter(criteria, eventParticipantQueryContext);
		cq.where(filter);
		cq.orderBy(cb.asc(eventParticipant.get(EventParticipant.UUID)));

		try {
			return Optional.of(em.createQuery(cq).setFirstResult(0).setMaxResults(1).getSingleResult());
		} catch (NoResultException e) {
			return Optional.empty();
		}
	}

	public List<EventParticipant> getByEventUuids(List<String> eventUuids) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<EventParticipant> cq = cb.createQuery(EventParticipant.class);
		Root<EventParticipant> epRoot = cq.from(EventParticipant.class);
		Join<EventParticipant, Event> eventJoin = epRoot.join(EventParticipant.EVENT, JoinType.LEFT);

		Predicate filter = cb.and(createDefaultFilter(cb, epRoot), eventJoin.get(AbstractDomainObject.UUID).in(eventUuids));

		cq.where(filter);
		return em.createQuery(cq).getResultList();
	}

	@Override
	public Predicate createChangeDateFilter(CriteriaBuilder cb, From<?, EventParticipant> from, Timestamp date) {
		Predicate dateFilter = super.createChangeDateFilter(cb, from, date);
		dateFilter =
			cb.or(dateFilter, vaccinationInfoService.createChangeDateFilter(cb, from.join(EventParticipant.VACCINATION_INFO, JoinType.LEFT), date));
		dateFilter = cb.or(dateFilter, changeDateFilter(cb, date, from, EventParticipant.SHARE_INFO_EVENT_PARTICIPANTS));

		return dateFilter;

	}

	public boolean isEventParticiapntEditAllowed(EventParticipant eventParticipant) {
		if (eventParticipant.getSormasToSormasOriginInfo() != null) {
			return eventParticipant.getSormasToSormasOriginInfo().isOwnershipHandedOver();
		}

		return eventParticipantJurisdictionChecker.isInJurisdiction(eventParticipant)
			&& !sormasToSormasShareInfoService.isEventParticipantOwnershipHandedOver(eventParticipant);
	}

	public Collection<EventParticipant> getByPersonUuids(List<String> personUuids) {
		if (CollectionUtils.isEmpty(personUuids)) {
			// Avoid empty IN clause
			return Collections.emptyList();
		} else if (personUuids.size() > ModelConstants.PARAMETER_LIMIT) {
			List<EventParticipant> eventParticipants = new LinkedList<>();
			IterableHelper.executeBatched(
				personUuids,
				ModelConstants.PARAMETER_LIMIT,
				batchedPersonUuids -> eventParticipants.addAll(getEventParticipantsByPersonUuids(batchedPersonUuids)));
			return eventParticipants;
		} else {
			return getEventParticipantsByPersonUuids(personUuids);
		}
	}

	private List<EventParticipant> getEventParticipantsByPersonUuids(List<String> personUuids) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<EventParticipant> cq = cb.createQuery(EventParticipant.class);
		Root<EventParticipant> eventParticipantRoot = cq.from(EventParticipant.class);
		Join<EventParticipant, Person> personJoin = eventParticipantRoot.join(Contact.PERSON, JoinType.LEFT);

		cq.where(personJoin.get(AbstractDomainObject.UUID).in(personUuids));

		return em.createQuery(cq).getResultList();
	}

	public Predicate isInJurisdictionOrOwned(
		CriteriaBuilder cb,
		CriteriaQuery<?> cq,
		Root<EventParticipant> eventParticipantRoot,
		EventParticipantJoins joins) {

		final User currentUser = this.getCurrentUser();

		final Predicate reportedByCurrentUser = cb.and(
			cb.isNotNull(joins.getEventParticipantReportingUser()),
			cb.equal(joins.getEventParticipantReportingUser().get(User.UUID), currentUser.getUuid()));

		final Predicate jurisdictionPredicate =
			EventParticipantJurisdictionPredicateValidator.of(cb, joins, currentUser).isInJurisdiction(currentUser.getJurisdictionLevel());

		return cb.or(reportedByCurrentUser, jurisdictionPredicate);
	}

}
