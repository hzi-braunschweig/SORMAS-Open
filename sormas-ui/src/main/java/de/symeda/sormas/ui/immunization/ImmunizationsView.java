package de.symeda.sormas.ui.immunization;

import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.VerticalLayout;

import de.symeda.sormas.api.immunization.ImmunizationCriteria;
import de.symeda.sormas.ui.ViewModelProviders;
import de.symeda.sormas.ui.immunization.components.ImmunizationFilterForm;
import de.symeda.sormas.ui.immunization.components.ImmunizationGrid;
import de.symeda.sormas.ui.utils.AbstractView;

public class ImmunizationsView extends AbstractView {

	public static final String VIEW_NAME = "immunizations";

	private final ImmunizationCriteria criteria;
	private final ImmunizationGrid grid;

	private final ImmunizationFilterForm filterForm;

	public ImmunizationsView() {
		super(VIEW_NAME);

		criteria = ViewModelProviders.of(ImmunizationsView.class).get(ImmunizationCriteria.class);
		grid = new ImmunizationGrid(criteria);

		final VerticalLayout gridLayout = new VerticalLayout();
		filterForm = new ImmunizationFilterForm();
		gridLayout.addComponent(filterForm);
		gridLayout.addComponent(grid);

		gridLayout.setMargin(true);
		gridLayout.setSpacing(false);
		gridLayout.setSizeFull();
		gridLayout.setExpandRatio(grid, 1);
		gridLayout.setStyleName("crud-main-layout");

		addComponent(gridLayout);
	}

	@Override
	public void enter(ViewChangeListener.ViewChangeEvent event) {

	}
}
