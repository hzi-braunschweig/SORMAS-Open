package de.symeda.sormas.ui.immunization.components;

import java.util.Date;
import java.util.stream.Collectors;

import com.vaadin.data.provider.DataProvider;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.ui.renderers.DateRenderer;
import com.vaadin.ui.renderers.TextRenderer;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.Language;
import de.symeda.sormas.api.caze.AgeAndBirthDateDto;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.immunization.ImmunizationCriteria;
import de.symeda.sormas.api.immunization.ImmunizationIndexDto;
import de.symeda.sormas.api.person.PersonHelper;
import de.symeda.sormas.api.utils.DateHelper;
import de.symeda.sormas.api.utils.SortProperty;
import de.symeda.sormas.ui.ControllerProvider;
import de.symeda.sormas.ui.utils.FieldAccessColumnStyleGenerator;
import de.symeda.sormas.ui.utils.FilteredGrid;
import de.symeda.sormas.ui.utils.ShowDetailsListener;
import de.symeda.sormas.ui.utils.UuidRenderer;

public class ImmunizationGrid extends FilteredGrid<ImmunizationIndexDto, ImmunizationCriteria> {

	public ImmunizationGrid(ImmunizationCriteria criteria) {
		super(ImmunizationIndexDto.class);
		setSizeFull();
		setLazyDataProvider();
		setCriteria(criteria);
		initColumns();
	}

	private void initColumns() {

		Language userLanguage = I18nProperties.getUserLanguage();
		setColumns(
			ImmunizationIndexDto.UUID,
			ImmunizationIndexDto.PERSON_UUID,
			ImmunizationIndexDto.FIRST_NAME,
			ImmunizationIndexDto.LAST_NAME,
			ImmunizationIndexDto.AGE_AND_BIRTH_DATE,
			ImmunizationIndexDto.SEX,
			ImmunizationIndexDto.DISTRICT,
			ImmunizationIndexDto.MEANS_OF_IMMUNIZATION,
			ImmunizationIndexDto.IMMUNIZATION_MANAGEMENT_STATUS,
			ImmunizationIndexDto.IMMUNIZATION_STATUS,
			ImmunizationIndexDto.START_DATE,
			ImmunizationIndexDto.END_DATE,
			ImmunizationIndexDto.RECOVERY_DATE);

		((Column<ImmunizationIndexDto, String>) getColumn(ImmunizationIndexDto.UUID)).setRenderer(new UuidRenderer());
		((Column<ImmunizationIndexDto, String>) getColumn(ImmunizationIndexDto.PERSON_UUID)).setRenderer(new UuidRenderer());
		((Column<ImmunizationIndexDto, AgeAndBirthDateDto>) getColumn(ImmunizationIndexDto.AGE_AND_BIRTH_DATE)).setRenderer(
			value -> value == null
				? ""
				: PersonHelper.getAgeAndBirthdateString(
					value.getAge(),
					value.getAgeType(),
					value.getDateOfBirthDD(),
					value.getDateOfBirthMM(),
					value.getDateOfBirthYYYY(),
					I18nProperties.getUserLanguage()),
			new TextRenderer());
		((Column<ImmunizationIndexDto, Date>) getColumn(ImmunizationIndexDto.START_DATE))
			.setRenderer(new DateRenderer(DateHelper.getLocalDateTimeFormat(userLanguage)));
		((Column<ImmunizationIndexDto, Date>) getColumn(ImmunizationIndexDto.END_DATE))
			.setRenderer(new DateRenderer(DateHelper.getLocalDateTimeFormat(userLanguage)));
		((Column<ImmunizationIndexDto, Date>) getColumn(ImmunizationIndexDto.RECOVERY_DATE))
			.setRenderer(new DateRenderer(DateHelper.getLocalDateTimeFormat(userLanguage)));

		for (Column<ImmunizationIndexDto, ?> column : getColumns()) {
			column.setCaption(I18nProperties.findPrefixCaptionWithDefault(column.getId(), column.getCaption(), ImmunizationIndexDto.I18N_PREFIX));
			column.setStyleGenerator(FieldAccessColumnStyleGenerator.getDefault(getBeanType(), column.getId()));
		}

		addItemClickListener(
			new ShowDetailsListener<>(
				ImmunizationIndexDto.UUID,
				e -> ControllerProvider.getImmunizationController().navigateToImmunization(e.getUuid())));
		addItemClickListener(
			new ShowDetailsListener<>(ImmunizationIndexDto.PERSON_UUID, e -> ControllerProvider.getPersonController().navigateToPerson(e.getUuid())));
	}

	public void setLazyDataProvider() {
		DataProvider<ImmunizationIndexDto, ImmunizationCriteria> dataProvider = DataProvider.fromFilteringCallbacks(
			query -> FacadeProvider.getImmunizationFacade()
				.getIndexList(
					query.getFilter().orElse(null),
					query.getOffset(),
					query.getLimit(),
					query.getSortOrders()
						.stream()
						.map(sortOrder -> new SortProperty(sortOrder.getSorted(), sortOrder.getDirection() == SortDirection.ASCENDING))
						.collect(Collectors.toList()))
				.stream(),
			query -> (int) FacadeProvider.getImmunizationFacade().count(query.getFilter().orElse(null)));
		setDataProvider(dataProvider);
		setSelectionMode(SelectionMode.NONE);
	}

	public void reload() {
		getDataProvider().refreshAll();
	}
}
