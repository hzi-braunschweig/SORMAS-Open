/*******************************************************************************
 * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * Copyright © 2016-2021 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
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
package de.symeda.sormas.api.utils;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.symeda.sormas.api.user.UserDto;

public class DefaultUserHelper {

	// default usernames and passwords
	public static final DataHelper.Pair<String, String> ADMIN_USERNAME_AND_PASSWORD = new DataHelper.Pair<>("admin", "sadmin");
	public static final DataHelper.Pair<String, String> SURV_SUP_USERNAME_AND_PASSWORD = new DataHelper.Pair<>("SurvSup", "SurvSup");
	public static final DataHelper.Pair<String, String> CASE_SUP_USERNAME_AND_PASSWORD = new DataHelper.Pair<>("CaseSup", "CaseSup");
	public static final DataHelper.Pair<String, String> CONT_SUP_USERNAME_AND_PASSWORD = new DataHelper.Pair<>("ContSup", "ContSup");
	public static final DataHelper.Pair<String, String> POE_SUP_USERNAME_AND_PASSWORD = new DataHelper.Pair<>("PoeSup", "PoeSup");
	public static final DataHelper.Pair<String, String> LAB_OFF_USERNAME_AND_PASSWORD = new DataHelper.Pair<>("LabOff", "LabOff");
	public static final DataHelper.Pair<String, String> EVE_OFF_USERNAME_AND_PASSWORD = new DataHelper.Pair<>("EveOff", "EveOff");
	public static final DataHelper.Pair<String, String> NAT_USER_USERNAME_AND_PASSWORD = new DataHelper.Pair<>("NatUser", "NatUser");
	public static final DataHelper.Pair<String, String> NAT_CLIN_USERNAME_AND_PASSWORD = new DataHelper.Pair<>("NatClin", "NatClin");
	public static final DataHelper.Pair<String, String> SURV_OFF_USERNAME_AND_PASSWORD = new DataHelper.Pair<>("SurvOff", "SurvOff");
	public static final DataHelper.Pair<String, String> HOSP_INF_USERNAME_AND_PASSWORD = new DataHelper.Pair<>("HospInf", "HospInf");
	public static final DataHelper.Pair<String, String> COMM_OFF_USERNAME_AND_PASSWORD = new DataHelper.Pair<>("CommOff", "CommOff");
	public static final DataHelper.Pair<String, String> POE_INF_USERNAME_AND_PASSWORD = new DataHelper.Pair<>("PoeInf", "PoeInf");
	public static final String SORMAS_TO_SORMAS_USER_NAME = "Sormas2Sormas";
	public static final String SORMAS_TO_SORMAS_PASSWORD = genS2Spassword();

	private static String genS2Spassword() {
		byte[] pwd = new byte[32];
		SecureRandom rnd = new SecureRandom();
		rnd.nextBytes(pwd);
		return new String(pwd);
	}

	private static final Map<String, String> defaultUsersWithPassword = new HashMap<>();

	static {
		addPairToUsernameAndPasswordMap(
			ADMIN_USERNAME_AND_PASSWORD,
			SURV_SUP_USERNAME_AND_PASSWORD,
			CASE_SUP_USERNAME_AND_PASSWORD,
			CONT_SUP_USERNAME_AND_PASSWORD,
			POE_SUP_USERNAME_AND_PASSWORD,
			LAB_OFF_USERNAME_AND_PASSWORD,
			EVE_OFF_USERNAME_AND_PASSWORD,
			NAT_USER_USERNAME_AND_PASSWORD,
			NAT_CLIN_USERNAME_AND_PASSWORD,
			SURV_OFF_USERNAME_AND_PASSWORD,
			HOSP_INF_USERNAME_AND_PASSWORD,
			COMM_OFF_USERNAME_AND_PASSWORD,
			POE_INF_USERNAME_AND_PASSWORD);
	}

	public static boolean isDefaultUser(String username) {
		return defaultUsersWithPassword.containsKey(username);
	}

	public static String getDefaultPassword(String username) {
		return defaultUsersWithPassword.get(username);
	}

	public static Set<String> getDefaultUserNames() {
		return defaultUsersWithPassword.keySet();
	}

	public static boolean usesDefaultPassword(String username, String passwordHash, String seed) {
		String defaultPassword = getDefaultPassword(username);
		if (defaultPassword == null) {
			return false;
		} else {
			return passwordHash.equals(PasswordHelper.encodePassword(defaultPassword, seed));
		}
	}

	public static boolean currentUserUsesDefaultPassword(List<UserDto> allUsersWithDefaultPassword, UserDto currentUser) {
		return allUsersWithDefaultPassword.contains(currentUser);
	}

	public static boolean otherUsersUseDefaultPassword(List<UserDto> allUsersWithDefaultPassword, UserDto currentUser) {
		return (allUsersWithDefaultPassword.contains(currentUser) && allUsersWithDefaultPassword.size() > 1)
			|| (!allUsersWithDefaultPassword.contains(currentUser) && allUsersWithDefaultPassword.size() > 0);
	}

	// internal helpers
	@SafeVarargs
	private static void addPairToUsernameAndPasswordMap(DataHelper.Pair<String, String>... usernameAndPasswordPairs) {
		for (DataHelper.Pair<String, String> usernameAndPasswordPair : usernameAndPasswordPairs) {
			defaultUsersWithPassword.put(usernameAndPasswordPair.getElement0(), usernameAndPasswordPair.getElement1());
		}
	}

}
