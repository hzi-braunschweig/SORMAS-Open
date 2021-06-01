package de.symeda.sormas.ui.contact.components.linelisting;

import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.themes.ValoTheme;

import de.symeda.sormas.api.caze.CaseReferenceDto;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.ui.ControllerProvider;
import de.symeda.sormas.ui.utils.ButtonHelper;
import de.symeda.sormas.ui.utils.CssStyles;

public class CaseSelector extends CustomField<CaseReferenceDto> {

	private CaseReferenceDto selectedCaseValue;

	@Override
	protected Component initContent() {
		HorizontalLayout layout = new HorizontalLayout();

		Label caseInfoLabel = new Label(I18nProperties.getString(Strings.infoNoSourceCaseSelected), ContentMode.HTML);
		Button chooseCaseButton = ButtonHelper.createButton(Captions.contactChooseCase, null, ValoTheme.BUTTON_PRIMARY, CssStyles.VSPACE_2);
		Button removeCaseButton = ButtonHelper.createButton(Captions.contactRemoveCase, null, ValoTheme.BUTTON_LINK);

		CssStyles.style(caseInfoLabel, CssStyles.VSPACE_TOP_4);
		layout.addComponent(caseInfoLabel);

		chooseCaseButton.addClickListener(e -> ControllerProvider.getContactController().openSelectCaseForContactWindow(null, selectedCase -> {
			if (selectedCase != null) {
				setValue(selectedCase.toReference());
				caseInfoLabel.setValue(
					String.format(
						I18nProperties.getString(Strings.infoContactCreationSourceCase),
						selectedCase.getPersonFirstName() + " " + selectedCase.getPersonLastName() + " " + "("
							+ DataHelper.getShortUuid(selectedCase.getUuid()) + ")"));
				caseInfoLabel.removeStyleName(CssStyles.VSPACE_TOP_4);
				removeCaseButton.setVisible(true);
				chooseCaseButton.setCaption(I18nProperties.getCaption(Captions.contactChangeCase));
			}
		}));
		layout.addComponent(chooseCaseButton);

		removeCaseButton.addClickListener(e -> {
			setValue(null);
			caseInfoLabel.setValue(I18nProperties.getString(Strings.infoNoSourceCaseSelected));
			caseInfoLabel.addStyleName(CssStyles.VSPACE_TOP_4);
			removeCaseButton.setVisible(false);
			chooseCaseButton.setCaption(I18nProperties.getCaption(Captions.contactChooseCase));
		});
		layout.addComponent(removeCaseButton);
		removeCaseButton.setVisible(false);

		return layout;
	}

	@Override
	protected void doSetValue(CaseReferenceDto caseReferenceDto) {
		this.selectedCaseValue = caseReferenceDto;
	}

	@Override
	public CaseReferenceDto getValue() {
		return selectedCaseValue;
	}
}