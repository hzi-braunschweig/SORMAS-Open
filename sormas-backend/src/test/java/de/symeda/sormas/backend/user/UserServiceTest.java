package de.symeda.sormas.backend.user;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;

import de.symeda.sormas.api.AuthProvider;
import de.symeda.sormas.api.user.UserDto;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.api.utils.PasswordHelper;
import de.symeda.sormas.backend.AbstractBeanTest;
import de.symeda.sormas.backend.TestDataCreator.RDCF;

public class UserServiceTest extends AbstractBeanTest {

	private static MockedStatic<AuthProvider> mockAuthProvider;

	@BeforeClass
	public static void beforeClass() {
		AuthProvider authProvider = mock(AuthProvider.class);
		mockAuthProvider = mockStatic(AuthProvider.class);
		assertNotNull(mockAuthProvider);
		mockAuthProvider.when(AuthProvider::getProvider).thenReturn(authProvider);
		when(authProvider.isUsernameCaseSensitive()).thenReturn(true);
	}

	@AfterClass
	public static void afterClass() {
		assertNotNull(mockAuthProvider);
		//Important: release static mock.
		mockAuthProvider.closeOnDemand();
	}

	@Test
	public void testGetReferenceList() {

		List<String> regionUuids = null;
		List<String> districtUuids = null;
		List<String> communityUuids = null;
		boolean includeSupervisors = false;
		boolean filterByJurisdiction = false;
		boolean activeOnly = false;
		List<UserRole> userRoles = null;

		// 0. No conditions, test signature with userRoles varArg parameter
		List<UserReference> result =
			getUserService().getReferenceList(regionUuids, districtUuids, includeSupervisors, filterByJurisdiction, activeOnly);
		assertThat(result, hasSize(1));
		UserReference admin = result.get(0);
		assertThat(admin.getUserRoles(), containsInAnyOrder(UserRole.ADMIN, UserRole.NATIONAL_USER));

		// 1a. Find admin with several conditions
		activeOnly = true;
		userRoles = Arrays.asList(UserRole.ADMIN);
		result = getUserService().getReferenceList(regionUuids, districtUuids, communityUuids, includeSupervisors, filterByJurisdiction, activeOnly, userRoles);
		assertThat(result, contains(admin));
		userRoles = Arrays.asList(UserRole.NATIONAL_USER);
		result = getUserService().getReferenceList(regionUuids, districtUuids, communityUuids, includeSupervisors, filterByJurisdiction, activeOnly, userRoles);
		assertThat(result, contains(admin));
		userRoles = Arrays.asList(UserRole.ADMIN, UserRole.CASE_OFFICER);
		result = getUserService().getReferenceList(regionUuids, districtUuids, communityUuids, includeSupervisors, filterByJurisdiction, activeOnly, userRoles);
		assertThat(result, contains(admin));

		// 1b. Exclude admin by role
		userRoles = Arrays.asList(UserRole.CASE_OFFICER);
		result = getUserService().getReferenceList(regionUuids, districtUuids, communityUuids, includeSupervisors, filterByJurisdiction, activeOnly, userRoles);
		assertThat(result, is(empty()));

		// 2. Include supervisors
		userRoles = Arrays.asList(UserRole.CONTACT_OFFICER);
		RDCF rdcf = creator.createRDCF();
		UserDto supervisor = creator.createUser(rdcf, UserRole.CONTACT_SUPERVISOR);
		result = getUserService().getReferenceList(regionUuids, districtUuids, communityUuids, includeSupervisors, filterByJurisdiction, activeOnly, userRoles);
		assertThat(result, is(empty()));
		includeSupervisors = true;
		result = getUserService().getReferenceList(regionUuids, districtUuids, communityUuids, includeSupervisors, filterByJurisdiction, activeOnly, userRoles);
		assertThat(result, hasSize(1));
		assertThat(result.get(0).getUuid(), equalTo(supervisor.getUuid()));

		// 3. Exclude inactive user as overall condition
		getUserFacade().disableUsers(Arrays.asList(supervisor.getUuid()));
		result = getUserService().getReferenceList(regionUuids, districtUuids, communityUuids, includeSupervisors, filterByJurisdiction, activeOnly, userRoles);
		assertThat(result, is(empty()));

		// 4. filterByJurisdiction to test that the invocation works and filters correctly concerning activeOnly
		filterByJurisdiction = true;
		result = getUserService().getReferenceList(regionUuids, districtUuids, communityUuids, includeSupervisors, filterByJurisdiction, activeOnly, userRoles);
		assertThat(result, is(empty()));
		activeOnly = false;
		result = getUserService().getReferenceList(regionUuids, districtUuids, communityUuids, includeSupervisors, filterByJurisdiction, activeOnly, userRoles);
		assertThat(result, hasSize(1));
		assertThat(result.get(0).getUuid(), equalTo(supervisor.getUuid()));

		// 5. regions filter
		result = getUserService().getReferenceList(Arrays.asList(rdcf.region.getUuid()), null, false, false, false);
		assertThat(result, hasSize(1));
		assertThat(result.get(0).getUuid(), equalTo(supervisor.getUuid()));

		// 6. districts filter
		result = getUserService().getReferenceList(null, Arrays.asList(rdcf.district.getUuid()), false, false, false);
		assertThat(result, hasSize(1));
		assertThat(result.get(0).getUuid(), equalTo(supervisor.getUuid()));
	}

	@Test
	public void testGetAllDefaultUsers() {
		User user = getUserService().getByUserName("admin");
		assertNotNull(user);

		user.setSeed(PasswordHelper.createPass(16));
		user.setPassword(PasswordHelper.encodePassword("sadmin", user.getSeed()));
		getEntityManager().merge(user);

		List<User> result = getUserService().getAllDefaultUsers();
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals("admin", result.get(0).getUserName());
	}

	@Test
	public void testGetAllDefaultUsersWithExistingUsers() {
		Set<User> defaultUsers = UserTestHelper.generateDefaultUsers(false);
		Set<User> randomUsers = UserTestHelper.generateRandomUsers(10);
		Set<User> testUsers = new HashSet<>();
		testUsers.addAll(defaultUsers);
		testUsers.addAll(randomUsers);

		for (User u : testUsers) {
			getEntityManager().persist(u);
		}

		List<User> result = getUserService().getAllDefaultUsers();
		// Default users size + 1 because one default admin is created by the AbstractBeanTest
		assertEquals(defaultUsers.size() + 1, result.size());
		for (User defaultUser : defaultUsers) {
			assertTrue(result.contains(defaultUser));
		}
		for (User randomUser : randomUsers) {
			assertFalse(result.contains(randomUser));
		}
	}
}
