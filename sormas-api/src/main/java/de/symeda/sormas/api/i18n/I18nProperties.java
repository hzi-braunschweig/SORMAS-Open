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
package de.symeda.sormas.api.i18n;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle.Control;

import org.apache.commons.lang3.StringUtils;

import de.symeda.sormas.api.Language;
import de.symeda.sormas.api.ResourceBundle;
import de.symeda.sormas.api.caze.InfectionSetting;

public final class I18nProperties {

	public static final String FULL_COUNTRY_LOCALE_PATTERN = "[a-zA-Z]*-[a-zA-Z]*";

	private static Map<Language, I18nProperties> instances = new HashMap<>();
	private static final ThreadLocal<Language> userLanguage = new ThreadLocal<>();

	private static Language defaultLanguage;

	private final ResourceBundle captionProperties;
	private final ResourceBundle descriptionProperties;
	private final ResourceBundle enumProperties;
	private final ResourceBundle validationProperties;
	private final ResourceBundle stringProperties;
	private final ResourceBundle countryProperties;
	private final ResourceBundle continentProperties;
	private final ResourceBundle subcontinentProperties;

	private static I18nProperties getInstance(Language language) {

		if (language == null) {
			Language defaultLanguage = getDefaultLanguage();
			if (defaultLanguage != null) {
				language = defaultLanguage;
			} else {
				language = Language.EN;
			}
		}

		I18nProperties instance = instances.get(language);
		if (instance == null) {
			instances.put(language, new I18nProperties(language));
			instance = instances.get(language);
		}

		return instance;
	}

	public static Language setUserLanguage(Language language) {

		if (userLanguage.get() != null && language == userLanguage.get()) {
			return language;
		}

		if (language == null) {
			language = getDefaultLanguage();
		}

		userLanguage.set(language);

		return language;
	}

	public static Language getUserLanguage() {

		Language language = userLanguage.get();
		return language == null ? getDefaultLanguage() : language;
	}

	public static void removeUserLanguage() {
		userLanguage.remove();
	}

	public static void setDefaultLanguage(Language language) {
		defaultLanguage = language;
	}

	private static Language getDefaultLanguage() {

		if (defaultLanguage == null) {
			defaultLanguage = Language.EN;
		}

		return defaultLanguage;
	}

	@SuppressWarnings("rawtypes")
	public static String getEnumCaption(Enum value) {

		final Language language = userLanguage.get();
		return getEnumCaption(language, value);
	}

	@SuppressWarnings("rawtypes")
	public static String getEnumCaption(Language language, Enum value) {
		return getEnumCaption(language, value, true);
	}

	@SuppressWarnings("rawtypes")
	public static String getEnumCaption(Language language, InfectionSetting value) {
		String caption = getEnumCaption(language, value, false);
		if (value.getParent() != null) {
			// Heavy Wide-Headed Rightwards Arrow U+2794
			caption = getEnumCaption(language, value.getParent(), false) + " ➔ " + caption;
		}

		return caption;
	}

	@SuppressWarnings("rawtypes")
	private static String getEnumCaption(Language language, Enum value, boolean handleParents) {
		if (handleParents && value instanceof InfectionSetting) {
			return getEnumCaption(language, (InfectionSetting) value);
		}

		String caption = getInstance(language).enumProperties.getString(value.getClass().getSimpleName() + "." + value.name());
		if (caption != null) {
			return caption;
		} else {
			return value.name();
		}
	}

	/**
	 * Retrieves the property by adding an additional string in between the class name and the property name,
	 * e.g. Disease.Short.EVD or FollowUpStatus.Desc.NO_FOLLOW_UP
	 * <p>
	 * Does fallback to enum caption without addition.
	 */
	public static String getEnumCaption(Enum<?> value, String addition) {

		String caption =
			getInstance(userLanguage.get()).enumProperties.getString(value.getClass().getSimpleName() + "." + addition + "." + value.name());
		if (caption != null) {
			return caption;
		} else {
			return getEnumCaption(value);
		}
	}

	public static String getEnumCaptionShort(Enum<?> value) {
		return getEnumCaption(value, "Short");
	}

	public static String getEnumDescription(Enum<?> value) {
		return getEnumCaption(value, "Desc");
	}

	/**
	 * Uses <param>key</param> as default value
	 */
	public static String getCaption(String key) {
		return getCaption(key, key);
	}

	public static String getCaption(String key, String defaultValue) {
		return getInstance(userLanguage.get()).captionProperties.getString(key, defaultValue);
	}

	/**
	 * Uses <param>key</param> as default value
	 */
	public static String getPrefixCaption(String prefix, String key) {
		return getPrefixCaption(prefix, key, key);
	}

	public static String getPrefixCaption(String prefix, String key, String defaultValue) {

		String result = null;
		if (prefix != null) {
			result = getInstance(userLanguage.get()).captionProperties.getString(prefix + "." + key);
		}
		if (result == null) {
			result = getCaption(key, defaultValue);
		}
		return result;
	}

	/**
	 * Iterates through the prefixes to determines the caption for the specified propertyId.
	 */
	public static String findPrefixCaption(String propertyId, String... prefixes) {

		for (String prefix : prefixes) {
			final String caption = I18nProperties.getPrefixCaption(prefix, propertyId, null);
			if (caption != null) {
				return caption;
			}
		}

		return propertyId;
	}

	/**
	 * Iterates through the prefixes to determines the caption for the specified propertyId.
	 */
	public static String findPrefixCaptionWithDefault(String propertyId, String defaultValue, String... prefixes) {

		for (String prefix : prefixes) {
			final String caption = I18nProperties.getPrefixCaption(prefix, propertyId, null);
			if (caption != null) {
				return caption;
			}
		}

		return defaultValue;
	}

	public static String getDescription(String key) {
		return getInstance(userLanguage.get()).descriptionProperties.getString(key);
	}

	public static String getDescription(String key, String defaultValue) {
		return getInstance(userLanguage.get()).descriptionProperties.getString(key, defaultValue);
	}

	/**
	 * Uses <param>key</param> as default value
	 */
	public static String getPrefixDescription(String prefix, String key) {
		return getPrefixDescription(prefix, key, key);
	}

	public static String getPrefixDescription(String prefix, String key, String defaultValue) {

		String result = null;
		if (prefix != null) {
			result = getDescription(prefix + "." + key);
		}
		if (result == null) {
			result = getDescription(key, defaultValue);
		}
		return result;
	}

	public static String getRequiredError(String fieldCaption) {
		return getValidationError(Validations.required, fieldCaption);
	}

	/**
	 * Uses <param>key</param> as default value
	 */
	public static String getValidationError(String key, Object... formatArgs) {

		String result = getInstance(userLanguage.get()).validationProperties.getString(key, null);
		if (result != null) {
			return String.format(result, formatArgs);
		} else if (formatArgs.length > 0 && formatArgs[0] != null) {
			return formatArgs[0].toString();
		} else {
			return "";
		}
	}

	public static String getPrefixValidationError(String prefix, String key, Object... formatArgs) {

		String result = null;
		if (prefix != null) {
			result = getInstance(userLanguage.get()).validationProperties.getString(prefix + "." + key);
			if (result != null) {
				return String.format(result, result);
			}
		}

		return getValidationError(key, formatArgs);
	}

	public static String getString(String property) {
		return getString(userLanguage.get(), property);
	}

	public static String getString(String property, String defaultValue) {

		String result = getString(userLanguage.get(), property);
		return StringUtils.isEmpty(result) ? defaultValue : result;
	}

	public static String getString(Language language, String property) {
		return getInstance(language).stringProperties.getString(property);
	}

	public static String getCountryName(String isoCode) {
		return getCountryName(userLanguage.get(), isoCode);
	}

	public static String getCountryName(String isoCode, String defaultValue) {

		// first, check if the defaultValue actually equals the defaultValue in english language. If not, it has been customized
		String trueDefaultName = getCountryName(Language.EN, isoCode);
		if (!StringUtils.isEmpty(trueDefaultName) && !trueDefaultName.equalsIgnoreCase(defaultValue)) {
			return defaultValue;
		}
		String translatedName = getCountryName(userLanguage.get(), isoCode);
		return StringUtils.isEmpty(translatedName) ? defaultValue : translatedName;
	}

	public static String getCountryName(Language language, String isoCode) {
		String nameLanguageKey = isoCode != null ? "country." + isoCode.toUpperCase() + ".name" : null;
		return getInstance(language).countryProperties.getString(nameLanguageKey);
	}

	private I18nProperties(Language language) {

		this.captionProperties = loadProperties("captions", language.getLocale());
		this.descriptionProperties = loadProperties("descriptions", language.getLocale());
		this.enumProperties = loadProperties("enum", language.getLocale());
		this.validationProperties = loadProperties("validations", language.getLocale());
		this.stringProperties = loadProperties("strings", language.getLocale());
		this.countryProperties = loadProperties("countries", language.getLocale());
		this.continentProperties = loadProperties("continents", language.getLocale());
		this.subcontinentProperties = loadProperties("subcontinents", language.getLocale());
	}

	private I18nProperties() {
		this(defaultLanguage);
	}

	public static String getContinentName(String defaultName) {
		String name = getInstance(userLanguage.get()).continentProperties
			.getString("continent." + cleanContinentOrSubcontinentDefaultName(defaultName) + ".name");
		return name != null ? name : defaultName;
	}

	public static String getSubcontinentName(String defaultName) {
		String name = getInstance(userLanguage.get()).subcontinentProperties
			.getString("subcontinent." + cleanContinentOrSubcontinentDefaultName(defaultName) + ".name");
		return name != null ? name : defaultName;
	}

	/**
	 * remove spaces und everything in brackets following (e.g. "Australia (Continent)" => "AUSTRALIA") for use with i18n-files
	 */
	private static String cleanContinentOrSubcontinentDefaultName(String defaultName) {
		return defaultName.substring(0, defaultName.contains("(") ? defaultName.indexOf("(") : defaultName.length())
			.trim()
			.replace(" ", "_")
			.toUpperCase();
	}

	public static ResourceBundle loadProperties(String propertiesGroup, Locale locale) {
		return new ResourceBundle(java.util.ResourceBundle.getBundle(propertiesGroup, locale, new UTF8Control()));
	}

	public static class UTF8Control extends Control {

		private static final char LOCALE_SEP = '-';

		public java.util.ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
			throws IllegalAccessException, InstantiationException, IOException {

			// The below is a copy of the default implementation.
			String bundleName = toBundleName(baseName, locale);
			String resourceName = toResourceName(bundleName, "properties");
			try (Reader reader = loadResource(loader, resourceName, reload)) {
				if (reader == null) {
					return null;
				} else {
					return new PropertyResourceBundle(reader);
				}
			}
		}

		/**
		 * Converts the given <code>baseName</code> and <code>locale</code>
		 * to the bundle name. This method is called from the default
		 * implementation of the {@link #newBundle(String, Locale, String,
		 * ClassLoader, boolean) newBundle} and {@link #needsReload(String,
		 * Locale, String, ClassLoader, ResourceBundle, long) needsReload}
		 * methods.
		 *
		 * <p>
		 * In contrast to <code>ResourceBundle.Control::toBundleName</code>
		 * '-' instead of '_' is used to separate the Locale components:
		 * </p>
		 *
		 * <p>
		 * This implementation returns the following value:
		 *
		 * <pre>
		 * baseName + "_" + language + "-" + script + "-" + country + "-" + variant
		 * </pre>
		 *
		 * where <code>language</code>, <code>script</code>, <code>country</code>,
		 * and <code>variant</code> are the language, script, country, and variant
		 * values of <code>locale</code>, respectively. Final component values that
		 * are empty Strings are omitted along with the preceding '-'. When the
		 * script is empty, the script value is omitted along with the preceding '_'.
		 * If all of the values are empty strings, then <code>baseName</code>
		 * is returned.
		 *
		 * <p>
		 * For example, if <code>baseName</code> is
		 * <code>"baseName"</code> and <code>locale</code> is
		 * <code>Locale("ja",&nbsp;"",&nbsp;"XX")</code>, then
		 * <code>"baseName_ja-&thinsp;-XX"</code> is returned. If the given
		 * locale is <code>Locale("en")</code>, then
		 * <code>"baseName_en"</code> is returned.
		 *
		 * <p>
		 * Overriding this method allows applications to use different
		 * conventions in the organization and packaging of localized
		 * resources.
		 *
		 * @param baseName
		 *            the base name of the resource bundle, a fully
		 *            qualified class name
		 * @param locale
		 *            the locale for which a resource bundle should be
		 *            loaded
		 * @return the bundle name for the resource bundle
		 * @exception NullPointerException
		 *                if <code>baseName</code> or <code>locale</code>
		 *                is <code>null</code>
		 */
		@Override
		public String toBundleName(String baseName, Locale locale) {
			if (locale == Locale.ROOT) {
				return baseName;
			}

			String language = locale.getLanguage();
			String script = locale.getScript();
			String country = locale.getCountry();
			String variant = locale.getVariant();

			if (language == "" && country == "" && variant == "") {
				return baseName;
			}

			StringBuilder sb = new StringBuilder(baseName);
			sb.append('_');
			if (script != "") {
				if (variant != "") {
					sb.append(language).append(LOCALE_SEP).append(script).append(LOCALE_SEP).append(country).append(LOCALE_SEP).append(variant);
				} else if (country != "") {
					sb.append(language).append(LOCALE_SEP).append(script).append(LOCALE_SEP).append(country);
				} else {
					sb.append(language).append(LOCALE_SEP).append(script);
				}
			} else {
				if (variant != "") {
					sb.append(language).append(LOCALE_SEP).append(country).append(LOCALE_SEP).append(variant);
				} else if (country != "") {
					sb.append(language).append(LOCALE_SEP).append(country);
				} else {
					sb.append(language);
				}
			}
			return sb.toString();

		}

		private Reader loadResource(ClassLoader loader, String resourceName, boolean reload) throws IOException {

			URL url = loader.getResource(resourceName);
			if (url == null) {
				return null;
			}

			URLConnection connection = url.openConnection();

			if (reload) {
				connection.setUseCaches(false);
			}

			return new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8);
		}
	}
}
