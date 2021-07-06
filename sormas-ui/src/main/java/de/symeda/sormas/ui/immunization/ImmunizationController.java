package de.symeda.sormas.ui.immunization;

import com.vaadin.navigator.Navigator;

import de.symeda.sormas.ui.SormasUI;

public class ImmunizationController {

	public void registerViews(Navigator navigator) {

	}

	public void navigateToImmunization(String uuid) {
		final String navigationState = ImmunizationsView.VIEW_NAME + "/" + uuid;
		SormasUI.get().getNavigator().navigateTo(navigationState);
	}
}
