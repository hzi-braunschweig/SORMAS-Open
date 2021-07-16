package de.symeda.sormas.ui.configuration.infrastructure;

import org.apache.commons.lang3.StringUtils;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.region.CountryCriteria;
import de.symeda.sormas.api.region.CountryDto;
import de.symeda.sormas.api.region.CountryIndexDto;
import de.symeda.sormas.api.user.UserRight;
import de.symeda.sormas.ui.ControllerProvider;
import de.symeda.sormas.ui.UserProvider;
import de.symeda.sormas.ui.ViewModelProviders;
import de.symeda.sormas.ui.utils.FilteredGrid;
import de.symeda.sormas.ui.utils.ViewConfiguration;

public class CountriesGrid extends FilteredGrid<CountryIndexDto, CountryCriteria> {

	private static final long serialVersionUID = -8192499609737564649L;

	public CountriesGrid(CountryCriteria criteria) {
		super(CountryIndexDto.class);

		setSizeFull();

		ViewConfiguration viewConfiguration = ViewModelProviders.of(CountriesView.class).get(ViewConfiguration.class);
		setInEagerMode(viewConfiguration.isInEagerMode());

		super.setCriteria(criteria, true);
		if (isInEagerMode() && UserProvider.getCurrent().hasUserRight(UserRight.PERFORM_BULK_OPERATIONS)) {
			setSelectionMode(SelectionMode.MULTI);
		} else {
			setSelectionMode(SelectionMode.NONE);
		}

		setColumns(
			CountryIndexDto.ISO_CODE,
			CountryIndexDto.DISPLAY_NAME,
			CountryIndexDto.SUBCONTINENT,
			CountryIndexDto.EXTERNAL_ID,
			CountryIndexDto.UNO_CODE,
			CountryIndexDto.DEFAULT_NAME);
		getColumn(CountryIndexDto.DEFAULT_NAME).setHidden(true);

		if (UserProvider.getCurrent().hasUserRight(UserRight.INFRASTRUCTURE_EDIT)) {
			addEditColumn(e -> ControllerProvider.getInfrastructureController().editCountry(e.getUuid()));
		}

		for (Column<?, ?> column : getColumns()) {
			column.setCaption(I18nProperties.getPrefixCaption(CountryDto.I18N_PREFIX, column.getId(), column.getCaption()));
		}
	}

	public void reload() {
		String nameCodeLike = getCriteria().getNameCodeLike();
		this.setItems(
			FacadeProvider.getCountryFacade()
				.getIndexList(getCriteria().nameCodeLike(null), null, null, null)
				.stream()
				.filter(
					country -> (StringUtils.isEmpty(nameCodeLike)
						|| country.getDefaultName().toLowerCase().contains(nameCodeLike)
						|| country.getDisplayName().toLowerCase().contains(nameCodeLike)
						|| country.getIsoCode().toLowerCase().contains(nameCodeLike)
						|| country.getUnoCode().toLowerCase().contains(nameCodeLike))));

		getCriteria().nameCodeLike(nameCodeLike);
		setSelectionMode(isInEagerMode() ? SelectionMode.MULTI : SelectionMode.NONE);
	}

}
