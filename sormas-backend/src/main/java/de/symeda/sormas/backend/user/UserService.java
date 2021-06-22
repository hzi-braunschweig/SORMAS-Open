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
package de.symeda.sormas.backend.user;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import de.symeda.sormas.backend.region.Community;
import org.apache.commons.collections4.CollectionUtils;

import de.symeda.sormas.api.AuthProvider;
import de.symeda.sormas.api.facility.FacilityType;
import de.symeda.sormas.api.region.RegionReferenceDto;
import de.symeda.sormas.api.user.JurisdictionLevel;
import de.symeda.sormas.api.user.UserCriteria;
import de.symeda.sormas.api.user.UserRight;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.api.utils.DefaultUserHelper;
import de.symeda.sormas.api.utils.PasswordHelper;
import de.symeda.sormas.backend.caze.Case;
import de.symeda.sormas.backend.common.AbstractDomainObject;
import de.symeda.sormas.backend.common.AdoServiceWithUserFilter;
import de.symeda.sormas.backend.common.CriteriaBuilderHelper;
import de.symeda.sormas.backend.event.Event;
import de.symeda.sormas.backend.facility.Facility;
import de.symeda.sormas.backend.region.District;
import de.symeda.sormas.backend.region.Region;
import de.symeda.sormas.backend.util.IterableHelper;
import de.symeda.sormas.backend.util.ModelConstants;

@Stateless
@LocalBean
public class UserService extends AdoServiceWithUserFilter<User> {

	@EJB
	private UserRoleConfigFacadeEjb.UserRoleConfigFacadeEjbLocal userRoleConfigFacade;

	public UserService() {
		super(User.class);
	}

	public User createUser() {

		User user = new User();
		// dummy password to make sure no one can login with this user
		String password = PasswordHelper.createPass(12);
		user.setSeed(PasswordHelper.createPass(16));
		user.setPassword(PasswordHelper.encodePassword(password, user.getSeed()));
		return user;
	}

	@Override
	public User getCurrentUser() {
		return super.getCurrentUser();
	}

	public User getByUserName(String userName) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		ParameterExpression<String> userNameParam = cb.parameter(String.class, User.USER_NAME);
		CriteriaQuery<User> cq = cb.createQuery(getElementClass());
		Root<User> from = cq.from(getElementClass());

		Expression<String> userNameExpression = from.get(User.USER_NAME);
		String userNameParamValue = userName;
		if (!AuthProvider.getProvider().isUsernameCaseSensitive()) {
			userNameExpression = cb.lower(userNameExpression);
			userNameParamValue = userName.toLowerCase();
		}

		cq.where(cb.equal(userNameExpression, userNameParam));

		TypedQuery<User> q = em.createQuery(cq).setParameter(userNameParam, userNameParamValue);

		User entity = q.getResultList().stream().findFirst().orElse(null);
		return entity;
	}

	public List<User> getAllByRegionAndUserRoles(Region region, UserRole... userRoles) {
		return getAllByRegionsAndUserRoles(Collections.singletonList(region), Arrays.asList(userRoles), null);
	}

	public List<User> getAllByRegionsAndUserRoles(List<Region> regions, UserRole... userRoles) {
		return getAllByRegionsAndUserRoles(regions, Arrays.asList(userRoles), null);
	}

	public List<User> getAllByRegionAndUserRolesInJurisdiction(Region region, UserRole... userRoles) {
		return getAllByRegionsAndUserRoles(Collections.singletonList(region), Arrays.asList(userRoles), this::createJurisdictionFilter);
	}

	/**
	 * @see #getReferenceList(List, List, List, boolean, boolean, boolean, List) This method is partly a duplication for getReferenceList,
	 *      but it's still in use for WeeklyReports and messageRecipients where more information of the user is needed
	 *      and method signatures rely on {@link User}.
	 */
	private List<User> getAllByRegionsAndUserRoles(
		List<Region> regions,
		Collection<UserRole> userRoles,
		BiFunction<CriteriaBuilder, Root<User>, Predicate> createExtraFilters) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<User> cq = cb.createQuery(getElementClass());
		Root<User> from = cq.from(getElementClass());

		Predicate filter = createDefaultFilter(cb, from);
		if (regions != null) {
			filter = from.get(User.REGION).in(regions);
		}

		if (userRoles.size() > 0) {
			Join<User, UserRole> joinRoles = from.join(User.USER_ROLES, JoinType.LEFT);
			Predicate rolesFilter = joinRoles.in(userRoles);
			filter = CriteriaBuilderHelper.and(cb, filter, rolesFilter);
		}

		if (createExtraFilters != null) {
			filter = CriteriaBuilderHelper.and(cb, filter, createExtraFilters.apply(cb, from));
		}

		if (filter != null) {
			cq.where(filter);
		}

		cq.distinct(true).orderBy(cb.asc(from.get(AbstractDomainObject.ID)));

		return em.createQuery(cq).getResultList();
	}

	/**
	 * Loads users filtered by combinable filter conditions.<br />
	 * Condition combination if parameter is set:<br />
	 * {@code ((regionUuids & districtUuids & filterByJurisdiction & userRoles) | includeSupervisors) & activeOnly}
	 * 
	 * @see #createJurisdictionFilter(CriteriaBuilder, From)
	 * @param regionUuids
	 * @param districtUuids
	 * @param includeSupervisors
	 *            If set to {@code true}, all supervisors are returned independent of other filters.
	 * @param filterByJurisdiction
	 * @param activeOnly
	 * @param userRoles
	 */
	public List<UserReference> getReferenceList(
		List<String> regionUuids,
		List<String> districtUuids,
		boolean includeSupervisors,
		boolean filterByJurisdiction,
		boolean activeOnly,
		UserRole... userRoles) {

		return getReferenceList(regionUuids, districtUuids, null, includeSupervisors, filterByJurisdiction, activeOnly, Arrays.asList(userRoles));
	}

	/**
	 * Loads users filtered by combinable filter conditions.<br />
	 * Condition combination if parameter is set:<br />
	 * {@code ((regionUuids & districtUuids & communityUuids & filterByJurisdiction & userRoles) | includeSupervisors) & activeOnly}
	 *
	 * @see #createJurisdictionFilter(CriteriaBuilder, From)
	 * @param regionUuids
	 * @param districtUuids
	 * @param communityUuids
	 * @param includeSupervisors
	 *            If set to {@code true}, all supervisors are returned independent of other filters.
	 * @param filterByJurisdiction
	 * @param activeOnly
	 * @param userRoles
	 */
	public List<UserReference> getReferenceList(
			List<String> regionUuids,
			List<String> districtUuids,
			List<String> communityUuids,
			boolean includeSupervisors,
			boolean filterByJurisdiction,
			boolean activeOnly,
			List<UserRole> userRoles) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<UserReference> cq = cb.createQuery(UserReference.class);
		Root<UserReference> root = cq.from(UserReference.class);
		Join<UserReference, UserRole> rolesJoin = root.join(User.USER_ROLES, JoinType.LEFT);
		Root<User> userRoot = cq.from(User.class);
		cq.select(root);

		// WHERE inner AND
		Predicate filter = null;
		boolean userEntityJoinUsed = false;

		if (CollectionUtils.isNotEmpty(regionUuids)) {
			Join<User, Region> regionJoin = userRoot.join(User.REGION, JoinType.LEFT);
			filter = CriteriaBuilderHelper.and(cb, filter, cb.in(regionJoin.get(Region.UUID)).value(regionUuids));
			userEntityJoinUsed = true;
		}
		if (CollectionUtils.isNotEmpty(districtUuids)) {
			Join<User, District> districtJoin = userRoot.join(User.DISTRICT, JoinType.LEFT);
			filter = CriteriaBuilderHelper.and(cb, filter, cb.in(districtJoin.get(District.UUID)).value(districtUuids));
			userEntityJoinUsed = true;
		}
		if (filterByJurisdiction) {
			filter = CriteriaBuilderHelper.and(cb, filter, createJurisdictionFilter(cb, userRoot));
			userEntityJoinUsed = true;
		}
		if (CollectionUtils.isNotEmpty(userRoles)) {
			filter = CriteriaBuilderHelper.and(cb, filter, rolesJoin.in(userRoles));
		}
		if (userEntityJoinUsed) {
			filter = CriteriaBuilderHelper.and(cb, filter, cb.equal(root.get(UserReference.ID), userRoot.get(User.ID)));
		}

		// WHERE OR
		if (includeSupervisors) {
			Predicate supervisorFilter = rolesJoin.in(
					Arrays.asList(UserRole.CASE_SUPERVISOR, UserRole.CONTACT_SUPERVISOR, UserRole.SURVEILLANCE_SUPERVISOR, UserRole.ADMIN_SUPERVISOR));
			filter = CriteriaBuilderHelper.or(cb, filter, supervisorFilter);
		}

		// WHERE outer AND
		if (activeOnly) {
			filter = CriteriaBuilderHelper.and(cb, filter, createDefaultFilter(cb, root));
		}

		if (CollectionUtils.isNotEmpty(communityUuids)) {
			Join<User, Community> communityJoin = userRoot.join(User.COMMUNITY, JoinType.LEFT);
			filter = CriteriaBuilderHelper.and(cb, filter, cb.in(communityJoin.get(Community.UUID)).value(communityUuids));
		}

		if (filter != null) {
			cq.where(filter);
		}

		cq.distinct(true);
		cq.orderBy(cb.asc(root.get(User.ID)));

		List<UserReference> resultList = em.createQuery(cq).getResultList();
		return resultList;
	}

	public User getRandomUser(District district, UserRole... userRoles) {

		return getRandomUser(getReferenceList(null, Arrays.asList(district.getUuid()), false, false, true, userRoles));
	}

	public User getRandomUser(Region region, UserRole... userRoles) {

		return getRandomUser(getReferenceList(Arrays.asList(region.getUuid()), null, false, false, true, userRoles));
	}

	public User getRandomUser(List<UserReference> candidates) {

		if (CollectionUtils.isEmpty(candidates)) {
			return null;
		}

		UserReference chosenUser = candidates.get(new Random().nextInt(candidates.size()));
		return getByUuid(chosenUser.getUuid());
	}

	public List<User> getInformantsOfFacility(Facility facility) {

		if (facility == null || !FacilityType.HOSPITAL.equals(facility.getType())) {
			throw new IllegalArgumentException("Facility needs to be a hospital");
		}

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<User> cq = cb.createQuery(getElementClass());
		Root<User> from = cq.from(getElementClass());
		Join<User, UserRole> joinRoles = from.join(User.USER_ROLES, JoinType.LEFT);

		Predicate filter = cb.and(
			createDefaultFilter(cb, from),
			cb.equal(from.get(User.HEALTH_FACILITY), facility),
			joinRoles.in(Arrays.asList(UserRole.HOSPITAL_INFORMANT)));

		cq.where(filter).distinct(true);
		return em.createQuery(cq).getResultList();
	}

	public List<User> getLabUsersOfLab(Facility facility) {

		if (facility == null || facility.getType() != FacilityType.LABORATORY) {
			throw new IllegalArgumentException("Facility needs to be a laboratory");
		}

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<User> cq = cb.createQuery(getElementClass());
		Root<User> from = cq.from(getElementClass());
		Join<User, UserRole> joinRoles = from.join(User.USER_ROLES, JoinType.LEFT);

		Predicate filter = cb.and(
			createDefaultFilter(cb, from),
			cb.equal(from.get(User.LABORATORY), facility),
			joinRoles.in(Arrays.asList(UserRole.LAB_USER, UserRole.EXTERNAL_LAB_USER)));
		cq.where(filter).distinct(true);

		return em.createQuery(cq).getResultList();
	}

	/**
	 * @param associatedOfficer
	 * @param userRoles
	 * @return
	 */
	public List<User> getAllByAssociatedOfficer(User associatedOfficer, UserRole... userRoles) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<User> cq = cb.createQuery(getElementClass());
		Root<User> from = cq.from(getElementClass());

		Predicate filter = cb.and(createDefaultFilter(cb, from), cb.equal(from.get(User.ASSOCIATED_OFFICER), associatedOfficer));
		filter = CriteriaBuilderHelper.and(cb, filter, buildUserRolesFilter(from, Arrays.asList(userRoles)));
		cq.where(filter);

		cq.orderBy(cb.asc(from.get(AbstractDomainObject.ID)));

		return em.createQuery(cq).getResultList();
	}

	public Map<String, User> getResponsibleUsersByEventUuids(List<String> eventUuids) {

		Map<String, User> responsibleUserByEventUuid = new HashMap<>();
		IterableHelper.executeBatched(eventUuids, ModelConstants.PARAMETER_LIMIT, batchedEventUuids -> {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
			Root<Event> eventRoot = cq.from(Event.class);
			Join<Event, User> responsibleUserJoin = eventRoot.join(Event.RESPONSIBLE_USER, JoinType.LEFT);

			cq.where(cb.and(createDefaultFilter(cb, responsibleUserJoin), eventRoot.get(Event.UUID).in(batchedEventUuids)));
			cq.multiselect(eventRoot.get(Event.UUID), responsibleUserJoin);

			cq.orderBy(cb.asc(eventRoot.get(Event.UUID)));

			responsibleUserByEventUuid
				.putAll(em.createQuery(cq).getResultList().stream().collect(Collectors.toMap(row -> (String) row[0], row -> (User) row[1])));
		});
		return responsibleUserByEventUuid;
	}

	public boolean isLoginUnique(String uuid, String userName) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		ParameterExpression<String> userNameParam = cb.parameter(String.class, User.USER_NAME);
		CriteriaQuery<User> cq = cb.createQuery(getElementClass());
		Root<User> from = cq.from(getElementClass());

		Expression<String> userNameExpression = from.get(User.USER_NAME);
		String userNameParamValue = userName;
		if (!AuthProvider.getProvider().isUsernameCaseSensitive()) {
			userNameExpression = cb.lower(userNameExpression);
			userNameParamValue = userName.toLowerCase();
		}

		cq.where(cb.equal(userNameExpression, userNameParam));

		TypedQuery<User> q = em.createQuery(cq).setParameter(userNameParam, userNameParamValue);

		User entity = q.getResultList().stream().findFirst().orElse(null);

		return entity == null || entity.getUuid().equals(uuid);
	}

	public String resetPassword(String userUuid) {

		User user = getByUuid(userUuid);

		if (user == null) {
//			logger.warn("resetPassword() for unknown user '{}'", realmUserUuid);
			return null;
		}

		String password = PasswordHelper.createPass(12);
		user.setSeed(PasswordHelper.createPass(16));
		user.setPassword(PasswordHelper.encodePassword(password, user.getSeed()));
		ensurePersisted(user);

		return password;
	}

	public Predicate buildCriteriaFilter(UserCriteria userCriteria, CriteriaBuilder cb, Root<User> from) {

		Predicate filter = null;
		if (userCriteria.getActive() != null) {
			filter = CriteriaBuilderHelper.and(cb, filter, cb.equal(from.get(User.ACTIVE), userCriteria.getActive()));
		}
		if (userCriteria.getUserRole() != null) {
			Join<User, UserRole> joinRoles = from.join(User.USER_ROLES, JoinType.LEFT);
			filter = CriteriaBuilderHelper.and(cb, filter, joinRoles.in(Arrays.asList(userCriteria.getUserRole())));
		}
		if (userCriteria.getRegion() != null) {
			filter = CriteriaBuilderHelper
				.and(cb, filter, cb.equal(from.join(Case.REGION, JoinType.LEFT).get(Region.UUID), userCriteria.getRegion().getUuid()));
		}
		if (userCriteria.getDistrict() != null) {
			filter = CriteriaBuilderHelper
				.and(cb, filter, cb.equal(from.join(Case.DISTRICT, JoinType.LEFT).get(District.UUID), userCriteria.getDistrict().getUuid()));
		}
		if (userCriteria.getFreeText() != null) {
			String[] textFilters = userCriteria.getFreeText().split("\\s+");
			for (String textFilter : textFilters) {
				if (DataHelper.isNullOrEmpty(textFilter)) {
					continue;
				}

				Predicate likeFilters = cb.or(
					CriteriaBuilderHelper.unaccentedIlike(cb, from.get(User.FIRST_NAME), textFilter),
					CriteriaBuilderHelper.unaccentedIlike(cb, from.get(User.LAST_NAME), textFilter),
					CriteriaBuilderHelper.unaccentedIlike(cb, from.get(User.USER_NAME), textFilter),
					CriteriaBuilderHelper.unaccentedIlike(cb, from.get(User.USER_EMAIL), textFilter),
					CriteriaBuilderHelper.ilike(cb, from.get(User.PHONE), textFilter),
					CriteriaBuilderHelper.ilike(cb, from.get(User.UUID), textFilter));
				filter = CriteriaBuilderHelper.and(cb, filter, likeFilters);
			}
		}

		return filter;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Predicate createUserFilter(CriteriaBuilder cb, CriteriaQuery cq, From<?, User> from) {
		// a user can read all other users
		return null;
	}

	/**
	 * Caution: Because this filter joins the users_userroles table, using it can result in duplicate results if the
	 * user in question has more than one user role.
	 */
	public Predicate createJurisdictionFilter(CriteriaBuilder cb, From<?, User> from) {
		if (hasRight(UserRight.SEE_PERSONAL_DATA_OUTSIDE_JURISDICTION)) {
			return null;
		}

		User currentUser = getCurrentUser();

		Predicate regionalOrNationalFilter =
			from.join(User.USER_ROLES, JoinType.LEFT).in(UserRole.getWithJurisdictionLevels(JurisdictionLevel.NATION));

		Predicate jurisdictionFilter = cb.conjunction();
		if (currentUser.getHealthFacility() != null) {
			jurisdictionFilter = cb.equal(from.get(User.HEALTH_FACILITY), currentUser.getHealthFacility());
		} else if (currentUser.getPointOfEntry() != null) {
			jurisdictionFilter = cb.equal(from.get(User.POINT_OF_ENTRY), currentUser.getPointOfEntry());
		} else if (currentUser.getLaboratory() != null) {
			jurisdictionFilter = cb.equal(from.get(User.LABORATORY), currentUser.getLaboratory());
		} else if (currentUser.getCommunity() != null) {
			jurisdictionFilter = cb.equal(from.get(User.COMMUNITY), currentUser.getCommunity());
		} else if (currentUser.getDistrict() != null) {
			jurisdictionFilter = cb.equal(from.get(User.DISTRICT), currentUser.getDistrict());
		} else if (currentUser.getRegion() != null) {
			jurisdictionFilter = cb.equal(from.get(User.REGION), currentUser.getRegion());
		}

		return CriteriaBuilderHelper.or(cb, regionalOrNationalFilter, jurisdictionFilter);
	}

	public Predicate buildUserRolesFilter(Root<User> from, Collection<UserRole> userRoles) {

		if (userRoles.size() > 0) {
			Join<User, UserRole> joinRoles = from.join(User.USER_ROLES, JoinType.LEFT);
			return joinRoles.in(Collections.singletonList(userRoles));
		}

		return null;
	}

	public Long countByAssignedOfficer(User officer, UserRole... userRoles) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<User> from = cq.from(getElementClass());
		Predicate filter = cb.equal(from.get(User.ASSOCIATED_OFFICER), officer);
		filter = CriteriaBuilderHelper.and(cb, filter, buildUserRolesFilter(from, Arrays.asList(userRoles)));
		cq.where(filter);
		cq.select(cb.count(from));
		return em.createQuery(cq).getSingleResult();
	}

	public Long countByRegion(Region region, UserRole... userRoles) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<User> from = cq.from(getElementClass());
		Predicate filter = cb.equal(from.get(User.REGION), region);
		filter = CriteriaBuilderHelper.and(cb, filter, buildUserRolesFilter(from, Arrays.asList(userRoles)));
		cq.where(filter);
		cq.select(cb.count(from));
		return em.createQuery(cq).getSingleResult();
	}

	public boolean hasRole(UserRole userRoleName) {
		return getCurrentUser().getUserRoles().contains(userRoleName);
	}

	public boolean hasAnyRole(Set<UserRole> typeRoles) {
		Set<UserRole> userRoles = getCurrentUser().getUserRoles();
		return !userRoles.stream().filter(userRole -> typeRoles.contains(userRole)).collect(Collectors.toList()).isEmpty();
	}

	public boolean hasRight(UserRight right) {
		User currentUser = getCurrentUser();
		return userRoleConfigFacade.getEffectiveUserRights(currentUser.getUserRoles().toArray(new UserRole[0])).contains(right);
	}

	public boolean hasRegion(RegionReferenceDto regionReference) {
		User currentUser = getCurrentUser();
		if (currentUser.getRegion() == null) {
			return false;
		}
		return currentUser.getRegion().getUuid().equals(regionReference.getUuid());
	}

	/**
	 * @param root
	 *            root to {@link User} or {@link UserReference}.
	 */
	private Predicate createDefaultFilter(CriteriaBuilder cb, From<?, ?> root) {
		return cb.isTrue(root.get(User.ACTIVE));
	}

	public List<User> getAllActive() {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<User> cq = cb.createQuery(getElementClass());
		Root<User> from = cq.from(getElementClass());
		cq.where(createDefaultFilter(cb, from));
		cq.orderBy(cb.asc(from.get(AbstractDomainObject.ID)));

		return em.createQuery(cq).getResultList();
	}

	public List<User> getAllDefaultUsers() {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<User> cq = cb.createQuery(getElementClass());
		Root<User> from = cq.from(getElementClass());
		cq.where(cb.and(createDefaultFilter(cb, from), from.get(User.USER_NAME).in(DefaultUserHelper.getDefaultUserNames())));
		cq.orderBy(cb.asc(from.get(User.USER_NAME)));

		return em.createQuery(cq).getResultList();
	}
}
