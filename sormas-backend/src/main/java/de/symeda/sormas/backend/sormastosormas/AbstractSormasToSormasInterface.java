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
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.symeda.sormas.api.EntityDto;
import de.symeda.sormas.api.HasUuid;
import de.symeda.sormas.api.feature.FeatureType;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.i18n.Validations;
import de.symeda.sormas.api.sormastosormas.SormasToSormasDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasEncryptedDataDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasEntityInterface;
import de.symeda.sormas.api.sormastosormas.SormasToSormasException;
import de.symeda.sormas.api.sormastosormas.SormasToSormasOptionsDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasOriginInfoDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasValidationException;
import de.symeda.sormas.api.sormastosormas.ValidationErrors;
import de.symeda.sormas.api.sormastosormas.sharerequest.ShareRequestDataType;
import de.symeda.sormas.api.sormastosormas.sharerequest.ShareRequestStatus;
import de.symeda.sormas.api.sormastosormas.sharerequest.SormasToSormasShareRequestDto;
import de.symeda.sormas.api.user.UserReferenceDto;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.api.utils.SormasToSormasEntityDto;
import de.symeda.sormas.backend.common.AbstractDomainObject;
import de.symeda.sormas.backend.common.BaseAdoService;
import de.symeda.sormas.backend.sormastosormas.SormasToSormasEncryptionFacadeEjb.SormasToSormasEncryptionFacadeEjbLocal;
import de.symeda.sormas.backend.feature.FeatureConfigurationFacadeEjb.FeatureConfigurationFacadeEjbLocal;
import de.symeda.sormas.backend.sormastosormas.shareinfo.SormasToSormasShareInfo;
import de.symeda.sormas.backend.sormastosormas.shareinfo.SormasToSormasShareInfoService;
import de.symeda.sormas.backend.sormastosormas.sharerequest.SormasToSormasShareRequestFacadeEJB.SormasToSormasShareRequestFacadeEJBLocal;
import de.symeda.sormas.backend.user.User;
import de.symeda.sormas.backend.user.UserService;

public abstract class AbstractSormasToSormasInterface<ADO extends AbstractDomainObject & SormasToSormasEntity, DTO extends EntityDto & SormasToSormasEntityDto, S extends SormasToSormasDto<DTO>, PREVIEW extends HasUuid, PROCESSED extends ProcessedData<DTO>>
	implements SormasToSormasEntityInterface {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSormasToSormasInterface.class);

	@EJB
	private UserService userService;
	@Inject
	private SormasToSormasRestClient sormasToSormasRestClient;
	@EJB
	private SormasToSormasShareInfoService shareInfoService;
	@EJB
	private SormasToSormasOriginInfoService originInfoService;
	@EJB
	private SormasToSormasShareRequestFacadeEJBLocal shareRequestFacade;
	@EJB
	private ShareDataBuilderHelper dataBuilderHelper;
	@EJB
	private ReceivedDataProcessorHelper dataProcessorHelper;
	@EJB
	private FeatureConfigurationFacadeEjbLocal featureConfigurationFacade;
	@EJB
	private SormasToSormasEncryptionFacadeEjbLocal sormasToSormasEncryptionEjb;

	private final String requestEndpoint;
	private final String requestRejectEndpoint;
	private final String requestAcceptEndpoint;
	private final String saveEndpoint;
	private final String syncEndpoint;

	private final String entityCaptionTag;

	private final ShareRequestDataType shareRequestDataType;
	private final Class<? extends ShareRequestData<PREVIEW>> previewType;

	public AbstractSormasToSormasInterface() {
		throw new RuntimeException("AbstractSormasToSormasInterface should not be instantiated");
	}

	public AbstractSormasToSormasInterface(
		String requestEndpoint,
		String requestRejectEndpoint,
		String requestAcceptEndpoint,
		String saveEndpoint,
		String syncEndpoint,
		String entityCaptionTag,
		ShareRequestDataType shareRequestDataType,
		Class<? extends ShareRequestData<PREVIEW>> previewType) {
		this.requestEndpoint = requestEndpoint;
		this.requestRejectEndpoint = requestRejectEndpoint;
		this.requestAcceptEndpoint = requestAcceptEndpoint;
		this.saveEndpoint = saveEndpoint;
		this.syncEndpoint = syncEndpoint;
		this.entityCaptionTag = entityCaptionTag;
		this.shareRequestDataType = shareRequestDataType;
		this.previewType = previewType;
	}

	@Override
	public void share(List<String> entityUuids, SormasToSormasOptionsDto options) throws SormasToSormasException {
		if (featureConfigurationFacade.isFeatureEnabled(FeatureType.SORMAS_TO_SORMAS_ACCEPT_REJECT)) {
			sendShareRequest(entityUuids, options);
		} else {
			shareEntities(entityUuids, options);
		}
	}

	public void sendShareRequest(List<String> entityUuids, SormasToSormasOptionsDto options) throws SormasToSormasException {
		User currentUser = userService.getCurrentUser();
		List<ADO> entities = getEntityService().getByUuids(entityUuids);

		if (options.isHandOverOwnership()) {
			validateOwnership(entities);
		}

		validateEntitiesBeforeShare(entities, options.isHandOverOwnership());

		List<PREVIEW> previewsToSend = new ArrayList<>();
		List<AssociatedEntityWrapper<?>> associatedEntities = new ArrayList<>();
		for (ADO entity : entities) {
			ShareData<ADO, PREVIEW> shareData = getShareDataBuilder().buildShareDataPreview(entity, currentUser, options);

			previewsToSend.add(shareData.getDto());

			associatedEntities.addAll(shareData.getAssociatedEntities());
		}

		SormasToSormasOriginInfoDto originInfo =
			dataBuilderHelper.createSormasToSormasOriginInfo(currentUser, options.isHandOverOwnership(), options.getComment());
		String requestUuid = DataHelper.createUuid();

		sormasToSormasRestClient
			.post(options.getOrganization().getUuid(), requestEndpoint, new ShareRequestData<>(requestUuid, previewsToSend, originInfo), null);

		saveNewShareInfo(currentUser.toReference(), options, requestUuid, ShareRequestStatus.PENDING, entities, associatedEntities);
	}

	@Override
	public void saveShareRequest(SormasToSormasEncryptedDataDto encryptedData) throws SormasToSormasException, SormasToSormasValidationException {
		ShareRequestData<PREVIEW> shareData = sormasToSormasEncryptionEjb.decryptAndVerify(encryptedData, previewType);

		Map<String, ValidationErrors> validationErrors = new HashMap<>();
		List<PREVIEW> previews = shareData.getPreviews();
		List<PREVIEW> previewsToSave = new ArrayList<>(previews.size());

		SormasToSormasOriginInfoDto originInfo = shareData.getOriginInfo();
		ValidationErrors originInfoErrors = dataProcessorHelper.processOriginInfo(originInfo, entityCaptionTag);
		if (originInfoErrors.hasError()) {
			validationErrors.put(I18nProperties.getCaption(entityCaptionTag), originInfoErrors);
		}

		for (PREVIEW preview : previews) {
			try {
				ValidationErrors caseErrors = validateSharedPreview(preview);

				if (caseErrors.hasError()) {
					validationErrors.put(buildEntityValidationGroupName(preview), caseErrors);
				} else {
					PREVIEW processedData = getReceivedDataProcessor().processReceivedPreview(preview);
					previewsToSave.add(processedData);
				}
			} catch (SormasToSormasValidationException validationException) {
				validationErrors.putAll(validationException.getErrors());
			}
		}

		if (validationErrors.size() > 0) {
			throw new SormasToSormasValidationException(validationErrors);
		}

		shareRequestFacade.saveShareRequest(createNewShareRequest(shareData.getRequestUuid(), originInfo, previewsToSave));
	}

	@Override
	public void sendRejectShareRequest(String uuid) throws SormasToSormasException {
		SormasToSormasShareRequestDto shareRequest = shareRequestFacade.getShareRequestByUuid(uuid);

		String organizationId = shareRequest.getOriginInfo().getOrganizationId();
		sormasToSormasRestClient.post(organizationId, requestRejectEndpoint, uuid, null);

		shareRequest.setChangeDate(new Date());
		shareRequest.setStatus(ShareRequestStatus.REJECTED);

		shareRequestFacade.saveShareRequest(shareRequest);
	}

	@Override
	@Transactional
	public void rejectShareRequest(SormasToSormasEncryptedDataDto encryptedRequestUuid) throws SormasToSormasException {
		String requestUuid = sormasToSormasEncryptionEjb.decryptAndVerify(encryptedRequestUuid, String.class);
		SormasToSormasShareInfo shareInfo = shareInfoService.getByRequestUuid(requestUuid);
		shareInfo.setRequestStatus(ShareRequestStatus.REJECTED);
		shareInfo.setOwnershipHandedOver(false);
		shareInfoService.ensurePersisted(shareInfo);
	}

	@Override
	@Transactional
	public void acceptShareRequest(String uuid) throws SormasToSormasException, SormasToSormasValidationException {
		SormasToSormasShareRequestDto shareRequest = shareRequestFacade.getShareRequestByUuid(uuid);
		String organizationId = shareRequest.getOriginInfo().getOrganizationId();

		SormasToSormasEncryptedDataDto encryptedData = sormasToSormasRestClient
			.post(organizationId, requestAcceptEndpoint, uuid, SormasToSormasEncryptedDataDto.class);

		saveSharedEntities(encryptedData, shareRequest.getOriginInfo());

		shareRequest.setChangeDate(new Date());
		shareRequest.setStatus(ShareRequestStatus.ACCEPTED);
		shareRequestFacade.saveShareRequest(shareRequest);
	}

	@Override
	public SormasToSormasEncryptedDataDto getDataForShareRequest(SormasToSormasEncryptedDataDto encryptedRequestUuid) throws SormasToSormasException {
		User currentUser = userService.getCurrentUser();
		String requestUuid = sormasToSormasEncryptionEjb.decryptAndVerify(encryptedRequestUuid, String.class);
		SormasToSormasShareInfo shareInfo = shareInfoService.getByRequestUuid(requestUuid);

		List<ShareData<ADO, S>> shareData = getShareDataBuilder().buildShareData(shareInfo, currentUser);

		List<S> entitiesToSend = shareData.stream().map(ShareData::getDto).collect(Collectors.toList());
		validateEntitiesBeforeShare(shareData.stream().map(ShareData::getEntity).collect(Collectors.toList()), shareInfo.isOwnershipHandedOver());

		SormasToSormasEncryptedDataDto encrypted = sormasToSormasEncryptionEjb.signAndEncrypt(entitiesToSend, shareInfo.getOrganizationId());

		shareInfo.setRequestStatus(ShareRequestStatus.ACCEPTED);
		shareInfoService.ensurePersisted(shareInfo);

		return encrypted;
	}

	@Override
	public void shareEntities(List<String> entityUuids, SormasToSormasOptionsDto options) throws SormasToSormasException {
		User currentUser = userService.getCurrentUser();
		List<ADO> entities = getEntityService().getByUuids(entityUuids);

		validateEntitiesBeforeShare(entities, options.isHandOverOwnership());

		List<S> entitiesToSend = new ArrayList<>();
		List<AssociatedEntityWrapper<?>> associatedEntities = new ArrayList<>();
		for (ADO entity : entities) {
			ShareData<ADO, S> shareData = getShareDataBuilder().buildShareData(entity, currentUser, options);

			entitiesToSend.add(shareData.getDto());

			associatedEntities.addAll(shareData.getAssociatedEntities());
		}

		sormasToSormasRestClient.post(options.getOrganization().getUuid(), saveEndpoint, entitiesToSend, null);
		saveNewShareInfo(currentUser.toReference(), options, DataHelper.createUuid(), ShareRequestStatus.ACCEPTED, entities, associatedEntities);
	}

	@Override
	public void saveSharedEntities(SormasToSormasEncryptedDataDto encryptedData, SormasToSormasOriginInfoDto originInfo)
		throws SormasToSormasException, SormasToSormasValidationException {
		decryptAndPersist(encryptedData, data -> getProcessedDataPersister().persistSharedData(data), originInfo);
	}

	@Override
	public void returnEntity(String entityUuid, SormasToSormasOptionsDto options) throws SormasToSormasException {
		options.setHandOverOwnership(true);
		User currentUser = userService.getCurrentUser();

		ADO entity = getEntityService().getByUuid(entityUuid);
		validateEntitiesBeforeShare(Collections.singletonList(entity), options.isHandOverOwnership());

		ShareData<ADO, S> shareData = getShareDataBuilder().buildShareData(entity, currentUser, options);

		sormasToSormasRestClient.put(options.getOrganization().getUuid(), saveEndpoint, Collections.singletonList(shareData.getDto()), null);

		SormasToSormasOriginInfo originInfo = entity.getSormasToSormasOriginInfo();
		originInfo.setOwnershipHandedOver(false);
		originInfoService.persist(originInfo);

		List<AssociatedEntityWrapper<?>> sharedAssociatedObjects = shareData.getAssociatedEntities()
			.stream()
			.filter(wrapper -> wrapper.getEntity().getSormasToSormasOriginInfo() == null)
			.collect(Collectors.toList());

		saveNewShareInfo(
			currentUser.toReference(),
			options,
			// if SORMAS_TO_SORMAS_ACCEPT_REJECT feature is not active then there is no request, so generate a random uuid in that case
			originInfo.getRequest() != null ? originInfo.getRequest().getUuid() : DataHelper.createUuid(),
			ShareRequestStatus.ACCEPTED,
			Collections.emptyList(),
			sharedAssociatedObjects);
	}

	@Override
	public void saveReturnedEntity(SormasToSormasEncryptedDataDto encryptedData) throws SormasToSormasException, SormasToSormasValidationException {
		decryptAndPersist(encryptedData, data -> getProcessedDataPersister().persistReturnedData(data, data.getOriginInfo()), null);
	}

	@Override
	public void syncEntity(String entityUuid, SormasToSormasOptionsDto options) throws SormasToSormasException {
		User currentUser = userService.getCurrentUser();
		ADO entity = getEntityService().getByUuid(entityUuid);

		validateEntitiesBeforeShare(Collections.singletonList(entity), options.isHandOverOwnership());

		ShareData<ADO, S> shareData = getShareDataBuilder().buildShareData(entity, currentUser, options);

		sormasToSormasRestClient.post(options.getOrganization().getUuid(), syncEndpoint, Collections.singletonList(shareData.getDto()), null);

		SormasToSormasShareInfo shareInfo = getShareInfoByEntityAndOrganization(entity.getUuid(), options.getOrganization().getUuid());
		List<AssociatedEntityWrapper<?>> additionalAssociatedObjects = shareData.getAssociatedEntities()
			.stream()
			.filter(entityWrapper -> !entityWrapper.isAddedToShareInfo(shareInfo))
			.collect(Collectors.toList());

		updateShareInfoOptions(shareInfo, additionalAssociatedObjects, options);
	}

	@Override
	public void saveSyncedEntity(SormasToSormasEncryptedDataDto encryptedData) throws SormasToSormasException, SormasToSormasValidationException {
		decryptAndPersist(encryptedData, data -> getProcessedDataPersister().persistSyncData(data), null);
	}

	private interface Persister<PROCESSED> {

		void call(PROCESSED data) throws SormasToSormasValidationException;
	}

	private void decryptAndPersist(
		SormasToSormasEncryptedDataDto encryptedData,
		Persister<PROCESSED> persister,
		SormasToSormasOriginInfoDto originInfo)
		throws SormasToSormasException, SormasToSormasValidationException {

		S[] receivedS2SEntities = sormasToSormasEncryptionEjb.decryptAndVerify(encryptedData, getShareDataClass());

		Map<String, ValidationErrors> validationErrors = new HashMap<>();
		List<PROCESSED> entitiesToPersist = new ArrayList<>(receivedS2SEntities.length);
		List<DTO> existingEntities =
			loadExistingEntities(Arrays.stream(receivedS2SEntities).map(e -> e.getEntity().getUuid()).collect(Collectors.toList()));
		Map<String, DTO> existingEntitiesMap = existingEntities.stream().collect(Collectors.toMap(EntityDto::getUuid, Function.identity()));

		for (S receivedS2SEntity : receivedS2SEntities) {
			try {

				DTO receivedEntity = receivedS2SEntity.getEntity();
				ValidationErrors validationError;

				if (existingEntities.isEmpty()) {
					// check that the newly shared entity does not exist
					validationError = validateSharedEntity(receivedEntity);
				} else {
					// check that the received entity already exists
					validationError = validateExistingEntity(receivedEntity, existingEntities);
				}

				if (validationError.hasError()) {
					validationErrors.put(buildEntityValidationGroupName(receivedEntity), validationError);
				} else {
					DTO existingEntity = existingEntitiesMap.get(receivedEntity.getUuid());
					PROCESSED processedData = getReceivedDataProcessor().processReceivedData(receivedS2SEntity, existingEntity);

					if (existingEntities.isEmpty()) {
						// update origin info if we receive a new entity
						processedData.getEntity().setSormasToSormasOriginInfo(originInfo != null ? originInfo : processedData.getOriginInfo());
					}

					entitiesToPersist.add(processedData);
				}
			} catch (SormasToSormasValidationException validationException) {
				validationErrors.putAll(validationException.getErrors());
			}
		}

		if (validationErrors.size() > 0) {
			throw new SormasToSormasValidationException(validationErrors);
		}

		for (PROCESSED data : entitiesToPersist) {
			persister.call(data);
		}
	}

	private String buildEntityValidationGroupName(HasUuid entity) {
		return buildEntityValidationGroupName(entity.getUuid());
	}

	private String buildEntityValidationGroupName(String uuid) {
		return buildValidationGroupName(entityCaptionTag, uuid);
	}

	protected abstract BaseAdoService<ADO> getEntityService();

	protected abstract ShareDataBuilder<ADO, S, PREVIEW> getShareDataBuilder();

	protected abstract ReceivedDataProcessor<DTO, S, PROCESSED, PREVIEW> getReceivedDataProcessor();

	protected abstract ProcessedDataPersister<PROCESSED> getProcessedDataPersister();

	protected abstract Class<S[]> getShareDataClass();

	protected abstract void validateEntitiesBeforeShare(List<ADO> entities, boolean handOverOwnership) throws SormasToSormasException;

	protected abstract ValidationErrors validateSharedEntity(DTO entity);

	protected abstract ValidationErrors validateSharedPreview(PREVIEW preview);

	protected abstract void addEntityToShareInfo(SormasToSormasShareInfo sormasToSormasShareInfo, List<ADO> entities);

	protected abstract SormasToSormasShareInfo getShareInfoByEntityAndOrganization(String entityUuid, String organizationId);

	protected abstract List<DTO> loadExistingEntities(List<String> uuids);

	protected ValidationErrors validateExistingEntity(DTO entity, List<DTO> existingEntities) {
		ValidationErrors errors = new ValidationErrors();

		if (existingEntities.stream().noneMatch(e -> e.getUuid().equals(entity.getUuid()))) {
			errors
				.add(I18nProperties.getCaption(entityCaptionTag), I18nProperties.getValidationError(Validations.sormasToSormasReturnEntityNotExists));

		}

		return errors;
	}

	private void saveNewShareInfo(
		UserReferenceDto sender,
		SormasToSormasOptionsDto options,
		String requestUuid,
		ShareRequestStatus requestStatus,
		List<ADO> entities,
		List<AssociatedEntityWrapper<?>> associatedEntities) {
		SormasToSormasShareInfo shareInfo = new SormasToSormasShareInfo();

		shareInfo.setUuid(DataHelper.createUuid());
		shareInfo.setCreationDate(new Timestamp(new Date().getTime()));
		shareInfo.setOrganizationId(options.getOrganization().getUuid());
		shareInfo.setSender(userService.getByReferenceDto(sender));
		shareInfo.setRequestUuid(requestUuid);
		shareInfo.setRequestStatus(requestStatus);

		addOptionsToShareInfo(options, shareInfo);

		addEntityToShareInfo(shareInfo, entities);
		associatedEntities.forEach(e -> {
			e.addEntityToShareInfo(shareInfo);
		});

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

	private void updateShareInfoOptions(
		SormasToSormasShareInfo shareInfo,
		List<AssociatedEntityWrapper<?>> associatedObjects,
		SormasToSormasOptionsDto options) {
		addOptionsToShareInfo(options, shareInfo);
		associatedObjects.forEach(a -> a.addEntityToShareInfo(shareInfo));

		shareInfoService.ensurePersisted(shareInfo);
	}

	private SormasToSormasShareRequestDto createNewShareRequest(String requestUuid, SormasToSormasOriginInfoDto originInfo, List<PREVIEW> previews) {
		SormasToSormasShareRequestDto request = SormasToSormasShareRequestDto.build();
		request.setUuid(requestUuid);
		request.setDataType(shareRequestDataType);
		request.setStatus(ShareRequestStatus.PENDING);

		request.setOriginInfo(originInfo);

		setShareRequestPreviewData(request, previews);

		return request;
	}

	protected void validateOwnership(List<ADO> entities) throws SormasToSormasException {
		List<String> notOwnedUuids = getUuidsWithPendingOwnershipHandedOver(entities);
		if (notOwnedUuids.size() > 0) {

			Map<String, ValidationErrors> errors = notOwnedUuids.stream()
				.collect(
					Collectors.toMap(
						this::buildEntityValidationGroupName,
						(uuid) -> ValidationErrors.create(
							I18nProperties.getCaption(entityCaptionTag),
							I18nProperties.getString(Strings.errorSormasToSormasOwnershipAlreadyHandedOver))));

			throw new SormasToSormasException(I18nProperties.getString(Strings.errorSormasToSormasShare), errors);
		}
	}

	protected abstract List<String> getUuidsWithPendingOwnershipHandedOver(List<ADO> entities);

	protected abstract void setShareRequestPreviewData(SormasToSormasShareRequestDto request, List<PREVIEW> previews);
}
