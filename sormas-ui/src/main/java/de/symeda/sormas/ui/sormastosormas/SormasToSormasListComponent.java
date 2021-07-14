/*
 * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * Copyright © 2016-2020 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
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

package de.symeda.sormas.ui.sormastosormas;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.caze.CaseDataDto;
import de.symeda.sormas.api.contact.ContactDto;
import de.symeda.sormas.api.event.EventDto;
import de.symeda.sormas.api.event.EventParticipantDto;
import de.symeda.sormas.api.feature.FeatureType;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.sample.SampleDto;
import de.symeda.sormas.api.sormastosormas.ServerAccessDataReferenceDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasOriginInfoDto;
import de.symeda.sormas.api.sormastosormas.SormasToSormasShareTree;
import de.symeda.sormas.api.sormastosormas.shareinfo.SormasToSormasShareInfoCriteria;
import de.symeda.sormas.api.sormastosormas.shareinfo.SormasToSormasShareInfoDto;
import de.symeda.sormas.api.sormastosormas.sharerequest.ShareRequestStatus;
import de.symeda.sormas.api.user.UserDto;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.ui.ControllerProvider;
import de.symeda.sormas.ui.utils.ButtonHelper;
import de.symeda.sormas.ui.utils.CssStyles;
import de.symeda.sormas.ui.utils.DateFormatHelper;
import de.symeda.sormas.ui.utils.PaginationList;

public class SormasToSormasListComponent extends VerticalLayout {

	private static final long serialVersionUID = -7189942121987530912L;
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	private final SormasToSormasList sormasToSormasList;

	private SormasToSormasOriginInfoDto originInfo;
	private ShareDataLoader loadShares;
	private Button.ClickListener returnButtonClickListener;

	public SormasToSormasListComponent(CaseDataDto caze, boolean shareEnabled) {

		sormasToSormasList = new SormasToSormasList(
			caze.getSormasToSormasOriginInfo() == null,
			Captions.sormasToSormasCaseNotShared,
			(i) -> ControllerProvider.getSormasToSormasController().syncCase(caze, i),
			(i) -> ControllerProvider.getSormasToSormasController().revokeShare(i, this::reloadList));

		initLayout(
			caze.getSormasToSormasOriginInfo(),
			() -> FacadeProvider.getSormasToSormasCaseFacade().getAllShares(caze.getUuid()),
			shareEnabled ? e -> ControllerProvider.getSormasToSormasController().shareCaseFromDetailsPage(caze, this) : null,
			(e) -> ControllerProvider.getSormasToSormasController().returnCase(caze));
	}

	public SormasToSormasListComponent(ContactDto contact, boolean shareEnabled) {
		sormasToSormasList = new SormasToSormasList(
			contact.getSormasToSormasOriginInfo() == null,
			Captions.sormasToSormasContactNotShared,
			(i) -> ControllerProvider.getSormasToSormasController().syncContact(contact, i),
			(i) -> ControllerProvider.getSormasToSormasController().revokeShare(i, this::reloadList));

		initLayout(
			contact.getSormasToSormasOriginInfo(),
			() -> FacadeProvider.getSormasToSormasContactFacade().getAllShares(contact.getUuid()),
			shareEnabled ? e -> ControllerProvider.getSormasToSormasController().shareContactFromDetailsPage(contact, this) : null,
			(e) -> ControllerProvider.getSormasToSormasController().returnContact(contact));
	}

	public SormasToSormasListComponent(SampleDto sample) {
		sormasToSormasList = new SormasToSormasList(sample.getSormasToSormasOriginInfo() == null, Captions.sormasToSormasSampleNotShared, null, null);

		initLayout(
			sample.getSormasToSormasOriginInfo(),
			() -> FacadeProvider.getSormasToSormasShareInfoFacade()
				.getIndexList(new SormasToSormasShareInfoCriteria().sample(sample.toReference()), null, null)
				.stream()
				.map(s -> new SormasToSormasShareTree(null, s, Collections.emptyList()))
				.collect(Collectors.toList()),
			null,
			null);
	}

	public SormasToSormasListComponent(EventDto event, boolean shareEnabled) {

		sormasToSormasList = new SormasToSormasList(
			event.getSormasToSormasOriginInfo() == null,
			Captions.sormasToSormasEventNotShared,
			(i) -> ControllerProvider.getSormasToSormasController().syncEvent(event, i),
			(i) -> ControllerProvider.getSormasToSormasController().revokeShare(i, this::reloadList));

		initLayout(
			event.getSormasToSormasOriginInfo(),
			() -> FacadeProvider.getSormasToSormasEventFacade().getAllShares(event.getUuid()),
			shareEnabled ? e -> ControllerProvider.getSormasToSormasController().shareEventFromDetailsPage(event, this) : null,
			(e) -> ControllerProvider.getSormasToSormasController().returnEvent(event));
	}

	public SormasToSormasListComponent(EventParticipantDto eventParticipant, boolean shareEnabled) {
		sormasToSormasList = new SormasToSormasList(
			eventParticipant.getSormasToSormasOriginInfo() == null,
			Captions.sormasToSormasEventParticipantNotShared,
			null,
			null);

		initLayout(
			eventParticipant.getSormasToSormasOriginInfo(),
			() -> FacadeProvider.getSormasToSormasEventFacade().getAllShares(eventParticipant.getEvent().getUuid()),
			null,
			null);
	}

	private void initLayout(
		SormasToSormasOriginInfoDto originInfo,
		ShareDataLoader loadShares,
		Button.ClickListener shareButtonClickListener,
		Button.ClickListener returnButtonClickListener) {
		this.originInfo = originInfo;
		this.loadShares = loadShares;
		this.returnButtonClickListener = returnButtonClickListener;

		setWidth(100, Unit.PERCENTAGE);
		setMargin(false);
		setSpacing(false);

		HorizontalLayout componentHeader = new HorizontalLayout();
		componentHeader.setMargin(false);
		componentHeader.setSpacing(false);
		componentHeader.setWidth(100, Unit.PERCENTAGE);
		addComponent(componentHeader);

		addComponent(sormasToSormasList);
		reloadList();

		Label header = new Label(I18nProperties.getCaption(Captions.sormasToSormasListTitle));
		header.addStyleName(CssStyles.H3);
		componentHeader.addComponent(header);

		if (shareButtonClickListener != null) {
			Button shareButtonButton =
				ButtonHelper.createIconButton(Captions.sormasToSormasShare, VaadinIcons.SHARE, shareButtonClickListener, ValoTheme.BUTTON_PRIMARY);

			componentHeader.addComponent(shareButtonButton);
			componentHeader.setComponentAlignment(shareButtonButton, Alignment.MIDDLE_RIGHT);
		}
	}

	public void reloadList() {
		UI currentUI = UI.getCurrent();
		UserDto currentUser = FacadeProvider.getUserFacade().getCurrentUser();

		sormasToSormasList.showPlaceholder(I18nProperties.getString(Strings.sormasToSormasLoadingShares));

		Thread loadSharesThread = new Thread(() -> {
			try {
				I18nProperties.setUserLanguage(currentUser.getLanguage());
				FacadeProvider.getI18nFacade().setUserLanguage(currentUser.getLanguage());

				currentUI.setPollInterval(300);

				List<SormasToSormasShareTree> shareInfos = loadShares.load();
				String currentServerOrgId = FacadeProvider.getSormasToSormasFacade().getOrganizationId();

				List<SormasToSormasShareInfoDto> shareInfoList = getShareInfoList(shareInfos);
				SormasToSormasShareInfoDto ownerShare = getOwnerShare(shareInfos);
				SormasToSormasOriginInfoDto rootOrigin = findRootOriginInfo(shareInfos);

				String ownerOrganizationId = getOwnerOrganizationId(ownerShare, rootOrigin, currentServerOrgId);
				boolean isOwnedByCurrentOrg = currentServerOrgId.equals(ownerOrganizationId);
				boolean isOwnedByOrigin = originInfo.getOrganizationId().equals(ownerOrganizationId);
				boolean isOwnedByRootOrg = rootOrigin != null && rootOrigin.getOrganizationId().equals(ownerOrganizationId);

				currentUI.access(() -> {
					try {
						// render origin
						if (originInfo != null) {
							HorizontalLayout originLayout = buildSormasOriginInfo(originInfo, isOwnedByOrigin, isOwnedByCurrentOrg);
							originLayout.addStyleName(CssStyles.VSPACE_3);
							addComponent(originLayout, getComponentIndex(sormasToSormasList));
						}

						// render the owner of the entity
						if (!isOwnedByCurrentOrg && !isOwnedByOrigin) {
							AbstractOrderedLayout ownerLayout = null;
							if (isOwnedByRootOrg) {
								ownerLayout = buildSormasOriginInfo(rootOrigin, true, false);
							} else if (ownerShare != null && !ownerShare.getTarget().getUuid().equals(currentServerOrgId)) {
								ownerLayout = buildOwnerShareLayout(ownerShare);
							}

							if (ownerLayout != null) {
								ownerLayout.addStyleName(CssStyles.VSPACE_3);
								addComponent(ownerLayout, getComponentIndex(sormasToSormasList));
							}
						}

						// show shares to other systems then owner and current one
						List<SormasToSormasShareListEntryData> listData = shareInfoList.stream().filter(s -> {
							String shareOrganizationId = s.getTarget().getUuid();

							if (ownerShare != null && shareOrganizationId.equals(ownerShare.getTarget().getUuid())) {
								return false;
							}

							if (originInfo != null && shareOrganizationId.equals(originInfo.getOrganizationId())) {
								return false;
							}

							return !shareOrganizationId.equals(currentServerOrgId);
						}).map(s -> {
							SormasToSormasShareListEntryData entryData = new SormasToSormasShareListEntryData();
							entryData.shareUuid = s.getUuid();
							entryData.target = s.getTarget().getCaption();
							entryData.sender = s.getSender().getShortCaption();
							entryData.status = s.getRequestStatus();
							entryData.creationDate = s.getCreationDate();
							entryData.comment = s.getComment();
							entryData.ownershipHandedOver = s.isOwnershipHandedOver();

							return entryData;
						}).collect(Collectors.toList());

						// add the creator of the entity as a share
						if (!isOwnedByRootOrg
							&& rootOrigin != null
							&& originInfo != null
							&& !rootOrigin.getOrganizationId().equals(originInfo.getOrganizationId())) {
							ServerAccessDataReferenceDto serverAccessDataRef =
								FacadeProvider.getSormasToSormasFacade().getOrganizationRef(rootOrigin.getOrganizationId());

							SormasToSormasShareListEntryData entryData = new SormasToSormasShareListEntryData();
							entryData.shareUuid = null;
							entryData.target = serverAccessDataRef.getCaption();
							entryData.sender = rootOrigin.getSenderName();
							entryData.status = ShareRequestStatus.ACCEPTED;
							entryData.creationDate = rootOrigin.getCreationDate();
							entryData.comment = rootOrigin.getComment();
							entryData.ownershipHandedOver = rootOrigin.isOwnershipHandedOver();

							listData.add(entryData);
						}

						if (shareInfoList.size() > 0) {
							if (listData.size() > 0) {
								Label shareListLabel = new Label(I18nProperties.getCaption(Captions.sormasToSormasSharedWith));
								shareListLabel.addStyleNames(CssStyles.LABEL_BOLD, CssStyles.VSPACE_4);
								addComponent(shareListLabel, getComponentIndex(sormasToSormasList));

								sormasToSormasList.setData(listData);
							} else {
								sormasToSormasList.setVisible(false);
							}
						} else {
							sormasToSormasList.showPlaceholder(null);
						}
					} catch (Exception e) {
						e.printStackTrace();
						sormasToSormasList.showPlaceholder(I18nProperties.getString(Strings.errorSormasToSormasLoadShares));
					} finally {
						currentUI.setPollInterval(-1);
					}

				});
			} catch (Exception e) {
				logger.error(e.getMessage(), e);

				currentUI.setPollInterval(-1);
				currentUI.access(() -> {
					sormasToSormasList.showPlaceholder(I18nProperties.getString(Strings.errorSormasToSormasLoadShares));
				});
			}
		});

		loadSharesThread.start();
	}

	private String getOwnerOrganizationId(SormasToSormasShareInfoDto ownerShare, SormasToSormasOriginInfoDto rootOrigin, String ownOrganizationId) {
		if (ownerShare != null) {
			return ownerShare.getTarget().getUuid();
		}

		if (rootOrigin != null && !rootOrigin.isOwnershipHandedOver()) {
			return rootOrigin.getOrganizationId();
		}

		if (originInfo != null) {
			return originInfo.getOrganizationId();
		}

		return ownOrganizationId;
	}

	private SormasToSormasOriginInfoDto findRootOriginInfo(List<SormasToSormasShareTree> shareInfos) {
		for (SormasToSormasShareTree shareInfo : shareInfos) {
			if (shareInfo.getReShares().size() > 0) {
				return shareInfo.getReShares().get(0).getOrigin();
			}
		}

		return null;
	}

	private List<SormasToSormasShareInfoDto> getShareInfoList(List<SormasToSormasShareTree> shareInfos) {
		return shareInfos.stream()
			.map((s -> Stream.concat(Stream.of(s.getShare()), getShareInfoList(s.getReShares()).stream())))
			.reduce(Stream.empty(), Stream::concat)
			.collect(Collectors.toList());
	}

	private SormasToSormasShareInfoDto getOwnerShare(List<SormasToSormasShareTree> shareInfos) {
		Optional<SormasToSormasShareTree> sharedWithOwnership = shareInfos.stream().filter(s -> {
			SormasToSormasShareInfoDto share = s.getShare();

			return share.getRequestStatus() == ShareRequestStatus.ACCEPTED && share.isOwnershipHandedOver();
		}).findFirst();

		if (sharedWithOwnership.isPresent()) {
			SormasToSormasShareTree ownerTree = sharedWithOwnership.get();
			SormasToSormasShareInfoDto ownerShare = getOwnerShare(ownerTree.getReShares());

			return ownerShare == null ? ownerTree.getShare() : ownerShare;
		}

		return null;
	}

	private SormasToSormasShareInfoDto getShareFromOrigin(List<SormasToSormasShareInfoDto> shareInfoList) {
		if (originInfo != null) {
			return shareInfoList.stream()
				.filter(s -> s.getRequestStatus() == ShareRequestStatus.ACCEPTED && s.getTarget().getUuid().equals(originInfo.getOrganizationId()))
				.findFirst()
				.orElse(null);
		}

		return null;
	}

	private HorizontalLayout buildReturnShareLayout(VerticalLayout infoLayout) {
		HorizontalLayout layout = new HorizontalLayout();
		layout.setMargin(false);
		layout.setWidthFull();

		layout.addComponent(infoLayout);
		layout.setExpandRatio(infoLayout, 1);

		if (returnButtonClickListener != null) {
			layout.addComponent(ButtonHelper.createIconButton(Captions.sormasToSormasReturn, VaadinIcons.REPLY, returnButtonClickListener));
		}

		return layout;
	}

	private VerticalLayout buildOwnerShareLayout(SormasToSormasShareInfoDto shareInfo) {
		VerticalLayout infoLayout = new VerticalLayout();
		infoLayout.setWidth(100, Unit.PERCENTAGE);
		infoLayout.setMargin(false);
		infoLayout.setSpacing(false);
		infoLayout.addStyleName(CssStyles.SORMAS_LIST_ENTRY);

		Label targetLabel = new Label(I18nProperties.getCaption(Captions.sormasToSormasOwnedBy) + " " + shareInfo.getTarget());
		targetLabel.addStyleName(CssStyles.LABEL_BOLD);
		infoLayout.addComponent(targetLabel);

		Label senderLabel = new Label(I18nProperties.getCaption(Captions.sormasToSormasSharedBy) + ": " + shareInfo.getSender().getShortCaption());
		infoLayout.addComponent(senderLabel);

		Label shareDateLabel =
			new Label(I18nProperties.getCaption(Captions.sormasToSormasSharedDate) + ": " + DateFormatHelper.formatDate(shareInfo.getCreationDate()));
		infoLayout.addComponent(shareDateLabel);

		if (!DataHelper.isNullOrEmpty(shareInfo.getComment())) {
			infoLayout.addComponent(new Label(shareInfo.getComment()));
		}

		return infoLayout;
	}

	private HorizontalLayout buildSormasOriginInfo(SormasToSormasOriginInfoDto originInfo, boolean isOwner, boolean canReturn) {
		HorizontalLayout layout = new HorizontalLayout();
		layout.setMargin(false);
		layout.setWidthFull();

		VerticalLayout infoLayout = new VerticalLayout();
		infoLayout.setMargin(false);
		infoLayout.setSpacing(false);

		ServerAccessDataReferenceDto serverAccessDataRef =
			FacadeProvider.getSormasToSormasFacade().getOrganizationRef(originInfo.getOrganizationId());

		Label senderOrganizationLabel = new Label(
			I18nProperties.getCaption(isOwner ? Captions.sormasToSormasOwnedBy : Captions.sormasToSormasSentFrom) + " " + serverAccessDataRef);
		senderOrganizationLabel.addStyleNames(CssStyles.LABEL_BOLD, CssStyles.LABEL_WHITE_SPACE_NORMAL);
		infoLayout.addComponent(senderOrganizationLabel);

		infoLayout.addComponent(new Label(I18nProperties.getCaption(Captions.sormasToSormasSharedBy) + ": " + originInfo.getSenderName()));

		if (originInfo.getSenderEmail() != null) {
			infoLayout.addComponent(new Label(originInfo.getSenderEmail()));
		}

		if (originInfo.getSenderPhoneNumber() != null) {
			infoLayout.addComponent(new Label(originInfo.getSenderPhoneNumber()));
		}

		Label shareDateLabel = new Label(
			I18nProperties.getCaption(Captions.sormasToSormasSharedDate) + ": " + DateFormatHelper.formatDate(originInfo.getCreationDate()));
		infoLayout.addComponent(shareDateLabel);

		if (!DataHelper.isNullOrEmpty(originInfo.getComment())) {
			infoLayout.addComponent(new Label(originInfo.getComment()));
		}

		layout.addComponent(infoLayout);
		layout.setExpandRatio(infoLayout, 1);

		if (canReturn && returnButtonClickListener != null) {
			layout.addComponent(ButtonHelper.createIconButton(Captions.sormasToSormasReturn, VaadinIcons.REPLY, returnButtonClickListener));
		}

		return layout;
	}

	private interface ShareDataLoader {

		List<SormasToSormasShareTree> load() throws Exception;

	}
	private static class SormasToSormasList extends PaginationList<SormasToSormasShareListEntryData> {

		private static final long serialVersionUID = -4659924105492791566L;
		private String defaultPlaceHolderText;
		private final Label placeholderLabel;
		private final Consumer<String> syncListener;
		private final Consumer<String> revokeListener;

		public SormasToSormasList(
			boolean showPlaceholder,
			String placeholderCaptionTag,
			Consumer<String> syncListener,
			Consumer<String> revokeListener) {
			super(5);

			this.defaultPlaceHolderText = placeholderCaptionTag != null ? I18nProperties.getCaption(placeholderCaptionTag) : null;
			this.placeholderLabel = new Label(defaultPlaceHolderText);
			this.placeholderLabel.setVisible(showPlaceholder);
			this.syncListener = syncListener;
			this.revokeListener = revokeListener;
		}

		@Override
		public void reload() {
		}

		public void setData(List<SormasToSormasShareListEntryData> data) {

			setEntries(data);

			listLayout.removeComponent(placeholderLabel);
			showPage(1);
		}

		public void showPlaceholder(String placeholderText) {
			setEntries(Collections.emptyList());
			updatePaginationLayout();

			placeholderLabel.setValue(placeholderText == null ? defaultPlaceHolderText : placeholderText);
			listLayout.addComponent(placeholderLabel);
			placeholderLabel.setVisible(true);
		}

		@Override
		protected void drawDisplayedEntries() {
			List<SormasToSormasShareListEntryData> displayedEntries = getDisplayedEntries();

			for (int i = 0; i < displayedEntries.size(); i++) {
				SormasToSormasShareListEntryData shareInfo = displayedEntries.get(i);
				SormasToSormasShareListEntry listEntry = new SormasToSormasShareListEntry(shareInfo, syncListener, revokeListener);
				if (i == 0) {
					listEntry.addStyleName(CssStyles.SORMAS_LIST_ENTRY_NO_BORDER);
				}
				listLayout.addComponent(listEntry);
			}
		}

	}
	private static class SormasToSormasShareListEntry extends HorizontalLayout {

		private static final long serialVersionUID = 7462585357530141263L;

		public SormasToSormasShareListEntry(SormasToSormasShareListEntryData data, Consumer<String> syncListener, Consumer<String> revokeListener) {
			setMargin(false);
			setSpacing(true);
			setWidth(100, Unit.PERCENTAGE);
			addStyleName(CssStyles.SORMAS_LIST_ENTRY);

			VerticalLayout infoLayout = new VerticalLayout();
			infoLayout.setWidth(100, Unit.PERCENTAGE);
			infoLayout.setMargin(false);
			infoLayout.setSpacing(false);

			Label targetLabel = new Label(data.target);
			targetLabel.addStyleName(CssStyles.LABEL_BOLD);
			infoLayout.addComponent(targetLabel);

			Label senderLabel = new Label(I18nProperties.getCaption(Captions.sormasToSormasSharedBy) + ": " + data.sender);
			infoLayout.addComponent(senderLabel);

			if (FacadeProvider.getFeatureConfigurationFacade().isFeatureEnabled(FeatureType.SORMAS_TO_SORMAS_ACCEPT_REJECT)) {
				Label statusLabel = new Label(I18nProperties.getCaption(Captions.SormasToSormasShareRequest_status) + ": " + data.status);
				infoLayout.addComponent(statusLabel);
			}

			Label shareDateLabel =
				new Label(I18nProperties.getCaption(Captions.sormasToSormasSharedDate) + ": " + DateFormatHelper.formatDate(data.creationDate));
			infoLayout.addComponent(shareDateLabel);

			if (!DataHelper.isNullOrEmpty(data.comment)) {
				infoLayout.addComponent(new Label(data.comment));
			}

			addComponent(infoLayout);
			setExpandRatio(infoLayout, 1);

			if (syncListener != null && data.shareUuid != null && data.status == ShareRequestStatus.ACCEPTED && !data.ownershipHandedOver) {
				addComponent(ButtonHelper.createIconButton(Captions.sormasToSormasSync, VaadinIcons.REFRESH, (e) -> {
					syncListener.accept(data.shareUuid);
				}));
			}

			if (FacadeProvider.getFeatureConfigurationFacade().isFeatureEnabled(FeatureType.SORMAS_TO_SORMAS_ACCEPT_REJECT)
				&& revokeListener != null
				&& data.shareUuid != null
				&& data.status == ShareRequestStatus.PENDING
				&& !data.ownershipHandedOver) {
				addComponent(ButtonHelper.createIconButton(Captions.sormasToSormasRevokeShare, VaadinIcons.TRASH, (e) -> {
					revokeListener.accept(data.shareUuid);
				}));
			}

		}

	}

	private static class SormasToSormasShareListEntryData {

		private String shareUuid;
		private String target;
		private String sender;
		private ShareRequestStatus status;
		private Date creationDate;
		private String comment;
		private Boolean ownershipHandedOver;
	}
}
