package de.symeda.sormas.backend.user;

import static de.symeda.sormas.api.user.UserRole.ADMIN;
import static de.symeda.sormas.api.user.UserRole.CASE_OFFICER;
import static de.symeda.sormas.api.user.UserRole.CASE_SUPERVISOR;
import static de.symeda.sormas.api.user.UserRole.COMMUNITY_INFORMANT;
import static de.symeda.sormas.api.user.UserRole.COMMUNITY_OFFICER;
import static de.symeda.sormas.api.user.UserRole.CONTACT_OFFICER;
import static de.symeda.sormas.api.user.UserRole.CONTACT_SUPERVISOR;
import static de.symeda.sormas.api.user.UserRole.DISTRICT_OBSERVER;
import static de.symeda.sormas.api.user.UserRole.EVENT_OFFICER;
import static de.symeda.sormas.api.user.UserRole.HOSPITAL_INFORMANT;
import static de.symeda.sormas.api.user.UserRole.NATIONAL_OBSERVER;
import static de.symeda.sormas.api.user.UserRole.NATIONAL_USER;
import static de.symeda.sormas.api.user.UserRole.POE_INFORMANT;
import static de.symeda.sormas.api.user.UserRole.POE_SUPERVISOR;
import static de.symeda.sormas.api.user.UserRole.STATE_OBSERVER;
import static de.symeda.sormas.api.user.UserRole.SURVEILLANCE_OFFICER;
import static de.symeda.sormas.api.user.UserRole.SURVEILLANCE_SUPERVISOR;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import de.symeda.sormas.api.AuthProvider;
import de.symeda.sormas.api.facility.FacilityReferenceDto;
import de.symeda.sormas.api.region.CommunityReferenceDto;
import de.symeda.sormas.api.region.DistrictReferenceDto;
import de.symeda.sormas.api.region.RegionReferenceDto;
import de.symeda.sormas.api.user.UserCriteria;
import de.symeda.sormas.api.user.UserDto;
import de.symeda.sormas.api.user.UserReferenceDto;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.api.utils.SortProperty;
import de.symeda.sormas.backend.AbstractBeanTest;
import de.symeda.sormas.backend.TestDataCreator.RDCF;
import de.symeda.sormas.backend.TestDataCreator.RDCFEntities;
import de.symeda.sormas.backend.region.Region;
import de.symeda.sormas.backend.region.RegionService;

public class UserFacadeEjbTest extends AbstractBeanTest {

	@InjectMocks
	private UserFacadeEjb userFacadeEjb;

	@Mock
	private UserService userService;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testGetUsersByRegionAndRoles() {
		getUserFacade().getUsersByRegionAndRoles(null, SURVEILLANCE_OFFICER);
	}

	/**
	 * Testing with some users that might not be selected DISTINCT because of 1:n relations.
	 */
	@Test
	public void testGetIndexList() {

		// 1 region, 2 districts
		RDCFEntities rdcf = creator.createRDCFEntities();
		Region region = getBean(RegionService.class).getByUuid(rdcf.region.getUuid());
		creator.createDistrict("district 2", region);

		// user with a 2 districts region, 2 user roles
		creator.createUser(rdcf, "my", "User", CASE_OFFICER, CONTACT_OFFICER);

		// some other users to be filtered out
		creator.createUser(rdcf, SURVEILLANCE_SUPERVISOR, CONTACT_SUPERVISOR);
		creator.createUser(rdcf, SURVEILLANCE_OFFICER, DISTRICT_OBSERVER);

		List<UserDto> result;

		// 1. Check that query works without filter: All 3 distinct users are found (+ admin by default)
		result = getUserFacade().getIndexList(new UserCriteria(), 0, 100, null);
		assertThat(result, hasSize(4));
		assertThat(result.stream().map(e -> e.getUuid()).collect(Collectors.toSet()), hasSize(4));

		// 2. Check that only the expected user is found
		result = getUserFacade().getIndexList(new UserCriteria().freeText("myUser"), 0, 100, null);
		assertThat(result, hasSize(1));
		assertThat(result.get(0).getUserName(), equalTo("myUser"));
	}

	@Test
	public void testGetReferencesByRoleAndJurisdiction() {

		UserReferenceDto userAdmin = getUserFacade().getAllUserRefs(false).get(0);

		RDCF rdcf1 = creator.createRDCF("Region1", "District1_1", "Community1_1", "Facility1");
		RDCF rdcf2 = creator.createRDCF("Region2", "District2", "Community2", null);

		RegionReferenceDto region1 = rdcf1.region;
		RegionReferenceDto region2 = rdcf2.region;

		DistrictReferenceDto district1_1 = rdcf1.district;
		DistrictReferenceDto district1_2 =
			new DistrictReferenceDto(creator.createDistrict("District1_2", getBean(RegionService.class).getByUuid(rdcf1.region.getUuid())).getUuid());
		DistrictReferenceDto district2 = rdcf2.district;

		CommunityReferenceDto community1_1 = rdcf1.community;
		CommunityReferenceDto community1_2 = creator.createCommunity("Community1_2", district1_1).toReference();
		CommunityReferenceDto community2 = rdcf2.community;

		FacilityReferenceDto facility1 = rdcf1.facility;

		//Jurisdiction Region
		UserReferenceDto userR1 = creator.createUserRef(region1.getUuid(), null, null, null, "Irmin", "Schmidt", EVENT_OFFICER);
		UserReferenceDto userR2 = creator.createUserRef(region2.getUuid(), null, null, null, "Michael", "Karoli", STATE_OBSERVER);

		// Jurisdiction District
		UserReferenceDto userD1 = creator.createUserRef(region1.getUuid(), district1_1.getUuid(), null, null, "Malcolm", "Mooney", CASE_OFFICER);
		UserReferenceDto userD2 = creator.createUserRef(region1.getUuid(), district1_2.getUuid(), null, null, "Rosko", "Gee", SURVEILLANCE_OFFICER);
		UserReferenceDto userD3 = creator.createUserRef(region2.getUuid(), district2.getUuid(), null, null, "Tim", "Hardin", CONTACT_OFFICER);

		// Jurisdiction Community
		UserReferenceDto userC1 =
			creator.createUserRef(region1.getUuid(), district1_1.getUuid(), community1_1.getUuid(), null, "Blixa", "Bargeld", COMMUNITY_INFORMANT);
		UserReferenceDto userC2 =
			creator.createUserRef(region1.getUuid(), district1_1.getUuid(), community1_2.getUuid(), null, "Jaki", "Liebezeit", COMMUNITY_OFFICER);
		UserReferenceDto userC3 =
			creator.createUserRef(region1.getUuid(), district2.getUuid(), community2.getUuid(), null, "Holger", "Czukay", COMMUNITY_INFORMANT);

		// Jurisdiction Nation
		UserReferenceDto userN1 = creator.createUserRef(null, null, null, null, "Fred", "Cole", NATIONAL_USER);
		UserReferenceDto userN2 = creator.createUserRef(null, null, null, null, "Toody", "Cole", NATIONAL_OBSERVER);

		// Jurisdiction Health Facility
		UserReferenceDto userHF1 = creator.createUserRef(null, null, null, facility1.getUuid(), "FM", "Einheit", HOSPITAL_INFORMANT);

		// Supervisors
		UserDto userS1 = creator.createUser(region1.getUuid(), null, facility1.getUuid(), "Joey", "Ramone", CASE_SUPERVISOR);
		UserDto userS2 = creator.createUser(region1.getUuid(), null, facility1.getUuid(), "Johnny", "Ramone", CONTACT_SUPERVISOR);
		UserDto userS3 = creator.createUser(region2.getUuid(), null, facility1.getUuid(), "Dee Dee", "Ramone", SURVEILLANCE_SUPERVISOR);
		UserDto userS4 = creator.createUser(region2.getUuid(), null, facility1.getUuid(), "Tommy", "Ramone", POE_SUPERVISOR);

		// Tests
		assertThat(getUserFacade().getUsersByRegionAndRoles(region1, COMMUNITY_OFFICER, SURVEILLANCE_OFFICER), containsInAnyOrder(userD2, userC2));
		assertThat(
			getUserFacade()
				.getUsersByRegionsAndRoles(Arrays.asList(region1, region2), COMMUNITY_INFORMANT, CASE_OFFICER, CONTACT_OFFICER, STATE_OBSERVER),
			containsInAnyOrder(userR2, userD1, userD3, userC1, userC3));

		assertThat(
			getUserFacade().getUserRefsByDistrict(district1_1, false, STATE_OBSERVER, CASE_OFFICER, COMMUNITY_OFFICER, POE_INFORMANT),
			containsInAnyOrder(userD1, userC2));
		assertThat(
			getUserFacade()
				.getUserRefsByDistricts(Arrays.asList(district1_1, district2), false, SURVEILLANCE_OFFICER, CONTACT_OFFICER, COMMUNITY_INFORMANT),
			containsInAnyOrder(userD3, userC1, userC3));

		assertThat(getUserFacade().getUserRefsByDistrict(district1_1, true, CASE_OFFICER), containsInAnyOrder(userD1, userS1, userS2, userS3));

		assertThat(getUserFacade().getUsersWithSuperiorJurisdiction(getUserFacade().getByUuid(userN1.getUuid())), anyOf(empty()));
		assertThat(
			getUserFacade().getUsersWithSuperiorJurisdiction(getUserFacade().getByUuid(userR1.getUuid())),
			containsInAnyOrder(userAdmin, userN1, userN2));
		assertThat(
			getUserFacade().getUsersWithSuperiorJurisdiction(getUserFacade().getByUuid(userD1.getUuid())),
			containsInAnyOrder(userR1, userS1, userS2));
		assertThat(getUserFacade().getUsersWithSuperiorJurisdiction(getUserFacade().getByUuid(userC1.getUuid())), containsInAnyOrder(userD1));

		assertThat(
			getUserFacade().getUsersWithSuperiorJurisdiction(getUserFacade().getByUuid(userHF1.getUuid())),
			containsInAnyOrder(userC1));

		assertThat(getUserFacade().getAllUserRefs(false), hasSize(16));

		userS4.setActive(false);
		getUserFacade().saveUser(userS4);

		assertThat(getUserFacade().getAllUserRefs(false), hasSize(15));
		assertThat(getUserFacade().getAllUserRefs(true), hasSize(16));
	}

	/**
	 * Test that the filtering on text fields and sorting be Location.address do not cause a database exception.
	 */
	@Test
	public void testGetIndexListFilteredAndOrderedByAddress() {

		List<UserDto> result =
			getUserFacade().getIndexList(new UserCriteria().freeText("min"), 0, 100, Collections.singletonList(new SortProperty(UserDto.ADDRESS)));

		assertThat(result, hasSize(1));
		assertThat(result.get(0).getUserName(), equalTo("admin"));
	}

	@Test
	public void testGetValidLoginRoles() {

		AuthProvider authProvider = mock(AuthProvider.class);

		MockedStatic<AuthProvider> mockAuthProvider = mockStatic(AuthProvider.class);
		mockAuthProvider.when(AuthProvider::getProvider).thenReturn(authProvider);
		when(authProvider.isUsernameCaseSensitive()).thenReturn(true);

		RDCF rdcf = creator.createRDCF();
		UserDto user = creator.createUser(rdcf, SURVEILLANCE_SUPERVISOR);
		String password = getUserFacade().resetPassword(user.getUuid());

		Set<UserRole> validLoginRoles = getUserFacade().getValidLoginRoles(user.getUserName(), password);
		assertThat(validLoginRoles, containsInAnyOrder(SURVEILLANCE_SUPERVISOR));

		user.setActive(false);
		getUserFacade().saveUser(user);

		validLoginRoles = getUserFacade().getValidLoginRoles(user.getUserName(), password);
		assertThat(validLoginRoles, nullValue());

		//Important: release static mock.
		mockAuthProvider.closeOnDemand();
	}

	@Test
	public void testGetExistentDefaultUsers() {
		Set<User> defaultUsers = UserTestHelper.generateDefaultUsers(true);
		Set<User> randomUsers = UserTestHelper.generateRandomUsers(30);

		List<User> testUsers = new ArrayList<>();
		testUsers.addAll(defaultUsers);
		testUsers.addAll(randomUsers);

		for (User user : testUsers) {
			Mockito.when(userService.getCurrentUser()).thenReturn(user);
			Mockito.when(userService.getAllDefaultUsers()).thenReturn(Arrays.asList(user));
			if (defaultUsers.contains(user)) {
				assertEquals(1, userFacadeEjb.getUsersWithDefaultPassword().size());
				assertEquals(UserFacadeEjb.toDto(user), userFacadeEjb.getUsersWithDefaultPassword().get(0));
			} else {
				assertEquals(0, userFacadeEjb.getUsersWithDefaultPassword().size());
			}

			Mockito.when(userService.getAllDefaultUsers()).thenReturn(testUsers);
			if (user.hasAnyUserRole(ADMIN)) {
				assertEquals(defaultUsers.size(), userFacadeEjb.getUsersWithDefaultPassword().size());
				for (User defUser : defaultUsers) {
					assertTrue(userFacadeEjb.getUsersWithDefaultPassword().contains(UserFacadeEjb.toDto(defUser)));
				}
				for (User randomUser : randomUsers) {
					assertFalse(userFacadeEjb.getUsersWithDefaultPassword().contains(UserFacadeEjb.toDto(randomUser)));
				}

			} else if (defaultUsers.contains(user)) {
				assertEquals(1, userFacadeEjb.getUsersWithDefaultPassword().size());
				assertEquals(UserFacadeEjb.toDto(user), userFacadeEjb.getUsersWithDefaultPassword().get(0));
			} else {
				assertEquals(0, userFacadeEjb.getUsersWithDefaultPassword().size());
			}
		}
	}

	@Test
	public void testGetExistentDefaultUsersUpperCase() {
		Set<User> defaultUsers = UserTestHelper.generateDefaultUsers(true);
		for (User user : defaultUsers) {
			user.setUserName(user.getUserName().toUpperCase());
			Mockito.when(userService.getCurrentUser()).thenReturn(user);
			Mockito.when(userService.getAllDefaultUsers()).thenReturn(Arrays.asList(user));
			assertEquals(0, userFacadeEjb.getUsersWithDefaultPassword().size());
		}

	}
}
