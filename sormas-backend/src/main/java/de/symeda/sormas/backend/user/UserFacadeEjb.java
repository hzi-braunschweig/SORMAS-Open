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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
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
import javax.validation.ValidationException;

import de.symeda.sormas.backend.region.Community;
import org.apache.commons.beanutils.BeanUtils;

import de.symeda.sormas.api.HasUuid;
import de.symeda.sormas.api.common.Page;
import de.symeda.sormas.api.region.DistrictReferenceDto;
import de.symeda.sormas.api.region.RegionReferenceDto;
import de.symeda.sormas.api.user.JurisdictionLevel;
import de.symeda.sormas.api.user.UserCriteria;
import de.symeda.sormas.api.user.UserDto;
import de.symeda.sormas.api.user.UserFacade;
import de.symeda.sormas.api.user.UserReferenceDto;
import de.symeda.sormas.api.user.UserRight;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.api.user.UserRole.UserRoleValidationException;
import de.symeda.sormas.api.user.UserSyncResult;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.api.utils.DefaultUserHelper;
import de.symeda.sormas.api.utils.PasswordHelper;
import de.symeda.sormas.api.utils.SortProperty;
import de.symeda.sormas.backend.caze.Case;
import de.symeda.sormas.backend.caze.CaseFacadeEjb.CaseFacadeEjbLocal;
import de.symeda.sormas.backend.caze.CaseService;
import de.symeda.sormas.backend.contact.Contact;
import de.symeda.sormas.backend.contact.ContactService;
import de.symeda.sormas.backend.event.EventService;
import de.symeda.sormas.backend.facility.Facility;
import de.symeda.sormas.backend.facility.FacilityFacadeEjb;
import de.symeda.sormas.backend.facility.FacilityService;
import de.symeda.sormas.backend.infrastructure.PointOfEntryFacadeEjb;
import de.symeda.sormas.backend.infrastructure.PointOfEntryService;
import de.symeda.sormas.backend.location.Location;
import de.symeda.sormas.backend.location.LocationFacadeEjb;
import de.symeda.sormas.backend.location.LocationFacadeEjb.LocationFacadeEjbLocal;
import de.symeda.sormas.backend.region.CommunityFacadeEjb;
import de.symeda.sormas.backend.region.CommunityService;
import de.symeda.sormas.backend.region.District;
import de.symeda.sormas.backend.region.DistrictFacadeEjb;
import de.symeda.sormas.backend.region.DistrictService;
import de.symeda.sormas.backend.region.RegionFacadeEjb;
import de.symeda.sormas.backend.region.RegionService;
import de.symeda.sormas.backend.user.event.PasswordResetEvent;
import de.symeda.sormas.backend.user.event.UserCreateEvent;
import de.symeda.sormas.backend.user.event.UserUpdateEvent;
import de.symeda.sormas.backend.util.DtoHelper;
import de.symeda.sormas.backend.util.JurisdictionHelper;
import de.symeda.sormas.backend.util.ModelConstants;

@Stateless(name = "UserFacade")
public class UserFacadeEjb implements UserFacade {

	@PersistenceContext(unitName = ModelConstants.PERSISTENCE_UNIT_NAME)
	private EntityManager em;

	@EJB
	private UserService userService;
	@EJB
	private LocationFacadeEjbLocal locationFacade;
	@EJB
	private RegionService regionService;
	@EJB
	private DistrictService districtService;
	@EJB
	private CommunityService communityService;
	@EJB
	private FacilityService facilityService;
	@EJB
	private CaseFacadeEjbLocal caseFacade;
	@EJB
	private CaseService caseService;
	@EJB
	private ContactService contactService;
	@EJB
	private EventService eventService;
	@EJB
	private PointOfEntryService pointOfEntryService;
	@Inject
	private Event<UserCreateEvent> userCreateEvent;
	@Inject
	private Event<UserUpdateEvent> userUpdateEvent;
	@Inject
	private Event<PasswordResetEvent> passwordResetEvent;

	public static UserDto toDto(User source) {

		if (source == null) {
			return null;
		}

		UserDto target = new UserDto();
		DtoHelper.fillDto(target, source);

		target.setActive(source.isActive());
		target.setUserName(source.getUserName());
		target.setFirstName(source.getFirstName());
		target.setLastName(source.getLastName());
		target.setUserEmail(source.getUserEmail());
		target.setPhone(source.getPhone());
		target.setAddress(LocationFacadeEjb.toDto(source.getAddress()));

		target.setRegion(RegionFacadeEjb.toReferenceDto(source.getRegion()));
		target.setDistrict(DistrictFacadeEjb.toReferenceDto(source.getDistrict()));
		target.setCommunity(CommunityFacadeEjb.toReferenceDto(source.getCommunity()));
		target.setHealthFacility(FacilityFacadeEjb.toReferenceDto(source.getHealthFacility()));
		target.setAssociatedOfficer(toReferenceDto(source.getAssociatedOfficer()));
		target.setLaboratory(FacilityFacadeEjb.toReferenceDto(source.getLaboratory()));
		target.setPointOfEntry(PointOfEntryFacadeEjb.toReferenceDto(source.getPointOfEntry()));
		target.setLimitedDisease(source.getLimitedDisease());
		target.setLanguage(source.getLanguage());
		target.setHasConsentedToGdpr(source.isHasConsentedToGdpr());

		source.getUserRoles().size();
		target.setUserRoles(new HashSet<UserRole>(source.getUserRoles()));
		return target;
	}

	public static UserReferenceDto toReferenceDto(User entity) {

		if (entity == null) {
			return null;
		}

		UserReferenceDto dto = new UserReferenceDto(entity.getUuid(), entity.getFirstName(), entity.getLastName(), entity.getUserRoles());
		return dto;
	}

	public static UserReferenceDto toReferenceDto(UserReference entity) {

		if (entity == null) {
			return null;
		}

		UserReferenceDto dto = new UserReferenceDto(entity.getUuid(), entity.getFirstName(), entity.getLastName(), entity.getUserRoles());
		return dto;
	}

	private List<String> toUuidList(HasUuid hasUuid) {

		/*
		 * Supports conversion of a null object into a list with one "null" value in it.
		 * Uncertain if that use case exists, but wasn't suppose to be broken when replacing the Dto to Entity lookup.
		 */
		return Arrays.asList(hasUuid == null ? null : hasUuid.getUuid());
	}

	@Override
	public List<UserReferenceDto> getUsersByRegionAndRoles(RegionReferenceDto regionRef, UserRole... assignableRoles) {

		return userService.getReferenceList(
			toUuidList(regionRef),
			null,
			false,
			true,
			true,
			assignableRoles)
			.stream()
			.map(f -> toReferenceDto(f))
			.collect(Collectors.toList());
	}

	@Override
	public List<UserReferenceDto> getUsersByRegionsAndRoles(List<RegionReferenceDto> regionRefs, UserRole... assignableRoles) {

		return userService.getReferenceList(
			regionRefs.stream().map(RegionReferenceDto::getUuid).collect(Collectors.toList()),
			null,
			false,
			true,
			true,
			assignableRoles)
			.stream()
			.map(UserFacadeEjb::toReferenceDto)
			.collect(Collectors.toList());
	}

	@Override
	/*
	 * Get all users with the next higher jurisdiction, whose location contains the current users location
	 * For facility users, this includes district and community users, if their district/community is identical with that of the facility
	 */
	public List<UserReferenceDto> getUsersWithSuperiorJurisdiction(UserDto user) {
		JurisdictionLevel superordinateJurisdiction =
			JurisdictionHelper.getSuperordinateJurisdiction(UserRole.getJurisdictionLevel(user.getUserRoles()));

		final List<UserReference> superiorUsersList;
		switch (superordinateJurisdiction) {
		case NATION:
			superiorUsersList =
				userService.getReferenceList(null, null, null, false, false, true, UserRole.getWithJurisdictionLevels(superordinateJurisdiction));
			break;
		case REGION:
			superiorUsersList = userService.getReferenceList(
				Arrays.asList(user.getRegion().getUuid()),
				null,
				null,
				false,
				false,
				true,
				UserRole.getWithJurisdictionLevels(superordinateJurisdiction));
			break;
		case DISTRICT:
			// if user is assigned to a facility, but that facility is not assigned to a district, show no superordinate users. Else, show users of the district (and community) in which the facility is located

			District district = null;
			Community community = null;
			List<UserRole> superordinateRoles = UserRole.getWithJurisdictionLevels(superordinateJurisdiction);
			if (user.getDistrict() != null) {
				district = districtService.getByReferenceDto(user.getDistrict());
			} else if (user.getHealthFacility() != null) {
				district = facilityService.getByReferenceDto(user.getHealthFacility()).getDistrict();
				community = facilityService.getByReferenceDto(user.getHealthFacility()).getCommunity();
				superordinateRoles.addAll(UserRole.getWithJurisdictionLevels(JurisdictionLevel.COMMUNITY));
			}

			if(district == null) {
				superiorUsersList = new ArrayList<>();
			} else if (community == null){
				superiorUsersList = userService.getReferenceList(null, Arrays.asList(district.getUuid()), null,false, false, true, superordinateRoles);
			} else {
				superiorUsersList = userService.getReferenceList(null, Arrays.asList(district.getUuid()), Arrays.asList(community.getUuid()), false, false, true, superordinateRoles);
			}

			break;
		default:
			superiorUsersList = null;
			break;
		}

		return superiorUsersList == null ? new ArrayList<>() : superiorUsersList.stream().map(f -> toReferenceDto(f)).collect(Collectors.toList());
	}

	@Override
	public List<UserReferenceDto> getUserRefsByDistrict(DistrictReferenceDto districtRef, boolean includeSupervisors, UserRole... userRoles) {

		return userService.getReferenceList(
			null,
			toUuidList(districtRef),
			includeSupervisors,
			true,
			true,
			userRoles)
			.stream()
			.map(f -> toReferenceDto(f))
			.collect(Collectors.toList());
	}

	@Override
	public List<UserReferenceDto> getUserRefsByDistricts(List<DistrictReferenceDto> districtRefs, boolean includeSupervisors, UserRole... userRoles) {

		return userService.getReferenceList(
			null,
			districtRefs.stream().map(DistrictReferenceDto::getUuid).collect(Collectors.toList()),
			includeSupervisors,
			true,
			true,
			userRoles)
			.stream()
			.map(UserFacadeEjb::toReferenceDto)
			.collect(Collectors.toList());
	}

	@Override
	public List<UserReferenceDto> getAllUserRefs(boolean includeInactive) {

		return userService.getReferenceList(null, null, false, true, !includeInactive)
			.stream()
			.map(c -> toReferenceDto(c))
			.collect(Collectors.toList());
	}

	@Override
	public List<UserDto> getUsersByAssociatedOfficer(UserReferenceDto associatedOfficerRef, UserRole... userRoles) {

		User associatedOfficer = userService.getByReferenceDto(associatedOfficerRef);
		return userService.getAllByAssociatedOfficer(associatedOfficer, userRoles).stream().map(f -> toDto(f)).collect(Collectors.toList());
	}

	@Override
	public Page<UserDto> getIndexPage(UserCriteria userCriteria, int offset, int size, List<SortProperty> sortProperties) {
		List<UserDto> userIndexList = getIndexList(userCriteria, offset, size, sortProperties);
		long totalElementCount = count(userCriteria);
		return new Page<>(userIndexList, offset, size, totalElementCount);
	}

	@Override
	public List<UserDto> getAllAfter(Date date) {
		return userService.getAllAfter(date, null).stream().map(c -> toDto(c)).collect(Collectors.toList());
	}

	@Override
	public List<UserDto> getByUuids(List<String> uuids) {
		return userService.getByUuids(uuids).stream().map(c -> toDto(c)).collect(Collectors.toList());
	}

	@Override
	public List<String> getAllUuids() {

		if (userService.getCurrentUser() == null) {
			return Collections.emptyList();
		}

		return userService.getAllUuids();
	}

	@Override
	public UserDto getByUuid(String uuid) {
		return toDto(userService.getByUuid(uuid));
	}

	@Override
	public UserDto getByUserName(String userName) {
		return toDto(userService.getByUserName(userName));
	}

	@Override
	public UserDto saveUser(UserDto dto) {

		User oldUser = null;
		if (dto.getCreationDate() != null) {
			try {
				oldUser = (User) BeanUtils.cloneBean(userService.getByUuid(dto.getUuid()));
			} catch (Exception e) {
				throw new IllegalArgumentException("Invalid bean access", e);
			}
		}

		User user = fromDto(dto, true);

		try {
			UserRole.validate(user.getUserRoles());
		} catch (UserRoleValidationException e) {
			throw new ValidationException(e);
		}

		userService.ensurePersisted(user);

		if (oldUser == null) {
			userCreateEvent.fire(new UserCreateEvent(user));
		} else {
			userUpdateEvent.fire(new UserUpdateEvent(oldUser, user));
		}

		return toDto(user);
	}

	@Override
	public List<UserDto> getIndexList(UserCriteria userCriteria, Integer first, Integer max, List<SortProperty> sortProperties) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<User> cq = cb.createQuery(User.class);
		Root<User> user = cq.from(User.class);
		Join<User, District> district = user.join(User.DISTRICT, JoinType.LEFT);
		Join<User, Location> address = user.join(User.ADDRESS, JoinType.LEFT);
		Join<User, Facility> facility = user.join(User.HEALTH_FACILITY, JoinType.LEFT);

		// TODO: We'll need a user filter for users at some point, to make sure that users can edit their own details,
		// but not those of others

		Predicate filter = null;

		if (userCriteria != null) {
			filter = userService.buildCriteriaFilter(userCriteria, cb, user);
		}

		if (filter != null) {
			/*
			 * No preemptive distinct because this does collide with
			 * ORDER BY User.location.address (which is not part of the SELECT clause).
			 */
			cq.where(filter);
		}

		if (sortProperties != null && sortProperties.size() > 0) {
			List<Order> order = new ArrayList<Order>(sortProperties.size());
			for (SortProperty sortProperty : sortProperties) {
				Expression<?> expression;
				switch (sortProperty.propertyName) {
				case UserDto.UUID:
				case UserDto.ACTIVE:
				case UserDto.USER_NAME:
				case UserDto.USER_EMAIL:
					expression = user.get(sortProperty.propertyName);
					break;
				case UserDto.NAME:
					expression = user.get(User.FIRST_NAME);
					order.add(sortProperty.ascending ? cb.asc(expression) : cb.desc(expression));
					expression = user.get(User.LAST_NAME);
					break;
				case UserDto.DISTRICT:
					expression = district.get(District.NAME);
					break;
				case UserDto.ADDRESS:
					expression = address.get(Location.REGION);
					break;
				case UserDto.HEALTH_FACILITY:
					expression = facility.get(Facility.NAME);
					break;
				default:
					throw new IllegalArgumentException(sortProperty.propertyName);
				}
				order.add(sortProperty.ascending ? cb.asc(expression) : cb.desc(expression));
			}
			cq.orderBy(order);
		} else {
			cq.orderBy(cb.desc(user.get(User.CHANGE_DATE)));
		}

		cq.select(user);

		List<User> resultList;
		if (first != null && max != null) {
			resultList = em.createQuery(cq).setFirstResult(first).setMaxResults(max).getResultList();
		} else {
			resultList = em.createQuery(cq).getResultList();
		}
		return resultList.stream().map(u -> toDto(u)).collect(Collectors.toList());
	}

	@Override
	public long count(UserCriteria userCriteria) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<User> root = cq.from(User.class);

		Predicate filter = null;

		if (userCriteria != null) {
			filter = userService.buildCriteriaFilter(userCriteria, cb, root);
		}

		if (filter != null) {
			cq.where(filter);
		}

		cq.select(cb.count(root));
		return em.createQuery(cq).getSingleResult();
	}

	private User fromDto(UserDto source, boolean checkChangeDate) {

		User target = DtoHelper.fillOrBuildEntity(source, userService.getByUuid(source.getUuid()), userService::createUser, checkChangeDate);

		target.setActive(source.isActive());
		target.setFirstName(source.getFirstName());
		target.setLastName(source.getLastName());
		target.setPhone(source.getPhone());
		target.setAddress(locationFacade.fromDto(source.getAddress(), checkChangeDate));

		target.setUserName(source.getUserName());
		target.setUserEmail(source.getUserEmail());

		target.setRegion(regionService.getByReferenceDto(source.getRegion()));
		target.setDistrict(districtService.getByReferenceDto(source.getDistrict()));
		target.setCommunity(communityService.getByReferenceDto(source.getCommunity()));
		target.setHealthFacility(facilityService.getByReferenceDto(source.getHealthFacility()));
		target.setAssociatedOfficer(userService.getByReferenceDto(source.getAssociatedOfficer()));
		target.setLaboratory(facilityService.getByReferenceDto(source.getLaboratory()));
		target.setPointOfEntry(pointOfEntryService.getByReferenceDto(source.getPointOfEntry()));
		target.setLimitedDisease(source.getLimitedDisease());
		target.setLanguage(source.getLanguage());
		target.setHasConsentedToGdpr(source.isHasConsentedToGdpr());

		target.setUserRoles(new HashSet<UserRole>(source.getUserRoles()));

		return target;
	}

	@Override
	public boolean isLoginUnique(String uuid, String userName) {
		return userService.isLoginUnique(uuid, userName);
	}

	@Override
	public String resetPassword(String uuid) {
		String resetPassword = userService.resetPassword(uuid);
		passwordResetEvent.fire(new PasswordResetEvent(userService.getByUuid(uuid)));
		return resetPassword;
	}

	@Override
	public UserDto getCurrentUser() {
		return toDto(userService.getCurrentUser());
	}

	@Override
	public UserReferenceDto getCurrentUserAsReference() {
		return new UserReferenceDto(userService.getCurrentUser().getUuid());
	}

	@Override
	public Set<UserRole> getValidLoginRoles(String userName, String password) {

		User user = userService.getByUserName(userName);
		if (user != null && user.isActive()) {
			if (DataHelper.equal(user.getPassword(), PasswordHelper.encodePassword(password, user.getSeed()))) {
				return new HashSet<UserRole>(user.getUserRoles());
			}
		}
		return null;
	}

	@Override
	public void removeUserAsSurveillanceAndContactOfficer(String userUuid) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Case> caseQuery = cb.createQuery(Case.class);
		Root<Case> caseRoot = caseQuery.from(Case.class);
		Join<Case, User> surveillanceOfficerJoin = caseRoot.join(Case.SURVEILLANCE_OFFICER, JoinType.LEFT);

		caseQuery.where(cb.equal(surveillanceOfficerJoin.get(User.UUID), userUuid));
		List<Case> cases = em.createQuery(caseQuery).getResultList();
		cases.forEach(c -> {
			c.setSurveillanceOfficer(null);
			caseFacade.setResponsibleSurveillanceOfficer(c);
			caseService.ensurePersisted(c);
			caseFacade.reassignTasksOfCase(c, true);
		});

		CriteriaQuery<Contact> contactQuery = cb.createQuery(Contact.class);
		Root<Contact> contactRoot = contactQuery.from(Contact.class);
		Join<Contact, User> contactOfficerJoin = contactRoot.join(Contact.CONTACT_OFFICER, JoinType.LEFT);

		contactQuery.where(cb.equal(contactOfficerJoin.get(User.UUID), userUuid));
		List<Contact> contacts = em.createQuery(contactQuery).getResultList();
		contacts.forEach(c -> {
			c.setContactOfficer(null);
			contactService.ensurePersisted(c);
		});
	}

	@Override
	public UserSyncResult syncUser(String uuid) {
		User user = userService.getByUuid(uuid);

		UserSyncResult userSyncResult = new UserSyncResult();
		userSyncResult.setSuccess(true);

		UserUpdateEvent event = new UserUpdateEvent(user);
		event.setExceptionCallback(exceptionMessage -> {
			userSyncResult.setSuccess(false);
			userSyncResult.setErrorMessage(exceptionMessage);
		});

		this.userUpdateEvent.fire(event);

		return userSyncResult;
	}

	@Override
	public List<UserDto> getUsersWithDefaultPassword() {
		User currentUser = userService.getCurrentUser();
		if (currentUser.getUserRoles().stream().anyMatch(r -> r.hasDefaultRight(UserRight.USER_EDIT))) {
			// user is allowed to change all passwords
			// a list of all users with a default password is returned
			return userService.getAllDefaultUsers()
				.stream()
				.filter(user -> DefaultUserHelper.usesDefaultPassword(user.getUserName(), user.getPassword(), user.getSeed()))
				.map(UserFacadeEjb::toDto)
				.collect(Collectors.toList());

		} else {
			// user has only access to himself
			// the list will include him/her or will be empty
			if (DefaultUserHelper.isDefaultUser(currentUser.getUserName())
				&& DefaultUserHelper.usesDefaultPassword(currentUser.getUserName(), currentUser.getPassword(), currentUser.getSeed())) {
				return Collections.singletonList(UserFacadeEjb.toDto(currentUser));
			} else {
				return Collections.emptyList();
			}
		}
	}

	@Override
	public void enableUsers(List<String> userUuids) {
		updateActiveState(userUuids, true);
	}

	@Override
	public void disableUsers(List<String> userUuids) {
		updateActiveState(userUuids, false);
	}

	private void updateActiveState(List<String> userUuids, boolean active) {

		List<User> users = userService.getByUuids(userUuids);
		for (User user : users) {
			User oldUser;
			try {
				oldUser = (User) BeanUtils.cloneBean(user);
			} catch (Exception e) {
				throw new IllegalArgumentException("Invalid bean access", e);
			}

			user.setActive(active);
			userService.ensurePersisted(user);

			userUpdateEvent.fire(new UserUpdateEvent(oldUser, user));
		}
	}

	@LocalBean
	@Stateless
	public static class UserFacadeEjbLocal extends UserFacadeEjb {

	}
}
