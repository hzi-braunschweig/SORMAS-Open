package de.symeda.sormas.app.component.validation;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import android.view.View;

import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Validations;
import de.symeda.sormas.api.utils.DateHelper;
import de.symeda.sormas.app.component.controls.ControlDateField;
import de.symeda.sormas.app.component.controls.ControlTextEditField;
import de.symeda.sormas.app.util.ResultCallback;

public class ValidationHelper {

	public static void initDateIntervalValidator(ControlDateField dateFromControl, ControlDateField dateUntilControl) {
		initDateIntervalValidator(dateFromControl, dateUntilControl, true);
	}

	public static void initDateIntervalValidator(
		ControlDateField dateFromControl,
		ControlDateField dateUntilControl,
		boolean doNotSetForHiddenFields) {
		if (doNotSetForHiddenFields && (dateFromControl.getVisibility() == View.GONE || dateUntilControl.getVisibility() == View.GONE)) {
			return;
		}

		ResultCallback<Boolean> dateFromUntilCallback = () -> {
			Date dateFromValue = dateFromControl.getValue();
			Date dateUntilValue = dateUntilControl.getValue();
			if (dateFromValue != null && dateUntilValue != null) {
				if (DateHelper.getStartOfDay(dateFromValue).after(DateHelper.getStartOfDay(dateUntilValue))) {
					dateFromControl.enableErrorState(
						I18nProperties.getValidationError(Validations.beforeDate, dateFromControl.getCaption(), dateUntilControl.getCaption()));
					dateUntilControl.enableErrorState(
						I18nProperties.getValidationError(Validations.afterDate, dateUntilControl.getCaption(), dateFromControl.getCaption()));
					return true;
				} else {
					dateFromControl.disableErrorState();
					dateUntilControl.disableErrorState();
				}
			}

			return false;
		};

		dateFromControl.setValidationCallback(dateFromUntilCallback);
		dateUntilControl.setValidationCallback(dateFromUntilCallback);
	}

	public static void initIntegerValidator(ControlTextEditField textEditField, String errorMessage, int min, int max) {
		textEditField.setValidationCallback(() -> {
			String vaccinationDosesValue = textEditField.getValue();
			if (!StringUtils.isEmpty(vaccinationDosesValue)) {
				try {
					int intValue = Integer.parseInt(vaccinationDosesValue);
					if (intValue < min || intValue > max) {
						textEditField.enableErrorState(errorMessage);

						return true;
					}
				} catch (NumberFormatException e) {
					textEditField.enableErrorState(errorMessage);

					return true;
				}
			}

			return false;
		});
	}
}
