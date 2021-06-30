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
package de.symeda.sormas.api.utils;

import java.io.IOException;
import java.io.InputStream;

public class InfoProvider {

	private static InfoProvider instance;

	private final String version;

	InfoProvider() {
		try {
			InputStream stream = InfoProvider.class.getResourceAsStream("/version.txt");
			String version = DataHelper.convertStreamToString(stream);
			this.version = version.trim();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static synchronized InfoProvider get() {
		if (instance == null) {
			instance = new InfoProvider();
		}
		return instance;
	}

	/**
	 * When changing this make sure to check also EXTERNAL_VISITS_API_VERSION.
	 * 
	 * @return
	 */
	public String getMinimumRequiredVersion() {
		return "1.62.0";
	}

	/**
	 * Reads the version from the version.txt where it is written by maven.
	 * We are doing it this way, because all other version information (manifest, pom) will be removed in the android app by gradle.
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Reads the version from the version.txt where it is written by maven and replaces the last version number with a 0.
	 */
	public String getBaseVersion() {
		return version.substring(0, version.lastIndexOf(".")) + ".0";
	}

	/**
	 * Checks if the app version is compatible with the api version. This is true when the version is at least as high as the
	 * MINIMUM_REQUIRED_VERSION and lower or equal to the version returned by getVersion().
	 */
	public CompatibilityCheckResponse isCompatibleToApi(String appVersionInput) {
		return isCompatibleToApi(VersionHelper.extractVersion(appVersionInput));
	}

	/**
	 * Checks if the app version is compatible with the api version. This is true when the version is at least as high as the
	 * MINIMUM_REQUIRED_VERSION and lower or equal to the version returned by getVersion().
	 */
	public CompatibilityCheckResponse isCompatibleToApi(int[] appVersion) {

		if (!VersionHelper.isVersion(appVersion)) {
			throw new IllegalArgumentException("No proper app version provided");
		}

		int[] minVersion = VersionHelper.extractVersion(getMinimumRequiredVersion());

		if (VersionHelper.isBefore(appVersion, minVersion)) {
			return CompatibilityCheckResponse.TOO_OLD;
		}

		int[] serverVersion = VersionHelper.extractVersion(getVersion());
		if (VersionHelper.isAfter(appVersion, serverVersion)) {
			return CompatibilityCheckResponse.TOO_NEW;
		}

		return CompatibilityCheckResponse.COMPATIBLE;
	}
}
