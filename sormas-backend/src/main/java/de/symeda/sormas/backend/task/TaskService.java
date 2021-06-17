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
package de.symeda.sormas.backend.task;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import de.symeda.sormas.api.EntityRelevanceStatus;
import de.symeda.sormas.api.task.TaskContext;
import de.symeda.sormas.api.task.TaskCriteria;
import de.symeda.sormas.api.task.TaskJurisdictionFlagsDto;
import de.symeda.sormas.api.task.TaskPriority;
import de.symeda.sormas.api.task.TaskStatus;
import de.symeda.sormas.api.user.JurisdictionLevel;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.backend.caze.Case;
import de.symeda.sormas.backend.caze.CaseService;
import de.symeda.sormas.backend.common.AdoServiceWithUserFilter;
import de.symeda.sormas.backend.common.CriteriaBuilderHelper;
import de.symeda.sormas.backend.common.TaskCreationException;
import de.symeda.sormas.backend.contact.Contact;
import de.symeda.sormas.backend.contact.ContactJoins;
import de.symeda.sormas.backend.contact.ContactService;
import de.symeda.sormas.backend.event.Event;
import de.symeda.sormas.backend.event.EventService;
import de.symeda.sormas.backend.person.Person;
import de.symeda.sormas.backend.region.District;
import de.symeda.sormas.backend.region.Region;
import de.symeda.sormas.backend.sample.SampleJurisdictionPredicateValidator;
import de.symeda.sormas.backend.user.User;
import de.symeda.sormas.backend.user.UserService;
import de.symeda.sormas.backend.util.JurisdictionHelper;
import de.symeda.sormas.utils.CaseJoins;
import de.symeda.sormas.utils.EventJoins;

@Stateless
@LocalBean
public class TaskService extends AdoServiceWithUserFilter<Task> {

	@EJB
	private CaseService caseService;
	@EJB
	private ContactService contactService;
	@EJB
	private EventService eventService;
	@EJB
	private UserService userService;

	public TaskService() {
		super(Task.class);
	}

	public List<Task> getAllActiveTasksAfter(Date date, User user) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Task> cq = cb.createQuery(getElementClass());
		Root<Task> from = cq.from(getElementClass());

		Predicate filter = buildActiveTasksFilter(cb, from);

		if (user != null) {
			Predicate userFilter = createUserFilter(cb, cq, from);
			filter = CriteriaBuilderHelper.and(cb, filter, userFilter);
		}

		if (date != null) {
			Predicate dateFilter = createChangeDateFilter(cb, from, date);
			filter = CriteriaBuilderHelper.and(cb, filter, dateFilter);
		}

		cq.where(filter);
		cq.orderBy(cb.desc(from.get(Task.CHANGE_DATE)));
		cq.distinct(true);

		return em.createQuery(cq).getResultList();
	}

	public List<String> getAllActiveUuids(User user) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<String> cq = cb.createQuery(String.class);
		Root<Task> from = cq.from(getElementClass());

		Predicate filter = buildActiveTasksFilter(cb, from);

		if (user != null) {
			Predicate userFilter = createUserFilter(cb, cq, from);
			filter = CriteriaBuilderHelper.and(cb, filter, userFilter);
		}

		cq.where(filter);
		cq.select(from.get(Task.UUID));

		return em.createQuery(cq).getResultList();
	}

	/**
	 * @see /sormas-backend/doc/UserDataAccess.md
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Predicate createUserFilter(CriteriaBuilder cb, CriteriaQuery cq, From<?, Task> taskPath) {

		Join<Object, User> assigneeUser = taskPath.join(Task.ASSIGNEE_USER, JoinType.LEFT);

		Predicate assigneeFilter = createAssigneeFilter(cb, assigneeUser);

		// National users can access all tasks in the system that are assigned in their jurisdiction
		User currentUser = getCurrentUser();
		final JurisdictionLevel jurisdictionLevel = currentUser.getJurisdictionLevel();
		if (currentUser == null
			|| (jurisdictionLevel == JurisdictionLevel.NATION && !UserRole.isPortHealthUser(currentUser.getUserRoles()))
			|| currentUser.hasAnyUserRole(UserRole.REST_USER)) {
			return assigneeFilter;
		}

		// whoever created the task or is assigned to it is allowed to access it
		Predicate filter = cb.equal(taskPath.join(Task.CREATOR_USER, JoinType.LEFT), currentUser);
		filter = cb.or(filter, cb.equal(assigneeUser, currentUser));

		Predicate caseFilter = caseService.createUserFilter(cb, cq, taskPath.join(Task.CAZE, JoinType.LEFT));
		if (caseFilter != null) {
			filter = cb.or(filter, caseFilter);
		}
		Predicate contactFilter = contactService.createUserFilter(cb, cq, taskPath.join(Task.CONTACT, JoinType.LEFT));
		if (contactFilter != null) {
			filter = cb.or(filter, contactFilter);
		}
		Predicate eventFilter = eventService.createUserFilter(cb, cq, taskPath.join(Task.EVENT, JoinType.LEFT));
		if (eventFilter != null) {
			filter = cb.or(filter, eventFilter);
		}

		return CriteriaBuilderHelper.and(cb, filter, assigneeFilter);
	}

	public Predicate createAssigneeFilter(CriteriaBuilder cb, Join<?, User> assigneeUserJoin) {
		return CriteriaBuilderHelper.or(cb, cb.isNull(assigneeUserJoin.get(User.UUID)), userService.createJurisdictionFilter(cb, assigneeUserJoin));
	}

	public long getCount(TaskCriteria taskCriteria) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<Task> from = cq.from(getElementClass());

		Predicate filter = buildCriteriaFilter(taskCriteria, cb, from);
		if (filter != null) {
			cq.where(filter);
		}

		cq.select(cb.countDistinct(from));

		return em.createQuery(cq).getSingleResult();
	}

	public List<Task> findBy(TaskCriteria taskCriteria, boolean ignoreUserFilter) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Task> cq = cb.createQuery(getElementClass());
		Root<Task> from = cq.from(getElementClass());

		Predicate filter = buildCriteriaFilter(taskCriteria, cb, from);
		if (!ignoreUserFilter) {
			filter = CriteriaBuilderHelper.and(cb, filter, createUserFilter(cb, cq, from));
		}

		if (filter != null) {
			cq.where(filter);
		}
		cq.orderBy(cb.asc(from.get(Task.CREATION_DATE)));

		return em.createQuery(cq).getResultList();
	}

	public List<String> getArchivedUuidsSince(Date since) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<String> cq = cb.createQuery(String.class);
		Root<Task> taskRoot = cq.from(Task.class);

		Predicate filter = createUserFilter(cb, cq, taskRoot);
		if (since != null) {
			Predicate dateFilter = cb.greaterThanOrEqualTo(taskRoot.get(Task.CHANGE_DATE), since);
			if (filter != null) {
				filter = cb.and(filter, dateFilter);
			} else {
				filter = dateFilter;
			}
		}

		Predicate archivedFilter = cb.equal(taskRoot.get(Task.ARCHIVED), true);
		if (filter != null) {
			filter = cb.and(filter, archivedFilter);
		} else {
			filter = archivedFilter;
		}

		cq.where(filter);
		cq.select(taskRoot.get(Task.UUID));

		return em.createQuery(cq).getResultList();
	}

	public Predicate buildCriteriaFilter(TaskCriteria taskCriteria, CriteriaBuilder cb, Root<Task> from) {
		return buildCriteriaFilter(taskCriteria, cb, from, new TaskJoins(from));
	}

	public Predicate buildCriteriaFilter(TaskCriteria taskCriteria, CriteriaBuilder cb, Root<Task> from, TaskJoins joins) {

		Predicate filter = null;

		if (taskCriteria.getTaskStatus() != null) {
			filter = CriteriaBuilderHelper.and(cb, filter, cb.equal(from.get(Task.TASK_STATUS), taskCriteria.getTaskStatus()));
		}
		if (taskCriteria.getTaskType() != null) {
			filter = CriteriaBuilderHelper.and(cb, filter, cb.equal(from.get(Task.TASK_TYPE), taskCriteria.getTaskType()));
		}
		if (taskCriteria.getAssigneeUser() != null) {
			filter = CriteriaBuilderHelper.and(cb, filter, cb.equal(joins.getAssignee().get(User.UUID), taskCriteria.getAssigneeUser().getUuid()));
		}
		if (taskCriteria.getExcludeAssigneeUser() != null) {
			filter = CriteriaBuilderHelper
				.and(cb, filter, cb.notEqual(joins.getAssignee().get(User.UUID), taskCriteria.getExcludeAssigneeUser().getUuid()));
		}
		if (taskCriteria.getCaze() != null) {
			filter = CriteriaBuilderHelper.and(cb, filter, cb.equal(joins.getCaze().get(Case.UUID), taskCriteria.getCaze().getUuid()));
		}
		if (taskCriteria.getContact() != null) {
			filter = CriteriaBuilderHelper.and(cb, filter, cb.equal(joins.getContact().get(Contact.UUID), taskCriteria.getContact().getUuid()));
		}
		if (taskCriteria.getContactPerson() != null) {
			filter =
				CriteriaBuilderHelper.and(cb, filter, cb.equal(joins.getContactPerson().get(User.UUID), taskCriteria.getContactPerson().getUuid()));
		}
		if (taskCriteria.getEvent() != null) {
			filter = CriteriaBuilderHelper.and(cb, filter, cb.equal(joins.getEvent().get(Event.UUID), taskCriteria.getEvent().getUuid()));
		}
		if (taskCriteria.getDueDateFrom() != null && taskCriteria.getDueDateTo() != null) {
			filter = CriteriaBuilderHelper.and(cb, filter, cb.greaterThanOrEqualTo(from.get(Task.DUE_DATE), taskCriteria.getDueDateFrom()));
			filter = CriteriaBuilderHelper.and(cb, filter, cb.lessThan(from.get(Task.DUE_DATE), taskCriteria.getDueDateTo()));
		}
		if (taskCriteria.getStartDateFrom() != null && taskCriteria.getStartDateTo() != null) {
			filter = CriteriaBuilderHelper.and(cb, filter, cb.greaterThanOrEqualTo(from.get(Task.SUGGESTED_START), taskCriteria.getStartDateFrom()));
			filter = CriteriaBuilderHelper.and(cb, filter, cb.lessThan(from.get(Task.SUGGESTED_START), taskCriteria.getStartDateTo()));
		}
		if (taskCriteria.getStatusChangeDateFrom() != null && taskCriteria.getStatusChangeDateTo() != null) {
			filter = CriteriaBuilderHelper
				.and(cb, filter, cb.greaterThanOrEqualTo(from.get(Task.STATUS_CHANGE_DATE), taskCriteria.getStatusChangeDateFrom()));
			filter = CriteriaBuilderHelper.and(cb, filter, cb.lessThan(from.get(Task.STATUS_CHANGE_DATE), taskCriteria.getStatusChangeDateTo()));
		}
		if (taskCriteria.getRelevanceStatus() != null) {
			if (taskCriteria.getRelevanceStatus() == EntityRelevanceStatus.ACTIVE) {
				filter = CriteriaBuilderHelper.and(cb, filter, buildActiveTasksFilter(cb, from));
			} else if (taskCriteria.getRelevanceStatus() == EntityRelevanceStatus.ARCHIVED) {
				filter = CriteriaBuilderHelper.and(
					cb,
					filter,
					cb.or(
						cb.isTrue(from.get(Task.ARCHIVED)),
						cb.and(cb.equal(from.get(Task.TASK_CONTEXT), TaskContext.CASE), cb.equal(joins.getCaze().get(Case.ARCHIVED), true)),
						cb.and(cb.equal(from.get(Task.TASK_CONTEXT), TaskContext.CONTACT), cb.equal(joins.getContactCase().get(Case.ARCHIVED), true)),
						cb.and(cb.equal(from.get(Task.TASK_CONTEXT), TaskContext.EVENT), cb.equal(joins.getEvent().get(Event.ARCHIVED), true))));
			}
		}
		if (taskCriteria.getTaskContext() != null) {
			filter = CriteriaBuilderHelper.and(cb, filter, cb.equal(from.get(Task.TASK_CONTEXT), taskCriteria.getTaskContext()));
		}
		if (taskCriteria.getRegion() != null) {
			Expression<Object> region = cb.selectCase()
				.when(cb.isNotNull(joins.getCaseRegion()), joins.getCaseRegion().get(Region.UUID))
				.otherwise(
					cb.selectCase()
						.when(cb.isNotNull(joins.getContactRegion()), joins.getContactRegion().get(Region.UUID))
						.otherwise(joins.getEventRegion().get(Region.UUID)));
			filter = CriteriaBuilderHelper.and(cb, filter, cb.equal(region, taskCriteria.getRegion().getUuid()));
		}
		if (taskCriteria.getDistrict() != null) {
			Expression<Object> district = cb.selectCase()
				.when(cb.isNotNull(joins.getCaseDistrict()), joins.getCaseDistrict().get(District.UUID))
				.otherwise(
					cb.selectCase()
						.when(cb.isNotNull(joins.getContactDistrict()), joins.getContactDistrict().get(District.UUID))
						.otherwise(joins.getEventDistrict().get(District.UUID)));
			filter = CriteriaBuilderHelper.and(cb, filter, cb.equal(district, taskCriteria.getDistrict().getUuid()));
		}
		if (taskCriteria.getFreeText() != null) {
			String[] textFilters = taskCriteria.getFreeText().split("\\s+");
			for (String textFilter : textFilters) {
				if (DataHelper.isNullOrEmpty(textFilter)) {
					continue;
				}

				Predicate likeFilters = cb.or(
					CriteriaBuilderHelper.ilike(cb, joins.getCaze().get(Case.UUID), textFilter),
					CriteriaBuilderHelper.unaccentedIlike(cb, joins.getCasePerson().get(Person.LAST_NAME), textFilter),
					CriteriaBuilderHelper.unaccentedIlike(cb, joins.getCasePerson().get(Person.FIRST_NAME), textFilter),
					CriteriaBuilderHelper.ilike(cb, joins.getContact().get(Contact.UUID), textFilter),
					CriteriaBuilderHelper.unaccentedIlike(cb, joins.getContactPerson().get(Person.LAST_NAME), textFilter),
					CriteriaBuilderHelper.unaccentedIlike(cb, joins.getContactPerson().get(Person.FIRST_NAME), textFilter),
					CriteriaBuilderHelper.ilike(cb, joins.getEvent().get(Event.UUID), textFilter),
					CriteriaBuilderHelper.unaccentedIlike(cb, joins.getEvent().get(Event.EVENT_TITLE), textFilter));
				filter = CriteriaBuilderHelper.and(cb, filter, likeFilters);
			}
		}
		if (taskCriteria.getAssigneeUserLike() != null) {
			String[] textFilters = taskCriteria.getAssigneeUserLike().split("\\s+");
			for (String textFilter : textFilters) {
				if (DataHelper.isNullOrEmpty(textFilter)) {
					continue;
				}

				Predicate likeFilters = cb.or(
					CriteriaBuilderHelper.unaccentedIlike(cb, joins.getAssignee().get(User.LAST_NAME), textFilter),
					CriteriaBuilderHelper.unaccentedIlike(cb, joins.getAssignee().get(User.FIRST_NAME), textFilter),
					CriteriaBuilderHelper.unaccentedIlike(cb, joins.getAssignee().get(User.USER_NAME), textFilter));
				filter = CriteriaBuilderHelper.and(cb, filter, likeFilters);
			}
		}
		if (taskCriteria.getCreatorUserLike() != null) {
			String[] textFilters = taskCriteria.getCreatorUserLike().split("\\s+");
			for (String textFilter : textFilters) {
				if (DataHelper.isNullOrEmpty(textFilter)) {
					continue;
				}

				Predicate likeFilters = cb.or(
					CriteriaBuilderHelper.ilike(cb, joins.getCaze().get(Case.UUID), textFilter),
					CriteriaBuilderHelper.unaccentedIlike(cb, joins.getCreator().get(User.LAST_NAME), textFilter),
					CriteriaBuilderHelper.unaccentedIlike(cb, joins.getCreator().get(User.FIRST_NAME), textFilter),
					CriteriaBuilderHelper.unaccentedIlike(cb, joins.getCreator().get(User.USER_NAME), textFilter));
				filter = CriteriaBuilderHelper.and(cb, filter, likeFilters);
			}
		}

		return filter;
	}

	private Predicate buildActiveTasksFilter(CriteriaBuilder cb, Root<Task> from) {

		Join<Task, Case> caze = from.join(Task.CAZE, JoinType.LEFT);
		Join<Task, Contact> contact = from.join(Task.CONTACT, JoinType.LEFT);
		Join<Contact, Case> contactCaze = contact.join(Contact.CAZE, JoinType.LEFT);
		Join<Task, Event> event = from.join(Task.EVENT, JoinType.LEFT);

		return cb.and(
			cb.isFalse(from.get(Task.ARCHIVED)),
			cb.or(
				cb.equal(from.get(Task.TASK_CONTEXT), TaskContext.GENERAL),
				cb.and(
					cb.equal(from.get(Task.TASK_CONTEXT), TaskContext.CASE),
					cb.or(cb.equal(caze.get(Case.ARCHIVED), false), cb.isNull(caze.get(Case.ARCHIVED)))),
				cb.and(
					cb.equal(from.get(Task.TASK_CONTEXT), TaskContext.CONTACT),
					cb.or(cb.equal(contactCaze.get(Case.ARCHIVED), false), cb.isNull(contactCaze.get(Case.ARCHIVED)))),
				cb.and(
					cb.equal(from.get(Task.TASK_CONTEXT), TaskContext.EVENT),
					cb.or(cb.equal(event.get(Event.ARCHIVED), false), cb.isNull(event.get(Event.ARCHIVED))))));
	}

	public Task buildTask(User creatorUser) {

		Task task = new Task();
		task.setCreatorUser(creatorUser);
		task.setPriority(TaskPriority.NORMAL);
		task.setTaskStatus(TaskStatus.PENDING);
		return task;
	}

	public User getTaskAssignee(Contact contact) throws TaskCreationException {

		User assignee = null;
		if (contact.getContactOfficer() != null) {
			// 1) The contact officer that is responsible for the contact
			assignee = contact.getContactOfficer();
		} else {
			// 2) A random contact officer from the contact's, contact person's or contact case's district
			Function<District, User> lookupByDistrict = district -> userService.getRandomUser(district, UserRole.CONTACT_OFFICER);
			if (contact.getDistrict() != null) {
				assignee = lookupByDistrict.apply(contact.getDistrict());
			}
			if (assignee == null && contact.getPerson().getAddress().getDistrict() != null) {
				assignee = lookupByDistrict.apply(contact.getPerson().getAddress().getDistrict());
			}
			if (assignee == null && contact.getCaze() != null && contact.getCaze().getDistrict() != null) {
				assignee = lookupByDistrict.apply(contact.getCaze().getDistrict());
			}
		}

		if (assignee == null) {
			// 3) Assign a random contact supervisor from the contact's, contact person's or contact case's region
			Function<Region, User> lookupByRegion = region -> userService.getRandomUser(region, UserRole.CONTACT_SUPERVISOR);
			if (contact.getRegion() != null) {
				assignee = lookupByRegion.apply(contact.getRegion());
			}
			if (assignee == null && contact.getPerson().getAddress().getRegion() != null) {
				assignee = lookupByRegion.apply(contact.getPerson().getAddress().getRegion());
			}
			if (assignee == null && contact.getCaze() != null && contact.getCaze().getResponsibleRegion() != null) {
				assignee = lookupByRegion.apply(contact.getCaze().getResponsibleRegion());
			}
			if (assignee == null && contact.getCaze() != null && contact.getCaze().getRegion() != null) {
				assignee = lookupByRegion.apply(contact.getCaze().getRegion());
			}
			if (assignee == null) {
				throw new TaskCreationException("Contact has not contact officer and no region - can't create follow-up task: " + contact.getUuid());
			}
		}

		return assignee;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void updateArchived(List<String> taskUuids, boolean archived) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaUpdate<Task> cu = cb.createCriteriaUpdate(Task.class);
		Root<Task> root = cu.from(Task.class);

		cu.set(Task.CHANGE_DATE, Timestamp.from(Instant.now()));
		cu.set(root.get(Task.ARCHIVED), archived);

		cu.where(root.get(Task.UUID).in(taskUuids));

		em.createQuery(cu).executeUpdate();
	}

	public TaskJurisdictionFlagsDto inJurisdictionOrOwned(Task task) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<TaskJurisdictionFlagsDto> cq = cb.createQuery(TaskJurisdictionFlagsDto.class);
		Root<Task> root = cq.from(Task.class);

		TaskJoins joins = new TaskJoins(root);

		ContactJoins<Task> contactJoins = new ContactJoins<>(joins.getContact());
		cq.multiselect(
				JurisdictionHelper.jurisdictionSelector(cb, inJurisdictionOrOwned(cb, joins)),
				JurisdictionHelper.jurisdictionSelector(
						cb,
						cb.and(cb.isNotNull(joins.getCaze()), caseService.inJurisdictionOrOwned(cb, new CaseJoins<>(joins.getCaze())))),
				JurisdictionHelper
						.jurisdictionSelector(cb, cb.and(cb.isNotNull(joins.getContact()), contactService.inJurisdictionOrOwned(cb, contactJoins))),
				JurisdictionHelper.jurisdictionSelector(
						cb,
						cb.and(
								cb.isNotNull(joins.getContact()),
								cb.isNotNull(contactJoins.getCaze()),
								caseService.inJurisdictionOrOwned(cb, new CaseJoins<>(contactJoins.getCaze())))),
				JurisdictionHelper.jurisdictionSelector(
						cb,
						cb.and(cb.isNotNull(joins.getEvent()), eventService.inJurisdictionOrOwned(cb, new EventJoins<>(joins.getEvent())))));

		cq.where(cb.equal(root.get(Task.UUID), task.getUuid()));


		return em.createQuery(cq).getResultList().stream().findFirst().orElse(null);
	}

	public Predicate inJurisdictionOrOwned(CriteriaBuilder cb, TaskJoins joins) {
		final User currentUser = userService.getCurrentUser();
		return TaskJurisdictionPredicateValidator.of(cb, joins, currentUser).inJurisdictionOrOwned();
	}
}
