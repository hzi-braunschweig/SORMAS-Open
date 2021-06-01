/*
 * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * Copyright © 2016-2021 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package de.symeda.sormas.api.sormastosormas;

import java.util.List;

import javax.ejb.Remote;

@Remote
public interface SormasToSormasEntityInterface {

	/**
	 * Send entities referenced by `entityUuid` to another SORMAS instance.
	 * 
	 * @param entityUuids
	 *            The UUIDs of the entities being shared.
	 * @param options
	 *            Meta data for the request.
	 * @throws SormasToSormasException
	 *             Throws in case the data cannot be shared.
	 */
	void shareEntities(List<String> entityUuids, SormasToSormasOptionsDto options) throws SormasToSormasException;

	/**
	 * Decrypt, process, and store shared entities received from another SORMAS instance.
	 * 
	 * @param encryptedData
	 *            The encrypted payload extracted from the request.
	 * @throws SormasToSormasException
	 *             Throws in case the operation did not succeed.
	 * @throws SormasToSormasValidationException
	 *             Throws in case the data could not be validated successfully.
	 */
	void saveSharedEntities(SormasToSormasEncryptedDataDto encryptedData) throws SormasToSormasException, SormasToSormasValidationException;

	/**
	 * Return a single, previously shared entity referenced by `entityUuid` back to the sender.
	 * 
	 * @param entityUuid
	 *            The UUID of the entity being returned.
	 * @param options
	 *            Meta data for the request.
	 * @throws SormasToSormasException
	 *             Throws in case the data cannot be shared.
	 */
	void returnEntity(String entityUuid, SormasToSormasOptionsDto options) throws SormasToSormasException;

	/**
	 * Decrypt, process, and store an entity that was returned (after previously sharing it) by another SORMAS instance.
	 *
	 * @param encryptedData
	 *            The encrypted payload extracted from the request.
	 * @throws SormasToSormasException
	 *             Throws in case the operation did not succeed.
	 * @throws SormasToSormasValidationException
	 *             Throws in case the data could not be validated successfully.
	 */
	void saveReturnedEntity(SormasToSormasEncryptedDataDto encryptedData) throws SormasToSormasException, SormasToSormasValidationException;

	/**
	 * Synchronize an entity referenced by `entityUuid` with another SORMAS instance.
	 *
	 * @param entityUuid
	 *            The UUIDs of the entities being synchronized.
	 * @param options
	 *            Meta data for the request.
	 * @throws SormasToSormasException
	 *             Throws in case the data cannot be shared.
	 */

	void syncEntity(String entityUuid, SormasToSormasOptionsDto options) throws SormasToSormasException;

	/**
	 * Decrypt, process, and store an entity for which a synchronization was requested (after previously sharing it) by another SORMAS
	 * instance.
	 * 
	 * @param encryptedData
	 *            The encrypted payload extracted from the request.
	 * @throws SormasToSormasException
	 *             Throws in case the operation did not succeed.
	 * @throws SormasToSormasValidationException
	 *             Throws in case the data could not be validated successfully.
	 */
	void saveSyncedEntity(SormasToSormasEncryptedDataDto encryptedData) throws SormasToSormasException, SormasToSormasValidationException;
}
