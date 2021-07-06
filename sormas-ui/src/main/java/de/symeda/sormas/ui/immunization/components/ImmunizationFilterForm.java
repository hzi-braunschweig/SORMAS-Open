package de.symeda.sormas.ui.immunization.components;

import com.vaadin.v7.ui.ComboBox;

import de.symeda.sormas.api.immunization.ImmunizationCriteria;
import de.symeda.sormas.api.immunization.ImmunizationDto;
import de.symeda.sormas.api.immunization.ImmunizationIndexDto;
import de.symeda.sormas.ui.utils.AbstractFilterForm;
import de.symeda.sormas.ui.utils.FieldConfiguration;

public class ImmunizationFilterForm extends AbstractFilterForm<ImmunizationCriteria> {

	public ImmunizationFilterForm() {
		super(ImmunizationCriteria.class, ImmunizationIndexDto.I18N_PREFIX);
	}

	@Override
	protected String[] getMainFilterLocators() {
		return new String[] {
			ImmunizationDto.DISEASE };
	}

	@Override
	protected void addFields() {
		addField(FieldConfiguration.pixelSized(ImmunizationDto.DISEASE, 140), ComboBox.class);
	}
}
