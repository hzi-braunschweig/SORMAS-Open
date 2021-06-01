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

package de.symeda.sormas.backend.sormastosormas;

import static de.symeda.sormas.backend.sormastosormas.ValidationHelper.buildValidationGroupName;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.ejb.EJB;
import javax.inject.Inject;

import de.symeda.sormas.api.EntityDto;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Validations;
import de.symeda.sormas.api.sormastosormas.SormasToSormasDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasEncryptedDataDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasEntityInterface;
import de.symeda.sormas.api.sormastosormas.SormasToSormasException;
import de.symeda.sormas.api.sormastosormas.SormasToSormasOptionsDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasValidationException;
import de.symeda.sormas.api.sormastosormas.ValidationErrors;
import de.symeda.sormas.api.user.UserReferenceDto;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.api.utils.SormasToSormasEntityDto;
import de.symeda.sormas.backend.common.AbstractDomainObject;
import de.symeda.sormas.backend.common.BaseAdoService;
import de.symeda.sormas.backend.user.User;
import de.symeda.sormas.backend.user.UserService;

public abstract class AbstractSormasToSormasInterface<T extends AbstractDomainObject & SormasToSormasEntity, U extends EntityDto & SormasToSormasEntityDto, S extends SormasToSormasDto<U>, P extends ProcessedData<U>>
	implements SormasToSormasEntityInterface {

	@EJB
	private UserService userService;
	@Inject
	private SormasToSormasRestClient sormasToSormasRestClient;
	@EJB
	private SormasToSormasShareInfoService shareInfoService;
	@EJB
	private SormasToSormasFacadeHelper sormasToSormasFacadeHelper;
	@EJB
	private SormasToSormasOriginInfoService originInfoService;

	private final String saveEndpoint;
	private final String syncEndpoint;

	private final String entityCaptionTag;

	public AbstractSormasToSormasInterface() {
		throw new RuntimeException("AbstractSormasToSormasInterface should not be instantiated");
	}

	public AbstractSormasToSormasInterface(String saveEndpoint, String syncEndpoint, String entityCaptionTag) {
		this.saveEndpoint = saveEndpoint;
		this.syncEndpoint = syncEndpoint;
		this.entityCaptionTag = entityCaptionTag;
	}

	protected abstract BaseAdoService<T> getEntityService();

	protected abstract ShareDataBuilder<T, S> getShareDataBuilder();

	protected abstract ReceivedDataProcessor<U, S, P> getReceivedDataProcessor();

	protected abstract ProcessedDataPersister<P> getProcessedDataPersister();

	protected abstract Class<S[]> getShareDataClass();

	protected abstract void validateEntitiesBeforeSend(List<T> entities) throws SormasToSormasException;

	/**
	 * Must ensure in the first place that the new, shared entity is NOT stored on the system. We need to error if the entity already
	 * exists.
	 * 
	 * Additional checks for invariants can be added (e.g., relation between contacts and cases).
	 * 
	 * @param entity
	 *            The entity which should NOT exist on the system.
	 * @return Validation error if any. An error is inserted if the entity already exists.
	 */
	protected abstract ValidationErrors validateSharedEntity(U entity);

	/**
	 * Must ensure in the first place that the received entity is already stored on the system. We need to error if the entity is NOT
	 * existing.
	 * Additional checks for invariants can be added (e.g., relation between contacts and cases).
	 * 
	 * @param entity
	 *            The entity which should exist on the system.
	 * @return Validation error if any. An error is inserted if the entity does NOT exist.
	 */
	protected ValidationErrors validateExistingEntity(U entity, List<U> existingEntities) {
		ValidationErrors errors = new ValidationErrors();

		if (existingEntities.stream().noneMatch(e -> e.getUuid().equals(entity.getUuid()))) {
			errors
				.add(I18nProperties.getCaption(entityCaptionTag), I18nProperties.getValidationError(Validations.sormasToSormasReturnEntityNotExists));

		}

		return errors;
	}

	protected abstract void setEntityShareInfoAssociatedObject(SormasToSormasShareInfo sormasToSormasShareInfo, T entity);

	protected abstract SormasToSormasShareInfo getShareInfoByEntityAndOrganization(String entityUuid, String organizationId);

	protected abstract List<U> loadExistingEntities(List<String> uuids);

	@Override
	public void shareEntities(List<String> entityUuids, SormasToSormasOptionsDto options) throws SormasToSormasException {
		User currentUser = userService.getCurrentUser();
		List<T> entities = getEntityService().getByUuids(entityUuids);

		validateEntitiesBeforeSend(entities);

		List<S> entitiesToSend = new ArrayList<>();
		List<AssociatedEntityWrapper<?>> associatedEntities = new ArrayList<>();

		for (T entity : entities) {
			ShareData<S> shareData = getShareDataBuilder().buildShareData(entity, currentUser, options);
			entitiesToSend.add(shareData.getDto());
			associatedEntities.addAll(shareData.getAssociatedEntities());
		}

		sormasToSormasFacadeHelper.sendEntitiesToSormas(
			entitiesToSend,
			options,
			(host, authToken, encryptedData) -> sormasToSormasRestClient.post(host, saveEndpoint, authToken, encryptedData));

		entities.forEach(entity -> saveNewShareInfo(currentUser.toReference(), options, entity, this::setEntityShareInfoAssociatedObject));
		associatedEntities.forEach(wrapper -> {
			saveNewShareInfo(currentUser.toReference(), options, wrapper.getEntity(), (s, e) -> {
				wrapper.setShareInfoAssociatedObject(s);
			});
		});
	}

	@Override
	public void saveSharedEntities(SormasToSormasEncryptedDataDto encryptedData) throws SormasToSormasException, SormasToSormasValidationException {
		saveNewEntities(encryptedData, data -> getProcessedDataPersister().persistSharedData(data));
	}

	@Override
	public void returnEntity(String entityUuid, SormasToSormasOptionsDto options) throws SormasToSormasException {
		options.setHandOverOwnership(true);
		User currentUser = userService.getCurrentUser();

		T entity = getEntityService().getByUuid(entityUuid);
		validateEntitiesBeforeSend(Collections.singletonList(entity));

		ShareData<S> shareData = getShareDataBuilder().buildShareData(entity, currentUser, options);

		sormasToSormasFacadeHelper.sendEntitiesToSormas(
			Collections.singletonList(shareData.getDto()),
			options,
			(host, authToken, encryptedData) -> sormasToSormasRestClient.put(host, saveEndpoint, authToken, encryptedData));

		entity.getSormasToSormasOriginInfo().setOwnershipHandedOver(false);
		originInfoService.persist(entity.getSormasToSormasOriginInfo());

		shareData.getAssociatedEntities().forEach(wrapper -> {
			if (wrapper.getEntity().getSormasToSormasOriginInfo() == null) {
				saveNewShareInfo(currentUser.toReference(), options, wrapper.getEntity(), (s, e) -> {
					wrapper.setShareInfoAssociatedObject(s);
				});
			}
		});
	}

	@Override
	public void saveReturnedEntity(SormasToSormasEncryptedDataDto encryptedData) throws SormasToSormasException, SormasToSormasValidationException {
		saveExistingEntities(encryptedData, data -> getProcessedDataPersister().persistReturnedData(data, data.getOriginInfo()));
	}

	@Override
	public void syncEntity(String entityUuid, SormasToSormasOptionsDto options) throws SormasToSormasException {
		User currentUser = userService.getCurrentUser();
		T entity = getEntityService().getByUuid(entityUuid);

		validateEntitiesBeforeSend(Collections.singletonList(entity));

		ShareData<S> shareData = getShareDataBuilder().buildShareData(entity, currentUser, options);

		sormasToSormasFacadeHelper.sendEntitiesToSormas(
			Collections.singletonList(shareData.getDto()),
			options,
			(host, authToken, encryptedData) -> sormasToSormasRestClient.post(host, syncEndpoint, authToken, encryptedData));

		SormasToSormasShareInfo shareInfo = getShareInfoByEntityAndOrganization(entity.getUuid(), options.getOrganization().getUuid());
		updateShareInfoOptions(shareInfo, options);

		shareData.getAssociatedEntities().forEach(entityWrapper -> {
			SormasToSormasShareInfo sampleShareInfo = entityWrapper.getExistingShareInfo(shareInfoService, options.getOrganization().getUuid());
			if (sampleShareInfo == null) {
				saveNewShareInfo(
					currentUser.toReference(),
					options,
					entityWrapper.getEntity(),
					(i, ae) -> entityWrapper.setShareInfoAssociatedObject(i));
			} else {
				updateShareInfoOptions(sampleShareInfo, options);
			}
		});
	}

	@Override
	public void saveSyncedEntity(SormasToSormasEncryptedDataDto encryptedData) throws SormasToSormasException, SormasToSormasValidationException {
		saveExistingEntities(encryptedData, data -> getProcessedDataPersister().persistSyncData(data));

	}

	private interface Persister<P> {

		/**
		 * Lambda for final persisting operation passed to each save function.
		 * 
		 * @param data
		 *            The processed data that should be saved. See {@link ProcessedDataPersister} for details.
		 * @throws SormasToSormasValidationException
		 *             Throws in case the data cannot be saved.
		 */
		void call(P data) throws SormasToSormasValidationException;
	}

	/**
	 * Handles the saving of entities received through S2S in the do not previously exists in the system and need to be
	 * created. This is the case for the share operations, as new entities enter the system.
	 * 
	 * @param encryptedData
	 *            The encrypted payload extracted from the request.
	 * @param persister
	 *            Lambda that is executed at the very end to persist the data at the very end. See {@link ProcessedDataPersister} for
	 *            details.
	 * @throws SormasToSormasException
	 *             Throws in case the operation did not succeed.
	 * @throws SormasToSormasValidationException
	 *             Throws in case the received data was not valid.
	 */
	private void saveNewEntities(SormasToSormasEncryptedDataDto encryptedData, Persister<P> persister)
		throws SormasToSormasException, SormasToSormasValidationException {
		decryptAndSave(encryptedData, persister, false);
	}

	/**
	 * Handles the saving of entities received through S2S in the case they already exists in the system.
	 * This is the case for return and sync operations as the case existed before it was shared to another instance
	 * 
	 * @param encryptedData
	 *            The encrypted payload extracted from the request.
	 * @param persister
	 *            Lambda that is executed at the very end to persist the data at the very end. See {@link ProcessedDataPersister} for
	 *            details.
	 * @throws SormasToSormasException
	 *             Throws in case the operation did not succeed.
	 * @throws SormasToSormasValidationException
	 *             Throws in case the received data was not valid.
	 */
	private void saveExistingEntities(SormasToSormasEncryptedDataDto encryptedData, Persister<P> persister)
		throws SormasToSormasException, SormasToSormasValidationException {
		decryptAndSave(encryptedData, persister, true);
	}

	/**
	 * Decrypts the received data, validates it, and finally persists it. The function has two modes governed by the `existing` flag, If it
	 * is set to true, the function will attempt to resolve and process existing entities. If set to false, it assumes that the entities are
	 * should be newly created and no scann for exisitng entities is performed.
	 *
	 * @param encryptedData
	 *            The encrypted payload extracted from the request which is getting finally decrypted by this function.
	 * @param persister
	 *            Lambda that is executed at the very end to persist the data at the very end. See {@link ProcessedDataPersister} for
	 *            details.
	 * @param existing
	 *            True for return and sync mode (as entities already exist on the system), else false.
	 * @throws SormasToSormasException
	 *             Throws in case the operation did not succeed.
	 * @throws SormasToSormasValidationException
	 *             Throws in case the received data was not valid.
	 */
	private void decryptAndSave(SormasToSormasEncryptedDataDto encryptedData, Persister<P> persister, boolean existing)
		throws SormasToSormasException, SormasToSormasValidationException {
		S[] receivedS2SEntities = sormasToSormasFacadeHelper.decryptSharedData(encryptedData, getShareDataClass());
		Map<String, ValidationErrors> validationErrors = new HashMap<>();
		List<P> dataToSave = new ArrayList<>(receivedS2SEntities.length);

		List<U> existingEntities = null;
		Map<String, U> existingEntitiesMap = null;
		if (existing) {
			// for all received entities, load the corresponding existing entities 
			existingEntities =
				loadExistingEntities(Arrays.stream(receivedS2SEntities).map(e -> e.getEntity().getUuid()).collect(Collectors.toList()));
			existingEntitiesMap = existingEntities.stream().collect(Collectors.toMap(EntityDto::getUuid, Function.identity()));
		}

		for (S receivedS2SEntity : receivedS2SEntities) {
			try {
				U receivedEntity = receivedS2SEntity.getEntity();
				ValidationErrors validationError;
				if (existing) {
					// check that the received entity is already existing
					validationError = validateExistingEntity(receivedEntity, existingEntities);
				} else {
					// check that the new, shared entity is not existing
					validationError = validateSharedEntity(receivedEntity);
				}
				if (validationError.hasError()) {
					validationErrors.put(buildValidationGroupName(entityCaptionTag, receivedEntity), validationError);
				} else {
					if (existing) {
						U existingEntity = existingEntitiesMap.get(receivedEntity.getUuid());

						// take the received and existing entity and process it
						P processedData = getReceivedDataProcessor().processReceivedData(receivedS2SEntity, existingEntity);

						dataToSave.add(processedData);
					} else {
						// take the new received process it
						P processedData = getReceivedDataProcessor().processReceivedData(receivedS2SEntity, null);

						// todo this looks like a no op
						processedData.getEntity().setSormasToSormasOriginInfo(processedData.getOriginInfo());

						dataToSave.add(processedData);
					}
				}
			} catch (SormasToSormasValidationException validationException) {
				validationErrors.putAll(validationException.getErrors());
			}
		}
		if (validationErrors.size() > 0) {
			throw new SormasToSormasValidationException(validationErrors);
		}
		for (P data : dataToSave) {
			persister.call(data);
		}
	}

	private <A> void saveNewShareInfo(
		UserReferenceDto sender,
		SormasToSormasOptionsDto options,
		A associatedObject,
		BiConsumer<SormasToSormasShareInfo, A> setAssociatedObject) {
		SormasToSormasShareInfo shareInfo = new SormasToSormasShareInfo();

		shareInfo.setUuid(DataHelper.createUuid());
		shareInfo.setCreationDate(new Timestamp(new Date().getTime()));
		shareInfo.setOrganizationId(options.getOrganization().getUuid());
		shareInfo.setSender(userService.getByReferenceDto(sender));

		addOptionsToShareInfo(options, shareInfo);
		setAssociatedObject.accept(shareInfo, associatedObject);

		shareInfoService.ensurePersisted(shareInfo);
	}

	private void addOptionsToShareInfo(SormasToSormasOptionsDto options, SormasToSormasShareInfo shareInfo) {
		shareInfo.setOwnershipHandedOver(options.isHandOverOwnership());
		shareInfo.setWithAssociatedContacts(options.isWithAssociatedContacts());
		shareInfo.setWithSamples(options.isWithSamples());
		shareInfo.setWithEventParticipants(options.isWithEventParticipants());
		shareInfo.setPseudonymizedPersonalData(options.isPseudonymizePersonalData());
		shareInfo.setPseudonymizedSensitiveData(options.isPseudonymizeSensitiveData());
		shareInfo.setComment(options.getComment());
	}

	private void updateShareInfoOptions(SormasToSormasShareInfo shareInfo, SormasToSormasOptionsDto options) {
		addOptionsToShareInfo(options, shareInfo);
		shareInfoService.ensurePersisted(shareInfo);
	}
}
