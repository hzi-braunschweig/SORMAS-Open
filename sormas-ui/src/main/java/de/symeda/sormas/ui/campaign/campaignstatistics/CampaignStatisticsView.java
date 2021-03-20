package de.symeda.sormas.ui.campaign.campaignstatistics;

import static de.symeda.sormas.ui.utils.FilteredGrid.EDIT_BTN_ID;

import java.util.List;
import java.util.function.Consumer;

import org.vaadin.hene.popupbutton.PopupButton;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.ui.OptionGroup;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.Language;
import de.symeda.sormas.api.campaign.data.CampaignFormDataCriteria;
import de.symeda.sormas.api.campaign.data.CampaignFormElementImportance;
import de.symeda.sormas.api.campaign.data.translation.TranslationElement;
import de.symeda.sormas.api.campaign.form.CampaignFormElement;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaDto;
import de.symeda.sormas.api.campaign.form.CampaignFormMetaReferenceDto;
import de.symeda.sormas.api.campaign.form.CampaignFormTranslations;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.user.UserDto;
import de.symeda.sormas.api.user.UserRight;
import de.symeda.sormas.ui.UserProvider;
import de.symeda.sormas.ui.ViewModelProviders;
import de.symeda.sormas.ui.campaign.AbstractCampaignView;
import de.symeda.sormas.ui.campaign.campaigndata.CampaignDataView;
import de.symeda.sormas.ui.campaign.components.CampaignSelector;
import de.symeda.sormas.ui.utils.ButtonHelper;
import de.symeda.sormas.ui.utils.CssStyles;
import de.symeda.sormas.ui.utils.ExportEntityName;
import de.symeda.sormas.ui.utils.GridExportStreamResource;

public class CampaignStatisticsView extends AbstractCampaignView {

	public static final String VIEW_NAME = ROOT_VIEW_NAME + "/campaignstatistics";

	private final CampaignSelector campaignLayout;
	private final CampaignFormDataCriteria criteria;
	private final CampaignStatisticsGrid grid;

	private CampaignStatisticsFilterForm filterForm;
	protected OptionGroup campaignFormElementImportance;

	public static final String ONLY_IMPORTANT_FORM_ELEMENTS = "onlyImportantFormElements";

	public CampaignStatisticsView() {
		super(VIEW_NAME);

		campaignLayout = new CampaignSelector();
		addHeaderComponent(campaignLayout);

		criteria = ViewModelProviders.of(getClass()).get(CampaignFormDataCriteria.class);
		grid = new CampaignStatisticsGrid(criteria);

		if (UserProvider.getCurrent().hasUserRight(UserRight.CAMPAIGN_FORM_DATA_EXPORT)) {
			VerticalLayout exportLayout = new VerticalLayout();
			{
				exportLayout.setSpacing(true);
				exportLayout.setMargin(true);
				exportLayout.addStyleName(CssStyles.LAYOUT_MINIMAL);
				exportLayout.setWidth(250, Unit.PIXELS);
			}

			PopupButton exportPopupButton = ButtonHelper.createIconPopupButton(Captions.export, VaadinIcons.DOWNLOAD, exportLayout);
			addHeaderComponent(exportPopupButton);
			{
				StreamResource streamResource = GridExportStreamResource.createStreamResource(grid, ExportEntityName.CAMPAIGN_DATA, EDIT_BTN_ID);
				addExportButton(streamResource, exportPopupButton, exportLayout, VaadinIcons.TABLE, Captions.export, Strings.infoBasicExport);
			}
		}

		VerticalLayout mainLayout = new VerticalLayout();
		HorizontalLayout filtersLayout = new HorizontalLayout();

		filtersLayout.setWidthFull();
		filtersLayout.setMargin(false);
		filtersLayout.setSpacing(true);

		CampaignStatisticsFilterForm filterBar = createFilterBar();
		filtersLayout.addComponent(filterBar);
		filtersLayout.setComponentAlignment(filterBar, Alignment.TOP_LEFT);
		filtersLayout.setExpandRatio(filterBar, 0.8f);

		createImportanceFilterSwitch();
		filtersLayout.addComponent(campaignFormElementImportance);
		filtersLayout.setComponentAlignment(campaignFormElementImportance, Alignment.TOP_RIGHT);
		filtersLayout.setExpandRatio(campaignFormElementImportance, 0.2f);

		mainLayout.addComponent(filtersLayout);

		filterForm.getField(CampaignFormDataCriteria.CAMPAIGN_FORM_META).addValueChangeListener(e -> {
			Object value = e.getProperty().getValue();
			campaignFormElementImportance.setVisible(value != null);
		});

		campaignFormElementImportance.addValueChangeListener(e -> {
			grid.reload();
			createFormMetaChangedCallback()
				.accept((CampaignFormMetaReferenceDto) filterForm.getField(CampaignFormDataCriteria.CAMPAIGN_FORM_META).getValue());
		});

		mainLayout.addComponent(grid);
		mainLayout.setMargin(true);
		mainLayout.setSpacing(false);
		mainLayout.setSizeFull();
		mainLayout.setExpandRatio(grid, 1);
		mainLayout.setStyleName("crud-main-layout");

		addComponent(mainLayout);
	}

	private void createImportanceFilterSwitch() {

		campaignFormElementImportance = new OptionGroup();
		CssStyles.style(campaignFormElementImportance, ValoTheme.OPTIONGROUP_HORIZONTAL, CssStyles.OPTIONGROUP_HORIZONTAL_PRIMARY);
		campaignFormElementImportance.setId(ONLY_IMPORTANT_FORM_ELEMENTS);
		campaignFormElementImportance.addItem(CampaignFormElementImportance.IMPORTANT);
		campaignFormElementImportance
			.setItemCaption(CampaignFormElementImportance.IMPORTANT, I18nProperties.getEnumCaption(CampaignFormElementImportance.IMPORTANT));
		campaignFormElementImportance.addItem(CampaignFormElementImportance.ALL);
		campaignFormElementImportance
			.setItemCaption(CampaignFormElementImportance.ALL, I18nProperties.getEnumCaption(CampaignFormElementImportance.ALL));

		campaignFormElementImportance.setValue(CampaignFormElementImportance.ALL);
		campaignFormElementImportance.setVisible(false);
	}

	public CampaignStatisticsFilterForm createFilterBar() {
		final UserDto user = UserProvider.getCurrent().getUser();
		criteria.setRegion(user.getRegion());
		criteria.setDistrict(user.getDistrict());
		criteria.setCommunity(user.getCommunity());
		filterForm = new CampaignStatisticsFilterForm();
		filterForm.addValueChangeListener(e -> {
			if (!filterForm.hasFilter() && campaignLayout == null) {
				navigateTo(null);
			}
		});
		filterForm.addResetHandler(e -> {
			ViewModelProviders.of(CampaignDataView.class).remove(CampaignFormDataCriteria.class);
			navigateTo(null, true);
		});
		filterForm.addApplyHandler(e -> {
			criteria.setCampaign(campaignLayout.getValue());
			grid.reload();
		});
		campaignLayout.addValueChangeListener(e -> {
			criteria.setCampaign(campaignLayout.getValue());
			grid.reload();
		});
		filterForm.setFormMetaChangedCallback(createFormMetaChangedCallback());

		return filterForm;
	}

	private Consumer<CampaignFormMetaReferenceDto> createFormMetaChangedCallback() {
		return formMetaReference -> {
			grid.removeAllColumns();
			grid.addDefaultColumns();
			if (formMetaReference != null) {
				CampaignFormMetaDto formMeta = FacadeProvider.getCampaignFormMetaFacade().getCampaignFormMetaByUuid(formMetaReference.getUuid());
				Language userLanguage = UserProvider.getCurrent().getUser().getLanguage();
				CampaignFormTranslations translations = null;
				if (userLanguage != null) {
					translations = formMeta.getCampaignFormTranslations()
						.stream()
						.filter(t -> t.getLanguageCode().equals(userLanguage.getLocale().toString()))
						.findFirst()
						.orElse(null);
				}
				final boolean onlyImportantFormElements = CampaignFormElementImportance.IMPORTANT.equals(campaignFormElementImportance.getValue());
				final List<CampaignFormElement> campaignFormElements = formMeta.getCampaignFormElements();
				for (CampaignFormElement element : campaignFormElements) {
					if (element.isImportant() || !onlyImportantFormElements) {
						String caption = null;
						if (translations != null) {
							caption = translations.getTranslations()
								.stream()
								.filter(t -> t.getElementId().equals(element.getId()))
								.map(TranslationElement::getCaption)
								.findFirst()
								.orElse(null);
						}
						if (caption == null) {
							caption = element.getCaption();
						}
					}
				}
			}
		};
	}
}
