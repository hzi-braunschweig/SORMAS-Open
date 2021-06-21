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
package de.symeda.sormas.backend;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.File;
import java.lang.reflect.Field;
import java.security.Principal;
import java.util.Properties;

import javax.ejb.SessionContext;
import javax.ejb.TimerService;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.Specializes;
import javax.jms.ConnectionFactory;
import javax.jms.Topic;
import javax.mail.Session;
import javax.transaction.UserTransaction;

import de.symeda.sormas.api.SormasToSormasConfig;
import de.symeda.sormas.api.utils.InfoProvider;
import de.symeda.sormas.backend.common.ConfigFacadeEjb;
import de.symeda.sormas.backend.sormastosormas.ServerAccessDataService;
import de.symeda.sormas.backend.sormastosormas.SormasToSormasEncryptionFacadeEjb.SormasToSormasEncryptionFacadeEjbLocal;
import de.symeda.sormas.backend.sormastosormas.SormasToSormasRestClient;
import de.symeda.sormas.backend.sormastosormas.SormasToSormasRestClientProducer;

/**
 * Creates mocks for resources needed in bean test / external services.
 * 
 * @author Stefan Kock
 */
public class MockProducer {

	private static final SessionContext sessionContext = mock(SessionContext.class);
	private static final Principal principal = mock(Principal.class);
	private static final Topic topic = mock(Topic.class);
	private static final ConnectionFactory connectionFactory = mock(ConnectionFactory.class);
	private static final TimerService timerService = mock(TimerService.class);
	public static final Properties properties = new Properties();
	private static final UserTransaction userTransaction = mock(UserTransaction.class);
	private static final SormasToSormasRestClient SORMAS_TO_SORMAS_REST_CLIENT = mock(SormasToSormasRestClient.class);
	private static final ManagedScheduledExecutorService managedScheduledExecutorService = mock(ManagedScheduledExecutorService.class);
	private static final String TMP_PATH = "target/tmp";

	// Receiving e-mail server is mocked: org. jvnet. mock_javamail. mailbox
	private static Session mailSession;

	static {
		properties.setProperty(ConfigFacadeEjb.COUNTRY_NAME, "nigeria");
		properties.setProperty(ConfigFacadeEjb.CSV_SEPARATOR, ";");
		properties.setProperty(ConfigFacadeEjb.TEMP_FILES_PATH, TMP_PATH);

		try {
			Field instance = InfoProvider.class.getDeclaredField("instance");
			instance.setAccessible(true);
			instance.set(null, spy(InfoProvider.class));

			File tmpDir = new File(TMP_PATH);
			if (!tmpDir.exists()) {
				tmpDir.mkdir();
			}

		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}

		// Make sure that the default session does not use a local mail server (if mock-javamail is removed)
		mailSession = Session.getInstance(properties);
	}

	static {
		wireMocks();
	}

	public static void resetMocks() {

		reset(
			sessionContext,
			principal,
			topic,
			connectionFactory,
			timerService,
			userTransaction,
			SORMAS_TO_SORMAS_REST_CLIENT,
			managedScheduledExecutorService);
		wireMocks();
	}

	public static void wireMocks() {

		when(sessionContext.getCallerPrincipal()).thenReturn(getPrincipal());
	}

	@Produces
	public static SessionContext getSessionContext() {
		return sessionContext;
	}

	@Produces
	public static Topic getTopic() {
		return topic;
	}

	@Produces
	public static ConnectionFactory getConnectionFactory() {
		return connectionFactory;
	}

	@Produces
	public static TimerService getTimerService() {
		return timerService;
	}

	@Produces
	public static Session getMailSession() {
		return mailSession;
	}

	@Produces
	public static Properties getProperties() {
		return properties;
	}

	@Produces
	public static UserTransaction getUserTransaction() {
		return userTransaction;
	}

	@Produces
	public static Principal getPrincipal() {
		return principal;
	}

	public static SormasToSormasRestClient getSormasToSormasClient() {
		return SORMAS_TO_SORMAS_REST_CLIENT;
	}

	@Specializes
	public static class MockRestClientBuilderProducer extends SormasToSormasRestClientProducer {

		@Override
		@Produces
		public SormasToSormasRestClient sormasToSormasClient(
			ServerAccessDataService serverAccessDataService,
			SormasToSormasEncryptionFacadeEjbLocal sormasToSormasEncryptionEjb,
			SormasToSormasConfig sormasToSormasConfig) {
			return SORMAS_TO_SORMAS_REST_CLIENT;
		}
	}

	@Produces
	public static ManagedScheduledExecutorService getManagedScheduledExecutorService() {
		return managedScheduledExecutorService;
	}

	public static void mockProperty(String property, String value) {
		properties.setProperty(property, value);
	}
}
